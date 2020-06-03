package pl.edu.agh.transformations.util;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.ConstantFieldref;
import org.apache.bcel.generic.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class TransformUtils {

    public static void addThreadPool(ClassGen cg) {
        Optional<MethodGen> classInitMethod = MethodUtils.findMethodByName(cg, Const.STATIC_INITIALIZER_NAME);
        ConstantPoolGen cp = cg.getConstantPool();
        addClassFields(cg, cp);
        InstructionList il = new InstructionList();
        classInitMethod.ifPresent(init -> {
            il.append(init.getInstructionList());
            try {
                il.delete(il.getEnd());
            } catch (TargetLostException e) {
                e.printStackTrace();
            }
            retargetStaticPuts(cg, il);
        });
        InstructionFactory factory = new InstructionFactory(cg, cp);
        String className = cg.getClassName();
        appendFieldsInstructions(il, factory, className);
        MethodGen mg = new MethodGen(Const.ACC_STATIC,
                Type.VOID,
                Type.NO_ARGS,
                new String[0],
                Const.STATIC_INITIALIZER_NAME,
                className,
                il,
                cp);
        mg.stripAttributes(true);
        mg.setMaxLocals();
        mg.setMaxStack();
        cg.replaceMethod(mg.getMethod(), mg.getMethod());
    }

    public static void addClassFields(ClassGen cg, ConstantPoolGen cp) {
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

    private static void appendFieldsInstructions(InstructionList il,
                                                 InstructionFactory factory,
                                                 String className) {
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

        il.append(new ICONST(1));
        il.append(InstructionFactory.createReturn(Type.INT));//TODO - would be nicer to get it by param from analyzer

        MethodGen subTaskmg = new MethodGen(Const.ACC_PUBLIC | Const.ACC_STATIC,
                Type.INT,//TODO - would be nicer to get it by param from analyzer
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

    private static void adjustInstructionTarget(InstructionHandle instruction,
                                                InstructionHandle returnHandle,
                                                InstructionHandle loopBeginning) {
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