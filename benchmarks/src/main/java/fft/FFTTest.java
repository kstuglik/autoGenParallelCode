package fft;

import pl.edu.agh.bcel.utils.ArrayUtils;

import java.util.Arrays;

public class FFTTest {

    private static final int ARR_LENGTH = 1024*1024*16;
    private static final double BOUND = 1.0;

    public static void main(String[] args) {
        double[] real = ArrayUtils.generateDArray1D(ARR_LENGTH, BOUND);
        double[] imag = ArrayUtils.generateDArray1D(ARR_LENGTH, BOUND);
        double[] real2 = Arrays.copyOf(real, real.length);
        double[] imag2 = Arrays.copyOf(imag, imag.length);
        SerialFFT serialFFT =new SerialFFT();
        ParallelFFT parallelFFT =new ParallelFFT();

        System.out.println("SerialFFT: Starting...");
        Long now = System.currentTimeMillis();
        serialFFT.transform(real, imag);
        System.out.println("Took " + (double) (System.currentTimeMillis() - now) / 1e3+"\n");


        System.out.println("ParallelFFT: Starting...");
        now = System.currentTimeMillis();
        parallelFFT.transform(real2, imag2);
        System.out.println("Took " + (double) (System.currentTimeMillis() - now) / 1e3+"\n");


        System.out.println(real[0] + " " + real[1] + " " + real[2]);
        System.out.println(real2[0] + " " + real2[1] + " " + real2[2]);
        System.out.println(imag[0] + " " + imag[1] + " " + imag[2]);
        System.out.println(imag2[0] + " " + imag2[1] + " " + imag2[2]);
        double[] diff = new double[real.length];
        for (int i = 0; i < real.length; i++) {
            diff[i] = real[i] - real2[i];
        }
        diff = Arrays.stream(diff)
                .filter(d -> !(d == 0))
                .toArray();
        System.out.println("DIFFERENCES!!! : " + diff.length);

    }
}
