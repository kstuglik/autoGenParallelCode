package pl.edu.agh.transformations;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;
import pl.edu.agh.transformations.util.New;

import java.io.FileOutputStream;
import java.io.IOException;

public class MyBcModifier {

    private static final String MODIFICATION_SUFFIX = "_mod";
    private static final String CLASS_SUFFIX = ".class";

    private static String CLASS_PATH;
    private static String CLASS_NAME;
    private static String CLASS_METHOD;

    private static String PATH_TO_INPUT_FILE;
    private static String PATH_TO_OUTPUT_FILE;

//    components of bytecode modifier

    private static final ObjectType i_stream = new ObjectType("java.io.InputStream");
    private static final ObjectType p_stream = new ObjectType("java.io.PrintStream");

    protected static JavaClass jclass;
    protected static Method[] methods;
    protected static Field[] fields;

    protected static ClassGen cg;
    protected static MethodGen mg;
    protected static ConstantPoolGen cp;
    protected static InstructionList il;
    protected static InstructionFactory factory;
    protected static LocalVariableGen lg;


    public void SetParameters(String classPath, String className, String classMethod){
        CLASS_PATH = classPath;
        CLASS_NAME = className;
        CLASS_METHOD = classMethod;

        PATH_TO_INPUT_FILE = classPath + className + CLASS_SUFFIX;
        PATH_TO_OUTPUT_FILE = classPath + className + MODIFICATION_SUFFIX + CLASS_SUFFIX;
    }


    public void WrapperExample(int choice) throws IOException {

        jclass = new ClassParser(PATH_TO_INPUT_FILE).parse();
        cg = new ClassGen(jclass);

        methods = jclass.getMethods();

        int methodPositionId;
        for (methodPositionId = 0; methodPositionId < methods.length; methodPositionId++) {
            if(methods[methodPositionId].getName().equals(CLASS_METHOD)){ break;}
        }

        if(methodPositionId<methods.length){
            if(choice == 0)
                AddTimeWrapper(methods[methodPositionId]);
            if(choice == 1)
                AddNewWrapper(methods[methodPositionId]);
        }else{
            System.err.println("Method: "+CLASS_METHOD+" not found in "+CLASS_NAME);
        }

    }


//  One of the examples from the intro/ibmbmbcel directory
    private static void AddTimeWrapper(Method method) {

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
        for (Type value : types) {
            slot += value.getSize();
        }

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
        for (Type type : types) {
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

        SaveModifiedClass();
    }


    private static void AddNewWrapper(Method method) {

       // set up the construction tools
        InstructionFactory factory = new InstructionFactory(cg);
        InstructionList il = new InstructionList();
        ConstantPoolGen cp = cg.getConstantPool();
        String cname = cg.getClassName();
        MethodGen wrap_mg = new MethodGen(Const.ACC_STATIC | Const.ACC_PUBLIC,
            Type.VOID,  null,null, "CLASS_METHOD", CLASS_NAME,
            il, cp);

        int id_A = New.CreateArrayField("A",wrap_mg,il,cp,Type.INT,2, new int[]{2,2});
        int id_B = New.CreateArrayField("B",wrap_mg,il,cp,Type.INT,2, new int[]{2,2});

        int jcm_ID = New.CreateObjectClass(
                "jcm", lg, il,factory,wrap_mg,
                "utils.JCudaMatrix", new int[]{id_A,id_B});

        il.append(new ALOAD(jcm_ID));

        lg = wrap_mg.addLocalVariable("C", new ArrayType(Type.FLOAT, 1), null, null);
        int id = lg.getIndex();

        il.append(factory.createInvoke(
                "utils.JCudaMatrix", "multiply",
                new ArrayType(Type.FLOAT,1), new Type[]{},Const.INVOKEVIRTUAL));

        il.append(new ASTORE(id));

        New.PrintArray(il,wrap_mg,factory,id,false);

        il.append(new RETURN());
        // finalize the constructed method

        wrap_mg.setMaxStack();
        wrap_mg.setMaxLocals();
        cg.addMethod(wrap_mg.getMethod());
        il.dispose();

        SaveModifiedClass();
    }


    public void CreateJCudaMatrix2D(){
        cg = new ClassGen(CLASS_NAME, "java.lang.Object","<generated>",
            Const.ACC_PUBLIC |Const.ACC_SUPER,null);
        factory = new InstructionFactory(cg);
        cp = cg.getConstantPool();
        il = new InstructionList();
        mg = new MethodGen(Const.ACC_STATIC | Const.ACC_PUBLIC,
            Type.VOID,  new Type[]{ new ArrayType(Type.STRING, 1)},
            new String[]{"argv"}, CLASS_METHOD, CLASS_NAME,
            il, cp);


        //    created array with the dimensions indicated, filled with values
        //     ex: new int[][]{1,2} -> {{1}, {1,2}}

        int id_A = New.CreateArrayField("A",mg,il,cp,Type.INT,2, new int[]{2,2});
        int id_B = New.CreateArrayField("B",mg,il,cp,Type.INT,2, new int[]{2,2});

        int jcm_ID = New.CreateObjectClass(
                "jcm", lg, il,factory,mg,
                "utils.JCudaMatrix", new int[]{id_A,id_B});

        il.append(new ALOAD(jcm_ID));

        lg = mg.addLocalVariable("C", new ArrayType(Type.FLOAT, 1), null, null);
        int id = lg.getIndex();

        il.append(factory.createInvoke(
                "utils.JCudaMatrix", "multiply",
                new ArrayType(Type.FLOAT,1), new Type[]{},Const.INVOKEVIRTUAL));

        il.append(new ASTORE(id));

        New.PrintArray(il,mg,factory,id,false);

//        the task to be optimized, a part of code to put the content into *.class file
//        looks almost always the same

        il.append(new RETURN());
        mg.setMaxStack();
        mg.setMaxLocals();
        cg.addMethod(mg.getMethod());
        il.dispose();

        SaveModifiedClass();

    }


    private static void SaveModifiedClass() {

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