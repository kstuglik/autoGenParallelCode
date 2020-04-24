import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.ClassGen;
import java.util.Arrays;

import java.io.IOException;

public class ScanClassFile {
    public static void main(String[] argv) {

        String path_to_file = "target/classes/MatrixBuilder/";
        String class_file = "Part1.class";
        String method_file = "Part1";
        int index;
        try {
            JavaClass jclass;
            jclass = new ClassParser(path_to_file+class_file).parse();
            ClassGen cg = new ClassGen(jclass);

            System.out.println("Class:\t"+jclass.getClassName());
            printCode(jclass.getMethods());

        } catch (IOException ex) {ex.printStackTrace(System.err);}
    }

    public static void printCode(Method[] methods) {
        for (int i = 0; i < methods.length; i++) {
            System.out.println(methods[i]);

            Code code = methods[i].getCode();
            if (code != null) // Non-abstract method
                System.out.println(code);
        }
    }
}