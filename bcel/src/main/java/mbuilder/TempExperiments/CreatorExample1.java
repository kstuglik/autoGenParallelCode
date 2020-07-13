package mbuilder.TempExperiments;

import org.apache.bcel.generic.Type;
import pl.edu.agh.transformations.ByteCodeModifier;
import pl.edu.agh.transformations.LaunchProperties;

import java.io.IOException;

import static pl.edu.agh.transformations.utils.New.*;

public class CreatorExample1 extends ByteCodeModifier {


    public static void main(final String[] argv) throws IOException {

        LaunchProperties.CLASS_NAME = "HelloWorld";
        LaunchProperties.CLASS_METHOD = "main";
        prepareToCreating();



        _il_new.append(_factory.createPrintln("HelloWorld"));
        createFieldArray2D("A", Type.INT, new int[][]{{1, 2, 3, 4}, {111, 222, 333, 444}});
        int id_arr1 = createFieldArray1D("CBA", Type.INT, new int[]{11, 22});
        println_array(id_arr1);
        int id_arr2 = createFieldArray1D("CBAA", Type.INT, new int[]{11, 33, 22});
        println_array(id_arr2);



        updateMethodBeforeSave();
        saveNewClassFile();
    }
}
