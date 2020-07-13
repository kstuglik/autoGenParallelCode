package mbuilder;

import pl.edu.agh.transformations.ByteCodeModifier;
import pl.edu.agh.transformations.LaunchProperties;

import java.io.IOException;

import static pl.edu.agh.transformations.ByteCodeModifier.saveNewClassFile;
import static pl.edu.agh.transformations.ByteCodeModifier.updateMethodBeforeSave;


public class Main {

    public static void main(String[] argv) throws IOException {

        ByteCodeModifier bcm = new ByteCodeModifier();

//        LaunchProperties.CLASS_DIR="CHANGE HERE IF U WANT OR IN CLASS FILE";
        LaunchProperties.CLASS_FILE = "Multiply.class";
        LaunchProperties.CLASS_METHOD = "multiply";
/*        LaunchProperties.CLASS_FILE = "Example.class";
        LaunchProperties.CLASS_METHOD = "doSomething";*/

        bcm.prepareToModifing();
        bcm.transformations(1);

        updateMethodBeforeSave();
        saveNewClassFile();

    }
}

// NBODY example
//LaunchProperties.CLASS_FILE="IntegrationTestClass.class";
//LaunchProperties.CLASS_METHOD="moveBodies";
