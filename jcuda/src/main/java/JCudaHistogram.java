import static jcuda.runtime.JCuda.cudaFree;
import static jcuda.runtime.JCuda.cudaMalloc;
import static jcuda.runtime.JCuda.cudaMemcpy;
import static jcuda.runtime.cudaMemcpyKind.cudaMemcpyDeviceToHost;
import static jcuda.runtime.cudaMemcpyKind.cudaMemcpyHostToDevice;

import java.util.Arrays;

import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.jcublas.JCublas2;
import jcuda.jcublas.cublasHandle;
import jcuda.jcublas.cublasOperation;

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

        System.out.println(Arrays.toString(matrix_A));
        System.out.println("+");
        System.out.println(Arrays.toString(matrix_B));

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
        System.out.println("=");
        System.out.println(Arrays.toString(data));

// Clean memory
        cudaFree(pointer_A);
        cudaFree(pointer_B);
        cudaFree(pointer_C);
    }

    public JCudaHistogram(int[][] matrix_AA) {
        this.N = matrix_AA.length;
        matrix_A = flatten(matrix_AA);
        matrix_B = get_array_value(N,1);
        matrix_C = new float[N];
    }

    public static void main(String[] args) {
        int[][] matrix_AA = {{3,1,2},{1,2,3}};
        JCudaHistogram jcudaHistogram = new JCudaHistogram(matrix_AA);
        jcudaHistogram.calculate();
    }

    public float[] flatten(int[][] arr) {
        int rows = arr.length;
        int cols = arr[0].length;

        this.N = rows * cols;
        float[] result = new float[ this.N];

        for (int row = 0; row < cols; row++){
            try{
                for(int col=0; col<rows; col++){
                    result[row*rows+col] = arr[col][row];
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        return result;
    }

    public static float[] get_array_value(int N, int value) {
        float[] result = new float[N];
        for (int i = 0; i < N; i++){
            result[i] = 1;
        }
//        System.out.println(Arrays.toString(result));
        return result;
    }

}