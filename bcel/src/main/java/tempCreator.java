import org.apache.bcel.generic.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.*;
import java.io.*;

public class tempCreator {
  private final InstructionFactory _factory;
  private final ConstantPoolGen    _cp;
  private final ClassGen           _cg;

  public tempCreator() {
    _cg = new ClassGen("temp", "java.lang.Object", "temp.java", Const.ACC_PUBLIC | Const.ACC_SUPER, new String[] {  });
    _cg.setMajor(52);
    _cg.setMinor(0);

    _cp = _cg.getConstantPool();
    _factory = new InstructionFactory(_cg, _cp);
  }

  public void create(OutputStream out) throws IOException {
    createFields();
    createMethod_0();
    createMethod_1();
    createMethod_2();
    createMethod_3();
    _cg.getJavaClass().dump(out);
  }

  private void createFields() {
    FieldGen field;

    field = new FieldGen(Const.ACC_STATIC, Type.INT, "resultRows", _cp);
    _cg.addField(field.getField());

    field = new FieldGen(Const.ACC_STATIC, Type.INT, "resultColumns", _cp);
    _cg.addField(field.getField());

    field = new FieldGen(Const.ACC_STATIC, Type.INT, "aaa", _cp);
    _cg.addField(field.getField());

    field = new FieldGen(Const.ACC_PUBLIC | Const.ACC_STATIC, new ArrayType(Type.FLOAT, 1), "arr", _cp);
    _cg.addField(field.getField());

    field = new FieldGen(Const.ACC_PUBLIC, new ArrayType(Type.FLOAT, 1), "data", _cp);
    _cg.addField(field.getField());

    field = new FieldGen(Const.ACC_PRIVATE, new ObjectType("java.util.List"), "partialResults", _cp);
    _cg.addField(field.getField());

    field = new FieldGen(Const.ACC_PRIVATE, new ArrayType(Type.FLOAT, 1), "results", _cp);
    _cg.addField(field.getField());

    field = new FieldGen(Const.ACC_PUBLIC | Const.ACC_STATIC, Type.INT, "NUM_THREADS", _cp);
    _cg.addField(field.getField());
  }

  private void createMethod_0() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(Const.ACC_PUBLIC, Type.VOID, Type.NO_ARGS, new String[] {  }, "<init>", "temp", il, _cp);

    InstructionHandle ih_0 = il.append(InstructionFactory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke("java.lang.Object", "<init>", Type.VOID, Type.NO_ARGS, Const.INVOKESPECIAL));
    InstructionHandle ih_4 = il.append(InstructionFactory.createLoad(Type.OBJECT, 0));
    il.append(new PUSH(_cp, 100));
    il.append(_factory.createNewArray(Type.FLOAT, (short) 1));
    il.append(_factory.createFieldAccess("temp", "data", new ArrayType(Type.FLOAT, 1), Const.PUTFIELD));
    il.append(InstructionFactory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_1() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(Const.ACC_PUBLIC | Const.ACC_STATIC, Type.VOID, new Type[] { new ArrayType(Type.STRING, 1) }, new String[] { "arg0" }, "main", "temp", il, _cp);

    InstructionHandle ih_0 = il.append(InstructionFactory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_2() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(Const.ACC_PUBLIC, Type.VOID, Type.NO_ARGS, new String[] {  }, "calculate", "temp", il, _cp);

    InstructionHandle ih_0 = il.append(new PUSH(_cp, 0));
    il.append(InstructionFactory.createStore(Type.INT, 1));
    InstructionHandle ih_2 = il.append(InstructionFactory.createLoad(Type.INT, 1));
    il.append(InstructionFactory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess("temp", "partialResults", new ObjectType("java.util.List"), Const.GETFIELD));
    il.append(_factory.createInvoke("java.util.List", "size", Type.INT, Type.NO_ARGS, Const.INVOKEINTERFACE));
        BranchInstruction if_icmpge_12 = InstructionFactory.createBranchInstruction(Const.IF_ICMPGE, null);
    il.append(if_icmpge_12);
    InstructionHandle ih_15 = il.append(InstructionFactory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess("temp", "partialResults", new ObjectType("java.util.List"), Const.GETFIELD));
    il.append(InstructionFactory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke("java.util.List", "get", Type.OBJECT, new Type[] { Type.INT }, Const.INVOKEINTERFACE));
    il.append(_factory.createCheckCast(new ObjectType("java.util.concurrent.Future")));
    il.append(InstructionFactory.createStore(Type.OBJECT, 2));
    InstructionHandle ih_29 = il.append(InstructionFactory.createLoad(Type.OBJECT, 2));
    il.append(_factory.createInvoke("java.util.concurrent.Future", "get", Type.OBJECT, Type.NO_ARGS, Const.INVOKEINTERFACE));
    il.append(_factory.createCheckCast(new ArrayType(Type.INT, 1)));
    il.append(InstructionFactory.createStore(Type.OBJECT, 3));
    InstructionHandle ih_39 = il.append(new PUSH(_cp, 0));
    il.append(InstructionFactory.createStore(Type.INT, 4));
    InstructionHandle ih_42 = il.append(InstructionFactory.createLoad(Type.INT, 4));
    il.append(InstructionFactory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess("temp", "results", new ArrayType(Type.FLOAT, 1), Const.GETFIELD));
    il.append(InstructionConst.ARRAYLENGTH);
        BranchInstruction if_icmpge_49 = InstructionFactory.createBranchInstruction(Const.IF_ICMPGE, null);
    il.append(if_icmpge_49);
    InstructionHandle ih_52 = il.append(InstructionFactory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess("temp", "results", new ArrayType(Type.FLOAT, 1), Const.GETFIELD));
    il.append(InstructionFactory.createLoad(Type.INT, 4));
    il.append(InstructionConst.DUP2);
    il.append(InstructionConst.FALOAD);
    il.append(InstructionFactory.createLoad(Type.OBJECT, 3));
    il.append(InstructionFactory.createLoad(Type.INT, 4));
    il.append(InstructionConst.IALOAD);
    il.append(InstructionConst.I2F);
    il.append(InstructionConst.FADD);
    il.append(InstructionConst.FASTORE);
    InstructionHandle ih_67 = il.append(new IINC(4, 1));
    InstructionHandle ih_70;
    BranchInstruction goto_70 = InstructionFactory.createBranchInstruction(Const.GOTO, ih_42);
    ih_70 = il.append(goto_70);
    InstructionHandle ih_73;
    BranchInstruction goto_73 = InstructionFactory.createBranchInstruction(Const.GOTO, null);
    ih_73 = il.append(goto_73);
    InstructionHandle ih_76 = il.append(InstructionFactory.createStore(Type.OBJECT, 3));
    InstructionHandle ih_77 = il.append(InstructionFactory.createLoad(Type.OBJECT, 3));
    il.append(_factory.createInvoke("java.lang.Exception", "printStackTrace", Type.VOID, Type.NO_ARGS, Const.INVOKEVIRTUAL));
    InstructionHandle ih_81 = il.append(new IINC(1, 1));
        BranchInstruction goto_84 = InstructionFactory.createBranchInstruction(Const.GOTO, ih_2);
    il.append(goto_84);
    InstructionHandle ih_87 = il.append(InstructionFactory.createReturn(Type.VOID));
    if_icmpge_12.setTarget(ih_87);
    if_icmpge_49.setTarget(ih_73);
    goto_73.setTarget(ih_81);
    method.addExceptionHandler(ih_29, ih_70, ih_76, new ObjectType("java.lang.InterruptedException"));
    method.addExceptionHandler(ih_29, ih_70, ih_76, new ObjectType("java.util.concurrent.ExecutionException"));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_3() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(Const.ACC_STATIC, Type.VOID, Type.NO_ARGS, new String[] {  }, "<clinit>", "temp", il, _cp);

    InstructionHandle ih_0 = il.append(new PUSH(_cp, 2));
    il.append(_factory.createFieldAccess("temp", "resultRows", Type.INT, Const.PUTSTATIC));
    InstructionHandle ih_4 = il.append(new PUSH(_cp, 3));
    il.append(_factory.createFieldAccess("temp", "resultColumns", Type.INT, Const.PUTSTATIC));
    InstructionHandle ih_8 = il.append(new PUSH(_cp, 1));
    il.append(_factory.createFieldAccess("temp", "aaa", Type.INT, Const.PUTSTATIC));
    InstructionHandle ih_12 = il.append(new PUSH(_cp, 10));
    il.append(_factory.createNewArray(Type.FLOAT, (short) 1));
    il.append(_factory.createFieldAccess("temp", "arr", new ArrayType(Type.FLOAT, 1), Const.PUTSTATIC));
    InstructionHandle ih_19 = il.append(new PUSH(_cp, 9));
    il.append(_factory.createFieldAccess("temp", "NUM_THREADS", Type.INT, Const.PUTSTATIC));
    il.append(InstructionFactory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  public static void main(String[] args) throws Exception {
    tempCreator creator = new tempCreator();
    creator.create(new FileOutputStream("temp.class"));
  }
}
