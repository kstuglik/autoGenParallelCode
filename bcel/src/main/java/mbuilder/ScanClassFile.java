package mbuilder;

import org.apache.bcel.classfile.*;

import java.io.IOException;

import static java.util.stream.IntStream.range;

public class ScanClassFile {
    public static void main(String[] argv) {

        String path_to_file = "src/main/java/mbuilder/classFiles/";
        String class_file = "jcudaMatrix.class";

        try {
            JavaClass jclass = new ClassParser(path_to_file+class_file).parse();
            System.out.println("Class:\t"+jclass.getClassName());
            printCode(jclass.getMethods());

        } catch (IOException ex) {ex.printStackTrace(System.err);}
    }

    public static void printCode(Method[] methods) {
        range(0, methods.length).forEach(i -> {
            System.out.println(methods[i]);
            Code code = methods[i].getCode();
            if (code != null) // Non-abstract method
                System.out.println(code);
        });
    }

}