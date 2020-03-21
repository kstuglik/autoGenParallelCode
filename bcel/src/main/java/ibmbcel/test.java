package ibmbcel;

import java.io.PrintStream;

public class test {
    public test() {
    }

    public static void main(String[] argv) {
        test inst = new test();

        for(int i = 0; i < argv.length; ++i) {
            String result = inst.buildString(Integer.parseInt(argv[i]));
            System.out.println("Constructed string of length " + result.length());
        }

    }

    String buildString$impl(int length) {
        String result = "";

        for(int i = 0; i < length; ++i) {
            result = result + (char)(i % 26 + 97);
        }

        return result;
    }

    String buildString(int var1) {
        long var2 = System.currentTimeMillis();
        String var4 = this.buildString$impl(var1);
        PrintStream var10000 = System.out;
        var10000.print("Call to method buildString$impl took ");
        var10000.print(System.currentTimeMillis() - var2);
        var10000.println(" ms.");
        return var4;
    }
}