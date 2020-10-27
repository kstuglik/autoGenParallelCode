package pl.edu.agh.bcel.transformation;

import org.apache.bcel.generic.*;
import pl.edu.agh.bcel.nested.ElementFOR;
import pl.edu.agh.bcel.nested.ElementIF;
import pl.edu.agh.bcel.utils.ForLoopUtils;
import pl.edu.agh.bcel.utils.InstructionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class Structure {

    public static void caseMatrix(ClassGen cg, MethodGen mg) {

        InstructionList il = new InstructionList();
        ConstantPoolGen cp = cg.getConstantPool();
        InstructionHandle[] ihy = mg.getInstructionList().getInstructionHandles();

        ArrayList<ElementFOR> listElementsFOR = new ArrayList<>();
        ArrayList<ElementIF> listElementsIF = new ArrayList<>();
        ArrayList<Integer> listIdInstructionIfInside = new ArrayList<>();
        ArrayList<Integer> listIdInstructionIfOutside = new ArrayList<>();

        System.out.println("\n-------- PHASE FIRST --------\n");
        phaseFirst(cg, mg, listElementsFOR);

        System.out.println("\n-------- PHASE SECOND --------\n");
        phaseSecond(cg, mg, listElementsIF, listIdInstructionIfInside, listIdInstructionIfOutside);

        System.out.println("\n-------- PHASE THIRD --------\n");
        phaseThird(listIdInstructionIfInside, listElementsFOR);

        displayElementsFor(ihy, listElementsFOR);
//        displayElementsIf(ihy, listElementsIF);
        displayElementsIfOutside(ihy, listElementsIF, listIdInstructionIfOutside);

        Matrix.matrixSubtask(cg, mg, listElementsFOR, ihy);
        Matrix.matrixMultiply(cg, mg, listElementsFOR, ihy);

    }

    public static void caseNbody(ClassGen cg, MethodGen mg) {

        InstructionList il = new InstructionList();
        ConstantPoolGen cp = cg.getConstantPool();
        InstructionHandle[] ihy = mg.getInstructionList().getInstructionHandles();

        ArrayList<ElementFOR> listElementsFOR = new ArrayList<>();
        ArrayList<ElementIF> listElementsIF = new ArrayList<>();
        ArrayList<Integer> listIdInstructionIfInside = new ArrayList<>();
        ArrayList<Integer> listIdInstructionIfOutside = new ArrayList<>();

        System.out.println("\n-------- PHASE FIRST --------\n");
        phaseFirst(cg, mg, listElementsFOR);

        System.out.println("\n-------- PHASE SECOND --------\n");
        phaseSecond(cg, mg, listElementsIF, listIdInstructionIfInside, listIdInstructionIfOutside);

        System.out.println("\n-------- PHASE THIRD --------\n");
        phaseThird(listIdInstructionIfInside, listElementsFOR);

        displayElementsFor(ihy, listElementsFOR);
//        displayElementsIf(ihy, listElementsIF);
        displayElementsIfOutside(ihy, listElementsIF, listIdInstructionIfOutside);

        Nbody.nbodyMovies(cg, mg, listElementsFOR, ihy);
        Nbody.nbodySubtask(cg, mg, listElementsFOR, ihy);

    }

    public static void caseHistogram(ClassGen cg, MethodGen mg) {

        InstructionList il = new InstructionList();
        ConstantPoolGen cp = cg.getConstantPool();
        InstructionHandle[] ihy = mg.getInstructionList().getInstructionHandles();

        ArrayList<ElementFOR> listElementsFOR = new ArrayList<>();
        ArrayList<ElementIF> listElementsIF = new ArrayList<>();
        ArrayList<Integer> listIdInstructionIfInside = new ArrayList<>();
        ArrayList<Integer> listIdInstructionIfOutside = new ArrayList<>();

        System.out.println("\n-------- PHASE FIRST --------\n");
        phaseFirst(cg, mg, listElementsFOR);

        System.out.println("\n-------- PHASE SECOND --------\n");
        phaseSecond(cg, mg, listElementsIF, listIdInstructionIfInside, listIdInstructionIfOutside);

        System.out.println("\n-------- PHASE THIRD --------\n");
        phaseThird(listIdInstructionIfInside, listElementsFOR);

        displayElementsFor(ihy, listElementsFOR);
//        displayElementsIf(ihy, listElementsIF);
        displayElementsIfOutside(ihy, listElementsIF, listIdInstructionIfOutside);

        Histogram.histogramSubtask(cg, mg, listElementsFOR);
        Histogram.histogramCalculate(cg, mg, listElementsFOR);

    }

    public static void caseFFT(ClassGen cg, MethodGen mg) throws TargetLostException {

        InstructionList il = new InstructionList();
        ConstantPoolGen cp = cg.getConstantPool();
        InstructionHandle[] ihy = mg.getInstructionList().getInstructionHandles();

        ArrayList<ElementFOR> listElementsFOR = new ArrayList<>();
        ArrayList<ElementIF> listElementsIF = new ArrayList<>();
        ArrayList<Integer> listIdInstructionIfInside = new ArrayList<>();
        ArrayList<Integer> listIdInstructionIfOutside = new ArrayList<>();

        System.out.println("\n-------- PHASE FIRST --------\n");
        phaseFirst(cg, mg, listElementsFOR);

        System.out.println("\n-------- PHASE SECOND --------\n");
        phaseSecond(cg, mg, listElementsIF, listIdInstructionIfInside, listIdInstructionIfOutside);

        System.out.println("\n-------- PHASE THIRD --------\n");
        phaseThird(listIdInstructionIfInside, listElementsFOR);

        displayElementsFor(ihy, listElementsFOR);
//        displayElementsIf(ihy, listElementsIF);
//        displayElementsIfOutside(ihy,listElementsIF,listIdInstructionIfOutside);

//        kolejnosc ma znaczenie, druga z kolei metoda usuwa czesc instrukcji
        FFT.fftSubtask(cg, mg, listElementsFOR);

        FFT.fftMethod(cg, mg, listElementsFOR);

    }

    public static void phaseFirst(ClassGen cg, MethodGen mg, ArrayList<ElementFOR> listFORs) {

        InstructionHandle[] ihy = mg.getInstructionList().getInstructionHandles();
        HashMap<Integer, Integer> hmPositionId = InstructionUtils.getHashmapPositionId(mg);

        for (int idGoTo = 0; idGoTo < ihy.length; idGoTo++) {
            if (ihy[idGoTo].getInstruction().getName().contains("goto")) {
                String[] table = ihy[idGoTo].getInstruction().toString().split("\\W+|_");

                int position = Integer.parseInt(table[3]);
                int idJump = hmPositionId.getOrDefault(position, -1);

                if (idGoTo > idJump) {
                    int idPrevStore = zGetFirstPrevIdBySignature(ihy, idJump, "storeprev");
                    String[] string = ihy[idGoTo - 1].getInstruction().toString().split("\\W+|_");
                    int ile = string.length;
                    int idIterator = Integer.parseInt(string[ile - 2]);
                    int idInc = getIdIncForStatement(ihy, idGoTo);
                    ElementFOR item = new ElementFOR(null, idPrevStore, idJump, -1, idInc, idGoTo, idIterator);
                    listFORs.add(item);
                }
            }
        }
    }

    public static int getIdIncForStatement(InstructionHandle[] ihy, int idGoTo) {
        int idINC = idGoTo;
        InstructionHandle ih = ihy[idGoTo].getPrev();
        idINC -= 1;
        if (!ih.toString().contains("inc")) {
//            System.out.println("niestandardowy przypadek INC");
//            System.out.println("\t" + ihy[idGoTo].getPrev().getPrev().toString());
//            trzeba cofac dotad az jest cos z LOAD/CONS
            ih = ih.getPrev();
            idINC -= 1;
//            System.out.println("\t\t"+ih);

            for (int i = idINC; ; i--) {

                ih = ih.getPrev();
                if (!ih.toString().contains("load")) {
                    if (!ih.toString().contains("const")) {
                        break;
                    }
                }
                idINC -= 1;
//                System.out.println("\t\t" + ih);
            }
        }

        System.out.println("\tidINC = " + idINC + ",\t" + ihy[idINC]);
        return idINC;
    }

    public static void phaseSecond(
            ClassGen cg, MethodGen mg,
            ArrayList<ElementIF> listElementsIF,
            ArrayList<Integer> listIdInstructionIfInside,
            ArrayList<Integer> listIdInstructionIfOutside) {

        InstructionHandle[] ihy = mg.getInstructionList().getInstructionHandles();
        HashMap<Integer, Integer> hashmapWithPositionAndIdInstruction = InstructionUtils.getHashmapPositionId(mg);
        int counter = 1;

        System.out.println("IF-INSTRUCTION IN METHOD:\n\tihy - InstructionHandle[]");
        for (int i = 0; i < ihy.length; i++) {

            int last, position, idJump;
            int last2, position2, idJump2;
            int idPrevLoad;
            ElementIF elementIf;

            if (ihy[i].getInstruction().getName().contains("if")) {
                System.out.print("\n" + (counter++) + ")\tihy[" + i + "],\t" + ihy[i].getInstruction() + ",\t");
                String[] table = ihy[i].getInstruction().toString().split("\\W+|_");
                last = table.length - 1;
                position = Integer.parseInt(table[last]);
                idJump = hashmapWithPositionAndIdInstruction.getOrDefault(position, -1);
                idPrevLoad = ForLoopUtils.aGetStartForBlockIf(ihy, i);//ta metoda daje +1 wiec pamietaj
                elementIf = new ElementIF(i, idJump - 1, idJump, idPrevLoad);

                listElementsIF.add(elementIf);


                if (ihy[idJump].getPrev().getInstruction().getName().contains("goto")) {

                    String[] table2 = ihy[idJump].getPrev().getInstruction().toString().split("\\W+|_");
                    last2 = table2.length - 1;
                    position2 = Integer.parseInt(table2[last2]);
                    idJump2 = hashmapWithPositionAndIdInstruction.getOrDefault(position2, -1);
                    if (idJump < idJump2) {
                        System.out.print("it's IF-ELSE-block");
                        listIdInstructionIfOutside.add(i);
                    } else {
                        System.out.print("it's IF inside FOR-loop");
                        listIdInstructionIfInside.add(i);
                    }
                } else {
                    System.out.print("it's IF-block");
                    listIdInstructionIfOutside.add(i);
                }
            }
        }
    }

    private static void phaseThird(ArrayList<Integer> listWithIdInstructionIF, ArrayList<ElementFOR> listElementsFOR) {

        ElementFOR elementFor;
        int idInstructionIf;
        ArrayList<Integer> listIdInstructionsIfInUse = new ArrayList<>();

        System.out.println("ALL ELEMENTS: " + listWithIdInstructionIF);
        for (int i = listWithIdInstructionIF.size() - 1; i >= 0; i--) {
            for (int j = 0; j < listElementsFOR.size(); j++) {
                elementFor = listElementsFOR.get(j);
                idInstructionIf = listWithIdInstructionIF.get(i);
                if (idInstructionIf == 93) {
                    System.out.println("\t\t\t! SUZUKI - to ten gogus!");
                }
                if (idInstructionIf >= elementFor.getIdPrevLoad() && idInstructionIf < elementFor.getIdInc()) {
                    if (!listIdInstructionsIfInUse.contains(idInstructionIf)) {
                        listIdInstructionsIfInUse.add(idInstructionIf);
                        elementFor.addIdInstructionIfIntoThisList(idInstructionIf);
                    }
                }
            }
        }

        for (ElementFOR elementFOR : listElementsFOR) {
            elementFOR.sortListWithElementsIF();
            elementFOR.setFirstInside();
        }

        listElementsFOR.sort(Comparator.comparing(ElementFOR::getIdPrevStore));

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

    public static void moreNested(ClassGen cg, MethodGen mg) {

        InstructionList il = new InstructionList();
        ConstantPoolGen cp = cg.getConstantPool();
        InstructionHandle[] ihy = mg.getInstructionList().getInstructionHandles();

        ArrayList<ElementFOR> listElementsFOR = new ArrayList<>();
        ArrayList<ElementIF> listElementsIF = new ArrayList<>();
        ArrayList<Integer> listIdInstructionIfInside = new ArrayList<>();
        ArrayList<Integer> listIdInstructionIfOutside = new ArrayList<>();

        System.out.println("\n-------- PHASE FIRST --------\n");
        phaseFirst(cg, mg, listElementsFOR);

        System.out.println("\n-------- PHASE SECOND --------\n");
        phaseSecond(cg, mg, listElementsIF, listIdInstructionIfInside, listIdInstructionIfOutside);

        System.out.println("\n-------- PHASE THIRD --------\n");
        phaseThird(listIdInstructionIfInside, listElementsFOR);

        displayElementsFor(ihy, listElementsFOR);
//        displayElementsIf(ihy, listElementsIF);
        displayElementsIfOutside(ihy, listElementsIF, listIdInstructionIfOutside);
    }

    private static void displayElementsFor(InstructionHandle[] ihy, ArrayList<ElementFOR> listFORs) {
        int i = 0;
        System.out.println("\n\n" + "DETECTED: FOR-loops-instructions" + "\n");
        for (ElementFOR elementFor : listFORs) {
            System.out.println("\n***************** FOR-" + (i++) + " ***************** ");
            elementFor.displayElementFor(ihy);
        }
    }

    private static void displayElementsIf(InstructionHandle[] ihy, ArrayList<ElementIF> listIFs) {
        int i = 0;
        System.out.println("\n\n" + "DETECTED: IF-all-instructions" + "\n");
        for (ElementIF ifElement : listIFs) {
            System.out.println("\n***************** IF-ELSE-" + (i++) + " ***************** ");
            ifElement.displayElementIf(ihy);
        }
    }

    private static void displayElementsIfOutside(
            InstructionHandle[] ihy,
            ArrayList<ElementIF> listIFs,
            ArrayList<Integer> listIdInstructionIfOutside) {
        int counter = 0;
        System.out.println("\n\n" + "DETECTED: IF-outside-instructions" + "\n");
        for (int i = 0; i < listIdInstructionIfOutside.size(); i++) {
            int id = listIdInstructionIfOutside.get(counter);
            ElementIF ifElement = getElementIfWithId(listIFs, id);
            System.out.println("\n***************** IF-outside-" + (counter++) + " ***************** ");
            ifElement.displayElementIf(ihy);
        }
    }

    private static ElementIF getElementIfWithId(ArrayList<ElementIF> listElementsIF, int id) {
        for (int i = 0; i < listElementsIF.size(); i++) {
            if (listElementsIF.get(i).getId() == id) return listElementsIF.get(i);
        }
        return null;
    }

}