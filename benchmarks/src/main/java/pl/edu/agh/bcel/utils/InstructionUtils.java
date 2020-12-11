package pl.edu.agh.bcel.utils;

import org.apache.bcel.Const;
import org.apache.bcel.generic.*;
import pl.edu.agh.bcel.LaunchProperties;

import java.util.Arrays;
import java.util.HashMap;

public class InstructionUtils {

    public static InstructionList addListCallableInstructions(ClassGen cg, MethodGen mg) {
        InstructionList il = new InstructionList();
        InstructionFactory factory = new InstructionFactory(cg, mg.getConstantPool());

        il.append(InstructionFactory.createLoad(Type.OBJECT, LaunchProperties.TASK_POOL_ID));
        il.append(factory.createNew(new ObjectType("Callable<Integer>() {" +
                "public Integer call() {" +
                "return " + LaunchProperties.SUBTASK_METHOD_NAME + "(" +
                LaunchProperties.START_CONDITION_NAME + "," + LaunchProperties.END_FINAL_INDEX_VAR_NAME +
                ");}}"
        )));

        il.append(factory.createInvoke("java.util.List",
                "add", Type.BOOLEAN, new Type[]{Type.OBJECT}, Const.INVOKEINTERFACE));
//    il.append(new POP());

        return il;
    }

    static InstructionHandle getInstructionFromHandles(Instruction i, InstructionHandle[] ih) {
        return Arrays.stream(ih)
                .filter(handle -> handle.getInstruction().equals(i))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No matching instruction found for instruction handle."));
    }

    public static InstructionList getInstructionsEndFinalInit(ClassGen cg, MethodGen mg) {
        InstructionList il = new InstructionList();

        int endVarIndex = VariableUtils.getLVarByName(
                LaunchProperties.STOP_CONDITION_NAME, mg.getLocalVariableTable(cg.getConstantPool())).getIndex();
        int endFinalVarIndex = VariableUtils.getLVarByName(
                LaunchProperties.END_FINAL_INDEX_VAR_NAME, mg.getLocalVariableTable(cg.getConstantPool())).getIndex();
//        int dataSizeIndex = VariableUtils.getLVarByName(
//                LaunchProperties.DATASIZE_VAR_NAME, mg.getLocalVariableTable(cg.getConstantPool())).getIndex();

        int dataSizeIndex = VariableUtils.getFieldIdByName(LaunchProperties.DATASIZE_VAR_NAME, cg);


        InstructionHandle ih_6 = il.append(InstructionFactory.createLoad(Type.INT, endVarIndex));
        il.append(InstructionFactory.createLoad(Type.INT, dataSizeIndex));
        il.append(new PUSH(mg.getConstantPool(), 1));
        il.append(InstructionConst.ISUB);
        BranchInstruction if_icmple_10 = InstructionFactory.createBranchInstruction(Const.IF_ICMPLE, null);
        il.append(if_icmple_10);
        InstructionHandle ih_13 = il.append(InstructionFactory.createLoad(Type.INT, dataSizeIndex));
        il.append(new PUSH(mg.getConstantPool(), 1));
        il.append(InstructionConst.ISUB);
        il.append(InstructionFactory.createStore(Type.INT, endVarIndex));


        InstructionHandle ih_15 = il.append(new ILOAD(endVarIndex));
        il.append(new F2I());
        il.append(new ISTORE(endFinalVarIndex));
        if_icmple_10.setTarget(ih_15);

        return il;
    }

    public static InstructionList getInstructionsEndInit(ClassGen cg, MethodGen mg, short dataSize) {
        InstructionList il = new InstructionList();
        int loopIteratorIndex = VariableUtils.getLVarByName(LaunchProperties.LOOP_ITERATOR_NAME, mg.getLocalVariableTable(cg.getConstantPool())).getIndex();
        int endVarIndex = VariableUtils.getLVarByName(LaunchProperties.STOP_CONDITION_NAME, mg.getLocalVariableTable(cg.getConstantPool())).getIndex();
        int numThreadsFieldIndex = VariableUtils.getFieldRefId(cg, LaunchProperties.NUMBER_OF_THREADS_NAME);

        il.append(new ILOAD(loopIteratorIndex));
        il.append(new ICONST(1));
        il.append(new IADD());
        il.append(new SIPUSH(dataSize));//TODO would be nice to get rid of short
        il.append(new GETSTATIC(numThreadsFieldIndex));
        il.append(new IDIV());
        il.append(new IMUL());
        il.append(new ICONST(1));
        il.append(new ISUB());
        il.append(new ISTORE(endVarIndex));


        return il;
    }

    public static InstructionList getInstructionsStartInit(ClassGen cg, MethodGen mg, short dataSize) {
        InstructionList il = new InstructionList();


        int loopIteratorIndex = VariableUtils.getLVarByName(LaunchProperties.LOOP_ITERATOR_NAME, mg.getLocalVariableTable(cg.getConstantPool())).getIndex();
        int startVarIndex = VariableUtils.getLVarByName(LaunchProperties.START_CONDITION_NAME, mg.getLocalVariableTable(cg.getConstantPool())).getIndex();
        int numThreadsFieldIndex = VariableUtils.getFieldRefId(cg, LaunchProperties.NUMBER_OF_THREADS_NAME);

        il.append(new ILOAD(loopIteratorIndex));
        il.append(new SIPUSH(dataSize));//TODO would be nice to get rid of short
        il.append(new GETSTATIC(numThreadsFieldIndex));
        il.append(new IDIV());
        il.append(new IMUL());
        il.append(new ISTORE(startVarIndex));

        return il;
    }

    public static HashMap<Integer, Integer> getHashmapPositionId(MethodGen mg) {
        HashMap<Integer, Integer> hashmapPositionId = new HashMap<>();
        InstructionHandle[] ihy = mg.getInstructionList().getInstructionHandles();
        for (int i = 0; i < ihy.length; i++) hashmapPositionId.put(ihy[i].getPosition(), i);
        return hashmapPositionId;
    }

}

