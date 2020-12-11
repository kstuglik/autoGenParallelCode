package histogram;

import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.jcublas.JCublas;
import jcuda.jcublas.JCublas2;
import jcuda.jcublas.cublasHandle;
import jcuda.jcublas.cublasOperation;
import pl.edu.agh.bcel.utils.ArrayUtils;

import java.util.Arrays;

import static jcuda.runtime.JCuda.*;
import static jcuda.runtime.cudaMemcpyKind.cudaMemcpyDeviceToHost;
import static jcuda.runtime.cudaMemcpyKind.cudaMemcpyHostToDevice;

public class JCudaHistogram {

    static int N;

    static Pointer alpha = Pointer.to(new float[]{1.0f});
    static Pointer beta = Pointer.to(new float[]{1.0f});

    static Pointer ptr_A = new Pointer(), ptr_B = new Pointer(), ptr_C = new Pointer();
    static float[] matrix_A;
    static float[] matrix_B;
    static float[] matrix_C;

    public JCudaHistogram(float[] matrix_AA, int range) {
        N = matrix_AA.length;

        matrix_A = matrix_AA;
        matrix_B = new float[N];
        matrix_C = new float[N];

        Arrays.fill(matrix_B, 1);
        System.out.println(Arrays.toString(matrix_A));
        System.out.println(Arrays.toString(matrix_B));
        addMatrix(10, 10);
        System.out.println(Arrays.toString(matrix_C));
    }

    public static void addMatrix(int n, int m) {
        JCublas.cublasInit();
        /* Allocate device memory for the matrices */
        JCublas.cublasAlloc(N, Sizeof.FLOAT, ptr_A);
        JCublas.cublasAlloc(N, Sizeof.FLOAT, ptr_B);
        JCublas.cublasAlloc(N, Sizeof.FLOAT, ptr_C);

        /* Initialize the device matrices with the host matrices */
        JCublas.cublasSetVector(N, Sizeof.FLOAT, Pointer.to(matrix_A), 1, ptr_A, 1);
        JCublas.cublasSetVector(N, Sizeof.FLOAT, Pointer.to(matrix_B), 1, ptr_B, 1);
        JCublas.cublasSetVector(N, Sizeof.FLOAT, Pointer.to(matrix_C), 1, ptr_C, 1);
        cublasHandle handle = new cublasHandle();
        JCublas2.cublasCreate(handle);
        JCublas2.cublasSgeam(handle, cublasOperation.CUBLAS_OP_N, cublasOperation.CUBLAS_OP_N, m, n, alpha, ptr_A, m, beta, ptr_B, m, ptr_C, m);
        JCublas.cublasGetVector(N, Sizeof.FLOAT, ptr_C, 1, Pointer.to(matrix_C), 1);

        JCublas.cublasFree(ptr_A);
        JCublas.cublasFree(ptr_B);
        JCublas.cublasFree(ptr_C);
        JCublas.cublasShutdown();

    }

//    public static void main(String[] args) {
//
//        float[] A = ArrayUtils.generateFArray1D(100, 5);
//        System.out.println(Arrays.toString(matrix_A));
//        JCudaHistogram serial = new JCudaHistogram(A, 10);
//
//    }

    public void calculate() {
        JCublas2.setExceptionsEnabled(true);
        cudaMalloc(ptr_A, N * Sizeof.FLOAT);
        cudaMalloc(ptr_B, N * Sizeof.FLOAT);
        cudaMalloc(ptr_C, N * Sizeof.FLOAT);

        cudaMemcpy(ptr_A, Pointer.to(matrix_A), N * Sizeof.FLOAT, cudaMemcpyHostToDevice);
        cudaMemcpy(ptr_B, Pointer.to(matrix_B), N * Sizeof.FLOAT, cudaMemcpyHostToDevice);
        cudaMemcpy(ptr_C, Pointer.to(matrix_C), N * Sizeof.FLOAT, cudaMemcpyHostToDevice);

//        System.out.println(Arrays.toString(matrix_A));
//        System.out.println("+");
//        System.out.println(Arrays.toString(matrix_B));

// SUM of matrices

        cublasHandle handle = new cublasHandle();
        //JCublas2.cublasCreate(handle);
        JCublas2.cublasSgeam(
                handle, cublasOperation.CUBLAS_OP_N,
                cublasOperation.CUBLAS_OP_N, N, N,
                alpha, ptr_A, N,
                beta, ptr_B, N,
                ptr_C, N);

// show results
        float[] data = new float[N];
        cudaMemcpy(Pointer.to(data), ptr_C, N * Sizeof.FLOAT, cudaMemcpyDeviceToHost);
//        System.out.println("=");
        System.out.println(Arrays.toString(data));

// Clean memory
        cudaFree(ptr_A);
        cudaFree(ptr_B);
        cudaFree(ptr_C);

        JCublas.cublasShutdown();

        /* Performs operation using JCublas *//*
        JCublas2.cublasSgeam(
                handle,
                CUBLAS_OP_N, CUBLAS_OP_N,
                N,N,
                alpha,
                ptr_A, N,
                beta,
                ptr_B,N,
                ptr_C,N);*/
    }

    float[] getResult() {
        return matrix_C;
    }
}