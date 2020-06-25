package utils;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

public class ArrayUtils {

    public static float[] randomFloatArray1D(int N, int range) {
        float[] arr = new float[N];
        Random random = new Random();
        for (int i = 0; i < N; i++) {
            arr[i] = random.nextInt(range);
        }
        return arr;
    }

    public static int[][] randomIntArray2D(int rows, int columns, int range) {
        int[][] A = new int[rows][columns];
        Random gen = new Random();
        for (int rowNum = 0; rowNum < rows; rowNum++) {
            for (int colNum = 0; colNum < columns; colNum++) {
                A[rowNum][colNum] = gen.nextInt(range);
            }
        }
        return A;
    }

    public static float[] flatten(int[][] arr) {
        int rows = arr.length;
        int cols = arr[0].length;
        float[] result = new float[rows*cols];

        for (int row = 0; row < cols; row++){
            for(int col=0; col<rows; col++){
                result[row*rows+col] = arr[col][row];
            }
        }
        System.out.println(Arrays.toString(result));
        return result;
    }

    public static void printMatrix(int[][] A) {
        int rows = A.length;
        int columns = A[0].length;
        for (int rowNum = 0; rowNum < rows; rowNum++) {
            for (int colNum = 0; colNum < columns; colNum++) {
                System.out.printf("%6d", A[rowNum][colNum]);
            }
            System.out.print("\n");
        }
    }

    public static int[] rewrite(float[] data) {
        int[] temp = IntStream.range(0, data.length).map(i -> (int) data[i]).toArray();
        return temp;
    }
    
}
