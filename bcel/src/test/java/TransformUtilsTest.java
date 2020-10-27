/**
 * OLD TEST: update is required
 */

import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import pl.edu.agh.bcel.ByteCodeModifier;
import pl.edu.agh.bcel.LaunchProperties;
import pl.edu.agh.bcel.utils.LoopUtilsOld;
import pl.edu.agh.bcel.utils.*;
import pl.edu.agh.bcel.utils.VariableUtils;

import java.io.IOException;


@FixMethodOrder()
public class TransformUtilsTest {

    @BeforeClass
    public static void init() {
        LaunchProperties.CLASS_DIR = "target/classes/nbody/";
        LaunchProperties.CLASS_METHOD = "moveBodies";
    }

    @Test
    public void test_1() throws Exception {
//        shouldInjectThreadPoolToClass
        LaunchProperties.CLASS_NAME = "SerialNbody";
        LaunchProperties.MODIFICATION_SUFFIX = "_T1";

        JavaClass analyzedClass = new ClassParser(LaunchProperties.getPathToIntputFile()).parse();
        ClassGen cgTarget = new ClassGen(analyzedClass);

        Method transformedMethod = ByteCodeModifier.getSelectedMethod0(cgTarget, LaunchProperties.CLASS_METHOD);
        MethodGen mg = new MethodGen(transformedMethod, cgTarget.getClassName(), cgTarget.getConstantPool());

//        ReadyFields.addThreadPoolExecutorService(cgTarget);
        Field[] fields = cgTarget.getFields();

        System.out.println("GOTO: " + LaunchProperties.getPathToOutputFile());
        cgTarget.getJavaClass().dump(LaunchProperties.getPathToOutputFile());

        Assert.assertTrue(VariableUtils.checkIfFieldExist(fields, LaunchProperties.NUMBER_OF_THREADS_NAME));
        Assert.assertTrue(VariableUtils.checkIfFieldExist(fields, LaunchProperties.EXECUTOR_SERVICE_NAME));

    }

    @Test
    public void test_2() throws IOException {
//        shouldInjectTaskPoolToClass
        LaunchProperties.CLASS_NAME = "SerialNbody_T1";
        LaunchProperties.MODIFICATION_SUFFIX = "_T2";

        JavaClass analyzedClass = new ClassParser(LaunchProperties.getPathToIntputFile()).parse();
        ClassGen cgTarget = new ClassGen(analyzedClass);

        Method transformedMethod = ByteCodeModifier.getSelectedMethod0(cgTarget, LaunchProperties.CLASS_METHOD);
        MethodGen mg = new MethodGen(transformedMethod, cgTarget.getClassName(), cgTarget.getConstantPool());

        ReadyFields.addFieldTaskPool(cgTarget, mg);
        ReadyMethods.addMethodToInitTaskPool(cgTarget);

        System.out.println("GOTO: " + LaunchProperties.getPathToOutputFile());
        cgTarget.getJavaClass().dump(LaunchProperties.getPathToOutputFile());

        LaunchProperties.CLASS_NAME = "SerialNbody_T1_T2";
        JavaClass _analyzedClass = new ClassParser(LaunchProperties.getPathToIntputFile()).parse();

        String signature = "";

        for (Method method : _analyzedClass.getMethods()) {
            if (method.getName().contains(LaunchProperties.CLASS_METHOD)) {
                Code code = method.getCode();
                if (code.toString().contains("Callable")) {
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

        Assert.assertEquals("()LArrayList<Callable<Integer>>;", signature);
    }

    @Test
    public void test_3() throws Exception {
//        shouldCopyLoopToMethod
        LaunchProperties.CLASS_NAME = "SerialNbody_T1_T2";
        LaunchProperties.MODIFICATION_SUFFIX = "_T3";

        JavaClass analyzedClass = new ClassParser(LaunchProperties.getPathToIntputFile()).parse();
        ClassGen cgTarget = new ClassGen(analyzedClass);

        Method transformedMethod = ByteCodeModifier.getSelectedMethod0(cgTarget, LaunchProperties.CLASS_METHOD);
        MethodGen mg = new MethodGen(transformedMethod, cgTarget.getClassName(), cgTarget.getConstantPool());

        int before = cgTarget.getMethods().length;

//        OLD SOLUTION TO REPLACE OR REBUILD
        TransformUtils.copyLoopToSubTaskMethod(cgTarget, mg, LaunchProperties.LOOP_ITERATOR_NAME);

        int after = cgTarget.getMethods().length;

        Assert.assertEquals(before + 1, after);

        System.out.println("GOTO: " + LaunchProperties.getPathToOutputFile());
        cgTarget.getJavaClass().dump(LaunchProperties.getPathToOutputFile());
    }

    //        replace stop condition in loop inside selected(subTask) method with use static field NumThreads
    @Test
    public void test_4() throws Exception {
//        shouldReplaceLoopIndexWithNumberOfThreads
        LaunchProperties.CLASS_NAME = "SerialNbody_T1_T2_T3";
        LaunchProperties.MODIFICATION_SUFFIX = "_T4";

        JavaClass analyzedClass = new ClassParser(LaunchProperties.getPathToIntputFile()).parse();
        ClassGen cgTarget = new ClassGen(analyzedClass);

        Method transformedMethod = ByteCodeModifier.getSelectedMethod0(cgTarget, LaunchProperties.CLASS_METHOD);
        MethodGen mg = new MethodGen(transformedMethod, cgTarget.getClassName(), cgTarget.getConstantPool());

        TransformUtils.changeLoopLimitToNumberOfThreads(cgTarget, mg);

        System.out.println("GOTO: " + LaunchProperties.getPathToOutputFile());
        cgTarget.getJavaClass().dump(LaunchProperties.getPathToOutputFile());

        InstructionHandle[] forLoop = LoopUtilsOld.getForLoop(mg);
//        WHY INDEX: 3?
        Assert.assertTrue(forLoop[3].getInstruction() instanceof GETSTATIC);

    }

    @Test
    public void test_5() throws IOException {
//        shouldThrowWhenNoNumberOfThreadsFieldIsPresent
        LaunchProperties.CLASS_NAME = "SerialNbody";
        LaunchProperties.CLASS_METHOD = "main";
        LaunchProperties.MODIFICATION_SUFFIX = "_T5";

        JavaClass analyzedClass = new ClassParser(LaunchProperties.getPathToIntputFile()).parse();
        ClassGen cgTarget = new ClassGen(analyzedClass);

        Method transformedMethod = ByteCodeModifier.getSelectedMethod0(cgTarget, LaunchProperties.CLASS_METHOD);
        MethodGen mg = new MethodGen(transformedMethod, cgTarget.getClassName(), cgTarget.getConstantPool());


        try {
            TransformUtils.changeLoopLimitToNumberOfThreads(cgTarget, mg);
            System.out.println("GOTO: " + LaunchProperties.getPathToOutputFile());
            cgTarget.getJavaClass().dump(LaunchProperties.getPathToOutputFile());
        } catch (Exception e) {
            System.out.println("************************************* DONE ***********************************");
            System.out.println("IllegalArgumentException: " + (e.getClass() == IllegalArgumentException.class));
            Assert.assertSame(IllegalArgumentException.class, e.getClass());
        }

    }
}
