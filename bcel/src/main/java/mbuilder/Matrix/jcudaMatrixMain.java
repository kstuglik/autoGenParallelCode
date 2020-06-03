/*
The program shows the expected effect of the file transformation:
Matrix. class (look at the Matrix2.java file)
the simplification consists in transferring only 2 parameters (arrays)
to the function that will perform the JCUDA multiplication
 */

package mbuilder.Matrix;

import utils.JCudaMatrix;
import java.util.Arrays;

public class jcudaMatrixMain {
    public static void main(String[] args) {

        int[][] A = {{3,2,0},{0,4,1},{2,0,1}};
        int[][] B = {{4},{3},{1}};

        JCudaMatrix jcudaMatrix = new JCudaMatrix(A,B);
        float[] C = jcudaMatrix.multiply();

        System.out.println(Arrays.toString(C));
    }
}
