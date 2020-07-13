package mbuilder.TempExperiments;


import org.apache.bcel.generic.*;
import pl.edu.agh.transformations.ByteCodeModifier;
import pl.edu.agh.transformations.LaunchProperties;
import pl.edu.agh.transformations.utils.New;

import java.io.IOException;

//////////////////////////////////////////////////////////////
//        for(int i = 0; i < rowsA; ++i) {
//            for(int j = 0; j < colsB; ++j) {
//                int id_c = i * colsB + j;
//
//                for(int k = 0; k < rowsB; ++k) {
//                    int id_a = k * rowsA + i;
//                    int id_b = j * rowsB + k;
//                    C[id_c] += A[id_a] * B[id_b];
//                }
//            }
//        }
/////////////////////////////////////////////////////////////

public class CreatorExample2 extends ByteCodeModifier {

    public static void main(String[] args) throws IOException {

        LaunchProperties.CLASS_NAME = "MatrixMultiply";
        LaunchProperties.CLASS_METHOD = "main";

        prepareToCreating();

        _il_new.append(_factory.createPrintln("COLUMN-MAJOR MATRIX, FILL MATRIXS!\n"));

        int id_A = New.createFieldArray1D("A", Type.INT, new int[]{1, 2, 3, 4, 5, 6});
        int id_RowsA = New.createFieldLDC(Type.INT, "rowsA", 3);
        int id_ColsA = New.createFieldLDC(Type.INT, "colsA", 2);
        New.printArray(id_A, false);
        int id_B = New.createFieldArray1D("B", Type.INT, new int[]{1, 2, 1, 2, 1, 2});
        int id_RowsB = New.createFieldLDC(Type.INT, "rowsB", 2);
        int id_ColsB = New.createFieldLDC(Type.INT, "colsB", 3);
        New.printArray(id_B, false);


        //int width = rowsA * colsB => order of operands: rowsA, colsB, width
        int id_Width = New.createFieldLDC0(Type.INT, "width");
        New.create_simple_expression(
                new int[]{id_RowsA, id_ColsB, id_Width},
                new String[]{"imul"});


        //double[] C = new double[width];
        int id_C = New.createFieldArrayInit(Type.INT, "C", 1, id_Width);

        int id_I = New.createFieldLDC(Type.INT, "i", 0);

//        1. loopStart
        InstructionHandle loopStart = _il_new.append(new LDC(_cp.addInteger(0)));


        int id_J = New.createFieldLDC(Type.INT, "j", 0);

//        2. loopStart2
        InstructionHandle loopStart2 = _il_new.append(new ILOAD(id_I));
        int id_c = New.createFieldLDC0(Type.INT, "id_c");
        New.create_simple_expression(
                new int[]{id_I, id_ColsB, id_J, id_c},
                new String[]{"imul", "iadd"});


        int id_K = New.createFieldLDC(Type.INT, "k", 0);
//        3. loopStart3: body: sum += A[i] * B[j];

        InstructionHandle loopStart3 = _il_new.append(new DLOAD(id_A));

//        int id_a = k * rowsA + i;
        int id_a = New.createFieldLDC0(Type.INT, "id_a");
        New.create_simple_expression(
                new int[]{id_K, id_RowsA, id_I, id_a},
                new String[]{"imul", "iadd"});

//        int id_b = j * rowsB + k;
        int id_b = New.createFieldLDC0(Type.INT, "id_b");
        New.create_simple_expression(
                new int[]{id_J, id_RowsB, id_K, id_b},
                new String[]{"imul", "iadd"});


//        C[id_c] += A[id_a] * B[id_b];

        _il_new.append(new DLOAD(id_C));
        _il_new.append(new ILOAD(id_c));
        _il_new.append(new DLOAD(id_A));
        _il_new.append(new ILOAD(id_a));
        _il_new.append(new DALOAD());
        _il_new.append(new DLOAD(id_B));
        _il_new.append(new ILOAD(id_b));
        _il_new.append(new DALOAD());
        _il_new.append(new IMUL());
        _il_new.append(new DLOAD(id_C));
        _il_new.append(new ILOAD(id_c));
        _il_new.append(new DALOAD());
        _il_new.append(new IADD());
        _il_new.append(new DASTORE());


        //3.Compare: for(int i = 0; i < rowsA; ++i)
        New.init_compare_for_loop(loopStart3, id_K, id_RowsB);

        //2.Compare: for(int j = 0; j < colsB; ++j)
        New.init_compare_for_loop(loopStart2, id_J, id_ColsB);

        //1.Compare: for(int i = 0; i < rowsA; ++i)
        New.init_compare_for_loop(loopStart, id_I, id_RowsA);

        New.printArray(id_C, false);

        updateMethodBeforeSave();
        saveNewClassFile();

    }
}