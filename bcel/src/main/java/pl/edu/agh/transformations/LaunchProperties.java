package pl.edu.agh.transformations;

import org.apache.bcel.generic.ObjectType;

public class LaunchProperties {

    public static String CLASS_DIR = "src/main/java/mbuilder/classFiles/";

    public static String MODIFICATION_SUFFIX = "_MOD";
    public static final String CLASS_SUFFIX = ".class";
    public static final String JAVA_SUFFIX = ".java";

    public static String CLASS_NAME;
    public static String CLASS_METHOD;

    public static final String ERR_MESSAGE = "IT WAS NOT POSSIBLE to add a new piece of code!\n\t";


    /*JCUDA PARAMS*/
    public static int CHOICE = 3;
    public static int OPTION = 4;

    public static final String ARRAY_1 = "var1";
    public static final String ARRAY_2 = "var2";

    public static final String DIM = "2D";

    private static final ObjectType i_stream = new ObjectType("java.io.InputStream");
    public static final ObjectType p_stream = new ObjectType("java.io.PrintStream");

    /*PARALLEL PARAMS*/
    public static final String NUMBER_OF_THREADS_NAME = "NUM_THREADS";
    public static final String EXECUTOR_SERVICE_NAME = "SERVICE";
    public static final String START_RANGE_NAME = "START_RANGE";
    public static final String END_RANGE_NAME = "END_RANGE";
    public static final String TASK_POOL_NAME = "tasks";
    public static final String RESULTS_POOL_NAME = "partialResults";
    public static final String START_INDEX_VARIABLE_NAME = "start";
    public static final String END_INDEX_VARIABLE_NAME = "end";
    public static final String SUBTASK_METHOD_NAME = "subTask";
    public static final String LOOP_ITERATOR_NAME = "i";

    public static final String GOTO_INSTRUCTION_NAME = "goto";

}
