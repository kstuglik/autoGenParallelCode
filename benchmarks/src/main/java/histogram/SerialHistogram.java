package histogram;

import java.util.Random;

public class SerialHistogram {

    private final int[] data;
    private final int[] results;

    public SerialHistogram(int[] data, int limit) {
        this.data = data;
        results = new int[limit + 1];
    }

    public void calculate() {
        for (int i = 0; i < data.length; i++) {
            results[data[i]]++;
        }
    }

    public int[] getResult() {
        return results;
    }


            public static int[] get_array_value2(int N, int dataBound) {
        int[] arr = new int[N];
        Random random = new Random();
        for (int i = 0; i < N; i++) {
            arr[i] = random.nextInt(dataBound + 1);
        }
        return arr;
    }

    public static void main(String[] args) {
                int[] A  = get_array_value2(1000,5);
        SerialHistogram serial = new SerialHistogram(A, 5);

        serial.calculate();
    }
}
