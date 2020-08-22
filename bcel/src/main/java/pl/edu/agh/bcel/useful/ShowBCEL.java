/*
 *   Exampple of use, answer for the question about CLASS_NAME: Arrays
 */

package pl.edu.agh.bcel.useful;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.util.BCELifier;
import pl.edu.agh.bcel.LaunchProperties;

import java.io.IOException;

public class ShowBCEL {

    public static void main(String[] args) throws IOException {

        LaunchProperties.CLASS_DIR = "target/classes/";
        LaunchProperties.CLASS_NAME = "temp";
        LaunchProperties.CLASS_METHOD = "main";

//        LaunchProperties.CLASS_DIR = "target/classes/matrix/";
//        LaunchProperties.CLASS_NAME = "SerialMultiplier";
//        LaunchProperties.CLASS_METHOD = "multiply";

//        LaunchProperties.CLASS_DIR = "target/classes/nbody/";
//        LaunchProperties.CLASS_NAME = "SerialNbody_T1";
//        LaunchProperties.CLASS_METHOD = "moveBodies";

//        Scanner _in = new Scanner(System.in);
//        System.out.println("CLASS_NAME:\t");
//        String CLASS_FILE = _in.next();

        JavaClass analyzedClass = new ClassParser(LaunchProperties.getPathToIntputFile()).parse();

        final BCELifier bcelifier = new BCELifier(analyzedClass, System.out);
        bcelifier.start();

    }
}

