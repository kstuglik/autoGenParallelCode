package pl.edu.agh.bcel.useful;

//javap -verbose temp.class


import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import pl.edu.agh.bcel.LaunchProperties;

import java.io.IOException;

public class ShowByteCode {
    public static void main(String[] argv) throws IOException {

//        LaunchProperties.CLASS_DIR = "target/classes/nbody/";
//        LaunchProperties.CLASS_NAME = "SerialNbody_ITEST";
//        LaunchProperties.CLASS_METHOD = "moveBodies";

//        LaunchProperties.CLASS_DIR = "target/classes/matrix/";
//        LaunchProperties.CLASS_NAME = "SerialMultiplier_ITEST";
//        LaunchProperties.CLASS_METHOD = "multiply";

        LaunchProperties.CLASS_DIR = "target/classes//";
        LaunchProperties.CLASS_NAME = "temp";
        LaunchProperties.CLASS_METHOD = "multiply";

        JavaClass _analyzedClass = new ClassParser(LaunchProperties.getPathToIntputFile()).parse();

        System.out.println("Class:\t" + _analyzedClass.getClassName());
        printCode(_analyzedClass.getMethods(), LaunchProperties.CLASS_METHOD);

    }

    public static void printCode(Method[] methods, String methodName) {
        for (Method method : methods) {
            System.out.println("\n" + method + "\n");
            if (method.getName().contains(methodName)) {
                Code code = method.getCode();
                System.out.println(code);
            }
        }
    }

}