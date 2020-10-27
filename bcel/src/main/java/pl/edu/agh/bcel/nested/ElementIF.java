package pl.edu.agh.bcel.nested;

import org.apache.bcel.generic.BranchHandle;
import org.apache.bcel.generic.InstructionHandle;

public class ElementIF {
    private int idPrevLoad;
    private int id;
    private int idGoto;
    private int idJump;
    private BranchHandle branchHandleIf;

    public ElementIF(int id, int idGoto, int idJump, int idPrevLoad) {
        this.id = id;
        this.idGoto = idGoto;
        this.idPrevLoad = idPrevLoad;
        this.idJump = idJump;
    }

    private static void displayOneLine(String name, int idPrevStore, String instruction) {
        System.out.println(String.format("|%-15s| ", name) + String.format("%-3d, ", idPrevStore) + instruction);
    }

    public BranchHandle getBranchHandleIf() {
        return branchHandleIf;
    }

    public void setBranchHandleIf(BranchHandle branchHandleIf) {
        this.branchHandleIf = branchHandleIf;
    }

    public void displayElementIf(InstructionHandle[] ihy) {
        displayOneLine("\tidPrevLoad", idPrevLoad, getInstruction(ihy, this.idPrevLoad));
        displayOneLine("\tid", id, getInstruction(ihy, this.id));
        displayOneLine("\tidGoto", idGoto, getInstruction(ihy, this.idGoto));
        displayOneLine("\tidJump", idJump, getInstruction(ihy, this.idJump));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdGoto() {
        return idGoto;
    }

    public void setIdGoto(int idGoto) {
        this.idGoto = idGoto;
    }

    public int getIdJump() {
        return idJump;
    }

    public void setIdJump(int idJump) {
        this.idJump = idJump;
    }

    public int getIdPrevLoad() {
        return idPrevLoad;
    }

    public void setIdPrevLoad(int idPrevLoad) {
        this.idPrevLoad = idPrevLoad;
    }

    public String getInstruction(InstructionHandle[] ihy, int id) {
        return id != -1 ? ihy[id].toString() : "-1";
    }
}
