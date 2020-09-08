package pl.edu.agh.bcel.transformation;

import org.apache.bcel.generic.*;
import pl.edu.agh.bcel.nested.ElementFOR;
import pl.edu.agh.bcel.nested.ElementIF;
import pl.edu.agh.bcel.utils.ForLoopUtils;
import pl.edu.agh.bcel.utils.InstructionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Structure {

    public static void caseMatrix(ClassGen cg, MethodGen mg) {

        InstructionList il = new InstructionList();
        ConstantPoolGen cp = cg.getConstantPool();

        InstructionHandle[] ihy = mg.getInstructionList().getInstructionHandles();
        ArrayList<ElementFOR> listFORow = new ArrayList<>();
        ArrayList<ElementIF> samotneIfy = new ArrayList<>();
        ArrayList<ElementIF> blokiIfElse = new ArrayList<>();
        ArrayList<Integer> listaIfowDoUzupelnienia = new ArrayList<>();

        phaseFirst(cg, mg, listFORow);
        phaseSecond(cg, mg, samotneIfy, blokiIfElse, listaIfowDoUzupelnienia);
        phaseThird(listaIfowDoUzupelnienia, listFORow);


        System.out.println("rozpoznane petle for");
        for (ElementFOR foRek : listFORow) {
            foRek.displayItem(ihy);
        }

        System.out.println("rozpoznane bloki if-else");
        for (ElementIF efEk : blokiIfElse) {
            efEk.displayItem(ihy);
        }

        Matrix.matrixSubtask(cg, mg, listFORow, ihy);
        Matrix.matrixMultiply(cg, mg, listFORow, ihy);

    }

    public static void caseNbody(ClassGen cg, MethodGen mg) {

        InstructionList il = new InstructionList();
        ConstantPoolGen cp = cg.getConstantPool();

        InstructionHandle[] ihy = mg.getInstructionList().getInstructionHandles();
        ArrayList<ElementFOR> listFORow = new ArrayList<>();
        ArrayList<ElementIF> samotneIfy = new ArrayList<>();
        ArrayList<ElementIF> blokiIfElse = new ArrayList<>();
        ArrayList<Integer> listaIfowDoUzupelnienia = new ArrayList<>();

        phaseFirst(cg, mg, listFORow);
        phaseSecond(cg, mg, samotneIfy, blokiIfElse, listaIfowDoUzupelnienia);
        phaseThird(listaIfowDoUzupelnienia, listFORow);

        System.out.println("rozpoznane petle for");
        for (ElementFOR foRek : listFORow) {
            foRek.displayItem(ihy);
        }

        System.out.println("rozpoznane bloki if-else");
        for (ElementIF efEk : blokiIfElse) {
            efEk.displayItem(ihy);
        }

        Nbody.nbodyMovies(cg, mg, listFORow, ihy);
        Nbody.nbodySubtask(cg, mg, listFORow, ihy);

    }

    public static void caseHistogram(ClassGen cg, MethodGen mg) {

        InstructionList il = new InstructionList();
        ConstantPoolGen cp = cg.getConstantPool();

        InstructionHandle[] ihy = mg.getInstructionList().getInstructionHandles();
        ArrayList<ElementFOR> listFORow = new ArrayList<>();
        ArrayList<ElementIF> samotneIfy = new ArrayList<>();
        ArrayList<ElementIF> blokiIfElse = new ArrayList<>();
        ArrayList<Integer> listaIfowDoUzupelnienia = new ArrayList<>();

        phaseFirst(cg, mg, listFORow);
        phaseSecond(cg, mg, samotneIfy, blokiIfElse, listaIfowDoUzupelnienia);
        phaseThird(listaIfowDoUzupelnienia, listFORow);

        System.out.println("rozpoznane petle for");
        for (ElementFOR foRek : listFORow) {
            foRek.displayItem(ihy);
        }

//        System.out.println("rozpoznane bloki if-else");
//        for (ElementIF efEk : blokiIfElse) {
//            efEk.displayItem(ihy);
//        }

        Histogram.histogramSubtask(cg, mg, listFORow);
        Histogram.histogramCalculate(cg, mg, listFORow);

    }

    public static void phaseFirst(ClassGen cg, MethodGen mg, ArrayList<ElementFOR> listFORow) {

        InstructionHandle[] ihy = mg.getInstructionList().getInstructionHandles();
        HashMap<Integer, Integer> hmPozycjaId = InstructionUtils.getHashmapPositionId(mg);


        for (int i = 0; i < ihy.length; i++) {
            if (ihy[i].getInstruction().getName().contains("goto")) {
                String[] tablica = ihy[i].getInstruction().toString().split("\\W+|_");

                int pozycja = Integer.parseInt(tablica[3]);
                int idSkoku = hmPozycjaId.getOrDefault(pozycja, -1);

                if (i > idSkoku) {
                    int idPrevStore = zGetFirstPrevIdBySignature(ihy, idSkoku, "storeprev");
                    String[] string = ihy[i - 1].getInstruction().toString().split("\\W+|_");
                    int ile = string.length;
                    int idIteratora = Integer.parseInt(string[ile - 2]);
                    int idInc = zGetFirstPrevIdBySignature(ihy, i, "inc");
                    ElementFOR item = new ElementFOR(null, idPrevStore, idSkoku, -1, idInc, i, idIteratora);
                    listFORow.add(item);
                }
            }
        }
    }

    public static void phaseSecond(
            ClassGen cg, MethodGen mg, ArrayList<ElementIF> samotneIfy, ArrayList<ElementIF> blokiIfElse,
            ArrayList<Integer> listaIfowDoUzupelnienia) {

        InstructionHandle[] ihy = mg.getInstructionList().getInstructionHandles();
        HashMap<Integer, Integer> hmPozycjaId = InstructionUtils.getHashmapPositionId(mg);


        for (int i = 0; i < ihy.length; i++) {
            if (ihy[i].getInstruction().getName().contains("if")) {
                String[] tablica = ihy[i].getInstruction().toString().split("\\W+|_");
                int last = tablica.length - 1;
                int pozycja = Integer.parseInt(tablica[last]);
                int idSkoku = hmPozycjaId.getOrDefault(pozycja, -1);
//                System.out.println(i + ")\t" + Arrays.toString(tablica));
                if (ihy[idSkoku].getPrev().getInstruction().getName().contains("goto")) {

                    String[] tablica2 = ihy[idSkoku].getPrev().getInstruction().toString().split("\\W+|_");
                    int last2 = tablica2.length - 1;
                    int pozycja2 = Integer.parseInt(tablica2[last2]);
                    int idSkoku2 = hmPozycjaId.getOrDefault(pozycja2, -1);
                    if (idSkoku < idSkoku2) {
//                        System.out.println("\t\tto jest blok IF_ELSE");
                        int idPrevLoad = ForLoopUtils.aGetStartForBlockIf(ihy, i);//ta metoda daje +1 wiec pamietaj
                        ElementIF ifEk = new ElementIF(i, idSkoku - 1, idSkoku, idPrevLoad);
                        blokiIfElse.add(ifEk);
                    } else {
//                        System.out.println("\t\ttenif nalezy do petli FOR");
                        listaIfowDoUzupelnienia.add(i);
                    }
                } else {
//                    System.out.println("\tto NIE jest blok IF_ELSE");
                    int idPrevLoad = ForLoopUtils.aGetStartForBlockIf(ihy, i);//ta metoda daje +1 wiec pamietaj
                    ElementIF ifEk = new ElementIF(i, idSkoku - 1, idSkoku, idPrevLoad);
                    samotneIfy.add(ifEk);
                }
            }
        }
    }

    private static void phaseThird(ArrayList<Integer> listaIfowDoUzupelnienia, ArrayList<ElementFOR> listFORow) {
        //        ODWRACAM KOLEJNOSC
        Collections.reverse(listFORow);
        HashMap<Integer, ArrayList<Integer>> abc = new HashMap<>();

        for (int i = 0; i < listFORow.size(); i++) {

            ArrayList<Integer> temp = new ArrayList<>();

//            System.out.println("petla nr: " + i);
            int start = listFORow.get(i).getIdPrevLoad();

            for (int j = 0; j < listaIfowDoUzupelnienia.size(); j++) {
                int porownaj = listaIfowDoUzupelnienia.get(j);
                if (porownaj > start) {
                    temp.add(porownaj);
//                    System.out.println("i = " + i + ", j = " + j + ", " + porownaj);
                }
            }
            abc.put(i, temp);
        }

        for (int i = 0; i < abc.size() - 1; i++) {
            ArrayList<Integer> a = abc.get(i);
            ArrayList<Integer> b = abc.get(i + 1);
            a.removeAll(b);
            listFORow.get(i).setListaIfow(a);
        }

        System.out.println("abc.size() - 1 = " + (abc.size() - 1));
        listFORow.get(abc.size() - 1).setListaIfow(abc.get(abc.size() - 1));

        updateFirstInside(listFORow);

    }

    private static void updateFirstInside(ArrayList<ElementFOR> listFORow) {
        for (ElementFOR elementFOR : listFORow) {
            int ile = elementFOR.getListaIfow().size();
            int idLastIf = elementFOR.getListaIfow().get(ile - 1);
            elementFOR.setFirstInside(idLastIf + 1);
        }
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

}

