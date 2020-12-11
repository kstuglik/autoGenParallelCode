package pl.edu.agh.bcel.utils;

import org.apache.bcel.generic.*;
import pl.edu.agh.bcel.LaunchProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LoopUtilsOld {

    //    TODO: IT BELOW MUST BE FIXED!
    private static final int START_CONDITION_INSTRUCTION_INDEX = 0;
    private static final int END_CONDITION_INSTRUCTION_INDEX = 3;

    private static void adjustLength(LocalVariableGen lg, InstructionHandle[] ih) {
        InstructionHandle start = InstructionUtils.getInstructionFromHandles(lg.getStart().getInstruction(), ih);
        InstructionHandle end = InstructionUtils.getInstructionFromHandles(lg.getEnd().getInstruction(), ih);
        lg.setStart(start);
        lg.setEnd(end);
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

    private static void appendAll(InstructionList ih, InstructionHandle[] ihy) {
        for (InstructionHandle handle : ihy) {
            appendSingle(ih, handle);
        }
    }

    private static void appendInstructionsAfterLoop(InstructionList ih, InstructionHandle[] ihy, InstructionHandle loopEndInstruction) {
        int lastLoopInstructionPosition = Arrays.asList(ihy).indexOf(loopEndInstruction);
        if (lastLoopInstructionPosition == ihy.length - 1) {
            return;
        }
        for (int i = lastLoopInstructionPosition + 1; i < ihy.length; i++) {
            appendSingle(ih, ihy[i]);
        }
    }

    private static void appendInstructionsUntilLoopStart(InstructionList ih, InstructionHandle[] ihy, InstructionHandle loopStartInstruction) {
        for (InstructionHandle handle : ihy) {
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

    public static void broadenCompareCondition(InstructionHandle[] ihy) {
        List<InstructionHandle> handles = Arrays.stream(ihy)
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

    public static InstructionHandle[] emptyLoop(InstructionHandle[] ihy) {
        InstructionHandle[] conditionInstructions = Arrays.copyOf(ihy, 5);
        InstructionHandle[] closingInstructions = Arrays.copyOfRange(ihy, ihy.length - 2, ihy.length);
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

    public static InstructionHandle[] getForLoop(MethodGen mg) throws IllegalArgumentException {
        InstructionHandle ih = getGoto(mg.getInstructionList().getInstructionHandles());
        int startPosition = ((BranchHandle) ih).getTarget().getPrev().getPrev().getPosition();
        int endPosition = ih.getPosition();
        return getIhsBetweenFromTo(
                mg.getInstructionList().getInstructionHandles(),
                startPosition, endPosition);
    }

    public static int getForLoopVariableIndex(InstructionHandle[] ihy) {
        return ((StoreInstruction) (ihy[1].getInstruction())).getIndex();
    }

    static InstructionHandle getGoto(InstructionHandle[] ih) {
        return Arrays.stream(ih)
                .filter(handle -> "goto".equals(handle.getInstruction().getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Method passed to 'getForLoop' does not have for loop."));
    }

    public static InstructionHandle[] getIhsBetweenFromTo(InstructionHandle[] ihy, int start, int end) {
        return Arrays.stream(ihy)
                .filter(instr -> isBetweenPositions(instr, start, end))
                .toArray(InstructionHandle[]::new);
    }

    private static boolean isBetweenPositions(InstructionHandle ih, int start, int end) {
        int ihPosition = ih.getPosition();
        return ihPosition >= start && ihPosition <= end;
    }

    private static void retargetEmptyLoopEndCondition(InstructionList il) {
        InstructionHandle[] ih = il.getInstructionHandles();
        InstructionHandle gotoHandle = LoopUtilsOld.getGoto(ih);
        InstructionHandle firstHandleAfterLoop = gotoHandle.getNext();
        InstructionHandle lastLoopHandle = gotoHandle.getPrev().getPrev();
        if (lastLoopHandle instanceof BranchHandle) {
            ((BranchHandle) lastLoopHandle).setTarget(firstHandleAfterLoop);
        } else {
            throw new IllegalStateException("Branch handle is not last ih of for loop");
        }
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

    public static void updateLoopEndCondition(InstructionHandle[] ihy, int endVariableIndex) {
        ihy[END_CONDITION_INSTRUCTION_INDEX].setInstruction(new ALOAD(endVariableIndex));
    }

    public static void updateLoopStartCondition(InstructionHandle[] ihy, int startVariableIndex) {
        ihy[START_CONDITION_INSTRUCTION_INDEX].setInstruction(new ILOAD(startVariableIndex));
    }

    public static void updateLoopVariableIndex(InstructionHandle[] ihy, int newSlot) {
        StoreInstruction forLoopStoreInstruction = (StoreInstruction) ihy[1].getInstruction();
        int oldSlot = forLoopStoreInstruction.getIndex();
        Arrays.stream(ihy)
                .map(InstructionHandle::getInstruction)
                .forEach(instr -> updateSingleInstructionILoopVariableIndex(instr, oldSlot, newSlot));
    }

    protected static void updateSingleInstructionILoopVariableIndex(Instruction ih, int oldSlot, int newSlot) {
        if (ih instanceof LocalVariableInstruction) {
            LocalVariableInstruction lvi = (LocalVariableInstruction) ih;
            if (lvi.getIndex() == oldSlot) {
                lvi.setIndex(newSlot);
            }
        }
    }

}
