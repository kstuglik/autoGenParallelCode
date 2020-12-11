package histogram;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class ParallelHistogram {

    private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();

    private final float[] data;
    private final float[] results;

    public ParallelHistogram(float[] data, int N) {
        this.data = data;
        results = new float[N];
    }

    public void calculate() {
        ExecutorService service = Executors.newFixedThreadPool(NUM_THREADS);
        List<Callable<float[]>> tasks = new LinkedList<>();
        List<Future<float[]>> partialResults = new LinkedList<>();
        int dataSize = data.length;
        for (int i = 0; i < NUM_THREADS; i++) {
            int start = i * (dataSize / NUM_THREADS);
            int stop = (i + 1) * (dataSize / NUM_THREADS) - 1;
            if (stop > dataSize - 1) {
                stop = dataSize - 1;
            }
            int finalStop = stop;
            tasks.add(() -> processChunk(start, finalStop));
        }
        try {
            partialResults = service.invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        service.shutdown();
        for (Future<float[]> partialResult : partialResults) {
            try {
                float[] part = partialResult.get();
                for (int i = 0; i < results.length; i++) {
                    results[i] += part[i];
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public float[] getResult() {
        return results;
    }

    private float[] processChunk(int start, int stop) {
        float[] partialResult = new float[results.length];
        for (int i = start; i <= stop; i++) {
            partialResult[i] = data[i]+ 1;
        }
        return partialResult;
    }

}
