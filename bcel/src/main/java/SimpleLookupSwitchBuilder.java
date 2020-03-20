/*
modified example from: http://www.geekyarticles.com/2011/08/manipulating-java-class-files-with-bcel_25.html
*/

import java.io.IOException;

import org.apache.bcel.Const;
import org.apache.bcel.generic.*;

public class SimpleLookupSwitchBuilder {

    public static void main(String[] args) {
        final ClassGen cg=new ClassGen(
                "package_name.SimpleLookupSwitch", "java.lang.Object","<generated>",
                Const.ACC_PUBLIC, null);
        
        final ConstantPoolGen cp = cg.getConstantPool();
        final InstructionList il = new InstructionList();
        final InstructionFactory factory = new InstructionFactory(cg);
        final ObjectType p_stream = new ObjectType("java.io.PrintStream");
        
        il.append(new ALOAD(0));//the argument of the main function
        il.append(new BIPUSH((byte)0));//Push 0.
        il.append(new AALOAD());//Got value of the variable as a String;

        il.append(factory.createInvoke(
                "java.lang.Integer","parseInt",
                Type.INT, new Type[]{Type.STRING}, Const.INVOKESTATIC)
        );//Now we got the value as int

        // CASE 1
        InstructionHandle case1 = il.append(
                factory.createFieldAccess(
                        "java.lang.System","out", p_stream, Const.GETSTATIC
                )
        );
        il.append(new LDC(cp.addString("One")));
        il.append(
                factory.createInvoke(
                        "java.io.PrintStream", "println",
                        Type.VOID, new Type[]{Type.STRING}, Const.INVOKEVIRTUAL
                )
        );
        BranchHandle goto1 = il.append(new GOTO(null));

        // CASE 2
        InstructionHandle case2 = il.append(
                factory.createFieldAccess(
                        "java.lang.System","out", p_stream, Const.GETSTATIC
                )
        );
        il.append(new LDC(cp.addString("Two")));
        il.append(
                factory.createInvoke(
                        "java.io.PrintStream", "println",
                        Type.VOID, new Type[]{Type.STRING}, Const.INVOKEVIRTUAL
                )
        );
        BranchHandle goto2 = il.append(new GOTO(null));

        // CASE 4
        InstructionHandle case4 = il.append(
                factory.createFieldAccess(
                        "java.lang.System","out", p_stream, Const.GETSTATIC
                )
        );
        il.append(new LDC(cp.addString("Four")));
        il.append(
                factory.createInvoke(
                        "java.io.PrintStream", "println",
                        Type.VOID, new Type[]{Type.STRING}, Const.INVOKEVIRTUAL
                )
        );
        BranchHandle goto4 = il.append(new GOTO(null));

        // CASE DEFAULT
        InstructionHandle caseDefault = il.append(
                factory.createFieldAccess(
                        "java.lang.System","out", p_stream, Const.GETSTATIC
                )
        );
        il.append(new LDC(cp.addString("Other")));
        il.append(
                factory.createInvoke(
                        "java.io.PrintStream", "println",
                        Type.VOID, new Type[]{Type.STRING}, Const.INVOKEVIRTUAL
                )
        );
        BranchHandle gotoDefault = il.append(new GOTO(null));

        InstructionHandle ret=il.append(new RETURN());

        il.insert(
                case1,
                new LOOKUPSWITCH(
                        new int [] {1,2,4},
                        new InstructionHandle[]{case1, case2, case4},
                        caseDefault)
        );
        goto1.setTarget(ret);
        goto2.setTarget(ret);
        goto4.setTarget(ret);
        gotoDefault.setTarget(ret);

        MethodGen mg = new MethodGen(
                Const.ACC_PUBLIC|Const.ACC_STATIC,
                Type.VOID,new Type[]{new ArrayType(Type.STRING, 1)},
                new String[]{"args"},"main","<generated>",il,cp);

        mg.setMaxLocals();
        mg.setMaxStack();

        cg.addMethod(mg.getMethod());

        try { cg.getJavaClass().dump("src/main/generated/SimpleLookupSwitchBuilder.class");}
        catch (IOException e) {System.err.println(e);}

        System.out.println("********Constant Pool**********");System.out.println(cp.getFinalConstantPool());
        System.out.println("********Method**********");System.out.println(mg);
        System.out.println("********Instruction List**********");System.out.println(il);
    }
}