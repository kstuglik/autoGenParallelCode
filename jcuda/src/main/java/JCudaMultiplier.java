/* Imports, JCublas */
import java.util.Arrays;
import jcuda.*;
import jcuda.jcublas.*;

public class JCudaMultiplier {

    float matrix_A[],matrix_B[],matrix_C[];
    int cols_A, rows_A, cols_B, rows_B;

    float alpha = 1.0f, beta = 0.0f;
    Pointer ptr_A = new Pointer(), ptr_B = new Pointer(), ptr_C = new Pointer();

    /* Main */
    public static void main(String args[]) {

        //PRZYPADEK 1   [6.0, 10.0, 10.0, 12.0]
        int[][] matrix_AA = {{3,1,2},{1,2,3}};
        int[][] matrix_BB = {{0,1},{2,1},{2,3}};

//PRZYPADEK 2 [-3.0, 14.0, 4.0, 1.0, -11.0, 6.0]
//        int[][] matrix_AA = {{-2,-3,1},{-1,4,0}};
//        int[][] matrix_BB = {{-2,-1,2},{3,0,2},{2,2,-1}};

//PRZYPADEK 3 [33.0, 86.0, 40.0, 66.0]
//        int[][] matrix_AA = {{1,4,6},{8,2,4}};
//        int[][] matrix_BB = {{9,6},{3,7},{2,1}};

//PRZYPADEK 4 [57.0, 59.0, 10.0, 48.0, 26.0, 10.0, 78.0, 46.0, 16.0]
//        int[][] matrix_AA = {{9,6},{3,7},{2,1}};
//        int[][] matrix_BB = {{1,4,6},{8,2,4}};

//PRZYPADEK 5 [32.0, 46.0, 14.0, 63.0, 110.0, 37.0, 50.0, 144.0, 30.0]
//        int[][] matrix_AA = {{5, 6, 1}, {8, 7, 9}, {1, 5, 2}};
//        int[][] matrix_BB = {{4, 6, 7}, {2, 5, 1}, {0, 3, 9}};



        JCudaMultiplier jcudaMultiplier = new JCudaMultiplier(matrix_AA,matrix_BB);
        jcudaMultiplier.multiply();
    }

    public JCudaMultiplier(int[][] matrix_AA, int[][] matrix_BB) {

        if (matrix_AA[0].length != matrix_BB.length) {
            throw new RuntimeException("Cannot perform multiplication because dimensions are not equal.");
        }

        this.matrix_A = flatten(matrix_AA);
        this.matrix_B = flatten(matrix_BB);

        cols_A = matrix_AA[0].length;
        rows_A = matrix_AA.length;
        cols_B = matrix_BB[0].length;
        rows_B = matrix_BB.length;

        matrix_C = new float[cols_B * rows_A];
    }

    public float[] multiply() {
        /* Initialize JCublas */
        JCublas.cublasInit();

        /* Allocate device memory for the matrices */
        JCublas.cublasAlloc(cols_A * rows_A, Sizeof.FLOAT, ptr_A);
        JCublas.cublasAlloc(cols_B * rows_B, Sizeof.FLOAT, ptr_B);
        JCublas.cublasAlloc(cols_B * rows_A, Sizeof.FLOAT, ptr_C);

        /* Initialize the device matrices with the host matrices */
        JCublas.cublasSetVector(cols_A * rows_A, Sizeof.FLOAT, Pointer.to(matrix_A), 1, ptr_A, 1);
        JCublas.cublasSetVector(cols_B * rows_B, Sizeof.FLOAT, Pointer.to(matrix_B), 1, ptr_B, 1);
        JCublas.cublasSetVector(cols_B * rows_A, Sizeof.FLOAT, Pointer.to(matrix_C), 1, ptr_C, 1);

        /* Performs operation using JCublas */
        JCublas.cublasSgemm(
                'n', 'n',
                rows_A, cols_B, cols_A,
                alpha,
                ptr_A, rows_A,
                ptr_B, rows_B,
                beta,
                ptr_C, rows_A);

        /* Read the result back */
        JCublas.cublasGetVector(cols_B * rows_A, Sizeof.FLOAT, ptr_C, 1, Pointer.to(matrix_C), 1);

        /* Memory clean up */
        JCublas.cublasFree(ptr_A);
        JCublas.cublasFree(ptr_B);
        JCublas.cublasFree(ptr_C);

        /* Shutdown */
        JCublas.cublasShutdown();

        System.out.println(Arrays.toString(matrix_C));
        return matrix_C;
    }

    public static float[] flatten(int[][] arr) {
        int rows = arr.length;
        int cols = arr[0].length;
        float[] result = new float[rows*cols];

        for (int row = 0; row < cols; row++){
            try{
                for(int col=0; col<rows; col++){
                    result[row*rows+col] = arr[col][row];
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        System.out.println(Arrays.toString(result));
        return result;
    }
}