import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.MethodGen;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import pl.edu.agh.bcel.ByteCodeModifier;
import pl.edu.agh.bcel.LaunchProperties;
import pl.edu.agh.bcel.transformation.Structure;
import pl.edu.agh.bcel.utils.ReadyFields;
import pl.edu.agh.bcel.utils.ReadyMethods;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class ParallelMatrixTest {


    @BeforeClass
    public static void init() {
        LaunchProperties.CLASS_DIR = "target/classes/matrix/";
        LaunchProperties.CLASS_NAME = "SerialMultiplier";
        LaunchProperties.CLASS_METHOD = "multiply";
        LaunchProperties.MODIFICATION_SUFFIX = "_ITEST";
    }


//      ADD import: java.util.concurrent.Callable;

    @Test
    public void matrixParallelTest() throws Exception {

        JavaClass analyzedClass = new ClassParser(LaunchProperties.getPathToIntputFile()).parse();
        ClassGen cgTarget = new ClassGen(analyzedClass);

        Method transformedMethod = ByteCodeModifier.getSelectedMethod0(cgTarget, LaunchProperties.CLASS_METHOD);
        MethodGen mg = new MethodGen(transformedMethod, cgTarget.getClassName(), cgTarget.getConstantPool());

//        ReadyFields.addThreadPoolExecutorService(cgTarget);
        ReadyFields.addStaticFields(cgTarget);
        ReadyMethods.addMethodSetStep(cgTarget);
        ReadyMethods.addMethodSetStop(cgTarget);
        ReadyMethods.addMethodToInitTaskPool(cgTarget);

        ReadyFields.addFieldStep(cgTarget, mg);
        ReadyFields.addFieldTaskPool(cgTarget, mg);
        ReadyFields.initFieldExecutorService(cgTarget, mg);

        Structure.selectBaseCase(cgTarget, mg, "matrix");


        System.out.println("\n\nGOTO: " + LaunchProperties.getPathToOutputFile());
        cgTarget.getJavaClass().dump(LaunchProperties.getPathToOutputFile());

        try {
            ProcessBuilder processBuilder = new ProcessBuilder();

            // -- Windows:  processBuilder.command("cmd.exe", "/c", ....);
            processBuilder.command("bash", "-c", "ls -dq " + LaunchProperties.getPathToOutputFile() + " | wc -l");

            Process process = processBuilder.start();

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

            int result = Integer.parseInt(stdInput.readLine());

            process.waitFor();
            Assert.assertEquals(1, result);
            Assert.assertEquals(1, result);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

}
