/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.ide.eclipse.core;

import org.codehaus.aspectwerkz.transform.inlining.EmittedJoinPoint;
import org.eclipse.jdt.core.IJavaProject;

/**
 * @author avasseur
 *
 */
public interface IWeaverListener {
    
    public void onWeaved(IJavaProject jproject, String className, ClassLoader loader,
            			 EmittedJoinPoint[] emittedJoinPoint,
            			 boolean isTriggered);

}
