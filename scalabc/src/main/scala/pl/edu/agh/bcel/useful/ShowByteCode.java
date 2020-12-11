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

        LaunchProperties.CLASS_DIR = "target/classes/org/example/";
        LaunchProperties.CLASS_NAME = "App";
        LaunchProperties.CLASS_METHOD = "main";

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