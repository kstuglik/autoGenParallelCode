package pl.edu.agh.utils;

import jcuda.Sizeof;
import org.apache.bcel.Const;
import org.apache.bcel.classfile.ConstantFieldref;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;
import pl.edu.agh.transformations.LaunchProperties;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;


public class TransformUtils {
// TODO: MAKE ORDER METHODS IN CLASS

    private static void addBlockCatch(InstructionHandle TRY_START, InstructionList il, InstructionFactory factory, MethodGen mg) {
        BranchInstruction GOTO = InstructionFactory.createBranchInstruction(Const.GOTO, null);
        InstructionHandle TRY_END = il.append(GOTO);

        InstructionHandle CATCH_START = il.append(InstructionFactory.createStore(Type.OBJECT, 0));
        il.append(factory.createFieldAccess("java.lang.System", "err",
                new ObjectType("java.io.PrintStream"), Const.GETSTATIC));
        il.append(InstructionFactory.createLoad(Type.OBJECT, 0));
        il.append(factory.createInvoke("java.lang.Exception", "getMessage",
                Type.STRING, Type.NO_ARGS, Const.INVOKEVIRTUAL));
        il.append(factory.createInvoke("java.io.Printstream", "println",
                Type.VOID, new Type[]{Type.STRING}, Const.INVOKEVIRTUAL));

        InstructionHandle CATCH_END = il.append(InstructionFactory.createReturn(Type.VOID));
        GOTO.setTarget(CATCH_END);
        mg.addExceptionHandler(TRY_START, TRY_END, CATCH_START, new ObjectType("java.lang.Exception"));
    }

//  addClassFields_old - add thread fields in class

    private static void addBlockTry(InstructionHandle[] handles, InstructionList il) {
        InstructionHandle _handle;
        Instruction _i_current;

        for (int i = 1, handlesLength = handles.length; i < handlesLength; i++) {
            _handle = handles[i];
            _i_current = _handle.getInstruction();

//            System.out.println("opcode: " + _i_current.getOpcode() + "\tname: " + _i_current.getName());
//            if (_i_current.getOpcode() == Const.RETURN)
//                System.out.println(_i_current.getOpcode() + " appears in " + _mg.getName());
//            else
            il.append(_i_current);
        }
    }

    public static void addCallJCudaMultiply(ClassGen cg, MethodGen mg) throws Exception {

        ConstantPoolGen cp = cg.getConstantPool();
        InstructionList il = new InstructionList();
        InstructionList il_old = mg.getInstructionList();

//        System.out.println(il_old.getEnd().getPosition());

        InstructionFactory factory = new InstructionFactory(cg, mg.getConstantPool());

        //get instruction from method for append to end

        Optional<InstructionHandle> i = Arrays.stream(il_old.getInstructionHandles())
                .filter(handle -> "return".equals(handle.getInstruction().getName()))
                .findFirst();

        int GOTO_ID = i.get().getPosition() - 1;

        InstructionHandle[] handles = LoopUtils.getIhsBetweenFromTo(
                mg.getInstructionList().getInstructionHandles(), 0, GOTO_ID);

        InstructionHandle instr;
        for (InstructionHandle handle : handles) {
            instr = handle;
//            System.out.println(instr.getInstruction().copy());
            if (instr instanceof BranchHandle) {
                BranchHandle branch = (BranchHandle) instr;
                il.append((BranchInstruction) branch.getInstruction().copy());
            } else il.append(instr.getInstruction().copy());
        }

        int id_A, id_B;

        id_A = LocalVariableUtils.getLVarByName(LaunchProperties.ARRAY_1, mg.getLocalVariableTable(cp)).getIndex();
        id_B = LocalVariableUtils.getLVarByName(LaunchProperties.ARRAY_2, mg.getLocalVariableTable(cp)).getIndex();

//        ************** verify the type of array: 1D or 2D or more
//        YOU MUST KNOW THAT INFO ABOUT COLS AND ROWS MUST BE SET IN LAUNCHPROPERTIES

        String signatureMyArray = LocalVariableUtils.getSignatureForLocalVariableByName(LaunchProperties.ARRAY_1, mg.getLocalVariableTable(cp));
        long dimArray = signatureMyArray.chars().filter(ch -> ch == '[').count();

        InstructionHandle ih_86 = il.append(InstructionFactory.createLoad(Type.OBJECT, id_A));
        il.append(InstructionFactory.createLoad(Type.OBJECT, id_B));


        if (dimArray == 1) {
            il.append(factory.createInvoke(cg.getClassName(), "prepareFieldsFromArrays1D", Type.VOID, new Type[]{new ArrayType(Type.INT, 1), new ArrayType(Type.INT, 1)}, Const.INVOKESTATIC));
            addMethodPrepareFieldsFromArrays1D(cg);
        } else if (dimArray == 2) {
            il.append(factory.createInvoke(cg.getClassName(), "prepareFieldsFromArrays2D", Type.VOID, new Type[]{new ArrayType(Type.INT, 2), new ArrayType(Type.INT, 2)}, Const.INVOKESTATIC));
            addMethodPrepareFieldsFromArrays2D(cg);
            addFlattenMethod(cg);
        } else {
            throw new IllegalArgumentException("ILLEGAL DIMENSION OF THE ARRAY WAS DETECTED!");
        }


        InstructionHandle ih_91 = il.append(factory.createInvoke(cg.getClassName(), "multiply", Type.VOID, Type.NO_ARGS, Const.INVOKESTATIC));

        InstructionHandle ih_94 = il.append(factory.createFieldAccess("java.lang.System", "out", new ObjectType("java.io.PrintStream"), Const.GETSTATIC));
        il.append(factory.createFieldAccess(cg.getClassName(), "matrix_C", new ArrayType(Type.FLOAT, 1), Const.GETSTATIC));
        il.append(factory.createInvoke("java.util.Arrays", "toString", Type.STRING, new Type[]{new ArrayType(Type.FLOAT, 1)}, Const.INVOKESTATIC));
        il.append(factory.createInvoke("java.io.PrintStream", "println", Type.VOID, new Type[]{Type.STRING}, Const.INVOKEVIRTUAL));

//        mg.setClassName(cg.getClassName());
        mg.setInstructionList(il);
        mg.setMaxStack();
        mg.setMaxLocals();
        cg.replaceMethod(mg.getMethod(), mg.getMethod());
        il.dispose();
    }

    public static void addClassFields(ClassGen cg, ConstantPoolGen cp, Type type, String fieldName) {
        FieldGen newField = new FieldGen(Const.ACC_PUBLIC | Const.ACC_STATIC, type, fieldName, cp);
        cg.addField(newField.getField());
    }

    public static void addClassFields_old(ClassGen cg, ConstantPoolGen cp) {
        FieldGen threadCount = new FieldGen(
                Const.ACC_PUBLIC | Const.ACC_STATIC | Const.ACC_FINAL,
                Type.INT,
                LaunchProperties.NUMBER_OF_THREADS_NAME,
                cp);
        FieldGen service = new FieldGen(
                Const.ACC_PUBLIC | Const.ACC_STATIC,
                Type.getType(ExecutorService.class),
                LaunchProperties.EXECUTOR_SERVICE_NAME,
                cp);
        cg.addField(threadCount.getField());
        cg.addField(service.getField());
    }

    private static void addFieldNumThreads(InstructionList instructionList, InstructionFactory instructionFactory, String className) {
        instructionList.append(instructionFactory.createInvoke("java.lang.Runtime",
                "getRuntime",
                Type.getType(Runtime.class),
                Type.NO_ARGS,
                Const.INVOKESTATIC));
        instructionList.append(instructionFactory.createInvoke("java.lang.Runtime",
                "availableProcessors",
                Type.INT,
                Type.NO_ARGS,
                Const.INVOKEVIRTUAL));
        instructionList.append(instructionFactory.createPutStatic(className,
                LaunchProperties.NUMBER_OF_THREADS_NAME,
                Type.INT));
//        instructionList.append(instructionFactory.createGetStatic(className,
//                                                                  Constants.NUMBER_OF_THREADS_CONSTANT_NAME,
//                                                                  Type.INT));
//        instructionList.append(instructionFactory.createInvoke("java.util.concurrent.Executors",
//                                                               "newFixedThreadPool",
//                                                               Type.getType(ExecutorService.class),
//                                                               new Type[]{Type.INT},
//                                                               Const.INVOKESTATIC));
//        instructionList.append(instructionFactory.createPutStatic(className,
//                                                                  Constants.EXECUTOR_SERVICE_CONSTANT_NAME,
//                                                                  Type.getType(ExecutorService.class)));
        instructionList.append(InstructionFactory.createReturn(Type.VOID));
    }

    public static void addFieldTaskPool(ClassGen cg, MethodGen mg) {
        ConstantPoolGen cp = cg.getConstantPool();
        InstructionFactory factory = new InstructionFactory(cg, cp);
        InstructionList il = new InstructionList();


        il.append(factory.createInvoke(
                cg.getClassName(), "init",
                new ObjectType("ArrayList<Callable<Integer>>"),
                Type.NO_ARGS, Const.INVOKESTATIC));

        il.append(InstructionFactory.createStore(Type.OBJECT, LaunchProperties.TASK_POOL_ID));
        il.append(InstructionFactory.createLoad(Type.OBJECT, LaunchProperties.TASK_POOL_ID));
        il.append(InstructionFactory.createStore(Type.OBJECT, LaunchProperties.TASK_POOL_ID + 1));

//        NOT GREAT SOLUTION, because appear: implement..
//        cg.addInterface("java.util.List");

        InstructionList currentList = mg.getInstructionList();
        il.append(currentList);
        mg.setInstructionList(il);

        updateMethodParametersScope(mg, cp);
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
            retargetStaticPuts(cg, il_new);
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

        createFields(cg, mg);
        initFields(cg, il_new, factory, cp);

        il_new.append(new RETURN());
        updateMethodParametersScope(mg, cp);
        mg.setMaxLocals();
        mg.setMaxStack();
        cg.replaceMethod(mg.getMethod(), mg.getMethod());
        il_new.dispose();
    }

    public static void addFlattenMethod(ClassGen cg) {
        InstructionList il = new InstructionList();
        ConstantPoolGen cp = cg.getConstantPool();

        MethodGen mg = new MethodGen(Const.ACC_PUBLIC | Const.ACC_STATIC, new ArrayType(Type.FLOAT, 1), new Type[]{new ArrayType(Type.INT, 2)}, new String[]{"arg0"}, "flattenIArray2Dto1D", cg.getClassName(), il, cp);
        InstructionFactory factory = new InstructionFactory(cg, mg.getConstantPool());


        InstructionHandle ih_0 = il.append(InstructionFactory.createLoad(Type.OBJECT, 0));
        il.append(InstructionConst.ARRAYLENGTH);
        il.append(InstructionFactory.createStore(Type.INT, 1));
        InstructionHandle ih_3 = il.append(InstructionFactory.createLoad(Type.OBJECT, 0));
        il.append(new PUSH(cp, 0));
        il.append(InstructionConst.AALOAD);
        il.append(InstructionConst.ARRAYLENGTH);
        il.append(InstructionFactory.createStore(Type.INT, 2));
        InstructionHandle ih_8 = il.append(InstructionFactory.createLoad(Type.INT, 1));
        il.append(InstructionFactory.createLoad(Type.INT, 2));
        il.append(InstructionConst.IMUL);
        il.append(factory.createNewArray(Type.FLOAT, (short) 1));
        il.append(InstructionFactory.createStore(Type.OBJECT, 3));
        InstructionHandle ih_14 = il.append(new PUSH(cp, 0));
        il.append(InstructionFactory.createStore(Type.INT, 4));
        InstructionHandle ih_17 = il.append(InstructionFactory.createLoad(Type.INT, 4));
        il.append(InstructionFactory.createLoad(Type.INT, 2));
        BranchInstruction if_icmpge_20 = InstructionFactory.createBranchInstruction(Const.IF_ICMPGE, null);
        il.append(if_icmpge_20);
        InstructionHandle ih_23 = il.append(new PUSH(cp, 0));
        il.append(InstructionFactory.createStore(Type.INT, 5));
        InstructionHandle ih_26 = il.append(InstructionFactory.createLoad(Type.INT, 5));
        il.append(InstructionFactory.createLoad(Type.INT, 1));
        BranchInstruction if_icmpge_29 = InstructionFactory.createBranchInstruction(Const.IF_ICMPGE, null);
        il.append(if_icmpge_29);
        InstructionHandle ih_32 = il.append(InstructionFactory.createLoad(Type.OBJECT, 3));
        il.append(InstructionFactory.createLoad(Type.INT, 4));
        il.append(InstructionFactory.createLoad(Type.INT, 1));
        il.append(InstructionConst.IMUL);
        il.append(InstructionFactory.createLoad(Type.INT, 5));
        il.append(InstructionConst.IADD);
        il.append(InstructionFactory.createLoad(Type.OBJECT, 0));
        il.append(InstructionFactory.createLoad(Type.INT, 5));
        il.append(InstructionConst.AALOAD);
        il.append(InstructionFactory.createLoad(Type.INT, 4));
        il.append(InstructionConst.IALOAD);
        il.append(InstructionConst.I2F);
        il.append(InstructionConst.FASTORE);
        InstructionHandle ih_49 = il.append(new IINC(5, 1));
        BranchInstruction goto_52 = InstructionFactory.createBranchInstruction(Const.GOTO, ih_26);
        il.append(goto_52);
        InstructionHandle ih_55 = il.append(new IINC(4, 1));
        BranchInstruction goto_58 = InstructionFactory.createBranchInstruction(Const.GOTO, ih_17);
        il.append(goto_58);
        InstructionHandle ih_61 = il.append(factory.createFieldAccess("java.lang.System", "out", new ObjectType("java.io.PrintStream"), Const.GETSTATIC));
        il.append(InstructionFactory.createLoad(Type.OBJECT, 3));
        il.append(factory.createInvoke("java.util.Arrays", "toString", Type.STRING, new Type[]{new ArrayType(Type.FLOAT, 1)}, Const.INVOKESTATIC));
        il.append(factory.createInvoke("java.io.PrintStream", "println", Type.VOID, new Type[]{Type.STRING}, Const.INVOKEVIRTUAL));
        InstructionHandle ih_71 = il.append(InstructionFactory.createLoad(Type.OBJECT, 3));
        il.append(InstructionFactory.createReturn(Type.OBJECT));
        if_icmpge_20.setTarget(ih_61);
        if_icmpge_29.setTarget(ih_55);

        il.append(new RETURN());

        mg.setMaxLocals();
        mg.setMaxStack();
        cg.replaceMethod(mg.getMethod(), mg.getMethod());
        il.dispose();
    }

/*CHECK IN FUTURE: id for field or variable
        int id = ConstantPoolUtils.getFieldIndex(cg,"jcm");
        int id = New.getLoacalVariableID("jcm",cp,mg);
        forLoop[3].setInstruction(new GETSTATIC(numThreadsConstantIndex));*/

    public static void addFutureResultsList(ClassGen cg, MethodGen mg) {
        ConstantPoolGen cp = cg.getConstantPool();
        InstructionFactory factory = new InstructionFactory(cg, cp);
        InstructionList il = new InstructionList();


        InstructionHandle ih_1 = il.append(factory.createInvoke("util.pl.edu.agh.utils.Generico", "init",
                new ObjectType("List<Callable<Integer>>"), Type.NO_ARGS, Const.INVOKESTATIC));


        il.append(InstructionFactory.createStore(Type.OBJECT, 2));
        InstructionHandle ih_2 = il.append(InstructionFactory.createLoad(Type.OBJECT, 2));
        InstructionHandle ih_3 = il.append(InstructionFactory.createStore(Type.OBJECT, 1));


        InstructionHandle ih_4 = il.append(factory.createNew("java.util.concurrent.Callable"));

        InstructionList currentList = mg.getInstructionList();
        il.append(currentList);
        mg.setInstructionList(il);
        updateMethodParametersScope(mg, cp);
        mg.setMaxStack();
        mg.setMaxLocals();
        cg.replaceMethod(mg.getMethod(), mg.getMethod());

    }

    private static void addMethodPrepareFieldsFromArrays1D(ClassGen cg) {

        InstructionList il = new InstructionList();
        ConstantPoolGen cp = cg.getConstantPool();

        MethodGen mg = new MethodGen(Const.ACC_PUBLIC | Const.ACC_STATIC, Type.VOID, new Type[]{new ArrayType(Type.FLOAT, 1), new ArrayType(Type.FLOAT, 1)}, new String[]{"arg0", "arg1"}, "prepareFieldFromArrays1D", cg.getClassName(), il, cp);
        InstructionFactory factory = new InstructionFactory(cg, mg.getConstantPool());

        InstructionHandle ih_0 = il.append(factory.createFieldAccess(cg.getClassName(), "cols_A", Type.INT, Const.GETSTATIC));
        il.append(factory.createFieldAccess(cg.getClassName(), "rows_B", Type.INT, Const.GETSTATIC));
        BranchInstruction if_icmpeq_6 = InstructionFactory.createBranchInstruction(Const.IF_ICMPEQ, null);
        il.append(if_icmpeq_6);
        InstructionHandle ih_9 = il.append(factory.createNew("java.lang.RuntimeException"));
        il.append(InstructionConst.DUP);
        il.append(new PUSH(cp, "Cannot perform multiplication because dimensions are not equal."));
        il.append(factory.createInvoke("java.lang.RuntimeException", "<init>", Type.VOID, new Type[]{Type.STRING}, Const.INVOKESPECIAL));
        il.append(InstructionConst.ATHROW);
        InstructionHandle ih_19 = il.append(InstructionFactory.createLoad(Type.OBJECT, 0));
        il.append(factory.createFieldAccess(cg.getClassName(), "matrix_A", new ArrayType(Type.FLOAT, 1), Const.PUTSTATIC));
        InstructionHandle ih_23 = il.append(InstructionFactory.createLoad(Type.OBJECT, 1));
        il.append(factory.createFieldAccess(cg.getClassName(), "matrix_B", new ArrayType(Type.FLOAT, 1), Const.PUTSTATIC));
        InstructionHandle ih_27 = il.append(new PUSH(cp, 3));
        il.append(factory.createFieldAccess(cg.getClassName(), "cols_A", Type.INT, Const.PUTSTATIC));
        InstructionHandle ih_31 = il.append(new PUSH(cp, 3));
        il.append(factory.createFieldAccess(cg.getClassName(), "rows_A", Type.INT, Const.PUTSTATIC));
        InstructionHandle ih_35 = il.append(new PUSH(cp, 1));
        il.append(factory.createFieldAccess(cg.getClassName(), "cols_B", Type.INT, Const.PUTSTATIC));
        InstructionHandle ih_39 = il.append(new PUSH(cp, 3));
        il.append(factory.createFieldAccess(cg.getClassName(), "rows_B", Type.INT, Const.PUTSTATIC));
        InstructionHandle ih_43 = il.append(factory.createFieldAccess(cg.getClassName(), "cols_B", Type.INT, Const.GETSTATIC));
        il.append(factory.createFieldAccess(cg.getClassName(), "rows_A", Type.INT, Const.GETSTATIC));
        il.append(InstructionConst.IMUL);
        il.append(factory.createNewArray(Type.FLOAT, (short) 1));
        il.append(factory.createFieldAccess(cg.getClassName(), "matrix_C", new ArrayType(Type.FLOAT, 1), Const.PUTSTATIC));
        InstructionHandle ih_55 = il.append(InstructionFactory.createReturn(Type.VOID));
        if_icmpeq_6.setTarget(ih_19);

        mg.setMaxStack();
        mg.setMaxLocals();
        cg.addMethod(mg.getMethod());
        il.dispose();
    }

    private static void addMethodPrepareFieldsFromArrays2D(ClassGen cg) {
        InstructionList il = new InstructionList();
        ConstantPoolGen cp = cg.getConstantPool();

        MethodGen mg = new MethodGen(Const.ACC_PUBLIC | Const.ACC_STATIC, Type.VOID, new Type[]{new ArrayType(Type.INT, 2), new ArrayType(Type.INT, 2)}, new String[]{"arg0", "arg1"}, "prepareFieldsFromArrays2D", cg.getClassName(), il, cp);
        InstructionFactory factory = new InstructionFactory(cg, mg.getConstantPool());

        InstructionHandle ih_0 = il.append(InstructionFactory.createLoad(Type.OBJECT, 0));
        il.append(new PUSH(cp, 0));
        il.append(InstructionConst.AALOAD);
        il.append(InstructionConst.ARRAYLENGTH);
        il.append(InstructionFactory.createLoad(Type.OBJECT, 1));
        il.append(InstructionConst.ARRAYLENGTH);
        BranchInstruction if_icmpeq_6 = InstructionFactory.createBranchInstruction(Const.IF_ICMPEQ, null);
        il.append(if_icmpeq_6);
        InstructionHandle ih_9 = il.append(factory.createNew("java.lang.RuntimeException"));
        il.append(InstructionConst.DUP);
        il.append(new PUSH(cp, "Cannot perform multiplication because dimensions are not equal."));
        il.append(factory.createInvoke("java.lang.RuntimeException", "<init>", Type.VOID, new Type[]{Type.STRING}, Const.INVOKESPECIAL));
        il.append(InstructionConst.ATHROW);
        InstructionHandle ih_19 = il.append(InstructionFactory.createLoad(Type.OBJECT, 0));
        il.append(factory.createInvoke(cg.getClassName(), "flattenIArray2Dto1D", new ArrayType(Type.FLOAT, 1), new Type[]{new ArrayType(Type.INT, 2)}, Const.INVOKESTATIC));
        il.append(factory.createFieldAccess(cg.getClassName(), "matrix_A", new ArrayType(Type.FLOAT, 1), Const.PUTSTATIC));
        InstructionHandle ih_26 = il.append(InstructionFactory.createLoad(Type.OBJECT, 1));
        il.append(factory.createInvoke(cg.getClassName(), "flattenIArray2Dto1D", new ArrayType(Type.FLOAT, 1), new Type[]{new ArrayType(Type.INT, 2)}, Const.INVOKESTATIC));
        il.append(factory.createFieldAccess(cg.getClassName(), "matrix_B", new ArrayType(Type.FLOAT, 1), Const.PUTSTATIC));
        InstructionHandle ih_33 = il.append(InstructionFactory.createLoad(Type.OBJECT, 0));
        il.append(new PUSH(cp, 0));
        il.append(InstructionConst.AALOAD);
        il.append(InstructionConst.ARRAYLENGTH);
        il.append(factory.createFieldAccess(cg.getClassName(), "cols_A", Type.INT, Const.PUTSTATIC));
        InstructionHandle ih_40 = il.append(InstructionFactory.createLoad(Type.OBJECT, 0));
        il.append(InstructionConst.ARRAYLENGTH);
        il.append(factory.createFieldAccess(cg.getClassName(), "rows_A", Type.INT, Const.PUTSTATIC));
        InstructionHandle ih_45 = il.append(InstructionFactory.createLoad(Type.OBJECT, 1));
        il.append(new PUSH(cp, 0));
        il.append(InstructionConst.AALOAD);
        il.append(InstructionConst.ARRAYLENGTH);
        il.append(factory.createFieldAccess(cg.getClassName(), "cols_B", Type.INT, Const.PUTSTATIC));
        InstructionHandle ih_52 = il.append(InstructionFactory.createLoad(Type.OBJECT, 1));
        il.append(InstructionConst.ARRAYLENGTH);
        il.append(factory.createFieldAccess(cg.getClassName(), "rows_B", Type.INT, Const.PUTSTATIC));
        InstructionHandle ih_57 = il.append(factory.createFieldAccess(cg.getClassName(), "cols_B", Type.INT, Const.GETSTATIC));
        il.append(factory.createFieldAccess(cg.getClassName(), "rows_A", Type.INT, Const.GETSTATIC));
        il.append(InstructionConst.IMUL);
        il.append(factory.createNewArray(Type.FLOAT, (short) 1));
        il.append(factory.createFieldAccess(cg.getClassName(), "matrix_C", new ArrayType(Type.FLOAT, 1), Const.PUTSTATIC));
        InstructionHandle ih_69 = il.append(InstructionFactory.createReturn(Type.VOID));
        if_icmpeq_6.setTarget(ih_19);

        mg.setMaxStack();
        mg.setMaxLocals();
        cg.addMethod(mg.getMethod());
        il.dispose();
    }

    public static void addMultiplyMethod(ClassGen cg) {
        InstructionList il = new InstructionList();
        ConstantPoolGen cp = cg.getConstantPool();

        MethodGen mg = new MethodGen(Const.ACC_PUBLIC | Const.ACC_STATIC, Type.VOID, Type.NO_ARGS, new String[]{}, "multiply", cg.getClassName(), il, cp);

        InstructionFactory factory = new InstructionFactory(cg, cp);

        InstructionHandle ih_0 = il.append(factory.createInvoke("jcuda.jcublas.JCublas", "cublasInit", Type.INT, Type.NO_ARGS, Const.INVOKESTATIC));
        il.append(InstructionConst.POP);
        InstructionHandle ih_4 = il.append(factory.createFieldAccess(cg.getClassName(), "cols_A", Type.INT, Const.GETSTATIC));
        il.append(factory.createFieldAccess(cg.getClassName(), "rows_A", Type.INT, Const.GETSTATIC));
        il.append(InstructionConst.IMUL);
        il.append(new PUSH(cp, Sizeof.FLOAT));
        il.append(factory.createFieldAccess(cg.getClassName(), "ptr_A", new ObjectType("jcuda.Pointer"), Const.GETSTATIC));
        il.append(factory.createInvoke("jcuda.jcublas.JCublas", "cublasAlloc", Type.INT, new Type[]{Type.INT, Type.INT, new ObjectType("jcuda.Pointer")}, Const.INVOKESTATIC));
        il.append(InstructionConst.POP);
        InstructionHandle ih_19 = il.append(factory.createFieldAccess(cg.getClassName(), "cols_B", Type.INT, Const.GETSTATIC));
        il.append(factory.createFieldAccess(cg.getClassName(), "rows_B", Type.INT, Const.GETSTATIC));
        il.append(InstructionConst.IMUL);
        il.append(new PUSH(cp, Sizeof.FLOAT));
        il.append(factory.createFieldAccess(cg.getClassName(), "ptr_B", new ObjectType("jcuda.Pointer"), Const.GETSTATIC));
        il.append(factory.createInvoke("jcuda.jcublas.JCublas", "cublasAlloc", Type.INT, new Type[]{Type.INT, Type.INT, new ObjectType("jcuda.Pointer")}, Const.INVOKESTATIC));
        il.append(InstructionConst.POP);
        InstructionHandle ih_34 = il.append(factory.createFieldAccess(cg.getClassName(), "cols_B", Type.INT, Const.GETSTATIC));
        il.append(factory.createFieldAccess(cg.getClassName(), "rows_A", Type.INT, Const.GETSTATIC));
        il.append(InstructionConst.IMUL);
        il.append(new PUSH(cp, Sizeof.FLOAT));
        il.append(factory.createFieldAccess(cg.getClassName(), "ptr_C", new ObjectType("jcuda.Pointer"), Const.GETSTATIC));
        il.append(factory.createInvoke("jcuda.jcublas.JCublas", "cublasAlloc", Type.INT, new Type[]{Type.INT, Type.INT, new ObjectType("jcuda.Pointer")}, Const.INVOKESTATIC));
        il.append(InstructionConst.POP);
        InstructionHandle ih_49 = il.append(factory.createFieldAccess(cg.getClassName(), "cols_A", Type.INT, Const.GETSTATIC));
        il.append(factory.createFieldAccess(cg.getClassName(), "rows_A", Type.INT, Const.GETSTATIC));
        il.append(InstructionConst.IMUL);
        il.append(new PUSH(cp, Sizeof.FLOAT));
        il.append(factory.createFieldAccess(cg.getClassName(), "matrix_A", new ArrayType(Type.FLOAT, 1), Const.GETSTATIC));
        il.append(factory.createInvoke("jcuda.Pointer", "to", new ObjectType("jcuda.Pointer"), new Type[]{new ArrayType(Type.FLOAT, 1)}, Const.INVOKESTATIC));
        il.append(new PUSH(cp, 1));
        il.append(factory.createFieldAccess(cg.getClassName(), "ptr_A", new ObjectType("jcuda.Pointer"), Const.GETSTATIC));
        il.append(new PUSH(cp, 1));
        il.append(factory.createInvoke("jcuda.jcublas.JCublas", "cublasSetVector", Type.INT, new Type[]{Type.INT, Type.INT, new ObjectType("jcuda.Pointer"), Type.INT, new ObjectType("jcuda.Pointer"), Type.INT}, Const.INVOKESTATIC));
        il.append(InstructionConst.POP);
        InstructionHandle ih_72 = il.append(factory.createFieldAccess(cg.getClassName(), "cols_B", Type.INT, Const.GETSTATIC));
        il.append(factory.createFieldAccess(cg.getClassName(), "rows_B", Type.INT, Const.GETSTATIC));
        il.append(InstructionConst.IMUL);
        il.append(new PUSH(cp, Sizeof.FLOAT));
        il.append(factory.createFieldAccess(cg.getClassName(), "matrix_B", new ArrayType(Type.FLOAT, 1), Const.GETSTATIC));
        il.append(factory.createInvoke("jcuda.Pointer", "to", new ObjectType("jcuda.Pointer"), new Type[]{new ArrayType(Type.FLOAT, 1)}, Const.INVOKESTATIC));
        il.append(new PUSH(cp, 1));
        il.append(factory.createFieldAccess(cg.getClassName(), "ptr_B", new ObjectType("jcuda.Pointer"), Const.GETSTATIC));
        il.append(new PUSH(cp, 1));
        il.append(factory.createInvoke("jcuda.jcublas.JCublas", "cublasSetVector", Type.INT, new Type[]{Type.INT, Type.INT, new ObjectType("jcuda.Pointer"), Type.INT, new ObjectType("jcuda.Pointer"), Type.INT}, Const.INVOKESTATIC));
        il.append(InstructionConst.POP);
        InstructionHandle ih_95 = il.append(factory.createFieldAccess(cg.getClassName(), "cols_B", Type.INT, Const.GETSTATIC));
        il.append(factory.createFieldAccess(cg.getClassName(), "rows_A", Type.INT, Const.GETSTATIC));
        il.append(InstructionConst.IMUL);
        il.append(new PUSH(cp, Sizeof.FLOAT));
        il.append(factory.createFieldAccess(cg.getClassName(), "matrix_C", new ArrayType(Type.FLOAT, 1), Const.GETSTATIC));
        il.append(factory.createInvoke("jcuda.Pointer", "to", new ObjectType("jcuda.Pointer"), new Type[]{new ArrayType(Type.FLOAT, 1)}, Const.INVOKESTATIC));
        il.append(new PUSH(cp, 1));
        il.append(factory.createFieldAccess(cg.getClassName(), "ptr_C", new ObjectType("jcuda.Pointer"), Const.GETSTATIC));
        il.append(new PUSH(cp, 1));
        il.append(factory.createInvoke("jcuda.jcublas.JCublas", "cublasSetVector", Type.INT, new Type[]{Type.INT, Type.INT, new ObjectType("jcuda.Pointer"), Type.INT, new ObjectType("jcuda.Pointer"), Type.INT}, Const.INVOKESTATIC));
        il.append(InstructionConst.POP);
        InstructionHandle ih_118 = il.append(new PUSH(cp, 110));
        il.append(new PUSH(cp, 110));
        il.append(factory.createFieldAccess(cg.getClassName(), "rows_A", Type.INT, Const.GETSTATIC));
        il.append(factory.createFieldAccess(cg.getClassName(), "cols_B", Type.INT, Const.GETSTATIC));
        il.append(factory.createFieldAccess(cg.getClassName(), "cols_A", Type.INT, Const.GETSTATIC));
        il.append(factory.createFieldAccess(cg.getClassName(), "alpha", Type.FLOAT, Const.GETSTATIC));
        il.append(factory.createFieldAccess(cg.getClassName(), "ptr_A", new ObjectType("jcuda.Pointer"), Const.GETSTATIC));
        il.append(factory.createFieldAccess(cg.getClassName(), "rows_A", Type.INT, Const.GETSTATIC));
        il.append(factory.createFieldAccess(cg.getClassName(), "ptr_B", new ObjectType("jcuda.Pointer"), Const.GETSTATIC));
        il.append(factory.createFieldAccess(cg.getClassName(), "rows_B", Type.INT, Const.GETSTATIC));
        il.append(factory.createFieldAccess(cg.getClassName(), "beta", Type.FLOAT, Const.GETSTATIC));
        il.append(factory.createFieldAccess(cg.getClassName(), "ptr_C", new ObjectType("jcuda.Pointer"), Const.GETSTATIC));
        il.append(factory.createFieldAccess(cg.getClassName(), "rows_A", Type.INT, Const.GETSTATIC));
        il.append(factory.createInvoke("jcuda.jcublas.JCublas", "cublasSgemm", Type.VOID, new Type[]{Type.CHAR, Type.CHAR, Type.INT, Type.INT, Type.INT, Type.FLOAT, new ObjectType("jcuda.Pointer"), Type.INT, new ObjectType("jcuda.Pointer"), Type.INT, Type.FLOAT, new ObjectType("jcuda.Pointer"), Type.INT}, Const.INVOKESTATIC));
        InstructionHandle ih_158 = il.append(factory.createFieldAccess(cg.getClassName(), "cols_B", Type.INT, Const.GETSTATIC));
        il.append(factory.createFieldAccess(cg.getClassName(), "rows_A", Type.INT, Const.GETSTATIC));
        il.append(InstructionConst.IMUL);
        il.append(new PUSH(cp, Sizeof.FLOAT));
        il.append(factory.createFieldAccess(cg.getClassName(), "ptr_C", new ObjectType("jcuda.Pointer"), Const.GETSTATIC));
        il.append(new PUSH(cp, 1));
        il.append(factory.createFieldAccess(cg.getClassName(), "matrix_C", new ArrayType(Type.FLOAT, 1), Const.GETSTATIC));
        il.append(factory.createInvoke("jcuda.Pointer", "to", new ObjectType("jcuda.Pointer"), new Type[]{new ArrayType(Type.FLOAT, 1)}, Const.INVOKESTATIC));
        il.append(new PUSH(cp, 1));
        il.append(factory.createInvoke("jcuda.jcublas.JCublas", "cublasGetVector", Type.INT, new Type[]{Type.INT, Type.INT, new ObjectType("jcuda.Pointer"), Type.INT, new ObjectType("jcuda.Pointer"), Type.INT}, Const.INVOKESTATIC));
        il.append(InstructionConst.POP);
        InstructionHandle ih_181 = il.append(factory.createFieldAccess(cg.getClassName(), "ptr_A", new ObjectType("jcuda.Pointer"), Const.GETSTATIC));
        il.append(factory.createInvoke("jcuda.jcublas.JCublas", "cublasFree", Type.INT, new Type[]{new ObjectType("jcuda.Pointer")}, Const.INVOKESTATIC));
        il.append(InstructionConst.POP);
        InstructionHandle ih_188 = il.append(factory.createFieldAccess(cg.getClassName(), "ptr_B", new ObjectType("jcuda.Pointer"), Const.GETSTATIC));
        il.append(factory.createInvoke("jcuda.jcublas.JCublas", "cublasFree", Type.INT, new Type[]{new ObjectType("jcuda.Pointer")}, Const.INVOKESTATIC));
        il.append(InstructionConst.POP);
        InstructionHandle ih_195 = il.append(factory.createFieldAccess(cg.getClassName(), "ptr_C", new ObjectType("jcuda.Pointer"), Const.GETSTATIC));
        il.append(factory.createInvoke("jcuda.jcublas.JCublas", "cublasFree", Type.INT, new Type[]{new ObjectType("jcuda.Pointer")}, Const.INVOKESTATIC));
        il.append(InstructionConst.POP);
        InstructionHandle ih_202 = il.append(factory.createInvoke("jcuda.jcublas.JCublas", "cublasShutdown", Type.INT, Type.NO_ARGS, Const.INVOKESTATIC));
        il.append(InstructionConst.POP);
        InstructionHandle ih_206 = il.append(InstructionFactory.createReturn(Type.VOID));
        mg.setMaxStack();
        mg.setMaxLocals();
        cg.addMethod(mg.getMethod());
//    il.dispose();
    }

    public static void addThreadPoolExecutorService(ClassGen cg) {

        Optional<MethodGen> classInitMethod = MethodUtils.findMethodByName(cg, Const.STATIC_INITIALIZER_NAME);
        ConstantPoolGen cp = cg.getConstantPool();
        addClassFields_old(cg, cp);
        InstructionList il_new = new InstructionList();

        classInitMethod.ifPresent(init -> {
            il_new.append(init.getInstructionList());
            try {
                il_new.delete(il_new.getEnd());
            } catch (TargetLostException e) {
                e.printStackTrace();
            }
            retargetStaticPuts(cg, il_new);
        });

        InstructionFactory _factory = new InstructionFactory(cg, cp);

        String className = cg.getClassName();

        addFieldNumThreads(il_new, _factory, className);

        MethodGen mg = new MethodGen(Const.ACC_STATIC,
                Type.VOID, Type.NO_ARGS, new String[0],
                Const.STATIC_INITIALIZER_NAME,
                className, il_new, cp);


        mg.setMaxLocals();
        mg.setMaxStack();
        cg.replaceMethod(mg.getMethod(), mg.getMethod());
//        il_new.dispose();
    }

    public static void addTryCatchService(ClassGen cg, MethodGen mg) {

        InstructionList il = new InstructionList();
        InstructionList il_old = mg.getInstructionList();

//        System.out.println(il_old.getEnd().getPosition());

        InstructionFactory factory = new InstructionFactory(cg, mg.getConstantPool());

        //get instruction from method for append to end
//        -1 is default arg val
        MethodUtils.getBodyFromMethodToNewInstructionList(mg, il, il_old);

//      ****************************************************************************************************
        InstructionHandle startTry = il.append(factory.createFieldAccess(
                cg.getClassName(),
                "SERVICE",
                new ObjectType("java.util.concurrent.ExecutorService"),
                Const.GETSTATIC));

        il.append(InstructionFactory.createLoad(Type.OBJECT, 55));
        il.append(factory.createInvoke("java.util.concurrent.ExecutorService", "invokeAll", new ObjectType("java.util.List"), new Type[]{new ObjectType("java.util.Collection")}, Const.INVOKEINTERFACE));
        il.append(InstructionConst.POP);

//        il.append(factory.createInvoke(
//                "java.util.concurrent.ExecutorService",
//                "invokeAll",
//                Type.VOID, Type.NO_ARGS,
//                Const.INVOKEINTERFACE));

        il.append(factory.createFieldAccess(
                cg.getClassName(),
                "SERVICE",
                new ObjectType("java.util.concurrent.ExecutorService"),
                Const.GETSTATIC));

        InstructionHandle endTry = il.append(factory.createInvoke(
                "java.util.concurrent.ExecutorService",
                "shutdown",
                Type.VOID, Type.NO_ARGS,
                Const.INVOKEINTERFACE));

        BranchInstruction gotoNext = InstructionFactory.createBranchInstruction(Const.GOTO, null);
        il.append(gotoNext);

        InstructionHandle startCatch = il.append(InstructionFactory.createStore(Type.OBJECT, mg.getMaxLocals()));

        il.append(InstructionFactory.createLoad(Type.OBJECT, mg.getMaxLocals()));
        il.append(factory.createInvoke(
                "java.lang.Exception",
                "printStackTrace",
                Type.VOID, Type.NO_ARGS,
                Const.INVOKEVIRTUAL));

        InstructionHandle returnId = il.append(InstructionFactory.createReturn(Type.VOID));

        gotoNext.setTarget(returnId);
        mg.addExceptionHandler(startTry, endTry, startCatch, new ObjectType("java.lang.Exception"));
//      ****************************************************************************************************

        mg.setInstructionList(il);
        updateMethodParametersScope(mg, mg.getConstantPool());
        mg.setMaxStack();
        mg.setMaxLocals();
        cg.replaceMethod(mg.getMethod(), mg.getMethod());
        il.dispose();
    }

    public static void addTryCatchServiceExecutor(ClassGen cg) {

        ConstantPoolGen cp = cg.getConstantPool();
        InstructionList il = new InstructionList();

        MethodGen mg = new MethodGen(Const.ACC_STATIC | Const.ACC_PUBLIC,
                Type.VOID, Type.NO_ARGS, null, "serviceCall", cg.getClassName(), il, cp);
        InstructionFactory factory = new InstructionFactory(cg, mg.getConstantPool());

        il.append(factory.createFieldAccess("java.lang.System", "out", new ObjectType("java.io.PrintStream"), Const.GETSTATIC));
        il.append(new PUSH(cp, 10));
        il.append(factory.createInvoke("java.io.PrintStream", "print", Type.VOID, new Type[]{Type.STRING}, Const.INVOKEVIRTUAL));

        InstructionHandle ih_22 = il.append(factory.createFieldAccess(cg.getClassName(), "SERVICE", new ObjectType("java.util.concurrent.ExecutorService"), Const.GETSTATIC));
        InstructionHandle ih_35 = il.append(factory.createInvoke("java.util.concurrent.ExecutorService", "shutdown", Type.VOID, Type.NO_ARGS, Const.INVOKEINTERFACE));

        InstructionHandle iha_22 = il.append(factory.createFieldAccess(cg.getClassName(),
                "SERVICE", new ObjectType("java.util.concurrent.ExecutorService"), Const.GETSTATIC));
        InstructionHandle iha_35 = il.append(factory.createInvoke("java.util.concurrent.ExecutorService",
                "invokeAll", Type.VOID, Type.NO_ARGS, Const.INVOKEINTERFACE));

        InstructionHandle ih_40;

        BranchInstruction goto_40 = InstructionFactory.createBranchInstruction(Const.GOTO, null);

        ih_40 = il.append(goto_40);
        InstructionHandle ih_43 = il.append(InstructionFactory.createStore(Type.OBJECT, 1));
        InstructionHandle ih_44 = il.append(InstructionFactory.createLoad(Type.OBJECT, 1));
        il.append(factory.createInvoke("java.lang.Exception", "printStackTrace",
                Type.VOID, Type.NO_ARGS, Const.INVOKEVIRTUAL));
        InstructionHandle ih_48 = il.append(InstructionFactory.createReturn(Type.VOID));

        goto_40.setTarget(ih_48);
        mg.addExceptionHandler(ih_22, iha_35, ih_43, new ObjectType("java.lang.Exception"));

        il.append(new RETURN());
        mg.setInstructionList(il);

        mg.setMaxStack();
        mg.setMaxLocals();

        cg.addMethod(mg.getMethod());
        il.dispose();
    }

    protected static void adjustInstructionTarget(InstructionHandle instruction, InstructionHandle returnHandle, InstructionHandle loopBeginning) {
        short opCode = instruction.getInstruction().getOpcode();
        BranchHandle branchHandle = (BranchHandle) instruction;
        if (opCode == Const.GOTO) branchHandle.setTarget(loopBeginning);
        else branchHandle.setTarget(returnHandle);
    }

//    copyLoopToMethod
//    ok if start & end condition for loop are simple int,
//    so if you copy loop from main method to subtask method and occures dynamic size like array.length
//    then must care about it and make changes in code,
//    pass arguments to method in right type (not int, but maybe object/array)

    public static void changeLoopLimitToNumberOfThreads(ClassGen cg, MethodGen mg) {
        InstructionList il = mg.getInstructionList();
        InstructionHandle[] forLoop = LoopUtils.getForLoop(mg);
        int numThreadsConstantIndex = ConstantPoolUtils.getFieldIndex(cg, LaunchProperties.NUMBER_OF_THREADS_NAME);
        forLoop[3].setInstruction(new GETSTATIC(numThreadsConstantIndex));

        cg.replaceMethod(mg.getMethod(), mg.getMethod());
    }

    public static void copyLoopToSubTaskMethod(ClassGen classGen, MethodGen methodGen, String iteratorName) {

        InstructionList subTaskInstructionList = getSubtaskInstructions(methodGen);

        subTaskInstructionList.append(new ICONST(1));
        subTaskInstructionList.append(InstructionFactory.createReturn(Type.INT));
        //TODO - would be nicer to get it by param from analyzer

        MethodGen subTaskMethod = new MethodGen(Const.ACC_PUBLIC | Const.ACC_STATIC,
                Type.INT,//TODO - would be nicer to get it by param from analyzer
                new Type[]{},
                new String[]{},
                LaunchProperties.SUBTASK_METHOD_NAME,
                classGen.getClassName(),
                subTaskInstructionList,
                classGen.getConstantPool());

        LocalVariableGen startVariable = subTaskMethod.addLocalVariable(LaunchProperties.START_INDEX_VAR_NAME,
                Type.INT,
                //0,
                null, null);

        LocalVariableGen endVariable = subTaskMethod.addLocalVariable(LaunchProperties.END_INDEX_VAR_NAME,
                Type.INT,
                //1,
                null, null);

        transferLocalVariables(methodGen, subTaskMethod);
        updateBranchInstructions(subTaskInstructionList);
        int newLoopIteratorVariableIndex = LocalVariableUtils.getLVarByName(iteratorName, subTaskMethod.getLocalVariableTable(classGen.getConstantPool())).getIndex();
        LoopUtils.broadenCompareCondition(subTaskInstructionList.getInstructionHandles());
        LoopUtils.updateLoopVariableIndex(subTaskInstructionList.getInstructionHandles(), newLoopIteratorVariableIndex);
        LoopUtils.updateLoopStartCondition(subTaskInstructionList.getInstructionHandles(), startVariable.getIndex());
        LoopUtils.updateLoopEndCondition(subTaskInstructionList.getInstructionHandles(), endVariable.getIndex());

        subTaskMethod.setArgumentNames(new String[]{LaunchProperties.START_INDEX_VAR_NAME, LaunchProperties.END_INDEX_VAR_NAME});
        subTaskMethod.setArgumentTypes(new Type[]{Type.INT, Type.INT});
        subTaskMethod.setMaxLocals();
        subTaskMethod.setMaxStack();
        classGen.addMethod(subTaskMethod.getMethod());
        classGen.getConstantPool().addMethodref(subTaskMethod);
    }

    private static void createFields(ClassGen cg, MethodGen mg) {
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

    static private void deleteIns(InstructionList il, InstructionHandle ih, InstructionHandle new_target) {
        // System.out.println("deleteIns: instructionList = " + il);
        // System.out.println("   handle = " + ih);
        try {
            il.delete(ih);
        } catch (TargetLostException e) {
            InstructionHandle[] targets = e.getTargets();
            for (InstructionHandle target : targets) {
                InstructionTargeter[] targeters = target.getTargeters();

                for (InstructionTargeter targeter : targeters) {
                    targeter.updateTarget(target, new_target);
                }
            }
        }
    }

    private static InstructionHandle gatHandleOnNewList(InstructionHandle handle, InstructionList il) {
        return Arrays.stream(il.getInstructionHandles())
                .filter(listHandle -> handle.getInstruction().equals(listHandle.getInstruction()))
                .findFirst()
                .orElse(il.getEnd());
    }

    protected static InstructionList getSubtaskInstructions(MethodGen mg) {
        InstructionHandle[] loopInstructions = LoopUtils.getForLoop(mg);
        InstructionList il = new InstructionList();
        for (InstructionHandle instr : loopInstructions) {
            if (instr instanceof BranchHandle) {
                BranchHandle branch = (BranchHandle) instr;
                il.append((BranchInstruction) branch.getInstruction().copy());
            } else {
                il.append(instr.getInstruction().copy());
            }
        }
        return il;
    }

    private static List<Integer> getVariableIndexes(InstructionList il) {
        //we will not consider 4th instruction which is for(...; I < VAR;...) because VAR will be replaced
        InstructionHandle irrelevant = il.getInstructionHandles()[0].getNext().getNext().getNext();
        return Arrays.stream(il.getInstructionHandles())
                .filter(handle -> !handle.equals(irrelevant))
                .filter(handle -> handle.getInstruction() instanceof LoadInstruction)
                .map(handle -> (LoadInstruction) handle.getInstruction())
                .map(LoadInstruction::getIndex)
                .distinct()
                .collect(Collectors.toList());
    }

    private static List<LocalVariableGen> getVariablesToCopy(MethodGen mg, InstructionList il) {
        List<Integer> variableIndexes = getVariableIndexes(il);
        return Arrays.stream(mg.getLocalVariables())
                .filter(variable -> variableIndexes.contains(variable.getIndex()))
                .collect(Collectors.toList());
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
        updateMethodParametersScope(mg, cp);
        mg.setMaxStack();
        mg.setMaxLocals();
        cg.replaceMethod(mg.getMethod(), mg.getMethod());
    }

    private static void initFields(ClassGen cg, InstructionList il, InstructionFactory factory, ConstantPoolGen cp) {
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

    private static void injectJcmMultiply(MethodGen mg, ConstantPoolGen cp, InstructionFactory factory, InstructionList il_new) {
        int id_A, id_B, jcm_ID, id;

        id_A = New.createArrayField("AA", mg, il_new, cp, Type.INT, 2, new int[]{2, 2});
        id_B = New.createArrayField("BB", mg, il_new, cp, Type.INT, 2, new int[]{2, 2});

        jcm_ID = New.createObjectClass("jcm", "utils.JCudaMatrix", il_new, factory, mg, new int[]{id_A, id_B});
        il_new.append(new ALOAD(jcm_ID));

        il_new.append(factory.createInvoke("utils.JCudaMatrix", "multiply", new ArrayType(Type.FLOAT, 1), new Type[]{}, Const.INVOKEVIRTUAL));
        LocalVariableGen lg = mg.addLocalVariable("CC", new ArrayType(Type.FLOAT, 1), null, null);

        id = lg.getIndex();
        il_new.append(new ASTORE(id));
        New.printArray(il_new, mg, factory, id, false);
    }

    private static void injectJcmMultiplyAB(ClassGen cg, MethodGen mg, ConstantPoolGen cp, InstructionFactory factory, InstructionList il_new) {
        int id_A, id_B, jcm_ID, id;

        id_A = LocalVariableUtils.getLVarByName(LaunchProperties.ARRAY_1, mg.getLocalVariableTable(cp)).getIndex();
        id_B = LocalVariableUtils.getLVarByName(LaunchProperties.ARRAY_2, mg.getLocalVariableTable(cp)).getIndex();


        jcm_ID = New.createObjectClass("jcm", "utils.JCudaMatrix", il_new, factory, mg, new int[]{id_A, id_B});
        il_new.append(new ALOAD(jcm_ID));

        il_new.append(factory.createInvoke("utils.JCudaMatrix", "multiply", new ArrayType(Type.FLOAT, 1), new Type[]{}, Const.INVOKEVIRTUAL));
        LocalVariableGen lg = mg.addLocalVariable("CC", new ArrayType(Type.FLOAT, 1), null, null);

        id = lg.getIndex();
        il_new.append(new ASTORE(id));
        New.printArray(il_new, mg, factory, id, true);
    }

    private static void injectPrintArrName(ClassGen cg, MethodGen mg, InstructionFactory factory, InstructionList il_new) {
        int id = ConstantPoolUtils.getFieldIndex(cg, "C");
        New.printArray(il_new, mg, factory, id, false);
    }

    private static void injectPrintVarName(MethodGen mg, ConstantPoolGen cp, InstructionFactory factory, InstructionList il_new) {
        int id = LocalVariableUtils.getIdLVarByName("luckyNumber",mg);
        il_new.append(factory.createFieldAccess("java.lang.System", "out",
                new ObjectType("java.io.PrintStream"), Const.GETSTATIC));
        il_new.append(new DUP());
        il_new.append(new ALOAD(id));
        il_new.append(factory.createInvoke("java.io.PrintStream", "println",
                Type.VOID, new Type[]{Type.STRING}, Const.INVOKEVIRTUAL));
    }

    public static void insertNewInstruciton(ClassGen cg, MethodGen mg, int option) throws Exception {

        ConstantPoolGen cp = cg.getConstantPool();

        InstructionFactory factory = new InstructionFactory(cg, cp);

        InstructionList il_new = new InstructionList();
        InstructionList il_old = mg.getInstructionList();

        Instruction i_current;

        InstructionHandle handle;
        InstructionHandle[] handles = il_old.getInstructionHandles();


        InstructionHandle TRY_START = il_new.append(handles[0].getInstruction());

//TODO: GET BODY FROM METHOD AND PUT INTO TRY CATCH BLOCK

        for (int i = 1; i < handles.length - 1; i++) {

            handle = handles[i];
            i_current = handle.getInstruction();

//            System.out.println("opcode: " + i_current.getOpcode() + "\tname: " + i_current.getName());

            if (i_current.getOpcode() == Const.RETURN) {
                System.out.println(i_current.getOpcode() + " appears in " + mg.getName());
            } else il_new.append(i_current);

        }
//        System.out.println("last instruction: " + handles[handles.length - 1].getInstruction().getOpcode());


        switch (option) {

            case 1: //CODE INJECTION - instruction jcm.multiply
                injectJcmMultiply(mg, cp, factory, il_new);
                break;
            case 2: //CODE INJECTION - printArray
                injectPrintArrName(cg, mg, factory, il_new);
                break;
            case 3: //CODE INJECTION - get id luckyNumber
                injectPrintVarName(mg, cp, factory, il_new);
                break;
            case 4: //CODE INJECTION - instruction jcm.multiply(A,B)
                injectJcmMultiplyAB(cg, mg, cp, factory, il_new);
                break;
            default:
                System.out.println("wrong option!");
                break;
        }

        addBlockCatch(TRY_START, il_new, factory, mg);

        il_new.append(new RETURN());
        mg.setInstructionList(il_new);
        mg.stripAttributes(true);
        mg.setMaxStack();
        mg.setMaxLocals();
        cg.replaceMethod(mg.getMethod(), mg.getMethod());
        il_new.dispose();//what's this for?

    }

    public static void printSomething(InstructionList il_new, InstructionFactory factory, ConstantPoolGen cp) {
        il_new.append(factory.createFieldAccess(
                "java.lang.System", "out",
                new ObjectType("java.io.PrintStream"), Const.GETSTATIC));
        il_new.append(InstructionConst.DUP);
        il_new.append(new PUSH(cp, "Hello, "));
        il_new.append(factory.createInvoke("java.io.PrintStream", "println",
                Type.VOID, new Type[]{Type.STRING}, Const.INVOKEVIRTUAL));
    }

//    JCUDA PART

    public static void removeBodyForLoopInSelectedMethod(ClassGen cg, MethodGen mg) {
        InstructionHandle[] forLoop = LoopUtils.getForLoop(mg);
        LoopUtils.emptyMethodLoop(mg, forLoop);
        cg.replaceMethod(mg.getMethod(), mg.getMethod());
        updateMethodParametersScope(mg, mg.getConstantPool());
        mg.setMaxStack();
        mg.setMaxLocals();
        cg.replaceMethod(mg.getMethod(), mg.getMethod());
    }

    private static void retargetSingleHandle(ConstantPoolGen cp, InstructionHandle handle, int classNameIndex) {
        PUTSTATIC staticPut = (PUTSTATIC) handle.getInstruction();
        ConstantFieldref constant = (ConstantFieldref) cp.getConstant(staticPut.getIndex());
        constant.setClassIndex(classNameIndex);
    }

    protected static void retargetStaticPuts(ClassGen cg, InstructionList il) {
        int classNameIndex = cg.getClassNameIndex();
        Arrays.stream(il.getInstructionHandles())
                .filter(handle -> handle.getInstruction() instanceof PUTSTATIC)
                .forEach(handle -> retargetSingleHandle(cg.getConstantPool(), handle, classNameIndex));
    }

    public static void setNewLoopBody(ClassGen cg, MethodGen mg, short dataSize) {
        InstructionList il = mg.getInstructionList();
        InstructionHandle[] loopHandles = LoopUtils.getForLoop(mg);
        InstructionHandle firstLoopInstruction = loopHandles[0];
        InstructionHandle lastLoopInstruction = loopHandles[loopHandles.length - 1];

        mg.addLocalVariable(LaunchProperties.START_INDEX_VAR_NAME,
                Type.INT,
                firstLoopInstruction,
                lastLoopInstruction);

        mg.addLocalVariable(LaunchProperties.END_INDEX_VAR_NAME,
                Type.FLOAT,
                firstLoopInstruction,
                lastLoopInstruction);

        mg.addLocalVariable(LaunchProperties.END_FINAL_INDEX_VAR_NAME,
                Type.INT,
                firstLoopInstruction,
                lastLoopInstruction);


        InstructionHandle startVarStart = loopHandles[4];
        InstructionList endVarStart = InstructionUtils.getStartInitInstructions(cg, mg, dataSize);
        InstructionHandle startVarEnd = endVarStart.getEnd();

        InstructionList endVarEnd = InstructionUtils.getEndInitInstructions(cg, mg, dataSize);
        InstructionHandle startVarFinalEnd = endVarEnd.getEnd();

        InstructionList endVarFinalEnd = InstructionUtils.getEndFinalInitInstructions(cg, mg);
        InstructionHandle startListCallable = endVarEnd.getEnd();

        InstructionList endListCallable = InstructionUtils.getListCallableInstructions(cg, mg);


        il.append(startVarStart, endVarStart);
        il.append(startVarEnd, endVarEnd);

        il.append(startListCallable, endListCallable);
        il.append(startVarFinalEnd, endVarFinalEnd);

        updateMethodParametersScope(mg, mg.getConstantPool());
        mg.setMaxStack();
        mg.setMaxLocals();
        cg.replaceMethod(mg.getMethod(), mg.getMethod());

    }

    public static void transferLocalVariables(MethodGen mg, MethodGen subTaskmg) {
        InstructionList il = subTaskmg.getInstructionList();
        List<LocalVariableGen> variablesToCopy = getVariablesToCopy(mg, il);
        for (LocalVariableGen localVariableGen : variablesToCopy) {

            subTaskmg.addLocalVariable(localVariableGen.getName(),
                    localVariableGen.getType(),
                    gatHandleOnNewList(localVariableGen.getStart(), il),
                    gatHandleOnNewList(localVariableGen.getEnd(), il));
        }
        Map<Integer, Integer> oldIndexesToNewIndexes = new HashMap<>();
        for (LocalVariableGen variable : variablesToCopy) {
            oldIndexesToNewIndexes.putIfAbsent(variable.getIndex(), LocalVariableUtils.getLVarByName(variable.getName(), subTaskmg.getLocalVariableTable(mg.getConstantPool())).getIndex());
        }
        updateVariableInstructions(variablesToCopy, subTaskmg, oldIndexesToNewIndexes);
    }

    protected static void updateBranchInstructions(InstructionList instructions) {
        InstructionHandle returnHandle = instructions.getInstructionHandles()[instructions.getInstructionHandles().length - 2];
        InstructionHandle loopBeginning = instructions.getInstructionHandles()[2];
        Arrays.stream(instructions.getInstructionHandles())
                .filter(BranchHandle.class::isInstance)
                .forEach(instr -> adjustInstructionTarget(instr, returnHandle, loopBeginning));
    }

    protected static void updateBranchInstructions2(InstructionList instructions) {
        InstructionHandle returnHandle = instructions.getInstructionHandles()[instructions.getInstructionHandles().length - 1];
        InstructionHandle loopBeginning = instructions.getInstructionHandles()[0];
        Arrays.stream(instructions.getInstructionHandles())
                .filter(BranchHandle.class::isInstance)
                .forEach(instr -> adjustInstructionTarget(instr, returnHandle, loopBeginning));
    }

    public static void updateMethodParametersScope(MethodGen mg, ConstantPoolGen cp) {
        InstructionHandle startHandle = mg.getInstructionList().getInstructionHandles()[0];
        Arrays.stream(mg.getLocalVariables())
                .filter(var -> var.getLocalVariable(cp).getStartPC() == 0)
                .forEach(var -> var.setStart(startHandle));
    }

    private static void updateVariableInstructions(List<LocalVariableGen> variablesToCopy, MethodGen subTaskmg, Map<Integer, Integer> oldIndexesToNewIndexes) {
        InstructionHandle[] instructionHandles = subTaskmg.getInstructionList().getInstructionHandles();
        List<Integer> indexes = variablesToCopy.stream()
                .map(LocalVariableGen::getIndex)
                .collect(Collectors.toList());
        Arrays.stream(instructionHandles)
                .filter(handle -> handle.getInstruction() instanceof LocalVariableInstruction)
                .map(handle -> (LocalVariableInstruction) handle.getInstruction())
                .filter(instruction -> indexes.contains(instruction.getIndex()))
                .forEach(instruction -> instruction.setIndex(oldIndexesToNewIndexes.get(instruction.getIndex())));
    }

}
