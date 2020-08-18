package pl.edu.agh.bcel.utils;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.ConstantFieldref;
import org.apache.bcel.generic.*;
import pl.edu.agh.bcel.LaunchProperties;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;



public class TransformUtils {

    public static void addCallJCudaMultiply(ClassGen cg, MethodGen mg) throws Exception {

        ConstantPoolGen cp = cg.getConstantPool();
        InstructionList il = new InstructionList();
        InstructionList il_old = mg.getInstructionList();

        InstructionFactory factory = new InstructionFactory(cg, mg.getConstantPool());

        Optional<InstructionHandle> i = Arrays.stream(il_old.getInstructionHandles())
                .filter(handle -> "return".equals(handle.getInstruction().getName()))
                .findFirst();

        int GOTO_ID = i.get().getPosition() - 1;

        InstructionHandle[] handles = LoopUtilsOld.getIhsBetweenFromTo(
                mg.getInstructionList().getInstructionHandles(), 0, GOTO_ID);

        InstructionHandle instr;
        for (InstructionHandle handle : handles) {
            instr = handle;
//            System.out.println(instr.getInstruction().copy());
            if (instr instanceof BranchHandle) {
                BranchHandle branch = (BranchHandle) instr;
                il.append((BranchInstruction) branch.getInstruction().copy());
            } else il.append(instr.getInstruction().copy());
        }

        int id_A, id_B;

        id_A = VariableUtils.getLVarByName(LaunchProperties.ARRAY_1, mg.getLocalVariableTable(cp)).getIndex();
        id_B = VariableUtils.getLVarByName(LaunchProperties.ARRAY_2, mg.getLocalVariableTable(cp)).getIndex();

//        ************** verify the type of array: 1D or 2D or more
//        YOU MUST KNOW THAT INFO ABOUT COLS AND ROWS MUST BE SET IN LAUNCHPROPERTIES

        String signatureMyArray = VariableUtils.getSignatureVarByName(LaunchProperties.ARRAY_1, mg.getLocalVariableTable(cp));

        long dimArray = signatureMyArray.chars().filter(ch -> ch == '[').count();

        InstructionHandle ih_86 = il.append(InstructionFactory.createLoad(Type.OBJECT, id_A));
        il.append(InstructionFactory.createLoad(Type.OBJECT, id_B));


        if (dimArray == 1) {
            il.append(factory.createInvoke(cg.getClassName(), "prepareFieldsFromArrays1D", Type.VOID, new Type[]{new ArrayType(Type.INT, 1), new ArrayType(Type.INT, 1)}, Const.INVOKESTATIC));
            ReadyMethods.addMethodPrepareFieldsFromArrays1D(cg);
        } else if (dimArray == 2) {
            il.append(factory.createInvoke(cg.getClassName(), "prepareFieldsFromArrays2D", Type.VOID, new Type[]{new ArrayType(Type.INT, 2), new ArrayType(Type.INT, 2)}, Const.INVOKESTATIC));
            ReadyMethods.addMethodPrepareFieldsFromArrays2D(cg);
            ReadyMethods.addFlattenMethod(cg);
        } else {
            throw new IllegalArgumentException("ILLEGAL DIMENSION OF THE ARRAY WAS DETECTED!");
        }


        InstructionHandle ih_91 = il.append(factory.createInvoke(cg.getClassName(), "multiply", Type.VOID, Type.NO_ARGS, Const.INVOKESTATIC));

        InstructionHandle ih_94 = il.append(factory.createFieldAccess("java.lang.System", "out", new ObjectType("java.io.PrintStream"), Const.GETSTATIC));
        il.append(factory.createFieldAccess(cg.getClassName(), "matrix_C", new ArrayType(Type.FLOAT, 1), Const.GETSTATIC));
        il.append(factory.createInvoke("java.util.Arrays", "toString", Type.STRING, new Type[]{new ArrayType(Type.FLOAT, 1)}, Const.INVOKESTATIC));
        il.append(factory.createInvoke("java.io.PrintStream", "println", Type.VOID, new Type[]{Type.STRING}, Const.INVOKEVIRTUAL));

//        mg.setClassName(cg.getClassName());
        mg.setInstructionList(il);
        mg.setMaxStack();
        mg.setMaxLocals();
        cg.replaceMethod(mg.getMethod(), mg.getMethod());
        il.dispose();
    }

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

/*CHECK IN FUTURE: id for field or variable
        int id = ConstantPoolUtils.getFieldIndex(cg,"jcm");
        int id = New.getLoacalVariableID("jcm",cp,mg);
        forLoop[3].setInstruction(new GETSTATIC(numThreadsConstantIndex));*/

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

        ReadyFields.addFieldNumThreads(il_new, _factory, className);

        MethodGen mg = new MethodGen(Const.ACC_STATIC,
                Type.VOID, Type.NO_ARGS, new String[0],
                Const.STATIC_INITIALIZER_NAME,
                className, il_new, cp);


        mg.setMaxLocals();
        mg.setMaxStack();
        cg.replaceMethod(mg.getMethod(), mg.getMethod());
//        il_new.dispose();
    }

    public static void addTryCatchService(ClassGen cg, MethodGen mg) {

        InstructionList il = new InstructionList();
        InstructionList il_old = mg.getInstructionList();

//        System.out.println(il_old.getEnd().getPosition());

        InstructionFactory factory = new InstructionFactory(cg, mg.getConstantPool());

        //get instruction from method for append to end
//        -1 is default arg val
        MethodUtils.getBodyFromMethodToNewInstructionList(mg, il, il_old);

//      ****************************************************************************************************
        InstructionHandle startTry = il.append(factory.createFieldAccess(
                cg.getClassName(),
                "SERVICE",
                new ObjectType("java.util.concurrent.ExecutorService"),
                Const.GETSTATIC));

        il.append(InstructionFactory.createLoad(Type.OBJECT, 55));
        il.append(factory.createInvoke("java.util.concurrent.ExecutorService", "invokeAll", new ObjectType("java.util.List"), new Type[]{new ObjectType("java.util.Collection")}, Const.INVOKEINTERFACE));
        il.append(InstructionConst.POP);

//        il.append(factory.createInvoke(
//                "java.util.concurrent.ExecutorService",
//                "invokeAll",
//                Type.VOID, Type.NO_ARGS,
//                Const.INVOKEINTERFACE));

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

        BranchInstruction gotoNext = InstructionFactory.createBranchInstruction(Const.GOTO, null);
        il.append(gotoNext);

        InstructionHandle startCatch = il.append(InstructionFactory.createStore(Type.OBJECT, mg.getMaxLocals()));

        il.append(InstructionFactory.createLoad(Type.OBJECT, mg.getMaxLocals()));
        il.append(factory.createInvoke(
                "java.lang.Exception",
                "printStackTrace",
                Type.VOID, Type.NO_ARGS,
                Const.INVOKEVIRTUAL));

        InstructionHandle returnId = il.append(InstructionFactory.createReturn(Type.VOID));

        gotoNext.setTarget(returnId);
        mg.addExceptionHandler(startTry, endTry, startCatch, new ObjectType("java.lang.Exception"));
//      ****************************************************************************************************

        mg.setInstructionList(il);
        updateMethodParametersScope(mg, mg.getConstantPool());
        mg.setMaxStack();
        mg.setMaxLocals();
        cg.replaceMethod(mg.getMethod(), mg.getMethod());
        il.dispose();
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

        BranchInstruction goto_40 = InstructionFactory.createBranchInstruction(Const.GOTO, null);

        ih_40 = il.append(goto_40);
        InstructionHandle ih_43 = il.append(InstructionFactory.createStore(Type.OBJECT, 1));
        InstructionHandle ih_44 = il.append(InstructionFactory.createLoad(Type.OBJECT, 1));
        il.append(factory.createInvoke("java.lang.Exception", "printStackTrace",
                Type.VOID, Type.NO_ARGS, Const.INVOKEVIRTUAL));
        InstructionHandle ih_48 = il.append(InstructionFactory.createReturn(Type.VOID));

        goto_40.setTarget(ih_48);
        mg.addExceptionHandler(ih_22, iha_35, ih_43, new ObjectType("java.lang.Exception"));

        il.append(new RETURN());
        mg.setInstructionList(il);

        mg.setMaxStack();
        mg.setMaxLocals();

        cg.addMethod(mg.getMethod());
        il.dispose();
    }

    protected static void adjustInstructionTarget(InstructionHandle instruction, InstructionHandle returnHandle, InstructionHandle loopBeginning) {
        short opCode = instruction.getInstruction().getOpcode();
        BranchHandle branchHandle = (BranchHandle) instruction;
        if (opCode == Const.GOTO) branchHandle.setTarget(loopBeginning);
        else branchHandle.setTarget(returnHandle);
    }

    public static void changeLoopLimitToNumberOfThreads(ClassGen cg, MethodGen mg) {
        InstructionList il = mg.getInstructionList();
        InstructionHandle[] forLoop = LoopUtilsOld.getForLoop(mg);
        int numThreadsConstantIndex = VariableUtils.getFieldRefId(cg, LaunchProperties.NUMBER_OF_THREADS_NAME);
        forLoop[3].setInstruction(new GETSTATIC(numThreadsConstantIndex));

        cg.replaceMethod(mg.getMethod(), mg.getMethod());
    }

    public static void copyLoopToSubTaskMethod(ClassGen classGen, MethodGen methodGen, String iteratorName) {

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

        LocalVariableGen startVariable = subTaskMethod.addLocalVariable(LaunchProperties.START_INDEX_VAR_NAME,
                Type.INT,
                //0,
                null, null);

        LocalVariableGen endVariable = subTaskMethod.addLocalVariable(LaunchProperties.END_INDEX_VAR_NAME,
                Type.INT,
                //1,
                null, null);

        transferLocalVariables(methodGen, subTaskMethod);
        updateBranchInstructions(subTaskInstructionList);
        int newLoopIteratorVariableIndex = VariableUtils.getLVarByName(iteratorName, subTaskMethod.getLocalVariableTable(classGen.getConstantPool())).getIndex();
        LoopUtilsOld.broadenCompareCondition(subTaskInstructionList.getInstructionHandles());
        LoopUtilsOld.updateLoopVariableIndex(subTaskInstructionList.getInstructionHandles(), newLoopIteratorVariableIndex);
        LoopUtilsOld.updateLoopStartCondition(subTaskInstructionList.getInstructionHandles(), startVariable.getIndex());
        LoopUtilsOld.updateLoopEndCondition(subTaskInstructionList.getInstructionHandles(), endVariable.getIndex());

        subTaskMethod.setArgumentNames(new String[]{LaunchProperties.START_INDEX_VAR_NAME, LaunchProperties.END_INDEX_VAR_NAME});
        subTaskMethod.setArgumentTypes(new Type[]{Type.INT, Type.INT});
        subTaskMethod.setMaxLocals();
        subTaskMethod.setMaxStack();
        classGen.addMethod(subTaskMethod.getMethod());
        classGen.getConstantPool().addMethodref(subTaskMethod);
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

    private static InstructionHandle gatHandleOnNewList(InstructionHandle handle, InstructionList il) {
        return Arrays.stream(il.getInstructionHandles())
                .filter(listHandle -> handle.getInstruction().equals(listHandle.getInstruction()))
                .findFirst()
                .orElse(il.getEnd());
    }

    protected static InstructionList getSubtaskInstructions(MethodGen mg) {
        InstructionHandle[] loopInstructions = LoopUtilsOld.getForLoop(mg);
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

    public static void removeBodyForLoopInSelectedMethod(ClassGen cg, MethodGen mg) {
        InstructionHandle[] forLoop = LoopUtilsOld.getForLoop(mg);
        LoopUtilsOld.emptyMethodLoop(mg, forLoop);
        cg.replaceMethod(mg.getMethod(), mg.getMethod());
        updateMethodParametersScope(mg, mg.getConstantPool());
        mg.setMaxStack();
        mg.setMaxLocals();
        cg.replaceMethod(mg.getMethod(), mg.getMethod());
    }

    private static void retargetSingleHandle(ConstantPoolGen cp, InstructionHandle handle, int classNameIndex) {
        PUTSTATIC staticPut = (PUTSTATIC) handle.getInstruction();
        ConstantFieldref constant = (ConstantFieldref) cp.getConstant(staticPut.getIndex());
        constant.setClassIndex(classNameIndex);
    }

    protected static void retargetStaticPuts(ClassGen cg, InstructionList il) {
        int classNameIndex = cg.getClassNameIndex();
        Arrays.stream(il.getInstructionHandles())
                .filter(handle -> handle.getInstruction() instanceof PUTSTATIC)
                .forEach(handle -> retargetSingleHandle(cg.getConstantPool(), handle, classNameIndex));
    }

    public static void setNewLoopBody(ClassGen cg, MethodGen mg, short dataSize) {
        InstructionList il = mg.getInstructionList();
        InstructionHandle[] loopHandles = LoopUtilsOld.getForLoop(mg);
        InstructionHandle firstLoopInstruction = loopHandles[0];
        InstructionHandle lastLoopInstruction = loopHandles[loopHandles.length - 1];

        mg.addLocalVariable(LaunchProperties.START_INDEX_VAR_NAME,
                Type.INT,
                firstLoopInstruction,
                lastLoopInstruction);

        mg.addLocalVariable(LaunchProperties.END_INDEX_VAR_NAME,
                Type.FLOAT,
                firstLoopInstruction,
                lastLoopInstruction);

        mg.addLocalVariable(LaunchProperties.END_FINAL_INDEX_VAR_NAME,
                Type.INT,
                firstLoopInstruction,
                lastLoopInstruction);


        InstructionHandle startVarStart = loopHandles[4];
        InstructionList endVarStart = InstructionUtils.getInstructionsStartInit(cg, mg, dataSize);
        InstructionHandle startVarEnd = endVarStart.getEnd();

        InstructionList endVarEnd = InstructionUtils.getInstructionsEndInit(cg, mg, dataSize);
        InstructionHandle startVarFinalEnd = endVarEnd.getEnd();

        InstructionList endVarFinalEnd = InstructionUtils.getInstructionsEndFinalInit(cg, mg);
        InstructionHandle startListCallable = endVarEnd.getEnd();

        InstructionList endListCallable = InstructionUtils.addListCallableInstructions(cg, mg);


        il.append(startVarStart, endVarStart);
        il.append(startVarEnd, endVarEnd);

        il.append(startListCallable, endListCallable);
        il.append(startVarFinalEnd, endVarFinalEnd);

        updateMethodParametersScope(mg, mg.getConstantPool());
        mg.setMaxStack();
        mg.setMaxLocals();
        cg.replaceMethod(mg.getMethod(), mg.getMethod());

    }

    public static void transferLocalVariables(MethodGen mg, MethodGen subTaskmg) {
        InstructionList il = subTaskmg.getInstructionList();
        List<LocalVariableGen> variablesToCopy = VariableUtils.getVariablesToCopy(mg, il);
        for (LocalVariableGen localVariableGen : variablesToCopy) {

            subTaskmg.addLocalVariable(localVariableGen.getName(),
                    localVariableGen.getType(),
                    gatHandleOnNewList(localVariableGen.getStart(), il),
                    gatHandleOnNewList(localVariableGen.getEnd(), il));
        }
        Map<Integer, Integer> oldIndexesToNewIndexes = new HashMap<>();
        for (LocalVariableGen variable : variablesToCopy) {
            oldIndexesToNewIndexes.putIfAbsent(variable.getIndex(), VariableUtils.getLVarByName(variable.getName(), subTaskmg.getLocalVariableTable(mg.getConstantPool())).getIndex());
        }
        updateVariableInstructions(variablesToCopy, subTaskmg, oldIndexesToNewIndexes);
    }

    protected static void updateBranchInstructions(InstructionList instructions) {
        InstructionHandle returnHandle = instructions.getInstructionHandles()[instructions.getInstructionHandles().length - 2];
        InstructionHandle loopBeginning = instructions.getInstructionHandles()[2];
        Arrays.stream(instructions.getInstructionHandles())
                .filter(BranchHandle.class::isInstance)
                .forEach(instr -> adjustInstructionTarget(instr, returnHandle, loopBeginning));
    }

    public static void updateMethodParametersScope(MethodGen mg, ConstantPoolGen cp) {
        InstructionHandle startHandle = mg.getInstructionList().getInstructionHandles()[0];
        Arrays.stream(mg.getLocalVariables())
                .filter(var -> var.getLocalVariable(cp).getStartPC() == 0)
                .forEach(var -> var.setStart(startHandle));
    }

    private static void updateVariableInstructions(
            List<LocalVariableGen> variablesToCopy, MethodGen subTaskmg, Map<Integer, Integer> oldIndexesToNewIndexes) {
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

}