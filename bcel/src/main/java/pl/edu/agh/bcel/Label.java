package pl.edu.agh.bcel;

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
