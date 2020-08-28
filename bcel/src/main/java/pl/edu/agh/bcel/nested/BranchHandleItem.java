package pl.edu.agh.bcel.nested;

import org.apache.bcel.generic.BranchHandle;

import java.util.ArrayList;

public class BranchHandleItem {
    private final ArrayList<ForLoopNEW> listOfPairedBlocksIfElse = new ArrayList<>();

    private BranchHandle instruction;
    private String signature;
    private int position = -1;
    private int idPosition = -1;
    private int positionToJump = -1;
    private int idPositionToJump = -1;
    private int idPositionPrevStore = -1;
    private int idPositionPrevLoad = -1;

    public void addItemToListOfPairedBlocksIfElse(ForLoopNEW cfi) {
        this.listOfPairedBlocksIfElse.add(cfi);
    }

    public int getIdPosition() {
        return idPosition;
    }

    public void setIdPosition(int idPosition) {
        this.idPosition = idPosition;
    }

    public int getIdPositionPrevLoad() {
        return idPositionPrevLoad;
    }

    public void setIdPositionPrevLoad(int idPositionPrevLoad) {
        this.idPositionPrevLoad = idPositionPrevLoad;
    }

    public int getIdPositionPrevStore() {
        return idPositionPrevStore;
    }

    public void setIdPositionPrevStore(int idPositionPrevStore) {
        this.idPositionPrevStore = idPositionPrevStore;
    }

    public int getIdPositionToJump() {
        return idPositionToJump;
    }

    public void setIdPositionToJump(int idPositionToJump) {
        this.idPositionToJump = idPositionToJump;
    }

    public BranchHandle getInstruction() {
        return instruction;
    }

    public void setInstruction(BranchHandle instruction) {
        this.instruction = instruction;
    }

    public ArrayList<ForLoopNEW> getListOfPairedBlocksIfElse() {
        return listOfPairedBlocksIfElse;
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

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
