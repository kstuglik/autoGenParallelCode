package pl.edu.agh.bcel.utils;

import org.apache.bcel.generic.*;

import java.util.*;

public class ForLoopItem {

    private final ArrayList<InstructionHandle> handleIF = new ArrayList<>();
    private InstructionHandle handleFirstInFor;
    private InstructionHandle handleStartLoop;
    private InstructionHandle handleInsideLoop;
    private InstructionHandle handleGOTO;
    private InstructionHandle handleINC;

    private InstructionHandle[] handlesBeforeFor;
    private InstructionHandle[] handlesAfterLoop;

    private final List<ForLoopItem> ifInsideLoop = new ArrayList<>();

    private int idIterator;

    private HashMap<Integer, Integer> positionId = new HashMap<>();

    public static HashMap<Integer, Integer> getHashmapPositionId(InstructionHandle[] ihy) {
        HashMap<Integer, Integer> hashmapInstructionPositionId = new HashMap<>();
        for (int i = 0; i < ihy.length; i++)
            hashmapInstructionPositionId.put(ihy[i].getPosition(), i);
        return hashmapInstructionPositionId;
    }

    public static int getIdByPositionInCode(HashMap<Integer, Integer> hashmapPositionId, int position) {
        return hashmapPositionId.get(position);
    }

    protected static InstructionHandle[] getIhyAfterForLoop(
            InstructionHandle[] ihy, List<ForLoopItem> forLoopItems, int idPetli, HashMap<Integer, Integer> hashmapPositionId) {

        InstructionHandle[] myAnswerIs = null;
        System.out.println("idPetli:\t" + idPetli);
        if (idPetli > 0) {
            int startPosition = forLoopItems.get(idPetli).getHandleGOTO().getPosition();
            int startId = getIdByPositionInCode(hashmapPositionId, startPosition) + 1;

            int endPosition = forLoopItems.get(idPetli - 1).getHandleINC().getPosition();
            int endId = getIdByPositionInCode(hashmapPositionId, endPosition);

            if (endId - startId > 1) myAnswerIs = Arrays.copyOfRange(ihy, startId, endId - startId);

        } else {
            int startPosition = forLoopItems.get(idPetli).getHandleGOTO().getPosition();
            int startId = getIdByPositionInCode(hashmapPositionId, startPosition) + 1;
            int endId = ihy.length;

            if (endId - startId > 1) myAnswerIs = Arrays.copyOfRange(ihy, startId, endId);
        }
        return myAnswerIs;
    }

    protected static InstructionHandle[] getIhyBeforeForLoop(
            InstructionHandle[] ihy, List<ForLoopItem> forLoopItems, int idPetli, HashMap<Integer, Integer> hashmapPositionId) {

        InstructionHandle[] myAnswerIs = null;

        if (idPetli > 0) {
            int startPosition = forLoopItems.get(idPetli - 1).getLastHandleCompares().getPosition();
            int startId = getIdByPositionInCode(hashmapPositionId, startPosition) + 1;

            int endPosition = forLoopItems.get(idPetli).getHandleStartLoop().getPosition();
            int endId = getIdByPositionInCode(hashmapPositionId, endPosition);

            if (endId - startId > 1) myAnswerIs = Arrays.copyOfRange(ihy, startId, endId - startId);

        } else {
            int startId = 0;
            int endPosition = forLoopItems.get(0).getHandleFirstInFor().getPosition();
            int endId = getIdByPositionInCode(hashmapPositionId, endPosition);

            if (endId - startId > 1) myAnswerIs = Arrays.copyOfRange(ihy, startId, endId);
        }
        return myAnswerIs;
    }

    public static int getLastInstructionPosition(List<InstructionHandle> forLoop) {
        return forLoop.get(forLoop.size() - 1).getPosition();
    }

    public static List<ForLoopItem> getListLoopForItems(InstructionHandle[] ihy, HashMap<Integer, Integer> hashmapPositionId) {

        List<ForLoopItem> allLoopsInMethod = new ArrayList<>();

        for (int i = 0; i < ihy.length; i++) {
            InstructionHandle item = ihy[i];
            if (item.getInstruction() instanceof GOTO) {
                ForLoopItem temp = new ForLoopItem();
                temp.setHandleGOTO(ihy[i]);
                temp.setHandleINC(ihy[i - 1]);
                int idIterator = Integer.parseInt(ihy[i - 1].getInstruction().toString().split(" ")[1]);
                temp.setIdIterator(idIterator);
                allLoopsInMethod.add(temp);
            }
        }

        for (ForLoopItem item : allLoopsInMethod) setHandlerFirstInForAndStartLoop(item, ihy, hashmapPositionId);

        for (ForLoopItem item : allLoopsInMethod) setHandlesIFAndInsideLoop(item, ihy, hashmapPositionId);

        verifyValidityAssignedIfToLoop(allLoopsInMethod);

        return allLoopsInMethod;
    }

    static void jesliIfNalezyDoLooopBodyToGoTamPrzenies(List<ForLoopItem> ifInsideLoop) {
        for (int i = 0; i < ifInsideLoop.size(); i++) {
            if (ifInsideLoop.get(i).getHandleIF().size() == 0) {
                System.out.println("wykryto przypadek fałszywej pętli :) dla i = " + i);
            }
        }
    }

    private static void setHandlerFirstInForAndStartLoop(ForLoopItem item, InstructionHandle[] ihy, HashMap<Integer, Integer> hashmapPositionId) {
        int position = Integer.parseInt(item.getHandleGOTO().toString().split("->")[1].replace(" ", ""));
        int idStartLoopArray = getIdByPositionInCode(hashmapPositionId, position);
        item.setHandleStartLoop(ihy[idStartLoopArray]);
//        ussualy: iload and istore are 2 instructions before idStartLoopArray
        item.setHandleFirstInFor(ihy[idStartLoopArray].getPrev().getPrev());
    }

    private static void setHandlesIFAndInsideLoop(
            ForLoopItem item, InstructionHandle[] ihy, HashMap<Integer, Integer> hashmapPositionId) {

        int positionStart = item.getHandleStartLoop().getPosition();
        int idStart = hashmapPositionId.get(positionStart);

        int positionEnd = item.getHandleGOTO().getPosition();
        int idEnd = hashmapPositionId.get(positionEnd);

        int i;

        for (i = idStart; i < idEnd; i++) {
            Instruction instr = ihy[i].getInstruction();

            if (instr instanceof IF_ICMPGE || instr instanceof IF_ICMPGT ||
                    instr instanceof IF_ICMPLE || instr instanceof IF_ICMPLT ||
                    instr instanceof IF_ICMPEQ || instr instanceof IF_ICMPNE ||
                    instr instanceof IF_ACMPEQ || instr instanceof IF_ACMPNE ||
                    instr instanceof IFLE || instr instanceof IFLT ||
                    instr instanceof IFGE || instr instanceof IFGT ||
                    instr instanceof IFEQ || instr instanceof IFNE) {
                item.addHandleIF(ihy[i]);

            }
        }
        item.setHandleInsideLoop(ihy[i + 1]);
    }

    private static void verifyValidityAssignedIfToLoop(List<ForLoopItem> allLoopsInMethod) {
        Collections.reverse(allLoopsInMethod);
        for (int i = 0; i < allLoopsInMethod.size() - 1; i++) {// -1 because compare
            ArrayList<InstructionHandle> outsideLoop = allLoopsInMethod.get(i).getHandleIF();
            ArrayList<InstructionHandle> internalLoop = allLoopsInMethod.get(i + 1).getHandleIF();
            outsideLoop.removeAll(internalLoop);
        }
    }

    public void addHandleIF(InstructionHandle ih) {
        this.handleIF.add(ih);
    }

    public void displayInfoAboutHandles() {
        System.out.println("\tfirstInstructionInFor:\t" + handleFirstInFor + "\n" +
                "\tloopStartHandler:\t" + handleStartLoop + "\n" +
                "\tifcompareHandler:\t" + handleIF + "\n" +
                "\tfirstInstructionInLoop:\t" + handleInsideLoop + "\n" +
                "\tincrementHandler:\t" + handleINC + "\n" +
                "\tgotoHandler:\t" + handleGOTO);
    }

    public InstructionHandle getHandleFirstInFor() {
        return handleFirstInFor;
    }

    public void setHandleFirstInFor(InstructionHandle ih) {
        this.handleFirstInFor = ih;
    }

    public InstructionHandle getHandleGOTO() {
        return handleGOTO;
    }

    public void setHandleGOTO(InstructionHandle ih) {
        this.handleGOTO = ih;
    }

    public ArrayList<InstructionHandle> getHandleIF() {
        return handleIF;
    }

    public InstructionHandle getHandleINC() {
        return handleINC;
    }

    public void setHandleINC(InstructionHandle ih) {
        this.handleINC = ih;
    }

    public InstructionHandle getHandleInsideLoop() {
        return handleInsideLoop;
    }

    public void setHandleInsideLoop(InstructionHandle ih) {
        this.handleInsideLoop = ih;
    }

    public InstructionHandle getHandleStartLoop() {
        return handleStartLoop;
    }

    public void setHandleStartLoop(InstructionHandle ih) {
        this.handleStartLoop = ih;
    }

    public int getIdIterator() {
        return idIterator;
    }

    public void setIdIterator(int idIterator) {
        this.idIterator = idIterator;
    }

    public InstructionHandle getLastHandleCompares() {
        int last = handleIF.size() - 1;
        return handleIF.get(last);
    }

    public HashMap<Integer, Integer> getPositionId() {
        return positionId;
    }

    public void setPositionId(HashMap<Integer, Integer> positionId) {
        this.positionId = positionId;
    }
}