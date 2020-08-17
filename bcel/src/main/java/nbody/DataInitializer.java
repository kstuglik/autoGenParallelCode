package nbody;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class DataInitializer {
    private static final String filePath = "src/main/java/nbody/bodies.dat";

    public static Body[] initBodiesFromFile() {
        try {
            return (Body[]) new ObjectInputStream(new FileInputStream(filePath)).readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
