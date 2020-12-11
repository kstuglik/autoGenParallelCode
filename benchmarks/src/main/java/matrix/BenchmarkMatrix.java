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

import java.io.IOException;


public class BenchmarkMatrix {

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(BenchmarkMatrix.class.getSimpleName())
                .threads(1)
                .forks(1)
                .warmupIterations(0)
                .mode(Mode.AverageTime)
                .build();

        new Runner(options).run();
    }

    @Benchmark
    public void MultiplySquareMatricesSerial(TestState state) {
        state.serialMultiplier.multiply();
    }

//    @Benchmark
//    public void MultiplySquareMatricesParallel(TestState state) {
//        state.parallelMultiplier.multiply();
//    }

//    @Benchmark
//    public void MultiplySquareMatricesJCuda(TestState state) {
//        state.jcudaMultiplier.multiply();
//    }


    @State(value = Scope.Benchmark)
    public static class TestState {
        int dim = 4096;
        String fileName = "benchmarks/src/main/resources/matrix" + dim + ".txt";
        int[][] A;
        int[][] B;

        {
            try {
                A = ArrayUtils.readMatrix2D(fileName);
                B = ArrayUtils.readMatrix2D(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



        int NUM_THREADS = Runtime.getRuntime().availableProcessors();


        SerialMultiplier serialMultiplier = new SerialMultiplier(A, B);
//        ParallelMultiplier parallelMultiplier = new ParallelMultiplier(A, B);
//        JCudaMultiplier jcudaMultiplier = new JCudaMultiplier(A, B);

        public TestState() {
        }
    }
}
