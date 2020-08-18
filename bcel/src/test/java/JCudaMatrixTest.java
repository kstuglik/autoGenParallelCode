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
import pl.edu.agh.bcel.utils.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class JCudaMatrixTest {

    private static ByteCodeModifier bcm;

    @BeforeClass
    public static void init() {
        LaunchProperties.CLASS_DIR = "target/classes/matrix/";
        LaunchProperties.CLASS_NAME = "Multiply2D";
        LaunchProperties.CLASS_METHOD = "multiplyMatrix";
        LaunchProperties.MODIFICATION_SUFFIX = "_ITEST";
    }

    @Test
    public void matrixJCudaTest() throws Exception {

        JavaClass analyzedClass = new ClassParser(LaunchProperties.getPathToIntputFile()).parse();
        ClassGen cgTarget = new ClassGen(analyzedClass);

        Method transformedMethod = ByteCodeModifier.getSelectedMethod0(cgTarget, LaunchProperties.CLASS_METHOD);
        MethodGen mg = new MethodGen(transformedMethod, cgTarget.getClassName(), cgTarget.getConstantPool());

        System.out.println("1)\til.lenght = " + mg.getInstructionList().getLength());
        ReadyFields.addFieldsForJcuda(cgTarget);

        System.out.println("2)\til.lenght = " + mg.getInstructionList().getLength());
        ReadyMethods.addMultiplyMethod(cgTarget);

        System.out.println("3)\til.lenght = " + mg.getInstructionList().getLength());
        TransformUtils.addCallJCudaMultiply(cgTarget, mg);

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