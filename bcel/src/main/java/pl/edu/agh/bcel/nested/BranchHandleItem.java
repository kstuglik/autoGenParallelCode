package pl.edu.agh.bcel.nested;

import org.apache.bcel.generic.InstructionHandle;

public class BranchHandleItem {
    private int position;
    private int idPosition;
    private int positionToJump;
    private int idPositionToJump;
    private InstructionHandle instruction;
    private String signature;
    private int prevStore;
    private int prevLoad;


    public int getIdPosition() {
        return idPosition;
    }

    public void setIdPosition(int idPosition) {
        this.idPosition = idPosition;
    }

    public int getIdPositionToJump() {
        return idPositionToJump;
    }

    public void setIdPositionToJump(int idPositionToJump) {
        this.idPositionToJump = idPositionToJump;
    }

    public InstructionHandle getInstruction() {
        return instruction;
    }

    public void setInstruction(InstructionHandle instruction) {
        this.instruction = instruction;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPositionToJump() {
        return positionToJump;
    }

    public void setPositionToJump(int positionToJump) {
        this.positionToJump = positionToJump;
    }

    public int getPrevLoad() {
        return prevLoad;
    }

    public void setPrevLoad(int prevLoad) {
        this.prevLoad = prevLoad;
    }

    public int getPrevStore() {
        return prevStore;
    }

    public void setPrevStore(int prevStore) {
        this.prevStore = prevStore;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
