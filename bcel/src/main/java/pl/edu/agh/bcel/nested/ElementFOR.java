package pl.edu.agh.bcel.nested;

import org.apache.bcel.generic.BranchHandle;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.InstructionHandle;

import java.util.ArrayList;
import java.util.Collections;

public class ElementFOR {

    private ArrayList<Integer> listWithIdInstructionIF;
    private ArrayList<Integer> listWithIdInstructionIfInside;
    private ArrayList<BranchHandle> listWithBranchHandleIfInFor;
    private int idPrevStore;
    private int idPrevLoad;
    private int firstInside;
    private int idInc;
    private int idGoTo;
    private boolean returnH = false;
    private int idIterator;
    private BranchInstruction branchInstructionGOTO;
    private InstructionHandle instructionHandleINC;
    private InstructionHandle instructionHandlePrevLOAD;
    private InstructionHandle instructionHandleFirstInside;
    private int typeOfNested;//-1: not nested, 0: is first for nested, 1: is nested
    public ElementFOR(
            ArrayList<Integer> listWithIdInstructionIF,
            int idPrevStore,
            int idPrevLoad,
            int firstInside,
            int idInc,
            int idGoTo,
            int idIterator) {

        this.idPrevStore = idPrevStore;
        this.idPrevLoad = idPrevLoad;
        this.firstInside = firstInside;
        this.idInc = idInc;
        this.idGoTo = idGoTo;
        this.idIterator = idIterator;
        this.listWithIdInstructionIF = new ArrayList<>();
        this.listWithBranchHandleIfInFor = new ArrayList<>();
    }

    private static void displayOneLine(String name, int idPrevStore, String instruction) {
        System.out.println(
                String.format("|%-15s| ", name) +
                        (idPrevStore != -1 ? String.format("%-3d, ", idPrevStore) : "") +
                        instruction);
    }

    public static void setNestedInArrayListElementFor(ArrayList<ElementFOR> listElementFors) {
        for (int i = 1; i < listElementFors.size(); i++) {
            ElementFOR elementFor1 = listElementFors.get(i - 1);
            ElementFOR elementFor2 = listElementFors.get(i);
            if (elementFor1.getIdPrevLoad() < elementFor2.getIdPrevStore() && elementFor1.getIdGoTo() > elementFor2.getIdGoTo()) {
                switch (elementFor1.getTypeOfNested()) {
                    case 0:
                    case 1:
                        elementFor1.setTypeOfNested(1);
                        elementFor2.setTypeOfNested(1);
                        break;
                    case -1:
                        elementFor1.setTypeOfNested(0);
                        elementFor2.setTypeOfNested(1);
                        break;
                }
            } else {
                elementFor1.setTypeOfNested(-1);
                elementFor2.setTypeOfNested(-1);
            }
        }
    }

    public ArrayList<BranchHandle> getListWithBranchHandleIfInFor() {
        return listWithBranchHandleIfInFor;
    }

    public void addBranchHandleIfInForToArrayList(BranchHandle bhIfInFor) {
        if (bhIfInFor != null)
            listWithBranchHandleIfInFor.add(bhIfInFor);
    }

    public InstructionHandle getInstructionHandleFirstInside() {
        return instructionHandleFirstInside;
    }

    public void setInstructionHandleFirstInside(InstructionHandle instructionHandleFirstInside) {
        this.instructionHandleFirstInside = instructionHandleFirstInside;
    }

    public int getTypeOfNested() {
        return typeOfNested;
    }

    public void setTypeOfNested(int typeOfNested) {
        this.typeOfNested = typeOfNested;
    }

    public InstructionHandle getInstructionHandlePrevLOAD() {
        return instructionHandlePrevLOAD;
    }

    public void setInstructionHandlePrevLOAD(InstructionHandle instructionHandlePrevLOAD) {
        this.instructionHandlePrevLOAD = instructionHandlePrevLOAD;
    }

    public InstructionHandle getInstructionHandleINC() {
        return instructionHandleINC;
    }

    public void setInstructionHandleINC(InstructionHandle instructionHandleINC) {
        this.instructionHandleINC = instructionHandleINC;
    }

    public BranchInstruction getBranchInstructionGOTO() {
        return branchInstructionGOTO;
    }

    public void setBranchInstructionGOTO(BranchInstruction branchInstructionGOTO) {
        this.branchInstructionGOTO = branchInstructionGOTO;
    }

    public void sortListWithElementsIF() {
        Collections.sort(this.listWithIdInstructionIF);
    }

    public void addIdInstructionIfIntoThisList(Integer i) {
        this.listWithIdInstructionIF.add(i);
    }

    public void addIdInstructionIfIntoInsideList(Integer i) {
        this.listWithIdInstructionIfInside.add(i);
    }

    public void displayElementFor(InstructionHandle[] ihy) {
        int counter = 1;
        displayOneLine("\tid_PrevSTORE", idPrevStore, getInstruction(ihy, idPrevStore));
        displayOneLine("\tid_PrevLOAD", idPrevLoad, getInstruction(ihy, idPrevLoad));
        displayOneLine("\tid_ITERATOR", idIterator, "");
        if (listWithIdInstructionIF.size() > 0) {
            displayOneLine("\tids_IFS", -1, "");

            for (Integer i : listWithIdInstructionIF) {
                System.out.println("\t\t" + (counter++) + ")\tihy[" + i + "], " + getInstruction(ihy, i));
            }
        }
        displayOneLine("\tid_FirstINSIDE", firstInside, getInstruction(ihy, firstInside));
        displayOneLine("\tid_INC", idInc, getInstruction(ihy, idInc));
        displayOneLine("\tid_GOTO", idGoTo, getInstruction(ihy, idGoTo));
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

    public int getidIterator() {
        return idIterator;
    }

    public void setidIterator(int idIterator) {
        this.idIterator = idIterator;
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

    public ArrayList<Integer> getListWithIdInstructionIF() {
        return listWithIdInstructionIF;
    }

    public void setListWithIdInstructionIF(ArrayList<Integer> listaIfow) {
        this.listWithIdInstructionIF = listaIfow;
    }

    public boolean isReturnH() {
        return returnH;
    }

    public void setReturnH(boolean returnH) {
        this.returnH = returnH;
    }

    public void setFirstInside() {
        if (listWithIdInstructionIF.size() > 0)
            this.firstInside = listWithIdInstructionIF.get(listWithIdInstructionIF.size() - 1) + 1;
        else this.firstInside = -1;
    }

}
