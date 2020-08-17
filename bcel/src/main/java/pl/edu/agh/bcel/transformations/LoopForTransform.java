package pl.edu.agh.bcel.transformations;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.generic.*;
import pl.edu.agh.bcel.transformations.utils.LaunchProperties;

import java.util.*;

/*
    The case handled refers to nested loops - one series i.e:
              code... nested loops... code
              Nested loops... code...
              code... nested loops

    does not yet support a type case:
              code... nested loops... code... nested loops...
*/

public class LoopForTransform {

    private static final InstructionHandle dodatkowe = null;
    private static final List<InstructionHandle> temporary = new ArrayList<>();
    static int currRowId = 0;
    static String nameIteratorInForLoop = "r";
    static String lvarNameToReplaceWithCurrRow = "rowNum";

    public static void addIncrementOrNextHandleToHashmap(
            HashMap<Integer, List<InstructionHandle>> hashmapa, int key, InstructionHandle value) {
        if (hashmapa.containsKey(key)) {
            hashmapa.get(key).add(value);
        } else {
            List<InstructionHandle> list = new ArrayList<>();
            list.add(value);
            hashmapa.put(key, list);
        }
    }

    public static void addSubTaskMethodFromNestedLoop(
            ClassGen cg, MethodGen mgOld, LoopFor loopForItem, HashMap<Integer, Integer> hashmapPositionId) {


        List<InstructionHandle> handlesLoopStart = new ArrayList<>();
        List<BranchHandle> branchesGOTO = new ArrayList<>();
        List<BranchHandle> branchHandlesIf = new ArrayList<>();
        HashMap<Integer, List<BranchHandle>> branchesIF = new HashMap<>();
        HashMap<Integer, List<InstructionHandle>> incrementOrNextHandle = new HashMap<>();//key - number of loop, keys: increment handles in looop


        InstructionList il = new InstructionList();
        ConstantPoolGen cp = cg.getConstantPool();
        InstructionFactory factory = new InstructionFactory(cg, cp);
        InstructionHandle[] ihy = mgOld.getInstructionList().getInstructionHandles();


        MethodGen mgNew = new MethodGen(Const.ACC_PRIVATE | Const.ACC_STATIC, Type.INT, new Type[]{Type.INT, Type.INT, Type.INT},
                new String[]{LaunchProperties.ROW_NUM_VAR_NAME, LaunchProperties.COL_NUM_VAR_NAME, LaunchProperties.STEP_VAR_NAME},
                LaunchProperties.SUBTASK_METHOD_NAME, cg.getClassName(), il, mgOld.getConstantPool());

        currRowId = mgNew.addLocalVariable(LaunchProperties.CURR_ROW_VAR_NAME, Type.INT, null, null).getIndex();
        int idNameAccessToChangeWithCurrRow = Variables.getLVarIdByName(lvarNameToReplaceWithCurrRow, mgOld);
        System.out.println("id variable to `change place` with currRow: " + idNameAccessToChangeWithCurrRow);

//      CREATE NEW VARIABLES IF NO EXIST AND PREPARE hASH MAP WITH OLD AND NEW ID
        HashMap<Integer, Integer> hashmapIdOldAndNewLVar =
                Variables.prepareLVarIfNoExistedAndReturnHashmap(mgOld, mgNew);

//      for(int currRow = rowNum;  currRow < rowNum + step && currRow < resultRows;  ++currRow)
//      STATIC PART OF CODE, ITS FIRST FOR
        il.append(InstructionFactory.createLoad(Type.INT, 0));
        il.append(InstructionFactory.createStore(Type.INT, currRowId));

        InstructionHandle startExternalLoopHandle = il.append(InstructionFactory.createLoad(Type.INT, currRowId));
        il.append(InstructionFactory.createLoad(Type.INT, 0));
        il.append(InstructionFactory.createLoad(Type.INT, 2));
        il.append(InstructionConst.IADD);

        BranchHandle if_icmpge_6 = il.append(new IF_ICMPGE(null));
        il.append(InstructionFactory.createLoad(Type.INT, currRowId));
        il.append(factory.createFieldAccess(cg.getClassName(), "resultRows", Type.INT, Const.GETSTATIC));

        BranchHandle if_icmpge_13 = il.append(new IF_ICMPGE(null));

//        ******************** DYNAMIC PART OF "FOR LOOP" ********************* START

        int idFirst = hashmapPositionId.get(loopForItem.getFirstInstructionInFor().getPosition());
        int idLoopStart = hashmapPositionId.get(loopForItem.getLoopStartHandler().getPosition());
        int idIfComparator = hashmapPositionId.get(loopForItem.getIfcompareHandler().get(0).getPosition());
        int countIfInOneForLoop = loopForItem.getIfcompareHandler().size();
        int nextInstructionAfterIfStatement = idIfComparator + countIfInOneForLoop;

        /*
        ADD INSTRUCTION, FROM RANGE: FROM  FIRST TO IF
        I CREATE NEW, BECAUSE IF CODE WILL BE CHANGED THAT REFERENCE CAN BE INCORRECT*/

        for (int j = idFirst; j < idIfComparator; j++) {
            if (j == idLoopStart) {
                InstructionHandle ih = il.append(
                        updateOldIndexInLVar(ihy[j].getInstruction(), hashmapPositionId, factory, mgOld, mgNew));
                handlesLoopStart.add(ih);
            } else {
                il.append(
                        updateOldIndexInLVar(ihy[j].getInstruction(), hashmapPositionId, factory, mgOld, mgNew));
            }
        }

//          ADD IF COMPARATOR

        for (int j = idIfComparator; j < (idIfComparator + countIfInOneForLoop); j++) {
            BranchHandle if_compare = null;
            if (ihy[j].getInstruction() instanceof IF_ICMPGE) if_compare = il.append(new IF_ICMPGE(null));
            if (ihy[j].getInstruction() instanceof IF_ICMPGT) if_compare = il.append(new IF_ICMPGT(null));
            if (ihy[j].getInstruction() instanceof IF_ICMPLE) if_compare = il.append(new IF_ICMPLE(null));
            if (ihy[j].getInstruction() instanceof IF_ICMPLT) if_compare = il.append(new IF_ICMPLT(null));
            if (ihy[j].getInstruction() instanceof IF_ICMPEQ) if_compare = il.append(new IF_ICMPEQ(null));
            if (ihy[j].getInstruction() instanceof IF_ICMPNE) if_compare = il.append(new IF_ICMPNE(null));
            if (ihy[j].getInstruction() instanceof IF_ACMPEQ) if_compare = il.append(new IF_ACMPEQ(null));
            if (ihy[j].getInstruction() instanceof IF_ACMPNE) if_compare = il.append(new IF_ACMPNE(null));
            if (ihy[j].getInstruction() instanceof IFLE) if_compare = il.append(new IFLE(null));
            if (ihy[j].getInstruction() instanceof IFLT) if_compare = il.append(new IFLT(null));
            if (ihy[j].getInstruction() instanceof IFGE) if_compare = il.append(new IFGE(null));
            if (ihy[j].getInstruction() instanceof IFGT) if_compare = il.append(new IFGT(null));
            if (ihy[j].getInstruction() instanceof IFEQ) if_compare = il.append(new IFEQ(null));
            if (ihy[j].getInstruction() instanceof IFNE) if_compare = il.append(new IFNE(null));
            branchHandlesIf.add(if_compare);
        }//MAYBE: ihy[j].getInstruction().toString().contains("if") will be work

        branchesIF.put(0, branchHandlesIf);

        int positionIncrement = loopForItem.getIncrementHandler().getPosition();
        int idIncrement = hashmapPositionId.get(positionIncrement);


//      >>>>>>>>>>>>>>>>>>>>>>      NEW INSTRUCTION INSIDE INNER LOOP BODY START HERE   >>>>>>>>>>>>>>>>>>>>>>
        il.append(factory.createPrintln("INSIDE LOOP :)"));
//        istnieje jeszcze przynajmniej jeden przypadek nie obsuzony: instrukcje warunkowe wewnatrz petli!!!

        for (int i = nextInstructionAfterIfStatement; i < idIncrement; i++) {
            il.append(updateOldIndexInLVar(ihy[i].getInstruction(), hashmapIdOldAndNewLVar, factory, mgOld, mgNew));
        }
//      <<<<<<<<<<<<<<<<<<<<<<      NEW INSTRUCTION INSIDE INNER LOOP BODY END HERE   <<<<<<<<<<<<<<<<<<<<<<


//        CLOSE INTERNAL LOOP
        String[] string = ihy[idIncrement].getInstruction().toString().split(" ");
        System.out.println(Arrays.toString(string));
        int idOLD = Integer.parseInt(string[string.length - 2]);
        int idNEW = hashmapIdOldAndNewLVar.getOrDefault(idOLD, -1);
        if (idNEW != -1) {
            int incrementValue = Integer.parseInt(string[string.length - 1]);
            InstructionHandle ih = il.append(new IINC(idNEW, incrementValue));
            addIncrementOrNextHandleToHashmap(incrementOrNextHandle, 0, ih);
        } else {
            InstructionHandle ih = il.append(ihy[idIncrement].getInstruction());
            addIncrementOrNextHandleToHashmap(incrementOrNextHandle, 0, ih);
        }


//        il.append(ihy[idIncrement].getInstruction());

        BranchHandle gotoHandler = il.append(new GOTO(handlesLoopStart.get(0)));
        branchesGOTO.add(gotoHandler);

//        ******************** STATIC PART OF "FOR LOOP" ********************* START
        InstructionHandle incFirstLoop = il.append(new IINC(currRowId, 1));
        BranchInstruction goto_26 = InstructionFactory.createBranchInstruction(Const.GOTO, startExternalLoopHandle);
        il.append(goto_26);
        InstructionHandle returnHandler = il.append(new PUSH(cp, 0));
        il.append(InstructionFactory.createReturn(Type.INT));
        if_icmpge_6.setTarget(returnHandler);
        if_icmpge_13.setTarget(returnHandler);

//        ******************** DYNAMIC PART OF "FOR LOOP" ********************* START
//branchesIF element consists of (indexOfLoopFor,List<BranchHandle>)

        for (Integer numberOfLoopFor : branchesIF.keySet()) {
//            if there are more internal loops then you have to modify,
//            because here the case for the penultimate loop has been handled,
//            then it actually points to firstIncrement
            for (int i = 0; i < branchesIF.get(0).size(); i++) {
                branchesIF.get(numberOfLoopFor).get(i).setTarget(incFirstLoop);
            }
        }

//        ******************** DYNAMIC PART OF "FOR LOOP" ********************* END

        mgNew.setInstructionList(il);
        mgNew.setMaxLocals();
        mgNew.setMaxStack();
        cg.addMethod(mgNew.getMethod());
        cg.getConstantPool().addMethodref(mgNew);
        il.dispose();
    }

    public static void changeNestedLoopInMatrixMultiply(ClassGen cg, MethodGen mgOld) {

        InstructionList ilOld = mgOld.getInstructionList();
        InstructionHandle[] ihy = ilOld.getInstructionHandles();

//        DISPLAY INFO ABOUT STRUCTURE OF SELECTED METHOD WITH CONSIDER HANDLES
//        LoopUtils.getHashMapLoopStructure(ihy);

        HashMap<Integer, Integer> hashmapPositionId = InstructionUtils.getHashmapPositionId(ihy);
        List<LoopFor> loopForItemsList = getListLoopForItems(ihy, hashmapPositionId);

        LoopUtils.displayInfoAboutLoopInMethod(loopForItemsList);

        List<LoopFor> loopForSubTask = new ArrayList<>(loopForItemsList.subList(2, 3));

//        int numberOfLoopToMove = 1;
        addSubTaskMethodFromNestedLoop(cg, mgOld, loopForItemsList.get(2), hashmapPositionId);

        InstructionList ilToAdd = parallelizeMethodWithNestedLoop(loopForItemsList, ihy, hashmapPositionId, cg, mgOld);

        mgOld.setInstructionList(ilToAdd);
        mgOld.setMaxLocals();
        mgOld.setMaxStack();
        cg.removeMethod(mgOld.getMethod());
        cg.addMethod(mgOld.getMethod());
        cg.getConstantPool().addMethodref(mgOld);
        ilToAdd.dispose();
    }

    public static void displayIncrementOrNextHandles(HashMap<Integer, List<InstructionHandle>> incrementOrNextHandle) {
        for (Integer key : incrementOrNextHandle.keySet()) {
            for (int i = 0; i < incrementOrNextHandle.get(key).size(); i++) {
                System.out.println("key = " + key + ", i = " + i + ", " + incrementOrNextHandle.get(key).get(i));
            }
        }
    }

    private static int findStartLoopHandlerId(List<Integer> listWithILOADids, int idIfHandle) {
        int i;
        for (i = listWithILOADids.size() - 1; listWithILOADids.get(i) > idIfHandle; i--) {
        }
        return listWithILOADids.get(i);
    }

    private static void forLoopItemSetIfAndFirstInLoopHandles(
            LoopFor item, InstructionHandle[] ihy, HashMap<Integer, Integer> hashmapPositionId) {

        int positionStart = item.getLoopStartHandler().getPosition();
        int idStart = hashmapPositionId.get(positionStart);

        int positionEnd = item.getGotoHandler().getPosition();
        int idEnd = hashmapPositionId.get(positionEnd);

        for (int i = idStart; i < idEnd; i++) {
            Instruction instr = ihy[i].getInstruction();
            if (instr instanceof IF_ICMPGE || instr instanceof IF_ICMPGT ||
                    instr instanceof IF_ICMPLE || instr instanceof IF_ICMPLT ||
                    instr instanceof IF_ICMPEQ || instr instanceof IF_ICMPNE ||
                    instr instanceof IF_ACMPEQ || instr instanceof IF_ACMPNE ||
                    instr instanceof IFLE || instr instanceof IFLT ||
                    instr instanceof IFGE || instr instanceof IFGT ||
                    instr instanceof IFEQ || instr instanceof IFNE) {
                item.setIfCompareHandle(ihy[i]);
                item.setFirstInLoopHandle(ihy[i + 1]);
            }
        }
    }

    private static void forLoopSetFirstInForAndStartLoopHandles(
            LoopFor item, InstructionHandle[] ihy, HashMap<Integer, Integer> hashmapPositionId) {
        int position = Integer.parseInt(item.getGotoHandler().toString().split("->")[1].replace(" ", ""));
        int idStartLoopArray = getIdByPositionInCode(hashmapPositionId, position);
        item.setLoopStartHandler(ihy[idStartLoopArray]);
//        ussualy: iload and istore are 2 instructions before idStartLoopArray
        item.setFirstInstructionInFor(ihy[idStartLoopArray].getPrev().getPrev());
    }

    private static void forLoopValidateAssignedIfStatement(List<LoopFor> loopForItemList) {
        Collections.reverse(loopForItemList);
        for (int i = 0; i < loopForItemList.size() - 1; i++) {// -1 because current "item" and next "item" are compared
            ArrayList<InstructionHandle> externalLoop = loopForItemList.get(i).getIfcompareHandler();
            ArrayList<InstructionHandle> innnerLoop = loopForItemList.get(i + 1).getIfcompareHandler();
            externalLoop.removeAll(innnerLoop);
        }
    }

    public static int getIdByPositionInCode(HashMap<Integer, Integer> hashmapPositionId, int position) {
        return hashmapPositionId.get(position);
    }

    private static InstructionHandle[] getInstructionAfterLoop(
            InstructionHandle[] ihy, List<LoopFor> loopFORs, HashMap<Integer, Integer> hashmapPositionId) {
        int getIdInArray = hashmapPositionId.get(
                loopFORs.get(0).getGotoHandler().getPosition()) + 1;
        return Arrays.copyOfRange(ihy, getIdInArray, ihy.length);
    }

    private static InstructionHandle[] getInstructionBeforeLoop(
            InstructionHandle[] ihy, LoopFor loopFORitem, HashMap<Integer, Integer> hashmapPositionId, int start) {

        int getIdInArray = hashmapPositionId.get(
                loopFORitem.getFirstInstructionInFor().getPosition());
        return Arrays.copyOfRange(ihy, start, getIdInArray);
    }

    public static List<LoopFor> getListLoopForItems(InstructionHandle[] ihy, HashMap<Integer, Integer> hashmapPositionId) {

        List<LoopFor> allLoopsInMethod = new ArrayList<>();

        for (int i = 0; i < ihy.length; i++) {
            InstructionHandle item = ihy[i];
            if (item.getInstruction() instanceof GOTO) {
                LoopFor temp = new LoopFor();
                temp.setGotoHandler(ihy[i]);
                temp.setIncrementHandler(ihy[i - 1]);
                int idIterator = Integer.parseInt(ihy[i - 1].getInstruction().toString().split(" ")[1]);
                temp.setIdIterator(idIterator);
                allLoopsInMethod.add(temp);
            }
        }

        for (LoopFor item : allLoopsInMethod)
            forLoopSetFirstInForAndStartLoopHandles(item, ihy, hashmapPositionId);

        for (LoopFor item : allLoopsInMethod) forLoopItemSetIfAndFirstInLoopHandles(item, ihy, hashmapPositionId);
        forLoopValidateAssignedIfStatement(allLoopsInMethod);

        return allLoopsInMethod;
    }

    private static int howManyInstructionAfterLoop(
            InstructionHandle firstHandle, InstructionHandle endHandle, HashMap<Integer, Integer> hashmapPositionId) {

        int start = hashmapPositionId.get(firstHandle.getPosition()) + 1;
        int end = hashmapPositionId.get(endHandle.getPosition()) - 1;

        return end - start;
    }

    public static Instruction lvarUpdateIndexIfChanged(
            Instruction ih1, Instruction ih2, HashMap<Integer, Integer> hashmapIdOldAndNewLVar, MethodGen mg) {

//       TODO: MAYBE WITH USE OPCODE WE CAN SET TYPE? now i use only INT

        String iCurrName = ih1.getName();
        String iNextName = ih2.getName();

//      search array/object load, e.i.: ILOAD & AALOAD
        if (iCurrName.contains("load") && iNextName.contains("aload") && !iNextName.contains("_")) {
//      often occures: ILOAD_1,
//          1 is index local variable value, if index has old value then i replace to new value

            int id = Integer.parseInt(iCurrName.split("_")[1]);
            int idNameAccessToChangeWithCurrRow = Variables.getLVarIdByName(lvarNameToReplaceWithCurrRow, mg);
            if (id == idNameAccessToChangeWithCurrRow)
                ih1 = InstructionFactory.createLoad(Type.INT, currRowId);
            else
                ih1 = InstructionFactory.createLoad(Type.INT, hashmapIdOldAndNewLVar.get(id));
        } else if (iCurrName.contains("_")) {
            System.out.println(iCurrName);
            String temp = iCurrName.split("_")[1];
            if (temp.matches("\\d+")) {
                int idOld = Integer.parseInt(iCurrName.split("_")[1]);
                int idUpdated = hashmapIdOldAndNewLVar.getOrDefault(idOld, -1);
                if (idUpdated != -1) {
                    System.out.println("UPDATE INDEX: " + idOld + " -> " + idUpdated);
                    if (iCurrName.contains("istore")) ih1 = InstructionFactory.createStore(Type.INT, idUpdated);
                    else if (iCurrName.contains("iload")) ih1 = InstructionFactory.createLoad(Type.INT, idUpdated);
                }
            }
        }
        return ih1;
    }

    public static Instruction lvarUpdateIndexIfChanged(
            InstructionHandle ih1, HashMap<Integer, Integer> hashmapIdOldAndNewLVar, MethodGen mgOld, MethodGen mgNew) {

        InstructionHandle ih2 = ih1.getNext();
        Instruction temp = null;

        if (ih1.getInstruction().toString().contains("_")) {
//      search array/object load, e.i.: ILOAD & AALOAD
            if (ih1.getInstruction().toString().contains("load")) {
                int idOld = Integer.parseInt(ih1.getInstruction().getName().split("_")[1]);
                int idNew = hashmapIdOldAndNewLVar.getOrDefault(idOld, -1);
                if (idNew != -1) {
                    LocalVariable l = Variables.getLVarNameById(idOld, mgOld.getLocalVariableTable(mgOld.getConstantPool()));
                    System.out.println("czy znalazlem: " + l.getName() + ", " + l.getIndex() + "," + l.getSignature());


                    if (l.getName().equals("rowNum")) {
                        int idCurrRow = Variables.getLVarIdByName("currRow", mgNew);
                        temp = InstructionFactory.createLoad(Type.getType(l.getSignature()), idCurrRow);
                    } else {
                        temp = InstructionFactory.createLoad(Type.getType(l.getSignature()), idNew);
                    }

                }
            }
        }
//        iCurrName.contains("load") && iNextName.contains("aload") && !iNextName.contains("_")){
////      often occures: ILOAD_1,
////          1 is index local variable value, if index has old value then i replace to new value
//
//            int id = Integer.parseInt(iCurrName.split("_")[1]);
//            int idNameAccessToChangeWithCurrRow = LocalVariableUtils.getIdLVarByName(lvarNameToReplaceWithCurrRow, mg);
//            if (id == idNameAccessToChangeWithCurrRow)
//                ih1 = InstructionFactory.createLoad(Type.INT, currRowId);
//            else
//                ih1 = InstructionFactory.createLoad(Type.INT, hashmapIdOldAndNewLVar.get(id));
//        } else if (iCurrName.contains("_")) {
//            System.out.println(iCurrName);
//            String temp = iCurrName.split("_")[1];
//            if (temp.matches("\\d+")) {
//                int idOld = Integer.parseInt(iCurrName.split("_")[1]);
//                int idUpdated = hashmapIdOldAndNewLVar.getOrDefault(idOld, -1);
//                if (idUpdated != -1) {
//                    System.out.println("UPDATE INDEX: " + idOld + " -> " + idUpdated);
//                    if (iCurrName.contains("istore")) ih1 = InstructionFactory.createStore(Type.INT, idUpdated);
//                    else if (iCurrName.contains("iload")) ih1 = InstructionFactory.createLoad(Type.INT, idUpdated);
//                }
//            }
//        }
        return temp;
    }

    public static InstructionList parallelizeMethodWithNestedLoop(
            List<LoopFor> loopForItemsList, InstructionHandle[] ihy,
            HashMap<Integer, Integer> hashmapPositionId, ClassGen cg, MethodGen mg) {

        InstructionList il_new = new InstructionList();
        InstructionFactory factory = new InstructionFactory(cg, mg.getConstantPool());
        InstructionHandle[] ihBeforeLoop = null;
        InstructionHandle[] ihAfterLoop = null;
        List<InstructionHandle> handlesLoopStart = new ArrayList<>();
        List<BranchHandle> branchesGOTO = new ArrayList<>();
        HashMap<Integer, List<BranchHandle>> branchesIF = new HashMap<>();
        HashMap<Integer, List<InstructionHandle>> incrementOrNextHandle = new HashMap<>();//key - number of loop, keys: increment handles in looop

        int odTerazZosanaTylkoNPetli = 2;
        int allLoopForItems = loopForItemsList.size();

        LoopFor loopForItemTemp = loopForItemsList.get(0);

//      set the instructions in before of the loop, if this is true
        if (ihy[0].getInstruction() != loopForItemTemp.getFirstInstructionInFor().getInstruction()) {
            ihBeforeLoop = getInstructionBeforeLoop(ihy, loopForItemTemp, hashmapPositionId, 0);
            for (InstructionHandle instructionHandle : ihBeforeLoop)
                il_new.append(instructionHandle.getInstruction());
        }

//      set the instructions in after the loop, if this is true
        InstructionHandle afterLastGoto = loopForItemsList.get(loopForItemsList.size() - 1).getGotoHandler().getNext();
        if (!(afterLastGoto.getInstruction() instanceof RETURN) && !(afterLastGoto.getNext().getInstruction() instanceof RETURN)) {
            ihAfterLoop = getInstructionAfterLoop(ihy, loopForItemsList, hashmapPositionId);
        }

//        for (LoopFor item : loopForItemsList) item.displayInfoAboutHandles();

        int WSZYSKIE_PETLE = odTerazZosanaTylkoNPetli;

        for (int i = 0; i < WSZYSKIE_PETLE; i++) {
            System.out.println("******* FOR LOOP no: " + i + " *********");
            /**
             * POSITION REFER TO LINE IN CODE, BUT IT IS NOT THE SAME WHAT ID IN ARRAY OF INSTRUCTIONHANDLE
             * */
            int idFirst = hashmapPositionId.get(loopForItemsList.get(i).getFirstInstructionInFor().getPosition());
            int idLoopStart = hashmapPositionId.get(loopForItemsList.get(i).getLoopStartHandler().getPosition());
            int idIfComparator = hashmapPositionId.get(loopForItemsList.get(i).getIfcompareHandler().get(0).getPosition());
            int countIfInOneForLoop = loopForItemsList.get(i).getIfcompareHandler().size();

            /**
             * ADD INSTRUCTION, FROM RANGE: FROM  FIRST TO IF
             * I CREATE NEW, BECAUSE IF CODE WILL BE CHANGED THAT REFERENCE CAN BE INCORRECT
             * */

            for (int j = idFirst; j < idIfComparator; j++) {
                if (j == idLoopStart) {
                    InstructionHandle ih = il_new.append(ihy[j].getInstruction());
                    handlesLoopStart.add(ih);
                } else il_new.append(ihy[j].getInstruction());
            }

//          ADD IF COMPARATOR
            int lastIdIfInFor = 0;
            List<BranchHandle> tempIfBranchHandle = new ArrayList<>();
            for (int j = idIfComparator; j < (idIfComparator + countIfInOneForLoop); j++) {
                BranchHandle if_compare = null;
                if (ihy[j].getInstruction() instanceof IF_ICMPGE) if_compare = il_new.append(new IF_ICMPGE(null));
                if (ihy[j].getInstruction() instanceof IF_ICMPGT) if_compare = il_new.append(new IF_ICMPGT(null));
                if (ihy[j].getInstruction() instanceof IF_ICMPLE) if_compare = il_new.append(new IF_ICMPLE(null));
                if (ihy[j].getInstruction() instanceof IF_ICMPLT) if_compare = il_new.append(new IF_ICMPLT(null));
                if (ihy[j].getInstruction() instanceof IF_ICMPEQ) if_compare = il_new.append(new IF_ICMPEQ(null));
                if (ihy[j].getInstruction() instanceof IF_ICMPNE) if_compare = il_new.append(new IF_ICMPNE(null));
                if (ihy[j].getInstruction() instanceof IF_ACMPEQ) if_compare = il_new.append(new IF_ACMPEQ(null));
                if (ihy[j].getInstruction() instanceof IF_ACMPNE) if_compare = il_new.append(new IF_ACMPNE(null));
                if (ihy[j].getInstruction() instanceof IFLE) if_compare = il_new.append(new IFLE(null));
                if (ihy[j].getInstruction() instanceof IFLT) if_compare = il_new.append(new IFLT(null));
                if (ihy[j].getInstruction() instanceof IFGE) if_compare = il_new.append(new IFGE(null));
                if (ihy[j].getInstruction() instanceof IFGT) if_compare = il_new.append(new IFGT(null));
                if (ihy[j].getInstruction() instanceof IFEQ) if_compare = il_new.append(new IFEQ(null));
                if (ihy[j].getInstruction() instanceof IFNE) if_compare = il_new.append(new IFNE(null));
                tempIfBranchHandle.add(if_compare);
                lastIdIfInFor = j;
            }

            branchesIF.put(i, tempIfBranchHandle);


            /**
             * GROUP OF INSTUCTION THAT OCCURES IN FRONT OF INTERNAL LOOP WILL BE ADDED
             * */
            if (i < loopForItemsList.size() - 1) {
                int idLastIfCompareFromPrevForLoop = lastIdIfInFor + 1;
                int idFirstInstructionInNextForLoop = hashmapPositionId.get(loopForItemsList.get(i + 1).getFirstInstructionInFor().getPosition());
                int numberIhBeforeLoop = idFirstInstructionInNextForLoop - idLastIfCompareFromPrevForLoop;

                System.out.println("number of instruction before loop: " + numberIhBeforeLoop);

                if (numberIhBeforeLoop > 0) {
                    for (int j = idLastIfCompareFromPrevForLoop; j < idFirstInstructionInNextForLoop; j++) {
                        System.out.println("petla: " + i + ") " + "[" + j + "], " + ihy[j].toString());
                        il_new.append(ihy[j].getInstruction());
                    }
                }
            }
        }


//      >>>>>>>>>>>>>>>>>>>>>>      NEW INSTRUCTION INSIDE INNER LOOP BODY START HERE   >>>>>>>>>>>>>>>>>>>>>>

        il_new.append(factory.createPrintln("IT WORKS"));
        ReadyFieldsMethods.changeBodyInnerLoopForMatrixMultiply(mg, il_new, factory);

//      <<<<<<<<<<<<<<<<<<<<<<      NEW INSTRUCTION INSIDE INNER LOOP BODY END HERE     <<<<<<<<<<<<<<<<<<<<<<


//        CLOSE INTERNAL LOOP
        int positionIncrement = loopForItemsList.get(WSZYSKIE_PETLE - 1).getIncrementHandler().getPosition();
        int idIncrement = hashmapPositionId.get(positionIncrement);
        InstructionHandle ih = il_new.append(ihy[idIncrement].getInstruction());
        addIncrementOrNextHandleToHashmap(incrementOrNextHandle, WSZYSKIE_PETLE - 1, ih);//2 is index (last)inner loop in case with number of loops = 3
        BranchHandle gotoHandler = il_new.append(new GOTO(handlesLoopStart.get(WSZYSKIE_PETLE - 1)));
        branchesGOTO.add(gotoHandler);


//zakladam ze petla najbarzdziej wewnetrzna zostala uzupelniona wyzej :))))))
//        z tego wynika ze indeks od ktorego zaczynam jest przesuniety - zmniejszony o 1 czyli wychodze z tej
//zazcynam od przedosatniej
        for (int i = WSZYSKIE_PETLE - 1 - 1; i >= 0; i--) {

            /**
             * GROUP OF INSTRUCTION THAT OCCURES IN END OF INTERNAL LOOP WILL BE ADDED
             * */
            positionIncrement = loopForItemsList.get(i).getIncrementHandler().getPosition();
            idIncrement = hashmapPositionId.get(positionIncrement);

            //            tutaj nie szukam tego co jest za petla o indeksie 0
            int start = hashmapPositionId.get(loopForItemsList.get(i + 1).getGotoHandler().getPosition()) + 1;
            int end = hashmapPositionId.get(loopForItemsList.get(i).getIncrementHandler().getPosition());
            int numberIhAfterLoop = end - start;

            System.out.println("number of instruction after loop: " + numberIhAfterLoop);

            if (numberIhAfterLoop > 0) {
                ih = il_new.append(ihy[start].getInstruction());
                addIncrementOrNextHandleToHashmap(incrementOrNextHandle, i, ih);
                int k = 1;
                System.out.println("petla: " + i + ") instrukcja nr: " + k++);
                if (numberIhAfterLoop > 1) {
                    for (int j = start + 1; j < end; j++) {
                        System.out.println("petla: " + i + ") instrukcja nr: " + k++ + ", " + ihy[j].toString());
                        il_new.append(ihy[j].getInstruction());
                    }
                }
            }
            ih = il_new.append(ihy[end].getInstruction());
            addIncrementOrNextHandleToHashmap(incrementOrNextHandle, i, ih);
            gotoHandler = il_new.append(new GOTO(handlesLoopStart.get(i)));
            branchesGOTO.add(gotoHandler);
        }

        /**
         * SETTARGET in if_branch_handle
         * first is set to "returnHandle", first instruction after loops
         * if there are other instructions behind the loop,
         * the handler does not indicate an increment of iterator of the loop above
         * but the first such instruction after this loop,
         * in other case indicate to increment of external iterator*/

        int positionNextInstr = loopForItemsList.get(0).getGotoHandler().getNext().getPosition();
        int idNextInstr = hashmapPositionId.get(positionNextInstr);


        InstructionHandle returnHandler = il_new.append(ihy[idNextInstr].getInstruction());

        System.out.println("ALL increments/next handles");
        displayIncrementOrNextHandles(incrementOrNextHandle);

        for (int i = 0; i < WSZYSKIE_PETLE; i++) {
            for (int j = 0; j < branchesIF.get(i).size(); j++) {
                if (i == 0) {
                    System.out.printf("i=%d, j=%d, with return handle: %s\n", i, j, returnHandler);
                    branchesIF.get(0).get(i).setTarget(returnHandler);
                } else {
                    System.out.printf("i=%d, j=%d, instr increment: %s\n", i, j, incrementOrNextHandle.get(i - 1).get(0));
                    branchesIF.get(i).get(j).setTarget(incrementOrNextHandle.get(i - 1).get(0));
                }
            }
        }

/*    NOTE: THIS 1 instruction of TRY CATCH BLOCK must be move UP and use as a return handle

      InstructionHandle returnHandle = il_new.append(factory.createFieldAccess(
                cg.getClassName(),
                "SERVICE",
                new ObjectType("java.util.concurrent.ExecutorService"),
                Const.GETSTATIC));*/

        il_new.append(InstructionFactory.createLoad(Type.OBJECT, 55));
        il_new.append(factory.createInvoke("java.util.concurrent.ExecutorService", "invokeAll", new ObjectType("java.util.List"), new Type[]{new ObjectType("java.util.Collection")}, Const.INVOKEINTERFACE));
        il_new.append(InstructionConst.POP);

        il_new.append(factory.createFieldAccess(
                cg.getClassName(),
                "SERVICE",
                new ObjectType("java.util.concurrent.ExecutorService"),
                Const.GETSTATIC));

        InstructionHandle endTry = il_new.append(factory.createInvoke(
                "java.util.concurrent.ExecutorService",
                "shutdown",
                Type.VOID, Type.NO_ARGS,
                Const.INVOKEINTERFACE));

        BranchInstruction gotoNext = InstructionFactory.createBranchInstruction(Const.GOTO, null);
        il_new.append(gotoNext);

        InstructionHandle startCatch = il_new.append(InstructionFactory.createStore(Type.OBJECT, mg.getMaxLocals()));

        il_new.append(InstructionFactory.createLoad(Type.OBJECT, mg.getMaxLocals()));
        il_new.append(factory.createInvoke(
                "java.lang.Exception",
                "printStackTrace",
                Type.VOID, Type.NO_ARGS,
                Const.INVOKEVIRTUAL));

        InstructionHandle returnId = il_new.append(ihy[idNextInstr].getInstruction());
        gotoNext.setTarget(returnId);
        mg.addExceptionHandler(returnHandler, endTry, startCatch, new ObjectType("java.lang.Exception"));


        /**
         * the condition checks if the number of instructions following the loop is greater than 1,
         * because if it is only 1, then we known it is RETURN
         * and in the effect GOTO of the first loop already points to it
         */

        if (ihAfterLoop != null && ihAfterLoop.length > 1) {
            for (int i = 1; i < ihAfterLoop.length; i++)
                il_new.append(ihAfterLoop[i].getInstruction());
        }

        return il_new;
    }

    //    TODO: REPAIR IT BECAUSE IS STATIC
    private static Instruction updateOldIndexInLVar(
            Instruction instr,
            HashMap<Integer, Integer> hashmapIdOldAndNewLVar,
            InstructionFactory factory,
            MethodGen mgOLD,
            MethodGen mgNEW) {
        Instruction temp = null;

        int indeksDoZamianyZcurrRow = 0;//rowNum -> currRow (z czego to wynika, czy np z tego ze w pierwszej petli jest indeksem?)


        if (instr.toString().contains("_")) {
            System.out.println(instr.getName());
            int idOLD = 0, idNEW = 0;
            String[] string;

            if (instr.toString().contains("_")) {
                string = instr.getName().split("_");
                idOLD = Integer.parseInt(string[1]);
            }

            if (idOLD == 1)//zamiana na currROw
            {
//                idNEW = 0;
                idNEW = 3;
            }

            if (idOLD == 2) {
                idNEW = 1;
            }
//
            if (idOLD == 4) {
                idNEW = 5;
            }

            if (idOLD == 0) {
                idNEW = 4;
            }

            if (idOLD == 3) {
                idNEW = 5;
            }

            if (idNEW != -777) {
                LocalVariable lv = Variables.getLVarNameById(idNEW, mgNEW.getLocalVariableTable(mgNEW.getConstantPool()));
                if (instr.toString().contains("load"))
                    temp = InstructionFactory.createLoad(Type.getType(lv.getSignature()), idNEW);
                if (instr.toString().contains("store"))
                    temp = InstructionFactory.createStore(Type.getType(lv.getSignature()), idNEW);

            }
        }

        if (temp != null)
            return temp;
        else
            return instr;
    }
}
