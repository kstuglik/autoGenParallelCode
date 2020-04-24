package MatrixBuilder;




public class test {
    public static void main(String[] argv) {
        int[] A = {1,4,2,5,3,6};
        int colsA = 3;
        int rowsA = 2;

        int[] B = {7,9,11,8,10,12};
        int colsB = 2;
        int rowsB = 3;

        int width = rowsA * colsB;
        int[] C = new int[width];

        for(int i = 0; i < rowsA; ++i) {
            for(int j = 0; j < colsB; ++j) {
                int id_c = i * rowsB + j;
                int sum = 0;

                for(int k = 0; k < rowsB; ++k) {
                    int id_a = k * rowsA + j;
                    int id_b = i * colsA + k;
                    sum += A[id_a] * B[id_b];
                }

                System.out.println(sum);
            }
        }

    }
}