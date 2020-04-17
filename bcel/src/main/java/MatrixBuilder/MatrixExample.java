package MatrixBuilder;

import java.util.Arrays;

public class MatrixExample {

    public static void main(String[] args) {
        int[][] A = new int[][]{{1, 2, 3}, {4, 5, 6}};
        int[][] B = new int[][]{{7, 8}, {9, 10}, {11, 12}};

        int r1 = A.length;
        int c1 = A[0].length;
        int r2 = B.length;
        int c2 = B[0].length;

        int[][] C = new int[r1][c2];

        for(int i = 0; i < r1; ++i) {
            for(int j = 0; j < c2; ++j) {
                C[i][j] = 0;

                for(int k = 0; k < r2; ++k) {
                    C[i][j] += A[i][k] * B[k][j];
                }
            }
        }

        System.out.println(Arrays.deepToString(C));
    }
}

