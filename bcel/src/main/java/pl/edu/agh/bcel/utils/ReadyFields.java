package pl.edu.agh.bcel.utils;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;
import pl.edu.agh.bcel.LaunchProperties;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

public class ReadyFields {

    public static void addStaticFields(ClassGen cg) {
        String className = cg.getClassName();
        ConstantPoolGen cp = cg.getConstantPool();
        InstructionList il = new InstructionList();
        InstructionFactory factory = new InstructionFactory(cg, cp);
        int FLAGS = Const.ACC_PUBLIC | Const.ACC_STATIC;// | Const.ACC_FINAL;


        FieldGen threadCount = new FieldGen(FLAGS, Type.INT, LaunchProperties.NUMBER_OF_THREADS_NAME, cp);
        cg.addField(threadCount.getField());
        il.append(factory.createInvoke("java.lang.Runtime", "getRuntime", Type.getType(Runtime.class), Type.NO_ARGS, Const.INVOKESTATIC));
        il.append(factory.createInvoke("java.lang.Runtime", "availableProcessors", Type.INT, Type.NO_ARGS, Const.INVOKEVIRTUAL));
        il.append(factory.createPutStatic(className, LaunchProperties.NUMBER_OF_THREADS_NAME, Type.INT));


        FieldGen thershold = new FieldGen(FLAGS, Type.INT,LaunchProperties.THRESHOLD_NAME, cp);
        cg.addField(thershold.getField());
        il.append(new PUSH(cp,1024));il.append(new PUSH(cp,32));il.append(new IMUL());
        il.append(factory.createPutStatic(cg.getClassName(),LaunchProperties.THRESHOLD_NAME,Type.INT));


        FieldGen service = new FieldGen(Const.ACC_PUBLIC | Const.ACC_STATIC, Type.getType(ExecutorService.class), LaunchProperties.EXECUTOR_SERVICE_NAME, cp);
        cg.addField(service.getField());
        il.append(factory.createGetStatic(cg.getClassName(), LaunchProperties.NUMBER_OF_THREADS_NAME, Type.INT));
        il.append(factory.createInvoke("java.util.concurrent.Executors", "newFixedThreadPool", Type.getType(ExecutorService.class), new Type[]{Type.INT}, Const.INVOKESTATIC));
        il.append(factory.createPutStatic(cg.getClassName(), LaunchProperties.EXECUTOR_SERVICE_NAME, Type.getType(ExecutorService.class)));


        il.append(InstructionFactory.createReturn(Type.VOID));
        MethodGen mg = new MethodGen(Const.ACC_STATIC, Type.VOID, Type.NO_ARGS, new String[0], Const.STATIC_INITIALIZER_NAME, className, il, cp);
        mg.setMaxLocals();
        mg.setMaxStack();
        cg.replaceMethod(mg.getMethod(), mg.getMethod());
    }

    public static void addFieldStep(ClassGen cg, MethodGen mg) {
        InstructionList il = new InstructionList();
        InstructionFactory factory = new InstructionFactory(cg, mg.getConstantPool());

        int id = mg.addLocalVariable("step", Type.INT, null, null).getIndex();

        il.append(factory.createInvoke(mg.getClassName(), "setStep", Type.INT, Type.NO_ARGS, Const.INVOKESTATIC));
        il.append(InstructionFactory.createStore(Type.INT, id));

        InstructionList currentList = mg.getInstructionList();
        il.append(currentList);
        mg.setInstructionList(il);

        mg.setMaxStack();
        mg.setMaxLocals();
        cg.replaceMethod(mg.getMethod(), mg.getMethod());
    }

    public static void addFieldTaskPool(ClassGen cg, MethodGen mg) {
        ConstantPoolGen cp = cg.getConstantPool();
        InstructionFactory factory = new InstructionFactory(cg, cp);
        InstructionList il = new InstructionList();

        il.append(factory.createInvoke(
                cg.getClassName(), "init", new ObjectType("ArrayList<Callable<Integer>>"),
                Type.NO_ARGS, Const.INVOKESTATIC));

        il.append(InstructionFactory.createStore(Type.OBJECT, LaunchProperties.TASK_POOL_ID));
        il.append(InstructionFactory.createLoad(Type.OBJECT, LaunchProperties.TASK_POOL_ID));
        il.append(InstructionFactory.createStore(Type.OBJECT, LaunchProperties.TASK_POOL_ID + 1));


        InstructionList currentList = mg.getInstructionList();

        il.append(currentList);
        mg.setInstructionList(il);
        mg.setMaxStack();
        mg.setMaxLocals();
        cg.replaceMethod(mg.getMethod(), mg.getMethod());
    }

    public static void addFieldsForJcuda(ClassGen cg) {

        Optional<MethodGen> classInitMethod = MethodUtils.findMethodByName(cg, Const.STATIC_INITIALIZER_NAME);
        ConstantPoolGen cp = cg.getConstantPool();
        InstructionList il_new = new InstructionList();

        classInitMethod.ifPresent(init -> {
            il_new.append(init.getInstructionList());
            try {
                il_new.delete(il_new.getEnd());
            } catch (TargetLostException e) {
                e.printStackTrace();
            }
            TransformUtils.retargetStaticPuts(cg, il_new);
        });

        MethodGen mg = new MethodGen(
                Const.ACC_STATIC,
                Type.VOID,
                Type.NO_ARGS,
                new String[0],
                Const.STATIC_INITIALIZER_NAME,
                cg.getClassName(),
                il_new,
                cp
        );


        int methodPositionId = MethodUtils.GetMethodIndex(cg.getMethods(), LaunchProperties.CLASS_METHOD);
        Method transformMethod = cg.getMethods()[methodPositionId];

        InstructionFactory factory = new InstructionFactory(cg, mg.getConstantPool());

        ReadyFields.createFieldsForMatrixJCudaCase(cg, mg);
        initFieldsForJCuda(cg, il_new, factory, cp);

        il_new.append(new RETURN());
        TransformUtils.updateMethodParametersScope(mg, cp);
        mg.setMaxLocals();
        mg.setMaxStack();
        cg.replaceMethod(mg.getMethod(), mg.getMethod());
        il_new.dispose();
    }

    //    IMPORTANT: WE USE "STATIC" - CAN BE SET IN LAUNCHPROPERTIES - CALL-TASK
    public static void changeBodyInnerLoopForMatrixMultiply(MethodGen mg, InstructionList ilNEW, InstructionFactory factory) {
        int idRowNum = VariableUtils.getLVarIdByName("rowNum", mg);
        int idColNum = VariableUtils.getLVarIdByName("colNum", mg);

        LocalVariableGen finalColNum = mg.addLocalVariable(
                "finalColNum", Type.INT, null, null);
        int idFinalColNum = finalColNum.getIndex();
        ilNEW.append(new FLOAD(idColNum));
        ilNEW.append(new F2I());
        ilNEW.append(new ISTORE(idFinalColNum));

        LocalVariableGen finalRowNum = mg.addLocalVariable("finalRowNum",
                Type.INT, null, null);
        int idFinalRowNum = finalRowNum.getIndex();

        ilNEW.append(new FLOAD(idRowNum));
        ilNEW.append(new F2I());
        ilNEW.append(new ISTORE(idFinalRowNum));

        LocalVariable step = VariableUtils.getLVarByName("step", mg.getLocalVariableTable(mg.getConstantPool()));

        LocalVariableGen[] lvt = mg.getLocalVariables();
//        for (LocalVariableGen localVariableGen : lvt) System.out.println(localVariableGen.getName());

        ilNEW.append(InstructionFactory.createLoad(Type.OBJECT, LaunchProperties.TASK_POOL_ID));
        ilNEW.append(factory.createNew(new ObjectType("Callable<Integer>() {" +
                "public Integer call() {" +
                "return " + LaunchProperties.SUBTASK_METHOD_NAME +
                "(" + finalRowNum.getName() + "," + finalColNum.getName() + "," + step.getName() + ")" +
                ";}}"
        )));

        ilNEW.append(factory.createInvoke("java.util.List",
                "add", Type.BOOLEAN, new Type[]{Type.OBJECT}, Const.INVOKEINTERFACE));
        mg.setMaxLocals();
        mg.setMaxStack();
//        ilNEW.dispose();
    }

    public static void createFieldsForMatrixJCudaCase(ClassGen cg, MethodGen mg) {
        FieldGen field;
        ConstantPoolGen cp = cg.getConstantPool();

        field = new FieldGen(Const.ACC_STATIC, new ArrayType(Type.FLOAT, 1), "matrix_A", cp);
        cg.addField(field.getField());

        field = new FieldGen(Const.ACC_STATIC, new ArrayType(Type.FLOAT, 1), "matrix_B", cp);
        cg.addField(field.getField());

        field = new FieldGen(Const.ACC_STATIC, new ArrayType(Type.FLOAT, 1), "matrix_C", cp);
        cg.addField(field.getField());

        field = new FieldGen(Const.ACC_STATIC, Type.INT, "cols_A", cp);
        cg.addField(field.getField());

        field = new FieldGen(Const.ACC_STATIC, Type.INT, "rows_A", cp);
        cg.addField(field.getField());

        field = new FieldGen(Const.ACC_STATIC, Type.INT, "cols_B", cp);
        cg.addField(field.getField());

        field = new FieldGen(Const.ACC_STATIC, Type.INT, "rows_B", cp);
        cg.addField(field.getField());

        field = new FieldGen(Const.ACC_STATIC, new ObjectType("jcuda.Pointer"), "ptr_A", cp);
        cg.addField(field.getField());

        field = new FieldGen(Const.ACC_STATIC, new ObjectType("jcuda.Pointer"), "ptr_B", cp);
        cg.addField(field.getField());

        field = new FieldGen(Const.ACC_STATIC, new ObjectType("jcuda.Pointer"), "ptr_C", cp);
        cg.addField(field.getField());

        field = new FieldGen(Const.ACC_STATIC, Type.FLOAT, "alpha", cp);
        cg.addField(field.getField());

        field = new FieldGen(Const.ACC_STATIC, Type.FLOAT, "beta", cp);
        cg.addField(field.getField());

    }

    public static void initFieldExecutorService(ClassGen cg, MethodGen mg) {

        ConstantPoolGen cp = cg.getConstantPool();
        InstructionFactory factory = new InstructionFactory(cg, cp);
        InstructionList il_append = new InstructionList();

        il_append.append(factory.createGetStatic(
                cg.getClassName(),
                LaunchProperties.NUMBER_OF_THREADS_NAME,
                Type.INT));
        il_append.append(factory.createInvoke(
                "java.util.concurrent.Executors",
                "newFixedThreadPool",
                Type.getType(ExecutorService.class),
                new Type[]{Type.INT},
                Const.INVOKESTATIC));
        il_append.append(factory.createPutStatic(
                cg.getClassName(),
                LaunchProperties.EXECUTOR_SERVICE_NAME,
                Type.getType(ExecutorService.class)));

        InstructionList il = mg.getInstructionList();
        il_append.append(il);
        mg.setInstructionList(il_append);
        TransformUtils.updateMethodParametersScope(mg, cp);
        mg.setMaxStack();
        mg.setMaxLocals();
        cg.replaceMethod(mg.getMethod(), mg.getMethod());
    }

    protected static void initFieldsForJCuda(ClassGen cg, InstructionList il, InstructionFactory factory, ConstantPoolGen cp) {
        InstructionHandle ih_0 = il.append(factory.createNew("jcuda.Pointer"));
        il.append(InstructionConst.DUP);
        il.append(factory.createInvoke("jcuda.Pointer", "<init>", Type.VOID, Type.NO_ARGS, Const.INVOKESPECIAL));
        il.append(factory.createFieldAccess(cg.getClassName(), "ptr_A", new ObjectType("jcuda.Pointer"), Const.PUTSTATIC));
        il.append(factory.createNew("jcuda.Pointer"));
        il.append(InstructionConst.DUP);
        il.append(factory.createInvoke("jcuda.Pointer", "<init>", Type.VOID, Type.NO_ARGS, Const.INVOKESPECIAL));
        il.append(factory.createFieldAccess(cg.getClassName(), "ptr_B", new ObjectType("jcuda.Pointer"), Const.PUTSTATIC));
        il.append(factory.createNew("jcuda.Pointer"));
        il.append(InstructionConst.DUP);
        il.append(factory.createInvoke("jcuda.Pointer", "<init>", Type.VOID, Type.NO_ARGS, Const.INVOKESPECIAL));
        il.append(factory.createFieldAccess(cg.getClassName(), "ptr_C", new ObjectType("jcuda.Pointer"), Const.PUTSTATIC));
        InstructionHandle ih_30 = il.append(new PUSH(cp, 1.0f));
        il.append(factory.createFieldAccess(cg.getClassName(), "alpha", Type.FLOAT, Const.PUTSTATIC));
        il.append(new PUSH(cp, 0.0f));
        il.append(factory.createFieldAccess(cg.getClassName(), "beta", Type.FLOAT, Const.PUTSTATIC));
    }

//    public static void addThreadPoolExecutorService(ClassGen cg) {
//        String className = cg.getClassName();
//        ConstantPoolGen cp = cg.getConstantPool();
//        FieldGen service = new FieldGen(
//                Const.ACC_PUBLIC | Const.ACC_STATIC, Type.getType(ExecutorService.class),
//                LaunchProperties.EXECUTOR_SERVICE_NAME, cp);
//
//        cg.addField(service.getField());
//
//        InstructionList il = new InstructionList();
//        il.append(InstructionFactory.createReturn(Type.VOID));
//
//        MethodGen mg = new MethodGen(Const.ACC_STATIC,
//                Type.VOID, Type.NO_ARGS, new String[0],
//                Const.STATIC_INITIALIZER_NAME,
//                className, il, cp);
//
//        mg.setMaxLocals();
//        mg.setMaxStack();
//        cg.replaceMethod(mg.getMethod(), mg.getMethod());
//        il.dispose();
//    }

    public static void addFieldGeneric(ClassGen cg, MethodGen mg, String genericType) {
        ConstantPoolGen cp = cg.getConstantPool();
        InstructionFactory factory = new InstructionFactory(cg, cp);
        InstructionList il = new InstructionList();


        il.append(factory.createInvoke(
                cg.getClassName(), LaunchProperties.METHOD_INIT_ARRAYLIST, new ObjectType(
                        genericType),
                Type.NO_ARGS, Const.INVOKESTATIC));

        il.append(InstructionFactory.createStore(Type.OBJECT, LaunchProperties.PARTIAL_RESULT_ID));
        il.append(InstructionFactory.createLoad(Type.OBJECT, LaunchProperties.PARTIAL_RESULT_ID));
        il.append(InstructionFactory.createStore(Type.OBJECT, LaunchProperties.PARTIAL_RESULT_ID + 1));


        InstructionList currentList = mg.getInstructionList();

        il.append(currentList);
        mg.setInstructionList(il);
        mg.setMaxStack();
        mg.setMaxLocals();
        cg.replaceMethod(mg.getMethod(), mg.getMethod());

    }

    public static void addFieldToClass(ClassGen cg, ConstantPoolGen cp, Type type, String fieldName) {
        FieldGen newField = new FieldGen(Const.ACC_PUBLIC | Const.ACC_STATIC, type, fieldName, cp);
        cg.addField(newField.getField());
    }

}