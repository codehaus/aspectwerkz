/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;

/**
 * This duplicated interface allows to refer to AW AOP annotations (@Aspect etc)
 * using their full qualified name, while when using the doclet style, we refer
 * to them using their nickname.
 *
 * ******** NOTE: do not use constants since they will get inlined by the compiler...
 *
 * TODO: kick out nickname ? How - this one makes sense for Annotations.get... runtime API.
 * TODO: else use a runtime java version check ?
 * TODO: what happens when we want to deploy AW 1.4 aspects in a 1.5 runtime (ADK)
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public abstract class AOPAnnotationConstants {

    public static String ANNOTATION_ASPECT() { return"org.codehaus.aspectwerkz.annotation.Aspect"; }
    public static String ANNOTATION_AROUND() { return"org.codehaus.aspectwerkz.annotation.Around"; }
    public static String ANNOTATION_BEFORE() { return"org.codehaus.aspectwerkz.annotation.Before"; }
    public static String ANNOTATION_AFTER() { return"org.codehaus.aspectwerkz.annotation.After"; }
    public static String ANNOTATION_AFTER_FINALLY() { return"org.codehaus.aspectwerkz.annotation.AfterFinally"; }
    public static String ANNOTATION_AFTER_RETURNING() { return"org.codehaus.aspectwerkz.annotation.AfterReturning"; }
    public static String ANNOTATION_AFTER_THROWING() { return"org.codehaus.aspectwerkz.annotation.AfterThrowing"; }
    public static String ANNOTATION_EXPRESSION() { return"org.codehaus.aspectwerkz.annotation.Expression"; }

    // TODO change implements to introduce
    public static String ANNOTATION_IMPLEMENTS() { return"org.codehaus.aspectwerkz.annotation.Implements"; }
    public static String ANNOTATION_INTRODUCE() { return"org.codehaus.aspectwerkz.annotation.Introduce"; }
}
