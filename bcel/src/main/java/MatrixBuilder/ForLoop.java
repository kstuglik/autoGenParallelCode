package MatrixBuilder;

import org.apache.bcel.Const;
import org.apache.bcel.generic.*;

import java.io.IOException;

public class ForLoop {

    public static void main(String[] args) {
        String packageName = "MatrixBuilder";
        String clsName = "TestForLoop";
        String fullClsName = packageName+"."+clsName;
        ClassGen cg = new ClassGen(
                fullClsName, "java.lang.Object",
                "<generated>", Const.ACC_PUBLIC | Const.ACC_SUPER, null);
        ConstantPoolGen cp = cg.getConstantPool(); // cg creates constant pool
        InstructionList il = new InstructionList();
        InstructionFactory factory = new InstructionFactory(cg);

        short acc_flags = Const.ACC_PUBLIC;
        final MethodGen mg = new MethodGen(
                Const.ACC_PUBLIC|Const.ACC_STATIC,
                Type.VOID,
                new Type[]{new ArrayType(Type.STRING, 1)},
                new String[]{"argv"},
                "main",
                clsName,
                il,
                cp
        );

        //double sum = 0;
        LocalVariableGen lg;
        lg = mg.addLocalVariable("sum", Type.DOUBLE, null, null);
        int idxSum = lg.getIndex();
        il.append(new LDC(cp.addDouble(0.0)));
        lg.setStart(il.append(new DSTORE(idxSum))); // "sum" valid from here

        //////////////////////////////////////////////////////////////
        //	for(int i=0; i<10; i++) {
        //		sum += args[i];
        //	}
        /////////////////////////////////////////////////////////////
        lg = mg.addLocalVariable("A", new ArrayType(Type.DOUBLE, 1), null, null);
        int idxA = lg.getIndex();
        il.append(new PUSH(cp, 6));
        il.append(new NEWARRAY(Type.DOUBLE));
        il.append(new ASTORE(idxA));
        /////////////////////////////////////////////////////////////
        lg = mg.addLocalVariable("B", new ArrayType(Type.DOUBLE, 1), null, null);
        int idxB = lg.getIndex();
        il.append(new PUSH(cp, 6));
        il.append(new NEWARRAY(Type.DOUBLE));
        il.append(new ASTORE(idxB));
        /////////////////////////////////////////////////////////////
        lg = mg.addLocalVariable("r1", Type.INT, null, null);
        int idxR1 = lg.getIndex();
        il.append(new LDC(cp.addInteger(2)));
        lg.setStart(il.append(new ISTORE(idxR1)));
        /////////////////////////////////////////////////////////////
        lg = mg.addLocalVariable("c1", Type.INT, null, null);
        int idxC1 = lg.getIndex();
        il.append(new LDC(cp.addInteger(3)));
        lg.setStart(il.append(new ISTORE(idxC1)));
        /////////////////////////////////////////////////////////////
        lg = mg.addLocalVariable("r2", Type.INT, null, null);
        int idxR2 = lg.getIndex();
        il.append(new LDC(cp.addInteger(3)));
        lg.setStart(il.append(new ISTORE(idxR2)));
        /////////////////////////////////////////////////////////////
        lg = mg.addLocalVariable("c2", Type.INT, null, null);
        int idxC2 = lg.getIndex();
        il.append(new LDC(cp.addInteger(2)));
        lg.setStart(il.append(new ISTORE(idxC2)));
        /////////////////////////////////////////////////////////////
        lg = mg.addLocalVariable("width", Type.INT, null, null);
        int idxWidth = lg.getIndex();
        il.append(new LDC(cp.addInteger(2)));//Load the constant
        il.append(new LDC(cp.addInteger(3)));//Load the constant
        il.append(new IMUL());//Add
        il.append(new ISTORE(idxWidth));
        /////////////////////////////////////////////////////////////
        lg = mg.addLocalVariable("C", new ArrayType(Type.DOUBLE, 1), null, null);
        int idxC = lg.getIndex();
        il.append(new ILOAD(idxWidth));
        il.append(new NEWARRAY(Type.DOUBLE));
        il.append(new ASTORE(idxC));
        /////////////////////////////////////////////////////////////
        //  --------------------------FOR LOOOP----------------------
        lg = mg.addLocalVariable("i", Type.INT, null, null);
        int idxI = lg.getIndex();

        lg = mg.addLocalVariable("j", Type.INT, null, null);
        int idxJ = lg.getIndex();

        lg = mg.addLocalVariable("k", Type.INT, null, null);
        int idxK = lg.getIndex();

        il.append(new LDC(cp.addInteger(0)));
        lg.setStart(il.append(new ISTORE(idxI))); // "i" valid from here

        InstructionHandle loopStart = il.append(new LDC(cp.addInteger(0)));
        lg.setStart(il.append(new ISTORE(idxJ))); // "j" valid from here
        //Loop sum = A[i] + B[i];
        InstructionHandle loopStart2 = il.append(new LDC(cp.addInteger(0)));
        lg.setStart(il.append(new ISTORE(idxK))); // "k" valid from here

        InstructionHandle loopStart3 = il.append(new DLOAD(idxA));

        il.append(new ILOAD(idxI));
        il.append(new DALOAD());
        il.append(new DLOAD(idxB));
        il.append(new ILOAD(idxJ));
        il.append(new DALOAD());
        il.append(new DMUL());
        il.append(new DLOAD(idxSum));
        il.append(new DADD());
        il.append(new DSTORE(idxSum));


        //k++
        il.append(new IINC(idxK, 1));
        //Compare: k < r2 - for(int j = 0; j < c2; ++j)
        InstructionHandle loopCmp3 = il.append(new ILOAD(idxK));
        il.append(new ILOAD(idxR2));
        il.append(new IF_ICMPLT(loopStart3));
        il.insert(loopStart3, new GOTO(loopCmp3));

        //j++
        il.append(new IINC(idxJ, 1));
        //Compare: j < c2 - for(int j = 0; j < c2; ++j)
        InstructionHandle loopCmp2 = il.append(new ILOAD(idxJ));
        il.append(new ILOAD(idxC2));
        il.append(new IF_ICMPLT(loopStart2));
        il.insert(loopStart2, new GOTO(loopCmp2));

        //i++
        il.append(new IINC(idxI, 1));

        //Compare: i < r1 - for(int i = 0; i < r1; ++i)
        InstructionHandle loopCmp = il.append(new ILOAD(idxI));
        il.append(new ILOAD(idxR1));
        il.append(new IF_ICMPLT(loopStart));

        il.insert(loopStart, new GOTO(loopCmp));

        il.append(InstructionConst.RETURN);
        /////////////////////////////////////////////////////////////


        mg.setMaxStack();
        cg.addMethod(mg.getMethod());
        il.dispose(); // Allow instruction handles to be reused

        cg.addEmptyConstructor(Const.ACC_PUBLIC);

        try { cg.getJavaClass().dump("src/main/java/MatrixBuilder/generated/TestForLoop.class");}
        catch (IOException e) {System.err.println(e);}
    }
}