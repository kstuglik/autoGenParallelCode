package mbuilder;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.TargetLostException;
import pl.edu.agh.transformations.MyBcModifier;
import pl.edu.agh.transformations.util.MethodUtils;
import pl.edu.agh.transformations.util.New;
import pl.edu.agh.transformations.util.TransformUtils;

import java.io.IOException;
import java.util.Arrays;

public class MainReactivateEdition {

    private static String CLASS_NAME;
    private static String CLASS_PATH;
    private static String CLASS_METHOD;

    public static void main(String[] args) throws IOException, TargetLostException {

        CLASS_PATH = "src/main/java/mbuilder/classFiles/";

/*        1 nbody parallel */
/*        3 jcuda for multiply*/

        int choice = 3;

        switch (choice){
            case 1:// NBODY CASE
                CLASS_NAME = "IntegrationTestClass";
                CLASS_METHOD = "moveBodies";
                break;

            case 2:// NEW CASE FOR JCUDA (activities in progress)
                CLASS_NAME = "Matrix2D_v2";
                CLASS_METHOD = "multiply";
                break;

            case 3:
                CLASS_NAME = "Example";
                CLASS_METHOD = "doSomething";
                break;

            default:
                System.out.println("incorrect value, select a number between 1 - 2");
                break;
        }


        JavaClass analyzedClass = new ClassParser(CLASS_PATH+CLASS_NAME+".class").parse();
        ClassGen modifiedClass = new ClassGen(analyzedClass);

        Method[] methods = modifiedClass.getMethods();
        int methodPositionId = (int) MyBcModifier.GetMethodIndex(methods,CLASS_METHOD);
        Method transformedMethod = methods[methodPositionId];//        Method transformedMethod = modifiedClass.getMethodAt(2);

        MethodGen mg = new MethodGen(transformedMethod, modifiedClass.getClassName(), modifiedClass.getConstantPool());
        ConstantPoolGen cp = modifiedClass.getConstantPool();

//        TRANSFORMATION
        if(choice == 2 || choice == 3){
            //transformation for jcuda
            TransformUtils.addCallJMultiply(modifiedClass, mg);
        }
        else{
            //tansformation for parallel

          /*  TransformUtils.addThreadPool(modifiedClass);
            TransformUtils.addExecutorServiceInit(modifiedClass, mg);
            TransformUtils.addTaskPool(modifiedClass, mg);*/
            TransformUtils.addFutureResultsList(modifiedClass, mg);
            /*TransformUtils.copyLoopToMethod(modifiedClass, mg);
            TransformUtils.changeLoopLimitToNumberOfThreads(modifiedClass, mg);
            TransformUtils.emptyMethodLoop(modifiedClass, mg);
            short dataSize = 1000;
            TransformUtils.setNewLoopBody(modifiedClass, mg, dataSize);*/

//            AnonymousClassUtils.addCallableCall(modifiedClass, classPath);
        }

        New.saveNewClassFile(modifiedClass,CLASS_PATH,CLASS_NAME);

    }

    private static ClassGen getModifiedClass(String className, JavaClass analyzedClass) {
        ClassGen oldClass = new ClassGen(analyzedClass);
        return new ClassGen(analyzedClass.getClassName() ,
                            Object.class.getName(),
                            "<generated>",
                            Const.ACC_PUBLIC,
                            null,
                            oldClass.getConstantPool());
    }

    private static void copyMethods(JavaClass oldClass, ClassGen newClass) {
        Arrays.stream(oldClass.getMethods())
                .forEach(newClass::addMethod);
        Arrays.stream(newClass.getMethods())
                .forEach(method -> MethodUtils.switchConstantRefsToNewClass(newClass, method));
    }

    private static void copyFields(JavaClass oldClass, ClassGen newClass) {
        Arrays.stream(oldClass.getFields())
                .forEach(newClass::addField);
    }

/*    ByteCodeModifier bcn = new ByteCodeModifier();
    bcn.modifyBytecode(CLASS_PATH, CLASS_NAME, 2, (short) 1000);*/

}