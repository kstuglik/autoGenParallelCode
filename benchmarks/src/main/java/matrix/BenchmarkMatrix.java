package matrix;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import pl.edu.agh.bcel.utils.ArrayUtils;


public class BenchmarkMatrix {

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(BenchmarkMatrix.class.getSimpleName())
                .mode(Mode.AverageTime)
                .build();

        new Runner(options).run();
    }

    @Benchmark
    public void MultiplySquareMatricesSerial(TestState state) {
        state.serialMultiplier.multiply();
    }

    @Benchmark
    public void MultiplySquareMatricesParallel(TestState state) {
        state.parallelMultiplier.multiply();
    }

    @Benchmark
    public void MultiplySquareMatricesJCuda(TestState state) {
        state.jcudaMultiplier.multiply();
    }


    @State(value = Scope.Benchmark)
    public static class TestState {
        int size = 1000;
        int bound = 5;
        int[][] A = ArrayUtils.generateIArray2D(size, size, bound);
        int[][] B = ArrayUtils.generateIArray2D(size, size, bound);
        int NUM_THREADS = Runtime.getRuntime().availableProcessors();


        SerialMultiplier serialMultiplier = new SerialMultiplier(A, B);
        ParallelMultiplier parallelMultiplier = new ParallelMultiplier(A, B,NUM_THREADS);
        JCudaMultiplier jcudaMultiplier = new JCudaMultiplier(A, B);
    }
}
