package pl.edu.agh.utils;

import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.ClassGen;
import pl.edu.agh.transformations.LaunchProperties;

import java.util.Arrays;
import java.util.Objects;

public class ConstantPoolUtils {

    public static int getFieldIndex(ClassGen cg, String constantName) {
        ConstantPool cp = cg.getConstantPool().getConstantPool();
        ConstantFieldref numThreadsField = Arrays.stream(cp.getConstantPool())
                .filter(ConstantFieldref.class::isInstance)
                .map(ConstantFieldref.class::cast)
                .filter((constant -> constantName.equals(getConstantName(cp, constant))))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Wrong state - constant " + constantName + " cannot be found."));
        for (int i = 1; i < cp.getConstantPool().length; i++) {
            if (Objects.equals(cp.getConstantPool()[i], numThreadsField)) {
                return i;
            }
        }
        return -1;
    }

    private static String getConstantName(ConstantPool cp, ConstantCP constant) {
        ConstantNameAndType constantNameAndType = (ConstantNameAndType) cp.getConstantPool()[constant.getNameAndTypeIndex()];
        return constantNameAndType.getName(cp);
    }

    static int getSubTaskMethodIndexInConstants(ClassGen cg) {
        ConstantPool cp = cg.getConstantPool().getConstantPool();
        ConstantMethodref subTaskMethod = Arrays.stream(cp.getConstantPool())
                .filter(ConstantMethodref.class::isInstance)
                .map(ConstantMethodref.class::cast)
                .filter(method -> LaunchProperties.SUBTASK_METHOD_NAME.equals(getConstantName(cp, method)))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("No subTask method found."));
        for (int i = 1; i < cp.getConstantPool().length; i++) {
            if (Objects.equals(cp.getConstantPool()[i], subTaskMethod)) {
                return i;
            }
        }
        return -1;
    }

    static int getInnerClassNameIndex(ClassGen cg, String innerClassName) {
//        ConstantPool cp = cg.getConstantPool().getConstantPool();
//        Arrays.stream(cp.getConstantPool())
//                .filter()
        return 0;
    }
}
