package pl.edu.agh.bcel.nested;

import org.apache.bcel.generic.InstructionHandle;

import java.util.ArrayList;

public class ForLoopNEW {

    private final ArrayList<BranchHandleItem> instrIF = new ArrayList<>();
    private final ArrayList<ForLoopNEW> blocksIFELSE = new ArrayList<>();
    private final ArrayList<InstructionHandle> increments = null;

    private BranchHandleItem branchGOTO = null;

    private int positionGOTO = -1;
    private int idPositionGOTO = -1;
    private int positionAfterElse = -1;
    private int idPositionAfterElse = -1;
    private int positionStartLoop = -1;
    private int idPositionStartLoop = -1;
    private int positionPrevStore = -1;
    private int idPositionPrevStore = -1;
    private int positionINC = -1;
    private int idPositionINC = -1;
//    private int positionInsideLoop = -1;
//    private int idPositionInsideLoop = -1;


    //    before and after is usable for alone block if else
    private boolean before = false;
    private boolean after = false;

    public void addBlocksIFELSE(ForLoopNEW bhi) {
        blocksIFELSE.add(bhi);
    }

    public void addIncrements(InstructionHandle item) {
        System.out.println("tutaj podobno blad.. " + item);
        if (this.increments != null) {
            System.out.println("podobno null...");
            this.increments.add(item);
        }
    }


    public void addInstrIF(BranchHandleItem instrIF) {
        getInstrIF().add(instrIF);
    }


    public boolean compareHandlesIFinFOR(int a, int b) {
        ArrayList<BranchHandleItem> instrIF_2 = getInstrIF();
        int id_a = instrIF_2.get(a).getIdPositionToJump();
        int id_b = instrIF_2.get(a).getIdPositionToJump();
        return id_a == id_b;
    }

    public ArrayList<ForLoopNEW> getBlocksIFELSE() {
        return blocksIFELSE;
    }

    public BranchHandleItem getBranchGOTO() {
        return branchGOTO;
    }

    public void setBranchGOTO(BranchHandleItem branchGOTO) {
        this.branchGOTO = branchGOTO;
    }

    public int getIdPositionAfterElse() {
        return idPositionAfterElse;
    }

    public void setIdPositionAfterElse(int idPositionAfterElse) {
        this.idPositionAfterElse = idPositionAfterElse;
    }

    public int getIdPositionGOTO() {
        return idPositionGOTO;
    }

    public void setIdPositionGOTO(int idPositionGOTO) {
        this.idPositionGOTO = idPositionGOTO;
    }

    public int getIdPositionINC() {
        return idPositionINC;
    }

    public void setIdPositionINC(int idPositionINC) {
        this.idPositionINC = idPositionINC;
    }

    public int getIdPositionPrevStore() {
        return idPositionPrevStore;
    }

    public void setIdPositionPrevStore(int idPositionPrevStore) {
        this.idPositionPrevStore = idPositionPrevStore;
    }

    public int getIdPositionStartLoop() {
        return idPositionStartLoop;
    }

    public void setIdPositionStartLoop(int idPositionStartLoop) {
        this.idPositionStartLoop = idPositionStartLoop;
    }

    public ArrayList<Integer> getIdsIFinFor() {
        if (instrIF.size() > 0) {
            ArrayList<Integer> ids = new ArrayList<>();
            for (BranchHandleItem branchHandleItem : instrIF) {
                ids.add(branchHandleItem.getIdPosition());
            }
            return ids;
        } else return null;
    }

    public ArrayList<Integer> getIdsIFinForStartLoop() {
        if (instrIF.size() > 0) {
            ArrayList<Integer> ids = new ArrayList<>();
            for (BranchHandleItem branchHandleItem : instrIF) {
                ids.add(branchHandleItem.getIdPositionPrevLoad());
            }
            return ids;
        } else return null;
    }

    public ArrayList<InstructionHandle> getIncrements() {
        return increments;
    }

    public ArrayList<BranchHandleItem> getInstrIF() {
        return instrIF;
    }

    public int getPositionAfterElse() {
        return positionAfterElse;
    }

    public void setPositionAfterElse(int positionAfterElse) {
        this.positionAfterElse = positionAfterElse;
    }

    public int getPositionGOTO() {
        return positionGOTO;
    }

    public void setPositionGOTO(int positionGOTO) {
        this.positionGOTO = positionGOTO;
    }

    public int getPositionINC() {
        return positionINC;
    }

    public void setPositionINC(int positionINC) {
        this.positionINC = positionINC;
    }

//    public void setPositionGOTO(int positionGOTO) {
//        this.positionGOTO = positionGOTO;
//    }

    public int getPositionPrevStore() {
        return positionPrevStore;
    }

    public void setPositionPrevStore(int positionPrevStore) {
        this.positionPrevStore = positionPrevStore;
    }

    public int getPositionStartLoop() {
        return positionStartLoop;
    }

    public void setPositionStartLoop(int positionStartLoop) {
        this.positionStartLoop = positionStartLoop;
    }

    public boolean isAfter() {
        return after;
    }

    public void setAfter(boolean after) {
        this.after = after;
    }

    public boolean isBefore() {
        return before;
    }

    public void setBefore(boolean before) {
        this.before = before;
    }

}
