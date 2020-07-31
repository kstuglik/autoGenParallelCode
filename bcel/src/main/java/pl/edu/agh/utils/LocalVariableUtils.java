package pl.edu.agh.utils;

import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;

import java.util.Arrays;

public class LocalVariableUtils {

    public static LocalVariable findLocalVariableByName(String name, LocalVariableTable localVariableTable) {
        return Arrays.stream(localVariableTable.getLocalVariableTable())
                .filter(var -> var.getName().equals(name))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Variable " + name + " not found by name."));
    }

//    public static String ClassVariable(ClassGen cg, String searchField) throws Exception {
//        final Field[] fields = cg.getFields();
//        Field f = null;
//        for (final Field field : fields) {
//            if (field.getName().equals(searchField)) {
//                f = field;
//                break;
//            }
//        }
//
//        return f.getSignature();
//    }

    public static String getSignatureForLocalVariableByName(String name, LocalVariableTable localVariableTable) {
        return Arrays.stream(localVariableTable.getLocalVariableTable())
                .filter(var -> var.getName().equals(name))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Variable " + name + " not found by name.")).getSignature();
    }

    public static LocalVariable getINstructionInvoke(String name, LocalVariableTable localVariableTable) {
        return Arrays.stream(localVariableTable.getLocalVariableTable())
                .filter(var -> var.getSignature().equals("invokestatic"))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Variable " + name + " not found by name."));
    }



}
