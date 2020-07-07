package pl.edu.agh.transformations.util;

import org.apache.bcel.Const;
import org.apache.bcel.generic.*;
import pl.edu.agh.transformations.LaunchProperties;

public class ExecutorUtils {

    public static void addExecutorInvocation(ClassGen cg, MethodGen mg) {

        ConstantPoolGen cp = cg.getConstantPool();
        InstructionFactory factory = new InstructionFactory(cg, cp);
        InstructionList il = mg.getInstructionList();
        InstructionHandle[] forLoop = LoopUtils.getForLoop(mg);
        InstructionHandle lastLoopHandle = forLoop[forLoop.length - 1];
        int executorIndex = ConstantPoolUtils.getFieldIndex(cg, LaunchProperties.EXECUTOR_SERVICE_CONSTANT_NAME);
        int tasksListIndex = LocalVariableUtils.findLocalVariableByName(LaunchProperties.TASK_POOL_NAME, mg.getLocalVariableTable(cp)).getIndex();
        InstructionList invokeInstructions = new InstructionList();
        invokeInstructions.append(new GETSTATIC(executorIndex));
        invokeInstructions.append(new ALOAD(tasksListIndex));
        invokeInstructions.append(factory.createInvoke(
                "java/util/concurrent/ExecutorService",
                "invokeAll",
                Type.getType("Ljava/util/List;"),
                new Type[]{Type.getType("Ljava/util/Collection;")},
                Const.INVOKEINTERFACE)
        );

        invokeInstructions.append(new POP());
        invokeInstructions.append(new GETSTATIC(executorIndex));
        invokeInstructions.append(factory.createInvoke(
                "java/util/concurrent/ExecutorService",
                "shutdown",
                Type.VOID,
                new Type[]{},
                Const.INVOKEINTERFACE)
        );

        il.append(lastLoopHandle, invokeInstructions);

        mg.setMaxLocals();
        cg.replaceMethod(mg.getMethod(), mg.getMethod());

    }
}
