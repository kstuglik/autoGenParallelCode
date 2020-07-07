package intro2useBCEL.geekyarticles;/*
modified example from: http://www.geekyarticles.com/2011/08/manipulating-java-class-files-with-bcel_25.html
*/

import java.io.IOException;

import org.apache.bcel.Const;
import org.apache.bcel.generic.*;


public class SimpleForBuilder {
    public static void main(String[] args) {
        final ClassGen cg=new ClassGen(
                "package_name.SimpleFor", "java.lang.Object","<MatrixBuilder.MatrixBuilder.generated>",
                Const.ACC_PUBLIC, null);
        final ConstantPoolGen cp = cg.getConstantPool();
        final InstructionList il = new InstructionList();
        final InstructionFactory factory = new InstructionFactory(cg);
        final ObjectType p_stream = new ObjectType("java.io.PrintStream");

        il.append(new ALOAD(0));//the argument of the main function
        il.append(new BIPUSH((byte)0));//Push 0.
        il.append(new DUP());

        //local variable 0 is the argument of the main method.
        //So we create a new local variable 1. It will work as loop counter
        il.append(new ISTORE(1));
        il.append(new AALOAD());//Got value of the variable as a String;

        il.append(factory.createInvoke(
                "java.lang.Integer","parseInt",
                Type.INT, new Type[]{Type.STRING}, Const.INVOKESTATIC)
        );//Now we got the value as int

        il.append(new ISTORE(2));// The limit to the loop counter.

        InstructionHandle loopStart = il.append(new ILOAD(1));
        il.append(new ILOAD(2));
        BranchHandle ifCheck = il.append(new IF_ICMPLT(null));//If less go inside the loop;
        //This is when the loop ends
        BranchHandle gotoJump = il.append(new GOTO(null));//Goto the return

        //Print the loop counter
        InstructionHandle insideLoop = il.append(
                factory.createFieldAccess( "java.lang.System","out", p_stream, Const.GETSTATIC)
        );//Get the reference to static field out in class java.lang.System.

        il.append(new ILOAD(1));
        il.append(factory.createInvoke("java.io.PrintStream", "println",
                Type.VOID, new Type[]{Type.INT}, Const.INVOKEVIRTUAL));

        il.append(new IINC(1, 1));// Increment the counter, the variable number is 1.
        il.append(new GOTO(loopStart));//In this case, we already know where to go.

        InstructionHandle returnHandle=il.append(new RETURN());

        //Set the handles
        ifCheck.setTarget(insideLoop);
        gotoJump.setTarget(returnHandle);

        //The usual method generation
        MethodGen mg = new MethodGen(
                Const.ACC_PUBLIC|Const.ACC_STATIC,
                Type.VOID,new Type[]{new ArrayType(Type.STRING, 1)},
                new String[]{"args"},"main","<MatrixBuilder.MatrixBuilder.generated>",
                il,cp);

        mg.setMaxLocals();//Calculate the maximum number of local variables.
        mg.setMaxStack();//Very important: must calculate the maximum size of the stack.

        cg.addMethod(mg.getMethod()); //Add the method to the class

        try { cg.getJavaClass().dump("src/main/java/intro.geekyarticles/generated/SimpleFor.class");}
        catch (IOException e) {System.err.println(e);}

        System.out.println("********Constant Pool**********");System.out.println(cp.getFinalConstantPool());
        System.out.println("********Method**********");System.out.println(mg);
        System.out.println("********Instruction List**********");System.out.println(il);
    }
}