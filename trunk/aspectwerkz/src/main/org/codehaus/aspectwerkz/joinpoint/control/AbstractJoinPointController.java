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
package org.codehaus.aspectwerkz.joinpoint.control;

import java.io.ObjectInputStream;

import org.codehaus.aspectwerkz.joinpoint.MethodJoinPoint;
import org.codehaus.aspectwerkz.pointcut.MethodPointcut;

/**
 * Abstract join point controller with convenience methods for managing advices.
 *
 * @author <a href="mailto:"">Stefan Finkenzeller</a>
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

    // convenience methods for hot deployment

    /**
     * Adds an advice to a pointcut
     *
     * @param adviceName    name of the advice as defined in the metadata
     * @param jp            joinpoint the controller is handling
     * @param pc            pointcut, the advice needs to be appended. If null, current pointcut is used.
     */
    public void addAdvice(final String adviceName,
                          final MethodJoinPoint jp,
                          MethodPointcut pc) {

        if (adviceName == null) throw new IllegalArgumentException("Advice can not be null");
        if (jp == null) throw new IllegalArgumentException("Joinpoint can not be null");

        if (pc == null) {
            pc = jp.getPointcuts()[m_currentPointcutIndex];
        }
        pc.addAdvice(adviceName);
    }

    /**
     * Removes an advice from a pointcut
     *
     * @param adviceName    name of the advice as defined in the metadata
     * @param jp            joinpoint the controller is handling
     * @param pc            pointcut, the advice needs to be appended. If null, current pointcut is used.
     */
    public void removeAdvice(final String adviceName,
                             final MethodJoinPoint jp,
                             MethodPointcut pc) {

        if (adviceName == null) throw new IllegalArgumentException("Advice can not be null");
        if (jp == null) throw new IllegalArgumentException("Joinpoint can not be null");

        if (pc == null) {
            pc = jp.getPointcuts()[m_currentPointcutIndex];
        }
        pc.removeAdvice(adviceName);
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
        return  (obj.m_currentPointcutIndex == this.m_currentPointcutIndex) &&
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
