package pl.edu.agh.bcel.transformation;


import org.apache.bcel.Const;
import org.apache.bcel.generic.*;
import pl.edu.agh.bcel.Label;
import pl.edu.agh.bcel.LaunchProperties;
import pl.edu.agh.bcel.nested.ElementFOR;
import pl.edu.agh.bcel.nested.ElementIF;
import pl.edu.agh.bcel.utils.ForLoopUtils;
import pl.edu.agh.bcel.utils.VariableUtils;

import java.util.ArrayList;

import static pl.edu.agh.bcel.Label.getListVariablesForSubTask;
import static pl.edu.agh.bcel.Label.getListVariablesToFinal;

public class FFT {
    static ArrayList<Label> labelsParamsSubTask;
    static ArrayList<Label> labelsVarInsideSubTask;
    static ArrayList<Label> labelsVarBetweenIntoFinal;

    static int iteratorToPass;


    public static void fftSubtask(ClassGen cg, MethodGen mgOld, ArrayList<ElementFOR> listElementsFOR) throws NullPointerException {

        InstructionHandle[] ihy = mgOld.getInstructionList().getInstructionHandles();
        InstructionList il = new InstructionList();
        ConstantPoolGen cp = cg.getConstantPool();

        MethodGen mgNew = new MethodGen(Const.ACC_PUBLIC | Const.ACC_STATIC, Type.INT,
                new Type[]{}, new String[]{}, LaunchProperties.SUBTASK_METHOD_NAME, cg.getClassName(), il, cp);

        ElementFOR elFor3 = listElementsFOR.get(3);
        ElementFOR elFor4 = listElementsFOR.get(4);
//        kolejnosc for to jest taka ze zewnetrzna petla jest wczesniej, tak to zostalo posortowane!
        int start = elFor3.getIdPrevStore();
        int end = elFor3.getIdGoTo();
        int position = ihy[elFor4.getIdPrevStore()].getPosition();
        iteratorToPass = elFor3.getidIterator();

        System.out.println("\n---- GET LIST VARIABLES TO SET IN SUBTASK-METHOD ----\n");
        labelsParamsSubTask = getListVariablesForSubTask(mgOld, position);
        Label.createVariablesFromLabels(labelsParamsSubTask, mgNew, cg.getConstantPool());

        System.out.println("\n---- SET PARAMS IN SUBTASK-METHOD ----\n");
        Label.setParamsInSubtskFromLabels(mgNew, labelsParamsSubTask);


        int idStart = listElementsFOR.get(2).getIdPrevLoad();
        int idStop = listElementsFOR.get(3).getIdPrevLoad();

        System.out.println("\n---- GET LIST VARIABLES TO SET WITH FINAL ----\n");
        labelsVarInsideSubTask = getListVariablesToFinal(mgOld, idStart, idStop);
        Label.createVariablesFromLabels(labelsVarInsideSubTask, mgNew, cg.getConstantPool());

//        IN THE FUTURE USE
//        ElementFOR.setNestedInArrayListElementFor(listElementsFOR);
//        displayTypeNestedFromArrayListElementFors(listElementsFOR);

        for (int id = start; id <= end; id++) {

            Instruction in = VariableUtils.replaceOldIdInInstruction(ihy[id], mgOld, mgNew);

            if (id == elFor3.getIdGoTo()) {
                BranchInstruction bh = InstructionFactory.createBranchInstruction(
                        Const.GOTO, elFor3.getInstructionHandlePrevLOAD());
                elFor3.setBranchInstructionGOTO(bh);
                il.append(bh);
            } else if (id == elFor4.getIdGoTo()) {
                BranchInstruction bh = InstructionFactory.createBranchInstruction(
                        Const.GOTO, elFor4.getInstructionHandlePrevLOAD());
                elFor4.setBranchInstructionGOTO(bh);
                il.append(bh);
            } else if (id == elFor3.getIdPrevLoad()) {
                InstructionHandle ihPrevLoad = il.append(in);
                elFor3.setInstructionHandlePrevLOAD(ihPrevLoad);
            } else if (id == elFor4.getIdPrevLoad()) {
                InstructionHandle ihPrevLoad = il.append(in);
                elFor4.setInstructionHandlePrevLOAD(ihPrevLoad);
            } else if (id == elFor3.getIdInc()) {
                InstructionHandle ihINC = il.append(in);
                elFor3.setInstructionHandleINC(ihINC);
            } else if (id == elFor4.getIdInc()) {
                InstructionHandle ihINC = il.append(in);
                elFor4.setInstructionHandleINC(ihINC);
            } else if (id == elFor3.getIdInsideLoop()) {
                InstructionHandle ihFirstInside = il.append(in);
                elFor3.setInstructionHandleFirstInside(ihFirstInside);
            } else if (id == elFor4.getIdInsideLoop()) {
                InstructionHandle ihFirstInside = il.append(in);
                elFor4.setInstructionHandleFirstInside(ihFirstInside);
            } else if (elFor3.getListWithIdInstructionIfInsideFor().contains(id)) {
                BranchHandle bh = ForLoopUtils.getBranchHandleIF(il, ihy[id].getInstruction());
                elFor3.addBranchHandleIfInForToArrayList(bh);
            } else if (elFor4.getListWithIdInstructionIfInsideFor().contains(id)) {
                BranchHandle bh = ForLoopUtils.getBranchHandleIF(il, ihy[id].getInstruction());
                elFor4.addBranchHandleIfInForToArrayList(bh);
            } else il.append(in);

        }

        InstructionHandle rh = il.append(new PUSH(cp, 0));
        il.append(InstructionFactory.createReturn(Type.INT));

        elFor4.getListWithBranchHandleIfInFor().get(0).setTarget(elFor3.getInstructionHandleINC());
        elFor3.getListWithBranchHandleIfInFor().get(0).setTarget(rh);

        mgNew.setMaxLocals();
        mgNew.setMaxStack();
        cg.addMethod(mgNew.getMethod());
        cg.getConstantPool().addMethodref(mgNew);

    }


    public static void fftMethod(ClassGen cg, MethodGen mgOld, ArrayList<ElementFOR> listElementsFOR, ArrayList<ElementIF> listElementsIF) throws NullPointerException, TargetLostException {

        System.out.println("labelsParamsSubTask: " + Label.displayNameFromArrayList(labelsParamsSubTask));
        System.out.println("labelsVarInsideSubTask: " + Label.displayNameFromArrayList(labelsVarInsideSubTask));

        InstructionHandle[] ihy = mgOld.getInstructionList().getInstructionHandles();
        InstructionList il = new InstructionList();

        ConstantPoolGen cp = cg.getConstantPool();
        InstructionFactory factory = new InstructionFactory(cg, cp);

        ElementFOR elFor0 = listElementsFOR.get(0);
        int start = elFor0.getIdPrevStore();
        int end = elFor0.getIdGoTo();

        for (int i = 0; i < elFor0.getIdPrevStore(); i++) {
            il.append(ihy[i].getInstruction());
        }

        for (int id = start; id <= end; id++) {

            Instruction in = ihy[id].getInstruction();

            if (id == elFor0.getIdGoTo()) {
                BranchInstruction bh = InstructionFactory.createBranchInstruction(
                        Const.GOTO, elFor0.getInstructionHandlePrevLOAD());
                elFor0.setBranchInstructionGOTO(bh);
                il.append(bh);
            } else if (id == elFor0.getIdPrevLoad()) {
                InstructionHandle ihPrevLoad = il.append(in);
                elFor0.setInstructionHandlePrevLOAD(ihPrevLoad);
            } else if (id == elFor0.getIdInc()) {
                InstructionHandle ihINC = il.append(in);
                elFor0.setInstructionHandleINC(ihINC);
            } else if (id == elFor0.getIdInsideLoop()) {
                InstructionHandle ihFirstInside = il.append(in);
                elFor0.setInstructionHandleFirstInside(ihFirstInside);
            } else if (elFor0.getListWithIdInstructionIfInsideFor().contains(id)) {
                BranchHandle bh = ForLoopUtils.getBranchHandleIF(il, ihy[id].getInstruction());
                elFor0.addBranchHandleIfInForToArrayList(bh);
            } else il.append(in);

        }

        InstructionHandle rh = il.append(ihy[elFor0.getIdGoTo() + 1].getInstruction());
        elFor0.getListWithBranchHandleIfInFor().get(0).setTarget(rh);

        ElementFOR elFor1 = listElementsFOR.get(1);
        start = elFor1.getIdPrevStore() + 1;//+1 poniewaz petla wczesniej "wziela te 1 instrukcje" za handler
        end = elFor1.getIdGoTo();

        for (int id = start; id <= end; id++) {

            Instruction in = ihy[id].getInstruction();

            if (id == elFor1.getIdGoTo()) {
                BranchInstruction bh = InstructionFactory.createBranchInstruction(
                        Const.GOTO, elFor1.getInstructionHandlePrevLOAD());
                elFor1.setBranchInstructionGOTO(bh);
                il.append(bh);
            } else if (id == elFor1.getIdPrevLoad()) {
                InstructionHandle ihPrevLoad = il.append(in);
                elFor1.setInstructionHandlePrevLOAD(ihPrevLoad);
            } else if (id == elFor1.getIdInc()) {
                InstructionHandle ihINC = il.append(in);
                elFor1.setInstructionHandleINC(ihINC);
            } else if (id == elFor1.getIdInsideLoop()) {
                InstructionHandle ihFirstInside = il.append(in);
                elFor1.setInstructionHandleFirstInside(ihFirstInside);
            } else if (elFor1.getListWithIdInstructionIfInsideFor().contains(id)) {
                BranchHandle bh = ForLoopUtils.getBranchHandleIF(il, ihy[id].getInstruction());
                elFor1.addBranchHandleIfInForToArrayList(bh);
            } else if (elFor1.getListWithIdInstructionIfInsideLoop().contains(id)) {
                BranchHandle bh = ForLoopUtils.getBranchHandleIF(il, ihy[id].getInstruction());
                elFor1.addBranchHandleIfInLoopToArrayList(bh);
            } else il.append(in);

        }

        InstructionHandle rh2 = il.append(ihy[elFor1.getIdGoTo() + 1].getInstruction());

        elFor1.getListWithBranchHandleIfInLoop().get(0).setTarget(elFor1.getInstructionHandleINC());
        elFor1.getListWithBranchHandleIfInFor().get(0).setTarget(rh2);

        ElementFOR elFor2 = listElementsFOR.get(2);
        int start2 = elFor2.getIdPrevStore() + 1;//+1 poniewaz petla wczesniej "wziela te 1 instrukcje" za handler
        int end2 = elFor2.getIdGoTo();

        ElementFOR elFor3 = listElementsFOR.get(3);
        int start3 = elFor3.getIdPrevStore();//+1 poniewaz petla wczesniej "wziela te 1 instrukcje" za handler
        int end3 = elFor3.getIdGoTo();

        ElementFOR elFor4 = listElementsFOR.get(4);
        int start4 = elFor4.getIdPrevStore();//+1 poniewaz petla wczesniej "wziela te 1 instrukcje" za handler
        int end4 = elFor4.getIdGoTo();


        for (int id = start2; id < start3; id++) {

            Instruction in = ihy[id].getInstruction();

            if (id == elFor2.getIdPrevLoad()) {
                InstructionHandle ihPrevLoad = il.append(in);
                elFor2.setInstructionHandlePrevLOAD(ihPrevLoad);
            } else if (id == elFor2.getIdInsideLoop()) {
                InstructionHandle ihFirstInside = il.append(in);
                elFor2.setInstructionHandleFirstInside(ihFirstInside);
            } else if (elFor2.getListWithIdInstructionIfInsideFor().contains(id)) {
                BranchHandle bh = ForLoopUtils.getBranchHandleIF(il, ihy[id].getInstruction());
                elFor2.addBranchHandleIfInForToArrayList(bh);
            } else if (elFor2.getListWithIdInstructionIfInsideLoop().contains(id)) {
                BranchHandle bh = ForLoopUtils.getBranchHandleIF(il, ihy[id].getInstruction());
                elFor2.addBranchHandleIfInLoopToArrayList(bh);
            } else il.append(in);

        }

//        czasami jesli nazwana zmienna nie jest uzyta to "zmienia sie" na varNNNN
        il.append(factory.createFieldAccess("java.lang.System", "out", new ObjectType("java.io.PrintStream"), Const.GETSTATIC));
        il.append(new ILOAD(9));
        il.append(factory.createInvoke("java.io.PrintStream", "println", Type.VOID, new Type[]{Type.INT}, Const.INVOKEVIRTUAL));


//        dodano do srodka

        for (int i = elFor2.getListWithIdInstructionIfInsideFor().get(0) - 1; i >= elFor2.getIdPrevLoad(); i--) {
            il.append(ihy[i].getInstruction());
        }
        il.append(new IDIV());

        il.append(factory.createGetStatic(mgOld.getClassName(), "THRESHOLD", Type.INT));
        BranchInstruction ih_if_inside = InstructionFactory.createBranchInstruction(Const.IF_ICMPGE, null);
        il.append(ih_if_inside);
        il.append(factory.createFieldAccess("java.lang.System", "out", new ObjectType("java.io.PrintStream"), Const.GETSTATIC));
        il.append(new PUSH(cp, "true"));
        il.append(factory.createInvoke("java.io.PrintStream", "println", Type.VOID, new Type[]{Type.STRING}, Const.INVOKEVIRTUAL));
        BranchInstruction bh_goto_after_else = InstructionFactory.createBranchInstruction(Const.GOTO, null);
        il.append(bh_goto_after_else);
        InstructionHandle ih_else_inside = il.append(factory.createFieldAccess("java.lang.System", "out", new ObjectType("java.io.PrintStream"), Const.GETSTATIC));
        il.append(new PUSH(cp, "false"));
        il.append(factory.createInvoke("java.io.PrintStream", "println", Type.VOID, new Type[]{Type.STRING}, Const.INVOKEVIRTUAL));


        for (int id = elFor3.getIdPrevStore(); id < elFor3.getIdInsideLoop(); id++) {

            Instruction in = ihy[id].getInstruction();

            if (id == elFor3.getIdPrevLoad()) {
                InstructionHandle ihPrevLoad = il.append(in);
                elFor3.setInstructionHandlePrevLOAD(ihPrevLoad);
            } else if (id == elFor3.getIdInsideLoop()) {
                InstructionHandle ihFirstInside = il.append(in);
                elFor3.setInstructionHandleFirstInside(ihFirstInside);
            } else if (elFor3.getListWithIdInstructionIfInsideFor().contains(id)) {
                BranchInstruction bin = (BranchInstruction) ihy[id].getInstruction();
                elFor3.listWithBranchInstruction2.add(bin);
                il.append(bin);
            } else if (elFor3.getListWithIdInstructionIfInsideLoop().contains(id)) {
                BranchInstruction bin = (BranchInstruction) ihy[id].getInstruction();
                elFor3.listWithBranchInstructionIfInLoop2.add(bin);
                il.append(bin);
            } else il.append(in);

        }

//        BranchHandle bheee =ForLoopUtils.getBranchHandleIF(il, ihy[elFor3.getListWithIdInstructionIfInsideFor().get(0)].getInstruction());// il.append(new IF_ICMPEQ(null));

        int idSize = VariableUtils.getLVarIdByName("size", mgOld);
        int idHalfSize = VariableUtils.getLVarIdByName("halfSize", mgOld);
        int idI = 10;

//        trzeba wyciagnac liste zmiennych z position: <= >= i dac final
        LocalVariableGen varFinalStartI = mgOld.addLocalVariable("finaliStart", Type.INT, null, null);
        LocalVariableGen varFinalStopI = mgOld.addLocalVariable("finaliStop", Type.INT, null, null);
        LocalVariableGen varFinalHalfSize = mgOld.addLocalVariable("finalhalfSize", Type.INT, null, null);
        LocalVariableGen varFinalSize = mgOld.addLocalVariable("finalSize", Type.INT, null, null);


        il.append(new ILOAD(idI));
        il.append(new F2I());
        il.append(new ISTORE(varFinalStartI.getIndex()));


        il.append(new ILOAD(idI));
        il.append(factory.createGetStatic(mgOld.getClassName(), "THRESHOLD", Type.INT));
        il.append(new IADD());
//        il.append(new D2I());
        il.append(InstructionFactory.createStore(Type.INT, varFinalStopI.getIndex()));

        il.append(new ILOAD(idSize));
        il.append(new F2I());
        il.append(new ISTORE(varFinalSize.getIndex()));

        il.append(new ILOAD(idHalfSize));
        il.append(new F2I());
        il.append(new ISTORE(varFinalHalfSize.getIndex()));


        il.append(factory.createFieldAccess("java.lang.System", "out", new ObjectType("java.io.PrintStream"), Const.GETSTATIC));
        il.append(new ILOAD(varFinalStopI.getIndex()));
        il.append(factory.createInvoke("java.io.PrintStream", "println", Type.VOID, new Type[]{Type.INT}, Const.INVOKEVIRTUAL));


        String iteratorName = "i";
        il.append(InstructionFactory.createLoad(Type.OBJECT, LaunchProperties.TASK_POOL_ID));

        String signatureCallable = "Callable() {" +
                "@Override public Integer call() throws Exception { return " +
                LaunchProperties.SUBTASK_METHOD_NAME + "(";
        for (int i = 0; i < labelsParamsSubTask.size(); i++) {
            signatureCallable += labelsParamsSubTask.get(i).getName();
            if (i < labelsParamsSubTask.size() - 1) signatureCallable += ",";
        }
        signatureCallable += ");}}";

        il.append(factory.createNew(new ObjectType(signatureCallable)));
        il.append(factory.createInvoke("java.util.List", "add", Type.BOOLEAN, new Type[]{Type.OBJECT}, Const.INVOKEINTERFACE));


        for (int id = elFor3.getIdInc(); id <= elFor3.getIdGoTo(); id++) {
            Instruction in = ihy[id].getInstruction();
            if (id == elFor3.getIdInc() + 1) {
                il.append(factory.createGetStatic(mgOld.getClassName(), "THRESHOLD", Type.INT));
            } else if (id == elFor3.getIdGoTo()) {
                BranchInstruction bh = InstructionFactory.createBranchInstruction(
                        Const.GOTO, elFor3.getInstructionHandlePrevLOAD());
                elFor3.setBranchInstructionGOTO(bh);
                il.append(bh);
            } else if (id == elFor3.getIdInc()) {

                InstructionHandle ihINC = il.append(in);
                elFor3.setInstructionHandleINC(ihINC);

            } else il.append(in);
        }


//        koniec dodawanai do srodka


        for (int id = elFor2.getIdInc(); id <= elFor2.getIdGoTo(); id++) {
            Instruction in = ihy[id].getInstruction();

            if (id == elFor2.getIdGoTo()) {
                BranchInstruction bh = InstructionFactory.createBranchInstruction(
                        Const.GOTO, elFor2.getInstructionHandlePrevLOAD());
                elFor2.setBranchInstructionGOTO(bh);
                il.append(bh);
            } else if (id == elFor2.getIdInc()) {
                InstructionHandle ihINC = il.append(in);
                elFor2.setInstructionHandleINC(ihINC);
            } else il.append(in);
        }


        InstructionHandle startTry = il.append(factory.createFieldAccess(
                cg.getClassName(), "SERVICE",
                new ObjectType("java.util.concurrent.ExecutorService"), Const.GETSTATIC));

        il.append(InstructionFactory.createLoad(Type.OBJECT, 55));
        il.append(factory.createInvoke("java.util.concurrent.ExecutorService", "invokeAll", new ObjectType("java.util.List"), new Type[]{new ObjectType("java.util.Collection")}, Const.INVOKEINTERFACE));
        il.append(InstructionConst.POP);


        il.append(factory.createFieldAccess(
                cg.getClassName(), "SERVICE",
                new ObjectType("java.util.concurrent.ExecutorService"), Const.GETSTATIC));

        InstructionHandle endTry = il.append(factory.createInvoke(
                "java.util.concurrent.ExecutorService", "shutdown",
                Type.VOID, Type.NO_ARGS, Const.INVOKEINTERFACE));

        BranchInstruction gotoNext = InstructionFactory.createBranchInstruction(Const.GOTO, null);
        il.append(gotoNext);

        InstructionHandle startCatch = il.append(InstructionFactory.createStore(Type.OBJECT, mgOld.getMaxLocals()));

        il.append(InstructionFactory.createLoad(Type.OBJECT, mgOld.getMaxLocals()));
        il.append(factory.createInvoke(
                "java.lang.Exception", "printStackTrace",
                Type.VOID, Type.NO_ARGS, Const.INVOKEVIRTUAL));


        InstructionHandle rh4 = il.append(InstructionFactory.createReturn(Type.VOID));
        elFor2.getListWithBranchHandleIfInFor().get(0).setTarget(rh4);

        ih_if_inside.setTarget(ih_else_inside);
        bh_goto_after_else.setTarget(elFor2.getInstructionHandleINC());
        elFor3.listWithBranchInstruction2.get(0).setTarget(startTry);

        gotoNext.setTarget(elFor2.getInstructionHandleINC());

        mgOld.addExceptionHandler(startTry, endTry, startCatch, new ObjectType("java.lang.Exception"));

        mgOld.setInstructionList(il);
        mgOld.setMaxLocals();
        mgOld.setMaxStack();
        cg.replaceMethod(mgOld.getMethod(), mgOld.getMethod());

    }

}
