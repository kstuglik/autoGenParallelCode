package mbuilder.TestAutoParallel;

import org.apache.bcel.Const;
import org.apache.bcel.generic.*;
import pl.edu.agh.transformations.util.New;

import java.io.IOException;


public class newTransformation {

    public static void main(final String[] argv) {
        final ClassGen cg = new ClassGen("HelloWorld", "java.lang.Object",
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
        int id_A = New.create_array_2D("A",mg,il,cp,Type.INT,2, new int[]{1,2});
        int id_B = New.create_array_2D("B",mg,il,cp,Type.INT,2, new int[]{2,1});
        int[] paramsID = {id_A,id_B};
        New.createObjectClass("input",il,factory,mg, "JCudaMatrix", paramsID);

        int name_id = New.createFieldName("hurra",il,mg);

//        // try { ...
//        final InstructionHandle try_start =
//                il.append(factory.createFieldAccess("java.lang.System", "out", p_stream, Const.GETSTATIC));
//
//        il.append(new PUSH(cp, "Please enter your name> "));
//        il.append(factory.createInvoke("java.io.PrintStream", "print", Type.VOID,
//                new Type[]{Type.STRING}, Const.INVOKEVIRTUAL));
//        il.append(new ALOAD(in));
//        il.append(factory.createInvoke("java.io.BufferedReader", "readLine",
//                Type.STRING, Type.NO_ARGS, Const.INVOKEVIRTUAL));
//        il.append(new ASTORE(name_id));
//
//        // Upon normal execution we jump behind exception handler, the target address is not known yet.
//        final GOTO g = new GOTO(null);
//        final InstructionHandle try_end = il.append(g);
//
//        /* } catch() { ... }
//         * Add exception handler: print exception and return from method
//         */
//        final InstructionHandle handler =
//                il.append(factory.createFieldAccess("java.lang.System", "out", p_stream, Const.GETSTATIC));
//        // Little trick in order not to save exception object temporarily
//        il.append(InstructionConst.SWAP);
//
//        il.append(factory.createInvoke("java.io.PrintStream", "println", Type.VOID, new Type[]{Type.OBJECT}, Const.INVOKEVIRTUAL));
//        il.append(InstructionConst.RETURN);
//        mg.addExceptionHandler(try_start, try_end, handler, new ObjectType("java.io.IOException"));

        // Normal code continues, now we can set the branch target of the GOTO that jumps over the handler code.
//        final InstructionHandle ih =
//                il.append(factory.createFieldAccess("java.lang.System", "out", p_stream, Const.GETSTATIC));
////        g.setTarget(ih);
//
//        // String concatenation compiles to StringBuffer operations.
//        il.append(factory.createNew(Type.STRINGBUFFER));
//        il.append(InstructionConst.DUP);
//        il.append(new PUSH(cp, "Hello, "));
//        il.append(factory.createInvoke("java.lang.StringBuffer", "<init>",
//                Type.VOID, new Type[]{Type.STRING},
//                Const.INVOKESPECIAL));
//        il.append(new ALOAD(name_id));
//
//        // Concatenate strings using a StringBuffer and print them.
//        il.append(factory.createInvoke("java.lang.StringBuffer", "append",
//                Type.STRINGBUFFER, new Type[]{Type.STRING},
//                Const.INVOKEVIRTUAL));
//        il.append(factory.createInvoke("java.lang.StringBuffer", "toString",
//                Type.STRING, Type.NO_ARGS,
//                Const.INVOKEVIRTUAL));
//
//        il.append(factory.createInvoke("java.io.PrintStream", "println",
//                Type.VOID, new Type[]{Type.STRING},
//                Const.INVOKEVIRTUAL));

        il.append(InstructionConst.RETURN);

        mg.setMaxStack();
        mg.setMaxLocals();
        cg.addMethod(mg.getMethod());

        il.dispose(); // Reuse instruction handles

        // Add public <init> method, i.e. empty constructor
        cg.addEmptyConstructor(Const.ACC_PUBLIC);


        // Get JavaClass object and dump it to file.
        try {
            cg.getJavaClass().dump("src/main/java/mbuilder/TestAutoParallel/HelloWorld.class");
        } catch (final IOException e) {
            System.err.println(e);
        }
    }
}