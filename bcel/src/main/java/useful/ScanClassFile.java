package useful;

//javap -verbose temp.class


import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import pl.edu.agh.transformations.LaunchProperties;

import java.io.IOException;

import static java.util.stream.IntStream.range;

public class ScanClassFile {
    public static void main(String[] argv) throws IOException {
//        LaunchProperties.CLASS_DIR = "target/classes/";
//        LaunchProperties.CLASS_NAME = "temp";

        LaunchProperties.CLASS_DIR = "target/classes/nbody/";
        LaunchProperties.CLASS_NAME = "SerialNbody_itest12";


        JavaClass _analyzedClass = new ClassParser(
                LaunchProperties.CLASS_DIR +
                        LaunchProperties.CLASS_NAME +
                        LaunchProperties.CLASS_SUFFIX).parse();

        System.out.println("Class:\t" + _analyzedClass.getClassName());
        printCode(_analyzedClass.getMethods());

    }

    public static void printCode(Method[] _methods) {
        range(0, _methods.length).forEach(i -> {
            System.out.println(_methods[i]);
            Code code = _methods[i].getCode();
            if (code != null) // Non-abstract method
                System.out.println(code);
        });
    }

}