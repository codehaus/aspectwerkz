/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package javax.ejb;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Inject annotation for defaulted field and setter injection as per EJB 3 spec, chpt 8.
 * Note that this implementation is very simple and does not provide @Inject annotation elements.
 * The injected resource will be determined from the annotated field type / setter argument type.
 * 
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Inject {
}
