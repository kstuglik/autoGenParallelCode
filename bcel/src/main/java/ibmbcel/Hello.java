package ibmbcel;

public class Hello {
    public static String buildString(int length) {
        String result = "";
        for (int i = 0; i < length; i++) {
            result += Integer.toString(i)+"->";
        }
        return result;
    }

    public static void main(String[] args) {
        String res = buildString(20);
        System.out.println(res);
    }
}
