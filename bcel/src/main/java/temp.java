import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.MethodGen;
import pl.edu.agh.bcel.ByteCodeModifier;
import pl.edu.agh.bcel.LaunchProperties;
import pl.edu.agh.bcel.transformation.Structure;
import pl.edu.agh.bcel.utils.ReadyFields;
import pl.edu.agh.bcel.utils.ReadyMethods;
import pl.edu.agh.bcel.utils.TransformUtils;

import java.io.IOException;


public class temp {


    public static void main(String[] args) throws IOException {
        LaunchProperties.CLASS_DIR = "bcel/target/classes/histogram/";
        LaunchProperties.CLASS_NAME = "SerialHistogramScala";
        LaunchProperties.CLASS_METHOD = "calculate";

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
    }
}
