package pl.edu.agh.transformations.utils;

import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import org.apache.bcel.generic.FieldOrMethod;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;


public class MethodUtils {

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

    public static void switchConstantRefsToNewClass(ClassGen cg, Method method) {
        MethodGen mg = new MethodGen(method, cg.getClassName(), cg.getConstantPool());
        InstructionHandle[] ih = mg.getInstructionList().getInstructionHandles();
        Arrays.stream(ih)
                .filter(doesReferenceOldClass())
                .map(handle -> (FieldOrMethod) handle.getInstruction())
                .forEach(instr -> switchReference(cg, instr));
    }

    private static Predicate<? super InstructionHandle> doesReferenceOldClass() {
        return handle -> handle.getInstruction() instanceof FieldOrMethod;
    }

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
}