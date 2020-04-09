/*
SOURCE: https://itsallbinary.com/do-your-own-static-code-analysis-programmatically-in-java-similar-to-findbugs-using-apache-bcel/
*/
package itsallbinary;

import org.apache.bcel.Const;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.util.ByteSequence;

import java.io.IOException;

public class BCEX {
    public static void main(String[] args) throws ClassFormatException, ClassNotFoundException {
        Class<?>[] classesToAnalyze = new Class[] { AnalyzeMeGoodCode.class, AnalyzeMeBadCode.class };

        for (Class<?> classToAnalyze : classesToAnalyze) {
            JavaClass javaClass = Repository.lookupClass(classToAnalyze);
            boolean hasEmptySyncBlock = false;
            int lineNumberOfEmptySyncBlock = -1;

            for (Method method : javaClass.getMethods()) {
                byte[] code = method.getCode().getCode();
                try {
                    ByteSequence stream = new ByteSequence(code);
                    short previousOpCode = -1;

                    for (int i = 0; stream.available() > 0; i++) {
                        int lineNumber = stream.getIndex();
                        short opCode = (short) stream.readUnsignedByte();
                        System.out.println(lineNumber + " = " + Const.getOpcodeName(opCode));
                        /*If previous line of code was start of synchronized block & this line of code
                         * is end of synchronized block, that means there is an empty synchronized
                         * block. Mark this as problem.*/
                        if (previousOpCode != -1 && Const.MONITORENTER == previousOpCode && Const.MONITOREXIT == opCode)
                        {
                            hasEmptySyncBlock = true;
                            lineNumberOfEmptySyncBlock = lineNumber;
                        }
                        previousOpCode = opCode;
                    }
                } catch (final IOException e) {e.printStackTrace();}
            }
            if (hasEmptySyncBlock) {
                System.out.println("##### PROBLEM: \n\tClass = " + classToAnalyze.getName() + " | Line Number = "
                        + lineNumberOfEmptySyncBlock
                        + "\n\tError = Empty synchronized block found. Please verify & remove if not needed.");
            } else {System.out.println("##### ALL GOOD. Class = " + classToAnalyze.getName());}
        }
    }
}
