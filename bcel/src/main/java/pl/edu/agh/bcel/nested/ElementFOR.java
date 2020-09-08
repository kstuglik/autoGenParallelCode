package pl.edu.agh.bcel.nested;

import org.apache.bcel.generic.InstructionHandle;

import java.util.ArrayList;

public class ElementFOR {

    private ArrayList<Integer> listaIfow;

    private int idPrevStore;
    private int idPrevLoad;
    private int firstInside;
    private int idInc;
    private int idGoTo;
    private boolean returnH = false;
    private int idIteratora;

    public ElementFOR(
            ArrayList<Integer> listaIfow,
            int idPrevStore,
            int idPrevLoad,
            int firstInside,
            int idInc,
            int idGoTo,
            int idIteratora) {

        this.idPrevStore = idPrevStore;
        this.idPrevLoad = idPrevLoad;
        this.firstInside = firstInside;
        this.idInc = idInc;
        this.idGoTo = idGoTo;
        this.idIteratora = idIteratora;
        this.listaIfow = new ArrayList<>();
    }

    public void addListaIfow(Integer i) {
        this.listaIfow.add(i);
    }

    public int getIdLastIfInList() {
        if (listaIfow.size() > 0) {
            return listaIfow.get(listaIfow.size() - 1);
        } else
            return -1;
    }

    public void displayItem(InstructionHandle[] ihy) {
        System.out.println();
        System.out.println("********************************FORek************************************");
        System.out.println("idPrevStore:\t" + this.idPrevStore + ",\t" + getInstruction(ihy, this.idPrevStore));
        System.out.println("idPrevLoad: \t" + this.idPrevLoad + ",\t" + getInstruction(ihy, this.idPrevLoad));
        System.out.println("idIteratora:\t" + this.idIteratora + ",\t" + getInstruction(ihy, this.idIteratora));
        if (listaIfow.size() > 0) {
            System.out.println("Instruckje IF");
            for (Integer i : listaIfow) {
                System.out.println("\ti:\t" + i + ",\t" + getInstruction(ihy, i));
            }
        }
        System.out.println("firstInside:\t" + this.firstInside + ",\t" + getInstruction(ihy, this.firstInside));
        System.out.println("idInc:      \t" + this.idInc + ",\t" + getInstruction(ihy, this.idInc));
        System.out.println("idGoTo     :\t" + this.idGoTo + ",\t" + getInstruction(ihy, this.idGoTo));
        System.out.println();
    }

    public int getIdGoTo() {
        return idGoTo;
    }

    public void setIdGoTo(int idGoTo) {
        this.idGoTo = idGoTo;
    }

    public int getIdInc() {
        return idInc;
    }

    public void setIdInc(int idInc) {
        this.idInc = idInc;
    }

    public int getIdInsideLoop() {
        return firstInside;
    }

    public int getIdIteratora() {
        return idIteratora;
    }

    public void setIdIteratora(int idIteratora) {
        this.idIteratora = idIteratora;
    }

    public int getIdPrevLoad() {
        return idPrevLoad;
    }

    public void setIdPrevLoad(int idPrevLoad) {
        this.idPrevLoad = idPrevLoad;
    }

    public int getIdPrevStore() {
        return idPrevStore;
    }

    public void setIdPrevStore(int idPrevStore) {
        this.idPrevStore = idPrevStore;
    }

    public String getInstruction(InstructionHandle[] ihy, int id) {
        return id != -1 ? ihy[id].toString() : "-1";
    }

    public ArrayList<Integer> getListaIfow() {
        return listaIfow;
    }

    public void setListaIfow(ArrayList<Integer> listaIfow) {
        this.listaIfow = listaIfow;
    }

    public boolean isReturnH() {
        return returnH;
    }

    public void setReturnH(boolean returnH) {
        this.returnH = returnH;
    }

    public void setFirstInside(int firstInside) {
        this.firstInside = firstInside;
    }
}
