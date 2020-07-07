package pl.edu.agh.transformations;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;
import pl.edu.agh.transformations.util.New;

import java.io.IOException;


public class MyBcModifier {

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
        CLASS_METHOD = classMethod;
        PATH_TO_INPUT_FILE = classPath + className + LaunchProperties.CLASS_SUFFIX;
        PATH_TO_OUTPUT_FILE = classPath + className + LaunchProperties.MODIFICATION_SUFFIX + LaunchProperties.CLASS_SUFFIX;
    }


    public void InitWrapper() throws IOException {

        jclass = new ClassParser(PATH_TO_INPUT_FILE).parse();
        cg = new ClassGen(jclass);
        methods = jclass.getMethods();
        CLASS_NAME = cg.getClassName();

    }


    public static int GetMethodIndex(Method[] methods, String methodName){

        int methodPositionId;

        for (methodPositionId = 0; methodPositionId < methods.length; methodPositionId++) {
            if(methods[methodPositionId].getName().equals(methodName)){ break;}
        }
        if(methodPositionId<methods.length){
            return methodPositionId;
        }else{
            System.err.println("Method: "+methodName+" not found in "+CLASS_NAME);
            return -1;
        }
    }


    public void WrapperTime() throws IOException {

        InitWrapper();
        int methodPositionId = (int) GetMethodIndex(methods,CLASS_METHOD);
        AddTimeWrapper(methods[methodPositionId]);
    }


    public void WrapperMatrix() throws IOException {
        InitWrapper();
        int methodPositionId = (int) GetMethodIndex(methods,CLASS_METHOD);
        AddNewWrapper(methods[methodPositionId]);
    }


//  One of the examples from the intro/ibmbmbcel directory
    private static void AddTimeWrapper(Method method) throws IOException {

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

        il.append(new RETURN());


/*        if(stripAttributes == true)
            mg.stripAttributes(true);*/

        mg.setMaxStack();
        mg.setMaxLocals();
        cg.addMethod(mg.getMethod());
        il.dispose();

        New.saveNewClassFile(cg);

    }


    private static void AddNewWrapper(Method method) throws IOException {

       // set up the construction tools
        InstructionFactory factory = new InstructionFactory(cg);
        InstructionList il = new InstructionList();
        ConstantPoolGen cp = cg.getConstantPool();
        String cname = cg.getClassName();
        MethodGen wrap_mg = new MethodGen(Const.ACC_STATIC | Const.ACC_PUBLIC,
            Type.VOID,  null,null, CLASS_METHOD+"_NEW", CLASS_NAME,
            il, cp);

        int id_A = New.CreateArrayField("A",wrap_mg,il,cp,Type.INT,2, new int[]{2,2});
        int id_B = New.CreateArrayField("B",wrap_mg,il,cp,Type.INT,2, new int[]{2,2});

        int jcm_ID = New.CreateObjectClass(
                "jcm", "utils.JCudaMatrix",
                il,factory,wrap_mg, new int[]{id_A,id_B});

        il.append(new ALOAD(jcm_ID));

        lg = wrap_mg.addLocalVariable("C", new ArrayType(Type.FLOAT, 1), null, null);
        int id = lg.getIndex();

        il.append(factory.createInvoke(
                "utils.JCudaMatrix", "multiply",
                new ArrayType(Type.FLOAT,1), new Type[]{},Const.INVOKEVIRTUAL));

        il.append(new ASTORE(id));

        New.PrintArray(il,wrap_mg,factory,id,false);

        il.append(new RETURN());

        /*        if(stripAttributes == true)
            mg.stripAttributes(true);*/

        mg.setMaxStack();
        mg.setMaxLocals();
        cg.addMethod(mg.getMethod());
        il.dispose();

        New.saveNewClassFile(cg);
    }


    public void CreateJCudaMatrix2D() throws IOException {
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
                "jcm", "utils.JCudaMatrix",
                il,factory,mg, new int[]{id_A,id_B});

        il.append(new ALOAD(jcm_ID));

        lg = mg.addLocalVariable("C", new ArrayType(Type.FLOAT, 1), null, null);
        int id = lg.getIndex();

        il.append(factory.createInvoke(
                "utils.JCudaMatrix", "multiply",
                new ArrayType(Type.FLOAT,1), new Type[]{},Const.INVOKEVIRTUAL));

        il.append(new ASTORE(id));

        New.PrintArray(il,mg,factory,id,false);

        il.append(new RETURN());

        /*        if(stripAttributes == true)
            mg.stripAttributes(true);*/

        mg.setMaxStack();
        mg.setMaxLocals();
        cg.addMethod(mg.getMethod());
        il.dispose();

        New.saveNewClassFile(cg);

    }

    public void WrapperTest() throws IOException {
        InitWrapper();
        int methodPositionId = (int) GetMethodIndex(methods,CLASS_METHOD);
        AddNewWrapper(methods[methodPositionId]);
    }

}