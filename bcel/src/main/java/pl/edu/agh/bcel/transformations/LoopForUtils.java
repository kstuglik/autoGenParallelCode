package pl.edu.agh.bcel.transformations;

import org.apache.bcel.generic.*;

import java.util.*;

public class LoopForUtils {
    public static int getIdByPositionInCode(HashMap<Integer, Integer> hashmapPositionId, int position) {
        return hashmapPositionId.get(position);
    }

    protected static InstructionHandle[] getIhyAfterForLoop(
            InstructionHandle[] ihy, List<LoopFor> loopFors, int idPetli, HashMap<Integer, Integer> hashmapPositionId) {

        InstructionHandle[] myAnswerIs = null;
        System.out.println("idPetli:\t" + idPetli);
        if (idPetli > 0) {
            int startPosition = loopFors.get(idPetli).getGotoHandler().getPosition();
            int startId = getIdByPositionInCode(hashmapPositionId, startPosition) + 1;

            int endPosition = loopFors.get(idPetli - 1).getIncrementHandler().getPosition();
            int endId = getIdByPositionInCode(hashmapPositionId, endPosition);

            if (endId - startId > 1) {
                myAnswerIs = Arrays.copyOfRange(ihy, startId, endId - startId);
            }

        } else {
            int startPosition = loopFors.get(idPetli).getGotoHandler().getPosition();
            int startId = getIdByPositionInCode(hashmapPositionId, startPosition) + 1;
            int endId = ihy.length;
            if (endId - startId > 1) {
                myAnswerIs = Arrays.copyOfRange(ihy, startId, endId);
            }
        }
        return myAnswerIs;
    }

    protected static InstructionHandle[] getIhyBeforeForLoop(
            InstructionHandle[] ihy, List<LoopFor> loopFors, int idPetli, HashMap<Integer, Integer> hashmapPositionId) {

        InstructionHandle[] myAnswerIs = null;

        if (idPetli > 0) {
            int startPosition = loopFors.get(idPetli - 1).getLastIfComparator().getPosition();
            int startId = getIdByPositionInCode(hashmapPositionId, startPosition) + 1;

            int endPosition = loopFors.get(idPetli).getLoopStartHandler().getPosition();
            int endId = getIdByPositionInCode(hashmapPositionId, endPosition);

            if (endId - startId > 1) {
                myAnswerIs = Arrays.copyOfRange(ihy, startId, endId - startId);
            }

        } else {
            int startId = 0;
            int endPosition = loopFors.get(0).getFirstInstructionInFor().getPosition();
            int endId = getIdByPositionInCode(hashmapPositionId, endPosition);
            if (endId - startId > 1) {
                myAnswerIs = Arrays.copyOfRange(ihy, startId, endId);
            }
        }
        return myAnswerIs;
    }

    public static int getLastInstructionPosition(List<InstructionHandle> forLoop) {
        return forLoop.get(forLoop.size() - 1).getPosition();
    }

    public static List<LoopFor> getListLoopForItems(InstructionHandle[] ihy,
                                                    HashMap<Integer, Integer> hashmapPositionId) {

        List<LoopFor> allLoopsInMethod = new ArrayList<>();

        for (int i = 0; i < ihy.length; i++) {
            InstructionHandle item = ihy[i];
            if (item.getInstruction() instanceof GOTO) {
                LoopFor temp = new LoopFor();
                temp.setGotoHandler(ihy[i]);
                temp.setIncrementHandler(ihy[i - 1]);
                int idIterator = Integer.parseInt(ihy[i - 1].getInstruction().toString().split(" ")[1]);
                temp.setIdIterator(idIterator);
                allLoopsInMethod.add(temp);
            }
        }

        for (LoopFor item : allLoopsInMethod)
            setHandlerFirstInForAndStartLoop(item, ihy, hashmapPositionId);

        for (LoopFor item : allLoopsInMethod) setHandlerIfAndFirstInLoop(item, ihy, hashmapPositionId);
        verifyValidityAssignedIfToLoop(allLoopsInMethod);

        return allLoopsInMethod;
    }

    private static void setHandlerFirstInForAndStartLoop(LoopFor item, InstructionHandle[] ihy, HashMap<Integer, Integer> hashmapPositionId) {
        int position = Integer.parseInt(item.getGotoHandler().toString().split("->")[1].replace(" ", ""));
        int idStartLoopArray = getIdByPositionInCode(hashmapPositionId, position);
        item.setLoopStartHandler(ihy[idStartLoopArray]);
//        ussualy: iload and istore are 2 instructions before idStartLoopArray
        item.setFirstInstructionInFor(ihy[idStartLoopArray].getPrev().getPrev());
    }

    private static void setHandlerIfAndFirstInLoop(LoopFor item, InstructionHandle[] ihy, HashMap<Integer, Integer> hashmapPositionId) {

        int positionStart = item.getLoopStartHandler().getPosition();
        int idStart = hashmapPositionId.get(positionStart);

        int positionEnd = item.getGotoHandler().getPosition();
        int idEnd = hashmapPositionId.get(positionEnd);

        for (int i = idStart; i < idEnd; i++) {
            Instruction instr = ihy[i].getInstruction();
            if (instr instanceof IF_ICMPGE || instr instanceof IF_ICMPGT ||
                    instr instanceof IF_ICMPLE || instr instanceof IF_ICMPLT ||
                    instr instanceof IF_ICMPEQ || instr instanceof IF_ICMPNE ||
                    instr instanceof IF_ACMPEQ || instr instanceof IF_ACMPNE ||
                    instr instanceof IFLE || instr instanceof IFLT ||
                    instr instanceof IFGE || instr instanceof IFGT ||
                    instr instanceof IFEQ || instr instanceof IFNE) {
                item.setIfCompareHandle(ihy[i]);
                item.setFirstInLoopHandle(ihy[i + 1]);
            }
        }
    }

    private static void verifyValidityAssignedIfToLoop(List<LoopFor> allLoopsInMethod) {
        Collections.reverse(allLoopsInMethod);
        for (int i = 0; i < allLoopsInMethod.size() - 1; i++) {// -1 because compare
            ArrayList<InstructionHandle> outsideLoop = allLoopsInMethod.get(i).getIfcompareHandler();
            ArrayList<InstructionHandle> internalLoop = allLoopsInMethod.get(i + 1).getIfcompareHandler();
            outsideLoop.removeAll(internalLoop);
        }
    }

}
