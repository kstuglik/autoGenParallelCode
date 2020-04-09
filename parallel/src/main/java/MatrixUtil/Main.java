package MatrixUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {

    public static double[][] generateMatrix(int size) {
        Random rand = new Random();

        double[][] result = new double[size][size];
        for (int i = 0; i < size; i++) {

            for (int j = 0; j < size; j++) {
                result[i][j] = rand.nextDouble();
            }
        }
        return result;
    }

    public static void writeIntoFile(String filename, double[][] x) throws IOException {
        BufferedWriter outputWriter = null;
        outputWriter = new BufferedWriter(new FileWriter(filename));
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[0].length; j++) {
                outputWriter.write(x[i][j] + " ");
            }
            outputWriter.newLine();
        }
        outputWriter.flush();
        outputWriter.close();
    }

    public static void appendStrToFile(String fileName, String str) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(fileName, true));
            out.write(str);
            out.close();
        } catch (IOException e) {
            System.out.println("exception occoured" + e);
        }
    }

    public static double[][] readMatrix(String filename) {
        double[][] matrix = null;
        try (BufferedReader br = new BufferedReader(new FileReader(new File(filename)))) {
            String line;
            List<String[]> list = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                String[] args = line.split(" ");
                try {
                    list.add(args);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            matrix = new double[list.size()][list.get(0).length];
            for (int i = 0; i < list.size(); i++) {
                for (int j = 0; j < list.get(0).length; j++) {
                    matrix[i][j] = Double.parseDouble(list.get(i)[j]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return matrix;
    }

}
