/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.control;

import org.codehaus.aspectwerkz.joinpoint.MethodJoinPoint;
import org.codehaus.aspectwerkz.regexp.CompiledPatternTuple;
import org.codehaus.aspectwerkz.IndexTuple;
import org.codehaus.aspectwerkz.pointcut.ExecutionPointcut;
import org.codehaus.aspectwerkz.xmldef.XmlDefSystem;
import org.codehaus.aspectwerkz.attribdef.AttribDefSystem;

import java.util.Iterator;

/**
 * Default controller following a linear execution model (adapted from MethodJoinPoint.proceed()).
 * It is going through the pointcuts one by one. In each pointcut, each advice is executed one by
 * one. Once the last advice on the last pointcut is executed, the original method of the target
 * object gets invoked.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:stefan.finkenzeller@gmx.net">Stefan Finkenzeller</a>
 */
public class DefaultJoinPointController extends AbstractJoinPointController {

    /**
     * Default implementation of a joinpoint controller that is being used if no other controller was
     * specified for the join point.<P>
     *
     * Steps linearly through each pointcut of the joinpoint. In each pointcut it executes its advices
     * one by one. After the last advice in the last pointcut was executed, the original method is being
     * invoked.
     *
     * @param joinPoint    The joinpoint using this controller
     * @return             The result of the invocation.
     */
    public Object proceed(final MethodJoinPoint joinPoint) throws Throwable {

        if (joinPoint.getPointcuts().length == 0) {
            // no pointcuts defined; invoke original method directly
            return joinPoint.invokeOriginalMethod();
        }

        // check for cflow pointcut dependencies
        if (joinPoint.getCFlowPointcuts().size() != 0) {
            // we must check if we are in the correct control flow
            boolean isInCFlow = false;
            for (Iterator it = joinPoint.getCFlowPointcuts().iterator(); it.hasNext();) {
                CompiledPatternTuple patternTuple = (CompiledPatternTuple)it.next();
                if (joinPoint.getSystem().isInControlFlowOf(patternTuple)) {
                    isInCFlow = true;
                    break;
                }
            }
            if (!isInCFlow) {
                // not in the correct cflow; invoke original method directly
                return joinPoint.invokeOriginalMethod();
            }
        }

        // we are in the correct control flow and we have advices to execute
        Object result = null;
        boolean pointcutSwitch = false;

        // if we are out of advices; try the next pointcut
        if (m_currentAdviceIndex == joinPoint.getPointcuts()[m_currentPointcutIndex].
                getAdviceIndexes().length - 1 &&
                m_currentPointcutIndex < joinPoint.getPointcuts().length - 1) {
            m_currentPointcutIndex++;
            m_currentAdviceIndex = -1; // start with the first advice in the chain
            pointcutSwitch = true; // mark this call as a pointcut switch
        }

        if (m_currentAdviceIndex == joinPoint.getPointcuts()[m_currentPointcutIndex].
                getAdviceIndexes().length - 1 &&
                m_currentPointcutIndex == joinPoint.getPointcuts().length - 1) {
            // we are out of advices and pointcuts; invoke the original method
            result = joinPoint.invokeOriginalMethod();
        }
        else {
            // invoke the next advice in the current pointcut
            try {
                m_currentAdviceIndex++;

                // TODO: joinPoint.getSystem() should return a LIST with systems, how-to handle that?
                if (joinPoint.getSystem().isAttribDef()) {
                    AttribDefSystem system = (AttribDefSystem)joinPoint.getSystem();

                    ExecutionPointcut methodPointcut = joinPoint.getPointcuts()[m_currentPointcutIndex];
                    IndexTuple index = methodPointcut.getAdviceIndex(m_currentAdviceIndex);
                    int aspectIndex = index.getAspectIndex();
                    int methodIndex = index.getMethodIndex();
                    result = system.getAspect(aspectIndex).___AW_invokeAdvice(methodIndex, joinPoint);
                }
                else {
                    XmlDefSystem system = (XmlDefSystem)joinPoint.getSystem();

                    IndexTuple index = joinPoint.getPointcuts()[m_currentPointcutIndex].
                            getAdviceIndex(m_currentAdviceIndex);
                    result = system.getAdvice(index).doExecute(joinPoint);
                }

                m_currentAdviceIndex--;
            }
            catch (ArrayIndexOutOfBoundsException ex) {
                throw new RuntimeException(joinPoint.createAdviceNotCorrectlyMappedMessage());
            }
        }

        if (pointcutSwitch) {
            // switch back to the previous pointcut and start with the last advice in the chain
            m_currentPointcutIndex--;
            m_currentAdviceIndex = joinPoint.getPointcuts()[m_currentPointcutIndex].getAdviceIndexes().length;
        }

        return result;
    }

    /**
     * Clones the controller
     */
    public JoinPointController deepCopy() {
        DefaultJoinPointController clone = new DefaultJoinPointController();
        clone.m_currentAdviceIndex = m_currentAdviceIndex;
        clone.m_currentPointcutIndex = m_currentPointcutIndex;
        return clone;
    }
}
