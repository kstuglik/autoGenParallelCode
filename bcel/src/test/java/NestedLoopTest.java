import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.MethodGen;
import org.junit.BeforeClass;
import org.junit.Test;
import pl.edu.agh.bcel.ByteCodeModifier;
import pl.edu.agh.bcel.LaunchProperties;
import pl.edu.agh.bcel.transformation.Structure;

/*
ABOUT:
    ADD: import java.util.concurrent.Callable; to result
    USE: bcel/ in path if needed
 */

public class NestedLoopTest {
    @BeforeClass
    public static void init() {
        LaunchProperties.CLASS_DIR = "target/classes/";
        LaunchProperties.CLASS_NAME = "NestedLoops1";
        LaunchProperties.CLASS_METHOD = "main";
    }

    @Test
    public void nestedLoopTest() throws Exception {
        JavaClass analyzedClass = new ClassParser(LaunchProperties.getPathToIntputFile()).parse();
        ClassGen cgTarget = new ClassGen(analyzedClass);

        Method transformedMethod = ByteCodeModifier.getSelectedMethod0(cgTarget, LaunchProperties.CLASS_METHOD);
        MethodGen mg = new MethodGen(transformedMethod, cgTarget.getClassName(), cgTarget.getConstantPool());

        Structure.selectBaseCase(cgTarget, mg, "");

        System.out.println("\n\nGOTO: " + LaunchProperties.getPathToOutputFile());
        cgTarget.getJavaClass().dump(LaunchProperties.getPathToOutputFile());
    }
}