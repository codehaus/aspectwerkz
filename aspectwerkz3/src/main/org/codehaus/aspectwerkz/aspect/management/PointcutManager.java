/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.aspect.management;

import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.expression.ExpressionContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Manages pointcuts and introductions defined by a specfic aspect.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class PointcutManager {
    /**
     * Holds references to all the pointcuts.
     */
    protected final List m_pointcuts = new ArrayList();

    /**
     * Holds references to all the pointcuts that has a cflow pointcut.
     */
    protected final List m_cflowPointcuts = new ArrayList();

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
     * Creates a new aspect.
     * 
     * @param name the name of the aspect
     */
    public PointcutManager(final String name) {
        this(name, DeploymentModel.PER_JVM);
    }

    /**
     * Creates a new aspect.
     * 
     * @param name the name of the aspect
     * @param deploymentModel the deployment model for the aspect
     */
    public PointcutManager(final String name, final int deploymentModel) {
        if (name == null) {
            throw new IllegalArgumentException("name can not be null");
        }
        if (deploymentModel < 0) {
            throw new IllegalArgumentException(deploymentModel
                + " is not a valid deployement model type");
        }
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
     * Adds an array with introductions to the open class. <br/>
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
            synchronized (m_cflowPointcuts) {
                m_pointcuts.add(pointcut);
                if (pointcut.getExpressionInfo().hasCflowPointcut()) {
                    m_cflowPointcuts.add(new Pointcut(pointcut.getAspectManager(), pointcut
                            .getExpressionInfo()));
                }
            }
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
     * Returns the pointcut for a specific expression.
     * 
     * @param expression the expression
     * @return the pointcut, or null
     */
    public Pointcut getPointcut(final String expression) {
        for (Iterator it = m_pointcuts.iterator(); it.hasNext();) {
            Pointcut pointcut = (Pointcut) it.next();
            if (pointcut.getExpressionInfo().getExpressionAsString().equals(expression)) {
                return pointcut;
            }
        }
        return null;
    }

    /**
     * Returns the cflow pointcut for a specific expression.
     * 
     * @param expression the expression
     * @return the pointcut, or null
     */
    public Pointcut getCflowPointcut(final String expression) {
        for (Iterator it = m_cflowPointcuts.iterator(); it.hasNext();) {
            Pointcut pointcut = (Pointcut) it.next();
            if (pointcut.getExpressionInfo().getExpressionAsString().equals(expression)) {
                return pointcut;
            }
        }
        return null;
    }

    /**
     * Returns all the pointcuts defined by a specific aspect.
     * 
     * @return the pointcuts
     */
    public List getPointcuts() {
        return m_pointcuts;
    }

    /**
     * Returns all the pointcuts defined by a specific aspect that has a cflow pointcut referenced.
     * 
     * @return the pointcuts
     */
    public List getCflowPointcuts() {
        return m_cflowPointcuts;
    }

    /**
     * Returns all the pointcuts for the join point specified.
     * 
     * @param ctx the expression context
     * @return the pointcuts that parse
     */
    public List getPointcuts(final ExpressionContext ctx) {
        if (ctx == null) {
            throw new IllegalArgumentException("context can not be null");
        }
        List pointcutList = new ArrayList();
        for (Iterator it = m_pointcuts.iterator(); it.hasNext();) {
            Pointcut pointcut = (Pointcut) it.next();
            if (pointcut.getExpressionInfo().getExpression().match(ctx)) {
                pointcutList.add(pointcut);
            }
        }
        return pointcutList;
    }

    /**
     * Returns all the cflow pointcuts for the join point specified.
     * 
     * @param ctx the expression context
     * @return the pointcuts that parse
     */
    public List getCflowPointcuts(final ExpressionContext ctx) {
        if (ctx == null) {
            throw new IllegalArgumentException("context can not be null");
        }
        List pointcutList = new ArrayList();
        for (Iterator it = m_cflowPointcuts.iterator(); it.hasNext();) {
            Pointcut pointcut = (Pointcut) it.next();
            if (pointcut.getExpressionInfo().getCflowExpression().match(ctx)) {
                pointcutList.add(pointcut);
            }
        }
        return pointcutList;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PointcutManager)) {
            return false;
        }
        final PointcutManager pointcutManager = (PointcutManager) o;
        if (m_deploymentModel != pointcutManager.m_deploymentModel) {
            return false;
        }
        if (!Arrays.equals(m_introductions, pointcutManager.m_introductions)) {
            return false;
        }
        if (!m_name.equals(pointcutManager.m_name)) {
            return false;
        }
        if (!m_pointcuts.equals(pointcutManager.m_pointcuts)) {
            return false;
        }
        if (!m_cflowPointcuts.equals(pointcutManager.m_cflowPointcuts)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int result;
        result = m_pointcuts.hashCode();
        result = m_cflowPointcuts.hashCode();
        result = (29 * result) + m_name.hashCode();
        result = (29 * result) + m_deploymentModel;
        return result;
    }
}