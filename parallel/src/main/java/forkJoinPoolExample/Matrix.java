package forkJoinPoolExample;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class Matrix extends RecursiveAction {
    private static final int THRESHOLD = 3;
    static volatile double[][] m;
    public double[][] A;
    public double[][] B;
    int number;

    public Matrix(String A, String B) {
        this.A = readMatrix(A);
        this.B = readMatrix(B);
        this.number = 0;
        if (this.A[0].length != this.B.length) {
            throw new IllegalArgumentException("Check matrix sizes and order!");
        }

        m = new double[this.A.length][this.B[0].length];
    }

    private Matrix(double[][] A, double[][] B) {
        this.A = A;
        this.B = B;
    }

    private double[][] readMatrix(String filename) {
        double[][] matrix = null;
        try (BufferedReader br = new BufferedReader(new FileReader(new File(filename)))) {
            String line;
            List<String[]> list = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                String[] args = line.split(" ");
                try {
                    list.add(args);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            matrix = new double[list.size()][list.get(0).length];
            for (int i = 0; i < list.size(); i++) {
                for (int j = 0; j < list.get(0).length; j++) {
                    matrix[i][j] = Double.parseDouble(list.get(i)[j]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return matrix;
    }

    private double[][] matrixLeft(double[][] matrix) {
//        System.out.println("begin left");
        double[][] m = new double[matrix.length][matrix[0].length / 2];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length / 2; j++) {
                m[i][j] = matrix[i][j];
            }
        }
//        System.out.println("returning left");
        return m;
    }

    private double[][] matrixRight(double[][] matrix) {
        double[][] m = new double[matrix.length][matrix[0].length / 2 + matrix[0].length % 2];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length / 2 + matrix[0].length % 2; j++) {
                m[i][j] = matrix[i][j + matrix[0].length / 2];
            }
        }
        return m;
    }

    @Override
    protected void compute() {
        if (A[0].length < THRESHOLD) {
            for (int i = 0; i < A.length; i++) {
                for (int j = 0; j < B[0].length; j++) {
                    for (int k = 0; k < B.length; k++) {
                        m[i][j] += A[i][k] * B[k][j];
                    }
                }
//                System.out.println("");
            }
        } else {
            Matrix left = new Matrix(matrixLeft(A),
                    Arrays.copyOf(B, B.length / 2));

            Matrix right = new Matrix(matrixRight(A),
                    Arrays.copyOfRange(B, B.length / 2, B.length));

            left.fork();
            right.fork();

            left.join();
            right.join();
//            System.out.println("completed");
//            toStringMatrix(m);
        }
    }

    public void resultThreadPool() {
        ExecutorService executorService = Executors.newCachedThreadPool();
        List<Future<Integer>> futuresArray = new ArrayList<>();
        int[][] matrix3 = new int[A.length][B[0].length];

        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < B[0].length; j++) {
                futuresArray.add(
                        executorService.submit(
                                new MultiplyThread(A, B, i, j)
                        )
                );
            }
        }
        try {
            int index = 0;
            for (int i = 0; i < A.length; i++) {
                for (int j = 0; j < B[0].length; j++) {
                    matrix3[i][j] = futuresArray.get(index).get();
                    index++;
                    System.out.print(matrix3[i][j] + " ");
                }
                System.out.println("\n");
            }
        } catch (InterruptedException | ExecutionException e) {
            // thrown if task was interrupted before completion
            e.printStackTrace();
        }
        executorService.shutdown();
    }
}