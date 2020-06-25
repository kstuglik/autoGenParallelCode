package pl.edu.agh.transformations.util;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.ConstantFieldref;
import org.apache.bcel.generic.*;
import pl.edu.agh.transformations.Constants;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class TransformUtils {

//  FIELDS

    public static void addThreadPool(ClassGen classGen) {
        Optional<MethodGen> classInitMethod = MethodUtils.findMethodByName(classGen, Const.STATIC_INITIALIZER_NAME);
        ConstantPoolGen constantPoolGen = classGen.getConstantPool();
        addClassFields_old(classGen, constantPoolGen);
        InstructionList instructionList = new InstructionList();
        classInitMethod.ifPresent(init -> {
            instructionList.append(init.getInstructionList());
            try {
                instructionList.delete(instructionList.getEnd());
            } catch (TargetLostException e) {
                e.printStackTrace();
            }
            retargetStaticPuts(classGen, instructionList);
        });
        InstructionFactory instructionFactory = new InstructionFactory(classGen, constantPoolGen);
        String className = classGen.getClassName();
        appendFieldsInstructions(instructionList, instructionFactory, className);
        MethodGen methodGen = new MethodGen(Const.ACC_STATIC,
                Type.VOID,
                Type.NO_ARGS,
                new String[0],
                Const.STATIC_INITIALIZER_NAME,
                className,
                instructionList,
                constantPoolGen);
        methodGen.stripAttributes(true);
        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        classGen.replaceMethod(methodGen.getMethod(), methodGen.getMethod());
    }

//  addClassFields_old- add thread fields in class

    public static void addClassFields_old(ClassGen cg, ConstantPoolGen cp) {
        FieldGen threadCount = new FieldGen(Const.ACC_PUBLIC | Const.ACC_STATIC | Const.ACC_FINAL,
                Type.INT,
                Constants.NUMBER_OF_THREADS_CONSTANT_NAME,
                cp);
        FieldGen service = new FieldGen(Const.ACC_PUBLIC | Const.ACC_STATIC,
                Type.getType(ExecutorService.class),
                Constants.EXECUTOR_SERVICE_CONSTANT_NAME,
                cp);
        cg.addField(threadCount.getField());
        cg.addField(service.getField());
    }

    /*  START JCUDA-SECTION  */

    public static void addClassFields(ClassGen cg, ConstantPoolGen cp, Type type, String fieldName) {
        FieldGen newField = new FieldGen(Const.ACC_PUBLIC | Const.ACC_STATIC,type,fieldName,cp);
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


    public static void insertNewInstruciton(ClassGen cg, MethodGen mg, int option) throws Exception, IllegalStateException {

        ConstantPoolGen cp = cg.getConstantPool();

        InstructionFactory factory = new InstructionFactory(cg, cp);

        InstructionList il_new = new InstructionList();
        InstructionList il_old = mg.getInstructionList();

        Instruction i_current;

        InstructionHandle handle;
        InstructionHandle[] handles = il_old.getInstructionHandles();

        /* rewrite instructions until you meet RETURN,then add new instructions and finally add RETURN.
        WEAKNESS: if the conditional instruction is the last instruction in the method,
        then there are two places where RETURN */

        for (InstructionHandle instructionHandle : handles) {

            handle = instructionHandle;i_current = handle.getInstruction();

            System.out.println("opcode: " + i_current.getOpcode() +  "\tname: " + i_current.getName());

            if (i_current.getOpcode() == Const.RETURN
                    && mg.getName().equals(mg.getName())) {
                System.out.println(i_current.getOpcode() + " appears in " + mg.getName());
            } else il_new.append(i_current);

        }


        System.out.println("last instruction: " + handles[handles.length-1].getInstruction().getOpcode());

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

        il_new.append(new RETURN());
        mg.setInstructionList(il_new);
        mg.stripAttributes(true);
        mg.setMaxStack();
        mg.setMaxLocals();
        cg.removeMethod(mg.getMethod());
        cg.addMethod(mg.getMethod());
        il_new.dispose();//what's this for?

    }

    private static void injectJcmMultiply(MethodGen mg, ConstantPoolGen cp, InstructionFactory factory, InstructionList il_new) {
        int id_A, id_B, jcm_ID, id;

        id_A = New.CreateArrayField("AA",mg,il_new,cp, Type.INT,2, new int[]{2,2});
        id_B = New.CreateArrayField("BB",mg,il_new,cp,Type.INT,2, new int[]{2,2});

        jcm_ID = New.CreateObjectClass("jcm", "utils.JCudaMatrix", il_new,factory,mg, new int[]{id_A,id_B});
        il_new.append(new ALOAD(jcm_ID));

        il_new.append(factory.createInvoke("utils.JCudaMatrix", "multiply",new ArrayType(Type.FLOAT,1), new Type[]{}, Const.INVOKEVIRTUAL));
        LocalVariableGen lg = mg.addLocalVariable("CC", new ArrayType(Type.FLOAT, 1), null, null);

        id = lg.getIndex();
        il_new.append(new ASTORE(id));
        New.PrintArray(il_new,mg,factory,id,false);
    }

    private static void injectJcmMultiplyAB(ClassGen cg, MethodGen mg, ConstantPoolGen cp, InstructionFactory factory, InstructionList il_new) {
        int id_A, id_B, jcm_ID, id;

        id_A = LocalVariableUtils.findLocalVariableByName(Constants.ARRAY_1 ,mg.getLocalVariableTable(cp)).getIndex();
        id_B = LocalVariableUtils.findLocalVariableByName(Constants.ARRAY_2 ,mg.getLocalVariableTable(cp)).getIndex();


        jcm_ID = New.CreateObjectClass("jcm", "utils.JCudaMatrix", il_new,factory,mg, new int[]{id_A,id_B});
        il_new.append(new ALOAD(jcm_ID));

        il_new.append(factory.createInvoke("utils.JCudaMatrix", "multiply",new ArrayType(Type.FLOAT,1), new Type[]{}, Const.INVOKEVIRTUAL));
        LocalVariableGen lg = mg.addLocalVariable("CC", new ArrayType(Type.FLOAT, 1), null, null);

        id = lg.getIndex();
        il_new.append(new ASTORE(id));
        New.PrintArray(il_new,mg,factory,id,true);
    }

    private static void injectPrintArrName(ClassGen cg, MethodGen mg, InstructionFactory factory, InstructionList il_new) {
        int id = ConstantPoolUtils.getFieldIndex(cg, "C");
        New.PrintArray(il_new, mg, factory, id, false);
    }

    private static void injectPrintVarName(MethodGen mg, ConstantPoolGen cp, InstructionFactory factory, InstructionList il_new) {
        int id = New.getLoacalVariableID("luckyNumber",cp,mg);
        il_new.append(factory.createFieldAccess("java.lang.System", "out",
                new ObjectType("java.io.PrintStream"), Const.GETSTATIC));
        il_new.append(new DUP());
        il_new.append(new ALOAD(id));
        il_new.append(factory.createInvoke("java.io.PrintStream", "println",
                Type.VOID, new Type[]{Type.STRING}, Const.INVOKEVIRTUAL));
    }

    public void printSomething(InstructionList il_new, InstructionFactory factory, ConstantPoolGen cp){
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


    /*  END JCUDA-SECTION  */

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

    public static void appendFieldsInstructions(InstructionList il, InstructionFactory factory, String className) {
        il.append(factory.createInvoke("java.lang.Runtime",
                "getRuntime",
                Type.getType(Runtime.class),
                Type.NO_ARGS,
                Const.INVOKESTATIC));
        il.append(factory.createInvoke("java.lang.Runtime",
                "availableProcessors",
                Type.INT,
                Type.NO_ARGS,
                Const.INVOKEVIRTUAL));
        il.append(factory.createPutStatic(className,
                Constants.NUMBER_OF_THREADS_CONSTANT_NAME,
                Type.INT));
//        il.append(factory.createGetStatic(className,
//                                                                  Constants.NUMBER_OF_THREADS_CONSTANT_NAME,
//                                                                  Type.INT));
//        il.append(factory.createInvoke("java.util.concurrent.Executors",
//                                                               "newFixedThreadPool",
//                                                               Type.getType(ExecutorService.class),
//                                                               new Type[]{Type.INT},
//                                                               Const.INVOKESTATIC));
//        il.append(factory.createPutStatic(className,
//                                                                  Constants.EXECUTOR_SERVICE_CONSTANT_NAME,
//                                                                  Type.getType(ExecutorService.class)));
        il.append(InstructionFactory.createReturn(Type.VOID));
    }

    public static void addExecutorServiceInit(ClassGen cg, MethodGen mg) {

        ConstantPoolGen cp = cg.getConstantPool();
        InstructionFactory factory = new InstructionFactory(cg, cp);
        InstructionList appendedInstructions = new InstructionList();
        appendedInstructions.append(factory.createGetStatic(cg.getClassName(),
                Constants.NUMBER_OF_THREADS_CONSTANT_NAME,
                Type.INT));
        appendedInstructions.append(factory.createInvoke("java.util.concurrent.Executors",
                "newFixedThreadPool",
                Type.getType(ExecutorService.class),
                new Type[]{Type.INT},
                Const.INVOKESTATIC));
        appendedInstructions.append(factory.createPutStatic(cg.getClassName(),
                Constants.EXECUTOR_SERVICE_CONSTANT_NAME,
                Type.getType(ExecutorService.class)));
        InstructionList il = mg.getInstructionList();
        appendedInstructions.append(il);
        mg.setInstructionList(appendedInstructions);

    }

    public static void addTaskPool(ClassGen cg, MethodGen mg) {
        ConstantPoolGen cp = cg.getConstantPool();
        InstructionFactory factory = new InstructionFactory(cg, cp);
        InstructionList appendedInstructions = new InstructionList();
        appendedInstructions.append(factory.createNew(ObjectType.getInstance("java.util.ArrayList")));
        appendedInstructions.append(InstructionFactory.createDup(1));
        appendedInstructions.append(factory.createInvoke("java.util.ArrayList",
                "<init>",
                Type.VOID,
                new Type[]{},
                Const.INVOKESPECIAL));
        appendedInstructions.append(InstructionFactory.createStore(Type.getType("Ljava/util/List;"), mg.getMaxLocals()));
        mg.addLocalVariable(Constants.TASK_POOL_NAME,
                Type.getType("Ljava/util/List;"),
                appendedInstructions.getEnd(),
                null);
        InstructionList currentList = mg.getInstructionList();
        appendedInstructions.append(currentList);
        mg.setInstructionList(appendedInstructions);
        updateMethodParametersScope(mg, cp);
        mg.setMaxStack();
        mg.setMaxLocals();
        cg.removeMethod(mg.getMethod());
        cg.addMethod(mg.getMethod());
    }

    private static void updateMethodParametersScope(MethodGen mg, ConstantPoolGen cp) {
        InstructionHandle startHandle = mg.getInstructionList().getInstructionHandles()[0];
        Arrays.stream(mg.getLocalVariables())
                .filter(var -> var.getLocalVariable(cp).getStartPC() == 0)
                .forEach(var -> var.setStart(startHandle));
    }

//    ?? alone ArrayList()

    public static void addFutureResultsList(ClassGen cg, MethodGen mg) {
        ConstantPoolGen cp = cg.getConstantPool();
        InstructionFactory factory = new InstructionFactory(cg, cp);
        InstructionList appendedInstructions = new InstructionList();
        appendedInstructions.append(factory.createNew(ObjectType.getInstance("java.util.ArrayList")));
        appendedInstructions.append(InstructionFactory.createDup(1));
        appendedInstructions.append(factory.createInvoke("java.util.ArrayList",
                "<init>",
                Type.VOID,
                new Type[]{},
                Const.INVOKESPECIAL));
        appendedInstructions.append(InstructionFactory.createStore(Type.getType("Ljava/util/List;"), mg.getMaxLocals()));
        mg.addLocalVariable(Constants.RESULTS_POOL_NAME,
                Type.getType("Ljava/util/List;"),
                appendedInstructions.getEnd(),
                null);
        InstructionList currentList = mg.getInstructionList();
        appendedInstructions.append(currentList);
        mg.setInstructionList(appendedInstructions);
        updateMethodParametersScope(mg, cp);
        mg.setMaxStack();
        mg.setMaxLocals();
        cg.removeMethod(mg.getMethod());
        cg.addMethod(mg.getMethod());

    }

    public static void copyLoopToMethod(ClassGen cg, MethodGen mg) {

        InstructionList il = getSubtaskInstructions(mg);

//        il.append(new ICONST(1));
//        TODO - get Type param from analyzer => PROBABLY DONE
        il.append(InstructionFactory.createReturn(mg.getReturnType()));

        MethodGen subTaskmg = new MethodGen(Const.ACC_PUBLIC | Const.ACC_STATIC,
//                Type.INT,//TODO - would be nicer to get it by param from analyzer
                mg.getReturnType(),
                new Type[]{},
                new String[]{},
                Constants.SUBTASK_METHOD_NAME,
                cg.getClassName(),
                il,
                cg.getConstantPool());

        LocalVariableGen startVariable = subTaskmg.addLocalVariable(Constants.START_INDEX_VARIABLE_NAME,
                Type.INT,
                //0,
                null, null);
        LocalVariableGen endVariable = subTaskmg.addLocalVariable(Constants.END_INDEX_VARIABLE_NAME,
                Type.INT,
                //1,
                null, null);

        transferLocalVariables(mg, subTaskmg);

        updateBranchInstructions(il);
        int newLoopIteratorVariableIndex = LocalVariableUtils.findLocalVariableByName(Constants.LOOP_ITERATOR_NAME, subTaskmg.getLocalVariableTable(cg.getConstantPool())).getIndex();
        LoopUtils.broadenCompareCondition(il.getInstructionHandles());
        LoopUtils.updateLoopVariableIndex(il.getInstructionHandles(), newLoopIteratorVariableIndex);
        LoopUtils.updateLoopStartCondition(il.getInstructionHandles(), startVariable.getIndex());
        LoopUtils.updateLoopEndCondition(il.getInstructionHandles(), endVariable.getIndex());
        subTaskmg.setArgumentNames(new String[]{Constants.START_INDEX_VARIABLE_NAME, Constants.END_INDEX_VARIABLE_NAME});
        subTaskmg.setArgumentTypes(new Type[]{Type.INT, Type.INT});
        subTaskmg.setMaxLocals();
        subTaskmg.setMaxStack();
        cg.addMethod(subTaskmg.getMethod());
        cg.getConstantPool().addMethodref(subTaskmg);
        mg.setMaxStack();
        mg.setMaxLocals();
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
                .map(handle -> (LoadInstruction)handle.getInstruction())
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
                .map(handle -> (LocalVariableInstruction)handle.getInstruction())
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
        if (opCode == Const.GOTO) {
            branchHandle.setTarget(loopBeginning);
        } else {
            branchHandle.setTarget(returnHandle);
        }
    }

    public static void changeLoopLimitToNumberOfThreads(ClassGen cg, MethodGen mg) {
        InstructionHandle[] forLoop = LoopUtils.getForLoop(mg);
        int numThreadsConstantIndex = ConstantPoolUtils.getFieldIndex(cg, Constants.NUMBER_OF_THREADS_CONSTANT_NAME);
        forLoop[3].setInstruction(new GETSTATIC(numThreadsConstantIndex));
        cg.replaceMethod(mg.getMethod(), mg.getMethod());
    }

    public static void emptyMethodLoop(ClassGen cg, MethodGen mg) {
        InstructionHandle[] forLoop = LoopUtils.getForLoop(mg);
        LoopUtils.emptyMethodLoop(mg, forLoop);
        cg.replaceMethod(mg.getMethod(), mg.getMethod());
    }

    public static void setNewLoopBody(ClassGen cg, MethodGen mg, short dataSize) {
        InstructionList allInstructionsList = mg.getInstructionList();
        InstructionHandle[] loopHandles = LoopUtils.getForLoop(mg);
        InstructionHandle firstLoopInstruction = loopHandles[0];
        InstructionHandle lastInstructionBeforeLoopBody = loopHandles[4];
        InstructionHandle lastLoopInstruction = loopHandles[loopHandles.length - 1];
        mg.addLocalVariable(Constants.START_INDEX_VARIABLE_NAME,
                Type.INT,
                firstLoopInstruction,
                lastLoopInstruction);
        mg.addLocalVariable(Constants.END_INDEX_VARIABLE_NAME,
                Type.INT,
                firstLoopInstruction,
                lastLoopInstruction);
        InstructionList startInitInstructions = InstructionUtils.getStartInitInstructions(cg, mg, dataSize);
        InstructionHandle endOfStartInit = startInitInstructions.getEnd();
        InstructionList endInitInstructions = InstructionUtils.getEndInitInstructions(cg, mg, dataSize);
        allInstructionsList.append(lastInstructionBeforeLoopBody, startInitInstructions);
        allInstructionsList.append(endOfStartInit, endInitInstructions);
        mg.setMaxStack();
        cg.replaceMethod(mg.getMethod(), mg.getMethod());
    }
}