package mbuilder;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.util.*;

import java.io.IOException;

public class BcelLifier {

    private static String CLASS_NAME;
    private static String CLASS_PATH;
    private static String CLASS_METHOD;

    public static void main(String[] args) throws IOException {

        CLASS_PATH = "src/main/java/mbuilder/classFiles/";
        CLASS_NAME = "Example";

        JavaClass java_class = new ClassParser(
                CLASS_PATH+CLASS_NAME+".class").parse();

        final BCELifier bcelifier = new BCELifier(java_class, System.out);
        bcelifier.start();

    }
}

