package nbody;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class DataInitializer {
    public static Body[] initBodiesFromFile(String DATA_FILE) {
        try {
            return (Body[]) new ObjectInputStream(new FileInputStream(DATA_FILE)).readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
