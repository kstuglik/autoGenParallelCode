package pl.edu.agh.bcel.utils;

import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import pl.edu.agh.bcel.LaunchProperties;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;

public class VariableUtils {

    public static boolean checkIfFieldExist(Field[] fields, String fieldName) {
        for (Field field : fields) if (field.getName().equals(fieldName)) return true;
        return false;
    }

    protected static void createLVarFromList(
            MethodGen mg, List<LocalVariable> listLVarToCreate, HashMap<Integer, Integer> hashmapLVarIdOldAndNew) {
        System.out.println("\n---> CREATE LOCAL VARIABLE IF NO EXISTED <---\n");
        for (LocalVariable item : listLVarToCreate) {
            LocalVariableGen lg = mg.addLocalVariable(item.getName(), Type.getType(item.getSignature()), null, null);
            int oldVarId = item.getIndex();
            int newVarId = lg.getIndex();
            hashmapLVarIdOldAndNew.replace(oldVarId, newVarId);
            System.out.print("\nvariable name:\t" + item.getName() + ", [ID]:\told = " + oldVarId + ", new = " + newVarId);
        }
        System.out.println();
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

    public static HashMap<Integer, Integer> getHashmapLVarIndexesOldAndNew(MethodGen mgOld, MethodGen mgNew) {
        HashMap<Integer, Integer> hashmapLVarIndexesOldAndNew = VariableUtils.getHashmapOldAndNewLVarId(mgOld, mgNew);

        List<LocalVariable> listLVarToCreate = VariableUtils.getLVarListToCreate(mgOld, hashmapLVarIndexesOldAndNew);

        VariableUtils.createLVarFromList(mgNew, listLVarToCreate, hashmapLVarIndexesOldAndNew);

        return hashmapLVarIndexesOldAndNew;
    }

    protected static HashMap<Integer, Integer> getHashmapOldAndNewLVarId(MethodGen mgOld, MethodGen mgNew) {

        HashMap<String, Integer> oldVariables = getHashmapStringIntegerLVar(mgOld);
        HashMap<String, Integer> newVariables = getHashmapStringIntegerLVar(mgNew);
        HashMap<Integer, Integer> hashmapOlNewLVarId = new HashMap<>();

        for (String key : oldVariables.keySet()) {
            int idOld = oldVariables.get(key);
            int idNew = newVariables.getOrDefault(key, -1);
//            System.out.print(""+key + ", old ID: " + oldVariables.get(key)+", new ID: "+idNew);
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

    public static List<LocalVariable> getLVarListToCreate(
            MethodGen mgOld, HashMap<Integer, Integer> hashmapLVarIdOldAndNew) {

        List<LocalVariable> listLVarToCreate = new ArrayList<>();
        LocalVariable[] lvsAll = mgOld.getLocalVariableTable(mgOld.getConstantPool()).getLocalVariableTable();
        LocalVariable[] lvsOhneThis = Arrays.copyOfRange(lvsAll, 1, lvsAll.length);

//        if the variable id is in my list and is assigned -1, it copies this object
        for (LocalVariable item : lvsOhneThis) {
            int idLVarInNewMethod = hashmapLVarIdOldAndNew.getOrDefault(item.getIndex(), -777);
            if (idLVarInNewMethod == -1) {
                listLVarToCreate.add(item);
            }
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

    public static String getSignatureForFieldsByName(String name, ClassGen cg) {
        Field[] fields = cg.getFields();

        return Arrays.stream(fields)
                .filter(var -> var.getName().equals(name))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Field: " + name + " not found.")).getSignature();
    }

    public static String getSignatureVarByName(String name, LocalVariableTable localVariableTable) {
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

    private static List<Integer> getVariableIndexes(InstructionList il) {
        //we will not consider 4th instruction which is for(...; I < VAR;...) because VAR will be replaced
        InstructionHandle irrelevant = il.getInstructionHandles()[0].getNext().getNext().getNext();
        return Arrays.stream(il.getInstructionHandles())
                .filter(handle -> !handle.equals(irrelevant))
                .filter(handle -> handle.getInstruction() instanceof LoadInstruction)
                .map(handle -> (LoadInstruction) handle.getInstruction())
                .map(LoadInstruction::getIndex)
                .distinct()
                .collect(Collectors.toList());
    }

    public static List<LocalVariableGen> getVariablesToCopy(MethodGen mg, InstructionList il) {
        List<Integer> variableIndexes = getVariableIndexes(il);
        return Arrays.stream(mg.getLocalVariables())
                .filter(variable -> variableIndexes.contains(variable.getIndex()))
                .collect(Collectors.toList());
    }

    public static void replaceSpecificIndexes(int idOLD, int idToReplace, HashMap<Integer, Integer> hashmapIdOldAndNewLVar) {
        hashmapIdOldAndNewLVar.replace(idOLD, idToReplace);
    }

    public static Instruction updateLVarIndexes(
            Instruction instr, HashMap<Integer, Integer> hashmapIdOldAndNewLVar, MethodGen mgNEW, ClassGen cg) {

        Instruction replace = null;
        int idOLD = -1, idNEW;

        if (instr.toString().contains("load") || instr.toString().contains("store")) {

            String[] string = instr.toString().split("\\W+|_");
            int ile = string.length;

            if (instr.toString().contains("_")) {
                if (instr.toString().contains("const")) idOLD = Integer.parseInt(string[1]);
                else idOLD = Integer.parseInt(string[ile - 1]);
                idNEW = hashmapIdOldAndNewLVar.getOrDefault(idOLD, -1);
                System.out.println("idOLD:" + idOLD + ", idNEW:" + idNEW);

                if (idNEW != -1) {
                    LocalVariable lv = VariableUtils.getLVarNameById(idNEW, mgNEW.getLocalVariableTable(cg.getConstantPool()));

                    if (string[0].contains("load")) {
                        replace = InstructionFactory.createLoad(Type.getType(lv.getSignature()), idNEW);
                    } else if (string[0].contains("store")) {
                        replace = InstructionFactory.createStore(Type.getType(lv.getSignature()), idNEW);
                    } else if (string[0].contains("const")) {
                        if (idNEW > 5)
                            replace = InstructionFactory.createStore(Type.getType(lv.getSignature()), idNEW);
                        else
                            replace = new ICONST(idNEW);
                    }
                }
            } else {

                idOLD = Integer.parseInt(string[ile - 1]);
                idNEW = hashmapIdOldAndNewLVar.getOrDefault(idOLD, -1);

                if (idNEW != -1) {
                    if (string[0].contains("store")) replace = new ISTORE(idNEW);
                    else if (string[0].contains("load")) replace = new ILOAD(idNEW);
                    else if (string[0].contains("inc")) {
                        idOLD = Integer.parseInt(string[ile - 2]);
                        idNEW = hashmapIdOldAndNewLVar.getOrDefault(idOLD, -1);

                        if (idNEW != -1) {
                            int incrementValue = Integer.parseInt(string[ile - 1]);
                            replace = new IINC(idNEW, incrementValue);
                        }
                    }

                }
            }
        }

        if (replace != null) return replace;
        else return instr;
    }

    private static void displayLVarFromMethod(ConstantPoolGen cp, MethodGen mgNew) {
        LocalVariable[] lvtp = mgNew.getLocalVariableTable(cp).getLocalVariableTable();
        System.out.println("\n");
        for (LocalVariable localVariable : lvtp) System.out.println(localVariable);
        System.out.println("\n");
    }

    public static int getCorrectIdInNewMethod(int id, int position, MethodGen mgOld, MethodGen mgNew) {

        LocalVariable[] lvgOld = mgOld.getLocalVariableTable(mgOld.getConstantPool()).getLocalVariableTable();

        for (LocalVariable localVariable : lvgOld) {
            int tempEnd = localVariable.getStartPC() + localVariable.getLength();
            int tempId = localVariable.getIndex();
            if (tempId == id && tempEnd >= position)
                return VariableUtils.getLVarIdByName(localVariable.getName(), mgNew);
        }

        return 0;
    }

    public static Instruction replaceOldIdInInstruction(InstructionHandle instruction, MethodGen mgOld, MethodGen mgNew) {
//        load/store
        Instruction replace = null;
        String instrString = instruction.toString();
        String[] string = instrString.split("\\W+|_");
        int idOLD = -1;
        int idNEW = 0;
        int length = string.length;

        if (string[2].contains("load") || string[2].contains("store")) {
            if (instruction.toString().contains("_")) idOLD = parseInt(string[3]);
            else idOLD = parseInt(string[length - 1]);
            idNEW = VariableUtils.getCorrectIdInNewMethod(idOLD, instruction.getPosition(), mgOld, mgNew);
            if (idNEW != -1) {
//                STORE
                switch (string[2]) {
                    case "istore":
                        replace = new ISTORE(idNEW);
                        break;
                    case "dstore":
                        replace = new DSTORE(idNEW);
                        break;
                    case "fstore":
                        replace = new FSTORE(idNEW);
                        break;
                    case "astore":
                        replace = new ASTORE(idNEW);
                        break;
//                LOAD
                    case "aload":
                        replace = new ALOAD(idNEW);
                        break;
                    case "fload":
                        replace = new FLOAD(idNEW);
                        break;
                    case "iload":
                        replace = new ILOAD(idNEW);
                        break;
                    case "dload":
                        replace = new DLOAD(idNEW);
                        break;
                }
            }
        } else if (instrString.contains("inc")) {
            idOLD = parseInt(string[length - 2]);
            idNEW = VariableUtils.getCorrectIdInNewMethod(idOLD, instruction.getPosition(), mgOld, mgNew);
            if (idNEW != -1) {
                int incrementValue = parseInt(string[length - 1]);
                replace = new IINC(idNEW, incrementValue);
            }
        }
//        System.out.print("\t"+idNEW+"\n");
        if (replace != null) return replace;
        else return instruction.getInstruction();
    }

}
