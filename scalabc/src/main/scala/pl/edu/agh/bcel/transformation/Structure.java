package pl.edu.agh.bcel.transformation;

import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.TargetLostException;
import pl.edu.agh.bcel.nested.ElementFOR;
import pl.edu.agh.bcel.nested.ElementIF;
import pl.edu.agh.bcel.utils.ForLoopUtils;
import pl.edu.agh.bcel.utils.InstructionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class Structure {

    public static void selectBaseCase(ClassGen cg, MethodGen mg, String signature) throws TargetLostException {

        InstructionHandle[] ihy = mg.getInstructionList().getInstructionHandles();

        ArrayList<ElementFOR> listElementsFOR = new ArrayList<>();
        ArrayList<ElementIF> listElementsIF = new ArrayList<>();


        System.out.println("\n-------- PHASE FIRST --------\n");
        phaseFirst(cg, mg, listElementsFOR);

        System.out.println("\n-------- PHASE SECOND --------\n");
        phaseSecond(mg, listElementsIF);

        System.out.println("\n-------- PHASE THIRD --------\n");
        phaseThird(listElementsIF, listElementsFOR);

        moveIdIfFromForStatementToLoop(listElementsFOR,listElementsIF);

//        ElementFOR.displayElementsFor(ihy, listElementsFOR);
//        ElementIF.displayElementsIf(ihy, listElementsIF);

        switch (signature) {
            case "matrix":
                Matrix.matrixSubtask(cg, mg, listElementsFOR, ihy);
                Matrix.matrixMultiply(cg, mg, listElementsFOR, ihy);
                break;
            case "nbody":
                Nbody.nbodyMovies(cg, mg, listElementsFOR, ihy);
                Nbody.nbodySubtask(cg, mg, listElementsFOR, ihy);
                break;
            case "fft":
                FFT.fftSubtask(cg, mg, listElementsFOR);
                FFT.fftMethod(cg, mg, listElementsFOR, listElementsIF);
                break;
            case "histogram":
                Histogram.histogramSubtask(cg, mg, listElementsFOR);
                Histogram.histogramCalculate(cg, mg, listElementsFOR);
                break;
        }

    }

    public static void phaseFirst(ClassGen cg, MethodGen mg, ArrayList<ElementFOR> listFORs) {

        System.out.println("list of the ElementFor is created");
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
                    int length = string.length;
                    int idIterator = Integer.parseInt(string[length - 2]);
                    int idInc = getIdIncForStatement(ihy, idGoTo);
                    ElementFOR item = new ElementFOR(null, idPrevStore, idJump, -1, idInc, idGoTo, idIterator);
                    listFORs.add(item);
                }
            }
        }
    }

    public static void phaseSecond(MethodGen mg, ArrayList<ElementIF> listElementsIF) {

        InstructionHandle[] ihy = mg.getInstructionList().getInstructionHandles();
        HashMap<Integer, Integer> hashmapWithPositionAndIdInstruction = InstructionUtils.getHashmapPositionId(mg);
        int counter = 1;

        System.out.println("list of the ElementIf is created\nclassification: inside/outside ElementFor, if/if-else");
//        System.out.println("IF-INSTRUCTION IN METHOD:\n\tihy - InstructionHandle[]");
        for (int i = 0; i < ihy.length; i++) {

            int last, position, idJump;
            int last2, position2, idJump2;
            int idPrevLoad;
            ElementIF elementIf;

            if (ihy[i].getInstruction().getName().contains("if")) {
//                System.out.print("\n" + (counter++) + ")\tihy[" + i + "],\t" + ihy[i].getInstruction() + ",\t");
                String[] table = ihy[i].getInstruction().toString().split("\\W+|_");
                last = table.length - 1;
                position = Integer.parseInt(table[last]);
                idJump = hashmapWithPositionAndIdInstruction.getOrDefault(position, -1);
                idPrevLoad = ForLoopUtils.aGetStartForBlockIf(ihy, i);//ta metoda daje +1 wiec pamietaj
                elementIf = new ElementIF(i, idJump - 1, idJump, idPrevLoad);

                if (ihy[idJump].getPrev().getInstruction().getName().contains("goto")) {

                    String[] table2 = ihy[idJump].getPrev().getInstruction().toString().split("\\W+|_");
                    last2 = table2.length - 1;
                    position2 = Integer.parseInt(table2[last2]);
                    idJump2 = hashmapWithPositionAndIdInstruction.getOrDefault(position2, -1);
                    if (idJump < idJump2) {
                        elementIf.setSignature("if-else");
                        elementIf.setInsideElementFor(false);
                    } else {
                        elementIf.setSignature("if-in-for-statement");
                        elementIf.setInsideElementFor(true);
                    }
                } else {
                    elementIf.setSignature("if");
                    elementIf.setInsideElementFor(false);
                }

                listElementsIF.add(elementIf);

            }
        }
    }

    private static void phaseThird(ArrayList<ElementIF> listElementsIF, ArrayList<ElementFOR> listElementsFOR) {

        System.out.println("set ID from ElementIF for: ElementFor statement");

        ElementIF elementIF;
        int idInstructionIf;
        ArrayList<Integer> listIdInstructionsIfInUse = new ArrayList<>();

        for (int id = listElementsIF.size() - 1; id >= 0; id--) {
            for (ElementFOR elementFOR : listElementsFOR) {
                elementIF = listElementsIF.get(id);
                idInstructionIf = elementIF.getId();
                if (elementIF.getId() >= elementFOR.getIdPrevLoad() && elementIF.getId() < elementFOR.getIdInc()) {
                    if (!listIdInstructionsIfInUse.contains(idInstructionIf)) {
                        listIdInstructionsIfInUse.add(idInstructionIf);
                        elementFOR.addIdInstructionIfInsideForIntoList(idInstructionIf);
                    }
                }
            }
        }

        for (ElementFOR elementFOR : listElementsFOR) {
            elementFOR.sortListWithElementsIF();
            elementFOR.setFirstInside();
        }

        listElementsFOR.sort(Comparator.comparing(ElementFOR::getIdPrevStore));

        ElementFOR.setTypeOfNestedInListElementFor(listElementsFOR);

    }

    public static int getIdIncForStatement(InstructionHandle[] ihy, int idGoTo) {
        int idINC = idGoTo;
        InstructionHandle ih = ihy[idGoTo].getPrev();
        idINC -= 1;
        if (!ih.toString().contains("inc")) {
            ih = ih.getPrev();
            idINC -= 1;
            for (int i = idINC; ; i--) {
                ih = ih.getPrev();
                if (!ih.toString().contains("load") && !ih.toString().contains("const")) break;
                idINC -= 1;
            }
        }
        return idINC;
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

    public static void moveIdIfFromForStatementToLoop(ArrayList<ElementFOR> listElementsFOR,
                                                      ArrayList<ElementIF> listElementsIF) {
        for (ElementFOR elementFOR : listElementsFOR) {
            ArrayList<Integer> ifs = elementFOR.getListWithIdInstructionIfInsideFor();
            for (Integer anIf : ifs) {
                ElementIF elementIF = ElementIF.getElementIfWithId(listElementsIF, anIf);
                if (elementIF != null) {
                    if (elementFOR.getIdInc() == elementIF.getIdJump()) {
                        elementFOR.addIdInstructionIfInsideLoopIntoList(elementIF.getId());
                    }
                    if (elementFOR.getIdInsideLoop() < elementIF.getId()) {
                        elementFOR.addIdInstructionIfInsideLoopIntoList(elementIF.getId());
                    }
                }
            }

            ArrayList<Integer> listWithIdsIfInsideLoop = elementFOR.getListWithIdInstructionIfInsideLoop();
            ArrayList<Integer> listWithIdsIfInsideFor = elementFOR.getListWithIdInstructionIfInsideFor();

            for (Integer id : listWithIdsIfInsideLoop) listWithIdsIfInsideFor.remove(id);
            elementFOR.setListWithIdInstructionIfInsideFor(listWithIdsIfInsideFor);

        }
    }
}