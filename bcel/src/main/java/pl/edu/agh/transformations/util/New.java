package pl.edu.agh.transformations.util;

import org.apache.bcel.Const;
import org.apache.bcel.generic.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class New {


    public static int CreateArrayField(String name,
                                       MethodGen mg, InstructionList il, ConstantPoolGen cp,
                                       Type t, int N, int[] nn){

        int index = cp.addArrayClass(new ArrayType(t, 1));

        LocalVariableGen lg = mg.addLocalVariable(name, new ArrayType(Type.INT, N), null, null);
        int id = lg.getIndex();

        il.append(new LDC(cp.addInteger(nn.length)));
        il.append(new ANEWARRAY(index));
        il.append(new DUP());

        for (int item = 0; item < nn.length; item++) {

            il.append(new LDC(cp.addInteger(item)));
            il.append(new LDC(cp.addInteger(nn[item])));
            il.append(new NEWARRAY((BasicType) t));
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


        il.append(new ASTORE(id));
//        il.append(new ALOAD(index));
//        il.append(new RETURN());
        return  id;
    }


    public static int CreateObjectClass(String name, LocalVariableGen lg,InstructionList il, InstructionFactory factory, MethodGen mg,
                                        String ClassName, int[] paramsID){

        il.append(factory.createNew(ClassName));
        il.append(InstructionConst.DUP);

        for (int i = 0; i < paramsID.length; i++) {
            il.append(new ALOAD(paramsID[i]));
        }

//      PARAMETRAMI TEJ KONKRETNEJ (TWORZEONEJ) FUNKCJI SA DWIE TABLICE 2D
//      NA PRZYSZLOSC TRZEBA BY OBSLUGIWAC DOWOLNY PRZYPADEK


        il.append(factory.createInvoke( ClassName, "<init>", Type.VOID,
                new Type[]{new ArrayType(Type.INT,2),new ArrayType(Type.INT,2)},
                Const.INVOKESPECIAL));

        lg = mg.addLocalVariable(name, new ObjectType(ClassName), null, null);
        int object_id = lg.getIndex();
        lg.setStart(il.append(new ASTORE(object_id)));
//        il.append(new ALOAD(object_id));
        return object_id;
    }


    public static void PrintArray(
                InstructionList il, MethodGen mg, InstructionFactory factory,
                Integer id_Array, boolean isMultiDim) {

        il.append(factory.createFieldAccess(
                "java.lang.System", "out",
                new ObjectType("java.io.PrintStream"), Const.GETSTATIC));

        il.append(new DUP2());
        il.append(new DLOAD(id_Array));

        String printNameMethod = isMultiDim == false ? "toString" : "deepToString";

        il.append(factory.createInvoke(
                "java.util.Arrays", printNameMethod,
                Type.DOUBLE, new Type[]{Type.STRING}, Const.INVOKESTATIC));

        il.append(factory.createInvoke(
                "java.io.PrintStream", "print",
                Type.VOID, new Type[]{Type.STRING}, Const.INVOKEVIRTUAL));

    }

    public static int create_simple_array(String name,MethodGen mg,Type t, int N){

        LocalVariableGen lg = mg.addLocalVariable(name, new ArrayType(t, N), null, null);
        int id = lg.getIndex();
        return  id;
    }


    public static int createFieldName(String name,InstructionList il,MethodGen mg){
        LocalVariableGen lg = mg.addLocalVariable(name, Type.STRING, null, null);
        int id = lg.getIndex();
        il.append(InstructionConst.ACONST_NULL);//il.append(new LDC(cp.addInteger(value)));
        lg.setStart(il.append(new ASTORE(id)));
        return id;
    }

        public static int create_field_array_type(String name, LocalVariableGen lg, InstructionList il, ConstantPoolGen cp, MethodGen mg,
                                              Type type, Integer dim, Integer n) {
        lg = mg.addLocalVariable(name, new ArrayType(type, dim), null, null);
        int id = lg.getIndex();
        if (n > 0) {
            il.append(new PUSH(cp, n));
            il.append(new NEWARRAY((BasicType) type));
            il.append(new ASTORE(id));
        }
        return id;
    }

    public static void SaveModifiedClass(ClassGen cg, String PATH_TO_OUTPUT_FILE) throws FileNotFoundException {
       try{
           FileOutputStream fos = new FileOutputStream(PATH_TO_OUTPUT_FILE);
           cg.getJavaClass().dump(fos);
           fos.close();
        } catch (IOException e) {
            throw new RuntimeException("Error during modified class save.", e);
        }
        System.out.println("*********************************** DONE! ***********************************\n" +
                "check the locations:\t"    +PATH_TO_OUTPUT_FILE);
    }
}


