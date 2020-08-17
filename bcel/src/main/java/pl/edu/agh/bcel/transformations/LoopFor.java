package pl.edu.agh.bcel.transformations;

import org.apache.bcel.generic.InstructionHandle;

import java.util.ArrayList;

public class LoopFor {

    private final ArrayList<InstructionHandle> ifcompareHandler = new ArrayList<>();
    private InstructionHandle firstInstructionInFor;
    private InstructionHandle loopStartHandler;
    private InstructionHandle firstInstructionInLoop;
    private InstructionHandle gotoHandler;
    private InstructionHandle incrementHandler;

    private InstructionHandle[] handlesBeforeFor;
    private InstructionHandle[] handlesAfterLoop;

    private int idIterator;

    public void displayInfoAboutHandles() {
        System.out.println("\tfirstInstructionInFor:\t" + firstInstructionInFor + "\n" +
                "\tloopStartHandler:\t" + loopStartHandler + "\n" +
                "\tifcompareHandler:\t" + ifcompareHandler + "\n" +
                "\tfirstInstructionInLoop:\t" + firstInstructionInLoop + "\n" +
                "\tincrementHandler:\t" + incrementHandler + "\n" +
                "\tgotoHandler:\t" + gotoHandler);
    }

    public InstructionHandle getFirstInstructionInFor() {
        return firstInstructionInFor;
    }

    public void setFirstInstructionInFor(InstructionHandle firstInstructionForLoop) {
        this.firstInstructionInFor = firstInstructionForLoop;
    }

    public InstructionHandle getFirstInstructionInLoop() {
        return firstInstructionInLoop;
    }

    public InstructionHandle getGotoHandler() {
        return gotoHandler;
    }

    public void setGotoHandler(InstructionHandle gotoHandler) {
        this.gotoHandler = gotoHandler;
    }

    public int getIdIterator() {
        return idIterator;
    }

    public void setIdIterator(int idIterator) {
        this.idIterator = idIterator;
    }

    public ArrayList<InstructionHandle> getIfcompareHandler() {
        return ifcompareHandler;
    }

    public InstructionHandle getIncrementHandler() {
        return incrementHandler;
    }

    public void setIncrementHandler(InstructionHandle incrementHandler) {
        this.incrementHandler = incrementHandler;
    }

    public InstructionHandle getLastIfComparator() {
        int count = ifcompareHandler.size();
        return ifcompareHandler.get(count - 1);
    }

    public InstructionHandle getLoopStartHandler() {
        return loopStartHandler;
    }

    public void setLoopStartHandler(InstructionHandle loopStartHandler) {
        this.loopStartHandler = loopStartHandler;
    }

    public void setFirstInLoopHandle(InstructionHandle firstInstructionInLoop) {
        this.firstInstructionInLoop = firstInstructionInLoop;
    }

    public void setIfCompareHandle(InstructionHandle ifcompareHandler) {
        this.ifcompareHandler.add(ifcompareHandler);
    }

}