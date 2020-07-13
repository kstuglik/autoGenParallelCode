/*
*   Exampple of use, answer for the question about CLASS_NAME: Arrays
*/

package mbuilder.useful;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.util.BCELifier;
import pl.edu.agh.transformations.LaunchProperties;

import java.io.IOException;
import java.util.Scanner;

public class BcelLifier {

    public static void main(String[] args) throws IOException {
//        LaunchProperties.CLASS_DIR = "src/main/java/mbuilder/classFiles/"; //its default dir
        Scanner in = new Scanner(System.in);
        System.out.println("CLASS_NAME:\t");
        String CLASS_NAME = in.next();

        JavaClass java_class = new ClassParser(LaunchProperties.CLASS_DIR + CLASS_NAME + ".class").parse();

        final BCELifier bcelifier = new BCELifier(java_class, System.out);
        bcelifier.start();

    }
}

