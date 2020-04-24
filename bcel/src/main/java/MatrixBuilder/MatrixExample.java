package MatrixBuilder;

import java.util.Arrays;

public class MatrixExample{

    public static void main(String[] args) {
        //columnar representation of the matrix
        int[] A = {1,4,2,5,3,6};
        int colsA = 3;
        int rowsA = 2;

        int[] B = {7,9,11,8,10,12};
        int colsB = 2;
        int rowsB = 3;

        int[] C = new int[rowsA*colsB];

        if(colsA != rowsB){System.out.println("cols_A != rows_B"); }
        else{
            for(int i=0; i<rowsA; i++){
                for(int j=0; j<colsB; j++){
                    int index = i*rowsA+j;
                    C[index] = 0;
                    for(int k=0; k<rowsB; k++){
                        int id_a = k*rowsA+j;
                        int id_b = k+colsA*i;
                        C[index] += A[id_a]*B[id_b];
                    }
                    System.out.println(C[index]);
                }
            }
            System.out.println(Arrays.toString(C));
        }
    }
}
