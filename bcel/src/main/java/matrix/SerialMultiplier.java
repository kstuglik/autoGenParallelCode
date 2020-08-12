package matrix;

import java.util.Arrays;

public class SerialMultiplier {
    private static int[][] matrixA;
    private static int[][] matrixB;
    private static int[][] matrixC;
    private static int resultRows;
    private static int resultColumns;

    public SerialMultiplier(int[][] pA, int[][] pB) {
        matrixA = pA;
        matrixB = pB;
        if (matrixA[0].length != matrixB.length) {
            throw new RuntimeException("Cannot perform multiplication because dimensions are not equal.");
        }
        resultRows = matrixA.length;
        resultColumns = matrixB[0].length;
        matrixC = new int[resultRows][resultColumns];
    }

    public static void main(String[] args) {

        int[][] A = {{3, 2, 6}, {0, 4, 1}, {2, 0, 1}};
        int[][] B = {{4}, {3}, {1}};

        SerialMultiplier serialMultiplier = new SerialMultiplier(A, B);
        int[][] C = serialMultiplier.multiply();

        System.out.println(Arrays.deepToString(C));
    }

    public int[][] multiply() {
        for (int rowNum = 0; rowNum < resultRows; rowNum++) {
            for (int colNum = 0; colNum < resultColumns; colNum++) {
                for (int r = 0; r < matrixB.length; r++) {
                    matrixC[rowNum][colNum] += matrixA[rowNum][r] * matrixB[r][colNum];
                }
            }
        }
        return matrixC;
    }

}