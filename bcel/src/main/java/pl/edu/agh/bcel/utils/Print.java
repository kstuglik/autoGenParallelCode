package pl.edu.agh.bcel.utils;

import org.apache.bcel.Const;
import org.apache.bcel.generic.*;

public class Print {

    public static void printArray(InstructionList il, MethodGen mg, InstructionFactory factory, Integer id_Array, boolean isMultiDim) {

        il.append(factory.createFieldAccess("java.lang.System", "out",
                new ObjectType("java.io.PrintStream"), Const.GETSTATIC));

        il.append(isMultiDim ? new DUP() : new DUP2());

        il.append(new DLOAD(id_Array));

        il.append(factory.createInvoke("java.util.Arrays", isMultiDim ? "toString" : "deepToString",
                Type.FLOAT, new Type[]{Type.STRING}, Const.INVOKESTATIC));

        il.append(factory.createInvoke("java.io.PrintStream", "print",
                Type.VOID, new Type[]{Type.STRING}, Const.INVOKEVIRTUAL));

    }

    public static String printCenter(String s, int length, char c) {
        StringBuilder sb = new StringBuilder(length);
        sb.setLength((length - s.length()) / 2);
        sb.append(s);
        sb.setLength(length);
        return sb.toString().replace('\0', c);
    }

}


