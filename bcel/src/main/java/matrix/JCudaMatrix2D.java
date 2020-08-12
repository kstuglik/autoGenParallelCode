/*
The program shows the expected effect of the file transformation:
Matrix. class (look at the Matrix2.java file)
the simplification consists in transferring only 2 parameters (arrays)
to the function that will perform the JCUDA multiplication
 */

package matrix;

import jcuda.Pointer;
import jcuda.jcublas.JCublas;

import java.util.Arrays;

public class JCudaMatrix2D {
    static float[] matrix_A;
    static float[] matrix_B;
    static float[] matrix_C;
    static int cols_A;
    static int rows_A;
    static int cols_B;
    static int rows_B;
    static Pointer ptr_A = new Pointer();
    static Pointer ptr_B = new Pointer();
    static Pointer ptr_C = new Pointer();
    static float alpha = 1.0F;
    static float beta = 0.0F;

    public static float[] flattenIArray2Dto1D(int[][] arg0) {
        int var1 = arg0.length;
        int var2 = arg0[0].length;
        float[] var3 = new float[var1 * var2];

        for (int var4 = 0; var4 < var2; ++var4) {
            for (int var5 = 0; var5 < var1; ++var5) {
                var3[var4 * var1 + var5] = (float) arg0[var5][var4];
            }
        }

        System.out.println(Arrays.toString(var3));
        return var3;
    }

    public static void main(String[] args) {
        multiplyMatrix();
    }

    public static void multiply() {
        JCublas.cublasInit();
        JCublas.cublasAlloc(cols_A * rows_A, 4, ptr_A);
        JCublas.cublasAlloc(cols_B * rows_B, 4, ptr_B);
        JCublas.cublasAlloc(cols_B * rows_A, 4, ptr_C);
        JCublas.cublasSetVector(cols_A * rows_A, 4, Pointer.to(matrix_A), 1, ptr_A, 1);
        JCublas.cublasSetVector(cols_B * rows_B, 4, Pointer.to(matrix_B), 1, ptr_B, 1);
        JCublas.cublasSetVector(cols_B * rows_A, 4, Pointer.to(matrix_C), 1, ptr_C, 1);
        JCublas.cublasSgemm('n', 'n', rows_A, cols_B, cols_A, alpha, ptr_A, rows_A, ptr_B, rows_B, beta, ptr_C, rows_A);
        JCublas.cublasGetVector(cols_B * rows_A, 4, ptr_C, 1, Pointer.to(matrix_C), 1);
        JCublas.cublasFree(ptr_A);
        JCublas.cublasFree(ptr_B);
        JCublas.cublasFree(ptr_C);
        JCublas.cublasShutdown();
    }

    public static void multiplyMatrix() {
        int[][] var1 = new int[][]{{2, 4}, {1, 3}};
        System.out.println(Arrays.deepToString(var1));
        int[][] var2 = new int[][]{{2, 4}, {1, 3}};
        System.out.println(Arrays.deepToString(var2));
        prepareFieldsFromArrays2D(var1, var2);
        multiply();
        System.out.println(Arrays.toString(matrix_C));
    }

    public static void prepareFieldsFromArrays2D(int[][] arg0, int[][] arg1) {
        if (arg0[0].length != arg1.length) {
            throw new RuntimeException("Cannot perform multiplication because dimensions are not equal.");
        } else {
            matrix_A = flattenIArray2Dto1D(arg0);
            matrix_B = flattenIArray2Dto1D(arg1);
            cols_A = arg0[0].length;
            rows_A = arg0.length;
            cols_B = arg1[0].length;
            rows_B = arg1.length;
            matrix_C = new float[cols_B * rows_A];
        }
    }

}
