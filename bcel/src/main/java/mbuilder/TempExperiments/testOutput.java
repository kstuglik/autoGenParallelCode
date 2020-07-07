package mbuilder.TempExperiments;

import utils.JCudaMatrix;

import java.util.Arrays;

public class testOutput {
    public static void main(String[] args) {
        multiply();
    }

    public static void multiply() {
        int[][] var0 = new int[][]{{2, 4}, {1, 3}};
        System.out.println(Arrays.deepToString(var0));
        int[][] var1 = new int[][]{{2, 4}, {1, 3}};
        System.out.println(Arrays.deepToString(var1));
        JCudaMatrix var2 = new JCudaMatrix(var0, var1);

        float[] var3 = var2.multiply();
        System.out.print(Arrays.toString(var3));
    }
}
