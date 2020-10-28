package pl.edu.agh.bcel.transformation;


import org.apache.bcel.Const;
import org.apache.bcel.generic.*;
import pl.edu.agh.bcel.Label;
import pl.edu.agh.bcel.LaunchProperties;
import pl.edu.agh.bcel.nested.ElementFOR;
import pl.edu.agh.bcel.utils.ForLoopUtils;
import pl.edu.agh.bcel.utils.VariableUtils;

import java.util.ArrayList;

import static pl.edu.agh.bcel.Label.getListVariablesForSubTask;

public class FFT {

    public static void fftSubtask(ClassGen cg, MethodGen mgOld, ArrayList<ElementFOR> listElementsFOR) throws NullPointerException {

        InstructionHandle[] ihy = mgOld.getInstructionList().getInstructionHandles();
        InstructionList il = new InstructionList();
        ConstantPoolGen cp = cg.getConstantPool();
        InstructionFactory factory = new InstructionFactory(cg, cp);

        MethodGen mgNew = new MethodGen(Const.ACC_PUBLIC | Const.ACC_STATIC, Type.INT,
                new Type[]{}, new String[]{}, LaunchProperties.SUBTASK_METHOD_NAME, cg.getClassName(), il, cp);

        System.out.println("wszystkich elementow for jest: " + listElementsFOR.size() + "\n");

        ElementFOR elFor1 = listElementsFOR.get(3);
        ElementFOR elFor2 = listElementsFOR.get(4);
//        kolejnosc for to jest taka ze zewnetrzna petla jest wczesniej, tak to zostalo posortowane!
        int start = elFor1.getIdPrevStore();
        int end = elFor1.getIdGoTo();
        int position = ihy[elFor2.getIdPrevStore()].getPosition();

        System.out.println("\n---- GET LIST VARIABLES TO SET IN SUBTASK-METHOD ----\n");
        ArrayList<Label> labels = getListVariablesForSubTask(mgOld, position, "<");
        Label.createVariablesFromLabels(labels, mgNew, false, cg.getConstantPool());
        System.out.println("\n---- SET PARAMS IN SUBTASK-METHOD ----\n");
        Label.setParamsInSubtskFromLabels(mgNew, labels);
        System.out.println("\n---- GET LIST VARIABLES TO SET IN INSIDE SUBTASK-METHOD ----\n");
        ArrayList<Label> labels2 = getListVariablesForSubTask(mgOld, position, ">=");
        Label.createVariablesFromLabels(labels2, mgNew, false, cg.getConstantPool());

//        IN THE FUTURE USE
//        ElementFOR.setNestedInArrayListElementFor(listElementsFOR);
//        displayTypeNestedFromArrayListElementFors(listElementsFOR);

        for (int id = start; id <= end; id++) {

            Instruction in = VariableUtils.replaceOldIdInInstruction(ihy[id], mgOld, mgNew);

            if (id == elFor1.getIdGoTo()) {
                BranchInstruction bh = InstructionFactory.createBranchInstruction(
                        Const.GOTO, elFor1.getInstructionHandlePrevLOAD());
                elFor1.setBranchInstructionGOTO(bh);
                il.append(bh);
            } else if (id == elFor2.getIdGoTo()) {
                BranchInstruction bh = InstructionFactory.createBranchInstruction(
                        Const.GOTO, elFor2.getInstructionHandlePrevLOAD());
                elFor2.setBranchInstructionGOTO(bh);
                il.append(bh);
            } else if (id == elFor1.getIdPrevLoad()) {
                InstructionHandle ihPrevLoad = il.append(in);
                elFor1.setInstructionHandlePrevLOAD(ihPrevLoad);
            } else if (id == elFor2.getIdPrevLoad()) {
                InstructionHandle ihPrevLoad = il.append(in);
                elFor2.setInstructionHandlePrevLOAD(ihPrevLoad);
            } else if (id == elFor1.getIdInc()) {
                InstructionHandle ihINC = il.append(in);
                elFor1.setInstructionHandleINC(ihINC);
            } else if (id == elFor2.getIdInc()) {
                InstructionHandle ihINC = il.append(in);
                elFor2.setInstructionHandleINC(ihINC);
            } else if (id == elFor1.getIdInsideLoop()) {
                InstructionHandle ihFirstInside = il.append(in);
                elFor1.setInstructionHandleFirstInside(ihFirstInside);
            } else if (id == elFor2.getIdInsideLoop()) {
                InstructionHandle ihFirstInside = il.append(in);
                elFor2.setInstructionHandleFirstInside(ihFirstInside);
            } else if (elFor1.getListWithIdInstructionIfInsideFor().contains(id)) {
                BranchHandle bh = ForLoopUtils.getBranchHandleIF(il, ihy[id].getInstruction());
                elFor1.addBranchHandleIfInForToArrayList(bh);
            } else if (elFor2.getListWithIdInstructionIfInsideFor().contains(id)) {
                BranchHandle bh = ForLoopUtils.getBranchHandleIF(il, ihy[id].getInstruction());
                elFor2.addBranchHandleIfInForToArrayList(bh);
            } else il.append(in);

        }

        InstructionHandle rh = il.append(new PUSH(cp, 0));
        il.append(InstructionFactory.createReturn(Type.INT));

        elFor2.getListWithBranchHandleIfInFor().get(0).setTarget(elFor1.getInstructionHandleINC());
        elFor1.getListWithBranchHandleIfInFor().get(0).setTarget(rh);

        mgNew.setMaxLocals();
        mgNew.setMaxStack();
        cg.addMethod(mgNew.getMethod());
        cg.getConstantPool().addMethodref(mgNew);

    }

    public static void fftMethod(ClassGen cg, MethodGen mgOld, ArrayList<ElementFOR> listElementsFOR) throws NullPointerException, TargetLostException {

        InstructionHandle[] ihy = mgOld.getInstructionList().getInstructionHandles();
        InstructionList il = new InstructionList();

        ConstantPoolGen cp = cg.getConstantPool();
        InstructionFactory factory = new InstructionFactory(cg, cp);

        System.out.println("wszystkich elementow for jest: " + listElementsFOR.size() + "\n");

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
        start = elFor0.getIdPrevStore();
        end = elFor0.getIdGoTo();

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
            } else {
                il.append(in);
            }

        }

        InstructionHandle rh2 = il.append(InstructionFactory.createReturn(Type.VOID));
        elFor0.getListWithBranchHandleIfInFor().get(0).setTarget(rh2);



        mgOld.setInstructionList(il);
        mgOld.setMaxLocals();
        mgOld.setMaxStack();
        cg.replaceMethod(mgOld.getMethod(), mgOld.getMethod());
        cg.getConstantPool().addMethodref(mgOld);

    }

}
