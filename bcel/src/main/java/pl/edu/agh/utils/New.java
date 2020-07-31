package pl.edu.agh.utils;

import org.apache.bcel.Const;
import org.apache.bcel.generic.*;
import pl.edu.agh.transformations.ByteCodeModifier;

import java.io.IOException;


public class New {

    public static int createFieldLDC(Type t, String name, Object value, ByteCodeModifier bcm) {
        bcm._lvg = bcm._mg.addLocalVariable(name, t, null, null);
        int id = bcm._lvg.getIndex();

        if (t == Type.FLOAT) bcm._il_new.append(new LDC(bcm._cp.addFloat((Float) value)));
        else if (t == Type.DOUBLE)
            bcm._il_new.append(new LDC(bcm._cp.addDouble((Double) value)));
        else if (t == Type.STRING)
            bcm._il_new.append(new LDC(bcm._cp.addString(String.valueOf(value))));
        else if (t == Type.INT)
            bcm._il_new.append(new LDC(bcm._cp.addInteger((Integer) value)));
        else bcm._il_new.append(new PUSH(bcm._cp, (Boolean) value));

        bcm._lvg.setStart(bcm._il_new.append(new ASTORE(id)));
        return id;
    }

    public static int createFieldLDC0(Type t, String name, ByteCodeModifier bcm) {
        bcm._lvg = bcm._mg.addLocalVariable(name, t, null, null);
        return bcm._lvg.getIndex();
    }

    public static int createFieldArray2D(String name, BasicType t, int[][] arr, ByteCodeModifier bcm) {

        int _id_array_1 = bcm._cp.addArrayClass(new ArrayType(t, 1));

        LocalVariableGen lg = bcm._mg.addLocalVariable(name, new ArrayType(Type.INT, arr.length), null, null);
        int _id_array = lg.getIndex();

        bcm._il_new.append(new LDC(bcm._cp.addInteger(arr.length)));
        bcm._il_new.append(new ANEWARRAY(_id_array_1));
        bcm._il_new.append(new DUP());

        for (int item = 0; item < arr.length; item++) {
            bcm._il_new.append(new LDC(bcm._cp.addInteger(item)));
            bcm._il_new.append(new LDC(bcm._cp.addInteger(arr[item].length)));
            bcm._il_new.append(new NEWARRAY(t));
            bcm._il_new.append(new DUP());

            for (int i = 0; i < arr[item].length; i++) {
                bcm._il_new.append(new LDC(bcm._cp.addInteger(i)));
                bcm._il_new.append(new LDC(bcm._cp.addInteger(arr[item][i])));
                bcm._il_new.append(new IASTORE());
                if (i == arr[item].length - 1) bcm._il_new.append(new AASTORE());
                bcm._il_new.append(new DUP());
            }
        }

        bcm._il_new.append(new ASTORE(_id_array));
        return _id_array;
    }

    public static int createFieldArray1D(String name, Type t, int[] arr, ByteCodeModifier bcm) {
        bcm._factory = new InstructionFactory(bcm._modifiedClass, bcm._cp);

        LocalVariableGen lg = bcm._mg.addLocalVariable(name, new ArrayType(t, 1), null, null);
        int _id_array = lg.getIndex();

        bcm._il_new.append(new PUSH(bcm._cp, arr.length)); // size of array
        bcm._il_new.append(bcm._factory.createNewArray(t, (short) 1)); // one dimension for dim size array
        for (int i = 0; i < arr.length; i++) {
            bcm._il_new.append(new DUP());
            bcm._il_new.append(new PUSH(bcm._cp, i));
            bcm._il_new.append(new PUSH(bcm._cp, arr[i]));
            bcm._il_new.append(new AASTORE());
        }
        bcm._il_new.append(InstructionFactory.createStore(Type.OBJECT, _id_array));
        return _id_array;
    }

    public static int createFieldArrayInit(BasicType t, String name, Integer dim, Integer id, ByteCodeModifier bcm) {
        bcm._lvg = bcm._mg.addLocalVariable(name, new ArrayType(t, dim), null, null);
        int _id_field = bcm._lvg.getIndex();
        bcm._il_new.append(new ALOAD(id));
        bcm._il_new.append(new NEWARRAY(t));
        bcm._il_new.append(new ASTORE(_id_field));
        return _id_field;
    }

    public static int createFieldObjectClass(String name, String ClassName, int[] paramsID, ByteCodeModifier bcm) {

        bcm._il_new.append(bcm._factory.createNew(ClassName));
        bcm._il_new.append(InstructionConst.DUP);

        for (int value : paramsID) bcm._il_new.append(new ALOAD(value));

//      PARAMETRAMI TEJ KONKRETNEJ FUNKCJI SA dwie TABLICE 2D, NA PRZYSZLOSC OBSLUGIWAC DOWOLNY PRZYPADEK

        bcm._il_new.append(bcm._factory.createInvoke(ClassName, "<init>", Type.VOID,
                new Type[]{new ArrayType(Type.INT, 2), new ArrayType(Type.INT, 2)}, Const.INVOKESPECIAL));

        LocalVariableGen lg = bcm._mg.addLocalVariable(name, new ObjectType(ClassName), null, null);
        int _id_object = lg.getIndex();
        lg.setStart(bcm._il_new.append(new ASTORE(_id_object)));
//        il.append(new ALOAD(object_id));
        return _id_object;
    }

    public static String center(String s, int length, char c) {
        StringBuilder sb = new StringBuilder(length);
        sb.setLength((length - s.length()) / 2);
        sb.append(s);
        sb.setLength(length);
        return sb.toString().replace('\0', c);
    }

    public static void initCompareForLoop(InstructionHandle loopStart, Integer id, Integer idBoundValue, ByteCodeModifier bcm) {
        bcm._il_new.append(new IINC(id, 1));
        InstructionHandle loop_compare = bcm._il_new.append(new ILOAD(id));
        bcm._il_new.append(new ILOAD(idBoundValue));
        bcm._il_new.append(new IF_ICMPLT(loopStart));
        bcm._il_new.insert(loopStart, new GOTO(loop_compare));
    }

    public static void createSimpleExpression(int[] idOperands, String[] operators, ByteCodeModifier bcm) {
        //example:  width = rowsA * colsB

        int counterOperands = idOperands.length;
        int counterOperators = operators.length;

        bcm._il_new.append(new ILOAD(idOperands[0]));

        for (int i = 0; i < counterOperators; i++) {

            bcm._il_new.append(new ILOAD(idOperands[i + 1]));
            //imul & dmul and others.. fmul/lmul is TO DO?
            if (operators[i] == "imul") {
                bcm._il_new.append(new IMUL());
            } else if (operators[i] == "dmul") {
                bcm._il_new.append(new DMUL());
            } else if (operators[i] == "ddiv") {
                bcm._il_new.append(new DDIV());
            } else if (operators[i] == "idiv") {
                bcm._il_new.append(new IDIV());
            } else if (operators[i] == "iadd") {
                bcm._il_new.append(new IADD());
            } else if (operators[i] == "dadd") {
                bcm._il_new.append(new DADD());
            } else if (operators[i] == "isub") {
                bcm._il_new.append(new ISUB());
            }
            //(operators[i]s[0] == "dsub")
            else {
                bcm._il_new.append(new DSUB());
            }
        }
        bcm._il_new.append(new ASTORE(idOperands[counterOperands - 1]));
    }

    public static void printInstructionFromIHandler(InstructionHandle[] loopInstructions) {
        System.out.println("******************************************************************************");
        for (InstructionHandle loopInstruction : loopInstructions) {
            Instruction instruction = loopInstruction.getInstruction();
            System.out.println(instruction.getName() + " : " + instruction.getOpcode());
        }
    }

    public static void printInstructionFromIHandler(InstructionHandle loopInstruction) {
        System.out.println(loopInstruction.getInstruction().getName() + " : " + loopInstruction.getInstruction().getOpcode());
    }

    public static void saveNewClassFile(ClassGen modifiedClass, String CLASS_PATH, String CLASS_NAME) throws IOException, IOException {
        String PATH_TO_OUTPUT_FILE = CLASS_PATH + CLASS_NAME + "_MOD.class";
        modifiedClass.getJavaClass().dump(PATH_TO_OUTPUT_FILE);
        System.out.println("*********************************** DONE! ***********************************\n" +
                "Go to file:\t" + PATH_TO_OUTPUT_FILE);
    }

    public static int createArrayField(String name, MethodGen mg, InstructionList il, ConstantPoolGen cp, Type t, int N, int[] nn) {

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
                il.append(new LDC(cp.addInteger(i - 1)));
                il.append(new LDC(cp.addInteger((int) Math.pow(2, i))));
                il.append(new IASTORE());
                if (i == nn[item]) {
                    il.append(new AASTORE());
                }
                il.append(new DUP());
            }
        }


        il.append(new ASTORE(id));
//        il.append(new ALOAD(index));
//        il.append(new RETURN());
        return id;
    }

    public static int createObjectClass(String name, String ClassName, InstructionList il, InstructionFactory factory, MethodGen mg, int[] paramsID) {

        il.append(factory.createNew(ClassName));
        il.append(InstructionConst.DUP);

        for (int i = 0; i < paramsID.length; i++) {
            il.append(new ALOAD(paramsID[i]));
        }

//      PARAMETRAMI TEJ KONKRETNEJ (TWORZEONEJ) FUNKCJI SA DWIE TABLICE 2D
//      NA PRZYSZLOSC TRZEBA BY OBSLUGIWAC DOWOLNY PRZYPADEK


        il.append(factory.createInvoke(ClassName, "<init>", Type.VOID,
                new Type[]{new ArrayType(Type.INT, 2), new ArrayType(Type.INT, 2)},
                Const.INVOKESPECIAL));

        LocalVariableGen lg = mg.addLocalVariable(name, new ObjectType(ClassName), null, null);
        int object_id = lg.getIndex();
        lg.setStart(il.append(new ASTORE(object_id)));
//        il.append(new ALOAD(object_id));
        return object_id;
    }

    public static void printArray(InstructionList il, MethodGen mg, InstructionFactory factory, Integer id_Array, boolean isMultiDim) {

        il.append(factory.createFieldAccess(
                "java.lang.System", "out",
                new ObjectType("java.io.PrintStream"), Const.GETSTATIC));

        il.append(isMultiDim ? new DUP() : new DUP2());

        il.append(new DLOAD(id_Array));

        il.append(factory.createInvoke(
                "java.util.Arrays",
                isMultiDim ? "toString" : "deepToString",
                Type.FLOAT, new Type[]{Type.STRING}, Const.INVOKESTATIC));

        il.append(factory.createInvoke(
                "java.io.PrintStream", "print",
                Type.VOID, new Type[]{Type.STRING}, Const.INVOKEVIRTUAL));

    }

    public static int getLoacalVariableID(String fieldName, ConstantPoolGen cp, MethodGen mg) {
        return LocalVariableUtils.findLocalVariableByName(fieldName, mg.getLocalVariableTable(cp)).getIndex();
    }

}


