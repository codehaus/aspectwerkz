/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;

import java.util.Set;

/**
 * An interface for Annotation which needs to remember the call signature of the member they apply to. This is used by
 * 'Expression' and 'Before/Around/After' to support args() binding.
 * 
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public interface ParameterizedAnnotationProxy {

    public void addArgument(String argName, String className);

    public Set getArgumentNames();

    public String getArgumentType(String parameterName);

}