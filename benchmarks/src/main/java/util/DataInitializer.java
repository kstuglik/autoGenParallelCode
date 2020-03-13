package util;

import nbody.Body;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class DataInitializer {
    public static Body[] initBodiesFromFile() {
        try {
            return (Body[]) new ObjectInputStream(new FileInputStream("autoparallel-benchmarks\\src\\main\\resources\\bodies.dat")).readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
