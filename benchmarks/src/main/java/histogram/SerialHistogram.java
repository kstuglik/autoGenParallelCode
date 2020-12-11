package histogram;

import pl.edu.agh.bcel.utils.ArrayUtils;

import java.util.Arrays;


public class SerialHistogram {

    private final float[] data;
    private final float[] results;

    public SerialHistogram(float[] data, int N) {
        this.data = data;
        results = new float[N];
    }

    public void calculate() {

        for (int i = 0; i < data.length; i++) {
            results[i] = data[i]++;
        }
    }

    public float[] getData() {
        return data;
    }

    public float[] getResult() {
        return results;
    }

//    public static void main(String[] args) {
//
//        float[] A = ArrayUtils.generateFArray1D(10, 5);
//        SerialHistogram serial = new SerialHistogram(A, 10);
//
//        System.out.println(Arrays.toString(serial.getData()));
//
//        serial.calculate();
//
//        System.out.println(Arrays.toString(serial.getResult()));
//    }
}
