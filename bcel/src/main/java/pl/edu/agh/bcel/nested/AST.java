package pl.edu.agh.bcel.nested;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.generic.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;


public class AST {

    public static void main(ClassGen cg, MethodGen mgOld) throws IOException {

        InstructionHandle[] ihy = mgOld.getInstructionList().getInstructionHandles();

        ArrayList<BranchHandleItem> branches = new ArrayList<>();
        ArrayList<BranchHandleItem> singleBrancheGOTO = new ArrayList<>();
        ArrayList<CreateForItem> listOfPairedBlocksFOR = new ArrayList<>();
        ArrayList<CreateForItem> listOfPairedBlocksIfElse = new ArrayList<>();

        zCreateAListBranchHandleItems(ihy, branches);

        aFillAListCreateForItem(ihy, listOfPairedBlocksFOR, branches, singleBrancheGOTO);
        zDisplayAListCreateForItemsExtended(listOfPairedBlocksFOR, "PETLA", ihy);


        ArrayList<BranchHandleItem> singleBrancheIF = zGetAListSingleInstructionsIF(branches, listOfPairedBlocksFOR);
        Collections.reverse(singleBrancheGOTO);

        aFillAListWithIfElseBlock(ihy, singleBrancheIF, singleBrancheGOTO, listOfPairedBlocksIfElse);
        Collections.reverse(listOfPairedBlocksIfElse);
        zDisplayDetailsAboutBlockIfElseExtended(listOfPairedBlocksIfElse, "IFELSE", ihy);

        aReconstructSelectedMethod(cg, mgOld, listOfPairedBlocksIfElse, listOfPairedBlocksFOR);

    }

    public static void aFillAListCreateForItem(
            InstructionHandle[] ihy, ArrayList<CreateForItem> cfi,
            ArrayList<BranchHandleItem> branches, ArrayList<BranchHandleItem> ifElseBlocks) {


        for (int i = branches.size() - 1; i >= 0; i--) {

            BranchHandleItem item = branches.get(i);

            if (item.getSignature().equals("goto")) {
                if (item.getPosition() > item.getPositionToJump()) {

                    CreateForItem temp = new CreateForItem();

                    int firstPrevStore = zGetFirstPrevIdBySignature(ihy, item.getIdPositionToJump(), "storeprev");
                    temp.setPrevStore(ihy[firstPrevStore]);
                    temp.setStartLoop(ihy[item.getIdPositionToJump()]);
                    temp.setGOTO(item);
                    temp.setINC(item.getInstruction().getPrev());
                    cfi.add(temp);

                } else ifElseBlocks.add(item);
            }
        }

//        ROZPOZNAJ PRAWIDLOWE FORY
        for (CreateForItem createForItem : cfi) {
            for (BranchHandleItem item : branches) {
                if (item.getSignature().contains("if")) {
                    if (createForItem.getPrevStore() == ihy[item.getPrevStore()]) {
                        createForItem.addInstrIF(item);
                    }
                }
            }
        }

    }

    private static void aFillAListWithIfElseBlock(
            InstructionHandle[] ihy,
            ArrayList<BranchHandleItem> singleBrancheIF,
            ArrayList<BranchHandleItem> singleBrancheGOTO,
            ArrayList<CreateForItem> listOfPairedBlocksIfElse) {

//        petla zewnetrzna dotyczy tych if else ktore wystepuja samotnie
//        i nie naleza do sekcji FOR petli
//        jak sie temu przyjrzec to wystepuja dwa liczniki,
//        poniewaz mogą wystąpić bloki if-else lub if

        for (int i = singleBrancheIF.size() - 1, j = singleBrancheGOTO.size() - 1; i >= 0; i--, j--) {

            CreateForItem item = new CreateForItem();
            if (j >= 0) item.setGOTO(singleBrancheGOTO.get(j));

            int prev = zGetFirstPrevIdBySignature(ihy, singleBrancheIF.get(i).getIdPosition(), "storeprev");
            int load = zGetFirstPrevIdBySignature(ihy, singleBrancheIF.get(i).getIdPosition(), "load");

            item.setPrevStore(ihy[prev]);
            item.setStartLoop(ihy[load]);
            item.addInstrIF(singleBrancheIF.get(i));

            listOfPairedBlocksIfElse.add(item);
        }
    }

    public static int aGetStartForSingleBlockIf(InstructionHandle[] ihy, int idPoistionIf) {

        ArrayList listInstruction2Fold = new ArrayList(Arrays.asList("add", "div", "mul", "or", "sub", "xor"));
        ArrayList listInstruction1Fold = new ArrayList(Arrays.asList("rem", "neg"));

        boolean stop = false;
        int numberOfLOAD = 0, i;

//        instrukcje load i const sa zaliczane do tej samej grupy: "przekazanie" wartości czyli [LOAD]
//        licze wystapienia instrukcji load, zawsze jest to >= 1

        for (i = idPoistionIf - 1; !stop; i--) {

            String signature = ihy[i].getInstruction().getName();
            signature = signature.substring(1);

            if (listInstruction2Fold.contains(signature)) numberOfLOAD += 2;
            else if (listInstruction1Fold.contains(signature)) numberOfLOAD++;
            else if (signature.contains("const")) numberOfLOAD--;
            else if (signature.contains("load")) numberOfLOAD--;

//            at least two instructions must be taken, one type 'load' and one type 'store'
            if (!(i == idPoistionIf - 1) && numberOfLOAD == 0 && i < idPoistionIf - 2) stop = true;

        }
        return i + 1;
    }

    public static void aReconstructSelectedMethod(
            ClassGen cg, MethodGen mgOld,
            ArrayList<CreateForItem> listOfPairedBlocksIfElse,
            ArrayList<CreateForItem> listOfPairedBlocksFOR) {


        ConstantPoolGen cp = cg.getConstantPool();
        InstructionFactory factory = new InstructionFactory(cg, cg.getConstantPool());
        InstructionHandle[] ihy = mgOld.getInstructionList().getInstructionHandles();
        InstructionList il = new InstructionList();


        MethodGen mgNew = new MethodGen(Const.ACC_PUBLIC | Const.ACC_STATIC,
                Type.INT, Type.NO_ARGS, new String[]{}, "testowo", null, il, cp);

        reCreateVariables(mgOld, mgNew, cp, il);

//        InstructionHandle ih_61 = il.append(factory.createFieldAccess(
//                "java.lang.System", "out", new ObjectType("java.io.PrintStream"), Const.GETSTATIC));
//        il.append(InstructionFactory.createLoad(Type.INT, 3));
//        il.append(factory.createInvoke("java.io.PrintStream", "println", Type.VOID, new Type[]{Type.INT}, Const.INVOKEVIRTUAL));

//       ****************************************

        CreateForItem przyklad = listOfPairedBlocksIfElse.get(0);
        int start = przyklad.getInstrIF().get(0).getIdPosition();
        int poprawione = aGetStartForSingleBlockIf(ihy, start);

        for (int i = poprawione; i < start; i++) il.append(ihy[i].getInstruction());
        il.append(new ISTORE(2));
//       ****************************************


        il.append(new ILOAD(1));
        il.append(InstructionFactory.createReturn(Type.INT));

        mgNew.setInstructionList(il);
        mgNew.setMaxLocals();
        mgNew.setMaxStack();
        cg.addMethod(mgNew.getMethod());
        il.dispose();

    }

    public static int getIdFromHandlePosition(InstructionHandle[] ihy, int position) {
        for (int i = 0; i < ihy.length; i++) if (ihy[i].getPosition() == position) return i;
        return -1;
    }

    public static void reCreateVariables(MethodGen mgOld, MethodGen mgNew, ConstantPoolGen cp, InstructionList il) {
        LocalVariable[] lvt = mgOld.getLocalVariableTable(cp).getLocalVariableTable();
        ArrayList<LocalVariableGen> lista = new ArrayList<>();

        for (LocalVariable localVariable : lvt) {
            lista.add(mgNew.addLocalVariable(
                    localVariable.getName(), Type.getType(localVariable.getSignature()), null, null));
        }

//      the value 0 is universal enough to fit almost all types of variables

        for (LocalVariableGen localVariableGen : lista) {
            il.append(new PUSH(cp, 0));
            localVariableGen.setStart(il.append(new ISTORE(localVariableGen.getIndex())));
        }
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
        String[] string = ih.toString().split("->");
        int position = Integer.parseInt(string[1].replace(" ", ""));
        bhi.setPositionToJump(position);
        bhi.setIdPositionToJump(hmPositionId.get(position));
        bhi.setPosition(ih.getPosition());
        int idPosition = hmPositionId.get(ih.getPosition());
        bhi.setIdPosition(idPosition);
        bhi.setInstruction(ih);
        bhi.setSignature(ih.getInstruction().getName());
        bhi.setPrevStore(zGetFirstPrevIdBySignature(ihy, idPosition, "storeprev"));
        bhi.setPrevLoad(zGetFirstPrevIdBySignature(ihy, idPosition, "load"));

        return bhi;
    }

    public static void zDisplayAListCreateForItemsExtended(
            ArrayList<CreateForItem> cfi, String label, InstructionHandle[] ihy) {

        for (int i = 0; i < cfi.size(); i++) {

            CreateForItem item = cfi.get(i);

            System.out.println(label + " nr: " + i);
            System.out.println("liczba instrukcji IF w FOR: " + item.getInstrIF().size());

            if (item.getInstrIF().size() > 0) {
                for (int j = 0; j < item.getInstrIF().size(); j++) {
                    System.out.println(item.getInstrIF().get(j).getInstruction());
                }
            }

            int idFirstInFor = getIdFromHandlePosition(ihy, item.getPrevStore().getPosition());

            int idStartLoop = getIdFromHandlePosition(ihy, item.getStartLoop().getPosition());

            System.out.println("\n\tINSTRUKCJE INICJALIZACJI FOR");
            for (int j = idFirstInFor; j < idStartLoop; j++) System.out.println("\t\t" + ihy[j]);

            int countIFinFOR = item.getInstrIF().size();
            int lastIFinFOR = item.getInstrIF().get(countIFinFOR - 1).getIdPosition();
            System.out.println("\tINSTRUKCJE WARUNKOWE W FOR");
            for (int j = idStartLoop; j <= lastIFinFOR; j++) System.out.println("\t\t" + ihy[j]);

            System.out.println("\tPOZOSTAŁE INFORMACJE");

            if (item.getIncrement() != null) System.out.println("\t\t" + item.getIncrement());
            if (item.getGoToBH() != null) System.out.println("\t\t" + item.getGoToBH().getInstruction());

            System.out.println("\n***********************************************************\n");
        }

    }

    public static void zDisplayDetailsAboutBlockIfElseExtended(
            ArrayList<CreateForItem> listOfPairedBlocksIfElse, String label, InstructionHandle[] ihy) {
        for (int i = 0; i < listOfPairedBlocksIfElse.size(); i++) {

            CreateForItem temp = listOfPairedBlocksIfElse.get(i);

            System.out.print(label + " nr: " + i);

            int idPositionIF = temp.getInstrIF().get(0).getIdPosition();
            int positionInCode = temp.getInstrIF().get(0).getPosition();
            System.out.print("\t(id IF: " + idPositionIF + ", position IF in code: " + positionInCode + ")\n\n");
            System.out.println("\tINSTRUKCJE WARUNKU IF");
            int idFirstInstruction = aGetStartForSingleBlockIf(ihy, idPositionIF);
            boolean jump = temp.getGoToBH() != null;

            for (int ii = idFirstInstruction; ii < idPositionIF; ii++) System.out.println("\t\t" + ihy[ii]);

            int jumpToElse = temp.getInstrIF().get(0).getIdPositionToJump();

            System.out.println("\tINSTRUKCJE BLOKU IF:");
            for (int j = idPositionIF; j < jumpToElse; j++) System.out.println("\t\t" + ihy[j]);

            if (jump) {
                int backToCode = temp.getGoToBH().getIdPositionToJump();
                System.out.println("\tINSTRUKCJE BLOKU ELSE:");
                for (int j = jumpToElse; j < backToCode; j++) System.out.println("\t\t" + ihy[j]);
            }
            System.out.println("\n***********************************************************\n");
        }


    }

    private static ArrayList<BranchHandleItem> zGetAListSingleInstructionsIF(
            ArrayList<BranchHandleItem> bhi, ArrayList<CreateForItem> cfi) {

        ArrayList<BranchHandleItem> resultTemp = new ArrayList<>();
        ArrayList<BranchHandleItem> result = new ArrayList<>();

        for (BranchHandleItem branch : bhi) {
            for (CreateForItem createForItem : cfi) {
                ArrayList<BranchHandleItem> list = createForItem.getInstrIF();
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

        int i;
        boolean extraPrevFlag = false;

        if (signature.contains("prev")) {
            extraPrevFlag = true;
            signature = signature.replace("prev", "");
        }

        for (i = start - 1; !ihy[i].toString().contains(signature); i--) {
        }

        //        minus one because we want the id of the instruction before "store"
        if (extraPrevFlag) i -= 1;

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
