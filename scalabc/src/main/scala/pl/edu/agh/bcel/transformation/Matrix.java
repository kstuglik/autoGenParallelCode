package pl.edu.agh.bcel.transformation;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.generic.*;
import pl.edu.agh.bcel.LaunchProperties;
import pl.edu.agh.bcel.nested.ElementFOR;
import pl.edu.agh.bcel.utils.ForLoopUtils;
import pl.edu.agh.bcel.utils.VariableUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class Matrix {

    public static void matrixMultiply(ClassGen cg, MethodGen mgOld, ArrayList<ElementFOR> listElementsFOR, InstructionHandle[] ihy) {
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
        //      *****************************************************************************************
        HashMap<Integer, Integer> hashmapIdOldAndNewLVar = VariableUtils.getHashmapLVarIndexesOldAndNew(mgOld, mgNew);
        //      *****************************************************************************************

        ArrayList<BranchHandle> listaIFinFOR = new ArrayList<>();
        ArrayList<BranchInstruction> listaGOTO = new ArrayList<>();
        ArrayList<InstructionHandle> listaSTART = new ArrayList<>();
        ArrayList<InstructionHandle> listaNEXT = new ArrayList<>();
        ArrayList<InstructionHandle> listaINSIDE = new ArrayList<>();
//      *****************************************************************************************
        int idStep = VariableUtils.getLVarIdByName(LaunchProperties.STEP_VAR_NAME, mgNew);
        ElementFOR elementFor1 = listElementsFOR.get(0);

//        INSTRUKCJE WCZEŚNIEJ
        int start = 0;
        int koniec = elementFor1.getIdPrevStore();
        for (int i = start; i < koniec; i++) {
            Instruction ih1 = VariableUtils.updateLVarIndexes(ihy[i].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
            il.append(ih1);
        }

        for (int i = elementFor1.getIdPrevStore(); i < elementFor1.getIdPrevLoad(); i++) {
            Instruction ih1 = VariableUtils.updateLVarIndexes(ihy[i].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
            il.append(ih1);
        }

        Instruction star_00 = VariableUtils.updateLVarIndexes(ihy[elementFor1.getIdPrevLoad()].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
        InstructionHandle aa = il.append(star_00);

        for (int i = elementFor1.getIdPrevLoad() + 1; i < elementFor1.getListWithIdInstructionIfInsideFor().get(0); i++) {
            Instruction ih01 = VariableUtils.updateLVarIndexes(ihy[i].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
            il.append(ih01);
        }

        BranchHandle if_00 = ForLoopUtils.getBranchHandleIF(il, ihy[elementFor1.getListWithIdInstructionIfInsideFor().get(0)].getInstruction());

//        ***************************************************************
        ElementFOR elementFor2 = listElementsFOR.get(1);

        for (int i = elementFor2.getIdPrevStore(); i < elementFor2.getIdPrevLoad(); i++) {
            Instruction ih1 = VariableUtils.updateLVarIndexes(ihy[i].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
            il.append(ih1);
        }

        Instruction start_11 = VariableUtils.updateLVarIndexes(ihy[elementFor2.getIdPrevLoad()].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
        InstructionHandle bb = il.append(start_11);

        for (int i = elementFor2.getIdPrevLoad() + 1; i < elementFor2.getListWithIdInstructionIfInsideFor().get(0); i++) {
            Instruction ih1 = VariableUtils.updateLVarIndexes(ihy[i].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
            il.append(ih1);
        }

        BranchHandle if_11 = ForLoopUtils.getBranchHandleIF(il, ihy[elementFor2.getListWithIdInstructionIfInsideFor().get(0)].getInstruction());

        listaINSIDE.add(il.append(factory.createPrintln("ABC")));
        int pole1 = elementFor1.getidIterator();
        int pole2 = elementFor2.getidIterator();

        LocalVariableGen finalStart = mgNew.addLocalVariable("finalStart", Type.INT, null, null);
        il.append(new ILOAD(pole1));
        il.append(new F2I());
        il.append(new ISTORE(finalStart.getIndex()));

        LocalVariableGen finalEnd = mgNew.addLocalVariable("finalEnd", Type.INT, null, null);
        il.append(new ILOAD(pole2));
        il.append(new F2I());
        il.append(new ISTORE(finalEnd.getIndex()));

        String nameStep = VariableUtils.getLVarNameById(idStep, mgNew);

        il.append(InstructionFactory.createLoad(Type.OBJECT, LaunchProperties.TASK_POOL_ID));
        il.append(factory.createNew(new ObjectType("Callable<Integer>() {" +
                "public Integer call() {" +
                "return " + LaunchProperties.SUBTASK_METHOD_NAME + "(" + finalStart.getName() + "," + finalEnd.getName() + "," + nameStep + ");" +
                "}}"
        )));

        il.append(factory.createInvoke("java.util.List", "add", Type.BOOLEAN, new Type[]{Type.OBJECT}, Const.INVOKEINTERFACE));


        ElementFOR elementFor3 = listElementsFOR.get(2);

        for (int i = elementFor2.getIdInsideLoop(); i < elementFor3.getIdPrevStore(); i++) {
            Instruction ih1 = VariableUtils.updateLVarIndexes(ihy[i].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
            il.append(ih1);
        }

        Instruction inc_11 = VariableUtils.updateLVarIndexes(ihy[elementFor2.getIdInc()].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
        InstructionHandle bbb = il.append(inc_11);

        BranchInstruction goto_11 = InstructionFactory.createBranchInstruction(Const.GOTO, bb);
        il.append(goto_11);
        listaGOTO.add(goto_11);
//********************************************************************

//        specjalne increment o step wiec trza zmienic
        ArrayList<Instruction> instrukcje = ForLoopUtils.crateIncrementInForLoopByVariable(
                ihy[elementFor1.getIdInc()].getInstruction(), hashmapIdOldAndNewLVar, idStep, "+");

        InstructionHandle aaa = il.append(instrukcje.get(0));
        for (int i = 1; i < instrukcje.size(); i++) il.append(instrukcje.get(i));

        BranchInstruction goto_00 = InstructionFactory.createBranchInstruction(Const.GOTO, aa);
        il.append(goto_00);
        listaGOTO.add(goto_00);


        InstructionHandle startTry = il.append(factory.createFieldAccess(
                cg.getClassName(),
                "SERVICE",
                new ObjectType("java.util.concurrent.ExecutorService"),
                Const.GETSTATIC));

        il.append(InstructionFactory.createLoad(Type.OBJECT, 55));
        il.append(factory.createInvoke("java.util.concurrent.ExecutorService", "invokeAll", new ObjectType("java.util.List"), new Type[]{new ObjectType("java.util.Collection")}, Const.INVOKEINTERFACE));
        il.append(InstructionConst.POP);


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

        InstructionHandle startCatch = il.append(InstructionFactory.createStore(Type.OBJECT, mgNew.getMaxLocals()));

        il.append(InstructionFactory.createLoad(Type.OBJECT, mgNew.getMaxLocals()));
        il.append(factory.createInvoke(
                "java.lang.Exception",
                "printStackTrace",
                Type.VOID, Type.NO_ARGS,
                Const.INVOKEVIRTUAL));


        InstructionHandle returnHandler = il.append(InstructionFactory.createReturn(Type.VOID));

        gotoNext.setTarget(returnHandler);

//      ****************************************************************************************************

        mgNew.setInstructionList(il);
//        updateMethodParametersScope(mgNew, mgNew.getConstantPool());


        if_00.setTarget(startTry);
        if_11.setTarget(aaa);
        mgNew.addExceptionHandler(startTry, endTry, startCatch, new ObjectType("java.lang.Exception"));


        mgNew.setMaxLocals();
        mgNew.setMaxStack();
        cg.replaceMethod(mgOld.getMethod(), mgNew.getMethod());
        cg.getConstantPool().addMethodref(mgNew);
    }

    public static void matrixSubtask(ClassGen cg, MethodGen mgOld, ArrayList<ElementFOR> listElementsFOR, InstructionHandle[] ihy) {
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
                Type.INT, Type.NO_ARGS, new String[]{}, LaunchProperties.SUBTASK_METHOD_NAME,
                cg.getClassName(), il, cp);
//        rowNum, int colNum, int step
        LocalVariableGen startVariable = mgNew.addLocalVariable(LaunchProperties.ROW_NUM_VAR_NAME, Type.INT, null, null);
        LocalVariableGen endVariable = mgNew.addLocalVariable(LaunchProperties.COL_NUM_VAR_NAME, Type.INT, null, null);
        LocalVariableGen stepVariable = mgNew.addLocalVariable(LaunchProperties.STEP_VAR_NAME, Type.INT, null, null);
        //      *****************************************************************************************
        HashMap<Integer, Integer> hashmapIdOldAndNewLVar = VariableUtils.getHashmapLVarIndexesOldAndNew(mgOld, mgNew);
        //      *****************************************************************************************

        LocalVariableGen currRowwVariable = mgNew.addLocalVariable(LaunchProperties.CURR_ROW_VAR_NAME, Type.INT, null, null);


        for (int i = 0; i < listElementsFOR.size(); i++) {
            System.out.println("FOR nr: " + i + ", posiada id inc: " + listElementsFOR.get(i).getidIterator());
        }

        int iteratr_0 = listElementsFOR.get(0).getidIterator();
        if (iteratr_0 == -1) System.out.println("TO JEST BLAD ITERATORA!!!");

        LocalVariable lv = currRowwVariable.getLocalVariable(mgNew.getConstantPool());
        System.out.println("iteratr_0, lv.getIndex(): \t" + iteratr_0 + "," + lv.getIndex());

//        zapamietaj jeszcze na chwole
        int oldidIterator = hashmapIdOldAndNewLVar.get(iteratr_0);

        hashmapIdOldAndNewLVar.replace(iteratr_0, lv.getIndex());

        ArrayList<BranchHandle> listaIFinFOR = new ArrayList<>();
        ArrayList<BranchInstruction> listaGOTO = new ArrayList<>();
        ArrayList<InstructionHandle> listaSTART = new ArrayList<>();
        ArrayList<InstructionHandle> listaNEXT = new ArrayList<>();
        ArrayList<InstructionHandle> listaINSIDE = new ArrayList<>();
        //      *****************************************************************************************


        ElementFOR elementFor = listElementsFOR.get(0);

        for (int i = elementFor.getIdPrevStore(); i < elementFor.getIdPrevLoad(); i++) {
            Instruction ih1 = VariableUtils.updateLVarIndexes(ihy[i].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
            il.append(ih1);
        }

        Instruction star_00 = VariableUtils.updateLVarIndexes(ihy[elementFor.getIdPrevLoad()].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
        InstructionHandle aa = il.append(star_00);

        for (int i = elementFor.getIdPrevLoad() + 1; i < elementFor.getListWithIdInstructionIfInsideFor().get(0); i++) {
            Instruction ih01 = VariableUtils.updateLVarIndexes(ihy[i].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
            il.append(ih01);
        }

        BranchHandle if_00 = ForLoopUtils.getBranchHandleIF(il, ihy[elementFor.getListWithIdInstructionIfInsideFor().get(0)].getInstruction());

//        *****************************************************************************************
//        powtorzenei pierwszego ifa, dzialanie celowe poniewac increment jest +step, wicc nie moze przejsc za to
        Instruction star_01 = VariableUtils.updateLVarIndexes(ihy[elementFor.getIdPrevLoad()].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
        InstructionHandle aa1 = il.append(star_00);

        il.append(new ILOAD(2));//step.index = 2
//        w tej czesc icelowo nie ma aktualizacji indeksu, bo trzeba wziac te zmienna do warunki
//        taki trick, bo po tym ja dale, odgornie ze cos zostaje zamienione to w haszmapiie tez sie zmienilo...
        il.append(new ILOAD(oldidIterator));
        il.append(new IADD());
        BranchHandle if_01 = ForLoopUtils.getBranchHandleIF(il, ihy[elementFor.getListWithIdInstructionIfInsideFor().get(0)].getInstruction());


//        ***************************************************************
        elementFor = listElementsFOR.get(2);


        for (int i = elementFor.getIdPrevStore(); i < elementFor.getIdPrevLoad(); i++) {
            Instruction ih1 = VariableUtils.updateLVarIndexes(ihy[i].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
            il.append(ih1);
        }

        Instruction start_11 = VariableUtils.updateLVarIndexes(ihy[elementFor.getIdPrevLoad()].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
        InstructionHandle bb = il.append(start_11);

        for (int i = elementFor.getIdPrevLoad() + 1; i < elementFor.getListWithIdInstructionIfInsideFor().get(0); i++) {
            Instruction ih1 = VariableUtils.updateLVarIndexes(ihy[i].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
            il.append(ih1);
        }

        BranchHandle if_11 = ForLoopUtils.getBranchHandleIF(il, ihy[elementFor.getListWithIdInstructionIfInsideFor().get(0)].getInstruction());

        listaINSIDE.add(il.append(factory.createPrintln("ABC")));
        for (int i = elementFor.getListWithIdInstructionIfInsideFor().get(0) + 1; i < elementFor.getIdInc(); i++) {
            Instruction ih1 = VariableUtils.updateLVarIndexes(ihy[i].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
            il.append(ih1);
        }

        Instruction inc_11 = VariableUtils.updateLVarIndexes(ihy[elementFor.getIdInc()].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
        InstructionHandle bbb = il.append(inc_11);

        BranchInstruction goto_11 = InstructionFactory.createBranchInstruction(Const.GOTO, bb);
        il.append(goto_11);
        listaGOTO.add(goto_11);
//********************************************************************
        elementFor = listElementsFOR.get(0);
        Instruction inc_00 = VariableUtils.updateLVarIndexes(ihy[elementFor.getIdInc()].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
        InstructionHandle aaa = il.append(inc_00);

        BranchInstruction goto_00 = InstructionFactory.createBranchInstruction(Const.GOTO, aa);
        il.append(goto_00);
        listaGOTO.add(goto_00);


        InstructionHandle returnHandler = il.append(new PUSH(cp, 0));
        il.append(InstructionFactory.createReturn(Type.INT));

        if_00.setTarget(returnHandler);
        if_01.setTarget(returnHandler);
        if_11.setTarget(aaa);

        mgNew.setArgumentNames(new String[]{
                LaunchProperties.START_CONDITION_NAME,
                LaunchProperties.STOP_CONDITION_NAME,
                LaunchProperties.STEP_VAR_NAME
        });
        mgNew.setArgumentTypes(new Type[]{Type.INT, Type.INT, Type.INT});
        mgNew.setMaxLocals();
        mgNew.setMaxStack();
        cg.addMethod(mgNew.getMethod());
        cg.getConstantPool().addMethodref(mgNew);

    }

}
