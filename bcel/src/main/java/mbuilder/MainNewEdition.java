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

    public static void main(final String[] argv) throws IOException, TargetLostException {

        MyBcModifier modifier = new MyBcModifier();

//        EXAMPLE 1) ADD timewrapper into *.class, that already existed

        String CLASS_PATH = "src/main/java/mbuilder/classFiles/";
        String CLASS_NAME = "Matrix2D";
        String CLASS_METHOD = "multiply";

        modifier.SetParameters(CLASS_PATH,CLASS_NAME,CLASS_METHOD);
        modifier.WrapperExample(0);

//        EXAMPLE 2) CREATE JCUDAMATRIX2D - new *.class file

//        String CLASS_PATH = "src/main/java/mbuilder/classFiles/";
//        String CLASS_NAME = "Example";
//        String CLASS_METHOD = "main";
//
//        modifier.SetParameters(CLASS_PATH,CLASS_NAME,CLASS_METHOD);
//        modifier.CreateJCudaMatrix2D();

//        EXAMPLE 3) ADD JCudaMatrix2D_multiply() into *.class, combination of examples: 1 and 2
//        modifier.WrapperExample(1);
//
//        String CLASS_PATH = "src/main/java/mbuilder/classFiles/";
//        String CLASS_NAME = "Matrix2D";
//        String CLASS_METHOD = "multiply";
//
//        modifier.SetParameters(CLASS_PATH,CLASS_NAME,CLASS_METHOD);
//        modifier.WrapperExample(1);

    }

}