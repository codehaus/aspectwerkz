/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

import java.util.Collection;
import java.util.List;
import java.lang.reflect.Method;

import org.codehaus.aspectwerkz.metadata.ClassNameMethodMetaDataTuple;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.FieldMetaData;
import org.codehaus.aspectwerkz.regexp.PointcutPatternTuple;
import org.codehaus.aspectwerkz.regexp.ClassPattern;

/**
 * Interface for the different system types to implement.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface System {

    /**
     * The UUID of the single AspectWerkz system if only one definition is used.
     */
    public static final String DEFAULT_SYSTEM = "default";

    /**
     * The path to the definition file.
     */
    boolean START_REMOTE_PROXY_SERVER = "true".equals(
            java.lang.System.getProperty("aspectwerkz.remote.server.run", "false")
    );

    /**
     * Initializes the system.
     */
    void initialize();

    /**
     * Checks if the definition is of type attribute definition.
     *
     * @return boolean
     */
    boolean isAttribDef();

    /**
     * Checks if the definition is of type XML definition.
     *
     * @return boolean
     */
    boolean isXmlDef();

    /**
     * Returns the aspect for the name specified.
     *
     * @param name the name of the aspect
     * @return the aspect
     */
    AspectMetaData getAspectMetaData(String name);

    /**
     * Returns the aspect for the class pattern specified.
     *
     * @param classPattern the class pattern
     * @return the aspect
     */
    AspectMetaData getAspectMetaData(ClassPattern classPattern);

    /**
     * Returns a list with all the aspects.
     *
     * @return the aspects
     */
    Collection getAspectsMetaData();

    /**
     * Returns the method pointcut list for the class and method specified.
     * Caches the list, needed since the actual method call is expensive
     * and is made each time a new instance of an advised class is created.
     *
     * @param classMetaData the meta-data for the class
     * @param methodMetaData meta-data for the method
     * @return the pointcuts for this join point
     */
    List getMethodPointcuts(ClassMetaData classMetaData, MethodMetaData methodMetaData);

    /**
     * Returns the get field pointcut list for the class and field specified.
     * Caches the list, needed since the actual method call is expensive
     * and is made each time a new instance of an advised class is created.
     *
     * @param classMetaData the meta-data for the class
     * @param methodMetaData meta-data for the method
     * @return the pointcuts for this join point
     */
    List getGetFieldPointcuts(ClassMetaData classMetaData, FieldMetaData fieldMetaData);

    /**
     * Returns the set field pointcut list for the class and field specified.
     * Caches the list, needed since the actual method call is expensive
     * and is made each time a new instance of an advised class is created.
     *
     * @param classMetaData the meta-data for the class
     * @param methodMetaData meta-data for the method
     * @return the pointcuts for this join point
     */
    List getSetFieldPointcuts(ClassMetaData classMetaData, FieldMetaData fieldMetaData);

    /**
     * Returns the throws pointcut list for the class and method specified.
     * Caches the list, needed since the actual method call is expensive
     * and is made each time a new instance of an advised class is created.
     *
     * @param classMetaData the meta-data for the class
     * @param methodMetaData meta-data for the method
     * @return the pointcuts for this join point
     */
    List getThrowsPointcuts(ClassMetaData classMetaData, MethodMetaData methodMetaData);

    /**
     * Returns the caller side pointcut list for the class and method specified.
     * Caches the list, needed since the actual method call is expensive
     * and is made each time a new instance of an advised class is created.
     *
     * @param className the class name
     * @param methodMetaData meta-data for the method
     * @return the pointcuts for this join point
     */
    List getCallerSidePointcuts(String className, MethodMetaData methodMetaData);

    /**
     * Returns a list with the cflow pointcuts that affects the join point with the
     * class name and the method name specified.
     *
     * @param className the name of the class for the join point
     * @param methodMetaData the meta-data for the method for the join point
     * @return a list with the cflow pointcuts
     */
    List getCFlowPointcuts(String className, MethodMetaData methodMetaData);

    /**
     * Returns the index for a specific name to advice mapping.
     *
     * @param name the name of the advice
     * @return the index of the advice
     */
    IndexTuple getAdviceIndexFor(String name);

    /**
     * Retrieves a specific introduction based it's index.
     *
     * @param index the index of the introduction
     * @return the introduction
     */
    Mixin getMixin(int index);

   /**
     * Returns the introduction for a specific name.
     *
     * @param name the name of the introduction
     * @return the the introduction
     */
    Mixin getMixin(String name);

    /**
     * Checks if a specific class has an aspect defined.
     *
     * @param name the name of the aspect
     * @return boolean true if the class has an aspect defined
     */
    boolean hasAspect(String name);

    /**
     * Returns a specific method by the class and the method index.
     *
     * @param klass the class housing the method
     * @param index the method index
     * @return the method
     */
    Method getMethod(Class klass, int index);

    /**
     * Registers entering of a control flow join point.
     *
     * @param metaData the classname:methodMetaData metaData
     */
    void enteringControlFlow(ClassNameMethodMetaDataTuple metaData);

    /**
     * Registers exiting from a control flow join point.
     *
     * @param metaData the classname:methodMetaData metaData
     */
    void exitingControlFlow(ClassNameMethodMetaDataTuple metaData);

    /**
     * Checks if we are in the control flow of a specific cflow pointcut.
     *
     * @param patternTuple the compiled tuple with the class pattern and the method pattern of the cflow pointcut
     * @return boolean
     */
    boolean isInControlFlowOf(PointcutPatternTuple patternTuple);
}
