/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint;

/**
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface JoinPoint {

    static final String METHOD_EXECUTION = "METHOD_EXECUTION";
    static final String METHOD_CALL = "METHOD_CALL";
    static final String CONSTRUCTOR_EXECUTION = "CONSTRUCTOR_EXECUTION";
    static final String CONSTRUCTOR_CALL = "CONSTRUCTOR_CALL";
    static final String FIELD_SET = "FIELD_SET";
    static final String FIELD_GET = "FIELD_GET";
    static final String CATCH_CLAUSE = "CATCH_CLAUSE";
    static final String STATIC_INITALIZATION = "STATIC_INITALIZATION";
    static final String THROWS = "THROWS";

    /**
     * Walks through the pointcuts and invokes all its advices. When the last
     * advice of the last pointcut has been invoked, the original method is
     * invoked. Is called recursively.
     *
     * @return the result from the next invocation
     * @throws Throwable
     */
    Object proceed() throws Throwable;

    /**
     * Returns the signature for the join point.
     *
     * @return the signature
     */
    Signature getSignature();

    /**
     * Returns the target instance or 'this'.
     * If the join point is executing in a static context it returns null.
     *
     * @return the target instance
     */
    Object getTargetInstance();

    /**
     * Returns the target class.
     *
     * @return the target class
     */
    Class getTargetClass();

    /**
     * Returns the join point type.
     *
     * @return the type
     */
    String getType();

    /**
     * Returns a string representation of the join point.
     *
     * @return a string representation
     */
    String toString();


    /**
     * @TODO: I rather not have this method in the public API
     *
     * Initializes the join point.
     * <p/>
     * Needs to be invoked before every new advice chain invocation is made.
     * <p/>
     * Sets the RTTI for the join point and does some additional setup.
     *
     * @param targetInstance
     * @param parameters
     */
    void initialize(Object targetInstance, Object[] parameters);
}
