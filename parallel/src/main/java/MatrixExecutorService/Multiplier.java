package MatrixExecutorService;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Multiplier {

    private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
    ExecutorService service;
    List<Callable<Integer>> tasks;
    int step;
    private double[][] A, B, C;
    private int resultRows, resultColumns;

    public Multiplier(double[][] pA, double[][] pB) {
        this.A = pA;
        this.B = pB;
        if (A[0].length != B.length) {
            throw new RuntimeException("Cannot perform multiplication because dimensions are not equal.");
        }
        resultRows = A.length;
        resultColumns = B[0].length;
        C = new double[resultRows][resultColumns];
        initializeExecutors();
    }

    public void initializeExecutors() {
        step = A.length / NUM_THREADS;
        service = Executors.newFixedThreadPool(NUM_THREADS);
        tasks = new LinkedList<>();
    }

    public double[][] multiply() {
        for (int rowNum = 0; rowNum < resultRows; rowNum += step) {
            for (int colNum = 0; colNum < resultColumns; colNum++) {
                int finalRowNum = rowNum;
                int finalColNum = colNum;
                tasks.add(() -> singleCalculation(finalRowNum, finalColNum, step));
            }
        }
        try {
            service.invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        service.shutdown();
        return C;
    }

    private int singleCalculation(int rowNum, int colNum, int step) {
        for (int currRow = rowNum; currRow < rowNum + step && currRow < resultRows; currRow++) {
            for (int r = 0; r < B.length; r++) {
                C[currRow][colNum] += A[currRow][r] * B[r][colNum];
            }
        }
        return 0;
    }

    public double[][] getC() {
        return C;
    }
}
