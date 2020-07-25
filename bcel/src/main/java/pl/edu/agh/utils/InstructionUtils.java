package pl.edu.agh.utils;

import org.apache.bcel.Const;
import org.apache.bcel.generic.*;
import pl.edu.agh.transformations.LaunchProperties;

import java.util.Arrays;

public class InstructionUtils {

    static InstructionHandle findByInstruction(Instruction i, InstructionHandle[] ih) {
        return Arrays.stream(ih)
                .filter(handle -> handle.getInstruction().equals(i))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No matching instruction found for instruction handle."));
    }

    public static InstructionList getStartInitInstructions(ClassGen cg, MethodGen mg, short dataSize) {
        InstructionList il = new InstructionList();
        int loopIteratorIndex = LocalVariableUtils.findLocalVariableByName(LaunchProperties.LOOP_ITERATOR_NAME, mg.getLocalVariableTable(cg.getConstantPool())).getIndex();
        int startVarIndex = LocalVariableUtils.findLocalVariableByName(LaunchProperties.START_INDEX_VARIABLE_NAME, mg.getLocalVariableTable(cg.getConstantPool())).getIndex();
        int numThreadsFieldIndex = ConstantPoolUtils.getFieldIndex(cg, LaunchProperties.NUMBER_OF_THREADS_NAME);

        il.append(new ILOAD(loopIteratorIndex));
        il.append(new SIPUSH(dataSize));//TODO would be nice to get rid of short
        il.append(new GETSTATIC(numThreadsFieldIndex));
        il.append(new IDIV());
        il.append(new IMUL());
        il.append(new ISTORE(startVarIndex));

        return il;
    }

    public static InstructionList getEndInitInstructions(ClassGen cg, MethodGen mg, short dataSize) {
        InstructionList il = new InstructionList();
        int loopIteratorIndex = LocalVariableUtils.findLocalVariableByName(LaunchProperties.LOOP_ITERATOR_NAME, mg.getLocalVariableTable(cg.getConstantPool())).getIndex();
        int endVarIndex = LocalVariableUtils.findLocalVariableByName(LaunchProperties.END_INDEX_VARIABLE_NAME, mg.getLocalVariableTable(cg.getConstantPool())).getIndex();
        int numThreadsFieldIndex = ConstantPoolUtils.getFieldIndex(cg, LaunchProperties.NUMBER_OF_THREADS_NAME);

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

    public static InstructionList getEndInitInstructions2(ClassGen cg, MethodGen mg, short dataSize) {
        InstructionList il = new InstructionList();
        int endVarIndex = LocalVariableUtils.findLocalVariableByName(LaunchProperties.END_INDEX_VARIABLE_NAME, mg.getLocalVariableTable(cg.getConstantPool())).getIndex();
        int endVarIndex2 = LocalVariableUtils.findLocalVariableByName(LaunchProperties.FINAL_END_INDEX_VARIABLE_NAME, mg.getLocalVariableTable(cg.getConstantPool())).getIndex();
        int startVarIndex = LocalVariableUtils.findLocalVariableByName(LaunchProperties.START_INDEX_VARIABLE_NAME, mg.getLocalVariableTable(cg.getConstantPool())).getIndex();


        il.append(new ILOAD(endVarIndex));
        il.append(new I2F());
        il.append(new ISTORE(endVarIndex2));


        return il;
    }

    public static InstructionList getTaskAdd(ClassGen cg, MethodGen mg, short dataSize) {
        InstructionList il = new InstructionList();
        int endVarIndex = LocalVariableUtils.findLocalVariableByName(LaunchProperties.END_INDEX_VARIABLE_NAME, mg.getLocalVariableTable(cg.getConstantPool())).getIndex();
        int endVarIndex2 = LocalVariableUtils.findLocalVariableByName(LaunchProperties.FINAL_END_INDEX_VARIABLE_NAME, mg.getLocalVariableTable(cg.getConstantPool())).getIndex();
        int startVarIndex = LocalVariableUtils.findLocalVariableByName(LaunchProperties.START_INDEX_VARIABLE_NAME, mg.getLocalVariableTable(cg.getConstantPool())).getIndex();

        InstructionFactory _factory = new InstructionFactory(cg, mg.getConstantPool());
//        InstructionHandle ih_69 = il.append(_factory.createLoad(Type.OBJECT, 0));
        il.append(new PUSH(mg.getConstantPool(), 10));
        il.append(new PUSH(mg.getConstantPool(), 120));
        il.append(_factory.createInvoke("subTask", "call", new ObjectType("java.util.concurrent.Callable"), new Type[]{Type.INT, Type.INT}, Const.INVOKEDYNAMIC));
        il.append(_factory.createInvoke("java.util.List", "add", Type.BOOLEAN, new Type[]{Type.OBJECT}, Const.INVOKEINTERFACE));
//        il.append(InstructionConst.POP);


        return il;
    }


}

