/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.codehaus.aspectwerkz.advice;

import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * Executes around or instead of the original method invocation.
 * <p/>
 * Supports four different deployment models:
 *     PER_JVM, PER_CLASS, PER_INSTANCE and PER_THREAD.<br/>
 *
 * The PER_JVM deployment model performance a bit better than the other models
 * since no synchronization and object creation is needed.
 *
 * @see aspectwerkz.DeploymentModel
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: AroundAdvice.java,v 1.1.1.1 2003-05-11 15:13:38 jboner Exp $
 */
public abstract class AroundAdvice extends AbstractAdvice {

    /**
     * The pre advices added to this around advice.
     */
//    protected PreAdvice[] m_preAdvices = new PreAdvice[0];

    /**
     * The post advices added to this around advice.
     */
//    protected PostAdvice[] m_postAdvices = new PostAdvice[0];

    /**
     * Sets the class of the class.
     */
    public AroundAdvice() {
        super();
    }

    /**
     * Executes by invoking the next around advice.
     * User should subclass and override this method to add specific behaviour
     * around the invocation.
     * To be implemented by the user.
     *
     * @param joinPoint the join point specified
     * @return the result from the method invocation
     * @throws Throwable
     */
    public abstract Object execute(final JoinPoint joinPoint) throws Throwable;

    /**
     * Executes the current advice and then redirects to the next advice in the
     * chain.<p/>
     * Callback method for the framework.
     *
     * @param joinPoint the join point the advice is executing at
     * @return the result from the next invocation
     * @throws Throwable
     */
    public Object doExecute(final JoinPoint joinPoint) throws Throwable {
//        executePreAdvices(joinPoint);

        Object result = null;
        switch (m_deploymentModel) {
            case DeploymentModel.PER_JVM:
                result = ((AroundAdvice)getPerJvmAdvice(joinPoint)).
                        execute(joinPoint);
                break;

            case DeploymentModel.PER_CLASS:
                result = ((AroundAdvice)getPerClassAdvice(joinPoint)).
                        execute(joinPoint);
                break;

            case DeploymentModel.PER_INSTANCE:
                result = ((AroundAdvice)getPerInstanceAdvice(joinPoint)).
                        execute(joinPoint);
                break;

            case DeploymentModel.PER_THREAD:
                result = ((AroundAdvice)getPerThreadAdvice()).
                        execute(joinPoint);
                break;

            default:
                throw new RuntimeException("invalid deployment model: " + m_deploymentModel);
        }

//        executePostAdvices(joinPoint);
        return result;
    }

    /**
     * Invokes all the pre advices one by one.
     *
     * @param joinPoint the joinpoint for the pointcut
     */
//    public void executePreAdvices(final JoinPoint joinPoint) throws Throwable {
//        for (int i = 0, j = m_preAdvices.length; i < j; i++) {
//            m_preAdvices[i].execute(joinPoint); // call to execute(..) NOT doExecute(..), since we don't want to pass on at this point
//        }
//    }

    /**
     * Invokes all the post advices one by one in the reversed order.
     *
     * @param joinPoint the joinpoint for the pointcut
     */
//    public void executePostAdvices(final JoinPoint joinPoint) throws Throwable {
//        for (int i = m_postAdvices.length - 1; i >= 0; i--) {
//            m_postAdvices[i].execute(joinPoint); // call to execute(..) NOT doExecute(..), since we don't want to pass on at this point
//        }
//    }

    /**
     * Returns the pre advices.
     *
     * @return the pre advices
     */
//    public PreAdvice[] getPreAdvices() {
//        return m_preAdvices;
//    }

    /**
     * Returns the post advices.
     *
     * @return the post advices
     */
//    public PostAdvice[] getPostAdvices() {
//        return m_postAdvices;
//    }

    /**
     * Adds an array of PreAdvices.
     *
     * @param advicesToAdd an array with PreAdvices
     */
//    public void addPreAdvices(final PreAdvice[] advicesToAdd) {
//        final PreAdvice[] preAdvices =
//                new PreAdvice[m_preAdvices.length + advicesToAdd.length];
//        int i;
//        for (i = 0; i < m_preAdvices.length; i++) {
//            preAdvices[i] = m_preAdvices[i];
//        }
//        for (int j = 0; j < advicesToAdd.length; i++, j++) {
//            preAdvices[i] = advicesToAdd[j];
//        }
//        m_preAdvices = new PreAdvice[preAdvices.length];
//        System.arraycopy(preAdvices, 0, m_preAdvices, 0, preAdvices.length);
//    }

    /**
     * Adds an array of PostAdvices.
     *
     * @param advicesToAdd an array with PostAdvices
     */
//    public void addPostAdvices(final PostAdvice[] advicesToAdd) {
//        synchronized (m_postAdvices) {
//            final PostAdvice[] postAdvices =
//                    new PostAdvice[m_postAdvices.length + advicesToAdd.length];
//            int i;
//            for (i = 0; i < m_postAdvices.length; i++) {
//                postAdvices[i] = m_postAdvices[i];
//            }
//            for (int j = 0; j < advicesToAdd.length; i++, j++) {
//                postAdvices[i] = advicesToAdd[j];
//            }
//            m_postAdvices = new PostAdvice[postAdvices.length];
//            System.arraycopy(postAdvices, 0, m_postAdvices, 0, postAdvices.length);
//        }
//    }

    /**
     * Adds a single PreAdvice.
     *
     * @param advice the PreAdvice to add
     */
//    public void addPreAdvice(final PreAdvice advice) {
//        synchronized (m_preAdvices) {
//            final PreAdvice[] preAdvices =
//                    new PreAdvice[m_preAdvices.length + 1];
//
//            for (int i = 0; i < m_preAdvices.length; i++) {
//                preAdvices[i] = m_preAdvices[i];
//            }
//            preAdvices[m_preAdvices.length] = advice;
//
//            m_preAdvices = new PreAdvice[preAdvices.length];
//            System.arraycopy(preAdvices, 0, m_preAdvices, 0, preAdvices.length);
//        }
//    }

    /**
     * Adds a single PostAdvice.
     *
     * @param advice the PostAdvice to add
     */
//    public void addPostAdvice(final PostAdvice advice) {
//        synchronized (m_postAdvices) {
//            final PostAdvice[] postAdvices =
//                    new PostAdvice[m_postAdvices.length + 1];
//
//            for (int i = 0; i < m_postAdvices.length; i++) {
//                postAdvices[i] = m_postAdvices[i];
//            }
//            postAdvices[m_postAdvices.length] = advice;
//
//            m_postAdvices = new PostAdvice[postAdvices.length];
//            System.arraycopy(postAdvices, 0, m_postAdvices, 0, postAdvices.length);
//        }
//    }
}
