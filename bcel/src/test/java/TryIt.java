import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TryIt {
    public static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
    public static ExecutorService SERVICE;

    public TryIt() {
    }

    public static void main(String[] args) {
        SERVICE = Executors.newFixedThreadPool(NUM_THREADS);

        for(int i = 0; i < NUM_THREADS; ++i) {

            int start = i * (20 / NUM_THREADS);
            int end = (i + 1) * (20 / NUM_THREADS) - 1;
            subTask(start,end);
        }

    }

    public static int subTask(int start, int end,int thread) {
        for(int i = start; i <= end; ++i) {
            System.out.println("Hello" + i+" from THREAD: "+);
        }

        return 1;
    }
}
