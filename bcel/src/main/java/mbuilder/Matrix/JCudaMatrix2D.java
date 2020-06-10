/*
The program shows the expected effect of the file transformation:
Matrix. class (look at the Matrix2.java file)
the simplification consists in transferring only 2 parameters (arrays)
to the function that will perform the JCUDA multiplication
 */

package mbuilder.Matrix;

import utils.JCudaMatrix;

public class JCudaMatrix2D {

    public static void main(String[] args) {

        int[][] A = {{1},{1}};
        int[][] B = {{1},{1}};

        JCudaMatrix jcm = new JCudaMatrix(A,B);
        float[] C = jcm.multiply();
    }
}
