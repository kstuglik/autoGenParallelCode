package pl.edu.agh.utils;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.ConstantFieldref;
import org.apache.bcel.generic.*;
import pl.edu.agh.transformations.LaunchProperties;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static pl.edu.agh.utils.MethodUtils.getBodyFromMethodToNewInstructionList;

public class TransformUtils {

    public static void addThreadPoolExecutorService(ClassGen cg) {

        Optional<MethodGen> classInitMethod = MethodUtils.findMethodByName(cg, Const.STATIC_INITIALIZER_NAME);
        ConstantPoolGen cp = cg.getConstantPool();
        addClassFields_old(cg, cp);
        InstructionList il_new = new InstructionList();

        classInitMethod.ifPresent(init -> {
            il_new.append(init.getInstructionList());
            try {
                il_new.delete(il_new.getEnd());
            } catch (TargetLostException e) {
                e.printStackTrace();
            }
            retargetStaticPuts(cg, il_new);
        });

        InstructionFactory _factory = new InstructionFactory(cg, cp);

        String className = cg.getClassName();

        appendFieldsInstructions(il_new, _factory, className);

        MethodGen mg = new MethodGen(Const.ACC_STATIC,
                Type.VOID,
                Type.NO_ARGS,
                new String[0],
                Const.STATIC_INITIALIZER_NAME,
                className,
                il_new,
                cp);

//        _mg.stripAttributes(true);
        mg.setMaxLocals();
        mg.setMaxStack();
        cg.replaceMethod(mg.getMethod(), mg.getMethod());
        il_new.dispose();
    }

//  addClassFields_old - add thread fields in class

    public static void addClassFields_old(ClassGen cg, ConstantPoolGen cp) {
        FieldGen threadCount = new FieldGen(
                Const.ACC_PUBLIC | Const.ACC_STATIC | Const.ACC_FINAL,
                Type.INT,
                LaunchProperties.NUMBER_OF_THREADS_NAME,
                cp);
        FieldGen service = new FieldGen(
                Const.ACC_PUBLIC | Const.ACC_STATIC,
                Type.getType(ExecutorService.class),
                LaunchProperties.EXECUTOR_SERVICE_NAME,
                cp);
        cg.addField(threadCount.getField());
        cg.addField(service.getField());
    }

    public static void addClassFields(ClassGen cg, ConstantPoolGen cp, Type type, String fieldName) {
        FieldGen newField = new FieldGen(Const.ACC_PUBLIC | Const.ACC_STATIC, type, fieldName, cp);
        cg.addField(newField.getField());
    }

    static private void deleteIns(InstructionList il, InstructionHandle ih, InstructionHandle new_target) {
        // System.out.println("deleteIns: instructionList = " + il);
        // System.out.println("   handle = " + ih);
        try {
            il.delete(ih);
        } catch (TargetLostException e) {
            InstructionHandle[] targets = e.getTargets();
            for (InstructionHandle target : targets) {
                InstructionTargeter[] targeters = target.getTargeters();

                for (InstructionTargeter targeter : targeters) {
                    targeter.updateTarget(target, new_target);
                }
            }
        }
    }

    private static void injectJcmMultiply(MethodGen mg, ConstantPoolGen cp, InstructionFactory factory, InstructionList il_new) {
        int id_A, id_B, jcm_ID, id;

        id_A = New.CreateArrayField("AA", mg, il_new, cp, Type.INT, 2, new int[]{2, 2});
        id_B = New.CreateArrayField("BB", mg, il_new, cp, Type.INT, 2, new int[]{2, 2});

        jcm_ID = New.CreateObjectClass("jcm", "utils.JCudaMatrix", il_new, factory, mg, new int[]{id_A, id_B});
        il_new.append(new ALOAD(jcm_ID));

        il_new.append(factory.createInvoke("utils.JCudaMatrix", "multiply", new ArrayType(Type.FLOAT, 1), new Type[]{}, Const.INVOKEVIRTUAL));
        LocalVariableGen lg = mg.addLocalVariable("CC", new ArrayType(Type.FLOAT, 1), null, null);

        id = lg.getIndex();
        il_new.append(new ASTORE(id));
        New.PrintArray(il_new, mg, factory, id, false);
    }

    private static void injectJcmMultiplyAB(ClassGen cg, MethodGen mg, ConstantPoolGen cp, InstructionFactory factory, InstructionList il_new) {
        int id_A, id_B, jcm_ID, id;

        id_A = LocalVariableUtils.findLocalVariableByName(LaunchProperties.ARRAY_1, mg.getLocalVariableTable(cp)).getIndex();
        id_B = LocalVariableUtils.findLocalVariableByName(LaunchProperties.ARRAY_2, mg.getLocalVariableTable(cp)).getIndex();


        jcm_ID = New.CreateObjectClass("jcm", "utils.JCudaMatrix", il_new, factory, mg, new int[]{id_A, id_B});
        il_new.append(new ALOAD(jcm_ID));

        il_new.append(factory.createInvoke("utils.JCudaMatrix", "multiply", new ArrayType(Type.FLOAT, 1), new Type[]{}, Const.INVOKEVIRTUAL));
        LocalVariableGen lg = mg.addLocalVariable("CC", new ArrayType(Type.FLOAT, 1), null, null);

        id = lg.getIndex();
        il_new.append(new ASTORE(id));
        New.PrintArray(il_new, mg, factory, id, true);
    }

    private static void injectPrintArrName(ClassGen cg, MethodGen mg, InstructionFactory factory, InstructionList il_new) {
        int id = ConstantPoolUtils.getFieldIndex(cg, "C");
        New.PrintArray(il_new, mg, factory, id, false);
    }

    private static void injectPrintVarName(MethodGen mg, ConstantPoolGen cp, InstructionFactory factory, InstructionList il_new) {
        int id = New.getLoacalVariableID("luckyNumber", cp, mg);
        il_new.append(factory.createFieldAccess("java.lang.System", "out",
                new ObjectType("java.io.PrintStream"), Const.GETSTATIC));
        il_new.append(new DUP());
        il_new.append(new ALOAD(id));
        il_new.append(factory.createInvoke("java.io.PrintStream", "println",
                Type.VOID, new Type[]{Type.STRING}, Const.INVOKEVIRTUAL));
    }

    public static void printSomething(InstructionList il_new, InstructionFactory factory, ConstantPoolGen cp) {
        il_new.append(factory.createFieldAccess(
                "java.lang.System", "out",
                new ObjectType("java.io.PrintStream"), Const.GETSTATIC));
        il_new.append(InstructionConst.DUP);
        il_new.append(new PUSH(cp, "Hello, "));
        il_new.append(factory.createInvoke("java.io.PrintStream", "println",
                Type.VOID, new Type[]{Type.STRING}, Const.INVOKEVIRTUAL));
    }

/*CHECK IN FUTURE: id for field or variable
        int id = ConstantPoolUtils.getFieldIndex(cg,"jcm");
        int id = New.getLoacalVariableID("jcm",cp,mg);
        forLoop[3].setInstruction(new GETSTATIC(numThreadsConstantIndex));*/

    private static void retargetStaticPuts(ClassGen cg, InstructionList il) {
        int classNameIndex = cg.getClassNameIndex();
        Arrays.stream(il.getInstructionHandles())
                .filter(handle -> handle.getInstruction() instanceof PUTSTATIC)
                .forEach(handle -> retargetSingleHandle(cg.getConstantPool(), handle, classNameIndex));
    }

    private static void retargetSingleHandle(ConstantPoolGen cp, InstructionHandle handle, int classNameIndex) {
        PUTSTATIC staticPut = (PUTSTATIC) handle.getInstruction();
        ConstantFieldref constant = (ConstantFieldref) cp.getConstant(staticPut.getIndex());
        constant.setClassIndex(classNameIndex);
    }

    private static void appendFieldsInstructions(InstructionList instructionList, InstructionFactory instructionFactory, String className) {
        instructionList.append(instructionFactory.createInvoke("java.lang.Runtime",
                "getRuntime",
                Type.getType(Runtime.class),
                Type.NO_ARGS,
                Const.INVOKESTATIC));
        instructionList.append(instructionFactory.createInvoke("java.lang.Runtime",
                "availableProcessors",
                Type.INT,
                Type.NO_ARGS,
                Const.INVOKEVIRTUAL));
        instructionList.append(instructionFactory.createPutStatic(className,
                LaunchProperties.NUMBER_OF_THREADS_NAME,
                Type.INT));
//        instructionList.append(instructionFactory.createGetStatic(className,
//                                                                  Constants.NUMBER_OF_THREADS_CONSTANT_NAME,
//                                                                  Type.INT));
//        instructionList.append(instructionFactory.createInvoke("java.util.concurrent.Executors",
//                                                               "newFixedThreadPool",
//                                                               Type.getType(ExecutorService.class),
//                                                               new Type[]{Type.INT},
//                                                               Const.INVOKESTATIC));
//        instructionList.append(instructionFactory.createPutStatic(className,
//                                                                  Constants.EXECUTOR_SERVICE_CONSTANT_NAME,
//                                                                  Type.getType(ExecutorService.class)));
        instructionList.append(InstructionFactory.createReturn(Type.VOID));
    }

    public static void initExecutorService(ClassGen cg, MethodGen mg) {

        ConstantPoolGen cp = cg.getConstantPool();
        InstructionFactory factory = new InstructionFactory(cg, cp);
        InstructionList il_append = new InstructionList();

        il_append.append(factory.createGetStatic(
                cg.getClassName(),
                LaunchProperties.NUMBER_OF_THREADS_NAME,
                Type.INT));
        il_append.append(factory.createInvoke(
                "java.util.concurrent.Executors",
                "newFixedThreadPool",
                Type.getType(ExecutorService.class),
                new Type[]{Type.INT},
                Const.INVOKESTATIC));
        il_append.append(factory.createPutStatic(
                cg.getClassName(),
                LaunchProperties.EXECUTOR_SERVICE_NAME,
                Type.getType(ExecutorService.class)));

        InstructionList il = mg.getInstructionList();
        il_append.append(il);
        mg.setInstructionList(il_append);
        updateMethodParametersScope(mg, cp);
        mg.setMaxStack();
        mg.setMaxLocals();
        cg.replaceMethod(mg.getMethod(), mg.getMethod());
    }

    public static void addTryCatchServiceExecutor(ClassGen cg) {

        ConstantPoolGen cp = cg.getConstantPool();
        InstructionList il = new InstructionList();

        MethodGen mg = new MethodGen(Const.ACC_STATIC | Const.ACC_PUBLIC,
                Type.VOID, Type.NO_ARGS, null, "serviceCall", cg.getClassName(), il, cp);
        InstructionFactory factory = new InstructionFactory(cg, mg.getConstantPool());

        il.append(factory.createFieldAccess("java.lang.System", "out", new ObjectType("java.io.PrintStream"), Const.GETSTATIC));
        il.append(new PUSH(cp, 10));
        il.append(factory.createInvoke("java.io.PrintStream", "print", Type.VOID, new Type[]{Type.STRING}, Const.INVOKEVIRTUAL));

        InstructionHandle ih_22 = il.append(factory.createFieldAccess(cg.getClassName(), "SERVICE", new ObjectType("java.util.concurrent.ExecutorService"), Const.GETSTATIC));
        InstructionHandle ih_35 = il.append(factory.createInvoke("java.util.concurrent.ExecutorService", "shutdown", Type.VOID, Type.NO_ARGS, Const.INVOKEINTERFACE));

        InstructionHandle iha_22 = il.append(factory.createFieldAccess(cg.getClassName(),
                "SERVICE", new ObjectType("java.util.concurrent.ExecutorService"), Const.GETSTATIC));
        InstructionHandle iha_35 = il.append(factory.createInvoke("java.util.concurrent.ExecutorService",
                "invokeAll", Type.VOID, Type.NO_ARGS, Const.INVOKEINTERFACE));

        InstructionHandle ih_40;

        BranchInstruction goto_40 = factory.createBranchInstruction(Const.GOTO, null);

        ih_40 = il.append(goto_40);
        InstructionHandle ih_43 = il.append(factory.createStore(Type.OBJECT, 1));
        InstructionHandle ih_44 = il.append(factory.createLoad(Type.OBJECT, 1));
        il.append(factory.createInvoke("java.lang.Exception", "printStackTrace",
                Type.VOID, Type.NO_ARGS, Const.INVOKEVIRTUAL));
        InstructionHandle ih_48 = il.append(factory.createReturn(Type.VOID));

        goto_40.setTarget(ih_48);
        mg.addExceptionHandler(ih_22, iha_35, ih_43, new ObjectType("java.lang.Exception"));

        il.append(new RETURN());
        mg.setInstructionList(il);

        mg.setMaxStack();
        mg.setMaxLocals();

        cg.addMethod(mg.getMethod());
        il.dispose();
    }

    public static void addTaskPool(ClassGen cg, MethodGen mg) {
        ConstantPoolGen cp = cg.getConstantPool();
        InstructionFactory factory = new InstructionFactory(cg, cp);
        InstructionList il = new InstructionList();


        il.append(factory.createInvoke(
                "util.pl.edu.agh.utils.Generico", "init",
                new ObjectType("List<Callable<Integer>>"),
                Type.NO_ARGS, Const.INVOKESTATIC));

        il.append(InstructionFactory.createStore(Type.OBJECT, 55));
        il.append(InstructionFactory.createLoad(Type.OBJECT, 55));
        il.append(InstructionFactory.createStore(Type.OBJECT, 56));


        InstructionList currentList = mg.getInstructionList();
        il.append(currentList);
        mg.setInstructionList(il);
        updateMethodParametersScope(mg, cp);
        mg.setMaxStack();
        mg.setMaxLocals();
        cg.replaceMethod(mg.getMethod(), mg.getMethod());
    }

    public static void updateMethodParametersScope(MethodGen mg, ConstantPoolGen cp) {
        InstructionHandle startHandle = mg.getInstructionList().getInstructionHandles()[0];
        Arrays.stream(mg.getLocalVariables())
                .filter(var -> var.getLocalVariable(cp).getStartPC() == 0)
                .forEach(var -> var.setStart(startHandle));
    }

    public static void addFutureResultsList(ClassGen cg, MethodGen mg) {
        ConstantPoolGen cp = cg.getConstantPool();
        InstructionFactory factory = new InstructionFactory(cg, cp);
        InstructionList il = new InstructionList();


        InstructionHandle ih_1 = il.append(factory.createInvoke("util.pl.edu.agh.utils.Generico", "init",
                new ObjectType("List<Callable<Integer>>"), Type.NO_ARGS, Const.INVOKESTATIC));


        il.append(InstructionFactory.createStore(Type.OBJECT, 2));
        InstructionHandle ih_2 = il.append(InstructionFactory.createLoad(Type.OBJECT, 2));
        InstructionHandle ih_3 = il.append(InstructionFactory.createStore(Type.OBJECT, 1));


        InstructionHandle ih_4 = il.append(factory.createNew("java.util.concurrent.Callable"));

        InstructionList currentList = mg.getInstructionList();
        il.append(currentList);
        mg.setInstructionList(il);
        updateMethodParametersScope(mg, cp);
        mg.setMaxStack();
        mg.setMaxLocals();
        cg.replaceMethod(mg.getMethod(), mg.getMethod());

    }

//    copyLoopToMethod
//    ok if start & end condition for loop are simple int,
//    so if you copy loop from main method to subtask method and occures dynamic size like array.length
//    then must care about it and make changes in code,
//    pass arguments to method in right type (not int, but maybe object/array)

    public static void copyLoopToSubTaskMethod(ClassGen classGen, MethodGen methodGen) {

        InstructionList subTaskInstructionList = getSubtaskInstructions(methodGen);

        subTaskInstructionList.append(new ICONST(1));
        subTaskInstructionList.append(InstructionFactory.createReturn(Type.INT));
        //TODO - would be nicer to get it by param from analyzer

        MethodGen subTaskMethod = new MethodGen(Const.ACC_PUBLIC | Const.ACC_STATIC,
                Type.INT,//TODO - would be nicer to get it by param from analyzer
                new Type[]{},
                new String[]{},
                LaunchProperties.SUBTASK_METHOD_NAME,
                classGen.getClassName(),
                subTaskInstructionList,
                classGen.getConstantPool());

        LocalVariableGen startVariable = subTaskMethod.addLocalVariable(LaunchProperties.START_INDEX_VARIABLE_NAME,
                Type.INT,
                //0,
                null, null);

        LocalVariableGen endVariable = subTaskMethod.addLocalVariable(LaunchProperties.END_INDEX_VARIABLE_NAME,
                Type.INT,
                //1,
                null, null);

        transferLocalVariables(methodGen, subTaskMethod);
        updateBranchInstructions(subTaskInstructionList);
        int newLoopIteratorVariableIndex = LocalVariableUtils.findLocalVariableByName(LaunchProperties.LOOP_ITERATOR_NAME, subTaskMethod.getLocalVariableTable(classGen.getConstantPool())).getIndex();
        LoopUtils.broadenCompareCondition(subTaskInstructionList.getInstructionHandles());
        LoopUtils.updateLoopVariableIndex(subTaskInstructionList.getInstructionHandles(), newLoopIteratorVariableIndex);
        LoopUtils.updateLoopStartCondition(subTaskInstructionList.getInstructionHandles(), startVariable.getIndex());
        LoopUtils.updateLoopEndCondition(subTaskInstructionList.getInstructionHandles(), endVariable.getIndex());

        subTaskMethod.setArgumentNames(new String[]{LaunchProperties.START_INDEX_VARIABLE_NAME, LaunchProperties.END_INDEX_VARIABLE_NAME});
        subTaskMethod.setArgumentTypes(new Type[]{Type.INT, Type.INT});
        subTaskMethod.setMaxLocals();
        subTaskMethod.setMaxStack();
        classGen.addMethod(subTaskMethod.getMethod());
        classGen.getConstantPool().addMethodref(subTaskMethod);

    }

    private static InstructionList getSubtaskInstructions(MethodGen mg) {
        InstructionHandle[] loopInstructions = LoopUtils.getForLoop(mg);
        InstructionList il = new InstructionList();
        for (InstructionHandle instr : loopInstructions) {
            if (instr instanceof BranchHandle) {
                BranchHandle branch = (BranchHandle) instr;
                il.append((BranchInstruction) branch.getInstruction().copy());
            } else {
                il.append(instr.getInstruction().copy());
            }
        }
        return il;
    }

    private static void transferLocalVariables(MethodGen mg, MethodGen subTaskmg) {
        InstructionList il = subTaskmg.getInstructionList();
        List<LocalVariableGen> variablesToCopy = getVariablesToCopy(mg, il);
        variablesToCopy.forEach(variable -> subTaskmg.addLocalVariable(variable.getName(),
                variable.getType(),
                gatHandleOnNewList(variable.getStart(), il),
                gatHandleOnNewList(variable.getEnd(), il)));
        Map<Integer, Integer> oldIndexesToNewIndexes = variablesToCopy.stream()
                .collect(Collectors.toMap(LocalVariableGen::getIndex, variable -> LocalVariableUtils.findLocalVariableByName(variable.getName(), subTaskmg.getLocalVariableTable(mg.getConstantPool())).getIndex(), (a, b) -> a));
        updateVariableInstructions(variablesToCopy, subTaskmg, oldIndexesToNewIndexes);
    }

    private static List<LocalVariableGen> getVariablesToCopy(MethodGen mg, InstructionList il) {
        List<Integer> variableIndexes = getVariableIndexes(il);
        return Arrays.stream(mg.getLocalVariables())
                .filter(variable -> variableIndexes.contains(variable.getIndex()))
                .collect(Collectors.toList());
    }

    private static List<Integer> getVariableIndexes(InstructionList il) {
        //we will not consider 4th instruction which is for(...; I < VAR;...) because VAR will be replaced
        InstructionHandle irrelevant = il.getInstructionHandles()[0].getNext().getNext().getNext();
        return Arrays.stream(il.getInstructionHandles())
                .filter(handle -> !handle.equals(irrelevant))
                .filter(handle -> handle.getInstruction() instanceof LoadInstruction)
                .map(handle -> (LoadInstruction) handle.getInstruction())
                .map(LoadInstruction::getIndex)
                .distinct()
                .collect(Collectors.toList());
    }

    private static InstructionHandle gatHandleOnNewList(InstructionHandle handle, InstructionList il) {
        return Arrays.stream(il.getInstructionHandles())
                .filter(listHandle -> handle.getInstruction().equals(listHandle.getInstruction()))
                .findFirst()
                .orElse(il.getEnd());
    }

    private static void updateVariableInstructions(List<LocalVariableGen> variablesToCopy, MethodGen subTaskmg, Map<Integer, Integer> oldIndexesToNewIndexes) {
        InstructionHandle[] instructionHandles = subTaskmg.getInstructionList().getInstructionHandles();
        List<Integer> indexes = variablesToCopy.stream()
                .map(LocalVariableGen::getIndex)
                .collect(Collectors.toList());
        Arrays.stream(instructionHandles)
                .filter(handle -> handle.getInstruction() instanceof LocalVariableInstruction)
                .map(handle -> (LocalVariableInstruction) handle.getInstruction())
                .filter(instruction -> indexes.contains(instruction.getIndex()))
                .forEach(instruction -> instruction.setIndex(oldIndexesToNewIndexes.get(instruction.getIndex())));
    }

    private static void updateBranchInstructions(InstructionList instructions) {
        InstructionHandle returnHandle = instructions.getInstructionHandles()[instructions.getInstructionHandles().length - 2];
        InstructionHandle loopBeginning = instructions.getInstructionHandles()[2];
        Arrays.stream(instructions.getInstructionHandles())
                .filter(BranchHandle.class::isInstance)
                .forEach(instr -> adjustInstructionTarget(instr, returnHandle, loopBeginning));
    }

    private static void adjustInstructionTarget(InstructionHandle instruction, InstructionHandle returnHandle, InstructionHandle loopBeginning) {
        short opCode = instruction.getInstruction().getOpcode();
        BranchHandle branchHandle = (BranchHandle) instruction;
        if (opCode == Const.GOTO) branchHandle.setTarget(loopBeginning);
        else branchHandle.setTarget(returnHandle);
    }

    public static void changeLoopLimitToNumberOfThreads(ClassGen cg, MethodGen mg) {
        InstructionList il = mg.getInstructionList();
        InstructionHandle[] forLoop = LoopUtils.getForLoop(mg);
        int numThreadsConstantIndex = ConstantPoolUtils.getFieldIndex(cg, LaunchProperties.NUMBER_OF_THREADS_NAME);
        forLoop[3].setInstruction(new GETSTATIC(numThreadsConstantIndex));

        cg.replaceMethod(mg.getMethod(), mg.getMethod());
    }

    public static void removeBodyForLoopInSelectedMethod(ClassGen cg, MethodGen mg) {
        InstructionHandle[] forLoop = LoopUtils.getForLoop(mg);
        LoopUtils.emptyMethodLoop(mg, forLoop);
        cg.replaceMethod(mg.getMethod(), mg.getMethod());
        updateMethodParametersScope(mg, mg.getConstantPool());
        mg.setMaxStack();
        mg.setMaxLocals();
        cg.replaceMethod(mg.getMethod(), mg.getMethod());
    }

    public static void setNewLoopBody(ClassGen cg, MethodGen mg, short dataSize) {
        InstructionList il = mg.getInstructionList();
        InstructionHandle[] loopHandles = LoopUtils.getForLoop(mg);
        InstructionHandle firstLoopInstruction = loopHandles[0];
        InstructionHandle lastLoopInstruction = loopHandles[loopHandles.length - 1];

        mg.addLocalVariable(LaunchProperties.START_INDEX_VARIABLE_NAME,
                Type.INT,
                firstLoopInstruction,
                lastLoopInstruction);

        mg.addLocalVariable(LaunchProperties.END_INDEX_VARIABLE_NAME,
                Type.FLOAT,
                firstLoopInstruction,
                lastLoopInstruction);

        mg.addLocalVariable(LaunchProperties.END_FINAL_INDEX_VARIABLE_NAME,
                Type.INT,
                firstLoopInstruction,
                lastLoopInstruction);


        InstructionHandle startVarStart = loopHandles[4];
        InstructionList endVarStart = InstructionUtils.getStartInitInstructions(cg, mg, dataSize);
        InstructionHandle startVarEnd = endVarStart.getEnd();

        InstructionList endVarEnd = InstructionUtils.getEndInitInstructions(cg, mg, dataSize);
        InstructionHandle startVarFinalEnd = endVarEnd.getEnd();

        InstructionList endVarFinalEnd = InstructionUtils.getFinalEndInitInstructions(cg, mg);
        InstructionHandle startListCallable = endVarEnd.getEnd();

        InstructionList endListCallable = InstructionUtils.getListCallableInstructions(cg, mg);


        il.append(startVarStart, endVarStart);
        il.append(startVarEnd, endVarEnd);

        il.append(startListCallable, endListCallable);
        il.append(startVarFinalEnd, endVarFinalEnd);

        updateMethodParametersScope(mg, mg.getConstantPool());
        mg.setMaxStack();
        mg.setMaxLocals();
        cg.replaceMethod(mg.getMethod(), mg.getMethod());

    }

    public static void insertNewInstruciton(ClassGen cg, MethodGen mg, int option) throws Exception, IllegalStateException {

        ConstantPoolGen cp = cg.getConstantPool();

        InstructionFactory factory = new InstructionFactory(cg, cp);

        InstructionList il_new = new InstructionList();
        InstructionList il_old = mg.getInstructionList();

        Instruction i_current;

        InstructionHandle handle;
        InstructionHandle[] handles = il_old.getInstructionHandles();


        InstructionHandle TRY_START = il_new.append(handles[0].getInstruction());

//TODO: GET BODY FROM METHOD AND PUT INTO TRY CATCH BLOCK

        for (int i = 1; i < handles.length - 1; i++) {

            handle = handles[i];
            i_current = handle.getInstruction();

//            System.out.println("opcode: " + i_current.getOpcode() + "\tname: " + i_current.getName());

            if (i_current.getOpcode() == Const.RETURN) {
                System.out.println(i_current.getOpcode() + " appears in " + mg.getName());
            } else il_new.append(i_current);

        }
//        System.out.println("last instruction: " + handles[handles.length - 1].getInstruction().getOpcode());


        switch (option) {

            case 1: //CODE INJECTION - instruction jcm.multiply
                injectJcmMultiply(mg, cp, factory, il_new);
                break;
            case 2: //CODE INJECTION - printArray
                injectPrintArrName(cg, mg, factory, il_new);
                break;
            case 3: //CODE INJECTION - get id luckyNumber
                injectPrintVarName(mg, cp, factory, il_new);
                break;
            case 4: //CODE INJECTION - instruction jcm.multiply(A,B)
                injectJcmMultiplyAB(cg, mg, cp, factory, il_new);
                break;
            default:
                System.out.println("wrong option!");
                break;
        }

        addBlockCatch(TRY_START, il_new, factory, mg);

        il_new.append(new RETURN());
        mg.setInstructionList(il_new);
        mg.stripAttributes(true);
        mg.setMaxStack();
        mg.setMaxLocals();
        cg.replaceMethod(mg.getMethod(), mg.getMethod());
        il_new.dispose();//what's this for?

    }

    private static void addBlockTry(InstructionHandle[] handles, InstructionList il) {
        InstructionHandle _handle;
        Instruction _i_current;

        for (int i = 1, handlesLength = handles.length; i < handlesLength; i++) {
            _handle = handles[i];
            _i_current = _handle.getInstruction();

//            System.out.println("opcode: " + _i_current.getOpcode() + "\tname: " + _i_current.getName());
//            if (_i_current.getOpcode() == Const.RETURN)
//                System.out.println(_i_current.getOpcode() + " appears in " + _mg.getName());
//            else
            il.append(_i_current);
        }
    }

    private static void addBlockCatch(InstructionHandle TRY_START, InstructionList il, InstructionFactory factory, MethodGen mg) {
        BranchInstruction GOTO = InstructionFactory.createBranchInstruction(Const.GOTO, null);
        InstructionHandle TRY_END = il.append(GOTO);

        InstructionHandle CATCH_START = il.append(InstructionFactory.createStore(Type.OBJECT, 0));
        il.append(factory.createFieldAccess("java.lang.System", "err",
                new ObjectType("java.io.PrintStream"), Const.GETSTATIC));
        il.append(InstructionFactory.createLoad(Type.OBJECT, 0));
        il.append(factory.createInvoke("java.lang.Exception", "getMessage",
                Type.STRING, Type.NO_ARGS, Const.INVOKEVIRTUAL));
        il.append(factory.createInvoke("java.io.Printstream", "println",
                Type.VOID, new Type[]{Type.STRING}, Const.INVOKEVIRTUAL));

        InstructionHandle CATCH_END = il.append(InstructionFactory.createReturn(Type.VOID));
        GOTO.setTarget(CATCH_END);
        mg.addExceptionHandler(TRY_START, TRY_END, CATCH_START, new ObjectType("java.lang.Exception"));
    }

    public static void addTryCatchService(ClassGen cg, MethodGen mg) {

        InstructionList il = new InstructionList();
        InstructionList il_old = mg.getInstructionList();

//        System.out.println(il_old.getEnd().getPosition());

        InstructionFactory factory = new InstructionFactory(cg, mg.getConstantPool());

        //get instruction from method for append to end
        getBodyFromMethodToNewInstructionList(mg, il, il_old);

//      ****************************************************************************************************
        InstructionHandle startTry = il.append(factory.createFieldAccess(
                cg.getClassName(),
                "SERVICE",
                new ObjectType("java.util.concurrent.ExecutorService"),
                Const.GETSTATIC));

        il.append(factory.createInvoke(
                "java.util.concurrent.ExecutorService",
                "invokeAll",
                Type.VOID, Type.NO_ARGS,
                Const.INVOKEINTERFACE));

        il.append(factory.createFieldAccess(
                cg.getClassName(),
                "SERVICE",
                new ObjectType("java.util.concurrent.ExecutorService"),
                Const.GETSTATIC));

        InstructionHandle endTry = il.append(factory.createInvoke(
                "java.util.concurrent.ExecutorService",
                "shutdown",
                Type.VOID, Type.NO_ARGS,
                Const.INVOKEINTERFACE));

        BranchInstruction gotoNext = factory.createBranchInstruction(Const.GOTO, null);
        il.append(gotoNext);

        InstructionHandle startCatch = il.append(factory.createStore(Type.OBJECT, mg.getMaxLocals()));

        il.append(factory.createLoad(Type.OBJECT, mg.getMaxLocals()));
        il.append(factory.createInvoke(
                "java.lang.Exception",
                "printStackTrace",
                Type.VOID, Type.NO_ARGS,
                Const.INVOKEVIRTUAL));

        InstructionHandle returnId = il.append(factory.createReturn(Type.VOID));

        gotoNext.setTarget(returnId);
        mg.addExceptionHandler(startTry, endTry, startCatch, new ObjectType("java.lang.Exception"));
//      ****************************************************************************************************

        mg.setInstructionList(il);

        mg.setMaxStack();
        mg.setMaxLocals();
        cg.replaceMethod(mg.getMethod(), mg.getMethod());
        il.dispose();
    }

}
