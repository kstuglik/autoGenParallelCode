package mbuilder.TestAutoParallel;

import org.apache.bcel.Const;
import org.apache.bcel.generic.*;
import pl.edu.agh.transformations.util.New;

import java.io.IOException;


public class newTransformation {

    public static void main(final String[] argv) {
        final ClassGen cg = new ClassGen("newTransformation", "java.lang.Object",
                "<MatrixBuilder.MatrixBuilder.generated>",
                Const.ACC_PUBLIC |Const.ACC_SUPER,
                null);

        final ConstantPoolGen cp = cg.getConstantPool(); // cg creates constant pool
        final InstructionList il = new InstructionList();

        final MethodGen mg = new MethodGen(Const.ACC_STATIC | Const.ACC_PUBLIC,
                Type.VOID,  new Type[]{ new ArrayType(Type.STRING, 1)},
                new String[]{"argv"}, "main", "HelloWorld",
                il, cp);

        final InstructionFactory factory = new InstructionFactory(cg);

        final ObjectType i_stream = new ObjectType("java.io.InputStream");
        final ObjectType p_stream = new ObjectType("java.io.PrintStream");

//      UTWORZENIE TABLC 2D, zainicjalizowane na wskazany rozmiar np. new int[]{1,2} -> {1}, {1,2}
        int id_A = New.create_array_2D("A",mg,il,cp,Type.INT,2, new int[]{2,2});
        int id_B = New.create_array_2D("B",mg,il,cp,Type.INT,2, new int[]{2,2});
        int[] paramsID = {id_A,id_B};
        New.createObjectClass("input",il,factory,mg, "JCudaMatrix", paramsID);

        int name_id = New.createFieldName("hurra",il,mg);

        il.append(InstructionConst.RETURN);

        mg.setMaxStack();
        mg.setMaxLocals();
        cg.addMethod(mg.getMethod());

        il.dispose(); // Reuse instruction handles

        // Add public <init> method, i.e. empty constructor
        cg.addEmptyConstructor(Const.ACC_PUBLIC);


        // Get JavaClass object and dump it to file.
        try {
            cg.getJavaClass().dump("src/main/java/mbuilder/classFiles/newTransformation.class");
        } catch (final IOException e) {
            System.err.println(e);
        }
    }
}