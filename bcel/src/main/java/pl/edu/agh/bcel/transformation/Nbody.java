package pl.edu.agh.bcel.transformation;

import org.apache.bcel.Const;
import org.apache.bcel.generic.*;
import pl.edu.agh.bcel.LaunchProperties;
import pl.edu.agh.bcel.nested.ElementFOR;
import pl.edu.agh.bcel.utils.ForLoopUtils;
import pl.edu.agh.bcel.utils.VariableUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class Nbody {

    public static void nbodyMovies(ClassGen cg, MethodGen mgOld, ArrayList<ElementFOR> listaFORow, InstructionHandle[] ihy) {

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
        ElementFOR item = listaFORow.get(0);
        ArrayList<BranchHandle> listaIFinFOR = new ArrayList<>();
        ArrayList<BranchInstruction> listaGOTO = new ArrayList<>();
        ArrayList<InstructionHandle> listaSTART = new ArrayList<>();
        //      *****************************************************************************************


        int ile = item.getListaIfow().size();
        int idOstatniegoIfaWPetli = item.getListaIfow().get(ile - 1);

        int start = listaFORow.get(0).getIdInsideLoop();
        int inc = listaFORow.get(0).getIdInc() - 1;

        for (int j = 0; j < item.getIdPrevLoad(); j++) il.append(ihy[j].getInstruction());

        int numThreadsFieldIndex = VariableUtils.getFieldRefId(cg, LaunchProperties.NUMBER_OF_THREADS_NAME);

        int loopIteratorIndex = ForLoopUtils.getIdLoadOrStoreFromInstruction(ihy[item.getIdPrevLoad() - 1].getInstruction());
        int dataSizeIndex = VariableUtils.getFieldIdByName("dataSize", cg);//zmienic na wartosc z LaunchPropeties

//        zastanowic sie nad tym jak ogarniac te podmiane...
        InstructionHandle prevLoad = il.append(ihy[item.getIdPrevLoad()].getInstruction());
        il.append(factory.createFieldAccess(cg.getClassName(), "NUM_THREADS", Type.INT, Const.GETSTATIC));

        BranchHandle if_0 = ForLoopUtils.getBranchHandleIF(il, ihy[item.getListaIfow().get(0)].getInstruction());

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

    public static void nbodySubtask(ClassGen cg, MethodGen mgOld, ArrayList<ElementFOR> listaFORow, InstructionHandle[] ihy) {
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
        ElementFOR item = listaFORow.get(0);
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

//        BranchHandle if_01 = ForLoopUtils.getBranchHandleIF(il, ihy[item.getListaIfow().get(0)]);
//        mozna dodac w przyszlosci obsluge, tzn teraz: przed <, po <=, a co będzie dla > ?
        BranchHandle if_01 = il.append(new IF_ICMPGT(null));


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
