import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.MethodGen;
import pl.edu.agh.bcel.ByteCodeModifier;
import pl.edu.agh.bcel.LaunchProperties;
import pl.edu.agh.bcel.transformation.Structure;
import pl.edu.agh.bcel.utils.ReadyFields;
import pl.edu.agh.bcel.utils.ReadyMethods;
import pl.edu.agh.bcel.utils.TransformUtils;

import java.io.IOException;


public class java {

    public static void main(String[] args) throws IOException {
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j <100; j+=5) {
                System.out.println(i+j);
            }
        }
    }
}
