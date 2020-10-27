package pl.edu.agh.bcel.transformation;


import org.apache.bcel.Const;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.generic.*;
import pl.edu.agh.bcel.Label;
import pl.edu.agh.bcel.LaunchProperties;
import pl.edu.agh.bcel.nested.ElementFOR;
import pl.edu.agh.bcel.utils.ForLoopUtils;
import pl.edu.agh.bcel.utils.VariableUtils;

import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.Integer.parseInt;

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
        createVariablesFromLabels(labels, mgNew, false, cg.getConstantPool());
        System.out.println("\n---- SET PARAMS IN SUBTASK-METHOD ----\n");
        setParamsInSubtskFromLabels(mgNew, labels);
        System.out.println("\n---- GET LIST VARIABLES TO SET IN INSIDE SUBTASK-METHOD ----\n");
        ArrayList<Label> labels2 = getListVariablesForSubTask(mgOld, position, ">=");
        createVariablesFromLabels(labels2, mgNew, false, cg.getConstantPool());

//        IN THE FUTURE USE
//        ElementFOR.setNestedInArrayListElementFor(listElementsFOR);
//        displayTypeNestedFromArrayListElementFors(listElementsFOR);

        for (int id = start; id <= end; id++) {

            Instruction in = replaceOldIdInInstruction(ihy[id], mgOld, mgNew);

            if (id == elFor1.getIdGoTo()) {
                BranchInstruction bh = InstructionFactory.createBranchInstruction(
                        Const.GOTO, elFor1.getInstructionHandlePrevLOAD());
                elFor1.setBranchInstructionGOTO(bh);
                il.append(bh);
                continue;
            } else if (id == elFor2.getIdGoTo()) {
                BranchInstruction bh = InstructionFactory.createBranchInstruction(
                        Const.GOTO, elFor2.getInstructionHandlePrevLOAD());
                elFor2.setBranchInstructionGOTO(bh);
                il.append(bh);
                continue;
            } else if (id == elFor1.getIdPrevLoad()) {
                InstructionHandle ihPrevLoad = il.append(in);
                elFor1.setInstructionHandlePrevLOAD(ihPrevLoad);
                continue;
            } else if (id == elFor2.getIdPrevLoad()) {
                InstructionHandle ihPrevLoad = il.append(in);
                elFor2.setInstructionHandlePrevLOAD(ihPrevLoad);
                continue;
            } else if (id == elFor1.getIdInc()) {
                InstructionHandle ihINC = il.append(in);
                elFor1.setInstructionHandleINC(ihINC);
                continue;
            } else if (id == elFor2.getIdInc()) {
                InstructionHandle ihINC = il.append(in);
                elFor2.setInstructionHandleINC(ihINC);
                continue;
            } else if (id == elFor1.getIdInsideLoop()) {
                InstructionHandle ihFirstInside = il.append(in);
                elFor1.setInstructionHandleFirstInside(ihFirstInside);
                continue;
            } else if (id == elFor2.getIdInsideLoop()) {
                InstructionHandle ihFirstInside = il.append(in);
                elFor2.setInstructionHandleFirstInside(ihFirstInside);
                continue;
            } else if (elFor1.getListWithIdInstructionIF().contains(id)) {
                BranchHandle bh = ForLoopUtils.getBranchHandleIF(il, ihy[id].getInstruction());
                elFor1.addBranchHandleIfInForToArrayList(bh);
                continue;
            } else if (elFor2.getListWithIdInstructionIF().contains(id)) {
                BranchHandle bh = ForLoopUtils.getBranchHandleIF(il, ihy[id].getInstruction());
                elFor2.addBranchHandleIfInForToArrayList(bh);
                continue;
            } else il.append(in);

        }

        InstructionHandle rh = il.append(new PUSH(cp, 0));
        il.append(InstructionFactory.createReturn(Type.INT));

        elFor2.getListWithBranchHandleIfInFor().get(0).setTarget(elFor1.getInstructionHandleINC());
        System.out.println("\tlist size = " + elFor1.getListWithBranchHandleIfInFor().size());
        elFor1.getListWithBranchHandleIfInFor().get(0).setTarget(rh);

        mgNew.setMaxLocals();
        mgNew.setMaxStack();
        cg.addMethod(mgNew.getMethod());
        cg.getConstantPool().addMethodref(mgNew);

    }

    public static void fftMethod(ClassGen cg, MethodGen mgOld, ArrayList<ElementFOR> listElementsFOR) throws NullPointerException, TargetLostException {

        InstructionHandle[] ihy = mgOld.getInstructionList().getInstructionHandles();
        InstructionList il = mgOld.getInstructionList();
        InstructionList ilNew = il;

        ConstantPoolGen cp = cg.getConstantPool();
        InstructionFactory factory = new InstructionFactory(cg, cp);

        System.out.println("wszystkich elementow for jest: " + listElementsFOR.size() + "\n");

        ElementFOR elFor1 = listElementsFOR.get(3);
        ElementFOR elFor2 = listElementsFOR.get(4);

        int start = elFor2.getIdPrevStore();
        int end = elFor2.getIdGoTo();
        int position = ihy[elFor2.getIdPrevStore()].getPosition();


//        try {
//            il.delete(ihy[start], ihy[end]);
//        } catch (TargetLostException e) {
//            e.printStackTrace();
//        }

        mgOld.setMaxLocals();
        mgOld.setMaxStack();
        cg.replaceMethod(mgOld.getMethod(),mgOld.getMethod());
        cg.getConstantPool().addMethodref(mgOld);

    }


    private static void displayTypeNestedFromArrayListElementFors(ArrayList<ElementFOR> listElementsFOR) {
        for (int i = 0; i < listElementsFOR.size(); i++) {
            System.out.println("for-" + i + ", type nested: " + listElementsFOR.get(i).getTypeOfNested());
        }
    }

    private static void setParamsInSubtskFromLabels(MethodGen mgNew, ArrayList<Label> labels) {
        ArrayList<Type> t = new ArrayList<>();
        ArrayList<String> s = new ArrayList<>();
        for (Label label : labels) {
            t.add(Type.getType(label.getSignature()));
            s.add(label.getName());
        }
        Type[] tt = t.toArray(new Type[0]);
        String[] ss = s.toArray(new String[0]);

        System.out.println(Arrays.toString(tt));
        System.out.println(Arrays.toString(ss));
        mgNew.setArgumentTypes(tt);
        mgNew.setArgumentNames(ss);
    }

    private static void displayLVarFromMethod(ConstantPoolGen cp, MethodGen mgNew) {
        LocalVariable[] lvtp = mgNew.getLocalVariableTable(cp).getLocalVariableTable();
        System.out.println("\n");
        for (LocalVariable localVariable : lvtp) System.out.println(localVariable);
        System.out.println("\n");
    }

    private static void createVariablesFromLabels(ArrayList<Label> labels, MethodGen mgNew, boolean init, ConstantPoolGen cp) {
        InstructionList il = mgNew.getInstructionList();

        for (Label label : labels) {
            if (VariableUtils.getLVarIdByName(label.getName(), mgNew) == -1) {
                LocalVariableGen lv = mgNew.addLocalVariable(
                        label.getName(),
                        Type.getType(label.getSignature()),
                        null, null);

                if (init) {
                    int idi = lv.getIndex();
                    System.out.println(lv.getType());
                    if (lv.getType() == Type.INT) {
                        il.append(new PUSH(cp, 0));
                        il.append(InstructionFactory.createStore(Type.INT, idi));
                    } else if (lv.getType() == Type.DOUBLE) {
                        il.append(new PUSH(cp, 0.0));
                        il.append(InstructionFactory.createStore(Type.DOUBLE, idi));
                    }

                }
            }
        }
    }

    public static Instruction replaceOldIdInInstruction(InstructionHandle instruction, MethodGen mgOld, MethodGen mgNew) {
//        load/store
        Instruction replace = null;
        String instrString = instruction.toString();
        String[] string = instrString.split("\\W+|_");
        int idOLD = -1;
        int idNEW = 0;
        int length = string.length;

        if (string[2].contains("load") || string[2].contains("store")) {
            if (instruction.toString().contains("_")) idOLD = parseInt(string[3]);
            else idOLD = parseInt(string[length - 1]);
            idNEW = getCorrentIdInNewMethod(idOLD, instruction.getPosition(), mgOld, mgNew);
            if (idNEW != -1) {
//                STORE
                switch (string[2]) {
                    case "istore":
                        replace = new ISTORE(idNEW);
                        break;
                    case "dstore":
                        replace = new DSTORE(idNEW);
                        break;
                    case "fstore":
                        replace = new FSTORE(idNEW);
                        break;
                    case "astore":
                        replace = new ASTORE(idNEW);
                        break;
//                LOAD
                    case "aload":
                        replace = new ALOAD(idNEW);
                        break;
                    case "fload":
                        replace = new FLOAD(idNEW);
                        break;
                    case "iload":
                        replace = new ILOAD(idNEW);
                        break;
                    case "dload":
                        replace = new DLOAD(idNEW);
                        break;
                }
            }
        } else if (instrString.contains("inc")) {
            idOLD = parseInt(string[length - 2]);
            idNEW = getCorrentIdInNewMethod(idOLD, instruction.getPosition(), mgOld, mgNew);
            if (idNEW != -1) {
                int incrementValue = parseInt(string[length - 1]);
                replace = new IINC(idNEW, incrementValue);
            }
        }
//        System.out.print("\t"+idNEW+"\n");
        if (replace != null) return replace;
        else return instruction.getInstruction();
    }

    public static int getCorrentIdInNewMethod(int id, int position, MethodGen mgOld, MethodGen mgNew) {

        LocalVariable[] lvgOld = mgOld.getLocalVariableTable(mgOld.getConstantPool()).getLocalVariableTable();

        for (LocalVariable localVariable : lvgOld) {
            int tempEnd = localVariable.getStartPC() + localVariable.getLength();
            int tempId = localVariable.getIndex();
            if (tempId == id && tempEnd >= position)
                return VariableUtils.getLVarIdByName(localVariable.getName(), mgNew);
        }

        return 0;
    }

    private static ArrayList<Label> getListVariablesForSubTask(MethodGen mgOld, int position, String compare) {

        LocalVariable[] lv = mgOld.getLocalVariableTable(mgOld.getConstantPool()).getLocalVariableTable();
        ArrayList<Label> labels = new ArrayList<>();

        for (LocalVariable localVariable : lv) {

            int startPc = localVariable.getStartPC();
            int endPc = startPc + localVariable.getLength();
            int id;
            String signature;
            String name;

            if (compare.equals("<")) {
                if (startPc < position && endPc > position) {
                    id = localVariable.getIndex();
                    signature = localVariable.getSignature();
                    name = localVariable.getName();

                    if (!name.equals("this")) labels.add(new Label(name, signature, id, startPc, endPc));
                }
            }
            if (compare.equals(">=")) {
                if (startPc >= position && endPc > position) {
                    id = localVariable.getIndex();
                    signature = localVariable.getSignature();
                    name = localVariable.getName();

                    if (!name.equals("this")) labels.add(new Label(name, signature, id, startPc, endPc));
                }
            }

        }

        return labels;
    }

}
