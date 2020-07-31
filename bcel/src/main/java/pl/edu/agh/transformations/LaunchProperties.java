package pl.edu.agh.transformations;

import org.apache.bcel.generic.ObjectType;

public class LaunchProperties {

    public static String CLASS_DIR = "src/main/java/mbuilder/classFiles/";
    public static String MODIFICATION_SUFFIX = "_MOD";
    public static String CLASS_NAME;
    public static String CLASS_METHOD;

    public static final String CLASS_SUFFIX = ".class";
    public static final String JAVA_SUFFIX = ".java";

    public static final ObjectType p_stream = new ObjectType("java.io.PrintStream");
    public static final ObjectType i_stream = new ObjectType("java.io.InputStream");
    // others??

    /*PARALLEL PARAMS*/
    public static final String NUMBER_OF_THREADS_NAME = "NUM_THREADS";
    public static final String EXECUTOR_SERVICE_NAME = "SERVICE";

    //    WARNING: task polll is generic list, my variable has name: var55 and the same id
    public static int TASK_POOL_ID = 55;
    ;
    public static final String START_INDEX_VARIABLE_NAME = "start";
    public static final String END_INDEX_VARIABLE_NAME = "end";
    public static final String END_FINAL_INDEX_VARIABLE_NAME = "endFinal";
    public static final String SUBTASK_METHOD_NAME = "subTask";
    public static final String LOOP_ITERATOR_NAME = "i";
    public static final String GOTO_INSTRUCTION_NAME = "goto";
    public static final String DATASIZE_VARIABLE_NAME = "dataSize";

    public static final String ARRAY_1 = "var1";
    public static final String ARRAY_2 = "var2";

//    PARAMS TO JCUDA CALCULATION
    public static final String COUNT_COLS_IN_FIRST_ARRAY_NAME = "COLS_A";
    public static final String COUNT_ROWS_IN_FIRST_ARRAY_NAME = "ROWS_A";
    public static final String COUNT_COLS_IN_SECOND_ARRAY_NAME = "COLS_B";
    public static final String COUNT_ROWS_IN_SECOND_ARRAY_NAME = "ROWS_B";

    public static final int COLS_A = 3;
    public static final int ROWS_A = 3;
    public static final int COLS_B = 1;
    public static final int ROWS_B = 3;
}
