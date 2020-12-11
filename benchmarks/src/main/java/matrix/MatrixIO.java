package matrix;

import pl.edu.agh.bcel.utils.ArrayUtils;

import java.io.IOException;
import java.util.Arrays;

public class MatrixIO {
    public static void main(String[] args) throws IOException {
        for (int dim = 8; dim <= 4096; dim *= 2) {

            int[][] matrix = ArrayUtils.generateIArray2D(dim, dim, 100);
            String fileName = "benchmarks/src/main/resources/matrix" + dim + ".txt";

            System.out.println("zapisywana tablica: " + dim + " x " + dim);
            if (dim <= 16) System.out.println(Arrays.deepToString(matrix));
            ArrayUtils.writeMatrix2D(fileName, matrix);

            if (dim <= 16) {
                int[][] matrix2 = ArrayUtils.readMatrix2D(fileName);
                System.out.println("odczytana tablica:");
                System.out.println(Arrays.deepToString(matrix2));
            }
        }

    }
}
