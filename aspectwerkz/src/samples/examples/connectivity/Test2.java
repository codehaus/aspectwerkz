/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.connectivity;

/**
 * @aspectwerkz.introduction-def name=test2
 *                               implementation=examples.connectivity.Test2Impl
 *                               deployment-model=perInstance
 *                               attribute=test2
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public interface Test2 {
    String test2();
}