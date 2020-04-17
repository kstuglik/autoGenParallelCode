package MatrixBuilder;

import org.apache.bcel.Const;
import org.apache.bcel.generic.*;

import java.io.IOException;


public class ForLoop {

    static String packageName;
    static String className;
    static String fullClassName;

    static ClassGen cg;
    static ConstantPoolGen cp;
    static InstructionList il;
    static InstructionFactory factory;
    static MethodGen mg;
    static LocalVariableGen lg;

    public ForLoop() {
        packageName = "MatrixBuilder";
        className   = "TestForLoop";
        fullClassName = packageName + "." + className;
        cg = new ClassGen(fullClassName, "java.lang.Object", "<generated>",
                Const.ACC_PUBLIC | Const.ACC_SUPER, null);
        cp = cg.getConstantPool(); // cg creates constant pool
        il = new InstructionList();
        factory = new InstructionFactory(cg);
        mg = new MethodGen(Const.ACC_PUBLIC | Const.ACC_STATIC,
                Type.VOID, new Type[]{new ArrayType(Type.STRING, 1)},
                new String[]{"argv"}, "main", className, il, cp);
    }

    public static int create_field_integer(String name){
        lg = mg.addLocalVariable(name, Type.INT, null, null);
        int id = lg.getIndex();
        return id;
    }

    public static int create_field_ldc_integer(String name, Integer value){
        lg = ForLoop.mg.addLocalVariable(name, Type.INT, null, null);
        int id = lg.getIndex();
        il.append(new LDC(cp.addInteger(value)));
        lg.setStart(il.append(new ISTORE(id)));
        return id;
    }

    public static int create_field_ldc_double(String name, Double value){
        lg = mg.addLocalVariable(name, Type.DOUBLE, null, null);
        int id = lg.getIndex();
        il.append(new LDC(cp.addDouble(value)));
        lg.setStart(il.append(new DSTORE(id)));
        return id;
    }

    public static int create_field_array_double(String name, Integer dim, Integer n){
        lg = mg.addLocalVariable(name, new ArrayType(Type.DOUBLE, dim), null, null);
        int id = lg.getIndex();
        if(n>0){
            il.append(new PUSH(cp, n));
            il.append(new NEWARRAY(Type.DOUBLE));
            il.append(new ASTORE(id));
        }
        return id;
    }

    public static void init_compare_for_loop(InstructionHandle loopStart,Integer idCounter, Integer idBoundValue, Integer incValue){
        il.append(new IINC(idCounter, 1));
        InstructionHandle loop_compare = il.append(new ILOAD(idCounter));
        il.append(new ILOAD(idBoundValue));
        il.append(new IF_ICMPLT(loopStart));
        il.insert(loopStart, new GOTO(loop_compare));
    }

    public static void confirm_and_save(String pathOutput){
        mg.setMaxStack();
        cg.addMethod(mg.getMethod());
        il.dispose();
        cg.addEmptyConstructor(Const.ACC_PUBLIC);
        try { cg.getJavaClass().dump(pathOutput);}
        catch (IOException e) {System.err.println(e);}
        System.out.println("DONE!");
    }


    public static void main(String[] args) {

        //////////////////////////////////////////////////////////////
        //        for(int i = 0; i < r1; ++i) {
        //            for(int j = 0; j < c2; ++j) {
        //                for(int k = 0; k < r2; ++k) {
        //                    sum += A[i] * B[j];
        //                }
        //            }
        //        }
        /////////////////////////////////////////////////////////////
        ForLoop fL = new ForLoop();

        int id_Sum = create_field_ldc_double("sum",0.0);
        int id_A  = create_field_array_double("A", 1,6);
        int id_B  = create_field_array_double("B", 1,6);
        int id_R1 = create_field_ldc_integer("r1",2);
        int id_C1 = create_field_ldc_integer("c1",3);
        int id_R2 = create_field_ldc_integer("r2",3);
        int id_C2 = create_field_ldc_integer("c2",3);


        //int width = 2 * 3;
        int id_Width = create_field_integer("width");
        il.append(new LDC(cp.addInteger(2)));//Load the constant
        il.append(new LDC(cp.addInteger(3)));//Load the constant
        il.append(new IMUL());//Add
        il.append(new ISTORE(id_Width));


        //double[] C = new double[width];
        int id_C = create_field_array_double("C", 1,0);
        il.append(new ILOAD(id_Width));
        il.append(new NEWARRAY(Type.DOUBLE));
        il.append(new ASTORE(id_C));


        //1.loopStart
        int id_I = create_field_ldc_integer("i",0);
        InstructionHandle loopStart = il.append(new LDC(cp.addInteger(0)));

        //2.loopStart2
        int id_J = create_field_ldc_integer("j",0);
        InstructionHandle loopStart2 = il.append(new LDC(cp.addInteger(0)));

        //3.loopStart3
        int id_K = create_field_ldc_integer("k",0);
        InstructionHandle loopStart3 = il.append(new DLOAD(id_A));


        //body: sum += A[i] * B[j];
        il.append(new ILOAD(id_I));
        il.append(new DALOAD());
        il.append(new DLOAD(id_B));
        il.append(new ILOAD(id_J));
        il.append(new DALOAD());
        il.append(new DMUL());
        il.append(new DLOAD(id_Sum));
        il.append(new DADD());
        il.append(new DSTORE(id_Sum));


        //3.Compare: k < r2 - for(int j = 0; j < c2; ++j)
        init_compare_for_loop(loopStart3,id_K,id_R2,1);

        //2.Compare: j < c2 - for(int j = 0; j < c2; ++j)
        init_compare_for_loop(loopStart2,id_J,id_C2,1);

        //1.Compare: i < r1 - for(int i = 0; i < r1; ++i)
        init_compare_for_loop(loopStart,id_I,id_R1,1);

        il.append(InstructionConst.RETURN);


        confirm_and_save("src/main/java/MatrixBuilder/generated/TestForLoop.class");
    }
}