//package pl.edu.agh.bcel.deprecated;
//
//import org.apache.bcel.Const;
//import org.apache.bcel.classfile.LocalVariable;
//import org.apache.bcel.generic.*;
//import pl.edu.agh.bcel.LaunchProperties;
//import pl.edu.agh.bcel.utils.ForLoopUtils;
//import pl.edu.agh.bcel.utils.TransformUtils;
//import pl.edu.agh.bcel.utils.VariableUtils;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//
//
//public class AST {
//
//    public static void aMoveSelectedForLoopIntoSubtask(
//            ClassGen cg, MethodGen mgOld, String nazwaSubtask, int firstFOR, int lastFOR) {
//
//        ArrayList<ForLoopNEW> listOfPairedBlocksFOR = new ArrayList<>();
//        ArrayList<ForLoopNEW> listOfPairedBlocksIfElse = new ArrayList<>();
//        ArrayList<ForLoopNEW> listAloneBlocksIfElse = new ArrayList<>();
//        InstructionList il = new InstructionList();
//        ConstantPoolGen cp = cg.getConstantPool();
//        InstructionFactory factory = new InstructionFactory(cg, cg.getConstantPool());
//
//
//        getLoopStructureIntoArrayLists(mgOld, listAloneBlocksIfElse, listOfPairedBlocksFOR, listOfPairedBlocksIfElse);
//
//        try {
//            int iloscPetli = listOfPairedBlocksFOR.size();
//            if (firstFOR > lastFOR || firstFOR < 0 || firstFOR > iloscPetli || lastFOR > iloscPetli) {
//                throw new Exception("\nWRONG PARAMS!\n" + "petli jest: " + iloscPetli +
//                        ", odtad min = 0, dotad max = " + (iloscPetli - 1));
//            } else {
////        method with 2 params
//                MethodGen mgNew = new MethodGen(Const.ACC_PUBLIC | Const.ACC_STATIC,
//                        Type.INT, Type.NO_ARGS, new String[]{}, nazwaSubtask, null, il, cp);
//                LocalVariableGen startVariable = mgNew.addLocalVariable(LaunchProperties.START_INDEX_VAR_NAME, Type.INT, null, null);
//                LocalVariableGen endVariable = mgNew.addLocalVariable(LaunchProperties.END_INDEX_VAR_NAME, Type.INT, null, null);
//
//                int ID_START = listOfPairedBlocksFOR.get(0).getIdInsideLoop();
//                int ID_STOP = listOfPairedBlocksFOR.get(0).getIdPositionINC();
//
//                InstructionList ilToAddInsideLoop = TransformUtils.setNewLoopBody(
//                        cg, mgOld, (short) 1000, ID_START, ID_STOP);
//
////      *****************************************************************************************
//                InstructionList ilApend = aReconstructSelectedForLoopSlave(
//                        listOfPairedBlocksFOR, firstFOR, lastFOR, true, cg, mgOld, mgNew);
////      *****************************************************************************************
//
//                il.append(ilApend);
//
//                mgOld.setInstructionList(il);
//                mgOld.setMaxLocals();
//                mgOld.setMaxStack();
//
//                cg.replaceMethod(mgOld.getMethod(), mgOld.getMethod());
//                il.dispose();
////                mgNew.setArgumentNames(new String[]{
////                        LaunchProperties.START_INDEX_VAR_NAME, LaunchProperties.END_INDEX_VAR_NAME});
//////                LoopUtilsOld.updateLoopStartCondition(il.getInstructionHandles(), startVariable.getIndex());
//////                LoopUtilsOld.updateLoopEndCondition(il.getInstructionHandles(), endVariable.getIndex());
////                mgNew.setInstructionList(il);
////                mgNew.setMaxLocals();
////                mgNew.setMaxStack();
////                cg.addMethod(mgNew.getMethod());
//
//            }
//        } catch (Exception e) {
//            System.err.println(e.getMessage());
//        }
//
//    }
//
//    public static InstructionList aReconstructSelectedForLoopSlave(
//            ArrayList<ForLoopNEW> listOfPairedBlocksFOR,
//            int odtad, int dotad, boolean flaga,
//            ClassGen cg, MethodGen mgOld, MethodGen mgNew) {
//
////      *****************************************************************************************
//        HashMap<Integer, ArrayList<BranchHandle>> hashmapIFinFOR = new HashMap<>();
//        HashMap<Integer, ArrayList<BranchInstruction>> hashmapGOTO = new HashMap<>();
//        HashMap<Integer, ArrayList<InstructionHandle>> hashmapNEXT = new HashMap<>();
//        HashMap<Integer, ArrayList<InstructionHandle>> hashmapSTART = new HashMap<>();
////      *****************************************************************************************
//        InstructionHandle[] ihy = mgOld.getInstructionList().getInstructionHandles();
//        InstructionFactory factory = new InstructionFactory(cg, cg.getConstantPool());
//        ConstantPoolGen cp = mgOld.getConstantPool();
////      *****************************************************************************************
//        HashMap<Integer, Integer> hashmapIdOldAndNewLVar = VariableUtils.getHashmapLVarIndexesOldAndNew(mgOld, mgNew);
////      *****************************************************************************************
//
//        System.out.println("odtad = " + odtad + ", dotad = " + dotad);
////        int BEFORE = listOfPairedBlocksFOR.get(0).getIdPositionPrevStore();
//
//        InstructionList il = new InstructionList();
//
//        for (int i = odtad; i <= dotad; i++) {
//
//            ForLoopNEW elementFor = listOfPairedBlocksFOR.get(i);
//            ArrayList<Integer> ids = elementFor.getIdsIFinFor();
//            ArrayList<Integer> idsls = elementFor.getIdsIFinForStartLoop();
//
//            System.out.println("ids[0] = " + ids.get(0) + ", " + ihy[ids.get(0)]);
//            int ile = ids.size();
//
//            ArrayList<BranchHandle> listaIFinFOR = new ArrayList<>();
//            ArrayList<BranchInstruction> listaGOTO = new ArrayList<>();
//            ArrayList<InstructionHandle> listaSTART = new ArrayList<>();
//
//
////        PART ONE: instrukcje z FORa
//            int idOstatniegoIfaWPetli = elementFor.getInstrIF().get(ile - 1).getIdPosition();
////            tutaj bylo mniejsze lub rowne ale nie wiem skad ta "niedokladnosc"
//            for (int j = elementFor.getIdPositionPrevStore(); j <= idOstatniegoIfaWPetli; j++) {
//                System.out.println("j = " + j + ", " + ihy[j].getInstruction().toString());
//                if (idsls.contains(j)) {
//                    System.out.println("doddano: loopstart: " + j);
//                    listaSTART.add(il.append(ihy[j].getInstruction()));
//                } else if (ids.contains(j)) {
//                    System.out.println("doddano: if: " + j);
//                    listaIFinFOR.add(ForLoopUtils.getBranchHandleIF(il, ihy[j]));
//                } else {
//                    System.out.println("doddano: inne: " + j);
//                    il.append(ihy[j].getInstruction());
//                }
//            }
//
//
//            int inc = elementFor.getIdPositionINC();
//            if (idOstatniegoIfaWPetli + 1 == inc) {
//                System.out.println("przypadek gdy INC jest ostatnim poleceniem w petli, tutaj next ==inc");
//            } else {
//                System.out.println("przypadek gdy INC NIE jest ostatnim poleceniem w petli, " +
//                        "\n\ttutaj next == to co po (chyba ze trzeba pomiedzy)");
//                for (int j = idOstatniegoIfaWPetli + 1; j < inc; j++) {
//                    System.out.println("dla j = " + j);
//                    il.append(ihy[j].getInstruction());
//                }
//            }
////        PART TWO: dodane instrukcje/lub odtworzone
////*********************************************************************************
//            il.append(factory.createPrintln("ahoj z tym wszystkim"));
////********************************************************************************
//            hashmapIFinFOR.put(i, listaIFinFOR);
//            hashmapGOTO.put(i, listaGOTO);
//            hashmapSTART.put(i, listaSTART);
//
//        }
//
//        for (int a = dotad; a >= odtad; a--) {
//
//            ForLoopNEW elementFor = listOfPairedBlocksFOR.get(a);
//            ArrayList<InstructionHandle> listaNEXT = new ArrayList<>();
//
////          *********************************************
//            int inc = elementFor.getIdPositionINC();
//            InstructionHandle ih;
//            String[] string = ihy[inc].getInstruction().toString().split("\\W+|_");
//
//            int idOLD = -1;
//            try {
//                idOLD = Integer.parseInt(string[1]);
//            } catch (Exception e) {
//                System.err.println("to jest blad idOld, idOld = " + idOLD + ", ih[inc] = " + ihy[inc].getInstruction().getName());
//            }
//            System.out.println("(kiedy one sie zaktualizowaly bo to jest nowe a pisze ze stare... idold = " + idOLD);
//            if (idOLD != -1) {
//                int idNEW = hashmapIdOldAndNewLVar.getOrDefault(idOLD, -1);
//                int incrementValue = Integer.parseInt(string[3]);
//                System.out.println("idnew = " + idNEW);
//                if (idNEW != -1) {
//                    ih = il.append(new IINC(idNEW, incrementValue));
//                } else {
//                    ih = il.append(new IINC(idOLD, incrementValue));
//                }
//                listaNEXT.add(ih);
//                hashmapNEXT.put(a, listaNEXT);
//            } else {
//                System.out.println("TUTAJ?B");
//                ih = il.append(ihy[inc].getInstruction());
//                listaNEXT.add(ih);
//                hashmapNEXT.put(a, listaNEXT);
//            }
//            System.out.println("TUTAJ?");
////          *************************************************
//
//            BranchInstruction gotobh = InstructionFactory.createBranchInstruction(
//                    Const.GOTO, hashmapSTART.get(a).get(0));
//            il.append(gotobh);
//            hashmapGOTO.get(a).add(gotobh);
//        }
//
//        InstructionHandle returnHandler = il.append(new PUSH(cp, 0));
//        il.append(InstructionFactory.createReturn(Type.INT));
//
//        for (int petlaNumer = odtad; petlaNumer <= dotad; petlaNumer++) {
//
//            ForLoopNEW elementFor = listOfPairedBlocksFOR.get(petlaNumer);
//            int ileIfow = elementFor.getInstrIF().size();
//            int positionInside = elementFor.getInstrIF().get(ileIfow - 1).getInstruction().getNext().getPosition();
//
//            for (int i = 0; i < ileIfow; i++) {
//                BranchHandleelementFor elementForif = elementFor.getInstrIF().get(i);
//
//                if (elementForif.getPositionToJump() == positionInside) {
//                    hashmapIFinFOR.get(petlaNumer).get(i).setTarget(hashmapSTART.get(petlaNumer).get(hashmapSTART.get(petlaNumer).size() - 1));
//                } else if (elementForif.getPositionToJump() > positionInside) {
//                    if (petlaNumer == odtad) hashmapIFinFOR.get(petlaNumer).get(i).setTarget(returnHandler);
//                    else hashmapIFinFOR.get(petlaNumer).get(i).setTarget(hashmapNEXT.get(petlaNumer - 1).get(0));
//                } else hashmapIFinFOR.get(petlaNumer).get(i).setTarget(hashmapSTART.get(petlaNumer).get(i + 2));
//            }
//
//        }
//
//        return il;
//    }
//
//    public static int getIdFromHandlePosition(InstructionHandle[] ihy, int position) {
//        for (int i = 0; i < ihy.length; i++) if (ihy[i].getPosition() == position) return i;
//        System.out.println("czy to tu?");
//        return -1;
//    }
//
//    public static int getPositionToJumpFromInstruction(InstructionHandle ih) {
//        String[] string = ih.toString().split("->");
//        return Integer.parseInt(string[1].replace(" ", ""));
//    }
//
//    public static void reCreateVariables(
//            MethodGen mgOld, MethodGen mgNew, ConstantPoolGen cp, InstructionList il) {
//        LocalVariable[] lvt = mgOld.getLocalVariableTable(cp).getLocalVariableTable();
//        ArrayList<LocalVariableGen> lista = new ArrayList<>();
//
//        for (LocalVariable variable : lvt) {
//            lista.add(
//                    mgNew.addLocalVariable(
//                            variable.getName(), Type.getType(
//                                    variable.getSignature()), il.getStart(), null));
//        }
//    }
//
//    public static int zGetFirstPrevIdBySignature(InstructionHandle[] ihy, int start, String signature) {
//
//        int i = start - 1;
//        boolean extraPrevFlag = false;
//
//        if (signature.contains("prev")) {
//            extraPrevFlag = true;
//            signature = signature.replace("prev", "");
//        }
//
//        while (!ihy[i].toString().contains(signature)) i--;
//
//        if (extraPrevFlag) i -= 1;//        we want the id of the instruction before "store"
//
//        return i;
//    }
//
//    public static HashMap<Integer, Integer> zGetHashmapPositionId(InstructionHandle[] ihy) {
//
//        HashMap<Integer, Integer> hashmapInstructionPositionId = new HashMap<>();
//
//        for (int i = 0; i < ihy.length; i++)
//            hashmapInstructionPositionId.put(ihy[i].getPosition(), i);
//
//        return hashmapInstructionPositionId;
//
//    }
//
//}
