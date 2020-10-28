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

    public Label(String name, String signature, int id, int startPc, int endPc) {
        setName(name);
        setSignature(signature);
        setId(id);
        setStartPc(startPc);
        setEndPc(endPc);
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

    public static void createVariablesFromLabels(ArrayList<Label> labels, MethodGen mgNew, boolean init, ConstantPoolGen cp) {
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

    public static ArrayList<Label> getListVariablesForSubTask(MethodGen mgOld, int position, String compare) {

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

    public void display() {
        System.out.println("name: " + getName() +
                ", signature:" + getSignature() +
                ", id: " + getId() +
                ", startPc: " + getStartPc() +
                ", endPc: " + getEndPc());
    }

}
