package nbody;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class IntegrationTest {
    public static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
    private static final double dt = 0.001D;
    public static ExecutorService SERVICE;
    private static final Body[] bodies = DataInitializer.initBodiesFromFile();
    private static Body[] beginningState;
    private static int START_RANGE;
    private static int END_RANGE;

    static {
        SERVICE = Executors.newFixedThreadPool(NUM_THREADS);
    }

    public IntegrationTest() {
    }

    public static Body[] getBodies() {
        return bodies;
    }

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            System.out.println(bodies[i].ax);
        }
        simulate(10);
        System.out.println("*****************************************");
        for (int i = 0; i < 10; i++) {
            System.out.println(bodies[i].ax);
        }
    }

    private static void moveBodies() throws InterruptedException {
        List partialResults = new ArrayList<>();
        SERVICE = Executors.newFixedThreadPool(NUM_THREADS);
        List tasks = new ArrayList();
        int dataSize = bodies.length;
        refreshBeginningState(dataSize);

        for (int i = 0; i < NUM_THREADS; ++i) {
            final int start = i * (16 / NUM_THREADS);
            final int end = (i + 1) * (16 / NUM_THREADS) - 1;
            tasks.add(new Callable<Integer>() {
                public Integer call() throws Exception {
                    return IntegrationTest.subTask(start, end);
                }
            });
        }

        partialResults = SERVICE.invokeAll(tasks);
        SERVICE.shutdown();
    }

    private static void refreshBeginningState(int dataSize) {
        beginningState = new Body[dataSize];

        for (int i = 0; i < dataSize; ++i) {
            beginningState[i] = new Body(bodies[i]);
        }

    }

    public static void simulate(int steps) throws InterruptedException {
        for (int i = 0; i < steps; ++i) {
            moveBodies();
        }

    }

    public static int subTask(int start, int end) {
        for (int i = start; i <= end; ++i) {
            Body body = bodies[i];
            updateState(body);
        }

        return 1;
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
