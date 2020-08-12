package pl.edu.agh.utils;

import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.generic.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class LocalVariableUtils {

    protected static void createLVarIfNoExisted(
            MethodGen mg, List<LocalVariable> listLVarToCreate, HashMap<Integer, Integer> hashmapLVarIdOldAndNew) {
        System.out.println("CREATE LOCAL VARIABLE IF NO EXISTED:");
        for (LocalVariable item : listLVarToCreate) {
            LocalVariableGen lg = mg.addLocalVariable(item.getName(), Type.getType(item.getSignature()), null, null);
            int oldVarId = item.getIndex();
            int newVarId = lg.getIndex();
            hashmapLVarIdOldAndNew.replace(oldVarId, newVarId);
            System.out.println("variable name: " + item.getName() +
                    ": oldIndex = " + oldVarId + ", newIndex = " + newVarId);
        }
    }

    public static String getFieldNameById(int id, ClassGen cg) {
        Field[] fields = cg.getFields();
        for (Field field : fields) {
            if (field.getNameIndex() == id)
                return field.getName();
        }
        return "none";
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

    public static LocalVariable getINstructionInvoke(String name, LocalVariableTable localVariableTable) {
        return Arrays.stream(localVariableTable.getLocalVariableTable())
                .filter(var -> var.getSignature().equals("invokestatic"))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Variable " + name + " not found by name."));
    }

    public static int getIdFieldByName(String name, ClassGen cg) {
        Field[] fields = cg.getFields();
        for (Field field : fields) {
            if (field.getName().equals(name))
                return field.getNameIndex();
        }
        return -1;
    }

    public static int getIdLVarByName(String name, MethodGen mg) {
        LocalVariable[] locals = mg.getLocalVariableTable(mg.getConstantPool()).getLocalVariableTable();
        for (LocalVariable local : locals) {
            if (local.getName().equals(name))
                return local.getIndex();
        }
        return -1;
    }

    public static LocalVariable getLVarById(int id, LocalVariableTable localVariableTable) {
        return Arrays.stream(localVariableTable.getLocalVariableTable())
                .filter(var -> var.getIndex() == id)
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Variable with id:" + id + " not found."));
    }

    public static LocalVariable getLVarByName(String name, LocalVariableTable localVariableTable) {
        return Arrays.stream(localVariableTable.getLocalVariableTable())
                .filter(var -> var.getName().equals(name))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Variable " + name + " not found by name."));
    }

    public static List<LocalVariable> getListLVarToCreate(MethodGen mgOld, HashMap<Integer, Integer> hashmapLVarIdOldAndNew) {

        List<LocalVariable> listLVarToCreate = new ArrayList<>();
        LocalVariable[] lvs = mgOld.getLocalVariableTable(mgOld.getConstantPool()).getLocalVariableTable();

//        if the variable id is in my list and is assigned -1, it copies this object
        for (LocalVariable item : lvs) {
            int idLVarInNewMethod = hashmapLVarIdOldAndNew.getOrDefault(item.getIndex(), -777);
            if (idLVarInNewMethod == -1) listLVarToCreate.add(item);
        }
        return listLVarToCreate;
    }

    public static LocalVariable[] getLvarFromMethod(ClassGen cg, MethodGen mgNew) {
        ConstantPoolGen cp = mgNew.getConstantPool();
        return mgNew.getLocalVariableTable(cp).getLocalVariableTable();
    }

    public static String getNameLVarById(int id, MethodGen mg) {
        LocalVariable[] locals = mg.getLocalVariableTable(mg.getConstantPool()).getLocalVariableTable();
        for (LocalVariable local : locals) {
            if (local.getIndex() == id)
                return local.getName();
        }
        return "none";
    }

    public static String getSignatureForLocalVariableByName(String name, LocalVariableTable localVariableTable) {
        return Arrays.stream(localVariableTable.getLocalVariableTable())
                .filter(var -> var.getName().equals(name))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Variable " + name + " not found by name.")).getSignature();
    }

    public static HashMap<Integer, Integer> prepareLVarIfNoExistedAndReturnHashmap(MethodGen mgOld, MethodGen mgNew) {
        HashMap<Integer, Integer> hashmapIdOldAndNewLVar = LocalVariableUtils.getHashmapOldAndNewLVarId(mgOld, mgNew);
        List<LocalVariable> listLVarToCreate = LocalVariableUtils.getListLVarToCreate(mgOld, hashmapIdOldAndNewLVar);
        LocalVariableUtils.createLVarIfNoExisted(mgNew, listLVarToCreate, hashmapIdOldAndNewLVar);
        return hashmapIdOldAndNewLVar;
    }
}
