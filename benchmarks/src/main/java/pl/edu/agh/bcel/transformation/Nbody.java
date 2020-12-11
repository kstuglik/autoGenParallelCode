package pl.edu.agh.bcel.transformation;

import org.apache.bcel.Const;
import org.apache.bcel.generic.*;
import pl.edu.agh.bcel.Label;
import pl.edu.agh.bcel.LaunchProperties;
import pl.edu.agh.bcel.nested.ElementFOR;
import pl.edu.agh.bcel.utils.ForLoopUtils;
import pl.edu.agh.bcel.utils.VariableUtils;

import java.util.ArrayList;
import java.util.HashMap;

import static pl.edu.agh.bcel.Label.getListVariablesForSubTask;
import static pl.edu.agh.bcel.Label.getListVariablesToFinal;

//  EXAMPLE OF A TASK WITH 1 LOOP

public class Nbody {

    static ArrayList<Label> labelsParamsSubTask;
    static ArrayList<Label> labelsVarInsideSubTask;

    static int iteratorToPass;

    public static void nbodyMovies(ClassGen cg, MethodGen mgOld, ArrayList<ElementFOR> listElementsFOR, InstructionHandle[] ihy) {

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
        ElementFOR elementFor = listElementsFOR.get(0);
        ArrayList<BranchHandle> listaIFinFOR = new ArrayList<>();
        ArrayList<BranchInstruction> listaGOTO = new ArrayList<>();
        ArrayList<InstructionHandle> listaSTART = new ArrayList<>();
        //      *****************************************************************************************


        int ile = elementFor.getListWithIdInstructionIfInsideFor().size();
        int idOstatniegoIfaWPetli = elementFor.getListWithIdInstructionIfInsideFor().get(ile - 1);

        int start = listElementsFOR.get(0).getIdInsideLoop();
        int inc = listElementsFOR.get(0).getIdInc() - 1;

        for (int j = 0; j < elementFor.getIdPrevLoad(); j++) il.append(ihy[j].getInstruction());

        int numThreadsFieldIndex = VariableUtils.getFieldRefId(cg, LaunchProperties.NUMBER_OF_THREADS_NAME);

        int loopIteratorIndex = ForLoopUtils.getIdLoadOrStoreFromInstruction(ihy[elementFor.getIdPrevLoad() - 1].getInstruction());
        int dataSizeIndex = VariableUtils.getFieldIdByName("dataSize", cg);//zmienic na wartosc z LaunchPropeties

//        zastanowic sie nad tym jak ogarniac te podmiane...
        InstructionHandle prevLoad = il.append(ihy[elementFor.getIdPrevLoad()].getInstruction());
        il.append(factory.createFieldAccess(cg.getClassName(), "NUM_THREADS", Type.INT, Const.GETSTATIC));

        BranchHandle if_0 = ForLoopUtils.getBranchHandleIF(il, ihy[elementFor.getListWithIdInstructionIfInsideFor().get(0)].getInstruction());

        LocalVariableGen l1 = mgNew.addLocalVariable(LaunchProperties.START_CONDITION_NAME, Type.INT, null, null);
        LocalVariableGen l2 = mgNew.addLocalVariable(LaunchProperties.STOP_CONDITION_NAME, Type.INT, null, null);

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
                LaunchProperties.START_CONDITION_NAME + "," + LaunchProperties.STOP_CONDITION_NAME +
                ");}}"
        )));

        il.append(factory.createInvoke("java.util.List", "add", Type.BOOLEAN, new Type[]{Type.OBJECT}, Const.INVOKEINTERFACE));

        InstructionHandle next = il.append(ihy[elementFor.getIdInc()].getInstruction());
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

    public static void nbodySubtask(ClassGen cg, MethodGen mgOld, ArrayList<ElementFOR> listElementsFOR) {

        InstructionHandle[] ihy = mgOld.getInstructionList().getInstructionHandles();
        InstructionList il = new InstructionList();
        ConstantPoolGen cp = cg.getConstantPool();

        MethodGen mgNew = new MethodGen(Const.ACC_PUBLIC | Const.ACC_STATIC, Type.INT,
                new Type[]{}, new String[]{}, LaunchProperties.SUBTASK_METHOD_NAME, cg.getClassName(), il, cp);


        ElementFOR elementFor = listElementsFOR.get(0);
        int start = elementFor.getIdPrevLoad()-1;
        int end = elementFor.getIdGoTo();

        System.out.println("\n---- GET LIST VARIABLES TO SET IN SUBTASK-METHOD ----\n");
        labelsParamsSubTask = getListVariablesForSubTask(mgOld, start);
        Label.createVariablesFromLabels(labelsParamsSubTask, mgNew, cg.getConstantPool());

        System.out.println("\n---- SET PARAMS IN SUBTASK-METHOD ----\n");
        Label.setParamsInSubtskFromLabels(mgNew, labelsParamsSubTask);


        System.out.println("\n---- GET LIST VARIABLES TO SET WITH FINAL ----\n");
        labelsVarInsideSubTask = getListVariablesToFinal(mgOld, start, end);
        Label.createVariablesFromLabels(labelsVarInsideSubTask, mgNew, cg.getConstantPool());


        il.append(new ILOAD(0));//iload(finaliStart)
        for (int id = start; id <= end; id++) {

            Instruction in = VariableUtils.replaceOldIdInInstruction(ihy[id], mgOld, mgNew);

            if (id == elementFor.getIdGoTo()) {
                BranchInstruction bh = InstructionFactory.createBranchInstruction(
                        Const.GOTO, elementFor.getInstructionHandlePrevLOAD());
                elementFor.setBranchInstructionGOTO(bh);
                il.append(bh);
            } else if(id == elementFor.getListWithIdInstructionIfInsideFor().get(0)-1) {
                il.append(new ILOAD(1));
            }
            else if (id == elementFor.getIdPrevLoad()) {
                InstructionHandle ihPrevLoad = il.append(in);
                elementFor.setInstructionHandlePrevLOAD(ihPrevLoad);
            } else if (id == elementFor.getIdInc()) {
                InstructionHandle ihINC = il.append(in);
                elementFor.setInstructionHandleINC(ihINC);
            } else if (id == elementFor.getIdInsideLoop()) {
                InstructionHandle ihFirstInside = il.append(in);
                elementFor.setInstructionHandleFirstInside(ihFirstInside);
            } else if (elementFor.getListWithIdInstructionIfInsideFor().contains(id)) {
                BranchHandle bh = ForLoopUtils.getBranchHandleIF(il, ihy[id].getInstruction());
                elementFor.addBranchHandleIfInForToArrayList(bh);
            } else il.append(in);

        }


        InstructionHandle returnHandler = il.append(new PUSH(cp, 0));
        il.append(InstructionFactory.createReturn(Type.INT));
        elementFor.getListWithBranchHandleIfInFor().get(0).setTarget(returnHandler);


        mgNew.setMaxLocals();
        mgNew.setMaxStack();
        cg.addMethod(mgNew.getMethod());
        cg.getConstantPool().addMethodref(mgNew);

    }

}
