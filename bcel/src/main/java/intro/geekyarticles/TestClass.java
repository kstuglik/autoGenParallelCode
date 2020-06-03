package intro.geekyarticles;

public class TestClass {
    public static void main(String[] args) {
        switch(Integer.parseInt(args[0])) {
            case 1:
                System.out.println("One");
                break;
            case 2:
                System.out.println("Two");
                break;
            case 4:
                System.out.println("Four");
                break;
            default:
                System.out.println("Other");
        }

    }
}