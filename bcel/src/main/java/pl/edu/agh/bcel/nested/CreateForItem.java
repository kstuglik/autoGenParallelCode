package pl.edu.agh.bcel.nested;

import org.apache.bcel.generic.InstructionHandle;

import java.util.ArrayList;

public class CreateForItem {
    private BranchHandleItem gotoBH = null;
    private InstructionHandle startLoop = null;
    private InstructionHandle prevStore = null;
    private InstructionHandle increment = null;
    private final ArrayList<BranchHandleItem> instrIF = new ArrayList<>();

    public void addInstrIF(BranchHandleItem instrIF) {
        this.instrIF.add(instrIF);
    }

    public BranchHandleItem getGoToBH() {
        return gotoBH;
    }

    public InstructionHandle getIncrement() {
        return increment;
    }

    public ArrayList<BranchHandleItem> getInstrIF() {
        return instrIF;
    }

    public InstructionHandle getPrevStore() {
        return prevStore;
    }

    public void setPrevStore(InstructionHandle prevStore) {
        this.prevStore = prevStore;
    }

    public InstructionHandle getStartLoop() {
        return startLoop;
    }

    public void setStartLoop(InstructionHandle startLoop) {
        this.startLoop = startLoop;
    }

    public void setGOTO(BranchHandleItem gotoBH) {
        this.gotoBH = gotoBH;
    }

    public void setINC(InstructionHandle increment) {
        this.increment = increment;
    }
}
