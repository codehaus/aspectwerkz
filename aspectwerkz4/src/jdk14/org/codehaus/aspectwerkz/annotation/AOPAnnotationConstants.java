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
 * TODO: kick out nickname ? How - this one makes sense for Annotations.get... runtime API.
 * TODO: else use a runtime java version check ?
 * TODO: what happens when we want to deploy AW 1.4 aspects in a 1.5 runtime (ADK)
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public interface AOPAnnotationConstants {

    public static final String ANNOTATION_ASPECT = "Aspect";
    public static final String ANNOTATION_AROUND = "Around";
    public static final String ANNOTATION_BEFORE = "Before";
    public static final String ANNOTATION_AFTER = "After";
    public static final String ANNOTATION_AFTER_FINALLY = "AfterFinally";
    public static final String ANNOTATION_AFTER_RETURNING = "AfterReturning";
    public static final String ANNOTATION_AFTER_THROWING = "AfterThrowing";
    public static final String ANNOTATION_EXPRESSION = "Expression";

    // TODO change implements to introduce
    public static final String ANNOTATION_IMPLEMENTS = "Implements";
    public static final String ANNOTATION_INTRODUCE = "Introduce";
}
