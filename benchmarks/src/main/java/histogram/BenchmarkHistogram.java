package histogram;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import pl.edu.agh.bcel.utils.*;

import java.util.Random;

public class BenchmarkHistogram {

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(BenchmarkHistogram.class.getSimpleName())
                .threads(1)
                .forks(5)
                .warmupIterations(1)
                .mode(Mode.AverageTime)
                .build();

        new Runner(options).run();
    }

    @Benchmark
    public void SerialHistogramBenchmark(TestState state) {
        state.serialHistogram.calculate();
    }

    @Benchmark
    public void ParallelHistogramBenchmark(TestState state) {
        state.parallelHistogram.calculate();
    }

    @Benchmark
    public void JCudaHistogramBenchmark(TestState state) {
        state.jcudaHistogram.calculate();
    }

    @State(value = Scope.Benchmark)
    public static class TestState {
        int N = 100;
        int range = 5;
        float[] data = ArrayUtils.generateFArray1D(N,range);
        SerialHistogram serialHistogram = new SerialHistogram(data, range);
        ParallelHistogram parallelHistogram = new ParallelHistogram(data, range);
        JCudaHistogram jcudaHistogram = new JCudaHistogram(data, range);

    }
}
