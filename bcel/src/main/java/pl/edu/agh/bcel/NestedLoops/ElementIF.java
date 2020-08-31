package pl.edu.agh.bcel.NestedLoops;

import org.apache.bcel.generic.InstructionHandle;

public class ElementIF {
    private int idPrevLoad;
    private int id;
    private int idGoto;
    private int idJump;

    public ElementIF(int id, int idGoto, int idJump, int idPrevLoad) {
        this.id = id;
        this.idGoto = idGoto;
        this.idPrevLoad = idPrevLoad;
        this.idJump = idJump;
    }

    public void displayItem(InstructionHandle[] ihy) {
        System.out.println();
        System.out.println("********************************IFek*************************************");
        System.out.println("idPrevLoad:\t" + this.idPrevLoad + ",\t" + getInstruction(ihy, this.idPrevLoad));
        System.out.println("id        :\t" + this.id + ",\t" + getInstruction(ihy, this.id));
        System.out.println("idGoto    :\t" + this.idGoto + ",\t" + getInstruction(ihy, this.idGoto));
        System.out.println("idJump    :\t" + this.idJump + ",\t" + getInstruction(ihy, this.idJump));
        System.out.println();
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
