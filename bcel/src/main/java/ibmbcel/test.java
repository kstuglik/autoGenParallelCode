//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package ibmbcel;

import java.io.PrintStream;

public class test {
    public test() {
    }

    public static void main(String[] args) {
        String res = buildString(2000);
        System.out.println(res);
    }

    public static String buildString$impl(int length) {
        String result = "";

        for(int i = 0; i < length; ++i) {
            result = result + Integer.toString(i) + "->";
        }

        return result;
    }

    public static String buildString(int var0) {
        long var1 = System.currentTimeMillis();
        String var3 = buildString$impl(var0);
        PrintStream var10000 = System.out;
        var10000.print("Call to method buildString$impl took ");
        var10000.print(System.currentTimeMillis() - var1);
        var10000.println(" ms.");
        return var3;
    }
}
