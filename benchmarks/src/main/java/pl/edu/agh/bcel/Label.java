package pl.edu.agh.bcel;

import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.generic.*;
import pl.edu.agh.bcel.utils.VariableUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class Label {

    private String name;
    private String signature;
    private int id;
    private int startPc;
    private int endPc;
    private boolean inUse;

    public Label(String name, String signature, int id, int startPc, int endPc) {
        setName(name);
        setSignature(signature);
        setId(id);
        setStartPc(startPc);
        setEndPc(endPc);
        setInUse(false);
    }

    public static void setParamsInSubtskFromLabels(MethodGen mgNew, ArrayList<Label> labels) {
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

    public static void createVariablesFromLabels(ArrayList<Label> labels,
                                                 MethodGen mgNew, ConstantPoolGen cp) {
        InstructionList il = mgNew.getInstructionList();

        for (Label label : labels) {
            if (VariableUtils.getLVarIdByName(label.getName(), mgNew) == -1) {
                LocalVariableGen lv = mgNew.addLocalVariable(
                        label.getName(),
                        Type.getType(label.getSignature()),
                        null, null);
            }
        }
    }

    public static ArrayList<Label> getListVariablesForSubTask(MethodGen mgOld, int position) {

        LocalVariable[] lv = mgOld.getLocalVariableTable(mgOld.getConstantPool()).getLocalVariableTable();
        ArrayList<Label> labels = new ArrayList<>();

        for (LocalVariable localVariable : lv) {

            int startPc = localVariable.getStartPC();
            int endPc = startPc + localVariable.getLength();
            int id;
            String signature;
            String name;

            if (startPc < position && endPc > position) {
                id = localVariable.getIndex();
                signature = localVariable.getSignature();
                name = localVariable.getName();
                if (!name.equals("this")) labels.add(new Label(name, signature, id, startPc, endPc));

            }
        }


        int index = labels.size() - 1;
        if (index != -1) labels.remove(index);
        labels.add(new Label(LaunchProperties.START_CONDITION_NAME, "I", -1, -1, -1));
        labels.add(new Label(LaunchProperties.STOP_CONDITION_NAME, "I", -1, -1, -1));

        return labels;
    }

    public static ArrayList<Label> getListVariablesToFinal(MethodGen mg, int idStart, int idStop) {

        ArrayList<Label> result = new ArrayList<>();
        InstructionHandle[] ihy = mg.getInstructionList().getInstructionHandles();
        ArrayList<Integer> lvarInUse = new ArrayList<>();
        for (int i = idStart; i < idStop; i++) {
            if (ihy[i].toString().contains("load") || ihy[i].toString().contains("store")) {
                Instruction instr = ihy[i].getInstruction();
                int idLVar = -1;
                String[] string = instr.toString().split("\\W+|_");
                int ile = string.length;

                if (ihy[i].toString().contains("_")) {
                    idLVar = Integer.parseInt(string[1]);
                } else {
                    idLVar = Integer.parseInt(string[ile - 1]);
                }
                if (!lvarInUse.contains(idLVar)) {
                    lvarInUse.add(idLVar);
                    Label label = searchInVariableTable(mg, ihy[i].getPosition(), idLVar);
                    if(label!=null) {
                        result.add(label);
                    }
                }

            }
        }

        return result;
    }

    private static Label searchInVariableTable(MethodGen mg, int idInstruction, int idLVar) {
        LocalVariable[] lvt = mg.getLocalVariableTable(mg.getConstantPool()).getLocalVariableTable();
        Label label = null;
        for (int i = lvt.length - 1; i >= 0; i--) {
            LocalVariable lv = lvt[i];
            if (lv.getIndex() == idLVar ) {
                label = new Label(lv.getName(), lv.getSignature(), lv.getIndex(), lv.getStartPC(), lv.getStartPC() + lv.getLength());
            }
        }
        return label;
    }

    public static String displayNameFromArrayList(ArrayList<Label> alLabels) {
        String response = "";
        for (Label alLabel : alLabels) {
            response = response + alLabel.getName() + " ";
        }
        return response;
    }

    public boolean getInUse() {
        return inUse;
    }

    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStartPc() {
        return startPc;
    }

    public void setStartPc(int startPc) {
        this.startPc = startPc;
    }

    public int getEndPc() {
        return endPc;
    }

    public void setEndPc(int endPc) {
        this.endPc = endPc;
    }

    public String display() {
        return "name: " + getName() + ", signature:" + getSignature() + ", id: " + getId() + ", startPc: " + getStartPc() + ", endPc: " + getEndPc();
    }
}
