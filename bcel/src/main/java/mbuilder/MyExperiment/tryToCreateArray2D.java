
package mbuilder.MyExperiment;

import org.apache.bcel.Const;
import org.apache.bcel.generic.*;
import pl.edu.agh.transformations.util.New;

import java.io.IOException;


public class tryToCreateArray2D {

    public static final String MODIFICATION_SUFFIX = "_mod";
    private static final String CLASS_SUFFIX = ".class";
    private static final String JAVA_SUFFIX = ".java";
    private static final String packageName = "zmodyfikowana";

    private static ClassGen cg;
    private static ConstantPoolGen cp;
    private static InstructionList il;
    private static InstructionFactory factory;
    private static MethodGen mg;
    private static LocalVariableGen lg;
    private static ObjectType p_stream;


    public void modifyBytecode(String classPath, String className, String methodName) {
        String fullClassName = packageName + "." + className;
        cg = new ClassGen(fullClassName, "java.lang.Object", "<generated>",
                Const.ACC_PUBLIC | Const.ACC_SUPER, null);
        cp = cg.getConstantPool(); // cg creates constant pool
        il = new InstructionList();
        factory = new InstructionFactory(cg);
        mg = new MethodGen(Const.ACC_PUBLIC | Const.ACC_STATIC,
                Type.VOID, new Type[]{new ArrayType(Type.STRING, 1)},
                new String[]{"argv"}, "main", className, il, cp);
        p_stream = new ObjectType("java.io.PrintStream");

        New.create_array_2D("arrayA",mg,il,cp,Type.INT,5, new int[]{1, 2, 3, 4, 5});

        saveModifiedClass(classPath,className,cg);
    }

    private void saveModifiedClass(String classPath, String className, ClassGen cg) {

       try{
           mg.setMaxStack();//mg - MethodGen object
           mg.setMaxLocals();
//           mg.stripAttributes(true);

           cg.addMethod(mg.getMethod());//cg - ClassGen object
           cg.addEmptyConstructor(Const.ACC_PUBLIC);

           il.dispose();//il - InstructionList

           String pathOutput = classPath + "/" + className + MODIFICATION_SUFFIX + CLASS_SUFFIX;

           cg.getJavaClass().dump(pathOutput);
        } catch (IOException e) {
            throw new RuntimeException("Error during modified class save.", e);
        }
        System.out.println("DONE!");
    }

    public static void main(String[] args) throws IOException, TargetLostException {

        String CLASS_PATH = "src/main/java/mbuilder/classFiles/";
        String CLASS_NAME = "Array2D";
        String METHOD_NAME = "Main";

        tryToCreateArray2D newClass = new tryToCreateArray2D();
        newClass.modifyBytecode(CLASS_PATH, CLASS_NAME, METHOD_NAME);
    }
}
