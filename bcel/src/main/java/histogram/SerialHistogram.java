package histogram;

import utils.ArrayUtils;

import java.util.Arrays;


public class SerialHistogram {

    private static float[] data;
    private static float[] results;

    public SerialHistogram(float[] data, int N) {
        SerialHistogram.data = data;
        results = new float[N];
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

    public static void main(String[] args) {

        float[] A = ArrayUtils.randomFloatArray1D(10, 5);
        SerialHistogram serial = new SerialHistogram(A, 10);

        System.out.println(Arrays.toString(serial.getData()));

        serial.calculate();

        System.out.println(Arrays.toString(serial.getResult()));
    }
}
