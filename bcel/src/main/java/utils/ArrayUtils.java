package utils;

import java.util.Arrays;
import java.util.Random;

public class ArrayUtils {

    public static double[] randomIntArray(int size) {
        double[] arr = new double[size];
        Random random = new Random();
        for (int i = 0; i < arr.length; i++) {
            arr[i] = random.nextDouble();
        }
        return arr;
    }

    public static double[] randomIntArrayWithBounds(int size, double bound) {
        double[] arr = new double[size];
        Random random = new Random();
        for (int i = 0; i < arr.length; i++) {
            arr[i] = random.nextDouble() * bound;
        }
        return arr;
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
}
