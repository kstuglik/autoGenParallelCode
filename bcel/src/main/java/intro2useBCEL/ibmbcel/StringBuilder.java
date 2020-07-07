/*
Source: https://www.ibm.com/developerworks/library/j-dyn0414/

set program arguments: int int ...
java intro.ibmbcel.StringBuilder.java 1000 2000

recompile
*/

package intro2useBCEL.ibmbcel;
public class StringBuilder
{
    String buildString(int length) {
        String result = "";
        for (int i = 0; i < length; i++) {
            result += (char)(i%26 + 'a');
        }
        return result;
    }

    public static void main(String[] argv) {
        StringBuilder inst = new StringBuilder();
        for (int i = 0; i < argv.length; i++) {
            String result = inst.buildString(Integer.parseInt(argv[i]));
            System.out.println("Constructed string of length " +
                    result.length());
        }
    }
}
