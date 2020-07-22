package pl.edu.agh.utils;

import org.apache.bcel.generic.*;
import pl.edu.agh.transformations.LaunchProperties;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LoopUtils {

    private static final int START_CONDITION_INSTRUCTION_INDEX = 0;
    private static final int END_CONDITION_INSTRUCTION_INDEX = 3;

    public static InstructionHandle[] getForLoop(MethodGen mg) throws IllegalArgumentException{
        InstructionHandle ih = getGoto(mg.getInstructionList().getInstructionHandles());
        int startPosition = ((BranchHandle) ih).getTarget().getPrev().getPrev().getPosition();
        int endPosition = ih.getPosition();
        return getInstructionsBetweenPositions(mg.getInstructionList().getInstructionHandles(), startPosition, endPosition);
    }

    static InstructionHandle getGoto(InstructionHandle[] ih) {
        return Arrays.stream(ih)
                .filter(handle -> LaunchProperties.GOTO_INSTRUCTION_NAME.equals(handle.getInstruction().getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Method passed to 'getForLoop' does not have for loop."));
    }

    private static InstructionHandle[] getInstructionsBetweenPositions(InstructionHandle[] allih, int start, int end) {
        return Arrays.stream(allih)
                .filter(instr -> isBetweenPositions(instr, start, end))
                .toArray(InstructionHandle[]::new);
    }

    private static boolean isBetweenPositions(InstructionHandle ih, int start, int end) {
        int ihPosition = ih.getPosition();
        return ihPosition >= start && ihPosition <= end;
    }

    public static int getForLoopVariableIndex(InstructionHandle[] loopih) {
        return ((StoreInstruction) (loopih[1].getInstruction())).getIndex();
    }

    public static void updateLoopVariableIndex(InstructionHandle[] loopih, int newSlot) {
        StoreInstruction forLoopStoreInstruction = (StoreInstruction) loopih[1].getInstruction();
        int oldSlot = forLoopStoreInstruction.getIndex();
        Arrays.stream(loopih)
                .map(InstructionHandle::getInstruction)
                .forEach(instr -> updateSingleInstructionILoopVariableIndex(instr, oldSlot, newSlot));
    }

    private static void updateSingleInstructionILoopVariableIndex(Instruction ih, int oldSlot, int newSlot) {
        if (ih instanceof LocalVariableInstruction) {
            LocalVariableInstruction lvi = (LocalVariableInstruction) ih;
            if (lvi.getIndex() == oldSlot) {
                lvi.setIndex(newSlot);
            }
        }
    }

    public static void broadenCompareCondition(InstructionHandle[] loopih) {
        List<InstructionHandle> handles = Arrays.stream(loopih)
                .filter(BranchHandle.class::isInstance)
                .filter(handle -> !(handle.getInstruction() instanceof GOTO))
                .collect(Collectors.toList());
        if (handles.size() != 1) {
            throw new IllegalStateException("Number of compare ihs in loop condition is different than 1.");
        }
        BranchHandle bh = (BranchHandle) handles.get(0);
        BranchInstruction branchInstruction = (BranchInstruction) bh.getInstruction();
        if (branchInstruction instanceof IF_ICMPGE) {
            bh.setInstruction(new IF_ICMPGT(bh.getTarget()));
        }
        if (branchInstruction instanceof IF_ICMPLE) {
            bh.setInstruction(new IF_ICMPLT(bh.getTarget()));
        }
    }

    public static void updateLoopStartCondition(InstructionHandle[] loopih, int startVariableIndex) {
        loopih[START_CONDITION_INSTRUCTION_INDEX].setInstruction(new ILOAD(startVariableIndex));
    }

    public static void updateLoopEndCondition(InstructionHandle[] loopih, int endVariableIndex) {
        loopih[END_CONDITION_INSTRUCTION_INDEX].setInstruction(new ALOAD(endVariableIndex));
    }

    public static InstructionHandle[] emptyLoop(InstructionHandle[] loopih) {
        InstructionHandle[] conditionInstructions = Arrays.copyOf(loopih, 5);
        InstructionHandle[] closingInstructions = Arrays.copyOfRange(loopih, loopih.length - 2, loopih.length);
        return Stream.concat(Arrays.stream(conditionInstructions), Arrays.stream(closingInstructions))
                .toArray(InstructionHandle[]::new);
    }

    public static void emptyMethodLoop(MethodGen mg, InstructionHandle[] forLoop) {
        InstructionList modifiedInstructionList = new InstructionList();

        appendInstructionsUntilLoopStart(modifiedInstructionList, mg.getInstructionList().getInstructionHandles(), forLoop[0]);
        appendAll(modifiedInstructionList, emptyLoop(forLoop));
        appendInstructionsAfterLoop(modifiedInstructionList,

                mg.getInstructionList().getInstructionHandles(), forLoop[forLoop.length - 1]);

        retargetEmptyLoopEndCondition(modifiedInstructionList);

        mg.setInstructionList(modifiedInstructionList);
        adjustLocalVariableTable(mg);
        mg.setMaxStack();
        mg.setMaxLocals();
        mg.removeLineNumbers();
    }

    private static void appendInstructionsUntilLoopStart(InstructionList ih, InstructionHandle[] allih, InstructionHandle loopStartInstruction) {
        for (InstructionHandle handle : allih) {
            if (handle == loopStartInstruction) {
                break;
            } else {
                appendSingle(ih, handle);
            }
        }
    }

    private static void appendSingle(InstructionList ih, InstructionHandle handle) {
        if (handle instanceof BranchHandle) {
            BranchHandle branch = (BranchHandle) handle;
            ih.append((BranchInstruction) branch.getInstruction().copy());
        } else {
            ih.append(handle.getInstruction().copy());
        }
    }

    private static void appendAll(InstructionList ih, InstructionHandle[] allih) {
        for (InstructionHandle handle : allih) {
            appendSingle(ih, handle);
        }
    }

    private static void appendInstructionsAfterLoop(InstructionList ih, InstructionHandle[] allih, InstructionHandle loopEndInstruction) {
        int lastLoopInstructionPosition = Arrays.asList(allih).indexOf(loopEndInstruction);
        if (lastLoopInstructionPosition == allih.length - 1) {
            return;
        }
        for (int i = lastLoopInstructionPosition + 1; i < allih.length; i++) {
            appendSingle(ih, allih[i]);
        }
    }

    private static void retargetEmptyLoopEndCondition(InstructionList modifiedInstructionList) {
        InstructionHandle[] ih = modifiedInstructionList.getInstructionHandles();
        InstructionHandle gotoHandle = LoopUtils.getGoto(ih);
        InstructionHandle firstHandleAfterLoop = gotoHandle.getNext();
        InstructionHandle lastLoopHandle = gotoHandle.getPrev().getPrev();
        if (lastLoopHandle instanceof BranchHandle) {
            ((BranchHandle) lastLoopHandle).setTarget(firstHandleAfterLoop);
        } else {
            throw new IllegalStateException("Branch handle is not last ih of for loop");
        }
    }

    private static void adjustLocalVariableTable(MethodGen mg) {
        InstructionHandle[] ih = mg.getInstructionList().getInstructionHandles();
        List<Integer> indexes = Arrays.stream(ih)
                .filter(handle -> handle.getInstruction() instanceof LocalVariableInstruction)
                .map(handle -> ((LocalVariableInstruction) handle.getInstruction()).getIndex())
                .collect(Collectors.toList());
        Arrays.stream(mg.getLocalVariables())
                .filter(lg -> indexes.contains(lg.getIndex()))
                .forEach(lg -> adjustLength(lg, ih));
    }

    private static void adjustLength(LocalVariableGen lg, InstructionHandle[] ih) {
        InstructionHandle start = InstructionUtils.findByInstruction(lg.getStart().getInstruction(), ih);
        InstructionHandle end = InstructionUtils.findByInstruction(lg.getEnd().getInstruction(), ih);
        lg.setStart(start);
        lg.setEnd(end);
    }

    public static void retargetLoopInInstructionsToFirstAfterLoop(MethodGen mg) {
        InstructionHandle[] forLoop = getForLoop(mg);
        InstructionHandle firstInstructionAfterLoop = getGoto(mg.getInstructionList().getInstructionHandles()).getNext();
        retargetLoopToInstruction(forLoop, firstInstructionAfterLoop);
    }

    private static void retargetLoopToInstruction(InstructionHandle[] forLoop, InstructionHandle firstInstructionAfterLoop) {
        Arrays.stream(forLoop)
                .filter(handle -> handle.getInstruction() instanceof IfInstruction)
                .map(BranchHandle.class::cast)
                .forEach(handle -> handle.setTarget(firstInstructionAfterLoop));
    }

    public static int getFirstInstructionPosition(List<InstructionHandle> forLoop) {
        return forLoop.get(0).getPosition();
    }

    public static int getLastInstructionPosition(List<InstructionHandle> forLoop) {
        return forLoop.get(forLoop.size() - 1).getPosition();
    }

}
