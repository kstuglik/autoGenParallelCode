package pl.edu.agh.bcel.utils;

import jcuda.Sizeof;
import org.apache.bcel.Const;
import org.apache.bcel.generic.*;

public class ReadyMethods {

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

    protected static void addMethodPrepareFieldsFromArrays1D(ClassGen cg) {

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

    protected static void addMethodPrepareFieldsFromArrays2D(ClassGen cg) {
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

    public static void addMethodSetStepLVar(ClassGen cg) {

        ConstantPoolGen cp = cg.getConstantPool();
        InstructionList il = new InstructionList();
        MethodGen method = new MethodGen(Const.ACC_PRIVATE | Const.ACC_STATIC, Type.INT, Type.NO_ARGS, new String[]{}, "setStepLVar", cg.getClassName(), il, cp);
        InstructionFactory factory = new InstructionFactory(method.getConstantPool());

        InstructionHandle ih_0 = il.append(factory.createFieldAccess(cg.getClassName(), "matrixA", new ArrayType(Type.INT, 2), Const.GETSTATIC));
        il.append(InstructionConst.ARRAYLENGTH);
        il.append(factory.createFieldAccess(cg.getClassName(), "NUM_THREADS", Type.INT, Const.GETSTATIC));
        BranchInstruction if_icmpge_7 = InstructionFactory.createBranchInstruction(Const.IF_ICMPGE, null);
        il.append(if_icmpge_7);
        InstructionHandle ih_10 = il.append(new PUSH(cp, 1));
        il.append(InstructionFactory.createReturn(Type.INT));
        InstructionHandle ih_12 = il.append(factory.createFieldAccess(cg.getClassName(), "matrixA", new ArrayType(Type.INT, 2), Const.GETSTATIC));
        il.append(InstructionConst.ARRAYLENGTH);
        il.append(factory.createFieldAccess(cg.getClassName(), "NUM_THREADS", Type.INT, Const.GETSTATIC));
        il.append(InstructionConst.IDIV);
        il.append(InstructionFactory.createReturn(Type.INT));
        if_icmpge_7.setTarget(ih_12);
        method.setMaxStack();
        method.setMaxLocals();
        cg.addMethod(method.getMethod());
        il.dispose();
    }

    public static void addMethodToInitTaskPool(ClassGen cg) {
        ConstantPoolGen cp = cg.getConstantPool();
        InstructionFactory factory = new InstructionFactory(cg, cg.getConstantPool());
        InstructionList il = new InstructionList();
        MethodGen mg = new MethodGen(Const.ACC_PUBLIC | Const.ACC_STATIC,
                new ObjectType("java.util.ArrayList"), Type.NO_ARGS, new String[]{},
                "init", null, il, cp);

        il.append(factory.createNew("java.util.ArrayList"));
        il.append(InstructionConst.DUP);
        il.append(factory.createInvoke("java.util.ArrayList", "<init>", Type.VOID, Type.NO_ARGS, Const.INVOKESPECIAL));
        il.append(InstructionFactory.createReturn(Type.OBJECT));

        mg.setMaxLocals();
        mg.setMaxStack();
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


}
