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

    public static final String ANNOTATION_ASPECT = "org.codehaus.aspectwerkz.annotation.Aspect";
    public static final String ANNOTATION_AROUND = "org.codehaus.aspectwerkz.annotation.Around";
    public static final String ANNOTATION_BEFORE = "org.codehaus.aspectwerkz.annotation.Before";
    public static final String ANNOTATION_AFTER = "org.codehaus.aspectwerkz.annotation.After";
    public static final String ANNOTATION_AFTER_FINALLY = "org.codehaus.aspectwerkz.annotation.AfterFinally";
    public static final String ANNOTATION_AFTER_RETURNING = "org.codehaus.aspectwerkz.annotation.AfterReturning";
    public static final String ANNOTATION_AFTER_THROWING = "org.codehaus.aspectwerkz.annotation.AfterThrowing";
    public static final String ANNOTATION_EXPRESSION = "org.codehaus.aspectwerkz.annotation.Expression";

    // TODO change implements to introduce
    public static final String ANNOTATION_IMPLEMENTS = "org.codehaus.aspectwerkz.annotation.Implements";
    public static final String ANNOTATION_INTRODUCE = "org.codehaus.aspectwerkz.annotation.Introduce";
}
