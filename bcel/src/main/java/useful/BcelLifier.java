/*
 *   Exampple of use, answer for the question about CLASS_NAME: Arrays
 */

package useful;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.util.BCELifier;
import pl.edu.agh.transformations.LaunchProperties;

import java.io.IOException;

public class BcelLifier {

    public static void main(String[] args) throws IOException {

        LaunchProperties.CLASS_DIR = "target/classes/";
        LaunchProperties.CLASS_NAME = "temp";

//        LaunchProperties.CLASS_DIR = "target/classes/nbody/";
//        LaunchProperties.CLASS_NAME = "SerialNbody_itest12";
//        Scanner _in = new Scanner(System.in);
//        System.out.println("CLASS_NAME:\t");
//        String CLASS_FILE = _in.next();

        JavaClass __analyzedClass = new ClassParser(
                LaunchProperties.CLASS_DIR +
                        LaunchProperties.CLASS_NAME +
                        LaunchProperties.CLASS_SUFFIX).parse();

        final BCELifier bcelifier = new BCELifier(__analyzedClass, System.out);
        bcelifier.start();

    }
}

