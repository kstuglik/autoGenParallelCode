package pl.edu.agh.bcel.utils;

import java.io.*;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

public class ArrayUtils {


    public static int[] convertF2IArray1D(float[] data) {
        return IntStream.range(0, data.length).map(i -> (int) data[i]).toArray();
    }

    public static float[] flattenIArray2D(int[][] arr) {
        int rows = arr.length;
        int cols = arr[0].length;
        float[] result = new float[rows * cols];

        for (int row = 0; row < cols; row++) {
            for (int col = 0; col < rows; col++) {
                result[row * rows + col] = arr[col][row];
            }
        }
        System.out.println(Arrays.toString(result));
        return result;
    }

    public static double[] generateDArray1D(int N, double range) {
        double[] arr = new double[N];
        double min = 0.0;
        Random random = new Random();
        for (int i = 0; i < N; i++) {
            arr[i] =  min + random.nextDouble() * (range - min);
        }
        return arr;
    }

    public static float[] generateFArray1D(int N, float range) {
        float[] arr = new float[N];
        float min = 0.0f;
        Random random = new Random();
        for (int i = 0; i < N; i++) {
            arr[i] =  min + random.nextFloat() * (range - min);
        }
        return arr;
    }

    public static int[][] generateIArray2D(int rows, int columns, int range) {
        int[][] A = new int[rows][columns];
        Random gen = new Random();
        for (int rowNum = 0; rowNum < rows; rowNum++) {
            for (int colNum = 0; colNum < columns; colNum++) {
                A[rowNum][colNum] = gen.nextInt(range);
            }
        }
        return A;
    }

    public static float[] readFromFileFArray1D(String fileName) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(new FileInputStream(fileName));
        int size = dataInputStream.available() / (Float.SIZE / 8);
        float[] A = new float[size];
        for (int i = 0; i < size; i++) A[i] = dataInputStream.readFloat();
        return A;
    }

    public static void writeToFileFArray1D(float[] A, String filePath) throws IOException {
        File file = new File(filePath);
        if (file.exists()) {
            System.out.println("File already exists");
        } else {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        FileOutputStream fos = new FileOutputStream(file, false);
        DataOutputStream dos = new DataOutputStream(fos);

        for (float v : A) dos.writeFloat(v);

        dos.close();
    }

    public static int[] rewrite(float[] data) {
        int[] temp = IntStream.range(0, data.length).map(i -> (int) data[i]).toArray();
        return temp;
    }

}
