package pl.edu.agh.bcel.nested;

import org.apache.bcel.generic.BranchHandle;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.InstructionHandle;

import java.util.ArrayList;
import java.util.Collections;

public class ElementFOR {

    private final ArrayList<BranchHandle> listWithBranchHandleIfInFor;
    private ArrayList<Integer> listWithIdInstructionIfInsideFor;
    private final ArrayList<Integer> listWithIdInstructionIfInsideLoop;
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
            int idPrevStore, int idPrevLoad, int firstInside, int idInc, int idGoTo, int idIterator) {

        this.idPrevStore = idPrevStore;
        this.idPrevLoad = idPrevLoad;
        this.firstInside = firstInside;
        this.idInc = idInc;
        this.idGoTo = idGoTo;
        this.idIterator = idIterator;
        this.listWithIdInstructionIfInsideFor = new ArrayList<>();
        this.listWithBranchHandleIfInFor = new ArrayList<>();
        this.typeOfNested = -1;
        this.listWithIdInstructionIfInsideLoop = new ArrayList<>();
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

    public ArrayList<Integer> getListWithIdInstructionIfInsideLoop() {
        return listWithIdInstructionIfInsideLoop;
    }

    public void addIdInstructionIfInsideLoopIntoList(int id) {
        this.listWithIdInstructionIfInsideLoop.add(id);
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
        Collections.sort(this.listWithIdInstructionIfInsideFor);
    }

    public void addIdInstructionIfInsideForIntoList(Integer i) {
        this.listWithIdInstructionIfInsideFor.add(i);
    }

    public void displayElementFor(InstructionHandle[] ihy) {
        int counter = 1;
        printTypeNested();
        displayOneLine("\tid_PrevSTORE", idPrevStore, getInstruction(ihy, idPrevStore));
        displayOneLine("\tid_PrevLOAD", idPrevLoad, getInstruction(ihy, idPrevLoad));
        displayOneLine("\tid_ITERATOR", idIterator, "");
        if (listWithIdInstructionIfInsideFor.size() > 0) {
            displayOneLine("\tif_in_for", -1, "");

            for (Integer i : listWithIdInstructionIfInsideFor) {
                System.out.println("\t\t" + (counter++) + ")\tihy[" + i + "], " + getInstruction(ihy, i));
            }
        }
        if (listWithIdInstructionIfInsideFor.size() > 0) {
            displayOneLine("\tif_in_loop", -1, "");

            for (Integer i : listWithIdInstructionIfInsideLoop) {
                System.out.println("\t\t" + (counter++) + ")\tihy[" + i + "], " + getInstruction(ihy, i));
            }
        }
        displayOneLine("\tid_FirstINSIDE", firstInside, getInstruction(ihy, firstInside));
        displayOneLine("\tid_INC", idInc, getInstruction(ihy, idInc));
        displayOneLine("\tid_GOTO", idGoTo, getInstruction(ihy, idGoTo));
    }

    private void printTypeNested() {
        System.out.print(String.format("|%-15s| ", "\tisNested") + (typeOfNested != -1 ? "YES" : "NO "));
        if (typeOfNested == 0) System.out.print(" -> \tparent\n");
        else if (typeOfNested == 1) System.out.print(" -> \tchild\n");
        else System.out.println();
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

    public ArrayList<Integer> getListWithIdInstructionIfInsideFor() {
        return listWithIdInstructionIfInsideFor;
    }

    public void setListWithIdInstructionIfInsideFor(ArrayList<Integer> listaIfow) {
        this.listWithIdInstructionIfInsideFor = listaIfow;
    }

    public boolean isReturnH() {
        return returnH;
    }

    public void setReturnH(boolean returnH) {
        this.returnH = returnH;
    }

    public void setFirstInside() {
        if (listWithIdInstructionIfInsideFor.size() > 0)
            this.firstInside = listWithIdInstructionIfInsideFor.get(listWithIdInstructionIfInsideFor.size() - 1) + 1;
        else this.firstInside = -1;
    }

    public void displayTypeNestedFromArrayListElementFors(ArrayList<ElementFOR> listElementsFOR) {
        for (int i = 0; i < listElementsFOR.size(); i++) {
            System.out.println("for-" + i + ", type nested: " + listElementsFOR.get(i).getTypeOfNested());
        }
    }

}
