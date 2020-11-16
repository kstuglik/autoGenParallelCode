package histogram;

import pl.edu.agh.bcel.utils.ArrayUtils;

public class HistogramTest {

    public static void main(String[] args) {

        int N = 100;
        int range = 100;
        float[] data = ArrayUtils.generateFArray1D(N, range);
        System.out.println("data.len = "+data.length);

        SerialHistogram histogram = new SerialHistogram(data, range);
        System.out.println("Starting...");
        Long now = System.currentTimeMillis();
        histogram.calculate();
        System.out.println("Took " + (double) (System.currentTimeMillis() - now) / 1e3 + "\n");


        ParallelHistogram histogram2 = new ParallelHistogram(data, range);
        System.out.println("Starting...");
        Long now2 = System.currentTimeMillis();
        histogram2.calculate();
        System.out.println("Took " + (double) (System.currentTimeMillis() - now2) / 1e3 + "\n");


        JCudaHistogram jcudaHistogram = new JCudaHistogram(data, range);
        System.out.println("JCudaHistogram: Starting...");
        Long now3 = System.currentTimeMillis();
        jcudaHistogram.calculate();
        System.out.println("Took " + (double) (System.currentTimeMillis() - now3) / 1e3 + "\n");

    }
}
