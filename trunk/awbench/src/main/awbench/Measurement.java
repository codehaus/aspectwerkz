/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package awbench;

/**
 * Interface for weaved class, to allow some warmup phase for JIT or aspectOf etc 
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public interface Measurement {

    /**
     * Some warm-up code
     * Note: Might need to invoke it once per instance to warmup some perInstance based test
     */
    public void warmup();
}
