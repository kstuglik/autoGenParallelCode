/**
 * OLD TEST: update is required (because work only with 1-level for-loop)
 */

import org.apache.bcel.generic.ConstantPushInstruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.LoadInstruction;
import org.junit.BeforeClass;
import org.junit.Test;
import pl.edu.agh.bcel.ByteCodeModifier;
import pl.edu.agh.bcel.LaunchProperties;
import pl.edu.agh.bcel.utils.LoopUtilsOld;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LoopUtilsOldTest {

    //    TO SET IT UP WITH MY WISHES
    private static final int EXPECTED_LOOP_VARIABLE_INDEX = 1;//2;
    private static final int EXPECTED_LOOP_START_CONDITION_INDEX = 99;
    private static final int EXPECTED_LOOP_END_CONDITION_INDEX = 9999;
    static ByteCodeModifier bcm;

    @BeforeClass
    public static void init() throws IOException {
        LaunchProperties.CLASS_DIR = "target/classes/nbody/";
        LaunchProperties.CLASS_NAME = "SerialNbody";
        LaunchProperties.CLASS_METHOD = "moveBodies";
    }

    @Test
    public void shouldChangeComparedVariableInLoop() throws Exception {
        LaunchProperties.CLASS_METHOD = "main";
        bcm = new ByteCodeModifier();
        bcm.prepareToModify();

        InstructionHandle[] loopInstructions = LoopUtilsOld.getForLoop(bcm._mg);
        assertTrue(loopInstructions[0].getInstruction() instanceof ConstantPushInstruction);

        LoopUtilsOld.updateLoopStartCondition(loopInstructions, EXPECTED_LOOP_START_CONDITION_INDEX);

        assertTrue(loopInstructions[0].getInstruction() instanceof LoadInstruction);

        int loadIndex = ((LoadInstruction) loopInstructions[0].getInstruction()).getIndex();

        assertEquals(EXPECTED_LOOP_START_CONDITION_INDEX, loadIndex);
    }

    // update end condition in for loop with use index another variables
    @Test
    public void shouldChangeLoopEndCondition() throws Exception {
        LaunchProperties.CLASS_METHOD = "main";
        LaunchProperties.MODIFICATION_SUFFIX = "_l1";
        bcm = new ByteCodeModifier();
        bcm.prepareToModify();

        InstructionHandle[] loopInstructions = LoopUtilsOld.getForLoop(bcm._mg);
        assertTrue(loopInstructions[3].getInstruction() instanceof ConstantPushInstruction);

        System.out.println("loopInstructions:");
        for (InstructionHandle item : loopInstructions) {
            System.out.println(item.getInstruction().toString());
        }

        System.out.println("loopInstructions[3]: " + loopInstructions[3].getInstruction().getOpcode());

        LoopUtilsOld.updateLoopEndCondition(loopInstructions, EXPECTED_LOOP_END_CONDITION_INDEX);

        System.out.println("loopInstructions:");
        for (InstructionHandle item : loopInstructions) {
            System.out.println(item.getInstruction().toString());
        }

        System.out.println("loopInstructions[3]: " + loopInstructions[3].getInstruction().getOpcode());

        assertTrue(loopInstructions[3].getInstruction() instanceof LoadInstruction);

        int loadIndex = ((LoadInstruction) loopInstructions[3].getInstruction()).getIndex();
        assertEquals(EXPECTED_LOOP_END_CONDITION_INDEX, loadIndex);

        bcm.saveNewClassFile();
    }

    @Test
    public void shouldGetLoopFromMainMethod() throws Exception {
        LaunchProperties.CLASS_METHOD = "main";
        bcm = new ByteCodeModifier();
        bcm.prepareToModify();

        List<InstructionHandle> forLoop = Arrays.asList(LoopUtilsOld.getForLoop(bcm._mg));

        int id_start = 0, id_end = 0;
        id_start = forLoop.get(0).getPosition();
        id_end = forLoop.get(forLoop.size() - 1).getPosition();

        assertTrue(id_start != id_end);
    }

    @Test
    public void shouldMakeEmptyLoopBody() throws Exception {
        LaunchProperties.CLASS_METHOD = "main";
        bcm = new ByteCodeModifier();
        bcm.prepareToModify();

        InstructionHandle[] loopInstructions = LoopUtilsOld.getForLoop(bcm._mg);
        InstructionHandle[] emptyLoop = LoopUtilsOld.emptyLoop(loopInstructions);

        assertTrue(loopInstructions.length != emptyLoop.length);

        System.out.println("number of instructions before update:\t" + loopInstructions.length);
        System.out.println("loopInstructions:");
        for (InstructionHandle item : loopInstructions) {
            System.out.println(item.getInstruction().toString());
        }

        System.out.println("number of instructions after update:\t" + emptyLoop.length);
        System.out.println("loopInstructions:");
        for (InstructionHandle item : loopInstructions) {
            System.out.println(item.getInstruction().toString());
        }

    }

    @Test
    public void shouldReturnTwoAsLoopVariableIndex() throws Exception {
        LaunchProperties.CLASS_METHOD = "main";
        bcm = new ByteCodeModifier();
        bcm.prepareToModify();

        InstructionHandle[] loopInstructions = LoopUtilsOld.getForLoop(bcm._mg);
        int forLoopVariableIndex = LoopUtilsOld.getForLoopVariableIndex(loopInstructions);

        assertEquals(EXPECTED_LOOP_VARIABLE_INDEX, forLoopVariableIndex);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenNoLoopFound() throws IOException {
        LaunchProperties.CLASS_METHOD = "getBodies";
        bcm = new ByteCodeModifier();
        bcm.prepareToModify();

        List<InstructionHandle> forLoop = Arrays.asList(LoopUtilsOld.getForLoop(bcm._mg));
    }

    @Test
    public void shouldUpdateLoopVariableTargetSlot() throws Exception {
        LaunchProperties.CLASS_METHOD = "main";
        bcm = new ByteCodeModifier();
        bcm.prepareToModify();

        InstructionHandle[] loopInstructions = LoopUtilsOld.getForLoop(bcm._mg);
        int oldLoopVariableIndex = LoopUtilsOld.getForLoopVariableIndex(loopInstructions);
        assertEquals(1, oldLoopVariableIndex);
        LoopUtilsOld.updateLoopVariableIndex(loopInstructions, EXPECTED_LOOP_END_CONDITION_INDEX);
        int newLoopVariableIndex = LoopUtilsOld.getForLoopVariableIndex(loopInstructions);
        assertEquals(EXPECTED_LOOP_END_CONDITION_INDEX, newLoopVariableIndex);
    }

//    TO DO SET LOOP START AND END INDEX IN ONE TIME
//    @Test
//    public void shouldSetLoopRange() throws Exception {
//        LaunchProperties.CLASS_METHOD = "main";
//        bcm = new ByteCodeModifier();
//        bcm.prepareToModify();
//
//
//        InstructionHandle[] loopInstructions = LoopUtils.getForLoop(bcm._mg);
//        LoopUtils.setLoopIterationRange(loopInstructions, 16);
//    }

}
