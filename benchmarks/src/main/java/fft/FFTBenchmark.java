package fft;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import pl.edu.agh.bcel.utils.ArrayUtils;

public class FFTBenchmark {

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(FFTBenchmark.class.getSimpleName())
                .threads(1)
                .forks(1)
                .warmupIterations(1)
                .mode(Mode.AverageTime)
                .build();

        new Runner(options).run();
    }

    @Benchmark
    public void serialFFTBenchmark(TestState testState) {
        ThreadTestState.serialFFT.transform(testState.real, testState.imag);
    }

    @Benchmark
    public void parallelFFTBenchmark(TestState testState) {
        ThreadTestState.parallelFFT.transform(testState.real, testState.imag);
    }

    @State(value = Scope.Benchmark)
    public static class TestState {
        int size = 1024*1024*32;
        double bound = 1.0;
        double[] real = ArrayUtils.generateDArray1D(size, bound);
        double[] imag = ArrayUtils.generateDArray1D(size, bound);
    }

    @State(value = Scope.Thread)
    public static class ThreadTestState {
        static SerialFFT serialFFT = new SerialFFT();
        static ParallelFFT parallelFFT = new ParallelFFT();
    }
}
