/**
 * OLD TEST: update is required
 */

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.InstructionHandle;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import pl.edu.agh.bcel.transformations.LoopUtils;
import pl.edu.agh.bcel.transformations.utils.ByteCodeModifier;
import pl.edu.agh.bcel.transformations.utils.LaunchProperties;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.*;

@FixMethodOrder()
public class TransformUtilsTest {

    @BeforeClass
    public static void init() {
        LaunchProperties.CLASS_DIR = "target/classes/nbody/";
        LaunchProperties.CLASS_METHOD = "moveBodies";
    }

    @Test
    public void test0() throws Exception {
//        shouldInjectThreadPoolToClass
        LaunchProperties.CLASS_NAME = "SerialNbody";

        LaunchProperties.MODIFICATION_SUFFIX = "_T1";

        ByteCodeModifier bcm = new ByteCodeModifier();
        bcm.prepareToModify();

        pl.edu.agh.bcel.transformations.TransformUtils.addThreadPoolExecutorService(bcm._modifiedClass);
        ArrayList<String> fields = bcm.getNameFields();

        assertTrue(fields.contains(LaunchProperties.NUMBER_OF_THREADS_NAME));
        assertTrue(fields.contains(LaunchProperties.EXECUTOR_SERVICE_NAME));

        bcm.saveNewClassFile();
    }

    @Test
    public void test1() throws IOException {
//        shouldInjectTaskPoolToClass
        LaunchProperties.CLASS_NAME = "SerialNbody_T1";

        LaunchProperties.MODIFICATION_SUFFIX = "_T2";

        ByteCodeModifier bcm = new ByteCodeModifier();
        bcm.prepareToModify();
        bcm.extraMe();

        pl.edu.agh.bcel.transformations.TransformUtils.addFieldTaskPool(bcm._modifiedClass, bcm._mg);
        bcm.saveNewClassFile();

        LaunchProperties.CLASS_NAME = "SerialNbody_T2";
        JavaClass _analyzedClass = new ClassParser(LaunchProperties.getPathToIntputFile()).parse();

        String signature = "";

        for (Method method : _analyzedClass.getMethods()) {
            if (method.getName().contains(LaunchProperties.CLASS_METHOD)) {
                Code code = method.getCode();
                if (code.toString().contains("Callable")) { // Non-abstract method
                    String[] table = method.getCode().toString().split("\\s+");
                    for (String s : table) {
                        if (s.contains("Callable")) {
                            signature = s;
                            System.out.println("-->\tSIGNATURE WAS FOUND:\t" + signature);
                            break;
                        }
                    }
                }
            }
        }

        assertEquals("()LArrayList<Callable<Integer>>;", signature);
    }

    @Test
    public void test2() throws Exception {
//        shouldCopyLoopToMethod
        LaunchProperties.CLASS_NAME = "SerialNbody_T2";

        LaunchProperties.MODIFICATION_SUFFIX = "_T3";

        ByteCodeModifier bcm = new ByteCodeModifier();
        bcm.prepareToModify();
        bcm.extraMe();

        int before = bcm.getMethodsArray().length;
        pl.edu.agh.bcel.transformations.TransformUtils.copyLoopToSubTaskMethod(bcm._modifiedClass, bcm._mg, LaunchProperties.LOOP_ITERATOR_NAME);

        bcm.setMethodsArray();
        int after = bcm.getMethodsArray().length;

        assertEquals(before + 1, after);

        bcm.saveNewClassFile();
    }

    //        replace stop condition in loop inside selected(subTask) method with use static field NumThreads
    @Test
    public void test3() throws Exception {
//        shouldReplaceLoopIndexWithNumberOfThreads
        LaunchProperties.CLASS_NAME = "SerialNbody_T3";

        LaunchProperties.MODIFICATION_SUFFIX = "_T4";

        ByteCodeModifier bcm = new ByteCodeModifier();
        bcm.prepareToModify();
        bcm.extraMe();

        pl.edu.agh.bcel.transformations.TransformUtils.changeLoopLimitToNumberOfThreads(bcm._modifiedClass, bcm._mg);

        InstructionHandle[] forLoop = LoopUtils.getForLoop(bcm._mg);

        assertTrue(forLoop[3].getInstruction() instanceof GETSTATIC);

        bcm.saveNewClassFile();
    }

    @Test
    public void test4() {
//        shouldThrowWhenNoNumberOfThreadsFieldIsPresent
        LaunchProperties.CLASS_NAME = "SerialNbody_T4";
        LaunchProperties.MODIFICATION_SUFFIX = "_T5";

        ByteCodeModifier bcm = new ByteCodeModifier();

        try {
            bcm.prepareToModify();
            bcm.extraMe();
            pl.edu.agh.bcel.transformations.TransformUtils.changeLoopLimitToNumberOfThreads(bcm._modifiedClass, bcm._mg);
            bcm.saveNewClassFile();
        } catch (Exception e) {
            System.out.println("************************************* DONE ***********************************");
            System.out.println("IllegalStateException: " + (e.getClass() == IllegalStateException.class));

            assertSame(e.getClass(), IllegalStateException.class);
        }

    }
}
