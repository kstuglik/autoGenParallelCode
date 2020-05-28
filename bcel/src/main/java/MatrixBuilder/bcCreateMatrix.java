
package MatrixBuilder;

import org.apache.bcel.Const;
import org.apache.bcel.generic.*;

import java.io.IOException;
import java.lang.Math;

public class bcCreateMatrix {

    public static final String MODIFICATION_SUFFIX = "_mod";
    private static final String CLASS_SUFFIX = ".class";
    private static final String JAVA_SUFFIX = ".java";
    private static final String packageName = "zmodyfikowana";

    private static ClassGen cg;
    private static ConstantPoolGen cp;
    private static InstructionList il;
    private static InstructionFactory factory;
    private static MethodGen mg;
    private static LocalVariableGen lg;
    private static ObjectType p_stream;


    public void modifyBytecode(String classPath, String className, String methodName) {
        String fullClassName = packageName + "." + className;
        cg = new ClassGen(fullClassName, "java.lang.Object", "<generated>",
                Const.ACC_PUBLIC | Const.ACC_SUPER, null);
        cp = cg.getConstantPool(); // cg creates constant pool
        il = new InstructionList();
        factory = new InstructionFactory(cg);
        mg = new MethodGen(Const.ACC_PUBLIC | Const.ACC_STATIC,
                Type.VOID, new Type[]{new ArrayType(Type.STRING, 1)},
                new String[]{"argv"}, "main", className, il, cp);
        p_stream = new ObjectType("java.io.PrintStream");

        create_array_2D(Type.INT,3, new int[]{1, 3, 9});

        saveModifiedClass(classPath,className,cg);
    }

    void create_array_2D(BasicType t, int N, int[] nn){

        int index = cp.addArrayClass(new ArrayType(t, 1));

        il.append(new LDC(cp.addInteger(N)));
        il.append(new ANEWARRAY(index));
        il.append(new DUP());

        for (int item = 0; item < N; item++) {

            il.append(new LDC(cp.addInteger(item)));
            il.append(new LDC(cp.addInteger(nn[item])));
            il.append(new NEWARRAY(t));
            il.append(new DUP());

            for (int i = 1; i <= nn[item]; i++) {
                il.append(new LDC(cp.addInteger(i-1)));
                il.append(new LDC(cp.addInteger((int) Math.pow(2, i))));
                il.append(new IASTORE());
                if(i==nn[item]){
                   il.append(new AASTORE());
                }
                il.append(new DUP());
            }
        }

        il.append(new ASTORE(index));
        il.append(new RETURN());
    }

    private void saveModifiedClass(String classPath, String className, ClassGen cg) {

       try{
           mg.setMaxStack();//mg - MethodGen object
           mg.setMaxLocals();
//           mg.stripAttributes(true);

           cg.addMethod(mg.getMethod());//cg - ClassGen object
           cg.addEmptyConstructor(Const.ACC_PUBLIC);

           il.dispose();//il - InstructionList

           String pathOutput = classPath + "/" + className + MODIFICATION_SUFFIX + CLASS_SUFFIX;

           cg.getJavaClass().dump(pathOutput);
        } catch (IOException e) {
            throw new RuntimeException("Error during modified class save.", e);
        }
        System.out.println("DONE!");
    }
}
