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

        LaunchProperties.CLASS_DIR = "target/classes/org/example/";
        LaunchProperties.CLASS_NAME = "App$";
        LaunchProperties.CLASS_METHOD = "main";

        JavaClass analyzedClass = new ClassParser(LaunchProperties.getPathToIntputFile()).parse();

        final BCELifier bcelifier = new BCELifier(analyzedClass, System.out);
        bcelifier.start();

    }
}

