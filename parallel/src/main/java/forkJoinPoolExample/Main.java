package forkJoinPoolExample;

import java.time.Duration;
import java.util.concurrent.ForkJoinPool;

import static MatrixUtil.Main.appendStrToFile;

public class Main {
    public static void main(String[] args) {
        Matrix matrix = new Matrix("src/main/resources/A.txt", "src/main/resources/B.txt");
        String podsumowanie = "src/main/resources/podsumowanie.txt";

        ForkJoinPool pool = new ForkJoinPool();
        System.out.println("Pool is created for " + pool.getParallelism() + " cores");

        final long sp = System.currentTimeMillis();

        pool.invoke(matrix);

        final long ep = System.currentTimeMillis();
        final Duration p = Duration.ofNanos(ep - sp);

        String str = "ForkJoinPool;" + matrix.A.length + "x" + matrix.A[0].length + ";" + p.getNano() + "\n";

        appendStrToFile(podsumowanie, str);
    }
}
