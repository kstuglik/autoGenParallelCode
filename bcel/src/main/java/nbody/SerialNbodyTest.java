package nbody;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SerialNbodyTest {
    public static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
    private static final Body[] bodies = DataInitializer.initBodiesFromFile();
    private static final double dt = 0.001D;
    public static ExecutorService SERVICE;
    private static Body[] beginningState;

    public SerialNbodyTest() {
    }

    public static Body[] getBodies() {
        return bodies;
    }

    public static void main(String[] args) {
        simulate(8);

    }

    private static void moveBodies() {
        SERVICE = Executors.newFixedThreadPool(NUM_THREADS);

        List<Callable<Integer>> tasks = new LinkedList();
        System.out.println(tasks);
//        int dataSize = bodies.length;
        refreshBeginningState();

        for (int i = 0; i < NUM_THREADS; ++i) {
            int start = i * (1000 / NUM_THREADS);
            int end = (i + 1) * (1000 / NUM_THREADS) - 1;
            tasks.add(() -> {
                return partialUpdate(start, end);
            });
        }

        try {
            SERVICE.invokeAll(tasks);
        } catch (InterruptedException var6) {
            var6.printStackTrace();
        }

        SERVICE.shutdown();
    }

    public static int partialUpdate(int start, int end) {
        for (int i = start; i <= end; ++i) {
            Body body = bodies[i];
            updateState(body);
        }

        return 0;
    }

    private static void refreshBeginningState() {
        int dataSize = bodies.length;
        beginningState = new Body[dataSize];

        for (int i = 0; i < dataSize; ++i) {
            beginningState[i] = new Body(bodies[i]);
        }

    }

    public static void simulate(int steps) {
        for (int i = 0; i < steps; ++i) {
            moveBodies();
        }

    }

    private static void updateState(Body body) {
        double netForceX = 0.0D;
        double netForceY = 0.0D;
        Body[] var5 = beginningState;
        int var6 = var5.length;

        for (int var7 = 0; var7 < var6; ++var7) {
            Body otherBody = var5[var7];
            if (otherBody != body) {
                netForceX += body.pairForceX(otherBody);
                netForceY += body.pairForceY(otherBody);
            }
        }

        body.ax = netForceX / body.mass;
        body.ay = netForceY / body.mass;
        body.vx += body.ax * 0.001D;
        body.vy += body.ay * 0.001D;
        body.x += body.vx * 0.001D;
        body.y += body.vy * 0.001D;
    }
}