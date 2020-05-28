/*
[sequential]
The purpose of the program is to generate valid 2D array initiated by new values
 */

package MatrixBuilder;

import org.apache.bcel.generic.TargetLostException;
import java.io.IOException;

public class bcCreateMatrixMain {
    private static final String CLASS_PATH = "src/main/java/MatrixBuilder/generated/";
    private static final String CLASS_NAME = "JCudaMatrixMain2";
    private static final String METHOD_NAME = "Main";

    public static void main(String[] args) throws IOException, TargetLostException {
        bcCreateMatrix newClass = new bcCreateMatrix();
        newClass.modifyBytecode(CLASS_PATH, CLASS_NAME, METHOD_NAME);
    }
}