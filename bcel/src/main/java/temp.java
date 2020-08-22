public class temp {
    static int resultRows = 2;
    static int resultColumns = 3;


    public static void main(String[] args) {
        multiply();
    }

    public static void multiply() {
        for (int j = 0; j < 10 && j > 2; j++) {
            int ss = 5;
            if ((j + ss) % 2 == 0)
                System.out.println("j%2=0");
            for (int rowNum = 0; rowNum < resultRows && rowNum % 2 == 0 || rowNum > 1; rowNum++) {
                int suma = 10 + rowNum;
                if (suma / 4 == 0) {
                    System.out.println("suma/4 == 0, " + rowNum);
                } else System.out.println(suma);
                suma *= 2;
                System.out.println(suma);
                if (suma / 2 == 0) {
                    System.out.println("suma/2 == 0, " + rowNum);
                } else System.out.println(suma);
                suma *= 0.5;
                System.out.println(suma);
            }
        }
        System.out.println("TO JUZ JEST KONIEC");
    }

}
