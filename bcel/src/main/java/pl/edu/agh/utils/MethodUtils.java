package pl.edu.agh.utils;

import org.apache.bcel.classfile.ConstantCP;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

import static pl.edu.agh.utils.LoopUtils.getGoto;
import static pl.edu.agh.utils.LoopUtils.getIhsBetweenFromTo;


public class MethodUtils {

    public static int GetMethodIndex(Method[] methods, String methodName) {

        int methodPositionId;

        for (methodPositionId = 0; methodPositionId < methods.length; methodPositionId++) {
            if (methods[methodPositionId].getName().equals(methodName)) {
                break;
            }
        }
        if (methodPositionId < methods.length) {
            return methodPositionId;
        } else {
            System.err.println("Method: " + methodName + " not found!");
            return -1;
        }
    }

    private static Predicate<? super InstructionHandle> doesReferenceOldClass() {
        return handle -> handle.getInstruction() instanceof FieldOrMethod;
    }

    public static Optional<MethodGen> findMethodByName(ClassGen cg, String methodName) {
        return Arrays.stream(cg.getMethods())
                .filter(method -> methodName.equals(method.getName()))
                .findFirst()
                .map(method -> new MethodGen(method, cg.getClassName(), cg.getConstantPool()));
    }

    public static MethodGen findMethodByNameOrThrow(ClassGen cg, String methodName) {
        return Arrays.stream(cg.getMethods())
                .filter(method -> methodName.equals(method.getName()))
                .findFirst()
                .map(method -> new MethodGen(method, cg.getClassName(), cg.getConstantPool()))
                .orElseThrow(() -> new IllegalStateException("Method with name " + methodName + " not found."));
    }

    //        getBodyFromMethodToNewInstructionList
    //
    //        CONCEPTION: COPY ALL INSTRUCTION FROM SELECTED METHOD (IL_OLD)
    //        AND APPEND TO NEW INSTRUCTION LIST (IL)

    public static void getBodyFromMethodToNewInstructionList(
            MethodGen mg, InstructionList il, InstructionList il_old) {

        int GOTO_ID = getGoto(il_old.getInstructionHandles()).getPosition();

        InstructionHandle[] handles = getIhsBetweenFromTo(
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
    }

    public static void switchConstantRefsToNewClass(ClassGen cg, Method method) {
        MethodGen mg = new MethodGen(method, cg.getClassName(), cg.getConstantPool());
        InstructionHandle[] ih = mg.getInstructionList().getInstructionHandles();
        Arrays.stream(ih)
                .filter(doesReferenceOldClass())
                .map(handle -> (FieldOrMethod) handle.getInstruction())
                .forEach(instr -> switchReference(cg, instr));
    }

    //        getBodyFromMethodToNewInstructionList
    //
    //        CONCEPTION: COPY ALL INSTRUCTION FROM SELECTED METHOD (IL_OLD)
    //        AND APPEND THIS INSTRUCTION TO IL

    private static void switchReference(ClassGen cg, FieldOrMethod instr) {
        ConstantPoolGen cp = cg.getConstantPool();
        int classNameIndex = cg.getClassNameIndex();
        ConstantCP fieldref = (ConstantCP) cp.getConstant(instr.getIndex());
        ConstantClass constantClass = (ConstantClass) cp.getConstant(fieldref.getClassIndex());
        String className = (String) constantClass.getConstantValue(cp.getConstantPool());
        if (cg.getClassName().contains(className)) {
            fieldref.setClassIndex(classNameIndex);
        }
    }
}