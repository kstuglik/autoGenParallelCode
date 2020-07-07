package intro2useBCEL.apachebcel;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class HelloWorld {
    public static void main(String[] argv) {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String name = null;

        try {
            System.out.print("Please enter your name> ");
            name = in.readLine();
        } catch (IOException var3) {
            System.out.println(var3);
            return;
        }

        System.out.println("Hello, " + name);
    }

    public HelloWorld() {
    }
}