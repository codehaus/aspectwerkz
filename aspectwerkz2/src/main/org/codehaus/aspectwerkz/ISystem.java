package org.codehaus.aspectwerkz;

import org.codehaus.aspectwerkz.aspect.management.AspectManager;
import org.codehaus.aspectwerkz.metadata.CflowMetaData;
import org.codehaus.aspectwerkz.definition.expression.Expression;

/**
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public interface ISystem {
    /**
     * The UUID of the single system if only one definition is used.
     */
    String DEFAULT_SYSTEM = "default";

    /**
     * Initializes the system. The initialization needs to be separated fromt he construction of the manager, and is
     * triggered by the runtime system
     */
    void initialize();

    /**
     * Returns the ordered aspect managers for this system.
     *
     * @return the aspect manager
     */
    AspectManager[] getAspectManagers();

    /**
     * Registers entering of a control flow join point.
     *
     * @param metaData the classname:methodMetaData metaData
     */
    void enteringControlFlow(CflowMetaData metaData);

    /**
     * Registers exiting from a control flow join point.
     *
     * @param metaData the classname:methodMetaData metaData
     */
    void exitingControlFlow(CflowMetaData metaData);

    /**
     * Checks if we are in the control flow of a specific cflow pointcut.
     *
     * @param cflowExpression the cflow expression
     * @return boolean
     */
    boolean isInControlFlowOf(Expression cflowExpression);

    public ClassLoader getDefiningClassLoader();

    public AspectManager getAspectManager(String uuid);

    public AspectManager getAspectManager(int aspectManagerIndex);

}
