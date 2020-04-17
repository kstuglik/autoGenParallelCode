package MatrixBuilder;

import java.util.Arrays;


public class MatrixExample{

    public static void main(String[] args) {

        //columnar representation of the matrix
        int[] A = {1,4,2,5,3,6};
        A[0] = 10;
        int colsA = 3;
        int rowsA = 2;

        int[] B = {7,9,11,8,10,12};
        int colsB = 2;
        int rowsB = 3;

        if(colsA == rowsB){

            int[] C = new int[rowsA*colsB];

            for(int i=0; i<rowsA; i++){

                for(int j=0; j<colsB; j++){
                    C[i*rowsA+j] = 0;

                    for(int k=0; k<rowsB; k++){

                        int a = A[k*rowsA+j];
                        int b = B[k+colsA*i];
                        C[i*rowsA+j] += a*b;

                    }
                }
            }

            System.out.println(Arrays.toString(C));

        }
        else{
            System.out.println("cols_A != rows_B");
        }

    }
}