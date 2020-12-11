package pl.edu.agh.bcel.transformation;

import org.apache.bcel.Const;
import org.apache.bcel.generic.*;
import pl.edu.agh.bcel.LaunchProperties;
import pl.edu.agh.bcel.nested.ElementFOR;
import pl.edu.agh.bcel.utils.ForLoopUtils;
import pl.edu.agh.bcel.utils.VariableUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class Histogram {

    public static void histogramCalculate(ClassGen cg, MethodGen mgOld, ArrayList<ElementFOR> listElementsFOR) {

        InstructionHandle[] ihy = mgOld.getInstructionList().getInstructionHandles();
//      *****************************************************************************************
        HashMap<Integer, ArrayList<BranchHandle>> hashmapIFinFOR = new HashMap<>();
        HashMap<Integer, ArrayList<BranchInstruction>> hashmapGOTO = new HashMap<>();
        HashMap<Integer, ArrayList<InstructionHandle>> hashmapNEXT = new HashMap<>();
        HashMap<Integer, ArrayList<InstructionHandle>> hashmapSTART = new HashMap<>();
//      *****************************************************************************************
        InstructionList il = new InstructionList();
        InstructionFactory factory = new InstructionFactory(cg, cg.getConstantPool());
        ConstantPoolGen cp = cg.getConstantPool();
//      *****************************************************************************************
        MethodGen mgNew = new MethodGen(Const.ACC_PUBLIC | Const.ACC_STATIC,
                Type.VOID, Type.NO_ARGS, new String[]{}, LaunchProperties.CLASS_METHOD,
                cg.getClassName(), il, cp);

        int l1 = mgNew.addLocalVariable(LaunchProperties.START_CONDITION_NAME, Type.INT, null, null).getIndex();
        int l2 = mgNew.addLocalVariable(LaunchProperties.STOP_CONDITION_NAME, Type.INT, null, null).getIndex();
//      *****************************************************************************************
        HashMap<Integer, Integer> hashmapIdOldAndNewLVar = VariableUtils.getHashmapLVarIndexesOldAndNew(mgOld, mgNew);
//      *****************************************************************************************
        ArrayList<BranchHandle> listaIFinFOR = new ArrayList<>();
        ArrayList<BranchInstruction> listaGOTO = new ArrayList<>();
        ArrayList<InstructionHandle> listaSTART = new ArrayList<>();
        ArrayList<InstructionHandle> listaNEXT = new ArrayList<>();
        ArrayList<InstructionHandle> listaINSIDE = new ArrayList<>();
//      *****************************************************************************************

        FieldGen field2 = new FieldGen(Const.ACC_PUBLIC | Const.ACC_STATIC, new ObjectType("java.util.List"), "partialResults", cp);

        cg.addField(field2.getField());
        InstructionHandle ih_0 = il.append(factory.createNew("java.util.ArrayList"));
        il.append(InstructionConst.DUP);
        il.append(factory.createInvoke("java.util.ArrayList", "<init>", Type.VOID, Type.NO_ARGS, Const.INVOKESPECIAL));
        il.append(factory.createFieldAccess(cg.getClassName(), "partialResults", new ObjectType("java.util.List"), Const.PUTSTATIC));

        ElementFOR item = listElementsFOR.get(0);
        int idIteratorZaktualizowane = hashmapIdOldAndNewLVar.get(item.getidIterator());

        for (int j = 0; j < item.getIdPrevLoad(); j++) {
            Instruction instruction = ihy[j].getInstruction();
            il.append(VariableUtils.updateLVarIndexes(instruction, hashmapIdOldAndNewLVar, mgNew, cg));
        }

        Instruction instruction = ihy[item.getIdPrevLoad()].getInstruction();
        InstructionHandle prevLoad = il.append(VariableUtils.updateLVarIndexes(instruction, hashmapIdOldAndNewLVar, mgNew, cg));

        il.append(factory.createFieldAccess(cg.getClassName(), "NUM_THREADS", Type.INT, Const.GETSTATIC));
        BranchHandle if_from_for_0 = ForLoopUtils.getBranchHandleIF(il, ihy[item.getListWithIdInstructionIfInsideFor().get(0)].getInstruction());

//        start variable
        il.append(new ILOAD(idIteratorZaktualizowane));
        il.append(factory.createFieldAccess(cg.getClassName(), "data",
                new ArrayType("FLOAT", 1), Const.GETSTATIC));
        il.append(new ARRAYLENGTH());
        il.append(new IMUL());
        il.append(factory.createFieldAccess(cg.getClassName(), "NUM_THREADS", Type.INT, Const.GETSTATIC));
        il.append(new IDIV());
        il.append(InstructionFactory.createStore(Type.INT, l1));


//         end variable
        il.append(new ILOAD(idIteratorZaktualizowane));
        il.append(factory.createFieldAccess(cg.getClassName(), "data", new ArrayType("FLOAT", 1), Const.GETSTATIC));
        il.append(new ARRAYLENGTH());
        il.append(factory.createFieldAccess(cg.getClassName(), "NUM_THREADS", Type.INT, Const.GETSTATIC));
        il.append(factory.createInvoke(cg.getClassName(), "setStop", Type.INT, new Type[]{Type.INT, Type.INT, Type.INT}, Const.INVOKESTATIC));
        il.append(new ISTORE(l2));

        il.append(InstructionFactory.createLoad(Type.OBJECT, LaunchProperties.TASK_POOL_ID));
        il.append(factory.createNew(new ObjectType("Callable() {" +
                "@Override public float[] call() throws Exception { return " + LaunchProperties.SUBTASK_METHOD_NAME + "(" +
                LaunchProperties.START_CONDITION_NAME + "," + LaunchProperties.STOP_CONDITION_NAME +
                ");}}")));

        il.append(factory.createInvoke("java.util.List", "add", Type.BOOLEAN, new Type[]{Type.OBJECT}, Const.INVOKEINTERFACE));

        instruction = ihy[item.getIdInc()].getInstruction();
        InstructionHandle inc_from_for_0 = il.append(VariableUtils.updateLVarIndexes(instruction, hashmapIdOldAndNewLVar, mgNew, cg));
        BranchHandle goto_from_for_0 = il.append(new GOTO(prevLoad));

//      ****************************************************************************************************

        InstructionHandle start_try_0 = il.append(factory.createFieldAccess(cg.getClassName(), "SERVICE", new ObjectType("java.util.concurrent.ExecutorService"), Const.GETSTATIC));

        il.append(InstructionFactory.createLoad(Type.OBJECT, 55));
        il.append(factory.createInvoke("java.util.concurrent.ExecutorService", "invokeAll", new ObjectType("java.util.List"), new Type[]{new ObjectType("java.util.Collection")}, Const.INVOKEINTERFACE));
        il.append(factory.createFieldAccess(cg.getClassName(), "partialResults", new ObjectType("java.util.List"), Const.PUTSTATIC));
        il.append(factory.createFieldAccess(cg.getClassName(), "SERVICE", new ObjectType("java.util.concurrent.ExecutorService"), Const.GETSTATIC));
        InstructionHandle end_try_0 = il.append(factory.createInvoke("java.util.concurrent.ExecutorService", "shutdown", Type.VOID, Type.NO_ARGS, Const.INVOKEINTERFACE));

        BranchInstruction goto_behind_try_catch_0 = InstructionFactory.createBranchInstruction(Const.GOTO, null);
        il.append(goto_behind_try_catch_0);

        InstructionHandle start_catch_0 = il.append(InstructionFactory.createStore(Type.OBJECT, mgNew.getMaxLocals()));
        il.append(InstructionFactory.createLoad(Type.OBJECT, mgNew.getMaxLocals()));
        il.append(factory.createInvoke("java.lang.Exception", "printStackTrace", Type.VOID, Type.NO_ARGS, Const.INVOKEVIRTUAL));
//      ****************************************************************************************************
        if_from_for_0.setTarget(start_try_0);
        mgNew.addExceptionHandler(start_try_0, end_try_0, start_catch_0, new ObjectType("java.lang.Exception"));
//      ****************************************************************************************************
        InstructionHandle afterTryCatch = il.append(factory.createPrintln("asd"));
//      **************************************************************************************************
        item = listElementsFOR.get(0);
        int idIteratorI2 = mgNew.addLocalVariable("i2", Type.INT, null, null).getIndex();
        il.append(new ICONST(0));
        il.append(new ISTORE(idIteratorI2));
        InstructionHandle prevLoad2 = il.append(new ILOAD(idIteratorI2));
        il.append(factory.createFieldAccess(cg.getClassName(), "results", new ArrayType(Type.FLOAT, 1), Const.GETSTATIC));
        il.append(InstructionConst.ARRAYLENGTH);
        BranchHandle if_from_for_1 = ForLoopUtils.getBranchHandleIF(il, ihy[item.getListWithIdInstructionIfInsideFor().get(0)].getInstruction());
//**********************************************************************************************************************
        LocalVariableGen l4 = mgNew.addLocalVariable(LaunchProperties.TEMP_RESULT_NAME,
                new ObjectType("java.util.concurrent.Future"), null, null);

        il.append(factory.createFieldAccess(cg.getClassName(), "partialResults", new ArrayType(Type.FLOAT, 1), Const.GETSTATIC));
        il.append(InstructionFactory.createLoad(Type.INT, idIteratorI2));
        il.append(factory.createInvoke(cg.getClassName(), "get", new ObjectType("java.util.List"), new Type[]{Type.INT}, Const.INVOKEINTERFACE));
        il.append(factory.createCheckCast(new ObjectType("java.util.concurrent.Future")));
        il.append(InstructionFactory.createStore(Type.OBJECT, l4.getIndex()));

        LocalVariableGen l5 = mgNew.addLocalVariable(LaunchProperties.PART_RESULT_NAME, new ArrayType(Type.INT, 1), null, null);


        InstructionHandle start_try_11 = il.append(factory.createPrintln("Przed drugim FOR"));

        il.append(new ALOAD(l4.getIndex()));
        il.append(factory.createInvoke("java.util.concurrent.Future", "get", Type.OBJECT, Type.NO_ARGS, Const.INVOKEINTERFACE));

        il.append(factory.createCheckCast(new ArrayType(Type.INT, 1)));
        il.append(InstructionFactory.createStore(new ArrayType(Type.INT, 1), l5.getIndex()));


//********************************************************************************************************

        for (int i = item.getIdPrevStore(); i < item.getIdPrevLoad(); i++) {
            instruction = ihy[i].getInstruction();
            il.append(VariableUtils.updateLVarIndexes(instruction, hashmapIdOldAndNewLVar, mgNew, cg));
        }
        instruction = ihy[item.getIdPrevLoad()].getInstruction();
        InstructionHandle prev_from_for_11 = il.append(VariableUtils.updateLVarIndexes(instruction, hashmapIdOldAndNewLVar, mgNew, cg));


//        tutaj wiedzialem ze jest podmiana tablic - nazw, dlatego takie a nie inne przesuniecie pobranej instrukcji
        il.append(factory.createFieldAccess(cg.getClassName(), "results", new ArrayType(Type.FLOAT, 1), Const.GETSTATIC));
//        il.append(InstructionConst.ARRAYLENGTH);
        for (int i = item.getIdPrevLoad() + 2; i < item.getListWithIdInstructionIfInsideFor().get(0); i++) {
            instruction = ihy[i].getInstruction();
            il.append(VariableUtils.updateLVarIndexes(instruction, hashmapIdOldAndNewLVar, mgNew, cg));
        }

        instruction = ihy[item.getListWithIdInstructionIfInsideFor().get(0)].getInstruction();
        BranchHandle if_from_for_11 = ForLoopUtils.getBranchHandleIF(il, instruction);
//specyficzna podmiana (miejscowo), zamiast data -> part

        il.append(factory.createFieldAccess(cg.getClassName(), "results", new ArrayType(Type.FLOAT, 1), Const.GETSTATIC));
        il.append(new ILOAD(idIteratorZaktualizowane));
//        il.append(new DUP());

        il.append(factory.createFieldAccess(cg.getClassName(), "results", new ArrayType(Type.FLOAT, 1), Const.GETSTATIC));
        il.append(new ILOAD(idIteratorZaktualizowane));
        il.append(new AALOAD());
        il.append(new ILOAD(l5.getIndex()));
        il.append(new ILOAD(idIteratorZaktualizowane));
        il.append(new IALOAD());
        il.append(new I2F());
        il.append(new FADD());
        il.append(new FASTORE());

        instruction = ihy[item.getIdInc()].getInstruction();
        InstructionHandle inc_from_for_11 = il.append(VariableUtils.updateLVarIndexes(instruction, hashmapIdOldAndNewLVar, mgNew, cg));

        BranchHandle goto_from_for_11 = il.append(new GOTO(prev_from_for_11));

//********************************************************************************************************
        instruction = ihy[item.getIdInc()].getInstruction();
        InstructionHandle inc_from_for_1 = il.append(new IINC(idIteratorI2, 1));
        BranchHandle goto_from_for_1 = il.append(new GOTO(prevLoad2));
//**************************************************************************************************

        BranchInstruction goto_behind_try_catch_11 = InstructionFactory.createBranchInstruction(Const.GOTO, null);
        il.append(goto_behind_try_catch_11);


        InstructionHandle rh = il.append(InstructionFactory.createStore(Type.OBJECT, mgNew.getMaxLocals()));
        il.append(InstructionFactory.createLoad(Type.OBJECT, mgNew.getMaxLocals()));
        il.append(factory.createInvoke("java.lang.Exception", "printStackTrace", Type.VOID, Type.NO_ARGS, Const.INVOKEVIRTUAL));


//**************************************************************************************************
        goto_behind_try_catch_0.setTarget(afterTryCatch);
        goto_behind_try_catch_11.setTarget(prev_from_for_11);

        mgNew.addExceptionHandler(start_try_11, goto_from_for_11, rh, new ObjectType("java.lang.Exception"));
        InstructionHandle rety = il.append(new RETURN());

        if_from_for_1.setTarget(rety);
        if_from_for_11.setTarget(inc_from_for_1);

//**************************************************************************************************
        goto_behind_try_catch_11.setTarget(inc_from_for_11);

        mgNew.update();

        mgNew.setInstructionList(il);
        mgNew.setMaxLocals();
        mgNew.setMaxStack();
        cg.replaceMethod(mgOld.getMethod(), mgNew.getMethod());
        cg.getConstantPool().addMethodref(mgNew);
    }

    public static void histogramSubtask(ClassGen cg, MethodGen mgOld, ArrayList<ElementFOR> listElementsFOR) {

        InstructionHandle[] ihy = mgOld.getInstructionList().getInstructionHandles();

        InstructionList il = new InstructionList();
        InstructionFactory factory = new InstructionFactory(cg, cg.getConstantPool());
        ConstantPoolGen cp = cg.getConstantPool();
        MethodGen mgNew = new MethodGen(Const.ACC_PUBLIC | Const.ACC_STATIC,
                new ArrayType(Type.FLOAT, 1), Type.NO_ARGS, new String[]{}, LaunchProperties.SUBTASK_METHOD_NAME,
                cg.getClassName(), il, cp);

        HashMap<Integer, Integer> hashmapIdOldAndNewLVar = VariableUtils.getHashmapLVarIndexesOldAndNew(mgOld, mgNew);
        //      *****************************************************************************************

        ArrayList<BranchInstruction> listaGOTO = new ArrayList<>();

        //      *****************************************************************************************
        ElementFOR item = listElementsFOR.get(0);

        LocalVariableGen lg = mgNew.addLocalVariable("partialResultsResult", new ArrayType("FLOAT", 1), null, null);
        int idpartialResults = lg.getIndex();
//
        il.append(InstructionFactory.createLoad(Type.OBJECT, 0));
        il.append(factory.createFieldAccess(cg.getClassName(), "results", new ArrayType(Type.FLOAT, 1), Const.GETSTATIC));
        il.append(InstructionConst.ARRAYLENGTH);
        il.append(new NEWARRAY(Type.FLOAT));
        il.append(new ASTORE(idpartialResults));


        il.append(new ILOAD(0));
        for (int i = item.getIdPrevStore() + 1; i < item.getIdPrevLoad(); i++) {
            Instruction ih1 = VariableUtils.updateLVarIndexes(ihy[i].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
            il.append(ih1);
        }

        Instruction star_00 = VariableUtils.updateLVarIndexes(ihy[item.getIdPrevLoad()].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
        InstructionHandle aa = il.append(star_00);


        il.append(new ILOAD(1));

        BranchHandle if_00 = ForLoopUtils.getBranchHandleIF(il, ihy[item.getListWithIdInstructionIfInsideFor().get(0)].getInstruction());

//        *****************************************************************************************
//        sprytna podmiana pierwszej instrukcji ktora jest od "strony lewej"
        il.append(new ALOAD(lg.getIndex()));
        for (int i = item.getIdInsideLoop() + 1; i < item.getIdInc(); i++) {
            Instruction ih1 = VariableUtils.updateLVarIndexes(ihy[i].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
            il.append(ih1);
        }

//        *****************************************************************************************
        item = listElementsFOR.get(0);
        Instruction inc_00 = VariableUtils.updateLVarIndexes(ihy[item.getIdInc()].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
        InstructionHandle aaa = il.append(inc_00);

        BranchInstruction goto_00 = InstructionFactory.createBranchInstruction(Const.GOTO, aa);
        il.append(goto_00);
        listaGOTO.add(goto_00);

        InstructionHandle returnHandler = il.append(InstructionFactory.createLoad(Type.OBJECT, idpartialResults));
        il.append(InstructionFactory.createReturn(Type.OBJECT));

        if_00.setTarget(returnHandler);

        mgNew.setArgumentNames(new String[]{LaunchProperties.START_CONDITION_NAME, LaunchProperties.STOP_CONDITION_NAME});
        mgNew.setArgumentTypes(new Type[]{Type.INT, Type.INT});
        mgNew.setMaxLocals();
        mgNew.setMaxStack();
        cg.addMethod(mgNew.getMethod());
        cg.getConstantPool().addMethodref(mgNew);

    }

}
