/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.aspect.management;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.definition.expression.Expression;
import org.codehaus.aspectwerkz.definition.expression.PointcutType;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.FieldMetaData;
import org.codehaus.aspectwerkz.metadata.MemberMetaData;

/**
 * Manages pointcuts and introductions defined by a specfic aspect.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class PointcutManager {

    private static final List EMTPTY_ARRAY_LIST = new ArrayList();

    /**
     * Holds references to all the the pointcuts.
     */
    protected final List m_pointcuts = new ArrayList();

//    /**
//     * Maps the method to all the cflow method it should care about.
//     */
//    private final Map m_methodToCFlowMethodsMap = new SequencedHashMap();

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
    //protected final String m_uuid;

    /**
     * Creates a new aspect.
     *
     * @param uuid the UUID for the AspectWerkz system
     * @param name the name of the aspect
     */
    public PointcutManager(/*final String uuid, */final String name) {
        this(/*uuid, */name, DeploymentModel.PER_JVM);
    }

    /**
     * Creates a new aspect.
     *
     * @param uuid            the UUID for the AspectWerkz system
     * @param name            the name of the aspect
     * @param deploymentModel the deployment model for the aspect
     */
    public PointcutManager(/*final String uuid, */final String name, final int deploymentModel) {
//        if (uuid == null) {
//            throw new IllegalArgumentException("uuid can not be null");
//        }
        if (name == null) {
            throw new IllegalArgumentException("name can not be null");
        }
        if (deploymentModel < 0) {
            throw new IllegalArgumentException(deploymentModel + " is not a valid deployement model type");
        }
//        m_uuid = uuid;
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
     * Adds a new pointcut.
     *
     * @param pointcut the pointcut to add
     */
    public void addPointcut(final Pointcut pointcut) {
        synchronized (m_pointcuts) {
            m_pointcuts.add(pointcut);
        }
    }

//    /**
//     * Adds a Execution expression to execution expr.->cflow call expr. map.
//     *
//     * @param expression      the execution expression
//     * @param cflowExpression the cflow call expression
//     */
//    public void addMethodToCflowExpressionMap(Expression expression, Expression cflowExpression) {
//        List cflowPatterns = (List)m_methodToCFlowMethodsMap.get(expression);
//        if (cflowPatterns != null) {
//            cflowPatterns.add(cflowExpression);
//        }
//        else {
//            cflowPatterns = new ArrayList();
//            cflowPatterns.add(cflowExpression);
//            m_methodToCFlowMethodsMap.put(expression, cflowPatterns);
//        }
//    }

    /**
     * Returns the introductions for the open class.
     *
     * @return an array with the introductions for the class
     */
    public String[] getIntroductions() {
        return m_introductions;
    }

    /**
     * Returns the pointcut for a specific expression.
     *
     * @param expression the expression
     * @return the pointcut, or null
     */
    public Pointcut getPointcut(final String expression) {
        for (Iterator it = m_pointcuts.iterator(); it.hasNext();) {
            Pointcut pointcut = (Pointcut)it.next();
            if (pointcut.getExpression().getExpression().equals(expression)) {
                return pointcut;
            }
        }
        return null;
    }

    /**
     * Returns all the execution pointcuts for the member join point specified.
     *
     * @param classMetaData  the meta-data for the class
     * @param memberMetaData the meta-data for the member
     * @return the execution pointcuts that match
     */
    public List getExecutionPointcuts(final ClassMetaData classMetaData, final MemberMetaData memberMetaData) {
        return getPointcuts(classMetaData, memberMetaData, PointcutType.EXECUTION);
    }

    /**
     * Returns all the pointcuts for the member join point specified.
     *
     * @param classMetaData  the meta-data for the class
     * @param memberMetaData the meta-data for the member
     * @param pointcutType
     * @return the pointcuts that match
     */
    private List getPointcuts(
            final ClassMetaData classMetaData,
            final MemberMetaData memberMetaData,
            final PointcutType pointcutType) {
        if (classMetaData == null) {
            throw new IllegalArgumentException("class meta-data can not be null");
        }
        List pointcutList = new ArrayList();
        for (Iterator it = m_pointcuts.iterator(); it.hasNext();) {
            Pointcut pointcut = (Pointcut)it.next();
            if (pointcut.getExpression().match(classMetaData, memberMetaData, pointcutType)) {
                //System.out.println("MATCH " + pointcut.getExpression().getName() + " : " + pointcut.getExpression());//AVAOPC ALEX
                pointcutList.add(pointcut);
            }
        }
        return pointcutList;
    }

    /**
     * Returns all the pointcuts for the method join point specified.
     *
     * @param classMetaData the meta-data for the class
     * @param fieldMetaData the meta-data for the field
     * @return the pointcuts
     */
    public List getGetPointcuts(final ClassMetaData classMetaData, final FieldMetaData fieldMetaData) {
        return getPointcuts(classMetaData, fieldMetaData, PointcutType.GET);
    }

    /**
     * Returns all the pointcuts for the method join point specified.
     *
     * @param classMetaData the meta-data for the class
     * @param fieldMetaData the meta-data for the field
     * @return the pointcuts
     */
    public List getSetPointcuts(final ClassMetaData classMetaData, final FieldMetaData fieldMetaData) {
        return getPointcuts(classMetaData, fieldMetaData, PointcutType.SET);
    }

    /**
     * Returns all the pointcuts for the method join point specified.
     *
     * @param classMetaData the meta-data for the class
     * @return the pointcuts
     */
    public List getHandlerPointcuts(final ClassMetaData classMetaData) {
        return getPointcuts(classMetaData, null, PointcutType.HANDLER);
    }

    /**
     * Returns all the pointcuts for the caller side join point specified.
     *
     * @param classMetaData  the class metadata
     * @param memberMetaData the meta-data for the member
     * @return the pointcuts
     */
    public List getCallPointcuts(final ClassMetaData classMetaData, final MemberMetaData memberMetaData) {
        return getPointcuts(classMetaData, memberMetaData, PointcutType.CALL);
    }

//    /** TODO: ALEX is that needed ?
//     * Returns the cflow pointcut for a specific uuid and expression.
//     *
//     * @param uuid the uuid
//     * @param expression the expression
//     * @return the method pointcut
//     */
//    public GetPointcut getCFlowPointcut(final String uuid, final String expression) {
//        throw new UnsupportedOperationException("not implemented yet");
//    }

    /**
     * Returns all the expression with 1+ cflow for the given metadata The Expression are optimized thru inflated
     * evaluation
     * <p/>
     * TODO: ALEX AVCF: support for @CALL etc cflow
     *
     * @param classMetaData       the name of the class
     * @param memberMetaData      the meta-data for the method / field
     * @param callerClassMetaData
     * @param pointcutType
     * @return the pointcuts
     */
    public List getCFlowExpressions(
            final ClassMetaData classMetaData,
            final MemberMetaData memberMetaData,
            final ClassMetaData callerClassMetaData,
            final PointcutType pointcutType) {
        if (classMetaData == null) {
            throw new IllegalArgumentException("class meta-data can not be null");
        }
        if (memberMetaData == null) {
            throw new IllegalArgumentException("member meta-data can not be null");
        }

        List expressions = new ArrayList();
        //TODO check cflow for ALL pc
        List pointcutListToLookIn = m_pointcuts;
//        List pointcutListToLookIn = null;
//        if (pointcutType.equals(PointcutType.EXECUTION)) {
//            pointcutListToLookIn = m_executionPointcuts;
//        }
//        else {
//            return EMTPTY_ARRAY_LIST;
////            throw new RuntimeException("TODO: cflow for @CALL @GET/SET ...");
//        }
        for (Iterator it = pointcutListToLookIn.iterator(); it.hasNext();) {
            Expression expression = ((Pointcut)it.next()).getExpression();
            // filter out if does not contains CFLOW exprs
            // and ignores CFLOW only expression (since they are CALL pc with CFlowSystemAspect bounded, which
            // must not be filtered out)
            if (expression.isOfType(PointcutType.CFLOW)
                && expression.getTypes().size() > 1
                && expression.match(classMetaData, memberMetaData, pointcutType)) {

                // generate a minimalist expression like "TRUE AND cflow OR FALSE"
                // where TRUE , FALSE etc is the result of the match as done at TF time
                expressions.add(expression.extractCflowExpression(classMetaData, memberMetaData, pointcutType));
            }
        }
        return expressions;
    }

    // --- over-ridden methods ---

    public String toString() {
        return '['
               + super.toString()
               + ": "
               + ',' + m_name
               //+ ',' + m_uuid
               + ',' + m_deploymentModel
               + ',' + m_introductions
//               + ',' + m_methodToCFlowMethodsMap
               + ']';
    }

    public int hashCode() {
        int result = 17;
        result = 37 * result + m_deploymentModel;
        result = 37 * result + hashCodeOrZeroIfNull(m_name);
        //result = 37 * result + hashCodeOrZeroIfNull(m_uuid);
        result = 37 * result + hashCodeOrZeroIfNull(m_introductions);
        result = 37 * result + hashCodeOrZeroIfNull(m_pointcuts);
//        result = 37 * result + hashCodeOrZeroIfNull(m_methodToCFlowMethodsMap);
        return result;
    }

    protected static int hashCodeOrZeroIfNull(final Object o) {
        if (null == o) {
            return 19;
        }
        return o.hashCode();
    }

    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PointcutManager)) {
            return false;
        }
        final PointcutManager obj = (PointcutManager)o;
        return areEqualsOrBothNull(obj.m_name, this.m_name)
               && (obj.m_deploymentModel == this.m_deploymentModel)
               //&& areEqualsOrBothNull(obj.m_uuid, this.m_uuid)
               && areEqualsOrBothNull(obj.m_introductions, this.m_introductions)
               && areEqualsOrBothNull(obj.m_pointcuts, this.m_pointcuts);
//               && areEqualsOrBothNull(obj.m_methodToCFlowMethodsMap, this.m_methodToCFlowMethodsMap);
    }

    protected static boolean areEqualsOrBothNull(final Object o1, final Object o2) {
        if (null == o1) {
            return (null == o2);
        }
        return o1.equals(o2);
    }
}
