package nbody;

public class SerialNbody {

    private static final Body[] bodies = DataInitializer.initBodiesFromFile();
    private static Body[] beginningState;
    private static final double dt = 0.001;

    public static Body[] getBodies() {
        return bodies;
    }

    public static void simulate(int steps) {
        for (int i = 0; i < steps; i++) {
            moveBodies();
        }
    }

    private static void moveBodies() {
        int dataSize = bodies.length;
        refreshBeginningState();
        for (int i = 0; i < dataSize; i++) {
            Body body = bodies[i];
            updateState(body);
        }
    }

    private static void refreshBeginningState() {
        int dataSize = bodies.length;
        beginningState = new Body[dataSize];
        for (int i = 0; i < dataSize; i++) {
            beginningState[i] = new Body(bodies[i]);
        }
    }

    private static void updateState(Body body) {
        double netForceX = 0.0;
        double netForceY = 0.0;
        for (Body otherBody : beginningState) {
            if (otherBody == body) {
                continue;
            } else {
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

//    private static final String path = "/src/main/java/mbuilder/nbody/bodies.dat";


    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            System.out.println(bodies[i].mass);
        }
//        WriteObjectToFile(path, bodies);
    }
//
//    public static Body[] initBodiesFromFile() {
//        return (Body[]) ReadObjectFromFile(filepath);
//    }
//
//    public static void WriteObjectToFile(String filepath, Object serObj) {
//
//        try {
//
//            FileOutputStream fileOut = new FileOutputStream(filepath);
//            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
//            objectOut.writeObject(serObj);
//            objectOut.close();
//            System.out.println("The Object  was succesfully written to a file");
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    public static Object ReadObjectFromFile(String filepath) {
//
//        try {
//
//            FileInputStream fileIn = new FileInputStream(filepath);
//            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
//
//            Object obj = objectIn.readObject();
//
//            System.out.println("The Object has been read from the file");
//            objectIn.close();
//            return obj;
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            return null;
//        }
//    }
}
