package pl.edu.agh.bcel.nested;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.generic.*;
import pl.edu.agh.bcel.utils.ForLoopUtils;
import pl.edu.agh.bcel.utils.VariableUtils;

import java.io.IOException;
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
            ArrayList<BranchHandleItem> singleBrancheIF,
            ArrayList<BranchHandleItem> singleBrancheGOTO,
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

    public static void aReconstructSelectedMethod(
            ClassGen cg, MethodGen mgOld,
            ArrayList<ForLoopNEW> listAloneBlocksIfElse,
            ArrayList<ForLoopNEW> listOfPairedBlocksFOR,
            int odtad, int dotad) {

        if (odtad == dotad) {
            if (odtad == 0)
                System.out.println("odtworze pierwszy FOR");
            else
                System.out.println("odtworze wybrany FOR");
        } else if (dotad - odtad == listOfPairedBlocksFOR.size() - 1) {
            System.out.println("odtworze wszystkie FOR");
        } else {
            System.out.println("odtworze kilka (nie wszystkie) FOR");
        }


        ConstantPoolGen cp = cg.getConstantPool();
        InstructionFactory factory = new InstructionFactory(cg, cg.getConstantPool());
        InstructionHandle[] ihy = mgOld.getInstructionList().getInstructionHandles();
        InstructionList il = new InstructionList();

        MethodGen mgNew = new MethodGen(Const.ACC_PUBLIC | Const.ACC_STATIC, Type.INT, Type.NO_ARGS,
                new String[]{}, "testowo", null, il, cp);

        reCreateVariables(mgOld, mgNew, cp, il);

//      *****************************************************************************************
        HashMap<Integer, ArrayList<BranchHandle>> hashmapIFinFOR = new HashMap<>();
        HashMap<Integer, ArrayList<BranchInstruction>> hashmapGOTO = new HashMap<>();
        HashMap<Integer, ArrayList<InstructionHandle>> hashmapNEXT = new HashMap<>();
        HashMap<Integer, ArrayList<InstructionHandle>> hashmapSTART = new HashMap<>();
//      *****************************************************************************************


        for (int i = odtad; i <= dotad; i++) {

            ForLoopNEW item = listOfPairedBlocksFOR.get(i);
            ArrayList<Integer> ids = item.getIdsIFinFor();
            ArrayList<Integer> idsls = item.getIdsIFinForStartLoop();

            int ile = ids.size();

            ArrayList<BranchHandle> listaIFinFOR = new ArrayList<>();
            ArrayList<BranchInstruction> listaGOTO = new ArrayList<>();
            ArrayList<InstructionHandle> listaSTART = new ArrayList<>();


//        PART ONE: instrukcje z FORa
            for (int j = item.getIdPositionPrevStore(); j <= ids.get(ile - 1); j++) {
                if (idsls.contains(j)) listaSTART.add(il.append(ihy[j].getInstruction()));
                else if (ids.contains(j)) listaIFinFOR.add(ForLoopUtils.getBranchHandleIF(il, ihy[j]));
                else il.append(ihy[j].getInstruction());
            }


//        PART TWO: dodane instrukcje/lub odtworzone
            listaSTART.add(il.append(factory.createPrintln("Hello World from FOR-LOOP-" + i)));

            hashmapIFinFOR.put(i, listaIFinFOR);
            hashmapGOTO.put(i, listaGOTO);
            hashmapSTART.put(i, listaSTART);
        }

        for (int a = dotad; a >= odtad; a--) {
            ForLoopNEW item = listOfPairedBlocksFOR.get(a);
            ArrayList<InstructionHandle> listaNEXT = new ArrayList<>();
            int inc = item.getIdPositionINC();
            listaNEXT.add(il.append(ihy[inc].getInstruction()));
            hashmapNEXT.put(a, listaNEXT);

            BranchInstruction gotobh = InstructionFactory.createBranchInstruction(Const.GOTO, hashmapSTART.get(a).get(0));
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

//      *****************************************************************************************
        mgNew.setInstructionList(il);
        mgNew.setMaxLocals();
        mgNew.setMaxStack();
        cg.addMethod(mgNew.getMethod());
        il.dispose();

    }

    private static ArrayList<ForLoopNEW> addBlocksIfElseTolistOfPairedBlocksFOR(
            ArrayList<ForLoopNEW> listOfPairedBlocksFOR,
            ArrayList<ForLoopNEW> listOfPairedBlocksIfElse) {

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
            ArrayList<ForLoopNEW> listAloneBlocksIfElse,
            InstructionHandle[] ihy) {

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
        return -1;
    }

    public static int getPositionToJumpFromInstruction(InstructionHandle ih) {
        String[] string = ih.toString().split("->");
        return Integer.parseInt(string[1].replace(" ", ""));
    }

    public static void main(ClassGen cg, MethodGen mgOld) throws IOException {

        InstructionHandle[] ihy = mgOld.getInstructionList().getInstructionHandles();

        ArrayList<BranchHandleItem> branches = new ArrayList<>();
        ArrayList<BranchHandleItem> singleBrancheGOTO = new ArrayList<>();
        ArrayList<ForLoopNEW> listOfPairedBlocksFOR = new ArrayList<>();
        ArrayList<ForLoopNEW> listOfPairedBlocksIfElse = new ArrayList<>();
        ArrayList<BranchHandleItem> singleBrancheIF;
        ArrayList<ForLoopNEW> listAloneBlocksIfElse;

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

        aReconstructSelectedMethod(cg, mgOld, listAloneBlocksIfElse, listOfPairedBlocksFOR, 0, 1);

    }

    public static void reCreateVariables(MethodGen mgOld, MethodGen mgNew, ConstantPoolGen cp, InstructionList il) {
        LocalVariable[] lvt = mgOld.getLocalVariableTable(cp).getLocalVariableTable();
        ArrayList<LocalVariableGen> lista = new ArrayList<>();

        for (LocalVariable variable : lvt) {
            lista.add(
                    mgNew.addLocalVariable(
                            variable.getName(), Type.getType(variable.getSignature()), il.getStart(), null));
        }
//      the value 0 is universal enough to fit almost all types of variables
//        for (LocalVariableGen localVariableGen : lista) {
//            il.append(new PUSH(cp, 0));
//            localVariableGen.setStart(il.append(new ASTORE(localVariableGen.getIndex())));
//        }
    }

    public static void zCreateAListBranchHandleItems(InstructionHandle[] ihy, ArrayList<BranchHandleItem> bhi) {

        HashMap<Integer, Integer> hashmapPositionId = zGetHashmapPositionId(ihy);

        for (InstructionHandle item : ihy) {
            if (item.getInstruction() instanceof GOTO || item.toString().contains("if")) {
                bhi.add(zCreateBranchHandleItem(ihy, item, hashmapPositionId));
            }
        }
    }

    public static BranchHandleItem zCreateBranchHandleItem(
            InstructionHandle[] ihy, InstructionHandle ih, HashMap<Integer, Integer> hmPositionId) {

        BranchHandleItem bhi = new BranchHandleItem();
        int position = getPositionToJumpFromInstruction(ih);
        bhi.setPositionToJump(position);
        bhi.setIdPositionToJump(hmPositionId.get(position));
        bhi.setPosition(ih.getPosition());
        int idPosition = hmPositionId.get(ih.getPosition());
        bhi.setIdPosition(idPosition);
        bhi.setInstruction((BranchHandle) ih);
        bhi.setSignature(ih.getInstruction().getName());

        bhi.setIdPositionPrevStore(zGetFirstPrevIdBySignature(ihy, idPosition, "storeprev"));
        bhi.setIdPositionPrevLoad(zGetFirstPrevIdBySignature(ihy, idPosition, "load"));

        return bhi;
    }

    public static void zDisplayAListCreateForItemsExtended(
            ArrayList<ForLoopNEW> cfi, String label, InstructionHandle[] ihy) {

        for (int i = 0; i < cfi.size(); i++) {

            ForLoopNEW item = cfi.get(i);

            System.out.println(label + " nr: " + i);
            System.out.println("liczba instrukcji IF w FOR: " + item.getInstrIF().size());

            if (item.getInstrIF().size() > 0) {
                for (int j = 0; j < item.getInstrIF().size(); j++) {
                    System.out.println(item.getInstrIF().get(j).getInstruction());
                }
            }

            int idFirstInFor = item.getIdPositionPrevStore();

            int idStartLoop = item.getIdPositionStartLoop();

            System.out.println("\n\tINSTRUKCJE INICJALIZACJI FOR");
            for (int j = idFirstInFor; j < idStartLoop; j++) System.out.println("\t\t" + ihy[j]);

            int countIFinFOR = item.getInstrIF().size();
            int lastIFinFOR = item.getInstrIF().get(countIFinFOR - 1).getIdPosition();
            System.out.println("\tINSTRUKCJE WARUNKOWE W FOR");
            for (int j = idStartLoop; j <= lastIFinFOR; j++) System.out.println("\t\t" + ihy[j]);

            System.out.println("\tPOZOSTAŁE INFORMACJE");

            if (item.getIdPositionINC() != -1) System.out.println("\t\t" + ihy[item.getIdPositionINC()]);
            if (item.getIdPositionGOTO() != -1) System.out.println("\t\t" + ihy[item.getIdPositionGOTO()]);

            System.out.println("\n***********************************************************\n");
        }

    }

    public static void zDisplayDetailsAboutBlockIfElseExtended(
            ArrayList<ForLoopNEW> listOfPairedBlocksIfElse, String label, InstructionHandle[] ihy) {

        for (int i = 0; i < listOfPairedBlocksIfElse.size(); i++) {

            ForLoopNEW temp = listOfPairedBlocksIfElse.get(i);

            System.out.print("\n" + label + " nr: " + i);

            int idPositionIF = temp.getInstrIF().get(0).getIdPosition();
            int positionInCode = temp.getInstrIF().get(0).getPosition();
            System.out.print("\t(id IF: " + idPositionIF + ", position IF in code: " + positionInCode + ")\n\n");
            System.out.println("\tINSTRUKCJE WARUNKU IF");
//            int idFirstInstruction = aGetStartForBlockIf(ihy, idPositionIF);
//            int idFirstInstruction = temp.getIdPositionPrevStore();
            int startIF = temp.getIdPositionStartLoop();

            for (int ii = startIF; ii < idPositionIF; ii++) System.out.println("\t\t" + ihy[ii]);

            int jumpToElse = temp.getInstrIF().get(0).getIdPositionToJump();

            System.out.println("\tINSTRUKCJE BLOKU IF:");
            for (int j = idPositionIF; j < jumpToElse; j++) System.out.println("\t\t" + ihy[j]);

//            tutaj ostroznie bo sa 2 przypadki tzn: w ifie i w "forze" bloku ifelse
            if (temp.getIdPositionGOTO() != -1) {
//                UWAGA BO JEST TYLKO DO PIERWSZEJ A TRZEBA OBSLUZYC WSZYSTKO
                int backToCode = temp.getIdPositionAfterElse();
                System.out.println("\tINSTRUKCJE BLOKU ELSE:");
                for (int j = jumpToElse; j < backToCode; j++) System.out.println("\t\t" + ihy[j]);
            }
            System.out.println("\n***********************************************************\n");
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

    public static void zczyWskazujeZapetle(InstructionHandle[] ihy, int i) {
        String werrdykt = "";
        for (int start = i; start > i - 3; start--) {
            if (ihy[i].getPrev().toString().contains("goto")) {
                werrdykt = "ta instrukcja pokazuje za petle";
                break;
            } else werrdykt = "ta instrukcja jest w srodku";
        }
        System.out.println(werrdykt);
    }

}
