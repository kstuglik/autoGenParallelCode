package pl.edu.agh.bcel.NestedLoops;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.generic.*;
import pl.edu.agh.bcel.LaunchProperties;
import pl.edu.agh.bcel.utils.ForLoopUtils;
import pl.edu.agh.bcel.utils.VariableUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class Transformation {

    static void matrixMultiply(ClassGen cg, MethodGen mgOld, ArrayList<ElementFOR> listaFORow, InstructionHandle[] ihy) {
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
        ElementFOR item1 = listaFORow.get(0);

//        INSTRUKCJE WCZEŚNIEJ
        int start = 0;
        int koniec = item1.getIdPrevStore();
        for (int i = start; i < koniec; i++) {
            Instruction ih1 = ForLoopUtils.updateLVarIndexes(ihy[i].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
            il.append(ih1);
        }

        for (int i = item1.getIdPrevStore(); i < item1.getIdPrevLoad(); i++) {
            Instruction ih1 = ForLoopUtils.updateLVarIndexes(ihy[i].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
            il.append(ih1);
        }

        Instruction star_00 = ForLoopUtils.updateLVarIndexes(ihy[item1.getIdPrevLoad()].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
        InstructionHandle aa = il.append(star_00);

        for (int i = item1.getIdPrevLoad() + 1; i < item1.getListaIfow().get(0); i++) {
            Instruction ih01 = ForLoopUtils.updateLVarIndexes(ihy[i].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
            il.append(ih01);
        }

        BranchHandle if_00 = ForLoopUtils.getBranchHandleIF(il, ihy[item1.getListaIfow().get(0)]);

//        ***************************************************************
        ElementFOR item2 = listaFORow.get(1);

        for (int i = item2.getIdPrevStore(); i < item2.getIdPrevLoad(); i++) {
            Instruction ih1 = ForLoopUtils.updateLVarIndexes(ihy[i].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
            il.append(ih1);
        }

        Instruction start_11 = ForLoopUtils.updateLVarIndexes(ihy[item2.getIdPrevLoad()].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
        InstructionHandle bb = il.append(start_11);

        for (int i = item2.getIdPrevLoad() + 1; i < item2.getListaIfow().get(0); i++) {
            Instruction ih1 = ForLoopUtils.updateLVarIndexes(ihy[i].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
            il.append(ih1);
        }

        BranchHandle if_11 = ForLoopUtils.getBranchHandleIF(il, ihy[item2.getListaIfow().get(0)]);

        listaINSIDE.add(il.append(factory.createPrintln("ABC")));
        int pole1 = item1.getIdIteratora();
        int pole2 = item2.getIdIteratora();

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


        ElementFOR item3 = listaFORow.get(2);

        for (int i = item2.getIdInsideLoop(); i < item3.getIdPrevStore(); i++) {
            Instruction ih1 = ForLoopUtils.updateLVarIndexes(ihy[i].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
            il.append(ih1);
        }

        Instruction inc_11 = ForLoopUtils.updateLVarIndexes(ihy[item2.getIdInc()].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
        InstructionHandle bbb = il.append(inc_11);

        BranchInstruction goto_11 = InstructionFactory.createBranchInstruction(Const.GOTO, bb);
        il.append(goto_11);
        listaGOTO.add(goto_11);
//********************************************************************

//        specjalne increment o step wiec trza zmienic
        ArrayList<Instruction> instrukcje = ForLoopUtils.crateIncrementInForLoopByVariable(
                ihy[item1.getIdInc()].getInstruction(), hashmapIdOldAndNewLVar, idStep, "+");

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
//        mgNew.addExceptionHandler(rety, iha_35, ih_43, new ObjectType("java.lang.Exception"));


        mgNew.setMaxLocals();
        mgNew.setMaxStack();
        cg.replaceMethod(mgOld.getMethod(), mgNew.getMethod());
        cg.getConstantPool().addMethodref(mgNew);
    }

    static void matrixSubtask(ClassGen cg, MethodGen mgOld, ArrayList<ElementFOR> listaFORow, InstructionHandle[] ihy) {
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


        for (int i = 0; i < listaFORow.size(); i++) {
            System.out.println("FOR nr: " + i + ", posiada id inc: " + listaFORow.get(i).getIdIteratora());
        }

        int iteratr_0 = listaFORow.get(0).getIdIteratora();
        if (iteratr_0 == -1) System.out.println("TO JEST BLAD ITERATORA!!!");

        LocalVariable lv = currRowwVariable.getLocalVariable(mgNew.getConstantPool());
        System.out.println("iteratr_0, lv.getIndex(): \t" + iteratr_0 + "," + lv.getIndex());

//        zapamietaj jeszcze na chwole
        int oldIdIteratora = hashmapIdOldAndNewLVar.get(iteratr_0);

        hashmapIdOldAndNewLVar.replace(iteratr_0, lv.getIndex());

        ArrayList<BranchHandle> listaIFinFOR = new ArrayList<>();
        ArrayList<BranchInstruction> listaGOTO = new ArrayList<>();
        ArrayList<InstructionHandle> listaSTART = new ArrayList<>();
        ArrayList<InstructionHandle> listaNEXT = new ArrayList<>();
        ArrayList<InstructionHandle> listaINSIDE = new ArrayList<>();
        //      *****************************************************************************************


        ElementFOR item = listaFORow.get(0);

        for (int i = item.getIdPrevStore(); i < item.getIdPrevLoad(); i++) {
            Instruction ih1 = ForLoopUtils.updateLVarIndexes(ihy[i].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
            il.append(ih1);
        }

        Instruction star_00 = ForLoopUtils.updateLVarIndexes(ihy[item.getIdPrevLoad()].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
        InstructionHandle aa = il.append(star_00);

        for (int i = item.getIdPrevLoad() + 1; i < item.getListaIfow().get(0); i++) {
            Instruction ih01 = ForLoopUtils.updateLVarIndexes(ihy[i].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
            il.append(ih01);
        }

        BranchHandle if_00 = ForLoopUtils.getBranchHandleIF(il, ihy[item.getListaIfow().get(0)]);

//        *****************************************************************************************
//        powtorzenei pierwszego ifa, dzialanie celowe poniewac increment jest +step, wicc nie moze przejsc za to
        Instruction star_01 = ForLoopUtils.updateLVarIndexes(ihy[item.getIdPrevLoad()].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
        InstructionHandle aa1 = il.append(star_00);

        il.append(new ILOAD(2));//step.index = 2
//        w tej czesc icelowo nie ma aktualizacji indeksu, bo trzeba wziac te zmienna do warunki
//        taki trick, bo po tym ja dale, odgornie ze cos zostaje zamienione to w haszmapiie tez sie zmienilo...
        il.append(new ILOAD(oldIdIteratora));
        il.append(new IADD());
        BranchHandle if_01 = ForLoopUtils.getBranchHandleIF(il, ihy[item.getListaIfow().get(0)]);


//        ***************************************************************
        item = listaFORow.get(2);


        for (int i = item.getIdPrevStore(); i < item.getIdPrevLoad(); i++) {
            Instruction ih1 = ForLoopUtils.updateLVarIndexes(ihy[i].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
            il.append(ih1);
        }

        Instruction start_11 = ForLoopUtils.updateLVarIndexes(ihy[item.getIdPrevLoad()].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
        InstructionHandle bb = il.append(start_11);

        for (int i = item.getIdPrevLoad() + 1; i < item.getListaIfow().get(0); i++) {
            Instruction ih1 = ForLoopUtils.updateLVarIndexes(ihy[i].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
            il.append(ih1);
        }

        BranchHandle if_11 = ForLoopUtils.getBranchHandleIF(il, ihy[item.getListaIfow().get(0)]);

        listaINSIDE.add(il.append(factory.createPrintln("ABC")));
        for (int i = item.getListaIfow().get(0) + 1; i < item.getIdInc(); i++) {
            Instruction ih1 = ForLoopUtils.updateLVarIndexes(ihy[i].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
            il.append(ih1);
        }

        Instruction inc_11 = ForLoopUtils.updateLVarIndexes(ihy[item.getIdInc()].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
        InstructionHandle bbb = il.append(inc_11);

        BranchInstruction goto_11 = InstructionFactory.createBranchInstruction(Const.GOTO, bb);
        il.append(goto_11);
        listaGOTO.add(goto_11);
//********************************************************************
        item = listaFORow.get(0);
        Instruction inc_00 = ForLoopUtils.updateLVarIndexes(ihy[item.getIdInc()].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
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
                LaunchProperties.START_INDEX_VAR_NAME,
                LaunchProperties.END_INDEX_VAR_NAME,
                LaunchProperties.STEP_VAR_NAME
        });
        mgNew.setArgumentTypes(new Type[]{Type.INT, Type.INT, Type.INT});
        mgNew.setMaxLocals();
        mgNew.setMaxStack();
        cg.addMethod(mgNew.getMethod());
        cg.getConstantPool().addMethodref(mgNew);

    }

    static void nbodyMovies(ClassGen cg, MethodGen mgOld, ArrayList<ElementFOR> listFORow, InstructionHandle[] ihy) {

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
                Type.VOID, Type.NO_ARGS, new String[]{}, mgOld.getName(), null, il, cp);
        HashMap<Integer, Integer> hashmapIdOldAndNewLVar = VariableUtils.getHashmapLVarIndexesOldAndNew(mgOld, mgNew);
        //      *****************************************************************************************
        ElementFOR item = listFORow.get(0);
        ArrayList<BranchHandle> listaIFinFOR = new ArrayList<>();
        ArrayList<BranchInstruction> listaGOTO = new ArrayList<>();
        ArrayList<InstructionHandle> listaSTART = new ArrayList<>();
        //      *****************************************************************************************


        int ile = item.getListaIfow().size();
        int idOstatniegoIfaWPetli = item.getListaIfow().get(ile - 1);

        int start = listFORow.get(0).getIdInsideLoop();
        int inc = listFORow.get(0).getIdInc() - 1;

        for (int j = 0; j < item.getIdPrevLoad(); j++) il.append(ihy[j].getInstruction());

        int numThreadsFieldIndex = VariableUtils.getFieldRefId(cg, LaunchProperties.NUMBER_OF_THREADS_NAME);

        int loopIteratorIndex = ForLoopUtils.getIdLoadOrStoreFromInstruction(ihy[item.getIdPrevLoad() - 1].getInstruction());
        int dataSizeIndex = VariableUtils.getFieldIdByName("dataSize", cg);//zmienic na wartosc z LaunchPropeties

//        zastanowic sie nad tym jak ogarniac te podmiane...
        InstructionHandle prevLoad = il.append(ihy[item.getIdPrevLoad()].getInstruction());
        il.append(factory.createFieldAccess(cg.getClassName(), "NUM_THREADS", Type.INT, Const.GETSTATIC));

        BranchHandle if_0 = ForLoopUtils.getBranchHandleIF(il, ihy[item.getListaIfow().get(0)]);

        LocalVariableGen l1 = mgNew.addLocalVariable(LaunchProperties.START_INDEX_VAR_NAME, Type.INT, null, null);
        LocalVariableGen l2 = mgNew.addLocalVariable(LaunchProperties.END_INDEX_VAR_NAME, Type.INT, null, null);


//        start variable
        il.append(new ILOAD(loopIteratorIndex));
        il.append(new SIPUSH((short) 1000));//TODO would be nice to get rid of short
        il.append(factory.createFieldAccess(cg.getClassName(), "NUM_THREADS", Type.INT, Const.GETSTATIC));
        il.append(new IDIV());
        il.append(new IMUL());
        il.append(new ISTORE(l1.getIndex()));

//         end variable
        il.append(new ILOAD(loopIteratorIndex));
        il.append(factory.createFieldAccess(cg.getClassName(), "dataSize", Type.INT, Const.GETSTATIC));
        il.append(factory.createFieldAccess(cg.getClassName(), "NUM_THREADS", Type.INT, Const.GETSTATIC));
        il.append(factory.createInvoke(cg.getClassName(), "setStop", Type.INT, new Type[]{Type.INT, Type.INT, Type.INT}, Const.INVOKESTATIC));
        il.append(new ISTORE(l2.getIndex()));


        il.append(InstructionFactory.createLoad(Type.OBJECT, LaunchProperties.TASK_POOL_ID));
        il.append(factory.createNew(new ObjectType("Callable<Integer>() {" +
                "public Integer call() {" +
                "return " + LaunchProperties.SUBTASK_METHOD_NAME + "(" +
                LaunchProperties.START_INDEX_VAR_NAME + "," + LaunchProperties.END_INDEX_VAR_NAME +
                ");}}"
        )));

        il.append(factory.createInvoke("java.util.List", "add", Type.BOOLEAN, new Type[]{Type.OBJECT}, Const.INVOKEINTERFACE));

        InstructionHandle next = il.append(ihy[item.getIdInc()].getInstruction());
        BranchInstruction gotobh = InstructionFactory.createBranchInstruction(Const.GOTO, prevLoad);
        il.append(gotobh);

        InstructionHandle rety = il.append(factory.createFieldAccess(cg.getClassName(), "SERVICE", new ObjectType("java.util.concurrent.ExecutorService"), Const.GETSTATIC));
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

        if_0.setTarget(rety);
        mgNew.addExceptionHandler(rety, iha_35, ih_43, new ObjectType("java.lang.Exception"));

        mgNew.setInstructionList(il);

        mgNew.setMaxLocals();
        mgNew.setMaxStack();
        cg.replaceMethod(mgOld.getMethod(), mgNew.getMethod());
    }

    static void nbodySubtask(ClassGen cg, MethodGen mgOld, ArrayList<ElementFOR> listFORow, InstructionHandle[] ihy) {
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
        LocalVariableGen startVariable = mgNew.addLocalVariable(LaunchProperties.START_INDEX_VAR_NAME, Type.INT, null, null);
        LocalVariableGen endVariable = mgNew.addLocalVariable(LaunchProperties.END_INDEX_VAR_NAME, Type.INT, null, null);
        //      *****************************************************************************************
        HashMap<Integer, Integer> hashmapIdOldAndNewLVar = VariableUtils.getHashmapLVarIndexesOldAndNew(mgOld, mgNew);
        //      *****************************************************************************************
        ElementFOR item = listFORow.get(0);
        ArrayList<BranchHandle> listaIFinFOR = new ArrayList<>();
        ArrayList<BranchInstruction> listaGOTO = new ArrayList<>();
        ArrayList<InstructionHandle> listaSTART = new ArrayList<>();
        //      *****************************************************************************************

        il.append(new ILOAD(0));//set start
        Instruction in2 = ForLoopUtils.updateLVarIndexes(ihy[item.getIdPrevStore() + 1].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
        il.append(in2);


        in2 = ForLoopUtils.updateLVarIndexes(ihy[item.getIdPrevLoad()].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
        InstructionHandle start = il.append(in2);
        il.append(new ILOAD(1));//set stop

        for (int i = item.getIdPrevLoad() + 2; i < item.getListaIfow().get(0); i++) {
            in2 = ForLoopUtils.updateLVarIndexes(ihy[i].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
            il.append(in2);
        }

//        tylko dla tego pierwszego ifa ustawiam taki warunek teraz
        BranchHandle if_01 = ForLoopUtils.getBranchHandleIF(il, ihy[item.getListaIfow().get(0)]);

        for (int j = item.getIdInsideLoop(); j < item.getIdInc(); j++) {
            Instruction temp = ForLoopUtils.updateLVarIndexes(ihy[j].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
            il.append(temp);
        }

        in2 = ForLoopUtils.updateLVarIndexes(ihy[item.getIdInc()].getInstruction(), hashmapIdOldAndNewLVar, mgNew, cg);
        InstructionHandle incr = il.append(in2);

        BranchInstruction goto_01 = InstructionFactory.createBranchInstruction(Const.GOTO, start);
        il.append(goto_01);


        InstructionHandle returnHandler = il.append(new PUSH(cp, 0));
        il.append(InstructionFactory.createReturn(Type.INT));

        if_01.setTarget(returnHandler);

        mgNew.setArgumentNames(new String[]{LaunchProperties.START_INDEX_VAR_NAME, LaunchProperties.END_INDEX_VAR_NAME});
        mgNew.setArgumentTypes(new Type[]{Type.INT, Type.INT});
        mgNew.setMaxLocals();
        mgNew.setMaxStack();
        cg.addMethod(mgNew.getMethod());
        cg.getConstantPool().addMethodref(mgNew);

    }

}
