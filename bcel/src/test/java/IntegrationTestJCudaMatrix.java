import org.junit.BeforeClass;
import org.junit.Test;
import pl.edu.agh.transformations.ByteCodeModifier;
import pl.edu.agh.transformations.LaunchProperties;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class IntegrationTestJCudaMatrix {

    private static ByteCodeModifier bcm;

    @BeforeClass
    public static void init() {
        LaunchProperties.CLASS_DIR = "target/classes/matrix/";
//        LaunchProperties.CLASS_NAME = "Multiply2D";
        LaunchProperties.CLASS_NAME = "Multiply2D";
        LaunchProperties.CLASS_METHOD = "multiplyMatrix";
        bcm = new ByteCodeModifier();
    }

    @Test
    public void matrixJCudaTest() throws Exception {

        LaunchProperties.MODIFICATION_SUFFIX = "_ITEST";

        bcm.modifySerialToJCudaMatrix(LaunchProperties.CLASS_DIR, LaunchProperties.CLASS_NAME, (short) 1000);

        Runtime runtime = Runtime.getRuntime();
        String command = System.getProperty("java.home") + "/bin/java -cp " +
                LaunchProperties.CLASS_DIR + " " + LaunchProperties.CLASS_NAME + LaunchProperties.MODIFICATION_SUFFIX;

        try {
            Process process = runtime.exec(command);
            process.waitFor();
            assertEquals(1, process.exitValue());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

/*
    @Test
    public void createArraysAndChcekIDs() throws Exception {

        ByteCodeModifier bcm = new ByteCodeModifier();
        LaunchProperties.CLASS_DIR = "target/classes/matrix/";
        LaunchProperties.CLASS_NAME = "HelloWorld";
        LaunchProperties.CLASS_METHOD = "main";
        bcm.prepareToCreate();

        bcm._il_new.append(bcm._factory.createPrintln("HelloWorld"));
        int id_arrA = createFieldArray2D("A", Type.INT, new int[][]{{1, 2, 3, 4}, {111, 222, 333, 444}}, bcm);
        New.printArray(bcm._il_new,bcm._mg,bcm._factory,id_arrA,true);

        int id_arrB = createFieldArray1D("B", Type.INT, new int[]{11, 22}, bcm);
        int id_arrC = createFieldArray1D("C", Type.INT, new int[]{11, 33, 22}, bcm);
        New.printArray(bcm._il_new,bcm._mg,bcm._factory,id_arrA,false);

        int check_id_arrC = findLocalVariableByName("C", bcm._mg.getLocalVariableTable(bcm._cp)).getIndex();

        bcm.updateMethodBeforeSave();
        bcm.saveNewClassFile();

        assertSame(id_arrC, check_id_arrC);

    }
*/

  /*  @Test
    public void shouldAddJCudaMultiplyCall() throws Exception {

        LaunchProperties.CLASS_DIR = "target/classes/matrix/";
        LaunchProperties.CLASS_NAME = "Multiply";
        LaunchProperties.CLASS_METHOD = "multiplyMatrix";
        LaunchProperties.MODIFICATION_SUFFIX = "_jcuda2";

        ByteCodeModifier bcm = new ByteCodeModifier();
        bcm.prepareToModify();

        TransformUtils.insertNewInstruciton(bcm._modifiedClass, bcm._mg, 4);

        bcm.setLocalVariables();
        LocalVariable var = LocalVariableUtils.findLocalVariableByName("jcm", bcm._mg.getLocalVariableTable(bcm._cp));

        assertTrue(bcm._lvgs[var.getIndex()].getType().toString().contains("JCudaMatrix"));

        bcm.saveNewClassFile();

    }*/

}
