/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultedAnnotation {

    public String s() default "default";

    public int[] is() default {1,2};

    public Class klass() default ReferencedClass.class;

    public Class[] klass2() default {ReferencedClass[].class, ReferencedClass.class};

    public NestedDefaultedAnnotation nested() default @NestedDefaultedAnnotation(s="default_const");

    public NestedDefaultedAnnotation nested2() default @NestedDefaultedAnnotation;

    static @interface NestedDefaultedAnnotation {
        public String s() default "default_nested";
    }

}
