package pl.edu.agh.bcel.nested;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.generic.*;
import pl.edu.agh.bcel.LaunchProperties;
import pl.edu.agh.bcel.utils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;


public class AST {

    public static void aFillAListCreateForItem(
            InstructionHandle[] ihy, ArrayList<ForLoopNEW> cfi,
            ArrayList<BranchHandleItem> branches, ArrayList<BranchHandleItem> ifElseBlocks) {


        for (int i = branches.size() - 1; i >= 0; i--) {

            BranchHandleItem item = branches.get(i);

            if (item.getSignature().equals("goto")) {
                if (item.getPosition() > item.getPositionToJump()) {

                    ForLoopNEW temp = new ForLoopNEW();

                    int firstPrevStore = zGetFirstPrevIdBySignature(ihy, item.getIdPositionToJump(), "storeprev");

                    int positionPrevStore = ihy[firstPrevStore].getPosition();
                    temp.setPositionPrevStore(positionPrevStore);
                    temp.setIdPositionPrevStore(getIdFromHandlePosition(ihy, positionPrevStore));

                    int positionStartLoop = ihy[item.getIdPositionToJump()].getPosition();
                    temp.setPositionStartLoop(positionStartLoop);
                    temp.setIdPositionStartLoop(getIdFromHandlePosition(ihy, positionStartLoop));

                    temp.setIdPositionGOTO(item.getIdPosition());
                    temp.setPositionGOTO(item.getPosition());
                    temp.setBranchGOTO(item);

                    int idPositionINC = item.getIdPosition() - 1;
//                    trzeba znalezc pierwsza, dalej już poleci normalnie
//                    chociaz mozna od razu tworzyc liste

                    temp.setPositionINC(ihy[idPositionINC].getPosition());
                    temp.setIdPositionINC(idPositionINC);

                    cfi.add(temp);

                } else ifElseBlocks.add(item);
            }
        }

//        ROZPOZNAJ PRAWIDLOWE FORY
        for (ForLoopNEW forLoopNEW : cfi) {
            for (BranchHandleItem item : branches) {
                if (item.getSignature().contains("if")) {
                    if (forLoopNEW.getIdPositionPrevStore() == item.getIdPositionPrevStore()) {
                        forLoopNEW.addInstrIF(item);
                    }
                }
            }
        }

    }

    private static void aFillAListWithIfElseBlock(
            InstructionHandle[] ihy,
            ArrayList<BranchHandleItem> singleBrancheIF, ArrayList<BranchHandleItem> singleBrancheGOTO,
            ArrayList<ForLoopNEW> listOfPairedBlocksIfElse) {

//        petla zewnetrzna dotyczy tych if else ktore wystepuja samotnie i nie naleza do sekcji FOR petli
//        jak sie temu przyjrzec to wystepuja dwa liczniki, poniewaz mogą wystąpić bloki if-else lub if

        for (int i = singleBrancheIF.size() - 1, j = singleBrancheGOTO.size() - 1; i >= 0; i--, j--) {

            ForLoopNEW item = new ForLoopNEW();
            if (j >= 0) {
                item.setBranchGOTO(singleBrancheGOTO.get(j));
            }

            int prev = zGetFirstPrevIdBySignature(ihy, singleBrancheIF.get(i).getIdPosition(), "storeprev");
            int load = zGetFirstPrevIdBySignature(ihy, singleBrancheIF.get(i).getIdPosition(), "load");

            item.setIdPositionPrevStore(prev);
            item.setIdPositionStartLoop(load);
            item.addInstrIF(singleBrancheIF.get(i));

            listOfPairedBlocksIfElse.add(item);
        }
    }

    public static int aGetStartForBlockIf(InstructionHandle[] ihy, int idPoistionIf) {

        ArrayList listInstruction2Fold = new ArrayList(Arrays.asList("add", "div", "mul", "or", "sub", "xor"));
        ArrayList listInstruction1Fold = new ArrayList(Arrays.asList("rem", "neg"));

        boolean stop = false;
        int numberOfLOAD = 0, i;

//        instrukcje load i const sa zaliczane do tej samej grupy: "przekazanie" wartości czyli [LOAD]
//        licze wystapienia instrukcji load, zawsze jest to >= 1
//        react - situation when alone ifelse block is on the first position

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

    public static void aMoveSelectedForLoopIntoSubtask(
            ClassGen cg, MethodGen mgOld, String nazwaSubtask, int firstFOR, int lastFOR) {

        ArrayList<ForLoopNEW> listOfPairedBlocksFOR = new ArrayList<>();
        ArrayList<ForLoopNEW> listOfPairedBlocksIfElse = new ArrayList<>();
        ArrayList<ForLoopNEW> listAloneBlocksIfElse = new ArrayList<>();
        InstructionList il = new InstructionList();
        ConstantPoolGen cp = cg.getConstantPool();
        InstructionFactory factory = new InstructionFactory(cg, cg.getConstantPool());


        getLoopStructureIntoArrayLists(mgOld, listAloneBlocksIfElse, listOfPairedBlocksFOR, listOfPairedBlocksIfElse);

        try {
            int iloscPetli = listOfPairedBlocksFOR.size();
            if (firstFOR > lastFOR || firstFOR < 0 || firstFOR > iloscPetli || lastFOR > iloscPetli) {
                throw new Exception("\nWRONG PARAMS!\n" + "petli jest: " + iloscPetli +
                        ", odtad min = 0, dotad max = " + (iloscPetli - 1));
            } else {
//        method with 2 params
                MethodGen mgNew = new MethodGen(Const.ACC_PUBLIC | Const.ACC_STATIC,
                        Type.INT, Type.NO_ARGS, new String[]{}, nazwaSubtask, null, il, cp);
                LocalVariableGen startVariable = mgNew.addLocalVariable(LaunchProperties.START_INDEX_VAR_NAME, Type.INT, null, null);
                LocalVariableGen endVariable = mgNew.addLocalVariable(LaunchProperties.END_INDEX_VAR_NAME, Type.INT, null, null);

                int ID_START = listOfPairedBlocksFOR.get(0).getIdInsideLoop();
                int ID_STOP = listOfPairedBlocksFOR.get(0).getIdPositionINC();

                InstructionList ilToAddInsideLoop = TransformUtils.setNewLoopBody(
                        cg, mgOld, (short) 1000, ID_START, ID_STOP);

//      *****************************************************************************************
                InstructionList ilApend = aReconstructSelectedForLoopSlave(
                        listOfPairedBlocksFOR, firstFOR, lastFOR, true, cg, mgOld, mgNew);
//      *****************************************************************************************

                il.append(ilApend);

                mgOld.setInstructionList(il);
                mgOld.setMaxLocals();
                mgOld.setMaxStack();

                cg.replaceMethod(mgOld.getMethod(), mgOld.getMethod());
                il.dispose();
//                mgNew.setArgumentNames(new String[]{
//                        LaunchProperties.START_INDEX_VAR_NAME, LaunchProperties.END_INDEX_VAR_NAME});
////                LoopUtilsOld.updateLoopStartCondition(il.getInstructionHandles(), startVariable.getIndex());
////                LoopUtilsOld.updateLoopEndCondition(il.getInstructionHandles(), endVariable.getIndex());
//                mgNew.setInstructionList(il);
//                mgNew.setMaxLocals();
//                mgNew.setMaxStack();
//                cg.addMethod(mgNew.getMethod());

            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }

    public static void aMoveSelectedForLoopIntoSubtask1(ClassGen cg, MethodGen mgOld, String nazwaSubtask, int firstFOR, int lastFOR) {

        ArrayList<ForLoopNEW> listOfPairedBlocksFOR = new ArrayList<>();
        ArrayList<ForLoopNEW> listOfPairedBlocksIfElse = new ArrayList<>();
        ArrayList<ForLoopNEW> listAloneBlocksIfElse = new ArrayList<>();
        InstructionList il = new InstructionList();
        ConstantPoolGen cp = cg.getConstantPool();
        InstructionFactory factory = new InstructionFactory(cg, cg.getConstantPool());

        getLoopStructureIntoArrayLists(mgOld, listAloneBlocksIfElse, listOfPairedBlocksFOR, listOfPairedBlocksIfElse);

        try {
            int iloscPetli = listOfPairedBlocksFOR.size();
            if (firstFOR > lastFOR || firstFOR < 0 || firstFOR > iloscPetli || lastFOR > iloscPetli) {
                throw new Exception("\nWRONG PARAMS!\n" + "petli jest: " + iloscPetli +
                        ", odtad min = 0, dotad max = " + (iloscPetli - 1));
            } else {
//        method with 2 params
                MethodGen mgNew = new MethodGen(Const.ACC_PUBLIC | Const.ACC_STATIC, Type.INT, Type.NO_ARGS, new String[]{}, nazwaSubtask, null, il, cp);
                LocalVariableGen startVariable = mgNew.addLocalVariable(LaunchProperties.START_INDEX_VAR_NAME, Type.INT, null, null);
                LocalVariableGen endVariable = mgNew.addLocalVariable(LaunchProperties.END_INDEX_VAR_NAME, Type.INT, null, null);
                mgNew.setArgumentNames(new String[]{LaunchProperties.START_INDEX_VAR_NAME, LaunchProperties.END_INDEX_VAR_NAME});
                mgNew.setArgumentTypes(new Type[]{Type.INT, Type.INT});

                int ID_START = listOfPairedBlocksFOR.get(0).getIdInsideLoop();
                int ID_STOP = listOfPairedBlocksFOR.get(0).getIdPositionINC();


//      *****************************************************************************************
                InstructionList ilApend = aReconstructSelectedForLoopSlave(
                        listOfPairedBlocksFOR, firstFOR, lastFOR, true,
                        cg, mgOld, mgNew);
//      *****************************************************************************************
                il.append(ilApend);
                il.append(factory.createPrintln("gogo"));


//                LoopUtilsOld.updateLoopStartCondition(il.getInstructionHandles(), startVariable.getIndex());
//                LoopUtilsOld.updateLoopEndCondition(il.getInstructionHandles(), endVariable.getIndex());

                mgNew.setInstructionList(il);
                mgNew.setMaxLocals();
                mgNew.setMaxStack();
                cg.addMethod(mgNew.getMethod());

            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }

    public static InstructionList aReconstructSelectedForLoopSlave(
            ArrayList<ForLoopNEW> listOfPairedBlocksFOR,
            int odtad, int dotad, boolean flaga,
            ClassGen cg, MethodGen mgOld, MethodGen mgNew) {

//      *****************************************************************************************
        HashMap<Integer, ArrayList<BranchHandle>> hashmapIFinFOR = new HashMap<>();
        HashMap<Integer, ArrayList<BranchInstruction>> hashmapGOTO = new HashMap<>();
        HashMap<Integer, ArrayList<InstructionHandle>> hashmapNEXT = new HashMap<>();
        HashMap<Integer, ArrayList<InstructionHandle>> hashmapSTART = new HashMap<>();
//      *****************************************************************************************
        InstructionHandle[] ihy = mgOld.getInstructionList().getInstructionHandles();
        InstructionFactory factory = new InstructionFactory(cg, cg.getConstantPool());
        ConstantPoolGen cp = mgOld.getConstantPool();
//      *****************************************************************************************
        HashMap<Integer, Integer> hashmapIdOldAndNewLVar = VariableUtils.getHashmapLVarIndexesOldAndNew(mgOld, mgNew);
//      *****************************************************************************************

        System.out.println("odtad = " + odtad + ", dotad = " + dotad);
//        int BEFORE = listOfPairedBlocksFOR.get(0).getIdPositionPrevStore();

        InstructionList il = new InstructionList();

        for (int i = odtad; i <= dotad; i++) {

            ForLoopNEW item = listOfPairedBlocksFOR.get(i);
            ArrayList<Integer> ids = item.getIdsIFinFor();
            ArrayList<Integer> idsls = item.getIdsIFinForStartLoop();

            System.out.println("ids[0] = " + ids.get(0) + ", " + ihy[ids.get(0)]);
            int ile = ids.size();

            ArrayList<BranchHandle> listaIFinFOR = new ArrayList<>();
            ArrayList<BranchInstruction> listaGOTO = new ArrayList<>();
            ArrayList<InstructionHandle> listaSTART = new ArrayList<>();


//        PART ONE: instrukcje z FORa
            int idOstatniegoIfaWPetli = item.getInstrIF().get(ile - 1).getIdPosition();
//            tutaj bylo mniejsze lub rowne ale nie wiem skad ta "niedokladnosc"
            for (int j = item.getIdPositionPrevStore(); j <= idOstatniegoIfaWPetli; j++) {
                System.out.println("j = " + j + ", " + ihy[j].getInstruction().toString());
                if (idsls.contains(j)) {
                    System.out.println("doddano: loopstart: " + j);
                    listaSTART.add(il.append(ihy[j].getInstruction()));
                } else if (ids.contains(j)) {
                    System.out.println("doddano: if: " + j);
                    listaIFinFOR.add(ForLoopUtils.getBranchHandleIF(il, ihy[j]));
                } else {
                    System.out.println("doddano: inne: " + j);
                    il.append(ihy[j].getInstruction());
                }
            }


            int inc = item.getIdPositionINC();
            if (idOstatniegoIfaWPetli + 1 == inc) {
                System.out.println("przypadek gdy INC jest ostatnim poleceniem w petli, tutaj next ==inc");
            } else {
                System.out.println("przypadek gdy INC NIE jest ostatnim poleceniem w petli, " +
                        "\n\ttutaj next == to co po (chyba ze trzeba pomiedzy)");
                for (int j = idOstatniegoIfaWPetli + 1; j < inc; j++) {
                    System.out.println("dla j = " + j);
                    il.append(ihy[j].getInstruction());
                }
            }
//        PART TWO: dodane instrukcje/lub odtworzone
//*********************************************************************************
            il.append(factory.createPrintln("ahoj z tym wszystkim"));
//********************************************************************************
            hashmapIFinFOR.put(i, listaIFinFOR);
            hashmapGOTO.put(i, listaGOTO);
            hashmapSTART.put(i, listaSTART);

        }

        for (int a = dotad; a >= odtad; a--) {

            ForLoopNEW item = listOfPairedBlocksFOR.get(a);
            ArrayList<InstructionHandle> listaNEXT = new ArrayList<>();

//          *********************************************
            int inc = item.getIdPositionINC();
            InstructionHandle ih;
            String[] string = ihy[inc].getInstruction().toString().split("\\W+|_");

            int idOLD = -1;
            try {
                idOLD = Integer.parseInt(string[1]);
            } catch (Exception e) {
                System.err.println("to jest blad idOld, idOld = " + idOLD + ", ih[inc] = " + ihy[inc].getInstruction().getName());
            }
            System.out.println("(kiedy one sie zaktualizowaly bo to jest nowe a pisze ze stare... idold = " + idOLD);
            if (idOLD != -1) {
                int idNEW = hashmapIdOldAndNewLVar.getOrDefault(idOLD, -1);
                int incrementValue = Integer.parseInt(string[3]);
                System.out.println("idnew = " + idNEW);
                if (idNEW != -1) {
                    ih = il.append(new IINC(idNEW, incrementValue));
                } else {
                    ih = il.append(new IINC(idOLD, incrementValue));
                }
                listaNEXT.add(ih);
                hashmapNEXT.put(a, listaNEXT);
            } else {
                System.out.println("TUTAJ?B");
                ih = il.append(ihy[inc].getInstruction());
                listaNEXT.add(ih);
                hashmapNEXT.put(a, listaNEXT);
            }
            System.out.println("TUTAJ?");
//          *************************************************

            BranchInstruction gotobh = InstructionFactory.createBranchInstruction(
                    Const.GOTO, hashmapSTART.get(a).get(0));
            il.append(gotobh);
            hashmapGOTO.get(a).add(gotobh);
        }

        InstructionHandle returnHandler = il.append(new PUSH(cp, 0));
        il.append(InstructionFactory.createReturn(Type.INT));

        for (int petlaNumer = odtad; petlaNumer <= dotad; petlaNumer++) {

            ForLoopNEW item = listOfPairedBlocksFOR.get(petlaNumer);
            int ileIfow = item.getInstrIF().size();
            int positionInside = item.getInstrIF().get(ileIfow - 1).getInstruction().getNext().getPosition();

            for (int i = 0; i < ileIfow; i++) {
                BranchHandleItem itemif = item.getInstrIF().get(i);

                if (itemif.getPositionToJump() == positionInside) {
                    hashmapIFinFOR.get(petlaNumer).get(i).setTarget(hashmapSTART.get(petlaNumer).get(hashmapSTART.get(petlaNumer).size() - 1));
                } else if (itemif.getPositionToJump() > positionInside) {
                    if (petlaNumer == odtad) hashmapIFinFOR.get(petlaNumer).get(i).setTarget(returnHandler);
                    else hashmapIFinFOR.get(petlaNumer).get(i).setTarget(hashmapNEXT.get(petlaNumer - 1).get(0));
                } else hashmapIFinFOR.get(petlaNumer).get(i).setTarget(hashmapSTART.get(petlaNumer).get(i + 2));
            }

        }

        return il;
    }

    private static ArrayList<ForLoopNEW> addBlocksIfElseTolistOfPairedBlocksFOR(
            ArrayList<ForLoopNEW> listOfPairedBlocksFOR, ArrayList<ForLoopNEW> listOfPairedBlocksIfElse) {

        ArrayList<ForLoopNEW> listAloneBlocksIfElse = new ArrayList<>();

        int numberOfFOR = listOfPairedBlocksFOR.size();

        for (int i = 0; i < numberOfFOR; i++) {
            ForLoopNEW itemFOR = listOfPairedBlocksFOR.get(i);
            for (ForLoopNEW itemIFELSE : listOfPairedBlocksIfElse) {
                int startIFELSE = itemIFELSE.getInstrIF().get(0).getIdPosition();

                int startFOR = itemFOR.getInstrIF().get(0).getIdPosition();
                int endFOR = itemFOR.getIdPositionINC();

                if (startIFELSE > startFOR && startIFELSE < endFOR) {
                    itemFOR.addBlocksIFELSE(itemIFELSE);
                }

//                furtka wyjscia dla tych ifelse ktore sa poza petlami w taki czy inny sposob to moze sie przydac

                else {
//                    chodzi tylko o petle zewnetrzna,
//                    bo sprawdzanie BEFORE/AFTER dla wewnetrznej mija sie z celem bo to jest BODY petli zewnetrznej
                    if (i == 0) {
                        if (startIFELSE < startFOR) itemIFELSE.setBefore(true);
                        else itemIFELSE.setAfter(true);
                        listAloneBlocksIfElse.add(itemIFELSE);
                    }
                }
            }
//
//            itemFOR.setPositionInsideLoop(itemFOR.getInstrIF().get(itemFOR.getInstrIF().size()).getPosition() + 1);
//            itemFOR.setPositionInsideLoop(itemFOR.getInstrIF().get(itemFOR.getInstrIF().size()).getIdPosition() + 1);
        }
        return listAloneBlocksIfElse;
    }

    private static void assignBranchGOTOifExist(
            ArrayList<ForLoopNEW> listAloneBlocksIfElse, InstructionHandle[] ihy) {

        for (int i = 0; i < listAloneBlocksIfElse.size(); i++) {

            ForLoopNEW temp = listAloneBlocksIfElse.get(i);

            for (int j = 0; j < temp.getInstrIF().size(); j++) {

                int j2 = temp.getInstrIF().get(j).getIdPositionToJump();

                if (ihy[j2].getPrev().toString().contains("goto")) {
//                    System.out.println("udalo sie znalezc GOTO, uzupelniono ids dla: " + ihy[j2]);

                    int position = ihy[j2].getPrev().getPosition();
                    int idPosition = getIdFromHandlePosition(ihy, position);
                    int positionAfterLoop = getPositionToJumpFromInstruction(ihy[j2].getPrev());
                    int idPositionAfterLoop = getIdFromHandlePosition(ihy, positionAfterLoop);

                    temp.setPositionAfterElse(positionAfterLoop);
                    temp.setIdPositionAfterElse(idPositionAfterLoop);

                    temp.setPositionGOTO(position);
                    temp.setIdPositionGOTO(idPosition);
                }
            }

        }
    }

    protected static Instruction factoryLoadStore(Instruction instr, MethodGen mgNEW) {

        Instruction replace = null;

        if (instr.toString().contains("_")) {
//            System.out.println(instr.getName());
            int idOLD = -1, idNEW;
            String[] string = instr.getName().split("_");
            idOLD = Integer.parseInt(string[1]);


            LocalVariable lv = VariableUtils.getLVarNameById(idOLD, mgNEW.getLocalVariableTable(mgNEW.getConstantPool()));

            if (instr.toString().contains("load"))
                replace = InstructionFactory.createLoad(Type.getType(lv.getSignature()), idOLD);
            if (instr.toString().contains("store"))
                replace = InstructionFactory.createStore(Type.getType(lv.getSignature()), idOLD);
        }
        if (replace != null) return replace;
        else return instr;
    }

    public static int getIdFromHandlePosition(InstructionHandle[] ihy, int position) {
        for (int i = 0; i < ihy.length; i++) if (ihy[i].getPosition() == position) return i;
        System.out.println("czy to tu?");
        return -1;
    }

    public static void getLoopStructureIntoArrayLists(MethodGen mg,
                                                      ArrayList<ForLoopNEW> listAloneBlocksIfElse,
                                                      ArrayList<ForLoopNEW> listOfPairedBlocksFOR,
                                                      ArrayList<ForLoopNEW> listOfPairedBlocksIfElse) {

        ArrayList<BranchHandleItem> branches = new ArrayList<>();
        ArrayList<BranchHandleItem> singleBrancheGOTO = new ArrayList<>();
        ArrayList<BranchHandleItem> singleBrancheIF;

        InstructionHandle[] ihy = mg.getInstructionList().getInstructionHandles();
        zCreateAListBranchHandleItems(ihy, branches);
        aFillAListCreateForItem(ihy, listOfPairedBlocksFOR, branches, singleBrancheGOTO);
//        zDisplayAListCreateForItemsExtended(listOfPairedBlocksFOR, "PETLA", ihy);

        singleBrancheIF = zGetAListSingleInstructionsIF(branches, listOfPairedBlocksFOR);
        Collections.reverse(singleBrancheGOTO);

        aFillAListWithIfElseBlock(ihy, singleBrancheIF, singleBrancheGOTO, listOfPairedBlocksIfElse);
        assignBranchGOTOifExist(listOfPairedBlocksIfElse, ihy);

        Collections.reverse(listOfPairedBlocksIfElse);
//        zDisplayDetailsAboutBlockIfElseExtended(listOfPairedBlocksIfElse, "IFELSE", ihy);

        listAloneBlocksIfElse = addBlocksIfElseTolistOfPairedBlocksFOR(listOfPairedBlocksFOR, listOfPairedBlocksIfElse);
//        zDisplayDetailsAboutBlockIfElseExtended(listAloneBlocksIfElse, "ALONE IFELSE", ihy);

        assignBranchGOTOifExist(listAloneBlocksIfElse, ihy);

    }

    public static int getPositionToJumpFromInstruction(InstructionHandle ih) {
        String[] string = ih.toString().split("->");
        return Integer.parseInt(string[1].replace(" ", ""));
    }


    public static void reCreateVariables(MethodGen mgOld, MethodGen mgNew, ConstantPoolGen cp, InstructionList il) {
        LocalVariable[] lvt = mgOld.getLocalVariableTable(cp).getLocalVariableTable();
        ArrayList<LocalVariableGen> lista = new ArrayList<>();

        for (LocalVariable variable : lvt) {
            lista.add(
                    mgNew.addLocalVariable(
                            variable.getName(), Type.getType(
                                    variable.getSignature()), il.getStart(), null));
        }
    }


    private static ArrayList<BranchHandleItem> zGetAListSingleInstructionsIF(
            ArrayList<BranchHandleItem> bhi, ArrayList<ForLoopNEW> cfi) {

        ArrayList<BranchHandleItem> resultTemp = new ArrayList<>();
        ArrayList<BranchHandleItem> result = new ArrayList<>();

        for (BranchHandleItem branch : bhi) {
            for (ForLoopNEW forLoopNEW : cfi) {
                ArrayList<BranchHandleItem> list = forLoopNEW.getInstrIF();
                if (list.contains(branch) && branch.getSignature().contains("if"))
                    resultTemp.add(branch);
            }
        }

        for (int i = 0; i < bhi.size() - 1; i++) {
            if (!resultTemp.contains(bhi.get(i)) && bhi.get(i).getSignature().contains("if")) {
                result.add(bhi.get(i));
            }
        }

        return result;
    }

    public static int zGetFirstPrevIdBySignature(InstructionHandle[] ihy, int start, String signature) {

        int i = start - 1;
        boolean extraPrevFlag = false;

        if (signature.contains("prev")) {
            extraPrevFlag = true;
            signature = signature.replace("prev", "");
        }

        while (!ihy[i].toString().contains(signature)) i--;

        if (extraPrevFlag) i -= 1;//        we want the id of the instruction before "store"

        return i;
    }

    public static HashMap<Integer, Integer> zGetHashmapPositionId(InstructionHandle[] ihy) {

        HashMap<Integer, Integer> hashmapInstructionPositionId = new HashMap<>();

        for (int i = 0; i < ihy.length; i++)
            hashmapInstructionPositionId.put(ihy[i].getPosition(), i);

        return hashmapInstructionPositionId;

    }

}
