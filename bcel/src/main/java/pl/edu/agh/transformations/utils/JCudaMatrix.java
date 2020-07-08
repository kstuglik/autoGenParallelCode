package pl.edu.agh.transformations.utils;

import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.jcublas.JCublas;


public class JCudaMatrix {

    float matrix_A[],matrix_B[],matrix_C[];

    int cols_A, rows_A;
    int cols_B, rows_B;

    Pointer ptr_A = new Pointer(), ptr_B = new Pointer(), ptr_C = new Pointer();
    float alpha = 1.0f, beta = 0.0f;


    public JCudaMatrix(int[][] matrix_AA, int[][] matrix_BB) {

        if (matrix_AA[0].length != matrix_BB.length) {
            throw new RuntimeException("Cannot perform multiplication because dimensions are not equal.");
        }

        this.matrix_A = ArrayUtils.flattenIARRAY2D(matrix_AA);
        this.matrix_B = ArrayUtils.flattenIARRAY2D(matrix_BB);

        cols_A = matrix_AA[0].length;
        rows_A = matrix_AA.length;
        cols_B = matrix_BB[0].length;
        rows_B = matrix_BB.length;

        matrix_C = new float[cols_B * rows_A];
    }

    public JCudaMatrix(float[] matrix_A, float[]matrix_B,int cols_A,int rows_A,int cols_B, int rows_B) {

        if (cols_A != rows_B) {
            throw new RuntimeException("Cannot perform multiplication because dimensions are not equal.");
        }

        this.matrix_A = matrix_A;
        this.matrix_B = matrix_B;

        this.cols_A = cols_A;
        this.rows_A = rows_A;

        this.cols_B = cols_B;
        this.rows_B = rows_B;

        matrix_C = new float[cols_B * rows_A];
    }

    //In the future: ADD multiply by scalar


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

//        System.out.println(Arrays.toString(matrix_C));
        return matrix_C;
    }
}
