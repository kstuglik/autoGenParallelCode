package pl.edu.agh.transformations;

import mbuilder.TempExperiments.MyBcModifier;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;

import java.io.IOException;

import static pl.edu.agh.transformations.utils.New.center;
import static pl.edu.agh.transformations.utils.TransformUtils.addFutureResultsList;
import static pl.edu.agh.transformations.utils.TransformUtils.insertNewInstruciton;

public class ByteCodeModifier {

    //    org.apache.bcel.classfile.*
    private static JavaClass analyzedClass;
    private static Method[] methods;
    private static Field[] fields;

    //    org.apache.bcel.generic.*
    private static ClassGen modifiedClass;
    private static MethodGen mg;
    private static ConstantPoolGen cp;
    private static InstructionList il;
    private static InstructionFactory factory;
    private static LocalVariableGen lg;

    public ByteCodeModifier() {
    }

    public void setTarget(boolean displayMenu) throws IOException {
        Menu.menu(displayMenu);
    }

    public void setTarget(int choice, int option) throws IOException {
        Menu.menu(choice, option);
    }

    public void initialize() throws IOException {
        analyzedClass = new ClassParser(LaunchProperties.CLASS_DIR + LaunchProperties.CLASS_FILE).parse();
        modifiedClass = new ClassGen(analyzedClass);
        cp = modifiedClass.getConstantPool();
        Method transformedMethod = getMethodWithSelectedName();
        mg = new MethodGen(transformedMethod, modifiedClass.getClassName(), modifiedClass.getConstantPool());
    }

    private Method getMethodWithSelectedName() {
        methods = modifiedClass.getMethods();
        int methodPositionId = MyBcModifier.GetMethodIndex(methods, LaunchProperties.CLASS_METHOD);
        return methods[methodPositionId];
    }

    public void saveNewClassFile() throws IOException {
        String CLASS_NAME = modifiedClass.getClassName();
        CLASS_NAME = CLASS_NAME.substring(CLASS_NAME.lastIndexOf('.') + 1);

        String PATH_TO_OUTPUT_FILE = LaunchProperties.CLASS_DIR + CLASS_NAME +
                LaunchProperties.MODIFICATION_SUFFIX + LaunchProperties.CLASS_SUFFIX;

        modifiedClass.getJavaClass().dump(PATH_TO_OUTPUT_FILE);
        System.out.println(center(" DONE ", 80, '*'));
        System.out.println("Go to file:" + center(PATH_TO_OUTPUT_FILE, 69, ' '));
    }

    public void transformation() {
        try {
            //transformation for jcuda
            if (LaunchProperties.CHOICE != 1) {
                insertNewInstruciton(modifiedClass, mg, LaunchProperties.OPTION);
            }
            //tansformation for parallel
            else {
          /*  addThreadPool(modifiedClass);
            addExecutorServiceInit(modifiedClass, mg);
            addTaskPool(modifiedClass, mg);*/
                addFutureResultsList(modifiedClass, mg);
            /*copyLoopToMethod(modifiedClass, mg);
            changeLoopLimitToNumberOfThreads(modifiedClass, mg);
            emptyMethodLoop(modifiedClass, mg);
            short dataSize = 1000;
            setNewLoopBody(modifiedClass, mg, dataSize);*/

//            AnonymousClassUtils.addCallableCall(modifiedClass, classPath);
            }
        } catch (Exception e) {
            System.err.println(LaunchProperties.ERR_MESSAGE + e.getClass() + "\n\t" + e.getMessage());
        }
    }

}