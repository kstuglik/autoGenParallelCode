package pl.edu.agh.transformations;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;

import java.io.FileOutputStream;
import java.io.IOException;

public class MyBcModifier {

    public static final String MODIFICATION_SUFFIX = "_mod";
    private static final String CLASS_SUFFIX = ".class";
    private static final String JAVA_SUFFIX = ".java";


    public void modifyBytecode(String classPath, String className, String methodName) throws IOException, TargetLostException {

        JavaClass jclass = new ClassParser(classPath + className + CLASS_SUFFIX).parse();
        ClassGen cg = new ClassGen(jclass);

        Method[] methods = jclass.getMethods();
//        Field[] field = jclass.getFields();

        int methodPositionId;
        for (methodPositionId = 0; methodPositionId < methods.length; methodPositionId++) {
            if(methods[methodPositionId].getName().equals(methodName)){ break;}
        }

        if(methodPositionId<methods.length){
            addTimeWrapper(cg,methods[methodPositionId]);
            saveModifiedClass(classPath + className + MODIFICATION_SUFFIX + CLASS_SUFFIX, cg);
        }else{
            System.err.println("Method: "+methodName+" not found in "+className);
        }

    }


    private static void addTimeWrapper(ClassGen cg, Method method) {

        // set up the construction tools
        InstructionFactory factory = new InstructionFactory(cg);
        InstructionList il = new InstructionList();
        ConstantPoolGen cp = cg.getConstantPool();
        String cname = cg.getClassName();
        MethodGen wrap_mg = new MethodGen(method, cname, cp);

        wrap_mg.setInstructionList(il);

        // rename a copy of the original method
        MethodGen mg = new MethodGen(method, cname, cp);
        cg.removeMethod(method);
        String iname = mg.getName() + "$impl";
        mg.setName(iname);
        cg.addMethod(mg.getMethod());
        Type result = mg.getReturnType();

        // compute the size of the calling parameters
        Type[] types = mg.getArgumentTypes();
        int slot = mg.isStatic() ? 0 : 1;
        for (int i = 0; i < types.length; i++) {slot += types[i].getSize();}

        // save time prior to invocation
        il.append(factory.createInvoke(
                "java.lang.System","currentTimeMillis",
                Type.LONG, Type.NO_ARGS,
                Const.INVOKESTATIC));

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
                Type.VOID, new Type[] { Type.STRING },
                Const.INVOKEVIRTUAL));

        il.append(factory.createInvoke(
                "java.lang.System","currentTimeMillis",
                Type.LONG, Type.NO_ARGS,
                Const.INVOKESTATIC));

        il.append(InstructionFactory.createLoad(Type.LONG, slot));

        il.append(InstructionConst.LSUB);

        il.append(factory.createInvoke(
                "java.io.PrintStream","print",
                Type.VOID, new Type[] { Type.LONG },
                Const.INVOKEVIRTUAL));

        il.append(new PUSH(cp, " ms."));

        il.append(factory.createInvoke(
                "java.io.PrintStream","println",
                Type.VOID, new Type[] { Type.STRING },
                Const.INVOKEVIRTUAL));

        // return result from wrapped method call
        if (result != Type.VOID) {il.append(InstructionFactory.createLoad(result, slot+2));}
        il.append(InstructionFactory.createReturn(result));

        // finalize the constructed method
        wrap_mg.stripAttributes(true);
        wrap_mg.setMaxStack();
        wrap_mg.setMaxLocals();
        cg.addMethod(wrap_mg.getMethod());
        il.dispose();
    }


    private void saveModifiedClass(String pathToModifiedFile, ClassGen cg) {
       try{
           FileOutputStream fos = new FileOutputStream(pathToModifiedFile);
           cg.getJavaClass().dump(fos);
           fos.close();
        } catch (IOException e) {
            throw new RuntimeException("Error during modified class save.", e);
        }
        System.out.println("SAVED!");
    }

}
