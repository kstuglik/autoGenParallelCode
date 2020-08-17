package pl.edu.agh.bcel.transformations;

import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import pl.edu.agh.bcel.transformations.utils.LaunchProperties;

import java.util.*;

public class Variables {

    protected static void createLVarIfNoExisted(
            MethodGen mg, List<LocalVariable> listLVarToCreate, HashMap<Integer, Integer> hashmapLVarIdOldAndNew) {
        System.out.println("CREATE LOCAL VARIABLE IF NO EXISTED:");
        for (LocalVariable item : listLVarToCreate) {
            LocalVariableGen lg = mg.addLocalVariable(item.getName(), Type.getType(item.getSignature()), null, null);
            int oldVarId = item.getIndex();
            int newVarId = lg.getIndex();
            hashmapLVarIdOldAndNew.replace(oldVarId, newVarId);
            System.out.println("var name:\t" + item.getName() + ", [ID]:\told = " + oldVarId + ", new = " + newVarId);
        }
    }

    private static String getConstantName(ConstantPool constantPool, ConstantCP constant) {
        ConstantNameAndType constantNameAndType = (ConstantNameAndType) constantPool.getConstantPool()[constant.getNameAndTypeIndex()];
        return constantNameAndType.getName(constantPool);
    }

    public static int getFieldIdByName(String name, ClassGen cg) {
        Field[] fields = cg.getFields();
        for (Field field : fields) {
            if (field.getName().equals(name)) return field.getNameIndex();
        }
        return -1;
    }

    public static String getFieldNameById(int id, ClassGen cg) {
        Field[] fields = cg.getFields();
        for (Field field : fields) {
            if (field.getNameIndex() == id) return field.getName();
        }
        return "none";
    }

    public static int getFieldRefId(ClassGen classGen, String constantName) {
        ConstantPool constantPool = classGen.getConstantPool().getConstantPool();
        ConstantFieldref numThreadsField = Arrays.stream(constantPool.getConstantPool())
                .filter(ConstantFieldref.class::isInstance)
                .map(ConstantFieldref.class::cast)
                .filter((constant -> constantName.equals(getConstantName(constantPool, constant))))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Wrong state - constant " + constantName + " cannot be found."));
        for (int i = 0; i < constantPool.getConstantPool().length; i++) {
            if (Objects.equals(constantPool.getConstantPool()[i], numThreadsField)) return i;
        }
        return -1;
    }

    protected static HashMap<Integer, Integer> getHashmapOldAndNewLVarId(MethodGen mgOld, MethodGen mgNew) {

        HashMap<String, Integer> oldVariables = getHashmapStringIntegerLVar(mgOld);
        HashMap<String, Integer> newVariables = getHashmapStringIntegerLVar(mgNew);
        HashMap<Integer, Integer> hashmapOlNewLVarId = new HashMap<>();

        System.out.println(String.format("\n%-20s", "NAME") + "INDEXES FROM LOCALVARIABLETABLE");

        for (String key : oldVariables.keySet()) {
            int idOld = oldVariables.getOrDefault(key, -1);
            int idNew = newVariables.getOrDefault(key, -1);
            System.out.println(String.format("%-20s", key) + "[OLD: " + idOld + ", NEW: " + (idNew == -1 ? "-1 => NO EXIST" : idNew));
            hashmapOlNewLVarId.put(idOld, idNew);
        }

        return hashmapOlNewLVarId;
    }

    private static HashMap<String, Integer> getHashmapStringIntegerLVar(MethodGen mg) {
        LocalVariableGen[] lv = mg.getLocalVariables();
        HashMap<String, Integer> hashmapWithLocalVariablesIds = new HashMap<>();
        for (LocalVariableGen item : lv) hashmapWithLocalVariablesIds.put(item.getName(), item.getIndex());
        return hashmapWithLocalVariablesIds;
    }

    public static LocalVariable getIstructionInvoke(String name, LocalVariableTable localVariableTable) {
        return Arrays.stream(localVariableTable.getLocalVariableTable())
                .filter(var -> var.getSignature().equals("invokestatic"))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Variable " + name + " not found by name."));
    }

    public static LocalVariable getLVarByName(String name, LocalVariableTable localVariableTable) {
        return Arrays.stream(localVariableTable.getLocalVariableTable())
                .filter(var -> var.getName().equals(name))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Variable " + name + " not found by name."));
    }

    public static int getLVarIdByName(String name, MethodGen mg) {
        LocalVariable[] locals = mg.getLocalVariableTable(mg.getConstantPool()).getLocalVariableTable();
        for (LocalVariable local : locals) {
            if (local.getName().equals(name)) return local.getIndex();
        }
        return -1;
    }

    public static List<LocalVariable> getLVarListToCreate(MethodGen mgOld, HashMap<Integer, Integer> hashmapLVarIdOldAndNew) {

        List<LocalVariable> listLVarToCreate = new ArrayList<>();
        LocalVariable[] lvs = mgOld.getLocalVariableTable(mgOld.getConstantPool()).getLocalVariableTable();

//        if the variable id is in my list and is assigned -1, it copies this object
        for (LocalVariable item : lvs) {
            int idLVarInNewMethod = hashmapLVarIdOldAndNew.getOrDefault(item.getIndex(), -777);
            if (idLVarInNewMethod == -1) listLVarToCreate.add(item);
        }
        return listLVarToCreate;
    }

    public static LocalVariable getLVarNameById(int id, LocalVariableTable localVariableTable) {
        return Arrays.stream(localVariableTable.getLocalVariableTable())
                .filter(var -> var.getIndex() == id)
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Variable with id:" + id + " not found."));
    }

    public static String getLVarNameById(int id, MethodGen mg) {
        LocalVariable[] locals = mg.getLocalVariableTable(mg.getConstantPool()).getLocalVariableTable();
        for (LocalVariable local : locals) {
            if (local.getIndex() == id) return local.getName();
        }
        return "none";
    }

    public static LocalVariable[] getLVarTableFromMethod(ClassGen cg, MethodGen mgNew) {
        ConstantPoolGen cp = mgNew.getConstantPool();
        return mgNew.getLocalVariableTable(cp).getLocalVariableTable();
    }

    public static String getSignatureForLocalVariableByName(String name, LocalVariableTable localVariableTable) {
        return Arrays.stream(localVariableTable.getLocalVariableTable())
                .filter(var -> var.getName().equals(name))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Variable " + name + " not found by name.")).getSignature();
    }

    static int getSubTaskMethodIndexInConstants(ClassGen classGen) {
        ConstantPool constantPool = classGen.getConstantPool().getConstantPool();
        ConstantMethodref subTaskMethod = Arrays.stream(constantPool.getConstantPool())
                .filter(ConstantMethodref.class::isInstance)
                .map(ConstantMethodref.class::cast)
                .filter(method -> LaunchProperties.SUBTASK_METHOD_NAME.equals(getConstantName(constantPool, method)))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("No subTask method found."));
        for (int i = 1; i < constantPool.getConstantPool().length; i++) {
            if (Objects.equals(constantPool.getConstantPool()[i], subTaskMethod)) return i;
        }
        return -1;
    }

    public static HashMap<Integer, Integer> prepareLVarIfNoExistedAndReturnHashmap(MethodGen mgOld, MethodGen mgNew) {
        HashMap<Integer, Integer> hashmapIdOldAndNewLVar = Variables.getHashmapOldAndNewLVarId(mgOld, mgNew);
        List<LocalVariable> listLVarToCreate = Variables.getLVarListToCreate(mgOld, hashmapIdOldAndNewLVar);
        Variables.createLVarIfNoExisted(mgNew, listLVarToCreate, hashmapIdOldAndNewLVar);
        return hashmapIdOldAndNewLVar;
    }

    public static boolean checkIfFieldExist(Field[] fields, String fieldName) {

        for (Field field : fields) {
            if (field.getName().equals(fieldName)) return true;
        }
        return false;
    }

}
