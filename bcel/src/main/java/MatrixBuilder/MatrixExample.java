package MatrixBuilder;

import java.util.Arrays;

public class MatrixExample {

    public static void main(String[] args) {
        //columnar representation of the matrix

        int[] A = {1,4,2,5,3,6};
        int colsA = 3;
        int rowsA = 2;

        int[] B = {7,9,11,8,10,12};
        int colsB = 2;
        int rowsB = 3;


        int width = rowsA * colsB;
        int[] C = new int[width];

        for(int i = 0; i < rowsA; ++i) {
            for(int j = 0; j < colsB; ++j) {
                int id_c = i * colsB + j;

                for(int k = 0; k < rowsB; ++k) {
                    int id_a = k * rowsA + i;
                    int id_b = j * rowsB + k;
                    C[id_c] += A[id_a] * B[id_b];
                }
            }
        }

        System.out.print(Arrays.toString(C));
    }
}