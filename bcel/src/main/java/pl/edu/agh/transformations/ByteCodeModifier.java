package pl.edu.agh.transformations;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;
import pl.edu.agh.transformations.utils.MethodUtils;

import java.io.IOException;

import static pl.edu.agh.transformations.utils.New.center;
import static pl.edu.agh.transformations.utils.TransformUtils.*;

public class ByteCodeModifier {

    //    org.apache.bcel.classfile.*
    public static JavaClass _analyzedClass;
    public static Method[] _methods;
    public Method _transformedMethod;
    public static Field[] _fields;

    //    org.apache.bcel.generic.*
    public static ClassGen _modifiedClass;
    public static MethodGen _mg;
    public static ConstantPoolGen _cp;
    public static LocalVariableGen _lg;

    public static InstructionFactory _factory;
    public static InstructionList _il_new;
    public static InstructionList _il_old;

    public static boolean is_create_process;


    public ByteCodeModifier() {
    }


    /*  setTarget(false) - default class file, default transformation option
     *   setTarget(true) - user class file, user transformation option, selected from menu
     *   setTarget(optionNo, choiceNo) - fast selected one of available configuration
     *
     *   you can get the same result with use LaunchProperties.* and set this values:
     *       CLASS_FILE
     *       CLASS_METHOD
     *       CLASS_DIR - if another then set
     *       OPTION
     *       CHOICE
     * */
    public void setTarget(boolean displayMenu) {
        Menu.menu(displayMenu);
    }

    public void setTarget(int choice, int option) {
        Menu.menu(choice, option);
    }

    public void prepareToModifing() throws IOException {
        is_create_process = false;

        _analyzedClass = new ClassParser(LaunchProperties.CLASS_DIR + LaunchProperties.CLASS_FILE).parse();
        _modifiedClass = new ClassGen(_analyzedClass);

        _cp = _modifiedClass.getConstantPool();
        _il_new = new InstructionList();
        _factory = new InstructionFactory(_modifiedClass, _cp);


        _transformedMethod = getMethodWithSelectedName();
        _mg = new MethodGen(_transformedMethod, _modifiedClass.getClassName(), _modifiedClass.getConstantPool());
        _il_old = _mg.getInstructionList();
    }

    public static void prepareToCreating() {
        is_create_process = true;
        _modifiedClass = new ClassGen(LaunchProperties.CLASS_NAME, "java.lang.Object", "<generate>",
                Const.ACC_PUBLIC | Const.ACC_SUPER, null);

        _cp = _modifiedClass.getConstantPool();
        _il_new = new InstructionList();
        _factory = new InstructionFactory(_modifiedClass, _cp);

        _mg = new MethodGen(Const.ACC_STATIC | Const.ACC_PUBLIC,
                Type.VOID, new Type[]{new ArrayType(Type.STRING, 1)},
                new String[]{"argv"}, LaunchProperties.CLASS_METHOD, "HelloWorld", _il_new, _cp);
    }

    public Method getMethodWithSelectedName() {
        _methods = _modifiedClass.getMethods();
        int methodPositionId = MethodUtils.GetMethodIndex(_methods, LaunchProperties.CLASS_METHOD);
        return _methods[methodPositionId];
    }

    public static void saveNewClassFile() throws IOException {
        String CLASS_NAME = _modifiedClass.getClassName();
        CLASS_NAME = CLASS_NAME.substring(CLASS_NAME.lastIndexOf('.') + 1);

        String PATH_TO_OUTPUT_FILE = LaunchProperties.CLASS_DIR + CLASS_NAME +
                LaunchProperties.MODIFICATION_SUFFIX + LaunchProperties.CLASS_SUFFIX;

        _modifiedClass.getJavaClass().dump(PATH_TO_OUTPUT_FILE);
        System.out.println(center(" DONE ", 80, '*'));
        System.out.println("Go to file:" + center(PATH_TO_OUTPUT_FILE, 69, ' '));
    }

    public void transformations(int OPTION) {
        try {

            InstructionHandle[] handles = _il_old.getInstructionHandles();
            InstructionHandle TRY_START = _il_new.append(handles[0].getInstruction());

            addBlockTry(handles);

            switch (OPTION) {

                case 1:
                    injectJcmMultiplyAB(this);
                    break;
                case 2: //CODE INJECTION - printArray
                    injectPrintArrName(this, true);
                    break;
                case 3: //CODE INJECTION - get id luckyNumber
                    injectPrintVarName(this);
                    break;
                case 4: //CODE INJECTION - instruction jcm.multiply(A,B)
                    injectJcmMultiply(this);
                    break;
                default:
                    System.out.println("wrong option!");
                    break;
            }

            addBlockCatch(TRY_START);

        } catch (Exception e) {
            System.err.println(LaunchProperties.ERR_MESSAGE + e.getClass() + "\n\t" + e.getMessage());
        }

    }

    public void old() {
        try {
//tansformation for parallel
//                    addThreadPool(modifiedClass);
//                    addExecutorServiceInit(modifiedClass, mg);
//                    addTaskPool(modifiedClass, mg);
            addFutureResultsList(_modifiedClass, _mg);
//                    copyLoopToMethod(modifiedClass, mg);
//                    changeLoopLimitToNumberOfThreads(modifiedClass, mg);
//                    emptyMethodLoop(modifiedClass, mg);
//                    short dataSize = 1000;
//                    setNewLoopBody(modifiedClass, mg, dataSize);
//                    AnonymousClassUtils.addCallableCall(modifiedClass, classPath);

            updateMethodBeforeSave();
            saveNewClassFile();

        } catch (Exception e) {
            System.err.println(LaunchProperties.ERR_MESSAGE + e.getClass() + "\n\t" + e.getMessage());
        }
    }

    public static void updateMethodBeforeSave() {
        _il_new.append(new RETURN());
        _mg.setInstructionList(_il_new);
//        _mg.stripAttributes(true);
        _mg.setMaxStack();
        _mg.setMaxLocals();
        if (!is_create_process) _modifiedClass.removeMethod(_mg.getMethod());
        _modifiedClass.addMethod(_mg.getMethod());
        _il_new.dispose();
    }

    private void addBlockTry(InstructionHandle[] handles) {
        InstructionHandle _handle;
        Instruction _i_current;

        for (int i = 1, handlesLength = handles.length; i < handlesLength; i++) {
            _handle = handles[i];
            _i_current = _handle.getInstruction();

            System.out.println("opcode: " + _i_current.getOpcode() + "\tname: " + _i_current.getName());

            if (_i_current.getOpcode() == Const.RETURN)
                System.out.println(_i_current.getOpcode() + " appears in " + _mg.getName());
            else _il_new.append(_i_current);
        }
    }

    private void addBlockCatch(InstructionHandle TRY_START) {
        BranchInstruction GOTO = InstructionFactory.createBranchInstruction(Const.GOTO, null);
        InstructionHandle TRY_END = _il_new.append(GOTO);

        InstructionHandle CATCH_START = _il_new.append(InstructionFactory.createStore(Type.OBJECT, 0));
        _il_new.append(_factory.createFieldAccess("java.lang.System", "err",
                new ObjectType("java.io.PrintStream"), Const.GETSTATIC));
        _il_new.append(InstructionFactory.createLoad(Type.OBJECT, 0));
        _il_new.append(_factory.createInvoke("java.lang.Exception", "getMessage",
                Type.STRING, Type.NO_ARGS, Const.INVOKEVIRTUAL));
        _il_new.append(_factory.createInvoke("java.io.Printstream", "println",
                Type.VOID, new Type[]{Type.STRING}, Const.INVOKEVIRTUAL));

        InstructionHandle CATCH_END = _il_new.append(InstructionFactory.createReturn(Type.VOID));
        GOTO.setTarget(CATCH_END);
        _mg.addExceptionHandler(TRY_START, TRY_END, CATCH_START, new ObjectType("java.lang.Exception"));
    }


}