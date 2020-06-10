/*
This file contains a previous version of the byte code modifier,
and it's the starting point for creating a new version,
is for me like an anchor to the methods that are used.
 */
package mbuilder;

import org.apache.bcel.generic.TargetLostException;
import pl.edu.agh.transformations.ByteCodeModifier;
import java.io.IOException;

public class MainOldEdition {

    public static void main(String[] args) throws IOException, TargetLostException {

        String CLASS_PATH = "src/main/java/mbuilder/classFiles/";
        String CLASS_NAME = "Matrix2D";
        String METHOD_NAME = "Main";

        ByteCodeModifier bcn = new ByteCodeModifier();
        bcn.modifyBytecode(CLASS_PATH, CLASS_NAME, 2, (short) 1000);

    }

}