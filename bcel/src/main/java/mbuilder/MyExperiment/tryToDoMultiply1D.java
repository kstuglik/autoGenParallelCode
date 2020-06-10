package mbuilder.MyExperiment;

import org.apache.bcel.Const;
import org.apache.bcel.generic.*;

import java.io.IOException;


public class tryToDoMultiply1D {

    static String packageName;
    static String className;
    static String fullClassName;

    static ClassGen cg;
    static ConstantPoolGen cp;
    static InstructionList il;
    static InstructionFactory factory;
    static MethodGen mg;
    static LocalVariableGen lg;

    static ObjectType p_stream;

    public tryToDoMultiply1D() {
        packageName = "mbuilder";
        className = "MatrixMul";
        fullClassName = packageName + "." + className;
        cg = new ClassGen(fullClassName, "java.lang.Object", "<generated>",
                Const.ACC_PUBLIC | Const.ACC_SUPER, null);
        cp = cg.getConstantPool(); // cg creates constant pool
        il = new InstructionList();
        factory = new InstructionFactory(cg);
        mg = new MethodGen(Const.ACC_PUBLIC | Const.ACC_STATIC,
                Type.VOID, new Type[]{new ArrayType(Type.STRING, 1)},
                new String[]{"argv"}, "main", className, il, cp);
        p_stream = new ObjectType("java.io.PrintStream");
    }

    public static int create_field_integer(String name) {
        lg = mg.addLocalVariable(name, Type.INT, null, null);
        int id = lg.getIndex();
        return id;
    }

    public static int create_field_ldc_integer(String name, Integer value) {
        lg = tryToDoMultiply1D.mg.addLocalVariable(name, Type.INT, null, null);
        int id = lg.getIndex();
        il.append(new LDC(cp.addInteger(value)));
        lg.setStart(il.append(new ISTORE(id)));
        return id;
    }

    public static int create_field_ldc_double(String name, Double value) {
        lg = mg.addLocalVariable(name, Type.DOUBLE, null, null);
        int id = lg.getIndex();
        il.append(new LDC(cp.addDouble(value)));
        lg.setStart(il.append(new DSTORE(id)));
        return id;
    }

    public static int create_field_array_type(Type type, String name, Integer dim, Integer n) {
        lg = mg.addLocalVariable(name, new ArrayType(type, dim), null, null);
        int id = lg.getIndex();
        if (n > 0) {
            il.append(new PUSH(cp, n));
            il.append(new NEWARRAY((BasicType) type));
            il.append(new ASTORE(id));
        }
        return id;
    }

    public static int create_field_array_type_init(Type type, String name, Integer dim, Integer id_2) {
        int id = create_field_array_type(type, name, dim, 0);
        il.append(new ILOAD(id_2));
        il.append(new NEWARRAY((BasicType) type));
        il.append(new ASTORE(id));
        return id;
    }

    public static void init_compare_for_loop(InstructionHandle loopStart, Integer idCounter, Integer idBoundValue, Integer incValue) {
        il.append(new IINC(idCounter, 1));
        InstructionHandle loop_compare = il.append(new ILOAD(idCounter));
        il.append(new ILOAD(idBoundValue));
        il.append(new IF_ICMPLT(loopStart));
        il.insert(loopStart, new GOTO(loop_compare));
    }

    public static void confirm_and_save(String pathOutput) {
        mg.setMaxStack();
        mg.setMaxLocals();
//        mg.stripAttributes(true);
        cg.addMethod(mg.getMethod());
        il.dispose();
        cg.addEmptyConstructor(Const.ACC_PUBLIC);
        try {
            cg.getJavaClass().dump(pathOutput);
        } catch (IOException e) {
            System.err.println(e);
        }
        System.out.println("DONE!");
    }

    //simplified arithmetic expression creation process
    public static void create_simple_expression(int[] idOperands, String[] operators) {
        //example:  width = rowsA * colsB

        int counterOperands = idOperands.length;
        int counterOperators = operators.length;

        il.append(new ILOAD(idOperands[0]));

        for (int i = 0; i < counterOperators; i++) {

            il.append(new ILOAD(idOperands[i + 1]));
            //imul & dmul and others.. fmul/lmul is TO DO?
            if (operators[i] == "imul") {
                il.append(new IMUL());
            } else if (operators[i] == "dmul") {
                il.append(new DMUL());
            } else if (operators[i] == "ddiv") {
                il.append(new DDIV());
            } else if (operators[i] == "idiv") {
                il.append(new IDIV());
            } else if (operators[i] == "iadd") {
                il.append(new IADD());
            } else if (operators[i] == "dadd") {
                il.append(new DADD());
            } else if (operators[i] == "isub") {
                il.append(new ISUB());
            }
            //(operators[i]s[0] == "dsub")
            else {
                il.append(new DSUB());
            }
        }
        il.append(new ISTORE(idOperands[counterOperands - 1]));
    }

    public static void println_array(Integer id_Array) {

        int slot = mg.isStatic() ? 0 : 1;
        il.append(factory.createFieldAccess(
                "java.lang.System", "out",
                new ObjectType("java.io.PrintStream"), Const.GETSTATIC));

        il.append(new DUP());
        il.append(new DUP());

        il.append(new DLOAD(id_Array));
        il.append(factory.createInvoke(
                "java.util.Arrays", "toString",
                Type.DOUBLE, new Type[]{Type.STRING}, Const.INVOKESTATIC));

        il.append(factory.createInvoke(
                "java.io.PrintStream", "print",
                Type.VOID, new Type[]{Type.STRING}, Const.INVOKEVIRTUAL));

    }

    public static void println_string(String result) {
        final InstructionHandle ih = il.append(
                factory.createFieldAccess(
                        "java.lang.System",
                        "out",
                        p_stream,
                        Const.GETSTATIC
                )
        );

        il.append(new LDC(cp.addString(result)));

        il.append(
                factory.createInvoke(
                        "java.io.PrintStream", "println",
                        Type.VOID, new Type[]{Type.STRING}, Const.INVOKEVIRTUAL
                )
        );
    }


    public static void main(String[] args) {

        //////////////////////////////////////////////////////////////
        //        for(int i = 0; i < rowsA; ++i) {
        //            for(int j = 0; j < colsB; ++j) {
        //                int id_c = i * colsB + j;
        //
        //                for(int k = 0; k < rowsB; ++k) {
        //                    int id_a = k * rowsA + i;
        //                    int id_b = j * rowsB + k;
        //                    C[id_c] += A[id_a] * B[id_b];
        //                }
        //            }
        //        }
        /////////////////////////////////////////////////////////////
        tryToDoMultiply1D fL = new tryToDoMultiply1D();

        int[] idOperands;
        String[] operators;
        println_string("COLUMN-MAJOR MATRIX, FILL MATRIXS!\n");

        int id_A = create_field_array_type(Type.INT, "A", 1, 6);
        int id_RowsA = create_field_ldc_integer("rowsA", 0);
        int id_ColsA = create_field_ldc_integer("colsA", 0);

        int id_B = create_field_array_type(Type.INT, "B", 1, 6);
        int id_RowsB = create_field_ldc_integer("rowsB", 0);
        int id_ColsB = create_field_ldc_integer("colsB", 0);


        //int width = rowsA * colsB => order of operands: rowsA, colsB, width
        int id_Width = create_field_integer("width");
        create_simple_expression(
                new int[]{id_RowsA, id_ColsB, id_Width},
                new String[]{"imul"});


        //double[] C = new double[width];
        int id_C = create_field_array_type_init(Type.INT, "C", 1, id_Width);

        //1.loopStart
        int id_I = create_field_ldc_integer("i", 0);
        InstructionHandle loopStart = il.append(new LDC(cp.addInteger(0)));

        //2.loopStart2
        int id_J = create_field_ldc_integer("j", 0);
        int id_c = create_field_integer("id_c");

        InstructionHandle loopStart2 = il.append(new ILOAD(id_I));
        create_simple_expression(
                new int[]{id_I, id_ColsB, id_J, id_c},
                new String[]{"imul", "iadd"});

        //3.loopStart3: body: sum += A[i] * B[j];
        int id_K = create_field_ldc_integer("k", 0);
        InstructionHandle loopStart3 = il.append(new DLOAD(id_A));

        int id_a = create_field_integer("id_a");
        create_simple_expression(
                new int[]{id_K, id_RowsA, id_I, id_a},
                new String[]{"imul", "iadd"});

        int id_b = create_field_integer("id_b");
        create_simple_expression(
                new int[]{id_J, id_RowsB, id_K, id_b},
                new String[]{"imul", "iadd"});


        //C[id_c] += A[id_a] * B[id_b];

        il.append(new DLOAD(id_C));
        il.append(new ILOAD(id_c));

        il.append(new DLOAD(id_A));
        il.append(new ILOAD(id_a));
        il.append(new DALOAD());

        il.append(new DLOAD(id_B));
        il.append(new ILOAD(id_b));
        il.append(new DALOAD());

        il.append(new IMUL());

        il.append(new DLOAD(id_C));
        il.append(new ILOAD(id_c));
        il.append(new DALOAD());

        il.append(new IADD());

        il.append(new DASTORE());


        //3.Compare: k < r2 - for(int j = 0; j < c2; ++j)
        init_compare_for_loop(loopStart3, id_K, id_RowsB, 1);

//        println_integer(id_C,id_c);

        //2.Compare: j < c2 - for(int j = 0; j < c2; ++j)
        init_compare_for_loop(loopStart2, id_J, id_ColsB, 1);

        //1.Compare: i < r1 - for(int i = 0; i < r1; ++i)
        init_compare_for_loop(loopStart, id_I, id_RowsA, 1);

        println_array(id_C);

        il.append(new RETURN());

        confirm_and_save("src/main/java/mbuilder/classFiles/MatrixMul.class");
    }
}