import org.apache.bcel.generic.ConstantPushInstruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.LoadInstruction;
import org.junit.BeforeClass;
import org.junit.Test;
import pl.edu.agh.transformations.ByteCodeModifier;
import pl.edu.agh.transformations.LaunchProperties;
import pl.edu.agh.utils.LoopUtils;
import pl.edu.agh.utils.New;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LoopUtilsTests {

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
    public void shouldGetLoopFromMainMethod() throws Exception {
        LaunchProperties.CLASS_METHOD = "main";
        bcm = new ByteCodeModifier();
        bcm.prepareToModify();

        List<InstructionHandle> forLoop = Arrays.asList(LoopUtils.getForLoop(bcm._mg));

        int id_start = 0, id_end = 0;
        id_start = forLoop.get(0).getPosition();
        id_end = forLoop.get(forLoop.size()-1).getPosition();

        assertTrue(id_start != id_end);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenNoLoopFound() throws IOException {
        LaunchProperties.CLASS_METHOD = "getBodies";
        bcm = new ByteCodeModifier();
        bcm.prepareToModify();

        List<InstructionHandle> forLoop = Arrays.asList(LoopUtils.getForLoop(bcm._mg));
    }

    @Test
    public void shouldReturnTwoAsLoopVariableIndex() throws Exception {
        LaunchProperties.CLASS_METHOD = "main";
        bcm = new ByteCodeModifier();
        bcm.prepareToModify();

        InstructionHandle[] loopInstructions = LoopUtils.getForLoop(bcm._mg);
        int forLoopVariableIndex = LoopUtils.getForLoopVariableIndex(loopInstructions);

        assertEquals(EXPECTED_LOOP_VARIABLE_INDEX, forLoopVariableIndex);
    }

    @Test
    public void shouldUpdateLoopVariableTargetSlot() throws Exception {
        LaunchProperties.CLASS_METHOD = "main";
        bcm = new ByteCodeModifier();
        bcm.prepareToModify();

        InstructionHandle[] loopInstructions = LoopUtils.getForLoop(bcm._mg);
        int oldLoopVariableIndex = LoopUtils.getForLoopVariableIndex(loopInstructions);
        assertEquals(1, oldLoopVariableIndex);
        LoopUtils.updateLoopVariableIndex(loopInstructions, EXPECTED_LOOP_END_CONDITION_INDEX);
        int newLoopVariableIndex = LoopUtils.getForLoopVariableIndex(loopInstructions);
        assertEquals(EXPECTED_LOOP_END_CONDITION_INDEX, newLoopVariableIndex);
    }

    @Test
    public void shouldChangeComparedVariableInLoop() throws Exception {
        LaunchProperties.CLASS_METHOD = "main";
        bcm = new ByteCodeModifier();
        bcm.prepareToModify();

        InstructionHandle[] loopInstructions = LoopUtils.getForLoop(bcm._mg);
        assertTrue(loopInstructions[0].getInstruction() instanceof ConstantPushInstruction);

        LoopUtils.updateLoopStartCondition(loopInstructions, EXPECTED_LOOP_START_CONDITION_INDEX);

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

        InstructionHandle[] loopInstructions = LoopUtils.getForLoop(bcm._mg);
        assertTrue(loopInstructions[3].getInstruction() instanceof ConstantPushInstruction);

        New.printInstructionFromIHandler(loopInstructions);
        System.out.println("loopInstructions[3]: " + loopInstructions[3].getInstruction().getOpcode());

        LoopUtils.updateLoopEndCondition(loopInstructions, EXPECTED_LOOP_END_CONDITION_INDEX);

        New.printInstructionFromIHandler(loopInstructions);
        System.out.println("loopInstructions[3]: " + loopInstructions[3].getInstruction().getOpcode());

        assertTrue(loopInstructions[3].getInstruction() instanceof LoadInstruction);

        int loadIndex = ((LoadInstruction) loopInstructions[3].getInstruction()).getIndex();
        assertEquals(EXPECTED_LOOP_END_CONDITION_INDEX, loadIndex);

        bcm.saveNewClassFile();
    }

    @Test
    public void shouldMakeEmptyLoopBody() throws Exception {
        LaunchProperties.CLASS_METHOD = "main";
        bcm = new ByteCodeModifier();
        bcm.prepareToModify();

        InstructionHandle[] loopInstructions = LoopUtils.getForLoop(bcm._mg);
        InstructionHandle[] emptyLoop = LoopUtils.emptyLoop(loopInstructions);

        assertTrue(loopInstructions.length != emptyLoop.length);

        System.out.println("number of instructions before update:\t" + loopInstructions.length);
        New.printInstructionFromIHandler(loopInstructions);
        System.out.println("number of instructions after update:\t" + emptyLoop.length);
        New.printInstructionFromIHandler(emptyLoop);
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
