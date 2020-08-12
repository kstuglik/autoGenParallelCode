package matrix;

import java.util.Arrays;

public class Matrix2D {
    private int[][] A, B, C;
    private int resultRows, resultColumns;

    public Matrix2D(int[][] pA, int[][] pB) {
        this.A = pA;
        this.B = pB;
        if (A[0].length != B.length) {
            throw new RuntimeException("Cannot perform multiplication because dimensions are not equal.");
        }
        resultRows = A.length;
        resultColumns = B[0].length;
        C = new int[resultRows][resultColumns];
    }


    public static void main(String[] args) {

        int[][] A = {{3, 2, 0}, {0, 4, 1}, {2, 0, 1}};
        int[][] B = {{4}, {3}, {1}};
        int rows_C, cols_C;

        if (A[0].length != B.length) {
            throw new RuntimeException("Dims aren't equal.");
        }

        rows_C = A.length;
        cols_C = B[0].length;
        int[][] C = new int[rows_C][cols_C];

        for (int rowNum = 0; rowNum < rows_C; rowNum++) {
            for (int colNum = 0; colNum < cols_C; colNum++) {
                for (int r = 0; r < B.length; r++) {
                    C[rowNum][colNum] += A[rowNum][r] * B[r][colNum];
                }
            }
        }
        System.out.println(Arrays.deepToString(C));
    }

        public int[][] multiply() {
        for (int rowNum = 0; rowNum < resultRows; rowNum++) {
            for (int colNum = 0; colNum < resultColumns; colNum++) {
                for (int r = 0; r < B.length; r++) {
                    C[rowNum][colNum] += A[rowNum][r] * B[r][colNum];
                }
            }
        }
        return C;
    }
}

