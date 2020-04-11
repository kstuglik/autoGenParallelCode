package MatrixExecutorService;

import java.time.Duration;

import static MatrixUtil.Main.appendStrToFile;
import static MatrixUtil.Main.readMatrix;

public class Main {

    public static void main(String[] args) {
        String podsumowanie = "src/main/resources/podsumowanie.txt";

        double[][] A = readMatrix("src/main/resources/A.txt");
        double[][] B = readMatrix("src/main/resources/B.txt");

        Multiplier parallelMultiplier = new Multiplier(A, B);

        final long sp = System.currentTimeMillis();
        parallelMultiplier.multiply();
        final long ep = System.currentTimeMillis();

        final Duration p = Duration.ofNanos(ep - sp);
        String str = "ExecutorService;" + A.length + "x" + A[0].length + ";" + p.getNano() + "\n";

        appendStrToFile(podsumowanie, str);
    }
}
