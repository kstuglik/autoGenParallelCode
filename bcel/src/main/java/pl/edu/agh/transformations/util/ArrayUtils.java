package pl.edu.agh.transformations.util;

import org.apache.bcel.generic.*;

public class ArrayUtils {

    public static void create_array_2D(InstructionList il,
                                       ConstantPoolGen cp,
                                       BasicType t,
                                       int N, int[] nn){

        int index = cp.addArrayClass(new ArrayType(t, 1));

        il.append(new LDC(cp.addInteger(N)));
        il.append(new ANEWARRAY(index));
        il.append(new DUP());

        for (int item = 0; item < N; item++) {

            il.append(new LDC(cp.addInteger(item)));
            il.append(new LDC(cp.addInteger(nn[item])));
            il.append(new NEWARRAY(t));
            il.append(new DUP());

            for (int i = 1; i <= nn[item]; i++) {
                il.append(new LDC(cp.addInteger(i-1)));
                il.append(new LDC(cp.addInteger((int) Math.pow(2, i))));
                il.append(new IASTORE());
                if(i==nn[item]){
                   il.append(new AASTORE());
                }
                il.append(new DUP());
            }
        }

        il.append(new ASTORE(index));
        il.append(new RETURN());
    }

}


