/*
The purpose of the program is to transform the main method by inserting
the call responsible for multiplication with use JCUDA
 */

package mbuilder.MyExperiment;

import org.apache.bcel.generic.TargetLostException;
import pl.edu.agh.transformations.MyBcModifier;

import java.io.IOException;

public class tryToAddTimeWrapper {

    private static final String CLASS_PATH = "src/main/java/mbuilder/classFiles/";
    private static final String CLASS_NAME = "Matrix2D";
    private static final String METHOD_NAME = "multiply";

//    private static final String CLASS_NAME = "jcudaMatrixMain";
//    private static final String METHOD_NAME = "main";

    //    private static BytecodeModifier modifier;
    private static MyBcModifier builder;

    public static void main(String[] args) throws IOException, TargetLostException {

        builder = new MyBcModifier();

        builder.modifyBytecode(
                CLASS_PATH,
                CLASS_NAME,
                METHOD_NAME
        );

    }
}