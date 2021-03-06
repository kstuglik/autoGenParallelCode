package nbody;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParallelNbody {

    private static final Body[] bodies = DataInitializer.initBodiesFromFile();
    private static final double dt = 0.001;
    private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
    private static Body[] beginningState;
    private static ExecutorService SERVICE;

    public static Body[] getBodies() {
        return bodies;
    }

    public static ArrayList init() {
        return new ArrayList();
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            System.out.println(bodies[i].x);
        }
        simulate(10);
        System.out.println("*******************");
        for (int i = 0; i < 10; i++) {
            System.out.println(bodies[i].x);
        }
    }

    private static void moveBodies() {
        SERVICE = Executors.newFixedThreadPool(NUM_THREADS);
        List<Callable<Integer>> tasks = init();
        int dataSize = bodies.length;

        refreshBeginningState(dataSize);
        for (int i = 0; i < NUM_THREADS; i++) {
            int start = i * (dataSize / NUM_THREADS);
            int stop = setStop(i, dataSize, NUM_THREADS);
            tasks.add(new Callable<Integer>() {
                public Integer call() {
                    return partialUpdate(start, stop);
                }
            });
        }
        try {
            SERVICE.invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        SERVICE.shutdown();
    }

    private static int partialUpdate(int start, int stop) {
        for (int i = start; i <= stop; i++) {
            Body body = bodies[i];
            updateState(body);
        }
        return 0;
    }

    private static void refreshBeginningState(int dataSize) {
        beginningState = new Body[dataSize];//we need deep copy
        for (int i = 0; i < dataSize; i++) {
            beginningState[i] = new Body(bodies[i]);
        }
    }

    private static int setStop(int i, int dataSize, int numThreads) {
        int var3 = (i + 1) * (dataSize / numThreads) - 1;
        if (var3 >= dataSize) {
            var3 = dataSize - 1;
        }

        return var3;
    }

    public static void simulate(int steps) {
        for (int i = 0; i < steps; i++) {
            moveBodies();
        }
    }

    private static void updateState(Body body) {
        double netForceX = 0.0;
        double netForceY = 0.0;
        for (Body otherBody : beginningState) {
            if (otherBody != body) {
                netForceX += body.pairForceX(otherBody);
                netForceY += body.pairForceY(otherBody);
            }
        }
        body.ax = netForceX / body.mass;
        body.ay = netForceY / body.mass;
        body.vx += body.ax * dt;
        body.vy += body.ay * dt;
        body.x += body.vx * dt;
        body.y += body.vy * dt;
    }

}
