package mbuilder;

import pl.edu.agh.transformations.ByteCodeModifier;
import pl.edu.agh.transformations.LaunchProperties;

import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void menu(boolean b) {

        if (b) {
            menuUserSelection();
        } else {
            menuAutoSelection();
        }

        switch (LaunchProperties.CHOICE) {
            case 1: // NBODY CASE
                LaunchProperties.CLASS_FILE = "IntegrationTestClass.class";
                LaunchProperties.CLASS_METHOD = "moveBodies";
                break;
            case 2:
                LaunchProperties.CLASS_FILE = "Example.class";
                LaunchProperties.CLASS_METHOD = "doSomething";
                break;
            case 3:
                LaunchProperties.CLASS_FILE = "Multiply.class";
                LaunchProperties.CLASS_METHOD = "multiply";
                break;
            default:
                System.out.println("BAD CHOICE!");
                break;
        }
    }

    private static void menuUserSelection() {
        Scanner in = new Scanner(System.in);
        System.out.println(
                "MENU: choose option:\n" +
                        "\tchoice:1 && option: any number => nbody\n" +
                        "\tchoice:2 && option: 1 or 3 => Example_MOD.class\n" +
                        "\t\tadd a static piece of code to the program\n" +
                        "\tchoice:3 && option: 4 => add invoke methode jcm.multiply\n" +
                        "\t\tadd call instructions for the jcuda method to perform multiplication\n\n" +
                        "\tyou can also change params in file:\n" +
                        "\t\tpl.edu.agh.transformations.util.TransformUtils.LaunchProperties\n\n" +
                        "your choice:\t"
        );
        LaunchProperties.CHOICE = in.nextInt();

        if (LaunchProperties.CHOICE >= 1 && LaunchProperties.CHOICE <= 4) {
            System.out.println("@param option - operation variant for insertion instructions [1,3]:");
            LaunchProperties.OPTION = in.nextInt();
        }
    }

    private static void menuAutoSelection() {
        LaunchProperties.CHOICE = 3;
        LaunchProperties.OPTION = 4;
    }

    public static void main(String[] args) throws IOException {

        menu(false);
        ByteCodeModifier o = new ByteCodeModifier();
        o.initialize();
        o.transformation();

    }

}