/*
* AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
* Copyright (C) 2002-2003  Jonas Bon�r. All rights reserved.
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR and PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package org.codehaus.aspectwerkz.joinpoint.control;

import org.codehaus.aspectwerkz.joinpoint.MethodJoinPoint;
import org.codehaus.aspectwerkz.regexp.PointcutPatternTuple;

import java.util.Iterator;

/**
 * Default controller following a linear execution model (adapted from MethodJoinPoint.proceed()).
 * It is going through the pointcuts one by one. In each pointcut, each advice is executed one by
 * one. Once the last advice on the last pointcut is executed, the original method of the target
 * object gets invoked.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
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
                PointcutPatternTuple patternTuple = (PointcutPatternTuple)it.next();
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

                result = joinPoint.getSystem().getAdvice(
                        joinPoint.getPointcuts()[m_currentPointcutIndex].
                        getAdviceIndex(m_currentAdviceIndex)).
                        doExecute(joinPoint);

                m_currentAdviceIndex--;
            }
            catch (ArrayIndexOutOfBoundsException ex) {
                throw new RuntimeException(joinPoint.createAdviceNotCorrectlyMappedMessage());
            }
        }

        if (pointcutSwitch) {
            // switch back to the previous pointcut and start with the last advice in the chain
            m_currentPointcutIndex--;
            m_currentAdviceIndex = joinPoint.
                    getPointcuts()[m_currentPointcutIndex].getAdviceIndexes().length;
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
