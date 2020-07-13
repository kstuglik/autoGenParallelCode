/*
import org.apache.bcel.generic.TargetLostException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

public class IntegrationTest {

    private static final String TEST_CLASS_LOCATION = "src/test/resources/";
    private static final String TEST_CLASS_NAME = "IntegrationTestClass";

    private static final String TEST_CLASS_LOCATION = "../autoparallel-benchmarks/target/classes/parallelized/nbody/";
    private static final String TEST_CLASS_NAME = "SerialNbody";

    private static final int MODIFIED_METHOD_POSITION = 1;

    private static ByteCodeModifier modifier;

    @BeforeClass
    public static void init() {
        modifier = new ByteCodeModifier();
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void modifiedClassRunTest() throws IOException, TargetLostException {
        modifier.modifyBytecode(TEST_CLASS_LOCATION, TEST_CLASS_NAME, MODIFIED_METHOD_POSITION, (short) 1000);
        Runtime runtime = Runtime.getRuntime();
        String cmd = System.getProperty("java.home") + "\\bin\\java -cp " + TEST_CLASS_LOCATION + " " + TEST_CLASS_NAME + BytecodeModifier.MODIFICATION_SUFFIX;
        try {
            Process process = runtime.exec(cmd);
            process.waitFor();
            assertEquals(0, process.exitValue());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void test() throws IOException, TargetLostException {
        modifier.modifyBytecode(TEST_CLASS_LOCATION, TEST_CLASS_NAME, MODIFIED_METHOD_POSITION);
        modifier.modifyBytecode(TEST_CLASS_LOCATION, TEST_CLASS_NAME, 3, (short) 1000);MOVE BODIES METHOD
        Runtime runtime = Runtime.getRuntime();
        String cmd = System.getProperty("java.home") + "/bin/java -cp " + TEST_CLASS_LOCATION + TEST_CLASS_NAME + BytecodeModifier.MODIFICATION_SUFFIX;
        try {
            Process process = runtime.exec(cmd);
            process.waitFor();
            assertEquals(1, process.exitValue());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    String path__to_class_test = "/home/zorro/_projects/mgr/autoGenParallelCode/bcel/target/test-classes/";
    String name_class_test = "Test2";

    @Test
    @SuppressWarnings("Duplicates")
    public void test_2() throws IOException, TargetLostException {
        modifier.modifyBytecode(TEST_CLASS_LOCATION, TEST_CLASS_NAME, MODIFIED_METHOD_POSITION);
        modifier.modifyBytecode(path__to_class_test, name_class_test, 1, (short) 1000);MOVE BODIES METHOD
        Runtime runtime = Runtime.getRuntime();
        String cmd = System.getProperty("java.home") + "/bin/java -cp " + TEST_CLASS_LOCATION + TEST_CLASS_NAME + BytecodeModifier.MODIFICATION_SUFFIX;
        try {
            Process process = runtime.exec(cmd);
            process.waitFor();
            assertEquals(1, process.exitValue());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}*/
