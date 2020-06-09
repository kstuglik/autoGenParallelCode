/*
The program shows the expected effect of the file transformation:
Matrix. class (look at the Matrix2.java file)
the simplification consists in transferring only 2 parameters (arrays)
to the function that will perform the JCUDA multiplication
 */

package mbuilder.Matrix;

import utils.JCudaMatrix;
import java.util.Arrays;

public class JCudaMatrix2D {

    public static void main(String[] args) {

        int[][] A = {{3,2,0},{0,4,1},{2,0,1}};
        int[][] B = {{4},{3},{1}};

        if (A[0].length != B.length) {
            throw new RuntimeException("Dims aren't equal.");
        }

        JCudaMatrix jcm = new JCudaMatrix(A,B);
        float[] C = jcm.multiply();

        System.out.println(Arrays.toString(C));
    }
}
