package matrix;

import pl.edu.agh.bcel.utils.ArrayUtils;

public class MatrixTest {

    public static void main(String[] args) {

        int size = 1024;
        int bound = 100;
        int[][] A = ArrayUtils.generateIArray2D(size, size, bound);
        int[][] B = ArrayUtils.generateIArray2D(size, size, bound);
        int NUM_THREADS = Runtime.getRuntime().availableProcessors();

        SerialMultiplier serialMultiplier = new SerialMultiplier(A, B);
        System.out.println("SerialMultiplier: Starting...");
        Long now = System.currentTimeMillis();
        serialMultiplier.multiply();
        System.out.println("Took " + (double) (System.currentTimeMillis() - now) / 1e3+"\n");

        ParallelMultiplier parallelMultiplier = new ParallelMultiplier(A, B);
        System.out.println("ParallelMultiplier: Starting...");
        Long now2 = System.currentTimeMillis();
        parallelMultiplier.multiply();
        System.out.println("Took " + (double) (System.currentTimeMillis() - now2) / 1e3+"\n");


        JCudaMultiplier jcudaMultiplier = new JCudaMultiplier(A, B);
        System.out.println("JCudaMultiplier: Starting...");
        Long now3 = System.currentTimeMillis();
        jcudaMultiplier.multiply();
        System.out.println("Took " + (double) (System.currentTimeMillis() - now3) / 1e3+"\n");


    }
}
