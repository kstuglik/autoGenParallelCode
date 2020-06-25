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
    private static String ERR_MESSAGE = "IT WAS NOT POSSIBLE to add a new piece of code!\n\t";

    public static void main(String[] args) throws IOException, TargetLostException {

        CLASS_PATH = "src/main/java/mbuilder/classFiles/";

        /*choice:2 and option:1 or 3 => Example_MOD.class*/
        /*choice:1 and option: NONE => nbody*/
        /*choice:3 and option: 4 => add invoke methode jcm.multiply*/


        int choice = 3;
        int option = 4; //operation variant for insertion instructions

        switch (choice){
            case 1:// NBODY CASE
                CLASS_NAME = "IntegrationTestClass";
                CLASS_METHOD = "moveBodies";
                break;
            case 2:
                CLASS_NAME = "Example";
                CLASS_METHOD = "doSomething";
                break;
            case 3:
                CLASS_NAME = "Multiply";
                CLASS_METHOD = "multiply";
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

        try {
            //transformation for jcuda
            if (choice == 2 || choice == 3 || choice == 4)
                TransformUtils.insertNewInstruciton(modifiedClass, mg, option);
            else {
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

            New.saveNewClassFile(modifiedClass, CLASS_PATH, CLASS_NAME);
        } catch (IOException e) {
            System.err.println(ERR_MESSAGE + e.getClass() + "\n\t" + e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println(ERR_MESSAGE + e.getClass() + "\n\t" + e.getMessage());
        } catch (Exception e) {
            System.err.println(ERR_MESSAGE + e.getClass() + "\n\t" + e.getMessage());
        }

    }

}