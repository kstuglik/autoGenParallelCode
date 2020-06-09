package mbuilder.TestAutoParallel;

import org.apache.bcel.generic.TargetLostException;
import pl.edu.agh.transformations.ByteCodeModifier;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, TargetLostException {
        String CLASS_PATH = "src/main/java/mbuilder/classFiles/";
        String CLASS_NAME = "Matrix2D";
        String METHOD_NAME = "Main";

        ByteCodeModifier bcn = new ByteCodeModifier();
        bcn.modifyBytecode(CLASS_PATH, CLASS_NAME, 2, (short) 1000);

    }

}
