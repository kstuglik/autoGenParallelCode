package pl.edu.agh.bcel.utils;

import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.generic.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class ForLoopUtils {

    static int CURR_ROW_ID = 0;

    public static int aGetStartForBlockIf(InstructionHandle[] ihy, int idPoistionIf) {

        ArrayList listInstruction2Fold = new ArrayList(Arrays.asList("add", "div", "mul", "or", "sub", "xor"));
        ArrayList listInstruction1Fold = new ArrayList(Arrays.asList("rem", "neg"));

        boolean stop = false;
        int numberOfLOAD = 0, i;

        boolean react = false;
        for (i = idPoistionIf - 1; !stop; i--) {
            if (i == -1) {
                i = 0;
                react = true;
            }
            String signature = ihy[i].getInstruction().getName();
            signature = signature.substring(1);

            if (listInstruction2Fold.contains(signature)) numberOfLOAD += 2;
            else if (listInstruction1Fold.contains(signature)) numberOfLOAD++;
            else if (signature.contains("const")) numberOfLOAD--;
            else if (signature.contains("load")) numberOfLOAD--;

//            at least two instructions must be taken, one type 'load' and one type 'store'
            if (!(i == idPoistionIf - 1) && numberOfLOAD == 0 && i < idPoistionIf - 2) stop = true;
            if (react) {
                stop = true;
            }

        }
        return i + 1;
    }

    public static void addHandleIncOrNext(HashMap<Integer, List<InstructionHandle>> hm, int key, InstructionHandle value) {
        if (hm.containsKey(key)) hm.get(key).add(value);
        else {
            List<InstructionHandle> list = new ArrayList<>();
            list.add(value);
            hm.put(key, list);
        }
    }

    public static ArrayList<Instruction> crateIncrementInForLoopByVariable(
            Instruction instr,
            HashMap<Integer, Integer> hashmapIdOldAndNewLVar,
            int idDodane, String operacja) {

        ArrayList<Instruction> instrukcje = new ArrayList<>();

        String[] string = instr.toString().split("\\W+|_");
        int ile = string.length;

        int idOLD = Integer.parseInt(string[ile - 2]);
        int idNEW = hashmapIdOldAndNewLVar.getOrDefault(idOLD, -1);


        if (idNEW != -1) instrukcje.add(new ILOAD(idNEW));
        else instrukcje.add(new ILOAD(idOLD));
        instrukcje.add(new ILOAD(idDodane));
        if (operacja.equals("+")) instrukcje.add(new IADD());
        else instrukcje.add(new ISUB());
        if (idNEW != -1) instrukcje.add(new ISTORE(idNEW));
        else instrukcje.add(new ISTORE(idOLD));

        return instrukcje;

    }

    public static void displayIncrementOrNextHandles(HashMap<Integer, List<InstructionHandle>> incrementOrNextHandle) {
        for (Integer key : incrementOrNextHandle.keySet()) {
            for (int i = 0; i < incrementOrNextHandle.get(key).size(); i++) {
                System.out.println("key = " + key + ", i = " + i + ", " + incrementOrNextHandle.get(key).get(i));
            }
        }
    }

//    private static void forLoopValidateAssignedIfStatement(List<ForLoopItem> forLoopItemsList) {
//        Collections.reverse(forLoopItemsList);
//        for (int i = 0; i < forLoopItemsList.size() - 1; i++) {// -1 because current "item" and next "item" are compared
//            ArrayList<InstructionHandle> externalLoop = forLoopItemsList.get(i).getHandleIF();
//            ArrayList<InstructionHandle> innnerLoop = forLoopItemsList.get(i + 1).getHandleIF();
//            externalLoop.removeAll(innnerLoop);
//        }
//    }

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

    public static int getIdLoadOrStoreFromInstruction(Instruction ih) {
        String[] string = ih.toString().split("\\W+|_");
        return Integer.parseInt(string[1]);
    }

//    public static InstructionList parallelizeMethodNestedLoop(ClassGen cg, MethodGen mgOld) {
//
//        InstructionList ilOld = mgOld.getInstructionList();
//        InstructionHandle[] ihy = ilOld.getInstructionHandles();
//        HashMap<Integer, Integer> hashmapPositionId = ForLoopItem.getHashmapPositionId(ihy);
//        List<ForLoopItem> forLoopItemsList = getListLoopForItems(ihy, hashmapPositionId);
//
//        InstructionList il_new = new InstructionList();
//        InstructionFactory factory = new InstructionFactory(cg, mgOld.getConstantPool());
//        InstructionHandle[] ihBeforeLoop = null;
//        InstructionHandle[] ihAfterLoop = null;
//        List<InstructionHandle> handlesLoopStart = new ArrayList<>();
//        List<BranchHandle> branchesGOTO = new ArrayList<>();
//        HashMap<Integer, List<BranchHandle>> branchesIF = new HashMap<>();
//        HashMap<Integer, List<InstructionHandle>> incrementOrNextHandle = new HashMap<>();//key - number of loop, keys: increment handles in looop
//
//        int selectedNumberOfLoops = 2;
//        int numberOfLoops = forLoopItemsList.size();
//
//        ForLoopItem forLoopItemTemp = forLoopItemsList.get(0);
//
////        for (LoopFor item : loopForItemsList) item.displayInfoAboutHandles();
//
//        for (int i = 0; i < selectedNumberOfLoops; i++) {
//            System.out.println("******* FOR LOOP no: " + i + " *********");
//
//            int idFirst = hashmapPositionId.get(forLoopItemsList.get(i).getHandleFirstInFor().getPosition());
//            int idLoopStart = hashmapPositionId.get(forLoopItemsList.get(i).getHandleStartLoop().getPosition());
//            int idIfComparator = hashmapPositionId.get(forLoopItemsList.get(i).getHandleIF().get(0).getPosition());
//            int countIfInOneForLoop = forLoopItemsList.get(i).getHandleIF().size();
//
//            //      set the instructions in BEFORE of the loop_0, if this is true
//
//            if (i == 0 && ihy[0].getInstruction() != forLoopItemsList.get(i).getHandleFirstInFor().getInstruction()) {
//                ihBeforeLoop = getInstructionBeforeLoop(ihy, forLoopItemTemp, hashmapPositionId, 0);
//                for (InstructionHandle instructionHandle : ihBeforeLoop)
//                    il_new.append(instructionHandle.getInstruction());
//            }
//
//            for (int j = idFirst; j < idIfComparator; j++) {
//                if (j == idLoopStart) {
//                    InstructionHandle ih = il_new.append(ihy[j].getInstruction());
//                    handlesLoopStart.add(ih);
//                } else il_new.append(ihy[j].getInstruction());
//            }
//
////          ADD IF COMPARATOR
//            int lastIdIfInFor = 0;
//            List<BranchHandle> tempIfBranchHandle = new ArrayList<>();
//            for (int j = idIfComparator; j < (idIfComparator + countIfInOneForLoop); j++) {
//                BranchHandle if_compare = getBranchHandleIF(il_new, ihy[j]);
//                tempIfBranchHandle.add(if_compare);
//                lastIdIfInFor = j;
//            }
//
//            branchesIF.put(i, tempIfBranchHandle);
//
//            /**
//             * GROUP OF INSTUCTION THAT OCCURES IN FRONT OF INTERNAL LOOP WILL BE ADDED
//             * */
//
//            if (i < forLoopItemsList.size() - 1) {
//                int idLastIfCompareFromPrevForLoop = lastIdIfInFor + 1;
//                int idFirstInstructionInNextForLoop = hashmapPositionId.get(forLoopItemsList.get(i + 1).getHandleFirstInFor().getPosition());
//                int numberIhBeforeLoop = idFirstInstructionInNextForLoop - idLastIfCompareFromPrevForLoop;
//
//                System.out.println("number of instruction before loop: " + numberIhBeforeLoop);
//
//                if (numberIhBeforeLoop > 0) {
//                    for (int j = idLastIfCompareFromPrevForLoop; j < idFirstInstructionInNextForLoop; j++) {
//                        System.out.println("loop: " + i + ") " + "[" + j + "], " + ihy[j].toString());
//                        il_new.append(ihy[j].getInstruction());
//                    }
//                }
//            }
//        }
//
//
////      >>>>>>>>>>>>>>>>>>>>>>      NEW INSTRUCTION INSIDE INNER LOOP BODY START HERE   >>>>>>>>>>>>>>>>>>>>>>
//
//        il_new.append(factory.createPrintln("IT WORKS"));
//        ReadyFields.changeBodyInnerLoopForMatrixMultiply(mgOld, il_new, factory);
//
////      <<<<<<<<<<<<<<<<<<<<<<      NEW INSTRUCTION INSIDE INNER LOOP BODY END HERE     <<<<<<<<<<<<<<<<<<<<<<
//
//
////        CLOSE INTERNAL LOOP
//        int positionIncrement = forLoopItemsList.get(selectedNumberOfLoops - 1).getHandleINC().getPosition();
//        int idIncrement = hashmapPositionId.get(positionIncrement);
//        InstructionHandle ih = il_new.append(ihy[idIncrement].getInstruction());
//        addHandleIncOrNext(incrementOrNextHandle, selectedNumberOfLoops - 1, ih);//2 is index (last)inner loop in case with number of loops = 3
//        BranchHandle gotoHandler = il_new.append(new GOTO(handlesLoopStart.get(selectedNumberOfLoops - 1)));
//        branchesGOTO.add(gotoHandler);
//
//
////       z tego wynika ze indeks od ktorego zaczynam jest przesuniety - zmniejszony o 1 czyli wychodze z tej
////          zazcynam od przed ostatniej
//        for (int i = selectedNumberOfLoops - 1 - 1; i >= 0; i--) {
//
//            /**
//             * GROUP OF INSTRUCTION THAT OCCURES IN END OF INTERNAL LOOP WILL BE ADDED
//             * */
//
//            int start = hashmapPositionId.get(forLoopItemsList.get(i + 1).getHandleGOTO().getPosition()) + 1;
//            int end = hashmapPositionId.get(forLoopItemsList.get(i).getHandleINC().getPosition());
//            int numberIhAfterLoop = end - start;
//
//            System.out.println("number of instruction after loop: " + numberIhAfterLoop);
//
//            if (numberIhAfterLoop > 0) {
//                ih = il_new.append(ihy[start].getInstruction());
//                addHandleIncOrNext(incrementOrNextHandle, i, ih);
//                int k = 1;
//                System.out.println("loop: " + i + ") instrukcja nr: " + k++);
//                if (numberIhAfterLoop > 1) {
//                    for (int j = start + 1; j < end; j++) {
//                        System.out.println("loop: " + i + ") instrukcja nr: " + k++ + ", " + ihy[j].toString());
//                        il_new.append(ihy[j].getInstruction());
//                    }
//                }
//            }
//            ih = il_new.append(ihy[end].getInstruction());
//            addHandleIncOrNext(incrementOrNextHandle, i, ih);
//            gotoHandler = il_new.append(new GOTO(handlesLoopStart.get(i)));
//            branchesGOTO.add(gotoHandler);
//        }
//
//        /**
//         * SET TARGET in if_branch_handle
//         * first is set to "returnHandle", first instruction after loops
//         * if there are other instructions behind the loop,
//         * the handler does not indicate an increment of iterator of the loop above
//         * but the first such instruction after this loop,
//         * in other case indicate to increment of external iterator*/
//
//        int positionNextInstr = forLoopItemsList.get(0).getHandleGOTO().getNext().getPosition();
//        int idNextInstr = hashmapPositionId.get(positionNextInstr);
//
//        InstructionHandle returnHandler = il_new.append(factory.createFieldAccess(
//                cg.getClassName(), "SERVICE",
//                new ObjectType("java.util.concurrent.ExecutorService"),
//                Const.GETSTATIC));
//
//        System.out.println("ALL increments/next handles");
//        displayIncrementOrNextHandles(incrementOrNextHandle);
//
//        for (int i = 0; i < selectedNumberOfLoops; i++) {
//            for (int j = 0; j < branchesIF.get(i).size(); j++) {
//                if (i == 0) {
//                    System.out.printf("i=%d, j=%d, with return handle: %s\n", i, j, returnHandler);
//                    branchesIF.get(0).get(i).setTarget(returnHandler);
//                } else {
//                    System.out.printf("i=%d, j=%d, instr increment: %s\n", i, j, incrementOrNextHandle.get(i - 1).get(0));
//                    branchesIF.get(i).get(j).setTarget(incrementOrNextHandle.get(i - 1).get(0));
//                }
//            }
//        }
//
//        /*    NOTE: THIS 1 instruction of TRY CATCH BLOCK must be move UP and use as a return handle*/
//
////        InstructionHandle returnHandle = il_new.append(factory.createFieldAccess(
////                cg.getClassName(),
////                "SERVICE",
////                new ObjectType("java.util.concurrent.ExecutorService"),
////                Const.GETSTATIC));
//        il_new.append(InstructionFactory.createLoad(Type.OBJECT, 55));
//        il_new.append(factory.createInvoke("java.util.concurrent.ExecutorService", "invokeAll", new ObjectType("java.util.List"), new Type[]{new ObjectType("java.util.Collection")}, Const.INVOKEINTERFACE));
//        il_new.append(InstructionConst.POP);
//
//        il_new.append(factory.createFieldAccess(
//                cg.getClassName(), "SERVICE", new ObjectType("java.util.concurrent.ExecutorService"), Const.GETSTATIC));
//
//        InstructionHandle endTry = il_new.append(factory.createInvoke(
//                "java.util.concurrent.ExecutorService", "shutdown", Type.VOID, Type.NO_ARGS, Const.INVOKEINTERFACE));
//
//        BranchInstruction gotoNext = InstructionFactory.createBranchInstruction(Const.GOTO, null);
//        il_new.append(gotoNext);
//
//        InstructionHandle startCatch = il_new.append(InstructionFactory.createStore(Type.OBJECT, mgOld.getMaxLocals()));
//
//        il_new.append(InstructionFactory.createLoad(Type.OBJECT, mgOld.getMaxLocals()));
//        il_new.append(factory.createInvoke(
//                "java.lang.Exception", "printStackTrace", Type.VOID, Type.NO_ARGS, Const.INVOKEVIRTUAL));
//
//        InstructionHandle returnId = il_new.append(ihy[idNextInstr].getInstruction());
//        gotoNext.setTarget(returnId);
//        mgOld.addExceptionHandler(returnHandler, endTry, startCatch, new ObjectType("java.lang.Exception"));
//
//
//        //      set the instructions in AFTER the loop_0, if this is true
//        InstructionHandle afterLastGoto = forLoopItemsList.get(forLoopItemsList.size() - 1).getHandleGOTO().getNext();
//        if (!(afterLastGoto.getInstruction() instanceof RETURN) && !(afterLastGoto.getNext().getInstruction() instanceof RETURN)) {
//            ihAfterLoop = getInstructionAfterLoop(ihy, forLoopItemsList, hashmapPositionId);
//
//            /**
//             * the condition checks if the number of instructions following the loop is greater than 1,
//             * because if it is only 1, then we known it is RETURN
//             * and in the effect GOTO of the first loop already points to it
//             */
//
//            if (ihAfterLoop.length > 0) {
//                for (int i = 1; i < ihAfterLoop.length; i++) il_new.append(ihAfterLoop[i].getInstruction());
//            }
//        }
//        return il_new;
//    }

    public static void replaceSpecificIndexes(int idOLD, int idToReplace, HashMap<Integer, Integer> hashmapIdOldAndNewLVar) {
        hashmapIdOldAndNewLVar.replace(idOLD, idToReplace);
    }

    public static Instruction updateLVarIndexes(
            Instruction instr, HashMap<Integer, Integer> hashmapIdOldAndNewLVar, MethodGen mgNEW, ClassGen cg) {

        Pattern pattern = Pattern.compile("-?\\d+?");
        Instruction replace = null;
        int idOLD = -1, idNEW;

        if (instr.toString().contains("_")) {

            String[] string = instr.toString().split("\\W+|_");
            int ile = string.length;

            if (instr.toString().contains("load") || instr.toString().contains("store")) {

                idOLD = Integer.parseInt(string[1]);
                idNEW = hashmapIdOldAndNewLVar.getOrDefault(idOLD, -1);

                if (idNEW != -1) {
                    LocalVariable lv = VariableUtils.getLVarNameById(idNEW, mgNEW.getLocalVariableTable(cg.getConstantPool()));

                    if (instr.toString().contains("load")) {
                        replace = InstructionFactory.createLoad(Type.getType(lv.getSignature()), idNEW);
                    } else if (instr.toString().contains("store")) {
                        replace = InstructionFactory.createStore(Type.getType(lv.getSignature()), idNEW);

                    }
                }
            }
        }

        if (instr.toString().contains("inc")) {

            String[] string = instr.toString().split("\\W+|_");
            int ile = string.length;

            idOLD = Integer.parseInt(string[ile - 2]);
            idNEW = hashmapIdOldAndNewLVar.getOrDefault(idOLD, -1);

            if (idNEW != -1) {
                int incrementValue = Integer.parseInt(string[ile - 1]);
                replace = new IINC(idNEW, incrementValue);
            }
        }

        if (replace != null) return replace;
        else return instr;
    }

}
