package forkJoinPoolExample;


import java.util.concurrent.Callable;

public class MultiplyThread implements Callable<Integer> {
    private double[][] matrix1;
    private double[][] matrix2;
    private int i;
    private int j;

    public MultiplyThread(double[][] matrix1, double[][] matrix2, int i, int j) {
        this.matrix1 = matrix1;
        this.matrix2 = matrix2;
        this.i = i;
        this.j = j;
    }

    @Override
    public Integer call() throws Exception {
        int result = 0;
        for (int k = 0; k < matrix2.length; k++) {
            result += matrix1[i][k] * matrix2[k][j];
        }
        return result;
    }
}