package pl.edu.agh.transformations;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;
import pl.edu.agh.utils.AnonymousClassUtils;
import pl.edu.agh.utils.MethodUtils;
import pl.edu.agh.utils.TransformUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static pl.edu.agh.utils.New.center;

public class ByteCodeModifier {

    //    org.apache.bcel.classfile.*
    public JavaClass _analyzedClass;
    public Method[] _methods;
    public Method _transformedMethod;
    public Field[] _fields;

    //    org.apache.bcel.generic.*
    public ClassGen _modifiedClass;
    public MethodGen _mg;
    public ConstantPoolGen _cp;
    public LocalVariableGen _lvg;
    public LocalVariableGen[] _lvgs;

    public InstructionFactory _factory;
    public InstructionList _il_new;
    public InstructionList _il_old;

//    public boolean is_create_process;


    public ByteCodeModifier() {
    }

    public void prepareToModify() throws IOException {
        _analyzedClass = new ClassParser(LaunchProperties.CLASS_DIR + LaunchProperties.CLASS_NAME + LaunchProperties.CLASS_SUFFIX).parse();
        _modifiedClass = new ClassGen(_analyzedClass);

        _cp = _modifiedClass.getConstantPool();
        _il_new = new InstructionList();
        _factory = new InstructionFactory(_modifiedClass, _cp);

        prepareSelectedMethod();
    }

    public void prepareToCreate() {
        _modifiedClass = new ClassGen(LaunchProperties.CLASS_NAME, "java.lang.Object", "<generate>",
                Const.ACC_PUBLIC | Const.ACC_SUPER, null);

        _cp = _modifiedClass.getConstantPool();
        _il_new = new InstructionList();
        _factory = new InstructionFactory(_modifiedClass, _cp);

        _mg = new MethodGen(Const.ACC_STATIC | Const.ACC_PUBLIC,
                Type.VOID, new Type[]{new ArrayType(Type.STRING, 1)},
                new String[]{"argv"}, LaunchProperties.CLASS_METHOD, "HelloWorld", _il_new, _cp);
    }

    private void prepareSelectedMethod() {
        _transformedMethod = getSelectedMethod();
        _mg = new MethodGen(_transformedMethod, _modifiedClass.getClassName(), _modifiedClass.getConstantPool());
        _il_old = _mg.getInstructionList();
    }

    public Method getSelectedMethod0(ClassGen cg) {
        int methodPositionId = MethodUtils.GetMethodIndex(cg.getMethods(), LaunchProperties.CLASS_METHOD);
        return cg.getMethods()[methodPositionId];
    }

    public Method getSelectedMethod() {
        setMethodsArray();
        int methodPositionId = MethodUtils.GetMethodIndex(_methods, LaunchProperties.CLASS_METHOD);
        return _methods[methodPositionId];
    }

    public void setMethodsArray() {
        _methods = _modifiedClass.getMethods();
    }

    public Method[] getMethodsArray() {
        return _methods;
    }

    public void saveNewClassFile() throws IOException {
        String CLASS_NAME = _modifiedClass.getClassName();
        CLASS_NAME = CLASS_NAME.substring(CLASS_NAME.lastIndexOf('.') + 1);

        String PATH_TO_OUTPUT_FILE =
                LaunchProperties.CLASS_DIR + CLASS_NAME +
                        LaunchProperties.MODIFICATION_SUFFIX +
                        LaunchProperties.CLASS_SUFFIX;

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
                    TransformUtils.injectJcmMultiplyAB(this);
                    break;
                case 2: //CODE INJECTION - printArray
                    TransformUtils.injectPrintArrName(this, true);
                    break;
                case 3: //CODE INJECTION - get id luckyNumber
                    TransformUtils.injectPrintVarName(this);
                    break;
                case 4: //CODE INJECTION - instruction jcm.multiply(A,B)
                    TransformUtils.injectJcmMultiply(this);
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

    public void updateMethodBeforeSave() {
        _il_new.append(new RETURN());
        _mg.setInstructionList(_il_new);
//        _mg.stripAttributes(true);
        _mg.setMaxStack();
        _mg.setMaxLocals();
        _modifiedClass.replaceMethod(_mg.getMethod(), _mg.getMethod());
        _il_new.dispose();
    }

    private void addBlockTry(InstructionHandle[] handles) {
        InstructionHandle _handle;
        Instruction _i_current;

        for (int i = 1, handlesLength = handles.length; i < handlesLength; i++) {
            _handle = handles[i];
            _i_current = _handle.getInstruction();

//            System.out.println("opcode: " + _i_current.getOpcode() + "\tname: " + _i_current.getName());
//            if (_i_current.getOpcode() == Const.RETURN)
//                System.out.println(_i_current.getOpcode() + " appears in " + _mg.getName());
//            else
            _il_new.append(_i_current);
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

    public void setFieldsArray() {
        _fields = _modifiedClass.getFields();
    }

    public ArrayList<String> getNameFields() {
        setFieldsArray();
        ArrayList<String> result = new ArrayList<>();
        for (Field field : _fields) result.add(field.getName());
        return result;
    }

    public void setLocalVariables() {
        _lvgs = _mg.getLocalVariables();
    }

    public ArrayList<String> getLocalVariables() {
        setLocalVariables();
        ArrayList<String> result = new ArrayList<>();
        for (LocalVariableGen var : _lvgs) result.add(var.getName());
        return result;
    }

    public void extraMe() {
        _transformedMethod = getSelectedMethod();
        _mg = new MethodGen(_transformedMethod, _modifiedClass.getClassName(), _modifiedClass.getConstantPool());
    }

    public void modifyBytecode(String classPath, String className, short dataSize) throws IOException, TargetLostException {
        JavaClass analyzedClass = new ClassParser(classPath + className + LaunchProperties.CLASS_SUFFIX).parse();
        ClassGen modifiedClass = getModifiedClass(className, analyzedClass);
        copyFields(analyzedClass, modifiedClass);
        copyMethods(analyzedClass, modifiedClass);

        Method transformedMethod = getSelectedMethod0(modifiedClass);
        System.out.println(transformedMethod.getName());
        MethodGen methodGen = new MethodGen(transformedMethod, modifiedClass.getClassName(), modifiedClass.getConstantPool());

        TransformUtils.addThreadPool(modifiedClass);
        TransformUtils.addExecutorServiceInit(modifiedClass, methodGen);
        TransformUtils.addTaskPool(modifiedClass, methodGen);
        TransformUtils.addFutureResultsList(modifiedClass, methodGen);
        TransformUtils.copyLoopToMethod(modifiedClass, methodGen);
        TransformUtils.changeLoopLimitToNumberOfThreads(modifiedClass, methodGen);
        TransformUtils.emptyMethodLoop(modifiedClass, methodGen);
        TransformUtils.setNewLoopBody(modifiedClass, methodGen, dataSize);
        AnonymousClassUtils.addCallableCall(modifiedClass, classPath);

        analyzedClass = new ClassParser(classPath + "/" + className + LaunchProperties.CLASS_SUFFIX).parse();
        modifiedClass = new ClassGen(analyzedClass);

        System.out.println("GOTO: " + classPath + className + LaunchProperties.MODIFICATION_SUFFIX + LaunchProperties.CLASS_SUFFIX);
        modifiedClass.getJavaClass().dump(classPath + className + LaunchProperties.MODIFICATION_SUFFIX + LaunchProperties.CLASS_SUFFIX);
    }

    private ClassGen getModifiedClass(String className, JavaClass analyzedClass) {
        ClassGen oldClass = new ClassGen(analyzedClass);
        return new ClassGen(analyzedClass.getPackageName() + className + LaunchProperties.MODIFICATION_SUFFIX,
                Object.class.getName(),
                className + LaunchProperties.MODIFICATION_SUFFIX + LaunchProperties.JAVA_SUFFIX,
                Const.ACC_PUBLIC,
                null,
                oldClass.getConstantPool());
    }

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

    private void saveModifiedClass(String classPath, String className, ClassGen classGen) {
        try (FileOutputStream outputStream = new FileOutputStream(classPath + className + LaunchProperties.MODIFICATION_SUFFIX + LaunchProperties.CLASS_SUFFIX)) {
            System.out.println("GOTO: " + classPath + className + LaunchProperties.MODIFICATION_SUFFIX + LaunchProperties.CLASS_SUFFIX);
            classGen.getJavaClass().dump(outputStream);
        } catch (IOException exception) {
            throw new RuntimeException("Error during modified class save.", exception);
        }
    }

}