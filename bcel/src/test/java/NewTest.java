import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.generic.Type;
import org.junit.Test;
import pl.edu.agh.transformations.ByteCodeModifier;
import pl.edu.agh.transformations.LaunchProperties;
import pl.edu.agh.utils.LocalVariableUtils;
import pl.edu.agh.utils.New;
import pl.edu.agh.utils.TransformUtils;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static pl.edu.agh.utils.LocalVariableUtils.findLocalVariableByName;
import static pl.edu.agh.utils.New.createFieldArray1D;
import static pl.edu.agh.utils.New.createFieldArray2D;

public class NewTest {

    @Test
    public void createArraysAndChcekIDs() throws Exception {

        ByteCodeModifier bcm = new ByteCodeModifier();
        LaunchProperties.CLASS_DIR = "target/classes/matrix/";
        LaunchProperties.CLASS_NAME = "HelloWorld";
        LaunchProperties.CLASS_METHOD = "main";
        bcm.prepareToCreate();

        bcm._il_new.append(bcm._factory.createPrintln("HelloWorld"));
        int id_arrA = createFieldArray2D("A", Type.INT, new int[][]{{1, 2, 3, 4}, {111, 222, 333, 444}}, bcm);

        int id_arrB = createFieldArray1D("B", Type.INT, new int[]{11, 22}, bcm);
        New.printlnArray(id_arrB, bcm);

        int id_arrC = createFieldArray1D("C", Type.INT, new int[]{11, 33, 22}, bcm);
        New.printlnArray(id_arrC, bcm);

        int check_id_arrC = findLocalVariableByName("C", bcm._mg.getLocalVariableTable(bcm._cp)).getIndex();

        bcm.updateMethodBeforeSave();
        bcm.saveNewClassFile();

        assertSame(id_arrC, check_id_arrC);

    }

    @Test
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

    }
}
