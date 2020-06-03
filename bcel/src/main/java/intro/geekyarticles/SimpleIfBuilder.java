package intro.geekyarticles;/*
modified example from: http://www.geekyarticles.com/2011/08/manipulating-java-class-files-with-bcel_25.html
*/

import java.io.IOException;

import org.apache.bcel.Const;
import org.apache.bcel.generic.*;


public class SimpleIfBuilder {

    public static void main(String[] args) {
        ClassGen cg = new ClassGen(
                "SimpleIf","java.lang.Object",
                "<MatrixBuilder.MatrixBuilder.generated>",Const.ACC_PUBLIC,null
        );

        final ConstantPoolGen cp = cg.getConstantPool();
        final InstructionList il = new InstructionList();
        final InstructionFactory factory = new InstructionFactory(cg);
        final ObjectType p_stream = new ObjectType("java.io.PrintStream");
        final GOTO g = new GOTO(null);

        final InstructionHandle ih = il.append(
                factory.createFieldAccess( "java.lang.System","out", p_stream, Const.GETSTATIC)
        );
        g.setTarget(ih);

        il.append(new ALOAD(0));//the argument of the main function
        il.append(new BIPUSH((byte)0));//Push 0.
        il.append(new AALOAD());//Got value of the variable as a String;
        il.append(factory.createInvoke(
                "java.lang.Integer","parseInt",
                Type.INT,new Type[]{Type.STRING},Const.INVOKESTATIC));
        il.append(new BIPUSH((byte)5));
        // We do not yet know the position of the target. Will set it later.
        BranchHandle ifh=il.append(new IF_ICMPGE(null));
        il.append(new LDC(cp.addString("Less than five")));        //Push the String to print
        //Invoke println. we already have the object ref in the stack
        il.append(factory.createInvoke(
                "java.io.PrintStream","println",
                Type.VOID,new Type[]{Type.STRING},Const.INVOKEVIRTUAL));
        //Not to fall through the else part also. jump to the end
        BranchHandle gotoHandle=il.append(new GOTO(null));// We do not yet know the position of the target.
        //Push the String to print. This would be the target of the if_icmpge
        InstructionHandle ih2=il.append(new LDC(cp.addString("Greater than or equal to five")));
        //Invoke println. we already have the object ref in the stack
        il.append(factory.createInvoke(
                "java.io.PrintStream","println",
                Type.VOID,new Type[]{Type.STRING},Const.INVOKEVIRTUAL));

        InstructionHandle returnHandle = il.append(new RETURN());//Return would be the target of the goto.
        ifh.setTarget(ih2);
        gotoHandle.setTarget(returnHandle);

        MethodGen mg=new MethodGen(
                Const.ACC_PUBLIC|Const.ACC_STATIC,
                Type.VOID,new Type[]{new ArrayType(Type.STRING, 1)},
                new String[]{"args"},"main","SimpleIf",
                il,cp);

        mg.setMaxLocals();//Calculate the maximum number of local variables.
        mg.setMaxStack();//Very important: must calculate the maximum size of the stack.

        cg.addMethod(mg.getMethod()); //Add the method to the class

        try { cg.getJavaClass().dump("src/main/java/intro.geekyarticles/MatrixBuilder.MatrixBuilder.generated/SimpleIf.class");}
        catch (IOException e) {System.err.println(e);}

        System.out.println("********Constant Pool**********");System.out.println(cp.getFinalConstantPool());
        System.out.println("********Method**********");System.out.println(mg);
        System.out.println("********Instruction List**********");System.out.println(il);
    }
}