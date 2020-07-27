import org.apache.bcel.generic.TargetLostException;
import org.junit.BeforeClass;
import org.junit.Test;
import pl.edu.agh.transformations.ByteCodeModifier;
import pl.edu.agh.transformations.LaunchProperties;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class IntegrationTest {

    private static ByteCodeModifier bcm;

    @BeforeClass
    public static void init() {
        LaunchProperties.CLASS_DIR = "target/classes/nbody/";
        LaunchProperties.MODIFICATION_SUFFIX = "_itest";
        LaunchProperties.CLASS_NAME = "SerialNbody";
        LaunchProperties.CLASS_METHOD = "moveBodies";
        bcm = new ByteCodeModifier();
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void modifiedClassRunTest() throws IOException, TargetLostException {
        Runtime runtime = Runtime.getRuntime();
        String command = System.getProperty("java.home") + "/bin/java -cp " +
                LaunchProperties.CLASS_DIR + " " + LaunchProperties.CLASS_NAME + LaunchProperties.MODIFICATION_SUFFIX;

        try {
            Process process = runtime.exec(command);
            process.waitFor();
            assertEquals(1, process.exitValue());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

//TODO:
//  var55.add(() -> subTask(start,finalEnd));
//        BEFORE: GOTO: target/classes/nbody/SerialNbody.class
//        AFTER: GOTO: target/classes/nbody/SerialNbody_ITEST.class
//        CHANGES: methods: moveBodies and subTask

    @Test
    public void test() throws Exception {

        LaunchProperties.MODIFICATION_SUFFIX =  "_ITEST";
//        czy na pewo w bcm czy w transformation
        bcm.modifyBytecode(LaunchProperties.CLASS_DIR, LaunchProperties.CLASS_NAME, (short) 1000);

        Runtime runtime = Runtime.getRuntime();
        String command = System.getProperty("java.home") + "/bin/java -cp " +
                LaunchProperties.CLASS_DIR + " " + LaunchProperties.CLASS_NAME + LaunchProperties.MODIFICATION_SUFFIX + "2";

        try {
            Process process = runtime.exec(command);
            process.waitFor();
            assertEquals(1, process.exitValue());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}
