package intro2useBCEL.useful;

import org.apache.bcel.classfile.*;

import java.io.IOException;

import static java.util.stream.IntStream.range;

public class ScanClassFile {
    public static void main(String[] argv) throws IOException {

        String CLASS_DIR = "src/main/java/mbuilder/classFiles/";
        String CLASS_FILE = "Arr.class";

        JavaClass _analyzedClass = new ClassParser(CLASS_DIR + CLASS_FILE).parse();
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