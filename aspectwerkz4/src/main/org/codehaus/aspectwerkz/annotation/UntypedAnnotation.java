/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;

import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * Untyped annotation interface. <p/>To be used with JavDoc-style, pure string based, one value only type of annotations.
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public interface UntypedAnnotation {

    /**
     * The raw annotation value
     */
    public String value();

    /**
     * The annotation name as it appears in the source code (f.e. "Untyped" when used as "@Untyped hello")
     */
    public String name();
}