package pl.edu.agh.bcel;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;
import pl.edu.agh.bcel.utils.MethodUtils;
import pl.edu.agh.bcel.utils.Print;

import java.io.IOException;
import java.util.Arrays;

public class ByteCodeModifier {

    public JavaClass _analyzedClass;
    public ClassGen cg;
    public MethodGen _mg;
    public ConstantPoolGen _cp;
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

    public void prepareToCreate() {
        cg = new ClassGen(LaunchProperties.CLASS_NAME, "java.lang.Object", "<generate>",
                Const.ACC_PUBLIC | Const.ACC_SUPER, null);

        _cp = cg.getConstantPool();
        _il_new = new InstructionList();
        _factory = new InstructionFactory(cg, _cp);

        _mg = new MethodGen(Const.ACC_STATIC | Const.ACC_PUBLIC,
                Type.VOID, new Type[]{new ArrayType(Type.STRING, 1)},
                new String[]{"argv"}, LaunchProperties.CLASS_METHOD, LaunchProperties.CLASS_NAME, _il_new, _cp);
    }

    public void prepareToModify() throws IOException {
        _analyzedClass = new ClassParser(LaunchProperties.CLASS_DIR + LaunchProperties.CLASS_NAME + LaunchProperties.CLASS_SUFFIX).parse();
        cg = new ClassGen(_analyzedClass);

        _cp = cg.getConstantPool();
        _il_new = new InstructionList();
        _factory = new InstructionFactory(cg, _cp);

        getSelectedMethod0(cg, LaunchProperties.CLASS_METHOD);
    }

    public void saveNewClassFile() throws IOException {
        String CLASS_NAME = cg.getClassName();
        CLASS_NAME = CLASS_NAME.substring(CLASS_NAME.lastIndexOf('.') + 1);

        String PATH_TO_OUTPUT_FILE = LaunchProperties.CLASS_DIR + CLASS_NAME +
                LaunchProperties.MODIFICATION_SUFFIX +
                LaunchProperties.CLASS_SUFFIX;

        cg.getJavaClass().dump(PATH_TO_OUTPUT_FILE);
        System.out.println(Print.printCenter(" DONE ", 80, '*'));
        System.out.println("Go to file:" + Print.printCenter(PATH_TO_OUTPUT_FILE, 69, ' '));
    }

    public void updateMethodBeforeSave() {
        _il_new.append(new RETURN());
        _mg.setInstructionList(_il_new);
        _mg.setMaxStack();
        _mg.setMaxLocals();
        cg.replaceMethod(_mg.getMethod(), _mg.getMethod());
        _il_new.dispose();
    }

}