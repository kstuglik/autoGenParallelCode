package histogram;

import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.jcublas.JCublas2;
import jcuda.jcublas.cublasHandle;
import jcuda.jcublas.cublasOperation;

import java.util.Arrays;
import java.util.Random;

import static jcuda.runtime.JCuda.*;
import static jcuda.runtime.cudaMemcpyKind.cudaMemcpyDeviceToHost;
import static jcuda.runtime.cudaMemcpyKind.cudaMemcpyHostToDevice;

public class JCudaHistogram {
    static int N;
    static float alpha = 1.0f;
    static float beta = 1.0f;

    static Pointer pointer_A = new Pointer(), pointer_B = new Pointer(), pointer_C = new Pointer();
    static float[] matrix_A,matrix_B, matrix_C;


    public void calculate(){
        JCublas2.setExceptionsEnabled(true);
        cudaMalloc(pointer_A, N * Sizeof.FLOAT);
        cudaMalloc(pointer_B, N * Sizeof.FLOAT);
        cudaMalloc(pointer_C, N * Sizeof.FLOAT);

        cudaMemcpy(pointer_A, Pointer.to(matrix_A), N * Sizeof.FLOAT, cudaMemcpyHostToDevice);
        cudaMemcpy(pointer_B, Pointer.to(matrix_B), N * Sizeof.FLOAT, cudaMemcpyHostToDevice);
        cudaMemcpy(pointer_C, Pointer.to(matrix_C), N * Sizeof.FLOAT, cudaMemcpyHostToDevice);

//        System.out.println(Arrays.toString(matrix_A));
//        System.out.println("+");
//        System.out.println(Arrays.toString(matrix_B));

// SUM of matrices

        cublasHandle handle = new cublasHandle();
        JCublas2.cublasCreate(handle);
        JCublas2.cublasSgeam(
                handle, cublasOperation.CUBLAS_OP_N,
                cublasOperation.CUBLAS_OP_N, N, N,
                Pointer.to(new float[] { alpha }), pointer_A, N, Pointer.to(new float[] { beta }), pointer_B, N,
                pointer_C, N);

// show results
        float data[] = new float[N];
        cudaMemcpy(Pointer.to(data), pointer_C, N * Sizeof.FLOAT, cudaMemcpyDeviceToHost);
//        System.out.println("=");
        System.out.println(Arrays.toString(data));

// Clean memory
        cudaFree(pointer_A);
        cudaFree(pointer_B);
        cudaFree(pointer_C);
        JCublas2.cublasDestroy(handle);
    }


    public JCudaHistogram(int[] matrix_AA, int limit) {
        //limit????
        this.N = matrix_AA.length;
        matrix_A = flatten(matrix_AA);
        matrix_B = get_array_value(N,limit);
        matrix_C = new float[N];
    }


    public float[] flatten(int[] arr) {
        int n = arr.length;
        float[] result  = new float[n];
        for(int i=0;i<n;i++){
            result[i] = (float) arr[i];
        }
        return result;
    }


    public static float[] get_array_value(int N, int dataBound) {
        float[] arr = new float[N];
        Random random = new Random();
        for (int i = 0; i < N; i++) {
            arr[i] = random.nextInt(dataBound + 1);
        }
        return arr;
    }

//    public static void main(String[] args) {
//        float[] A  = get_array_value(1000,5);
//        JCudaHistogram jcudaHistogram = new JCudaHistogram(flatten(A), 5);
//        jcudaHistogram.calculate();
//    }
}