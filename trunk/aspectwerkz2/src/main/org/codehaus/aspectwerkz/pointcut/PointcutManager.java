/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.pointcut;

import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.codehaus.aspectwerkz.pointcut.GetPointcut;
import org.codehaus.aspectwerkz.pointcut.ExecutionPointcut;
import org.codehaus.aspectwerkz.pointcut.CallPointcut;
import org.codehaus.aspectwerkz.pointcut.SetPointcut;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.FieldMetaData;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.util.SequencedHashMap;
import org.codehaus.aspectwerkz.definition.expression.Expression;
import org.codehaus.aspectwerkz.DeploymentModel;

/**
 * Manages pointcuts and introductions defined by a specfic aspect.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class PointcutManager {

    /**
     * Holds references to all the the execution pointcuts.
     */
    protected final List m_executionPointcuts = new ArrayList();

    /**
     * Holds references to all the the call pointcuts.
     */
    protected final List m_callPointcuts = new ArrayList();

    /**
     * Holds references to all the the get pointcuts.
     */
    protected final List m_getPointcuts = new ArrayList();

    /**
     * Holds references to all the the set pointcuts.
     */
    protected final List m_setPointcuts = new ArrayList();

    /**
     * Maps the method to all the cflow method it should care about.
     */
    private final Map m_methodToCFlowMethodsMap = new SequencedHashMap();

    /**
     * Holds references to all the the introductions.
     */
    protected String[] m_introductions = new String[0];

    /**
     * The name of the aspect.
     */
    protected final String m_name;

    /**
     * The deployment model for the aspect.
     */
    protected final int m_deploymentModel;

    /**
     * The UUID for the system.
     */
    protected final String m_uuid;

    /**
     * Creates a new aspect.
     *
     * @param uuid the UUID for the AspectWerkz system
     * @param name the name of the aspect
     */
    public PointcutManager(final String uuid, final String name) {
        this(uuid, name, DeploymentModel.PER_JVM);
    }

    /**
     * Creates a new aspect.
     *
     * @param uuid the UUID for the AspectWerkz system
     * @param name the name of the aspect
     * @param deploymentModel the deployment model for the aspect
     */
    public PointcutManager(final String uuid, final String name, final int deploymentModel) {
        if (uuid == null) throw new IllegalArgumentException("uuid can not be null");
        if (name == null) throw new IllegalArgumentException("name can not be null");
        if (deploymentModel < 0) throw new IllegalArgumentException(deploymentModel + " is not a valid deployement model type");
        m_uuid = uuid;
        m_name = name;
        m_deploymentModel = deploymentModel;
    }

    /**
     * Returns the name of the aspect.
     *
     * @return the aspect name
     */
    public String getName() {
        return m_name;
    }

    /**
     * Returns the deployment model for the aspect.
     *
     * @return the deployment model
     */
    public int getDeploymentModel() {
        return m_deploymentModel;
    }

    /**
     * Returns the deployment model for the aspect.
     *
     * @return the deployment model
     */
    public String getDeploymentModelAsString() {
        return DeploymentModel.getDeploymentModelAsString(m_deploymentModel);
    }

    /**
     * Adds an introduction to the open class.
     *
     * @param introduction the name of the introduction to add
     */
    public final void addIntroduction(final String introduction) {
        synchronized (m_introductions) {
            final String[] tmp = new String[m_introductions.length + 1];
            java.lang.System.arraycopy(m_introductions, 0, tmp, 0, m_introductions.length);
            tmp[m_introductions.length] = introduction;
            m_introductions = new String[m_introductions.length + 1];
            java.lang.System.arraycopy(tmp, 0, m_introductions, 0, tmp.length);
        }
    }

    /**
     * Adds an array with introductions to the open class.<br/>
     *
     * @param introductions the introductions to add
     */
    public final void addIntroductions(final String[] introductions) {
        synchronized (m_introductions) {
            final String[] clone = new String[introductions.length];
            java.lang.System.arraycopy(introductions, 0, clone, 0, introductions.length);
            final String[] tmp = new String[m_introductions.length + introductions.length];
            int i;
            for (i = 0; i < m_introductions.length; i++) {
                tmp[i] = m_introductions[i];
            }
            for (int j = 0; j < clone.length; i++, j++) {
                tmp[i] = clone[j];
            }
            m_introductions = new String[tmp.length];
            java.lang.System.arraycopy(tmp, 0, m_introductions, 0, tmp.length);
        }
    }

    /**
     * Adds a new execution pointcut to the class.
     *
     * @param pointcut the pointcut to add
     */
    public void addExecutionPointcut(final ExecutionPointcut pointcut) {
        synchronized (m_executionPointcuts) {
            m_executionPointcuts.add(pointcut);
        }
    }

    /**
     * Adds a new get pointcut to the class.
     *
     * @param pointcut the pointcut to add
     */
    public final void addGetPointcut(final GetPointcut pointcut) {
        synchronized (m_getPointcuts) {
            m_getPointcuts.add(pointcut);
        }
    }

    /**
     * Adds a new set pointcut to the class.
     *
     * @param pointcut the pointcut to add
     */
    public void addSetPointcut(final SetPointcut pointcut) {
        synchronized (m_setPointcuts) {
            m_setPointcuts.add(pointcut);
        }
    }

    /**
     * Adds a new call pointcut to the class.
     *
     * @param pointcut the pointcut to add
     */
    public final void addCallPointcut(final CallPointcut pointcut) {
        synchronized (m_callPointcuts) {
            m_callPointcuts.add(pointcut);
        }
    }

//    /** TODO: ALEX RM
//     * Adds a new method pattern to the method->cflow-method map.
//     *
//     * @param patternTuple the method pointcut definition
//     * @param cflowPatternTuple the cflow pointcut definition
//     */
//    public void addMethodToCFlowMethodMap(final CompiledPatternTuple patternTuple,
//                                          final CompiledPatternTuple cflowPatternTuple) {
//        List cflowPatterns = (List)m_methodToCFlowMethodsMap.get(patternTuple);
//        if (cflowPatterns != null) {
//            cflowPatterns.add(cflowPatternTuple);
//        }
//        else {
//            cflowPatterns = new ArrayList();
//            cflowPatterns.add(cflowPatternTuple);
//            m_methodToCFlowMethodsMap.put(patternTuple, cflowPatterns);
//        }
//    }

    /**
     * Adds a Execution expression to execution expr.->cflow call expr. map.
     *
     * @param expression the execution expression
     * @param cflowExpression the cflow call expression
     */
    public void addMethodToCflowExpressionMap(Expression expression, Expression cflowExpression) {
        List cflowPatterns = (List)m_methodToCFlowMethodsMap.get(expression);
        if (cflowPatterns != null) {
            cflowPatterns.add(cflowExpression);
        }
        else {
            cflowPatterns = new ArrayList();
            cflowPatterns.add(cflowExpression);
            m_methodToCFlowMethodsMap.put(expression, cflowPatterns);
        }
    }

    /**
     * Returns the introductions for the open class.
     *
     * @return an array with the introductions for the class
     */
    public String[] getIntroductions() {
        return m_introductions;
    }

    /**
     * Returns the execution pointcut for a specific uuid and expression.
     *
     * @param expression the expression
     * @return the execution pointcut
     */
    public ExecutionPointcut getExecutionPointcut(final String expression) {
        for (Iterator it = m_executionPointcuts.iterator(); it.hasNext();) {
            ExecutionPointcut pointcut = (ExecutionPointcut)it.next();
            if (pointcut.getExpression().getExpression().equals(expression)) {
                return pointcut;
            }
        }
        return null;
    }

    /**
     * Returns all the pointcuts for the method join point specified.
     *
     * @param classMetaData the meta-data for the class
     * @param methodMetaData the meta-data for the method
     * @return the pointcuts
     */
    public List getExecutionPointcuts(final ClassMetaData classMetaData,
                                      final MethodMetaData methodMetaData) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");

        List pointcutList = new ArrayList();
        for (Iterator it = m_executionPointcuts.iterator(); it.hasNext();) {
            ExecutionPointcut pointcut = (ExecutionPointcut)it.next();
            if (pointcut.getExpression().match(classMetaData, methodMetaData)) {
                pointcutList.add(pointcut);
            }
        }
        return pointcutList;
    }

    /**
     * Returns the get pointcut for a specific uuid and expression.
     *
     * @param expression the expression
     * @return the get pointcut
     */
    public GetPointcut getGetPointcut(final String expression) {
        for (Iterator it = m_getPointcuts.iterator(); it.hasNext();) {
            GetPointcut pointcut = (GetPointcut)it.next();
            if (pointcut.getExpression().getExpression().equals(expression)) {
                return pointcut;
            }
        }
        return null;
    }

    /**
     * Returns all the pointcuts for the method join point specified.
     *
     * @param classMetaData the meta-data for the class
     * @param fieldMetaData the meta-data for the field
     * @return the pointcuts
     */
    public List getGetPointcuts(final ClassMetaData classMetaData,
                                final FieldMetaData fieldMetaData) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (fieldMetaData == null) throw new IllegalArgumentException("field meta-data can not be null");

        List pointcutList = new ArrayList();
        for (Iterator it = m_getPointcuts.iterator(); it.hasNext();) {
            final GetPointcut pointcut = (GetPointcut)it.next();
            if (pointcut.getExpression().match(classMetaData, fieldMetaData)) {
                pointcutList.add(pointcut);
            }
        }
        return pointcutList;
    }

    /**
     * Returns the set pointcut for a specific uuid and expression.
     *
     * @param expression the expression
     * @return the method pointcut
     */
    public SetPointcut getSetPointcut(final String expression) {
        for (Iterator it = m_setPointcuts.iterator(); it.hasNext();) {
            SetPointcut pointcut = (SetPointcut)it.next();
            if (pointcut.getExpression().getExpression().equals(expression)) {
                return pointcut;
            }
        }
        return null;
    }

    /**
     * Returns all the pointcuts for the method join point specified.
     *
     * @param classMetaData the meta-data for the class
     * @param fieldMetaData the meta-data for the field
     * @return the pointcuts
     */
    public List getSetPointcuts(final ClassMetaData classMetaData,
                                final FieldMetaData fieldMetaData) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (fieldMetaData == null) throw new IllegalArgumentException("field meta-data can not be null");

        List pointcutList = new ArrayList();
        for (Iterator it = m_setPointcuts.iterator(); it.hasNext();) {
            final SetPointcut pointcut = (SetPointcut)it.next();
            boolean flag = pointcut.getExpression().match(classMetaData, fieldMetaData);
            if (flag) {
                pointcutList.add(pointcut);
            }
        }
        return pointcutList;
    }

    /**
     * Returns the call pointcut for a specific uuid and expression.
     *
     * @param expression the expression
     * @return the call pointcut
     */
    public CallPointcut getCallPointcut(final String expression) {
        for (Iterator it = m_callPointcuts.iterator(); it.hasNext();) {
            CallPointcut pointcut = (CallPointcut)it.next();
            if (pointcut.getExpression().getExpression().equals(expression)) {
                return pointcut;
            }
        }
        return null;
    }

    /**
     * Returns all the pointcuts for the caller side join point specified.
     *
     * @param classMetaData the class metadata
     * @param methodMetaData the meta-data for the method
     * @return the pointcuts
     */
    public List getCallPointcuts(final ClassMetaData classMetaData,
                                 final MethodMetaData methodMetaData) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");

        List pointcutList = new ArrayList();
        for (Iterator it = m_callPointcuts.iterator(); it.hasNext();) {
            final CallPointcut pointcut = (CallPointcut)it.next();
            if (pointcut.getExpression().match(classMetaData, methodMetaData)) {
                pointcutList.add(pointcut);
            }
        }
        return pointcutList;
    }

//    /** TODO: ALEX RM
//     * Returns the cflow pointcut for a specific uuid and expression.
//     *
//     * @param uuid the uuid
//     * @param expression the expression
//     * @return the method pointcut
//     */
//    public GetPointcut getCFlowPointcut(final String uuid, final String expression) {
//        throw new UnsupportedOperationException("not implemented yet");
//    }

//    /** TODO: ALEX RM
//     * Returns all the pointcuts for the cflow join point specified.
//     *
//     * @param className the name of the class
//     * @param methodMetaData the meta-data for the method
//     * @return the pointcuts
//     */
//    public List getCFlowPointcuts(final String className,
//                                  final MethodMetaData methodMetaData) {
//        if (className == null) throw new IllegalArgumentException("class name can not be null");
//        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");
//
//        List pointcutList = new ArrayList();
//        for (Iterator it = m_methodToCFlowMethodsMap.entrySet().iterator(); it.hasNext();) {
//            Map.Entry entry = (Map.Entry)it.next();
//            CompiledPatternTuple methodPatternTuple = (CompiledPatternTuple)entry.getKey();
//            if (methodPatternTuple.getClassPattern().matches(className) &&
//                    ((MethodPattern)methodPatternTuple.getPattern()).matches(methodMetaData)) {
//                pointcutList.addAll((List)entry.getValue());
//            }
//        }
//        return pointcutList;
//    }

    /**
     * Returns all the cflow call expression for the given metadata (callee side)
     *
     * @param classMetaData the name of the class
     * @param methodMetaData the meta-data for the method
     * @return the pointcuts
     */
    public List getCFlowExpressions(final ClassMetaData classMetaData,
                                  final MethodMetaData methodMetaData) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");

        List pointcutList = new ArrayList();
        for (Iterator it = m_methodToCFlowMethodsMap.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            Expression expression = (Expression)entry.getKey();
            if (expression.match(classMetaData, methodMetaData)) {
                pointcutList.addAll((List)entry.getValue());
            }
        }
        return pointcutList;
    }

    // --- over-ridden methods ---

    public String toString() {
        return "["
                + super.toString()
                + ": "
                + "," + m_name
                + "," + m_uuid
                + "," + m_deploymentModel
                + "," + m_introductions
                + "," + m_executionPointcuts
                + "," + m_getPointcuts
                + "," + m_setPointcuts
                + "," + m_callPointcuts
                + "," + m_methodToCFlowMethodsMap
                + "]";
    }

    public int hashCode() {
        int result = 17;
        result = 37 * result + m_deploymentModel;
        result = 37 * result + hashCodeOrZeroIfNull(m_name);
        result = 37 * result + hashCodeOrZeroIfNull(m_uuid);
        result = 37 * result + hashCodeOrZeroIfNull(m_introductions);
        result = 37 * result + hashCodeOrZeroIfNull(m_executionPointcuts);
        result = 37 * result + hashCodeOrZeroIfNull(m_getPointcuts);
        result = 37 * result + hashCodeOrZeroIfNull(m_setPointcuts);
        result = 37 * result + hashCodeOrZeroIfNull(m_callPointcuts);
        result = 37 * result + hashCodeOrZeroIfNull(m_methodToCFlowMethodsMap);
        return result;
    }

    protected static int hashCodeOrZeroIfNull(final Object o) {
        if (null == o) return 19;
        return o.hashCode();
    }

    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof PointcutManager)) return false;
        final PointcutManager obj = (PointcutManager)o;
        return areEqualsOrBothNull(obj.m_name, this.m_name)
                && (obj.m_deploymentModel == this.m_deploymentModel)
                && areEqualsOrBothNull(obj.m_uuid, this.m_uuid)
                && areEqualsOrBothNull(obj.m_introductions, this.m_introductions)
                && areEqualsOrBothNull(obj.m_executionPointcuts, this.m_executionPointcuts)
                && areEqualsOrBothNull(obj.m_getPointcuts, this.m_getPointcuts)
                && areEqualsOrBothNull(obj.m_setPointcuts, this.m_setPointcuts)
                && areEqualsOrBothNull(obj.m_callPointcuts, this.m_callPointcuts)
                && areEqualsOrBothNull(obj.m_methodToCFlowMethodsMap, this.m_methodToCFlowMethodsMap);
    }

    protected static boolean areEqualsOrBothNull(final Object o1, final Object o2) {
        if (null == o1) return (null == o2);
        return o1.equals(o2);
    }
}
