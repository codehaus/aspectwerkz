/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.control;

import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.Collection;
import java.util.HashSet;
import java.util.ArrayList;

import org.codehaus.aspectwerkz.joinpoint.MethodJoinPoint;
import org.codehaus.aspectwerkz.pointcut.ExecutionPointcut;
import org.codehaus.aspectwerkz.xmldef.advice.Advice;
import org.codehaus.aspectwerkz.xmldef.XmlDefSystem;
import org.codehaus.aspectwerkz.System;
import org.codehaus.aspectwerkz.attribdef.AttribDefSystem;

/**
 * Abstract join point controller with convenience methods for managing advices.
 *
 * @author <a href="mailto:stefan.finkenzeller@gmx.net">Stefan Finkenzeller</a>
 */
public abstract class AbstractJoinPointController implements JoinPointController {

    /**
     * The index of the current advice.
     */
    protected int m_currentAdviceIndex = -1;

    /**
     * The index of the current pointcut.
     */
    protected int m_currentPointcutIndex = 0;

    /**
     * Adds an advice to a pointcut
     *
     * @param adviceName    name of the advice as defined in the metadata
     * @param jp            joinpoint the controller is handling
     * @param pc            pointcut, the advice needs to be appended. If null, current pointcut is used.
     */
    public void addAdvice(final String adviceName,
                          final MethodJoinPoint jp,
                          ExecutionPointcut pc) {
        if (adviceName == null) throw new IllegalArgumentException("advice can not be null");
        if (jp == null) throw new IllegalArgumentException("joinpoint can not be null");

        if (pc == null) {
            pc = jp.getPointcuts()[m_currentPointcutIndex];
        }
        pc.addAdvice(adviceName);
    }

    /**
     * Removes an advice from a pointcut
     *
     * @param adviceName    name of the advice as defined in the metadata
     * @param joinPoint     joinpoint the controller is handling
     * @param pointcut      pointcut, the advice needs to be appended. If null, current pointcut is used.
     */
    public void removeAdvice(final String adviceName,
                             final MethodJoinPoint joinPoint,
                             ExecutionPointcut pointcut) {

        if (adviceName == null) throw new IllegalArgumentException("advice can not be null");
        if (joinPoint == null) throw new IllegalArgumentException("joinpoint can not be null");

        if (pointcut == null) {
            pointcut = joinPoint.getPointcuts()[m_currentPointcutIndex];
        }
        pointcut.removeAdvice(adviceName);
    }

    /**
     * Returns all advices of the joinpoint. If asSet is true, returns result as a set, otherwise
     * returns all advices in the order they are pointcut.
     *
     * @param joinPoint  the joinpoint to inspect
     * @param asSet     flag, whether result is a set
     * @return          iterator containing all advices
     */
    public static Iterator getAllAdvices(final MethodJoinPoint joinPoint, boolean asSet) {
        Collection cAdvices;
        if (asSet) {
            cAdvices = new HashSet();
        }
        else {
            cAdvices = new ArrayList();
        }

        // TODO: joinPoint.getSystem() should return a LIST with systems, how-to handle that?
        if (joinPoint.getSystem().isAttribDef()) {
            AttribDefSystem system = (AttribDefSystem)joinPoint.getSystem();

            // TODO: handle attribdef model
        }
        else {
            XmlDefSystem system = (XmlDefSystem)joinPoint.getSystem();
            for (int i = 0; i < joinPoint.getPointcuts().length; i++) {
                for (int j = 0; j < joinPoint.getPointcuts()[i].getAdviceIndexes().length; j++) {
                    cAdvices.add(system.getAdvice(
                            joinPoint.getPointcuts()[i].getAdviceIndex(j)
                    ));
                }
            }
        }

        return cAdvices.iterator();
    }

    /**
     * Removes all empty pointcuts from the joinpoint. Pointcuts can become empty after advices
     * have been deleted by e.g. the use of removeAdvice(), so if the proceed() method of the controller
     * implemented cannot handle empty pointcuts, the pointcuts need to be purged before proceed() is bein
     * executed.
     *
     * @param joinPoint       the joinpoint which pointcuts need to be purged
     */
    public static void purgePointcuts(final MethodJoinPoint joinPoint) {

        // find number of non-empty pointcuts
        int i, num = 0;
        for (i = 0; i < joinPoint.getPointcuts().length; i++) {
            if (joinPoint.getPointcuts()[i].getAdviceIndexes().length > 0) {
                num++;
            }
        }

        // create array of non-empty pointcuts
        ExecutionPointcut[] purgedPointcuts = new ExecutionPointcut[num];
        num = 0;
        for (i = 0; i < joinPoint.getPointcuts().length; i++) {
            if (joinPoint.getPointcuts()[i].getAdviceIndexes().length > 0) {
                purgedPointcuts[num] = joinPoint.getPointcuts()[i];
                num++;
            }
        }

        joinPoint.setPointcuts(purgedPointcuts);
    }

    /**
     * Checks for redundant advices of a given type in a joinpoint and then removes all
     * redundant advices.<P>
     *
     * Redundancy can be caused by the use of very global regexp's or the combination of advice definitions
     * in java doclets <i>and</i> XML. After calling this method, only the first of a series
     * of redundant advices will remain in the advice chain that gets executed by the controller.
     *
     * @param joinPoint            the joinpoint where redundant advices need to be removed
     * @param adviceName    the name of the advice to check for redundancy (e.g. log)
     * @param purge         true, if empty pointcuts shall be removed after redundancies have been found
     */
    public int clearRedundancy(final MethodJoinPoint joinPoint,
                               final String adviceName,
                               boolean purge) {
        int iFound = 0;

        // TODO: joinPoint.getSystem() should return a LIST with systems, how-to handle that?
        if (joinPoint.getSystem().isAttribDef()) {
            AttribDefSystem system = (AttribDefSystem)joinPoint.getSystem();

            // TODO: handle attribdef model
        }
        else {
            XmlDefSystem system = (XmlDefSystem)joinPoint.getSystem();

            for (int i = 0; i < joinPoint.getPointcuts().length; i++) {
                for (int j = 0; j < joinPoint.getPointcuts()[i].getAdviceIndexes().length; j++) {
                    if (system.getAdvice(joinPoint.getPointcuts()[i].
                            getAdviceIndex(j)).getName().equals(adviceName)) {
                        iFound++;
                        if (iFound > 1) {
                            removeAdvice(adviceName, joinPoint, joinPoint.getPointcuts()[i]);
                        }
                    }
                }
            }
        }

        // Purge pointcuts if requested
        if (purge) {
            purgePointcuts(joinPoint);
        }

        return iFound - 1;
    }

    /**
     * Checks all advices of a joinpoint for redundancies. If advices are redundant, they are removed.
     *
     * @param jp        the joinpoint where redundant advices need to be removed
     */
    public int clearAllRedundancies(final MethodJoinPoint jp) {
        Iterator it = getAllAdvices(jp, true);

        int iRedundancies = 0;
        while (it.hasNext()) {
            Advice advice = (Advice)it.next();
            iRedundancies += clearRedundancy(jp, advice.getName(), false);
        }

        // Now that advices have been removed, purge the pointcuts
        purgePointcuts(jp);

        return iRedundancies;
    }

    /**
     * Proceeds in the execution model for the join point to the next logical pointcut/advice<<P>
     *
     * Joinpoint controller implementations need to implement the business logic for handling e.g.
     * advice redundancy, advice dependency, advice compatibility or special exception handling
     * here.<P>
     *
     * To implement this logic it can use the knowledge about the joinpoint, the current advice and pointcut.
     *
     * @param jp    The joinpoint using this controller
     * @return      The result of the invocation.
     */
    public abstract Object proceed(MethodJoinPoint jp) throws Throwable;

    /**
     * Clones the controller
     */
    public abstract JoinPointController deepCopy();

    /**
     * Overridden hashCode method.
     *
     * @return
     */
    public int hashCode() {
        int result = 17;
        result = 37 * result + m_currentAdviceIndex;
        result = 37 * result + m_currentPointcutIndex;
        return result;
    }

    /**
     * The overridden equals method.
     *
     * @param o the other object
     * @return boolean
     */
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractJoinPointController)) return false;

        final AbstractJoinPointController obj = (AbstractJoinPointController)o;
        return (obj.m_currentPointcutIndex == this.m_currentPointcutIndex) &&
                (obj.m_currentAdviceIndex == this.m_currentAdviceIndex);
    }

    /**
     * Provides custom deserialization.
     *
     * @param stream the object input stream containing the serialized object
     * @throws java.lang.Exception in case of failure
     */
    private void readObject(final ObjectInputStream stream) throws Exception {
        ObjectInputStream.GetField fields = stream.readFields();
        m_currentAdviceIndex = fields.get("m_currentAdviceIndex", -1);
        m_currentPointcutIndex = fields.get("m_currentPointcutIndex", -1);
    }
}
