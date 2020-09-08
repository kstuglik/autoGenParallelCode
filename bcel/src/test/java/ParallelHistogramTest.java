import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.util.SyntheticRepository;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import pl.edu.agh.bcel.ByteCodeModifier;
import pl.edu.agh.bcel.LaunchProperties;
import pl.edu.agh.bcel.transformation.Structure;
import pl.edu.agh.bcel.utils.ReadyFields;
import pl.edu.agh.bcel.utils.ReadyMethods;
import pl.edu.agh.bcel.utils.TransformUtils;
import scala.reflect.internal.util.ScalaClassLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ParallelHistogramTest {

    @BeforeClass
    public static void init() {
        LaunchProperties.CLASS_DIR = "target/classes/histogram/";
        LaunchProperties.CLASS_NAME = "SerialHistogram";
        LaunchProperties.CLASS_METHOD = "calculate";
    }

//      ADD import: java.util.concurrent.Callable;

    @Test
    public void matrixParallelTest() throws Exception {

        JavaClass analyzedClass = new ClassParser(LaunchProperties.getPathToIntputFile()).parse();
        ClassGen cgTarget = new ClassGen(analyzedClass);
//            SyntheticRepository.getInstance(LaunchProperties.getPathToIntputFile());


        Method transformedMethod = ByteCodeModifier.getSelectedMethod0(cgTarget, LaunchProperties.CLASS_METHOD);
        MethodGen mg = new MethodGen(transformedMethod, cgTarget.getClassName(), cgTarget.getConstantPool());

        TransformUtils.addThreadPoolExecutorService(cgTarget);

        ReadyMethods.addMethodToInitTaskPool(cgTarget);
        ReadyMethods.addMethodSetStop(cgTarget);

        ReadyFields.addFieldTaskPool(cgTarget, mg);
        ReadyFields.initFieldExecutorService(cgTarget, mg);


        Structure.caseHistogram(cgTarget, mg);

        LocalVariable[] lvt = mg.getLocalVariableTable(mg.getConstantPool()).getLocalVariableTable();
        for (int i = 0; i < lvt.length; i++) {
            System.out.println("i = " + i + "," + lvt[i]);
        }

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
            Assert.assertEquals(1, result);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

}

