package mbuilder._matrix;


import org.apache.bcel.generic.*;
import pl.edu.agh.transformations.ByteCodeModifier;
import pl.edu.agh.transformations.LaunchProperties;
import pl.edu.agh.utils.New;

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

public class CreatorExample2 {

    public static void main(String[] args) throws IOException, TargetLostException {

        ByteCodeModifier bcm = new ByteCodeModifier();

        LaunchProperties.CLASS_NAME = "MatrixMultiply";
        LaunchProperties.CLASS_METHOD = "main";

        bcm.prepareToCreate();

        bcm._il_new.append(bcm._factory.createPrintln("COLUMN-MAJOR MATRIX, FILL MATRIXS!\n"));

        int id_A = New.createFieldArray1D("A", Type.INT, new int[]{1, 2, 3, 4, 5, 6}, bcm);
        int id_RowsA = New.createFieldLDC(Type.INT, "rowsA", 3, bcm);
        int id_ColsA = New.createFieldLDC(Type.INT, "colsA", 2, bcm);
        New.printArray(id_A, false, bcm);
        int id_B = New.createFieldArray1D("B", Type.INT, new int[]{1, 2, 1, 2, 1, 2}, bcm);
        int id_RowsB = New.createFieldLDC(Type.INT, "rowsB", 2, bcm);
        int id_ColsB = New.createFieldLDC(Type.INT, "colsB", 3, bcm);
        New.printArray(id_B, false, bcm);


        //int width = rowsA * colsB => order of operands: rowsA, colsB, width
        int id_Width = New.createFieldLDC0(Type.INT, "width", bcm);
        New.createSimpleExpression(
                new int[]{id_RowsA, id_ColsB, id_Width},
                new String[]{"imul"}, bcm);


        //double[] C = new double[width];
        int id_C = New.createFieldArrayInit(Type.INT, "C", 1, id_Width, bcm);

        int id_I = New.createFieldLDC(Type.INT, "i", 0, bcm);

//        1. loopStart
        InstructionHandle loopStart = bcm._il_new.append(new LDC(bcm._cp.addInteger(0)));


        int id_J = New.createFieldLDC(Type.INT, "j", 0, bcm);

//        2. loopStart2
        InstructionHandle loopStart2 = bcm._il_new.append(new ILOAD(id_I));
        int id_c = New.createFieldLDC0(Type.INT, "id_c", bcm);
        New.createSimpleExpression(
                new int[]{id_I, id_ColsB, id_J, id_c},
                new String[]{"imul", "iadd"}, bcm);


        int id_K = New.createFieldLDC(Type.INT, "k", 0, bcm);
//        3. loopStart3: body: sum += A[i] * B[j];

        InstructionHandle loopStart3 = bcm._il_new.append(new DLOAD(id_A));

//        int id_a = k * rowsA + i;
        int id_a = New.createFieldLDC0(Type.INT, "id_a", bcm);
        New.createSimpleExpression(
                new int[]{id_K, id_RowsA, id_I, id_a},
                new String[]{"imul", "iadd"}, bcm);

//        int id_b = j * rowsB + k;
        int id_b = New.createFieldLDC0(Type.INT, "id_b", bcm);
        New.createSimpleExpression(
                new int[]{id_J, id_RowsB, id_K, id_b},
                new String[]{"imul", "iadd"}, bcm);


//        C[id_c] += A[id_a] * B[id_b];

        bcm._il_new.append(new DLOAD(id_C));
        bcm._il_new.append(new ILOAD(id_c));
        bcm._il_new.append(new DLOAD(id_A));
        bcm._il_new.append(new ILOAD(id_a));
        bcm._il_new.append(new DALOAD());
        bcm._il_new.append(new DLOAD(id_B));
        bcm._il_new.append(new ILOAD(id_b));
        bcm._il_new.append(new DALOAD());
        bcm._il_new.append(new IMUL());
        bcm._il_new.append(new DLOAD(id_C));
        bcm._il_new.append(new ILOAD(id_c));
        bcm._il_new.append(new DALOAD());
        bcm._il_new.append(new IADD());
        bcm._il_new.append(new DASTORE());


        //3.Compare: for(int i = 0; i < rowsA; ++i)
        New.initCompareForLoop(loopStart3, id_K, id_RowsB, bcm);

        //2.Compare: for(int j = 0; j < colsB; ++j)
        New.initCompareForLoop(loopStart2, id_J, id_ColsB, bcm);

        //1.Compare: for(int i = 0; i < rowsA; ++i)
        New.initCompareForLoop(loopStart, id_I, id_RowsA, bcm);

        New.printArray(id_C, false, bcm);

        bcm.updateMethodBeforeSave();
        bcm.saveNewClassFile();

    }
}