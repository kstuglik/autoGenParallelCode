package pl.edu.agh.bcel.utils;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.generic.*;
import pl.edu.agh.bcel.LaunchProperties;

import java.util.*;

/*
    The case handled refers to nested loops - one series i.e:
              code... nested loops... code
              Nested loops... code...
              code... nested loops

    does not yet support a type case:
              code... nested loops... code... nested loops...
*/

public class ForLoopUtils {

    static int CURR_ROW_ID = 0;

    public static void addHandleIncOrNext(HashMap<Integer, List<InstructionHandle>> hm, int key, InstructionHandle value) {
        if (hm.containsKey(key)) hm.get(key).add(value);
        else {
            List<InstructionHandle> list = new ArrayList<>();
            list.add(value);
            hm.put(key, list);
        }
    }

//    subtask moze i chyba nawet powinien byc wywolany po tym jak sie zrobi metode zrownoleglana
//    tzn. z tamtego miejsca trzeba przekazac info typu: lista pętli, wskazanie petli ktore beda uzyte w subtask
//    wtedy nie jeden element listy ale co najmniej 2

    public static void addSubTaskInNestedLoop(ClassGen cg, MethodGen mgOld, int externalLoop, int internalLoop) {

        InstructionList ilOld = mgOld.getInstructionList();
        InstructionHandle[] ihy = ilOld.getInstructionHandles();

        InstructionList ilNew = new InstructionList();
        ConstantPoolGen cp = cg.getConstantPool();
        InstructionFactory factory = new InstructionFactory(cg, cp);

        HashMap<Integer, List<BranchHandle>> hashMapBHandleIF = new HashMap<>();
        HashMap<Integer, List<InstructionHandle>> hashMapHandleIncOrNext = new HashMap<>();
        HashMap<Integer, Integer> hashmapPositionId = ForLoopItem.getHashmapPositionId(ihy);
        List<ForLoopItem> forLoopItemsList = getListLoopForItems(ihy, hashmapPositionId);
        List<InstructionHandle> handlesLoopStart = new ArrayList<>();
        List<BranchHandle> listBHandleGOTO = new ArrayList<>();
        List<BranchHandle> listBHandleIF = new ArrayList<>();

        //key - number of loop, keys: increment handles in loop

        MethodGen mgNew = new MethodGen(Const.ACC_PRIVATE | Const.ACC_STATIC, Type.INT, new Type[]{Type.INT, Type.INT, Type.INT}, new String[]{LaunchProperties.ROW_NUM_VAR_NAME, LaunchProperties.COL_NUM_VAR_NAME, LaunchProperties.STEP_VAR_NAME}, LaunchProperties.SUBTASK_METHOD_NAME, cg.getClassName(), ilNew, mgOld.getConstantPool());

        CURR_ROW_ID = mgNew.addLocalVariable(LaunchProperties.CURR_ROW_VAR_NAME, Type.INT, null, null).getIndex();

//        REPLACEMENT ID: rowNUM (with id from old MethodGen) with currRow (with id in new MethodGen)
        int idOldLVarToReplace = VariableUtils.getLVarIdByName("rowNum", mgOld);
        System.out.println("id variable to `change place` with currRow: " + idOldLVarToReplace);

        HashMap<Integer, Integer> hashmapIdOldAndNewLVar = VariableUtils.getHashmapLVarIndexesOldAndNew(mgOld, mgNew);
        replaceSpecificIndexes(idOldLVarToReplace, CURR_ROW_ID, hashmapIdOldAndNewLVar);

//      STATIC PART OF CODE
//      for(int currRow = rowNum;  currRow < rowNum + step && currRow < resultRows;  ++currRow)
//        @PARAMS in METHOD have indexes: 0 - first, 1 - second, 2 - third
//        first parameter is assigned as value to iterator
        ilNew.append(InstructionFactory.createLoad(Type.INT, 0));
        ilNew.append(InstructionFactory.createStore(Type.INT, CURR_ROW_ID));

        int aaaa = hashmapPositionId.get(forLoopItemsList.get(0).getHandleInsideLoop().getPosition());
        int bbbb = hashmapPositionId.get(forLoopItemsList.get(0).getHandleIF().get(0).getPosition());
        int ile = bbbb - aaaa;

        InstructionHandle startExternalLoopHandle = ilNew.append(
                updateLVarIndexes(ihy[aaaa].getInstruction(), hashmapIdOldAndNewLVar, mgNew));

        System.out.println("ILE WYNOSI: " + ile);

        //          ADD IF COMPARATOR
        int lastIdIfInFor = 0;
//        List<BranchHandle> tempIfBranchHandle = new ArrayList<>();
//        for (int j = aaaa; j < bbbb; j++) {
//            BranchHandle if_compare = getBranchHandleIF(il_new, ihy[j]);
//            tempIfBranchHandle.add(if_compare);
//            lastIdIfInFor = j;
//        }
//
//        branchesIF.put(i, tempIfBranchHandle);
//        InstructionHandle startExternalLoopHandle = ilNew.append(InstructionFactory.createLoad(Type.INT, CURR_ROW_ID));
        ilNew.append(InstructionFactory.createLoad(Type.INT, 0));
        ilNew.append(InstructionFactory.createLoad(Type.INT, 2));
        ilNew.append(InstructionConst.IADD);
        BranchHandle if_icmpge_1 = ilNew.append(new IF_ICMPGE(null));

        ilNew.append(InstructionFactory.createLoad(Type.INT, CURR_ROW_ID));
        ilNew.append(factory.createFieldAccess(cg.getClassName(), "resultRows", Type.INT, Const.GETSTATIC));
        BranchHandle if_icmpge_2 = ilNew.append(new IF_ICMPGE(null));

//        ******************** DYNAMIC PART OF INNER "FOR LOOP" ********************* START

        int idFirst = hashmapPositionId.get(forLoopItemsList.get(2).getHandleFirstInFor().getPosition());
        int idLoopStart = hashmapPositionId.get(forLoopItemsList.get(2).getHandleStartLoop().getPosition());
        int idIfComparator = hashmapPositionId.get(forLoopItemsList.get(2).getHandleIF().get(0).getPosition());
        int countIfInOneForLoop = forLoopItemsList.get(2).getHandleIF().size();
        int nextInstructionAfterIfStatement = idIfComparator + countIfInOneForLoop;

//        ADD INSTRUCTION BETWEEN: FIRST ADN IF
        for (int j = idFirst; j < idIfComparator; j++) {
            if (j == idLoopStart) {
                InstructionHandle ih = ilNew.append(updateLVarIndexes(ihy[j].getInstruction(), hashmapIdOldAndNewLVar, mgNew));
                handlesLoopStart.add(ih);
            } else ilNew.append(updateLVarIndexes(ihy[j].getInstruction(), hashmapIdOldAndNewLVar, mgNew));
        }

//          ADD IF COMPARATOR
        for (int j = idIfComparator; j < (idIfComparator + countIfInOneForLoop); j++) {
            BranchHandle if_compare = getBranchHandleIF(ilNew, ihy[j]);
            listBHandleIF.add(if_compare);
        }

//        put item on index: 0 because is only one, first loop (in the feature it can be change)
        hashMapBHandleIF.put(1, listBHandleIF);

        int positionIncrement = forLoopItemsList.get(2).getHandleINC().getPosition();
        int idIncrement = hashmapPositionId.get(positionIncrement);

//      >>>>>>>>>>>>>>>>>>>>>>      NEW INSTRUCTION INSIDE INNER LOOP BODY START HERE   >>>>>>>>>>>>>>>>>>>>>>
        ilNew.append(factory.createPrintln("INSIDE LOOP :)"));
        for (int i = nextInstructionAfterIfStatement; i < idIncrement; i++) {
            ilNew.append(updateLVarIndexes(ihy[i].getInstruction(), hashmapIdOldAndNewLVar, mgNew));
        }
//      <<<<<<<<<<<<<<<<<<<<<<      NEW INSTRUCTION INSIDE INNER LOOP BODY END HERE   <<<<<<<<<<<<<<<<<<<<<<


//        CLOSE INTERNAL LOOP
        String[] string = ihy[idIncrement].getInstruction().toString().split(" ");
        System.out.println(Arrays.toString(string));
        int idOLD = Integer.parseInt(string[string.length - 2]);
        int idNEW = hashmapIdOldAndNewLVar.getOrDefault(idOLD, -1);
        if (idNEW != -1) {
            int incrementValue = Integer.parseInt(string[string.length - 1]);
            InstructionHandle ih = ilNew.append(new IINC(idNEW, incrementValue));
            addHandleIncOrNext(hashMapHandleIncOrNext, 0, ih);
        } else {
            InstructionHandle ih = ilNew.append(ihy[idIncrement].getInstruction());
            addHandleIncOrNext(hashMapHandleIncOrNext, 0, ih);
        }

//        il.append(ihy[idIncrement].getInstruction());

        BranchHandle gotoHandler = ilNew.append(new GOTO(handlesLoopStart.get(0)));
        listBHandleGOTO.add(gotoHandler);

//        ******************** STATIC PART OF "FOR LOOP" ********************* START
        InstructionHandle incFirstLoop = ilNew.append(new IINC(CURR_ROW_ID, 1));
        BranchInstruction goto_26 = InstructionFactory.createBranchInstruction(Const.GOTO, startExternalLoopHandle);
        ilNew.append(goto_26);
        InstructionHandle returnHandler = ilNew.append(new PUSH(cp, 0));
        ilNew.append(InstructionFactory.createReturn(Type.INT));
        if_icmpge_1.setTarget(returnHandler);
        if_icmpge_2.setTarget(returnHandler);

//        ******************** DYNAMIC PART OF "FOR LOOP" ********************* START
//           hashMapBHandleIF element consists of (indexOfLoopFor,List<BranchHandle>)

        for (Integer numberOfLoopFor : hashMapBHandleIF.keySet()) {
            for (int i = 0; i < hashMapBHandleIF.get(1).size(); i++) {
                hashMapBHandleIF.get(numberOfLoopFor).get(i).setTarget(incFirstLoop);
            }
        }
//            if there are more internal loops then you have to modify,
//            because here the case for the penultimate loop has been handled,
//            then it actually points to firstIncrement
//        ******************** DYNAMIC PART OF "FOR LOOP" ********************* END

        mgNew.setInstructionList(ilNew);
        mgNew.setMaxLocals();
        mgNew.setMaxStack();
        cg.addMethod(mgNew.getMethod());
        cg.getConstantPool().addMethodref(mgNew);
        ilNew.dispose();
    }

    public static void displayIncrementOrNextHandles(HashMap<Integer, List<InstructionHandle>> incrementOrNextHandle) {
        for (Integer key : incrementOrNextHandle.keySet()) {
            for (int i = 0; i < incrementOrNextHandle.get(key).size(); i++) {
                System.out.println("key = " + key + ", i = " + i + ", " + incrementOrNextHandle.get(key).get(i));
            }
        }
    }

    public static void displayIndexesToUpdateFromHashMap(MethodGen mgNew, HashMap<Integer, Integer> hashmapIdOldAndNewLVar) {
        for (Integer idPrev : hashmapIdOldAndNewLVar.keySet()) {
            int idNow = hashmapIdOldAndNewLVar.get(idPrev);
            String nameLVar = VariableUtils.getLVarNameById(idNow, mgNew);
            System.out.printf("%s\t[old: %d, new:%d]\n", nameLVar, idPrev, idNow);
        }
    }

    private static void forLoopItemSetIfAndFirstInLoopHandles(
            ForLoopItem item, InstructionHandle[] ihy, HashMap<Integer, Integer> hashmapPositionId) {

        int positionStart = item.getHandleStartLoop().getPosition();
        int idStart = hashmapPositionId.get(positionStart);

        int positionEnd = item.getHandleGOTO().getPosition();
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
                item.addHandleIF(ihy[i]);
                item.setHandleInsideLoop(ihy[i + 1]);
            }
        }
    }

    private static void forLoopSetFirstInForAndStartLoopHandles(
            ForLoopItem item, InstructionHandle[] ihy, HashMap<Integer, Integer> hashmapPositionId) {
        int position = Integer.parseInt(item.getHandleGOTO().toString().split("->")[1].replace(" ", ""));
        int idStartLoopArray = getIdByPositionInCode(hashmapPositionId, position);
        item.setHandleStartLoop(ihy[idStartLoopArray]);
//        ussualy: iload and istore are 2 instructions before idStartLoopArray
        item.setHandleFirstInFor(ihy[idStartLoopArray].getPrev().getPrev());
    }

    private static void forLoopValidateAssignedIfStatement(List<ForLoopItem> forLoopItemsList) {
        Collections.reverse(forLoopItemsList);
        for (int i = 0; i < forLoopItemsList.size() - 1; i++) {// -1 because current "item" and next "item" are compared
            ArrayList<InstructionHandle> externalLoop = forLoopItemsList.get(i).getHandleIF();
            ArrayList<InstructionHandle> innnerLoop = forLoopItemsList.get(i + 1).getHandleIF();
            externalLoop.removeAll(innnerLoop);
        }
    }

    public static BranchHandle getBranchHandleIF(InstructionList il, InstructionHandle instructionHandle) {
        BranchHandle if_compare = null;
        Instruction item = instructionHandle.getInstruction();
        if (item instanceof IF_ICMPGE) if_compare = il.append(new IF_ICMPGE(null));
        if (item instanceof IF_ICMPGT) if_compare = il.append(new IF_ICMPGT(null));
        if (item instanceof IF_ICMPLE) if_compare = il.append(new IF_ICMPLE(null));
        if (item instanceof IF_ICMPLT) if_compare = il.append(new IF_ICMPLT(null));
        if (item instanceof IF_ICMPEQ) if_compare = il.append(new IF_ICMPEQ(null));
        if (item instanceof IF_ICMPNE) if_compare = il.append(new IF_ICMPNE(null));
        if (item instanceof IF_ACMPEQ) if_compare = il.append(new IF_ACMPEQ(null));
        if (item instanceof IF_ACMPNE) if_compare = il.append(new IF_ACMPNE(null));
        if (item instanceof IFLE) if_compare = il.append(new IFLE(null));
        if (item instanceof IFLT) if_compare = il.append(new IFLT(null));
        if (item instanceof IFGE) if_compare = il.append(new IFGE(null));
        if (item instanceof IFGT) if_compare = il.append(new IFGT(null));
        if (item instanceof IFEQ) if_compare = il.append(new IFEQ(null));
        if (item instanceof IFNE) if_compare = il.append(new IFNE(null));
        return if_compare;
    }

    public static int getIdByPositionInCode(HashMap<Integer, Integer> hashmapPositionId, int position) {
        return hashmapPositionId.get(position);
    }

    private static InstructionHandle[] getInstructionAfterLoop(
            InstructionHandle[] ihy, List<ForLoopItem> forLoopItems,
            HashMap<Integer, Integer> hashmapPositionId) {
        int getIdInArray = hashmapPositionId.get(forLoopItems.get(0).getHandleGOTO().getPosition()) + 1;
        return Arrays.copyOfRange(ihy, getIdInArray, ihy.length);
    }

    private static InstructionHandle[] getInstructionBeforeLoop(
            InstructionHandle[] ihy, ForLoopItem loopFORitem,
            HashMap<Integer, Integer> hashmapPositionId, int start) {

        int getIdInArray = hashmapPositionId.get(loopFORitem.getHandleFirstInFor().getPosition());
        return Arrays.copyOfRange(ihy, start, getIdInArray);
    }

    public static List<ForLoopItem> getListLoopForItems(InstructionHandle[] ihy, HashMap<Integer, Integer> hashmapPositionId) {

        List<ForLoopItem> allLoopsInMethod = new ArrayList<>();

        for (int i = 0; i < ihy.length; i++) {
            InstructionHandle item = ihy[i];
            if (item.getInstruction() instanceof GOTO) {
                ForLoopItem temp = new ForLoopItem();
                temp.setHandleGOTO(ihy[i]);
                temp.setHandleINC(ihy[i - 1]);
                int idIterator = Integer.parseInt(ihy[i - 1].getInstruction().toString().split(" ")[1]);
                temp.setIdIterator(idIterator);
                allLoopsInMethod.add(temp);
            }
        }

        for (ForLoopItem item : allLoopsInMethod) forLoopSetFirstInForAndStartLoopHandles(item, ihy, hashmapPositionId);
        for (ForLoopItem item : allLoopsInMethod) forLoopItemSetIfAndFirstInLoopHandles(item, ihy, hashmapPositionId);
        forLoopValidateAssignedIfStatement(allLoopsInMethod);

        return allLoopsInMethod;
    }

    public static InstructionList parallelizeMethodNestedLoop(ClassGen cg, MethodGen mgOld) {

        InstructionList ilOld = mgOld.getInstructionList();
        InstructionHandle[] ihy = ilOld.getInstructionHandles();
        HashMap<Integer, Integer> hashmapPositionId = ForLoopItem.getHashmapPositionId(ihy);
        List<ForLoopItem> forLoopItemsList = getListLoopForItems(ihy, hashmapPositionId);

        InstructionList il_new = new InstructionList();
        InstructionFactory factory = new InstructionFactory(cg, mgOld.getConstantPool());
        InstructionHandle[] ihBeforeLoop = null;
        InstructionHandle[] ihAfterLoop = null;
        List<InstructionHandle> handlesLoopStart = new ArrayList<>();
        List<BranchHandle> branchesGOTO = new ArrayList<>();
        HashMap<Integer, List<BranchHandle>> branchesIF = new HashMap<>();
        HashMap<Integer, List<InstructionHandle>> incrementOrNextHandle = new HashMap<>();//key - number of loop, keys: increment handles in looop

        int selectedNumberOfLoops = 2;
        int numberOfLoops = forLoopItemsList.size();

        ForLoopItem forLoopItemTemp = forLoopItemsList.get(0);

//        for (LoopFor item : loopForItemsList) item.displayInfoAboutHandles();

        for (int i = 0; i < selectedNumberOfLoops; i++) {
            System.out.println("******* FOR LOOP no: " + i + " *********");

            int idFirst = hashmapPositionId.get(forLoopItemsList.get(i).getHandleFirstInFor().getPosition());
            int idLoopStart = hashmapPositionId.get(forLoopItemsList.get(i).getHandleStartLoop().getPosition());
            int idIfComparator = hashmapPositionId.get(forLoopItemsList.get(i).getHandleIF().get(0).getPosition());
            int countIfInOneForLoop = forLoopItemsList.get(i).getHandleIF().size();

            //      set the instructions in BEFORE of the loop_0, if this is true

            if (i == 0 && ihy[0].getInstruction() != forLoopItemsList.get(i).getHandleFirstInFor().getInstruction()) {
                ihBeforeLoop = getInstructionBeforeLoop(ihy, forLoopItemTemp, hashmapPositionId, 0);
                for (InstructionHandle instructionHandle : ihBeforeLoop)
                    il_new.append(instructionHandle.getInstruction());
            }

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
                BranchHandle if_compare = getBranchHandleIF(il_new, ihy[j]);
                tempIfBranchHandle.add(if_compare);
                lastIdIfInFor = j;
            }

            branchesIF.put(i, tempIfBranchHandle);

            /**
             * GROUP OF INSTUCTION THAT OCCURES IN FRONT OF INTERNAL LOOP WILL BE ADDED
             * */

            if (i < forLoopItemsList.size() - 1) {
                int idLastIfCompareFromPrevForLoop = lastIdIfInFor + 1;
                int idFirstInstructionInNextForLoop = hashmapPositionId.get(forLoopItemsList.get(i + 1).getHandleFirstInFor().getPosition());
                int numberIhBeforeLoop = idFirstInstructionInNextForLoop - idLastIfCompareFromPrevForLoop;

                System.out.println("number of instruction before loop: " + numberIhBeforeLoop);

                if (numberIhBeforeLoop > 0) {
                    for (int j = idLastIfCompareFromPrevForLoop; j < idFirstInstructionInNextForLoop; j++) {
                        System.out.println("loop: " + i + ") " + "[" + j + "], " + ihy[j].toString());
                        il_new.append(ihy[j].getInstruction());
                    }
                }
            }
        }


//      >>>>>>>>>>>>>>>>>>>>>>      NEW INSTRUCTION INSIDE INNER LOOP BODY START HERE   >>>>>>>>>>>>>>>>>>>>>>

        il_new.append(factory.createPrintln("IT WORKS"));
        ReadyFields.changeBodyInnerLoopForMatrixMultiply(mgOld, il_new, factory);

//      <<<<<<<<<<<<<<<<<<<<<<      NEW INSTRUCTION INSIDE INNER LOOP BODY END HERE     <<<<<<<<<<<<<<<<<<<<<<


//        CLOSE INTERNAL LOOP
        int positionIncrement = forLoopItemsList.get(selectedNumberOfLoops - 1).getHandleINC().getPosition();
        int idIncrement = hashmapPositionId.get(positionIncrement);
        InstructionHandle ih = il_new.append(ihy[idIncrement].getInstruction());
        addHandleIncOrNext(incrementOrNextHandle, selectedNumberOfLoops - 1, ih);//2 is index (last)inner loop in case with number of loops = 3
        BranchHandle gotoHandler = il_new.append(new GOTO(handlesLoopStart.get(selectedNumberOfLoops - 1)));
        branchesGOTO.add(gotoHandler);


//       z tego wynika ze indeks od ktorego zaczynam jest przesuniety - zmniejszony o 1 czyli wychodze z tej
//          zazcynam od przed ostatniej
        for (int i = selectedNumberOfLoops - 1 - 1; i >= 0; i--) {

            /**
             * GROUP OF INSTRUCTION THAT OCCURES IN END OF INTERNAL LOOP WILL BE ADDED
             * */

            int start = hashmapPositionId.get(forLoopItemsList.get(i + 1).getHandleGOTO().getPosition()) + 1;
            int end = hashmapPositionId.get(forLoopItemsList.get(i).getHandleINC().getPosition());
            int numberIhAfterLoop = end - start;

            System.out.println("number of instruction after loop: " + numberIhAfterLoop);

            if (numberIhAfterLoop > 0) {
                ih = il_new.append(ihy[start].getInstruction());
                addHandleIncOrNext(incrementOrNextHandle, i, ih);
                int k = 1;
                System.out.println("loop: " + i + ") instrukcja nr: " + k++);
                if (numberIhAfterLoop > 1) {
                    for (int j = start + 1; j < end; j++) {
                        System.out.println("loop: " + i + ") instrukcja nr: " + k++ + ", " + ihy[j].toString());
                        il_new.append(ihy[j].getInstruction());
                    }
                }
            }
            ih = il_new.append(ihy[end].getInstruction());
            addHandleIncOrNext(incrementOrNextHandle, i, ih);
            gotoHandler = il_new.append(new GOTO(handlesLoopStart.get(i)));
            branchesGOTO.add(gotoHandler);
        }

        /**
         * SET TARGET in if_branch_handle
         * first is set to "returnHandle", first instruction after loops
         * if there are other instructions behind the loop,
         * the handler does not indicate an increment of iterator of the loop above
         * but the first such instruction after this loop,
         * in other case indicate to increment of external iterator*/

        int positionNextInstr = forLoopItemsList.get(0).getHandleGOTO().getNext().getPosition();
        int idNextInstr = hashmapPositionId.get(positionNextInstr);

        InstructionHandle returnHandler = il_new.append(factory.createFieldAccess(
                cg.getClassName(), "SERVICE",
                new ObjectType("java.util.concurrent.ExecutorService"),
                Const.GETSTATIC));

        System.out.println("ALL increments/next handles");
        displayIncrementOrNextHandles(incrementOrNextHandle);

        for (int i = 0; i < selectedNumberOfLoops; i++) {
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

        /*    NOTE: THIS 1 instruction of TRY CATCH BLOCK must be move UP and use as a return handle*/

//        InstructionHandle returnHandle = il_new.append(factory.createFieldAccess(
//                cg.getClassName(),
//                "SERVICE",
//                new ObjectType("java.util.concurrent.ExecutorService"),
//                Const.GETSTATIC));
        il_new.append(InstructionFactory.createLoad(Type.OBJECT, 55));
        il_new.append(factory.createInvoke("java.util.concurrent.ExecutorService", "invokeAll", new ObjectType("java.util.List"), new Type[]{new ObjectType("java.util.Collection")}, Const.INVOKEINTERFACE));
        il_new.append(InstructionConst.POP);

        il_new.append(factory.createFieldAccess(
                cg.getClassName(), "SERVICE", new ObjectType("java.util.concurrent.ExecutorService"), Const.GETSTATIC));

        InstructionHandle endTry = il_new.append(factory.createInvoke(
                "java.util.concurrent.ExecutorService", "shutdown", Type.VOID, Type.NO_ARGS, Const.INVOKEINTERFACE));

        BranchInstruction gotoNext = InstructionFactory.createBranchInstruction(Const.GOTO, null);
        il_new.append(gotoNext);

        InstructionHandle startCatch = il_new.append(InstructionFactory.createStore(Type.OBJECT, mgOld.getMaxLocals()));

        il_new.append(InstructionFactory.createLoad(Type.OBJECT, mgOld.getMaxLocals()));
        il_new.append(factory.createInvoke(
                "java.lang.Exception", "printStackTrace", Type.VOID, Type.NO_ARGS, Const.INVOKEVIRTUAL));

        InstructionHandle returnId = il_new.append(ihy[idNextInstr].getInstruction());
        gotoNext.setTarget(returnId);
        mgOld.addExceptionHandler(returnHandler, endTry, startCatch, new ObjectType("java.lang.Exception"));


        //      set the instructions in AFTER the loop_0, if this is true
        InstructionHandle afterLastGoto = forLoopItemsList.get(forLoopItemsList.size() - 1).getHandleGOTO().getNext();
        if (!(afterLastGoto.getInstruction() instanceof RETURN) && !(afterLastGoto.getNext().getInstruction() instanceof RETURN)) {
            ihAfterLoop = getInstructionAfterLoop(ihy, forLoopItemsList, hashmapPositionId);

            /**
             * the condition checks if the number of instructions following the loop is greater than 1,
             * because if it is only 1, then we known it is RETURN
             * and in the effect GOTO of the first loop already points to it
             */

            if (ihAfterLoop.length > 0) {
                for (int i = 1; i < ihAfterLoop.length; i++) il_new.append(ihAfterLoop[i].getInstruction());
            }
        }

        return il_new;
    }

    public static void parallelizeNestedLoop(ClassGen cg, MethodGen mgOld) {


        InstructionList ilOld = mgOld.getInstructionList();

//        ADD METHOD:    SUB TASK
        addSubTaskInNestedLoop(cg, mgOld, 0, 2);
//        STATIC: 2 is third for-loop (to update if we want dynamically set amount for-loop to move)

//        CHANGE SELECTED METHOD WITH NESTED LOOPS
        InstructionList ilToAdd = parallelizeMethodNestedLoop(cg, mgOld);

        mgOld.setInstructionList(ilToAdd);
        mgOld.setMaxLocals();
        mgOld.setMaxStack();
        cg.removeMethod(mgOld.getMethod());
        cg.addMethod(mgOld.getMethod());
        cg.getConstantPool().addMethodref(mgOld);
        ilToAdd.dispose();
    }

    public static void replaceSpecificIndexes(int idOLD, int idToReplace, HashMap<Integer, Integer> hashmapIdOldAndNewLVar) {
        hashmapIdOldAndNewLVar.replace(idOLD, idToReplace);
    }

    protected static Instruction updateLVarIndexes(Instruction instr, HashMap<Integer, Integer> hashmapIdOldAndNewLVar, MethodGen mgNEW) {

        Instruction replace = null;

        if (instr.toString().contains("_")) {
//            System.out.println(instr.getName());
            int idOLD = -1, idNEW;
            String[] string = instr.getName().split("_");
            idOLD = Integer.parseInt(string[1]);

            idNEW = hashmapIdOldAndNewLVar.getOrDefault(idOLD, -1);

            if (idNEW > -1) {
                LocalVariable lv = VariableUtils.getLVarNameById(idNEW, mgNEW.getLocalVariableTable(mgNEW.getConstantPool()));

                if (instr.toString().contains("load"))
                    replace = InstructionFactory.createLoad(Type.getType(lv.getSignature()), idNEW);
                if (instr.toString().contains("store"))
                    replace = InstructionFactory.createStore(Type.getType(lv.getSignature()), idNEW);
            }
        }

        if (replace != null) return replace;
        else return instr;
    }
}
