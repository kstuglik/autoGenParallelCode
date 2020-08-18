package pl.edu.agh.bcel;

public class LaunchProperties {

    public static final String CLASS_SUFFIX = ".class";
    public static final String JAVA_SUFFIX = ".java";

    /*PARALLEL PARAMS*/
    public static final String NUMBER_OF_THREADS_NAME = "NUM_THREADS";
    public static final String EXECUTOR_SERVICE_NAME = "SERVICE";
    public static final String START_INDEX_VAR_NAME = "start";
    public static final String END_INDEX_VAR_NAME = "end";
    // others??
    public static final String END_FINAL_INDEX_VAR_NAME = "endFinal";
    public static final String SUBTASK_METHOD_NAME = "SubTask";
    public static final String LOOP_ITERATOR_NAME = "i";
    public static final String GOTO_INSTRUCTION_NAME = "goto";
    public static final String DATASIZE_VAR_NAME = "dataSize";
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
    public static final String TASK_POOL_NAME = "var55";
    public static short SIZE_OF_PROBLEM;

    public static String CLASS_DIR = "target/classes/matrix/";
    public static String MODIFICATION_SUFFIX = "_MOD";
    public static String CLASS_NAME;
    public static String CLASS_METHOD;
    public static String PATH_TO_INTPUT_FILE;
    public static String PATH_TO_OUTPUT_FILE;
    //    WARNING: task polll is generic list, my variable has name: var55 and the same id
    public static int TASK_POOL_ID = 55;
    public static String ROW_NUM_VAR_NAME = "rowNum";
    public static String COL_NUM_VAR_NAME = "colNum";
    public static String STEP_VAR_NAME = "step";
    public static String CURR_ROW_VAR_NAME = "currRow";
    public static String MATRIX_A_VAR_NAME = "matrixA";
    public static String MATRIX_B_VAR_NAME = "matrixB";
    public static String MATRIX_C_VAR_NAME = "matrixC";



    public static String getPathToIntputFile() {
        return CLASS_DIR + CLASS_NAME + CLASS_SUFFIX;
    }

    public static String getPathToOutputFile() {
        return CLASS_DIR + CLASS_NAME + MODIFICATION_SUFFIX + CLASS_SUFFIX;
    }
}
