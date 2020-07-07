package pl.edu.agh.transformations;

import mbuilder.TempExperiments.MyBcModifier;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;
import pl.edu.agh.transformations.util.New;
import pl.edu.agh.transformations.util.TransformUtils;

import java.io.IOException;

public class ByteCodeModifier {

    protected JavaClass analyzedClass;
    protected Method[] methods;
    protected Field[] fields;

    protected static ClassGen modifiedClass;
    protected MethodGen mg;
    protected ConstantPoolGen cp;
    protected InstructionList il;
    protected InstructionFactory factory;
    protected LocalVariableGen lg;


    public void initialize() throws IOException {
        analyzedClass = new ClassParser(LaunchProperties.CLASS_DIR + LaunchProperties.CLASS_FILE).parse();
        modifiedClass = new ClassGen(analyzedClass);

        methods = modifiedClass.getMethods();
        int methodPositionId = (int) MyBcModifier.GetMethodIndex(methods, LaunchProperties.CLASS_METHOD);
        Method transformedMethod = methods[methodPositionId];//        Method transformedMethod = modifiedClass.getMethodAt(2);

        mg = new MethodGen(transformedMethod, modifiedClass.getClassName(), modifiedClass.getConstantPool());
        cp = modifiedClass.getConstantPool();
    }


    public void transformation() {

        try {
            //transformation for jcuda
            if (LaunchProperties.CHOICE != 1) {
                TransformUtils.insertNewInstruciton(modifiedClass, mg, LaunchProperties.OPTION);
            }
            //tansformation for parallel
            else {
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

            New.saveNewClassFile(modifiedClass);
        } catch (Exception e) {
            System.err.println(LaunchProperties.ERR_MESSAGE + e.getClass() + "\n\t" + e.getMessage());
        }
    }
}
