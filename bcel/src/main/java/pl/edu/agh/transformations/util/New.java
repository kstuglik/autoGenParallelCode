package pl.edu.agh.transformations.util;

import org.apache.bcel.Const;
import org.apache.bcel.generic.*;

public class New {

    public static int create_array_2D(String name,MethodGen mg, InstructionList il, ConstantPoolGen cp,
                                       BasicType t, int N, int[] nn){

        int index = cp.addArrayClass(new ArrayType(t, 1));

        LocalVariableGen lg = mg.addLocalVariable(name, new ArrayType(Type.INT, 2), null, null);
        int id = lg.getIndex();

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


        il.append(new ASTORE(id));
//        il.append(new ALOAD(index));
//        il.append(new RETURN());
        return  id;
    }



    public static int createFieldName(String name,InstructionList il,MethodGen mg){
        LocalVariableGen lg = mg.addLocalVariable(name, Type.STRING, null, null);
        int id = lg.getIndex();
        il.append(InstructionConst.ACONST_NULL);//il.append(new LDC(cp.addInteger(value)));
        lg.setStart(il.append(new ASTORE(id)));
        return id;
    }


    public static void createObjectClass(String name, InstructionList il, InstructionFactory factory, MethodGen mg,
                                         String ClassName, int[] paramsID){

        il.append(factory.createNew(ClassName));
        il.append(InstructionConst.DUP);

        for (int i = 0; i < paramsID.length; i++) {
            il.append(new ALOAD(paramsID[i]));
        }

//      PARAMETRAMI TEJ KONKRETNEJ (TWORZEONEJ) FUNKCJI SA 2 TABLICE 2D
//      NA PRZYSZLOSC MOZNA TO UJEDNOLICIC BY OBSLUGIWAC DOWOLNY PRZYPADEK

        il.append(factory.createInvoke( ClassName, "<init>", Type.VOID,
                new Type[]{new ArrayType(Type.INT,2),new ArrayType(Type.INT,2)},
                Const.INVOKESPECIAL));

        LocalVariableGen lg = mg.addLocalVariable(name, new ObjectType(ClassName), null, null);
        int object_id = lg.getIndex();
        lg.setStart(il.append(new ASTORE(object_id)));
        il.append(new ALOAD(object_id));
    }
}


