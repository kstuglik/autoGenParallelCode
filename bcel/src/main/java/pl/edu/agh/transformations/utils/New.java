package pl.edu.agh.transformations.utils;

import org.apache.bcel.Const;
import org.apache.bcel.generic.*;
import pl.edu.agh.transformations.ByteCodeModifier;


public class New extends ByteCodeModifier {

    public static int createFieldLDC(Type t, String name, Object value) {
        ByteCodeModifier._lg = ByteCodeModifier._mg.addLocalVariable(name, t, null, null);
        int id = ByteCodeModifier._lg.getIndex();

        if (t == Type.FLOAT) ByteCodeModifier._il_new.append(new LDC(ByteCodeModifier._cp.addFloat((Float) value)));
        else if (t == Type.DOUBLE)
            ByteCodeModifier._il_new.append(new LDC(ByteCodeModifier._cp.addDouble((Double) value)));
        else if (t == Type.STRING)
            ByteCodeModifier._il_new.append(new LDC(ByteCodeModifier._cp.addString(String.valueOf(value))));
        else if (t == Type.INT)
            ByteCodeModifier._il_new.append(new LDC(ByteCodeModifier._cp.addInteger((Integer) value)));
        else ByteCodeModifier._il_new.append(new PUSH(ByteCodeModifier._cp, (Boolean) value));

        ByteCodeModifier._lg.setStart(ByteCodeModifier._il_new.append(new ASTORE(id)));
        return id;
    }

    public static int createFieldLDC0(Type t, String name) {
        ByteCodeModifier._lg = ByteCodeModifier._mg.addLocalVariable(name, t, null, null);
        int id = ByteCodeModifier._lg.getIndex();
        return id;
    }

    public static int createFieldArray2D(String name, Type t, int[][] arr) {

        int index = _cp.addArrayClass(new ArrayType(t, 1));

        LocalVariableGen lg = _mg.addLocalVariable(name, new ArrayType(Type.INT, arr.length), null, null);
        int id = lg.getIndex();

        _il_new.append(new LDC(_cp.addInteger(arr.length)));
        _il_new.append(new ANEWARRAY(index));
        _il_new.append(new DUP());

        for (int item = 0; item < arr.length; item++) {
            _il_new.append(new LDC(_cp.addInteger(item)));
            _il_new.append(new LDC(_cp.addInteger(arr[item].length)));
            _il_new.append(new NEWARRAY((BasicType) t));
            _il_new.append(new DUP());

            for (int i = 0; i < arr[item].length; i++) {
                _il_new.append(new LDC(_cp.addInteger(i)));
                _il_new.append(new LDC(_cp.addInteger(arr[item][i])));
                _il_new.append(new IASTORE());
                if (i == arr[item].length - 1) _il_new.append(new AASTORE());
                _il_new.append(new DUP());
            }
        }

        _il_new.append(new ASTORE(id));
        return id;
    }

    public static int createFieldArray1D(String name, Type t, int[] arr) {
        _factory = new InstructionFactory(_modifiedClass, _cp);

        LocalVariableGen lg = _mg.addLocalVariable(name, new ArrayType(t, 1), null, null);
        int id = lg.getIndex();

        _il_new.append(new PUSH(_cp, arr.length)); // size of array
        _il_new.append(_factory.createNewArray(t, (short) 1)); // one dimension for dim size array
        for (int i = 0; i < arr.length; i++) {
            _il_new.append(new DUP());
            _il_new.append(new PUSH(_cp, i));
            _il_new.append(new PUSH(_cp, arr[i]));
            _il_new.append(new AASTORE());
        }
        _il_new.append(InstructionFactory.createStore(Type.OBJECT, id));
        return id;
    }

    public static int createFieldArrayInit(BasicType t, String name, Integer dim, Integer id_2) {
        ByteCodeModifier._lg = ByteCodeModifier._mg.addLocalVariable(name, new ArrayType(t, dim), null, null);
        int id = ByteCodeModifier._lg.getIndex();
        ByteCodeModifier._il_new.append(new ALOAD(id_2));
        ByteCodeModifier._il_new.append(new NEWARRAY(t));
        ByteCodeModifier._il_new.append(new ASTORE(id));
        return id;
    }

    public static int createFieldObjectClass(String name, String ClassName, int[] paramsID) {

        _il_new.append(_factory.createNew(ClassName));
        _il_new.append(InstructionConst.DUP);

        for (int value : paramsID) _il_new.append(new ALOAD(value));

//      PARAMETRAMI TEJ KONKRETNEJ FUNKCJI SA dwie TABLICE 2D, NA PRZYSZLOSC OBSLUGIWAC DOWOLNY PRZYPADEK

        _il_new.append(_factory.createInvoke(ClassName, "<init>", Type.VOID,
                new Type[]{new ArrayType(Type.INT, 2), new ArrayType(Type.INT, 2)}, Const.INVOKESPECIAL));

        LocalVariableGen lg = _mg.addLocalVariable(name, new ObjectType(ClassName), null, null);
        int object_id = lg.getIndex();
        lg.setStart(_il_new.append(new ASTORE(object_id)));
//        il.append(new ALOAD(object_id));
        return object_id;
    }

    public static void println_array(Integer id_Array) {

        _il_new.append(_factory.createFieldAccess(
                "java.lang.System", "out", new ObjectType("java.io.PrintStream"), Const.GETSTATIC));

        _il_new.append(new DUP2());
        _il_new.append(new DLOAD(id_Array));

        _il_new.append(_factory.createInvoke(
                "java.util.Arrays", "toString", Type.DOUBLE, new Type[]{Type.STRING}, Const.INVOKESTATIC));
        _il_new.append(_factory.createInvoke(
                "java.io.PrintStream", "print", Type.VOID, new Type[]{Type.STRING}, Const.INVOKEVIRTUAL));

    }

    public static void printArray(Integer id, boolean isMultiDim) {

        _il_new.append(_factory.createFieldAccess(
                "java.lang.System", "out", new ObjectType("java.io.PrintStream"), Const.GETSTATIC));

//        _il_new.append(new DUP2());
        _il_new.append(InstructionFactory.createLoad(Type.OBJECT, id));

        _il_new.append(_factory.createInvoke(
                "java.util.Arrays", isMultiDim ? "deepToString" : "toString", Type.FLOAT, new Type[]{Type.STRING}, Const.INVOKESTATIC));

        _il_new.append(_factory.createInvoke(
                "java.io.PrintStream", "println", Type.VOID, new Type[]{Type.STRING}, Const.INVOKEVIRTUAL));

    }

    public static int getLoacalVariableID(String fieldName) {
        return LocalVariableUtils.findLocalVariableByName(
                fieldName,
                _mg.getLocalVariableTable(_cp)).getIndex();
    }

    public static String center(String s, int length, char c) {
        StringBuilder sb = new StringBuilder(length);
        sb.setLength((length - s.length()) / 2);
        sb.append(s);
        sb.setLength(length);
        return sb.toString().replace('\0', c);
    }

    public static void init_compare_for_loop(InstructionHandle loopStart, Integer id, Integer idBoundValue) {
        _il_new.append(new IINC(id, 1));
        InstructionHandle loop_compare = _il_new.append(new ILOAD(id));
        _il_new.append(new ILOAD(idBoundValue));
        _il_new.append(new IF_ICMPLT(loopStart));
        _il_new.insert(loopStart, new GOTO(loop_compare));
    }

    public static void create_simple_expression(int[] idOperands, String[] operators) {
        //example:  width = rowsA * colsB

        int counterOperands = idOperands.length;
        int counterOperators = operators.length;

        _il_new.append(new ILOAD(idOperands[0]));

        for (int i = 0; i < counterOperators; i++) {

            _il_new.append(new ILOAD(idOperands[i + 1]));
            //imul & dmul and others.. fmul/lmul is TO DO?
            if (operators[i] == "imul") {
                _il_new.append(new IMUL());
            } else if (operators[i] == "dmul") {
                _il_new.append(new DMUL());
            } else if (operators[i] == "ddiv") {
                _il_new.append(new DDIV());
            } else if (operators[i] == "idiv") {
                _il_new.append(new IDIV());
            } else if (operators[i] == "iadd") {
                _il_new.append(new IADD());
            } else if (operators[i] == "dadd") {
                _il_new.append(new DADD());
            } else if (operators[i] == "isub") {
                _il_new.append(new ISUB());
            }
            //(operators[i]s[0] == "dsub")
            else {
                _il_new.append(new DSUB());
            }
        }
        _il_new.append(new ASTORE(idOperands[counterOperands - 1]));
    }


}


