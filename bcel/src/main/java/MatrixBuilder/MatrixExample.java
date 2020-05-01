package MatrixBuilder;

import java.util.Arrays;

public class MatrixExample {

    public static void main(String[] args) {
        //columnar representation of the matrix

        //EXAMPLE 1
        // int[] A = {3, 0, 2, 2, 4, 0, 0, 1, 1};
        // int colsA = 3;
        // int rowsA = 3;

        // int[] B = {4, 3, 1};
        // int colsB = 1;
        // int rowsB = 3;

        //EXAMPLE 2
        // int[] A = {1, 4, 2, 5, 3, 6};
        // int colsA = 3;
        // int rowsA = 2;

        // int[] B = {10, 20, 30, 11, 21, 31};
        // int colsB = 2;
        // int rowsB = 3;

        //EXAMPLE 3
        // int[] A = {1,4,2,5,3,6};
        // int colsA = 3;
        // int rowsA = 2;

        // int[] B = {7,9,11,8,10,12};
        // int colsB = 2;
        // int rowsB = 3;

        //EXAMPLE 4
        // int[] A = {11,12,13};
        // int colsA = 3;
        // int rowsA = 1;

        // int[] B = {11,21,31,12,22,32,13,23,33};
        // int colsB = 3;
        // int rowsB = 3;

        //EXAMPLE 5
        // int[] A = {1,2,3};
        // int colsA = 3;
        // int rowsA = 1;

        // int[] B = {2,3,4,1,3,1,3,2,2};
        // int colsB = 3;
        // int rowsB = 3;

        //EXAMPLE 6
        int[] A = {1, 3, 7, 2, 4, 6, 3, 5, 4};
        int colsA = 3;
        int rowsA = 3;

        int[] B = {5, 5, 7, 2, 6, 6, 6, 7, 4};
        int colsB = 3;
        int rowsB = 3;

        int[] C = new int[rowsA * colsB];


        if (colsA != rowsB) {
            System.out.println("cols_A != rows_B");
        } else {

            int id_a = 0;
            int id_b = 0;
            int id = 0;

            for (int i = 0; i < rowsA; i++) {
                for (int j = 0; j < colsB; j++) {
                    id = i * colsB + j;

                    for (int k = 0; k < rowsB; k++) {
                        id_a = k * rowsA + i;
                        id_b = j * rowsB + k;

                        C[id] += A[id_a] * B[id_b];
//                        System.out.println(
//                                "ida = " + id_a +", idb = " + id_b +
//                                        ", A[id_a] = " + A[id_a] + ", B[id_b] = " + B[id_b] +
//                                        ", i = " + i + ", j = " + j + ", k = " + k + ", id = " + id
//                        );
                    }

                }
            }
            System.out.println(Arrays.toString(C));
        }
    }
}