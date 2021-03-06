package matrix;

import java.util.Arrays;

public class Matrix1D {

    public static void main(String[] args) {
        //columnar representation of the MBuilder.matrix

        int[] A = {3, 0, 2, 2, 4, 0, 0, 1, 1};
        int colsA = 3;
        int rowsA = 3;

        int[] B = {4, 3, 1};
        int colsB = 1;
        int rowsB = 3;


        int width = rowsA * colsB;
        int[] C = new int[width];

        for (int i = 0; i < rowsA; ++i) {
            for (int j = 0; j < colsB; ++j) {
                int id_c = i * colsB + j;

                for (int k = 0; k < rowsB; ++k) {
                    int id_a = k * rowsA + i;
                    int id_b = j * rowsB + k;
                    C[id_c] += A[id_a] * B[id_b];
                }
            }
        }

        System.out.print(Arrays.toString(C));
    }
}