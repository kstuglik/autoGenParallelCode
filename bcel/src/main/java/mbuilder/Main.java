package mbuilder;

import pl.edu.agh.transformations.ByteCodeModifier;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {

        ByteCodeModifier bcm = new ByteCodeModifier();
//        bcm.setTarget(false);
//        bcm.setTarget(true);
        bcm.setTarget(3,4);
        bcm.initialize();
        bcm.transformation();
        bcm.saveNewClassFile();

    }

}