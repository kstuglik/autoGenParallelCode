/*
SOURCE: https://www.ibm.com/developerworks/library/j-dyn0414/

How does it work?
1: compile ibmbcel.StringBuilder.java with parameters, e.g. 100 200
2: compile BCELTiming, it make modification *.class file  (added time measurement)
3: copy the code from bcel/target/classes/ibmbcel/ibmbcel.StringBuilder.class into a new *.java file
*/

package ibmbcel;

import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.bcel.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;

public class BCELTiming {
    private static void addWrapper(ClassGen cgen, Method method) {

        // set up the construction tools
        InstructionFactory factory = new InstructionFactory(cgen);
        InstructionList il = new InstructionList();
        ConstantPoolGen cp = cgen.getConstantPool();
        String cname = cgen.getClassName();
        MethodGen wrap_mg = new MethodGen(method, cname, cp);

        wrap_mg.setInstructionList(il);

        // rename a copy of the original method
        MethodGen mg = new MethodGen(method, cname, cp);
        cgen.removeMethod(method);
        String iname = mg.getName() + "$impl";
        mg.setName(iname);
        cgen.addMethod(mg.getMethod());
        Type result = mg.getReturnType();

        // compute the size of the calling parameters
        Type[] types = mg.getArgumentTypes();
        int slot = mg.isStatic() ? 0 : 1;
        for (int i = 0; i < types.length; i++) {slot += types[i].getSize();}

        // save time prior to invocation
        il.append(factory.createInvoke(
                "java.lang.System","currentTimeMillis",
                Type.LONG, Type.NO_ARGS,Const.INVOKESTATIC));
        il.append(InstructionFactory.createStore(Type.LONG, slot));

        // call the wrapped method
        int offset = 0;
        short invoke = Const.INVOKESTATIC;
        if (!mg.isStatic()) {
            il.append(InstructionFactory.createLoad(Type.OBJECT, 0));
            offset = 1;
            invoke = Const.INVOKEVIRTUAL;
        }
        for (int i = 0; i < types.length; i++) {
            Type type = types[i];
            il.append(InstructionFactory.createLoad(type, offset));
            offset += type.getSize();
        }
        il.append(factory.createInvoke(cname,iname, result, types, invoke));

        // store result for return later
        if (result != Type.VOID) {il.append(InstructionFactory.createStore(result, slot+2));}

        // print time required for method call
        il.append(factory.createFieldAccess(
                "java.lang.System","out",
                new ObjectType("java.io.PrintStream"),Const.GETSTATIC));
        il.append(InstructionConst.DUP);
        il.append(InstructionConst.DUP);
        String text = "Call to method " + mg.getName() + " took ";
        il.append(new PUSH(cp, text));
        il.append(factory.createInvoke(
                "java.io.PrintStream","print",
                Type.VOID, new Type[] { Type.STRING },Const.INVOKEVIRTUAL));
        il.append(factory.createInvoke(
                "java.lang.System","currentTimeMillis",
                Type.LONG, Type.NO_ARGS,Const.INVOKESTATIC));
        il.append(InstructionFactory.createLoad(Type.LONG, slot));
        il.append(InstructionConst.LSUB);
        il.append(factory.createInvoke(
                "java.io.PrintStream","print",
                Type.VOID, new Type[] { Type.LONG },Const.INVOKEVIRTUAL));
        il.append(new PUSH(cp, " ms."));
        il.append(factory.createInvoke(
                "java.io.PrintStream","println",
                Type.VOID, new Type[] { Type.STRING },Const.INVOKEVIRTUAL));

        // return result from wrapped method call
        if (result != Type.VOID) {il.append(InstructionFactory.createLoad(result, slot+2));}
        il.append(InstructionFactory.createReturn(result));

        // finalize the constructed method
        wrap_mg.stripAttributes(true);
        wrap_mg.setMaxStack();
        wrap_mg.setMaxLocals();
        cgen.addMethod(wrap_mg.getMethod());
        il.dispose();
    }

    public static void main(String[] argv) {

        String path_to_file = "/home/lenovo/Documents/MGR/bcel/target/classes/ibmbcel/";
        String class_file = "StringBuilder.class";
        String method_file = "buildString";

        try {
            JavaClass jclass;
            jclass = new ClassParser(path_to_file+class_file).parse();
            ClassGen cg = new ClassGen(jclass);

            Method[] methods = jclass.getMethods();
            int index;
            for (index = 0; index < methods.length; index++) {
                if (methods[index].getName().equals(method_file)) {break;}
            }
            if (index < methods.length) {
                addWrapper(cg, methods[index]);
                FileOutputStream fos = new FileOutputStream(path_to_file+class_file);
                cg.getJavaClass().dump(fos);
                fos.close();
            } else {System.err.println("Method " + method_file + " not found in " + class_file);}
        } catch (IOException ex) {ex.printStackTrace(System.err);}
    }
}
