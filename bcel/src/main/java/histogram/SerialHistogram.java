package histogram;

import pl.edu.agh.bcel.utils.ArrayUtils;


public class SerialHistogram {

    private static float[] data;
    private static float[] results;

    public SerialHistogram(float[] data, int N) {
        SerialHistogram.data = data;
        results = new float[N];
    }

    public static void main(String[] args) {

        float[] A = ArrayUtils.generateFArray1D(1000000, 5);
        SerialHistogram serial = new SerialHistogram(A, 1000000);

//        System.out.println(Arrays.toString(serial.getData()));
        long startTime = System.nanoTime();
        serial.calculate();
        long endTime = System.nanoTime();
        long timeElapsed = endTime - startTime;
        System.out.println("Execution time in milliseconds : " + timeElapsed / 1000000);

//        System.out.println(Arrays.toString(serial.getResult()));
    }

    public void calculate() {
        for (int i = 0; i < data.length; i++) {
            results[i] = data[i] + 1;
        }
    }

    public float[] getData() {
        return data;
    }

    public float[] getResult() {
        return results;
    }
}
