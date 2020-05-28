package MatrixBuilder;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.TargetLostException;
import pl.edu.agh.transformations.util.MethodUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class bcModifier {

    public static final String MODIFICATION_SUFFIX = "_mod";
    private static final String CLASS_SUFFIX = ".class";
    private static final String JAVA_SUFFIX = ".java";
    private static final String packageName = "zmodyfikowana";


    public void modifyBytecode(String classPath, String className, String methodName) throws IOException, TargetLostException {

        JavaClass jclass = new ClassParser(classPath + className + CLASS_SUFFIX).parse();
        ClassGen cg = new ClassGen(jclass);

        Method[] methods = jclass.getMethods();

        int methodPositionId;
        for (methodPositionId = 0; methodPositionId < methods.length; methodPositionId++) {
            if(methods[methodPositionId].getName().equals(methodName)){ break;}
        }

        if(methodPositionId<methods.length){
            addWraper(cg,methods[methodPositionId]);
        }else{
            System.err.println("Method: "+methodName+" not found in "+className);
        }

        String pathToModifiedFile = classPath + className + MODIFICATION_SUFFIX + CLASS_SUFFIX;
        saveModifiedClass(pathToModifiedFile,className,cg);
    }

    private void addWraper(ClassGen cg, Method method) {
    }

    //     ******************* ZMODYFIKOWANA METODA ZAPISU *******************
    private void saveModifiedClass(String pathToModifiedFile, String className, ClassGen cg) {
       try{
           FileOutputStream fos = new FileOutputStream(pathToModifiedFile);
           cg.getJavaClass().dump(fos);
           fos.close();
        } catch (IOException e) {
            throw new RuntimeException("Error during modified class save.", e);
        }
        System.out.println("SAVED!");
    }

//    ************************* ORYGINALNE METODY ************************

    private void copyMethods(JavaClass oldClass, ClassGen newClass) {
        Arrays.stream(oldClass.getMethods())
                .forEach(newClass::addMethod);
        Arrays.stream(newClass.getMethods())
                .forEach(method -> MethodUtils.switchConstantRefsToNewClass(newClass, method));
    }

    private void copyFields(JavaClass oldClass, ClassGen newClass) {
        Arrays.stream(oldClass.getFields())
                .forEach(newClass::addField);
    }

    private ClassGen getModifiedClass(String className, JavaClass jclass) {
        ClassGen oldClass = new ClassGen(jclass);
        return new ClassGen(jclass.getPackageName() + className + MODIFICATION_SUFFIX,
                Object.class.getName(),
                className + MODIFICATION_SUFFIX + JAVA_SUFFIX,
                Const.ACC_PUBLIC,
                null,
                oldClass.getConstantPool());
    }
}
