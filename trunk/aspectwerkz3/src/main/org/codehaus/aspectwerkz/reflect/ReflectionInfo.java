/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.reflect;

import java.util.List;

/**
 * Base interface for the reflection info hierarchy.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public interface ReflectionInfo {

    /**
     * Returns the name of the class.
     * 
     * @return the name of the class
     */
    String getName();

    /**
     * Returns the class modifiers.
     * 
     * @return the class modifiers
     */
    int getModifiers();

    /**
     * Returns the annotation infos.
     * 
     * @return the annotations infos
     */
    List getAnnotations();
}