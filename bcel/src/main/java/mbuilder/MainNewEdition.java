/*
The solution creates a new file, with fields:
    array A, array B, JCudaMatrix class object with 2 parameters - array references.
The Multiply (Matrix) method was called on the object
    and finally the result was displayed in the console.

An example can be seen in the directory:
    src/main/java/mbuilder/classFiles/example.class
 */

package mbuilder;

import org.apache.bcel.generic.TargetLostException;
import pl.edu.agh.transformations.MyBcModifier;

import java.io.IOException;



public class MainNewEdition {


    private static String CLASS_PATH;
    private static String CLASS_NAME;
    private static String CLASS_METHOD;


    public static void main(final String[] argv) throws IOException, TargetLostException {

        MyBcModifier modifier = new MyBcModifier();

        int choice = 2;

        switch(choice) {

//        EXAMPLE 1) ADD timewrapper into *.class, that already existed
        case 1:
            CLASS_PATH = "src/main/java/mbuilder/classFiles/";
            CLASS_NAME = "Matrix2D";
            CLASS_METHOD = "multiply";

            modifier.SetParameters(CLASS_PATH,CLASS_NAME,CLASS_METHOD);
            modifier.WrapperTime();

            break;

//        EXAMPLE 2) CREATE JCUDAMATRIX2D - new *.class file
        case 2:
            CLASS_PATH = "src/main/java/mbuilder/classFiles/";
            CLASS_NAME = "CreateJCudaMatrix2D";
            CLASS_METHOD = "main";

            modifier.SetParameters(CLASS_PATH,CLASS_NAME,CLASS_METHOD);
            modifier.CreateJCudaMatrix2D();

            break;

//        EXAMPLE 3) ADD JCudaMatrix2D_multiply() into *.class, combination of examples: 1 and 2
        case 3:
            CLASS_PATH = "src/main/java/mbuilder/classFiles/";
            CLASS_NAME = "Matrix2D";
            CLASS_METHOD = "multiply";

            modifier.SetParameters(CLASS_PATH,CLASS_NAME,CLASS_METHOD);
            modifier.WrapperMatrix();
            break;

        default:
            System.out.println("Wrong value, set correct value for variable choice!");
            break;
        }

    }

}