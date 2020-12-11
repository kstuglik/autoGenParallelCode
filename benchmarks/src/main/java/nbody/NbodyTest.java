package nbody;

public class NbodyTest {

    private static final int steps = 100;

    public static void main(String[] args) {

        Body[] bodies = SerialNbody.getBodies("src/main/java/nbody/bodies.dat");
        Body[] bodies2 = ParallelNbody.getBodies("src/main/java/nbody/bodies.dat");

        System.out.println(bodies[0].x + ", " + bodies[0].y);
        System.out.println(bodies2[0].x + ", " + bodies2[0].y);

        long start = System.currentTimeMillis();
        SerialNbody.simulate(steps);
        System.out.println(System.currentTimeMillis() - start);

        start = System.currentTimeMillis();
        ParallelNbody.simulate(steps);
        System.out.println(System.currentTimeMillis() - start);
        System.out.println(bodies[0].x + ", " + bodies[0].y);
        System.out.println(bodies2[0].x + ", " + bodies2[0].y);
    }

}
