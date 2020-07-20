import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.InstructionHandle;
import org.junit.BeforeClass;
import org.junit.Test;
import pl.edu.agh.transformations.ByteCodeModifier;
import pl.edu.agh.transformations.LaunchProperties;
import pl.edu.agh.utils.LoopUtils;
import pl.edu.agh.utils.TransformUtils;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class TransformUtilsTests {


    @BeforeClass
    public static void init() {
        LaunchProperties.CLASS_DIR = "target/classes/nbody/";
    }

    @Test
    public void shouldInjectThreadPoolToClass() throws Exception {
        LaunchProperties.CLASS_NAME = "SerialNbody";
        LaunchProperties.CLASS_METHOD = "main";
        LaunchProperties.MODIFICATION_SUFFIX = "_T1";

        ByteCodeModifier bcm = new ByteCodeModifier();
        bcm.prepareToModify();

        TransformUtils.addThreadPool(bcm._modifiedClass);// metoda samodzielna, można od razu zapisywać do pliku
        ArrayList<String> fields = bcm.getNameFields();

        assertTrue(fields.contains(LaunchProperties.NUMBER_OF_THREADS_NAME));
        assertTrue(fields.contains(LaunchProperties.EXECUTOR_SERVICE_NAME));

        bcm.saveNewClassFile();
    }

    @Test
    public void shouldInjectTaskPoolToClass() throws IOException {
        LaunchProperties.CLASS_NAME = "SerialNbody_T1";
        LaunchProperties.CLASS_METHOD = "main";
        LaunchProperties.MODIFICATION_SUFFIX = "_T2";

        ByteCodeModifier bcm = new ByteCodeModifier();
        bcm.prepareToModify();
        bcm.extraMe();

        TransformUtils.addTaskPool(bcm._modifiedClass, bcm._mg);
        ArrayList<String> variables = bcm.getLocalVariables();

        assertTrue(variables.contains(LaunchProperties.TASK_POOL_NAME));
        bcm.saveNewClassFile();
    }

    //    Create subTask method with params: start, end to FOR LOOP, do the same thing
    @Test
    public void shouldCopyLoopToMethod() throws Exception {
        LaunchProperties.CLASS_NAME = "SerialNbody_T2";
        LaunchProperties.CLASS_METHOD = "main";
        LaunchProperties.MODIFICATION_SUFFIX = "_T3";

        ByteCodeModifier bcm = new ByteCodeModifier();
        bcm.prepareToModify();
        bcm.extraMe();

        int before = bcm.getMethodsArray().length;
        TransformUtils.copyLoopToMethod(bcm._modifiedClass, bcm._mg);

        bcm.setMethodsArray();
        int after = bcm.getMethodsArray().length;

        assertEquals(before + 1, after);

        bcm.saveNewClassFile();
    }

//        replace stop condition in loop inside selected(subTask) method with use static field NumThreads
    @Test
    public void shouldReplaceLoopIndexWithNumberOfThreads() throws Exception {
        LaunchProperties.CLASS_NAME = "SerialNbody_T3";
        LaunchProperties.CLASS_METHOD = "main";
        LaunchProperties.MODIFICATION_SUFFIX = "_T4";

        ByteCodeModifier bcm = new ByteCodeModifier();
        bcm.prepareToModify();
        bcm.extraMe();

        TransformUtils.changeLoopLimitToNumberOfThreads(bcm._modifiedClass, bcm._mg);

        InstructionHandle[] forLoop = LoopUtils.getForLoop(bcm._mg);

        assertTrue(forLoop[3].getInstruction() instanceof GETSTATIC);

        bcm.saveNewClassFile();
    }

    @Test
    public void shouldThrowWhenNoNumberOfThreadsFieldIsPresent() {
        LaunchProperties.CLASS_NAME = "SerialNbody_T4";
        LaunchProperties.CLASS_METHOD = "main";
        LaunchProperties.MODIFICATION_SUFFIX = "_T5";

        ByteCodeModifier bcm = new ByteCodeModifier();
        try {
            bcm.prepareToModify();
            bcm.extraMe();
            TransformUtils.changeLoopLimitToNumberOfThreads(bcm._modifiedClass, bcm._mg);
            bcm.saveNewClassFile();
        } catch (Exception e) {
            System.out.println("************************************* DONE ***********************************");
            System.out.println("IllegalStateException: " + (e.getClass() == IllegalStateException.class));

            assertSame(e.getClass(), IllegalStateException.class);
        }

    }
}
