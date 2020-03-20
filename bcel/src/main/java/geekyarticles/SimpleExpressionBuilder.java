/*
modified example from: http://www.geekyarticles.com/2011/08/manipulating-java-class-files-with-bcel_18.html
*/
package geekyarticles;

import java.io.IOException;

import org.apache.bcel.Const;
import org.apache.bcel.generic.*;


public class SimpleExpressionBuilder {
    public static void main(String[] args) {
        System.out.println("Generating Class");

        ClassGen cg = new ClassGen(
                "SimpleExpression",
                "java.lang.Object",
                "<generated>",
                Const.ACC_PUBLIC,
                null
        );

        //Get the reference to constant pool of the class will be modified as we add methods, fields etc
        final ConstantPoolGen cp = cg.getConstantPool();
        final InstructionList il = new InstructionList();//The list of instructions for a method.

        final MethodGen mg = new MethodGen(
                Const.ACC_PUBLIC|Const.ACC_STATIC,
                Type.VOID,
                new Type[]{new ArrayType(Type.STRING, 1)},
                new String[]{"argv"},
                "main",
                "SampleExpresion",
                il,
                cp
        );

        final InstructionFactory factory = new InstructionFactory(cg);
        final ObjectType p_stream = new ObjectType("java.io.PrintStream");

        //Get the reference to static field out in class java.lang.System.
        final GOTO g = new GOTO(null);
        final InstructionHandle ih = il.append(
                factory.createFieldAccess(
                        "java.lang.System",
                        "out",
                        p_stream,
                        Const.GETSTATIC
                )
        );

        g.setTarget(ih);

        il.append(new LDC(cp.addInteger(2)));//Load the constant
        il.append(new LDC(cp.addInteger(3)));//Load the constant
        il.append(new IADD());//Add

        //Invoke the method.
        il.append(factory.createInvoke(
                "java.io.PrintStream","println",
                Type.VOID,new Type[]{Type.STRING},Const.INVOKEVIRTUAL));

        //Return from the method
        il.append(InstructionConst.RETURN);

        mg.setMaxLocals();//Calculate the maximum number of local variables.
        mg.setMaxStack();//Very important: must calculate the maximum size of the stack.

        cg.addMethod(mg.getMethod()); //Add the method to the class

        try {
            cg.getJavaClass().dump("src/main/java/geekyarticles/generated/SimpleExpression.class");
        } catch (IOException e) {
            System.err.println(e);
        }

        System.out.println("********Constant Pool**********");System.out.println(cp.getFinalConstantPool());
        System.out.println("********Method**********");System.out.println(mg);
        System.out.println("********Instruction List**********");System.out.println(il);
    }
}