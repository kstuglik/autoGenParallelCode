/*
 *   Exampple of use, answer for the question about CLASS_NAME: Arrays
 */

package intro2useBCEL.useful;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.util.BCELifier;
import pl.edu.agh.transformations.LaunchProperties;

import java.io.IOException;
import java.util.Scanner;

public class BcelLifier {

    public static void main(String[] args) throws IOException {
//        LaunchProperties.CLASS_DIR = "src/main/java/mbuilder/classFiles/"; //its default dir
        Scanner _in = new Scanner(System.in);
        System.out.println("CLASS_NAME:\t");
        String CLASS_FILE = _in.next();

        JavaClass __analyzedClass = new ClassParser(LaunchProperties.CLASS_DIR + CLASS_FILE).parse();

        final BCELifier bcelifier = new BCELifier(__analyzedClass, System.out);
        bcelifier.start();

    }
}

