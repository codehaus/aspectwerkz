/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.ejb3;

/**
 * Extension to the spec.
 * 
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public @interface AroundInvokeAOP {
    // a real poincut that we will OR with the regular EJB 3 interceptor bindings
    String value();
}
