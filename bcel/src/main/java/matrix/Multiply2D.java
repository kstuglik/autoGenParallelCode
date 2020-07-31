package matrix;

import java.util.Arrays;

class Multiply2D {
    public static void multiplyMatrix() {
        int[][] var1 = new int[][]{{2, 4}, {1, 3}};
        System.out.println(Arrays.deepToString(var1));
        int[][] var2 = new int[][]{{2, 4}, {1, 3}};
        System.out.println(Arrays.deepToString(var2));
    }

    public static void main(String[] args) {
        multiplyMatrix();
    }
}