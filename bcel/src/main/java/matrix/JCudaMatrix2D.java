/*
The program shows the expected effect of the file transformation:
Matrix. class (look at the Matrix2.java file)
the simplification consists in transferring only 2 parameters (arrays)
to the function that will perform the JCUDA multiplication
 */

package mbuilder._matrix;

import pl.edu.agh.utils.JCudaMatrix;

import java.util.Arrays;

public class JCudaMatrix2D {

    public static void main(String[] args) {

        int[][] A = {{2,4},{1,3}};
        int[][] B = {{2,4},{1,3}};

        JCudaMatrix jcm = new JCudaMatrix(A,B);
        float[] C = jcm.multiply();
        System.out.println(Arrays.toString(C));
    }
}
