package pl.edu.agh.utils;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.generic.*;
import pl.edu.agh.transformations.LaunchProperties;

public class PreparedFieldsAndMethods {

    public static void addFieldStep(ClassGen cg, MethodGen mg) {
        InstructionList il = new InstructionList();
        InstructionFactory factory = new InstructionFactory(cg, mg.getConstantPool());
        ConstantPoolGen cp = mg.getConstantPool();

        int id = mg.addLocalVariable("step", Type.INT, null, null).getIndex();

        InstructionHandle ih_0 = il.append(factory.createInvoke(mg.getClassName(), "setStepLVar", Type.INT, Type.NO_ARGS, Const.INVOKESTATIC));
        il.append(InstructionFactory.createStore(Type.INT, id));

        InstructionList currentList = mg.getInstructionList();
        il.append(currentList);
        mg.setInstructionList(il);
//        TransformUtils.updateMethodParametersScope(mg, cp);
        mg.setMaxStack();
        mg.setMaxLocals();
        cg.replaceMethod(mg.getMethod(), mg.getMethod());
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

    public static void addSetStepLVar(ClassGen cg) {

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
        il.append(factory.createReturn(Type.INT));
        InstructionHandle ih_12 = il.append(factory.createFieldAccess(cg.getClassName(), "matrixA", new ArrayType(Type.INT, 2), Const.GETSTATIC));
        il.append(InstructionConst.ARRAYLENGTH);
        il.append(factory.createFieldAccess(cg.getClassName(), "NUM_THREADS", Type.INT, Const.GETSTATIC));
        il.append(InstructionConst.IDIV);
        il.append(factory.createReturn(Type.INT));
        if_icmpge_7.setTarget(ih_12);
        method.setMaxStack();
        method.setMaxLocals();
        cg.addMethod(method.getMethod());
        il.dispose();
    }

    //    IMPORTANT: WE USE "STATIC" - CAN BE SET IN LAUNCHPROPERTIES - CALL-TASK
    public static void changeBodyInnerLoopForMatrixMultiply(MethodGen mg, InstructionList ilNEW, InstructionFactory factory) {
        int idRowNum = LocalVariableUtils.getIdLVarByName("rowNum", mg);
        int idColNum = LocalVariableUtils.getIdLVarByName("colNum", mg);

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

        LocalVariable step = LocalVariableUtils.getLVarByName("step", mg.getLocalVariableTable(mg.getConstantPool()));

        LocalVariableGen[] lvt = mg.getLocalVariables();
        for (int i = 0; i < lvt.length; i++) {
            System.out.println(lvt[i].getName());
        }

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


}
