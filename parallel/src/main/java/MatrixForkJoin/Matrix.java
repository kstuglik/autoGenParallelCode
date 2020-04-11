package MatrixForkJoin;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class Matrix extends RecursiveAction {
    private static final int THRESHOLD = 16;
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
        double[][] m = new double[matrix.length][matrix[0].length / 2];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length / 2; j++) {
                m[i][j] = matrix[i][j];
            }
        }
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
        if (A[0].length <= THRESHOLD) {
            for (int i = 0; i < A.length; i++) {
                for (int j = 0; j < B[0].length; j++) {
                    for (int k = 0; k < B.length; k++) {
                        m[i][j] += A[i][k] * B[k][j];
//                        System.out.println("JESTEM"+A[0].length );
                    }
                }
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
        }
    }
}