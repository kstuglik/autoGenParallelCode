import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.TargetLostException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import pl.edu.agh.bcel.transformations.ReadyFieldsMethods;
import pl.edu.agh.bcel.transformations.TransformUtils;
import pl.edu.agh.bcel.transformations.utils.ByteCodeModifier;
import pl.edu.agh.bcel.transformations.utils.LaunchProperties;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.junit.Assert.assertEquals;

public class ParallelNBodyTest {

    @BeforeClass
    public static void init() {
        LaunchProperties.CLASS_DIR = "target/classes/nbody/";
        LaunchProperties.CLASS_NAME = "SerialNbody";
        LaunchProperties.CLASS_METHOD = "moveBodies";
        LaunchProperties.MODIFICATION_SUFFIX = "_ITEST";

        LaunchProperties.SIZE_OF_PROBLEM = 1000;
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

    @Test
    public void nbodyParallelTest() throws Exception {

        JavaClass analyzedClass = new ClassParser(LaunchProperties.getPathToIntputFile()).parse();
        ClassGen cgTarget = new ClassGen(analyzedClass);

        Method transformedMethod = ByteCodeModifier.getSelectedMethod0(cgTarget, LaunchProperties.CLASS_METHOD);
        MethodGen mg = new MethodGen(transformedMethod, cgTarget.getClassName(), cgTarget.getConstantPool());

        System.out.println("1)\til.lenght = " + mg.getInstructionList().getLength());
        TransformUtils.addThreadPoolExecutorService(cgTarget);

        System.out.println("2)\til.lenght = " + mg.getInstructionList().getLength());
        TransformUtils.initFieldExecutorService(cgTarget, mg);

        System.out.println("3)\til.lenght = " + mg.getInstructionList().getLength());
        TransformUtils.copyLoopToSubTaskMethod(cgTarget, mg, LaunchProperties.LOOP_ITERATOR_NAME);

        System.out.println("4)\til.lenght = " + mg.getInstructionList().getLength());
        TransformUtils.changeLoopLimitToNumberOfThreads(cgTarget, mg);

        System.out.println("5)\til.lenght = " + mg.getInstructionList().getLength());
        TransformUtils.addFieldTaskPool(cgTarget, mg);

        System.out.println("6)\til.lenght = " + mg.getInstructionList().getLength());
        TransformUtils.removeBodyForLoopInSelectedMethod(cgTarget, mg);

        System.out.println("7)\til.lenght = " + mg.getInstructionList().getLength());
        TransformUtils.setNewLoopBody(cgTarget, mg, LaunchProperties.SIZE_OF_PROBLEM);

        System.out.println("8)\til.lenght = " + mg.getInstructionList().getLength());
        TransformUtils.addTryCatchService(cgTarget, mg);

        System.out.println("9)\til.lenght = " + mg.getInstructionList().getLength());
        ReadyFieldsMethods.addMethodToInitTaskPool(cgTarget);

        System.out.println("GOTO: " + LaunchProperties.getPathToOutputFile());
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

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

}
