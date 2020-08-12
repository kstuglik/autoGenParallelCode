package pl.edu.agh.transformations;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import pl.edu.agh.utils.MethodUtils;
import pl.edu.agh.utils.PreparedFieldsAndMethods;
import pl.edu.agh.utils.TransformUtils;
import pl.edu.agh.utils.LoopForTransform;

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


    public ByteCodeModifier() {
    }

    private void copyFields(JavaClass oldClass, ClassGen newClass) {
        Arrays.stream(oldClass.getFields())
                .forEach(newClass::addField);
    }

    private void copyMethods(JavaClass oldClass, ClassGen newClass) {
        Arrays.stream(oldClass.getMethods())
                .forEach(newClass::addMethod);
        Arrays.stream(newClass.getMethods())
                .forEach(method -> MethodUtils.switchConstantRefsToNewClass(newClass, method));
    }

//    public void prepareSelectedMethod() {
//        _transformedMethod = getSelectedMethod();
//        _mg = new MethodGen(_transformedMethod, _modifiedClass.getClassName(), _modifiedClass.getConstantPool());
//        _il_old = _mg.getInstructionList();
//    }

    public void extraMe() {
        _transformedMethod = getSelectedMethod();
        _mg = new MethodGen(_transformedMethod, _modifiedClass.getClassName(), _modifiedClass.getConstantPool());
    }

    public ArrayList<String> getLocalVariables() {
        setLocalVariables();
        ArrayList<String> result = new ArrayList<>();
        for (LocalVariableGen var : _lvgs) result.add(var.getName());
        return result;
    }

    public Method[] getMethodsArray() {
        return _methods;
    }

    public ArrayList<String> getNameFields() {
        setFieldsArray();
        ArrayList<String> result = new ArrayList<>();
        for (Field field : _fields) result.add(field.getName());
        return result;
    }

    public Method getSelectedMethod() {
        setMethodsArray();
        int methodPositionId = MethodUtils.GetMethodIndex(_methods, LaunchProperties.CLASS_METHOD);
        return _methods[methodPositionId];
    }

    public Method getSelectedMethod0(ClassGen cg, String selectedMethod) {
        int methodPositionId = MethodUtils.GetMethodIndex(cg.getMethods(), selectedMethod);
        return cg.getMethods()[methodPositionId];
    }

    public void modifySerialToJCudaMatrix(String classPath, String className, short N) throws Exception {
        JavaClass analyzedClass = new ClassParser(classPath + className + LaunchProperties.CLASS_SUFFIX).parse();
        ClassGen modifiedClass = new ClassGen(analyzedClass);

        Method transformedMethod = getSelectedMethod0(modifiedClass, LaunchProperties.CLASS_METHOD);
        MethodGen mg = new MethodGen(transformedMethod, modifiedClass.getClassName(), modifiedClass.getConstantPool());

        System.out.println("1)\til.lenght = " + mg.getInstructionList().getLength());
        TransformUtils.addFieldsForJcuda(modifiedClass);

        System.out.println("2)\til.lenght = " + mg.getInstructionList().getLength());
        TransformUtils.addMultiplyMethod(modifiedClass);

        System.out.println("3)\til.lenght = " + mg.getInstructionList().getLength());
        TransformUtils.addCallJCudaMultiply(modifiedClass, mg);

        System.out.println("GOTO: " + classPath + className + LaunchProperties.MODIFICATION_SUFFIX + LaunchProperties.CLASS_SUFFIX);
        modifiedClass.getJavaClass().dump(classPath + className + LaunchProperties.MODIFICATION_SUFFIX + LaunchProperties.CLASS_SUFFIX);
    }

    public void modifySerialToParallelMatrix(String classPath, String className) throws Exception {

        JavaClass analyzedClass = new ClassParser(classPath + className + LaunchProperties.CLASS_SUFFIX).parse();
        ClassGen cgTarget = new ClassGen(analyzedClass);

        Method transformedMethod = getSelectedMethod0(cgTarget, LaunchProperties.CLASS_METHOD);
        MethodGen mg = new MethodGen(transformedMethod, cgTarget.getClassName(), cgTarget.getConstantPool());

//        System.out.println("1)\til.lenght = " + mg.getInstructionList().getLength());
        TransformUtils.addThreadPoolExecutorService(cgTarget);

//        System.out.println("2)\til.lenght = " + mg.getInstructionList().getLength());
        PreparedFieldsAndMethods.addSetStepLVar(cgTarget);
        PreparedFieldsAndMethods.addFieldStep(cgTarget, mg);
        TransformUtils.addFieldTaskPool(cgTarget, mg);
        TransformUtils.initFieldExecutorService(cgTarget, mg);

//        System.out.println("9)\taddMethodToInitializeTaskPool");
        PreparedFieldsAndMethods.addMethodToInitTaskPool(cgTarget);


//        System.out.println("10)\taddMethodSingleCalculation");

        LoopForTransform.changeNestedLoopInMatrixMultiply(cgTarget, mg);
//        UnlimiteLevel.nestedLoopCopyToNewMethod(modifiedClass, mg);


//        System.out.println("3)\til.lenght = " + mg.getInstructionList().getLength());
//        TransformUtils.copyLoopToSubTaskMethod(modifiedClass, mg, "rowNum");
////
//        System.out.println("4)\til.lenght = " + mg.getInstructionList().getLength());
//        TransformUtils.changeLoopLimitToNumberOfThreads(modifiedClass, mg);


//        System.out.println("6)\til.lenght = " + mg.getInstructionList().getLength());
//        TransformUtils.removeBodyForLoopInSelectedMethod(modifiedClass, mg);
//
//        System.out.println("7)\til.lenght = " + mg.getInstructionList().getLength());
//        TransformUtils.setNewLoopBody(modifiedClass, mg);

//        System.out.println("8)\til.lenght = " + mg.getInstructionList().getLength());
//        TransformUtils.addTryCatchService(cgTarget, mg);


//        UnlimiteLevel.changeBody(modifiedClass,mg);
//
//        cgTarget.setClassName("SerialMultiplier_ITEST");
//
//        cgTarget.update();


        System.out.println("GOTO: " + classPath + className + LaunchProperties.MODIFICATION_SUFFIX + LaunchProperties.CLASS_SUFFIX);
        cgTarget.getJavaClass().dump(classPath + className + LaunchProperties.MODIFICATION_SUFFIX + LaunchProperties.CLASS_SUFFIX);

    }

    public void modifySerialToParallelNBody(String classPath, String className, short N) throws Exception {
        JavaClass analyzedClass = new ClassParser(classPath + className + LaunchProperties.CLASS_SUFFIX).parse();
        ClassGen modifiedClass = new ClassGen(analyzedClass);

        Method transformedMethod = getSelectedMethod0(modifiedClass, LaunchProperties.CLASS_METHOD);
        MethodGen mg = new MethodGen(transformedMethod, modifiedClass.getClassName(), modifiedClass.getConstantPool());


        System.out.println("1)\til.lenght = " + mg.getInstructionList().getLength());
        TransformUtils.addThreadPoolExecutorService(modifiedClass);

        System.out.println("2)\til.lenght = " + mg.getInstructionList().getLength());
        TransformUtils.initFieldExecutorService(modifiedClass, mg);

        System.out.println("3)\til.lenght = " + mg.getInstructionList().getLength());
        TransformUtils.copyLoopToSubTaskMethod(modifiedClass, mg, LaunchProperties.LOOP_ITERATOR_NAME);

        System.out.println("4)\til.lenght = " + mg.getInstructionList().getLength());
        TransformUtils.changeLoopLimitToNumberOfThreads(modifiedClass, mg);

        System.out.println("5)\til.lenght = " + mg.getInstructionList().getLength());
        TransformUtils.addFieldTaskPool(modifiedClass, mg);

        System.out.println("6)\til.lenght = " + mg.getInstructionList().getLength());
        TransformUtils.removeBodyForLoopInSelectedMethod(modifiedClass, mg);

        System.out.println("7)\til.lenght = " + mg.getInstructionList().getLength());
        TransformUtils.setNewLoopBody(modifiedClass, mg, N);

        System.out.println("8)\til.lenght = " + mg.getInstructionList().getLength());
        TransformUtils.addTryCatchService(modifiedClass, mg);

        System.out.println("9)\til.lenght = " + mg.getInstructionList().getLength());
        PreparedFieldsAndMethods.addMethodToInitTaskPool(modifiedClass);

        System.out.println("GOTO: " + classPath + className + LaunchProperties.MODIFICATION_SUFFIX + LaunchProperties.CLASS_SUFFIX);
        modifiedClass.getJavaClass().dump(classPath + className + LaunchProperties.MODIFICATION_SUFFIX + LaunchProperties.CLASS_SUFFIX);
    }

    public void prepareToCreate() {
        _modifiedClass = new ClassGen(LaunchProperties.CLASS_NAME, "java.lang.Object", "<generate>",
                Const.ACC_PUBLIC | Const.ACC_SUPER, null);

        _cp = _modifiedClass.getConstantPool();
        _il_new = new InstructionList();
        _factory = new InstructionFactory(_modifiedClass, _cp);

        _mg = new MethodGen(Const.ACC_STATIC | Const.ACC_PUBLIC,
                Type.VOID, new Type[]{new ArrayType(Type.STRING, 1)},
                new String[]{"argv"}, LaunchProperties.CLASS_METHOD, LaunchProperties.CLASS_NAME, _il_new, _cp);
    }

    public void prepareToModify() throws IOException {
        _analyzedClass = new ClassParser(LaunchProperties.CLASS_DIR + LaunchProperties.CLASS_NAME + LaunchProperties.CLASS_SUFFIX).parse();
        _modifiedClass = new ClassGen(_analyzedClass);

        _cp = _modifiedClass.getConstantPool();
        _il_new = new InstructionList();
        _factory = new InstructionFactory(_modifiedClass, _cp);

        getSelectedMethod0(_modifiedClass, LaunchProperties.CLASS_METHOD);
    }

    private void printLocalVariableName(MethodGen methodGen) {
        LocalVariableGen[] _ltable = methodGen.getLocalVariables();

        for (LocalVariableGen localVariableGen : _ltable) {
            System.out.println(localVariableGen.getName());
        }
    }

    public void saveNewClassFile() throws IOException {
        String CLASS_NAME = _modifiedClass.getClassName();
        CLASS_NAME = CLASS_NAME.substring(CLASS_NAME.lastIndexOf('.') + 1);

        String PATH_TO_OUTPUT_FILE = LaunchProperties.CLASS_DIR + CLASS_NAME +
                LaunchProperties.MODIFICATION_SUFFIX +
                LaunchProperties.CLASS_SUFFIX;

        _modifiedClass.getJavaClass().dump(PATH_TO_OUTPUT_FILE);
        System.out.println(center(" DONE ", 80, '*'));
        System.out.println("Go to file:" + center(PATH_TO_OUTPUT_FILE, 69, ' '));
    }

    public void setFieldsArray() {
        _fields = _modifiedClass.getFields();
    }

    public void setLocalVariables() {
        _lvgs = _mg.getLocalVariables();
    }

    public void setMethodsArray() {
        _methods = _modifiedClass.getMethods();
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

}