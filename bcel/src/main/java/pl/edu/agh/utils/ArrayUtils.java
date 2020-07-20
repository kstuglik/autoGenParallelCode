package pl.edu.agh.utils;

import java.io.*;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

public class ArrayUtils {

    public static float[] createFARRAY1D(int N) {
        float[] arr = new float[N];
        Random random = new Random(1);
        for (int i = 0; i < N; i++) {
            arr[i] = random.nextFloat();
        }
        return arr;
    }

    public static int[][] createIARRAY2D(int rows, int columns, int range) {
        int[][] A = new int[rows][columns];
        Random gen = new Random();
        for (int rowNum = 0; rowNum < rows; rowNum++) {
            for (int colNum = 0; colNum < columns; colNum++) {
                A[rowNum][colNum] = gen.nextInt(range);
            }
        }
        return A;
    }

    public static float[] flattenIARRAY2D(int[][] arr) {
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

    public static int[] changeF2IARRAY(float[] data) {
        int[] temp = IntStream.range(0, data.length).map(i -> (int) data[i]).toArray();
        return temp;
    }

    public static void writeFARRAY1D(int size, String filePath) throws IOException {
        float[] A = createFARRAY1D(size);/*        System.out.println(Arrays.toString(A));*/
        System.out.println(filePath);

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

    public static float[] readFARRAY1D(String fileName) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(new FileInputStream(fileName));
        int size = dataInputStream.available() / (Float.SIZE / 8);
        float[] A = new float[size];
        for (int i = 0; i < size; i++) A[i] = dataInputStream.readFloat();
        return A;
    }

}
