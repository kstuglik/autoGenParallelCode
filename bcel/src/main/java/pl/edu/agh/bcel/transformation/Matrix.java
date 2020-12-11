package pl.edu.agh.bcel.transformation;

import org.apache.bcel.Const;
import org.apache.bcel.generic.*;
import pl.edu.agh.bcel.Label;
import pl.edu.agh.bcel.LaunchProperties;
import pl.edu.agh.bcel.nested.ElementFOR;
import pl.edu.agh.bcel.utils.ForLoopUtils;
import pl.edu.agh.bcel.utils.ReadyFields;
import pl.edu.agh.bcel.utils.VariableUtils;

import java.util.ArrayList;

public class Matrix {

    static ArrayList<Label> labelsParamsSubTask;
    static ArrayList<Label> labelsVarInsideSubTask;


    public static void matrixMultiply(ClassGen cg, MethodGen mgOld, ArrayList<ElementFOR> listElementsFOR) {

        InstructionHandle[] ihy = mgOld.getInstructionList().getInstructionHandles();
        InstructionList il = new InstructionList();
        ConstantPoolGen cp = cg.getConstantPool();

        MethodGen mgNew = new MethodGen(mgOld.getAccessFlags(), mgOld.getType(),
                mgOld.getArgumentTypes(), mgOld.getArgumentNames(),
                mgOld.getName(), cg.getClassName(), il, cp);

        InstructionFactory factory = new InstructionFactory(cg, mgNew.getConstantPool());

        ReadyFields.addFieldStepToIL(il, mgNew, factory);
        ReadyFields.addFieldTaskPoolToIL(cg, il, factory);
        ReadyFields.initFieldExecutorServiceToIL(cg, il, factory);


        System.out.println("count of for:" + listElementsFOR.size());

        ElementFOR elFor0 = listElementsFOR.get(0);
        ElementFOR elFor1 = listElementsFOR.get(1);
        ElementFOR elFor2 = listElementsFOR.get(2);

        int position = ihy[elFor2.getIdPrevStore()].getPosition();

        System.out.println("\n---- GET LIST VARIABLES TO SET IN SUBTASK-METHOD ----\n");
        labelsParamsSubTask = Label.getListVariablesForSubTask(mgOld, position);
        Label.createVariablesFromLabels(labelsParamsSubTask, mgNew, cg.getConstantPool());

        System.out.println("\n---- SET PARAMS IN SUBTASK-METHOD ----\n");
        Label.setParamsInSubtskFromLabels(mgNew, labelsParamsSubTask);

        System.out.println("\n---- GET LIST VARIABLES TO SET WITH FINAL ----\n");
        labelsVarInsideSubTask = Label.getListVariablesToFinal(mgOld, elFor0.getIdPrevStore(), elFor0.getIdInc());
        Label.createVariablesFromLabels(labelsVarInsideSubTask, mgNew, cg.getConstantPool());

//        IN THE FUTURE USE
//        ElementFOR.setNestedInArrayListElementFor(listElementsFOR);
//        displayTypeNestedFromArrayListElementFors(listElementsFOR);
        int length = elFor0.getListWithIdInstructionIfInsideFor().size();
        int last = elFor0.getListWithIdInstructionIfInsideFor().get(length - 1);

        il.append(new ICONST(0));
        il.append(new ISTORE(2));
        InstructionHandle prevLoad = il.append(new PUSH(cp, 10));
        il.append(new ILOAD(2));
        BranchHandle bh0 = il.append(new IF_ICMPEQ(null));
        il.append(factory.createPrintln("Hello world"));
        il.append(new IINC(2, 1));
        il.append(new GOTO(prevLoad));

        InstructionHandle rh = il.append(new PUSH(cp, 10));
        il.append(factory.createNewArray(Type.INT, (short) 1));
        il.append(InstructionFactory.createReturn(Type.OBJECT));

        bh0.setTarget(rh);

        mgNew.setInstructionList(il);
        mgNew.setMaxLocals();
        mgNew.setMaxStack();
        cg.replaceMethod(mgOld.getMethod(), mgNew.getMethod());
    }


    public static void matrixSubtask(ClassGen cg, MethodGen mgOld, ArrayList<ElementFOR> listElementsFOR) {

        InstructionHandle[] ihy = mgOld.getInstructionList().getInstructionHandles();
        InstructionList il = new InstructionList();
        ConstantPoolGen cp = cg.getConstantPool();

        MethodGen mgNew = new MethodGen(Const.ACC_PUBLIC | Const.ACC_STATIC, Type.INT,
                new Type[]{}, new String[]{}, LaunchProperties.SUBTASK_METHOD_NAME, cg.getClassName(), il, cp);


        ElementFOR elFor_0 = listElementsFOR.get(0);
        ElementFOR elFor_1 = listElementsFOR.get(2);

        int position = ihy[elFor_1.getIdPrevStore()].getPosition();

        ArrayList<Integer> listWithIdRange = new ArrayList<>();
        Structure.setArrayListWithRange(listWithIdRange, listElementsFOR, 0, 2);

        System.out.println("\n---- GET LIST VARIABLES TO SET IN SUBTASK-METHOD ----\n");
        labelsParamsSubTask = Label.getListVariablesForSubTask(mgOld, position);
        Label.createVariablesFromLabels(labelsParamsSubTask, mgNew, cg.getConstantPool());

        System.out.println("\n---- SET PARAMS IN SUBTASK-METHOD ----\n");
        Label.setParamsInSubtskFromLabels(mgNew, labelsParamsSubTask);

        System.out.println("\n---- GET LIST VARIABLES TO SET WITH FINAL ----\n");
        labelsVarInsideSubTask = Label.getListVariablesToFinal(mgOld, elFor_0.getIdPrevStore(), elFor_1.getIdPrevStore());
        Label.createVariablesFromLabels(labelsVarInsideSubTask, mgNew, cg.getConstantPool());

//        IN THE FUTURE USE
//        ElementFOR.setNestedInArrayListElementFor(listElementsFOR);
//        displayTypeNestedFromArrayListElementFors(listElementsFOR);


        for (int i = 0; i < listWithIdRange.size(); i += 2) {
//            System.out.println("petla zewnetrzna i = " + i);
            for (int ii = listWithIdRange.get(i); ii < listWithIdRange.get(i + 1); ii++) {

                Instruction in = VariableUtils.replaceOldIdInInstruction(ihy[ii], mgOld, mgNew);
//                System.out.println("\tpetla wewnetrzna ii = " + ii + ", " + in);

                if (ii == elFor_0.getIdGoTo()) {
                    BranchInstruction bh = InstructionFactory.createBranchInstruction(
                            Const.GOTO, elFor_0.getInstructionHandlePrevLOAD());
                    elFor_0.setBranchInstructionGOTO(bh);
                    il.append(bh);
                } else if (ii == elFor_1.getIdGoTo()) {
                    BranchInstruction bh = InstructionFactory.createBranchInstruction(
                            Const.GOTO, elFor_1.getInstructionHandlePrevLOAD());
                    elFor_1.setBranchInstructionGOTO(bh);
                    il.append(bh);
                } else if (ii == elFor_0.getIdPrevLoad()) {
                    InstructionHandle ihPrevLoad = il.append(in);
                    elFor_0.setInstructionHandlePrevLOAD(ihPrevLoad);
                } else if (ii == elFor_1.getIdPrevLoad()) {
                    InstructionHandle ihPrevLoad = il.append(in);
                    elFor_1.setInstructionHandlePrevLOAD(ihPrevLoad);
                } else if (ii == elFor_0.getIdInc()) {
                    InstructionHandle ihINC = il.append(in);
                    elFor_0.setInstructionHandleINC(ihINC);
                } else if (ii == elFor_1.getIdInc()) {
                    InstructionHandle ihINC = il.append(in);
                    elFor_1.setInstructionHandleINC(ihINC);
                } else if (ii == elFor_0.getIdInsideLoop()) {
                    InstructionHandle ihFirstInside = il.append(in);
                    elFor_0.setInstructionHandleFirstInside(ihFirstInside);
                } else if (ii == elFor_1.getIdInsideLoop()) {
                    InstructionHandle ihFirstInside = il.append(in);
                    elFor_1.setInstructionHandleFirstInside(ihFirstInside);
                } else if (elFor_0.getListWithIdInstructionIfInsideFor().contains(ii)) {
                    BranchHandle bh = ForLoopUtils.getBranchHandleIF(il, ihy[ii].getInstruction());
                    elFor_0.addBranchHandleIfInForToArrayList(bh);
                } else if (elFor_1.getListWithIdInstructionIfInsideFor().contains(ii)) {
                    BranchHandle bh = ForLoopUtils.getBranchHandleIF(il, ihy[ii].getInstruction());
                    elFor_1.addBranchHandleIfInForToArrayList(bh);
                } else il.append(in);

            }
        }

        InstructionHandle rh = il.append(new PUSH(cp, 0));
        il.append(InstructionFactory.createReturn(Type.INT));


        elFor_1.getListWithBranchHandleIfInFor().get(0).setTarget(elFor_0.getInstructionHandleINC());
        elFor_0.getListWithBranchHandleIfInFor().get(0).setTarget(rh);

        mgNew.setMaxLocals();
        mgNew.setMaxStack();
        cg.addMethod(mgNew.getMethod());
        cg.getConstantPool().addMethodref(mgNew);

    }

}
