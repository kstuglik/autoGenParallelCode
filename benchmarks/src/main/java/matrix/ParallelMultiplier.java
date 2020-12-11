package matrix;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParallelMultiplier {

    private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
    ExecutorService service;
    List<Callable<Integer>> tasks;
    int step;
    private final int[][] A;
    private final int[][] B;
    private final int[][] C;
    private final int resultRows;
    private final int resultColumns;

    public ParallelMultiplier(int[][] pA, int[][] pB) {
        this.A = pA;
        this.B = pB;
        if (A[0].length != B.length) {
            throw new RuntimeException("Cannot perform multiplication because dimensions are not equal.");
        }
        resultRows = A.length;
        resultColumns = B[0].length;
        C = new int[resultRows][resultColumns];
        initializeExecutors();
    }

    public static void main(String[] args) {
        int[][] A = new int[][]{{3, 2, 6}, {0, 4, 1}, {2, 0, 1}};
        int[][] B = new int[][]{{4}, {3}, {1}};
        ParallelMultiplier parallelMultiplier = new ParallelMultiplier(A, B);

        int[][] C = parallelMultiplier.multiply();

        System.out.println(Arrays.deepToString(C));
    }

    public int[][] getC() {
        return C;
    }

    public void initializeExecutors() {
        step = A.length / NUM_THREADS;
        if (step == 0)
            step = 1;
        service = Executors.newFixedThreadPool(NUM_THREADS);
        tasks = new LinkedList<>();
    }

    public int[][] multiply() {
        for (int rowNum = 0; rowNum < resultRows; rowNum += step) {
            for (int colNum = 0; colNum < resultColumns; colNum++) {
                int finalRowNum = rowNum;
                int finalColNum = colNum;
                tasks.add(() -> singleCalculation(finalRowNum, finalColNum, step));
            }
        }
        try {
            service.invokeAll(tasks);
//            service.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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
}
