import org.junit.BeforeClass;
import org.junit.Test;
import pl.edu.agh.transformations.ByteCodeModifier;
import pl.edu.agh.transformations.LaunchProperties;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class IntegrationTestParallelMatrix {


    private static ByteCodeModifier bcm;

    @BeforeClass
    public static void init() {
        LaunchProperties.CLASS_DIR = "target/classes/matrix/";
        LaunchProperties.CLASS_NAME = "SerialMultiplier";
        LaunchProperties.CLASS_METHOD = "multiply";
        bcm = new ByteCodeModifier();
    }

    @Test
    public void matrixParallelTest() throws Exception {
//      YOU NEED ADD IMPORT IN OUTPUT FILE: import java.util.concurrent.Callable;
        LaunchProperties.MODIFICATION_SUFFIX = "_ITEST";

        bcm.modifySerialToParallelMatrix(LaunchProperties.CLASS_DIR, LaunchProperties.CLASS_NAME);
//          INSTRUCTIONS BELOW RESPONSIBLE FOR THE COMPILATION *.CLASS IMMEDIATELY BEFORE MODIFICATION
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

}
