/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint;

/**
 * Provides static and reflective information about the join point.
 *
 * @TODO: Can be problems with call side pointcuts. What is the targetClass and targetInstance? The caller or callee? Should we provide a way of getting both?
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface Signature {

    /**
     * Returns the declaring class.
     *
     * @return the declaring class
     */
    Class getDeclaringType();

    /**
     * Returns the modifiers for the signature.
     * <p/>
     * Could be used like this:
     * <pre>
     *      boolean isPublic = java.lang.reflect.Modifier.isPublic(signature.getModifiers());
     * </pre>
     *
     * @return the mofifiers
     */
    int getModifiers();

    /**
     * Returns the name (f.e. name of method of field).
     *
     * @return
     */
    String getName();

    /**
     * Returns a string representation of the join point.
     *
     * @return a string representation
     */
    String toString();

    /**
     * Creates a deep copy of the signature.
     *
     * @return a deep copy of the signature
     */
    Signature deepCopy();
}
