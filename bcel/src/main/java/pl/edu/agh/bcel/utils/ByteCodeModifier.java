package pl.edu.agh.bcel.utils;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;
import pl.edu.agh.bcel.transformations.MethodUtils;
import pl.edu.agh.bcel.transformations.Print;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

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


    public ByteCodeModifier() {
    }

    public static Method getSelectedMethod0(ClassGen cg, String selectedMethod) {
        int methodPositionId = MethodUtils.GetMethodIndex(cg.getMethods(), selectedMethod);
        return cg.getMethods()[methodPositionId];
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

    public void saveNewClassFile() throws IOException {
        String CLASS_NAME = _modifiedClass.getClassName();
        CLASS_NAME = CLASS_NAME.substring(CLASS_NAME.lastIndexOf('.') + 1);

        String PATH_TO_OUTPUT_FILE = LaunchProperties.CLASS_DIR + CLASS_NAME +
                LaunchProperties.MODIFICATION_SUFFIX +
                LaunchProperties.CLASS_SUFFIX;

        _modifiedClass.getJavaClass().dump(PATH_TO_OUTPUT_FILE);
        System.out.println(Print.printCenter(" DONE ", 80, '*'));
        System.out.println("Go to file:" + Print.printCenter(PATH_TO_OUTPUT_FILE, 69, ' '));
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