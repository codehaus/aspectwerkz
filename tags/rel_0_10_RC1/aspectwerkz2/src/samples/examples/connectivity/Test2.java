/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.connectivity;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @aspectwerkz.introduction-def name=test2 implementation=examples.connectivity.Test2Impl deployment-model=perInstance
 * attribute=test2
 */
public interface Test2 {
    String test2();
}
