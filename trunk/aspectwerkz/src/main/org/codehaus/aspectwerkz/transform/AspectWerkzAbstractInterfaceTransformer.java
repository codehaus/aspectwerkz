/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

/**
 * Abstract class for interface transformer component
 *
 * Note: derived from JMangler - needs refactoring
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public abstract class AspectWerkzAbstractInterfaceTransformer {

   public abstract void transformInterface(final AspectWerkzExtensionSet es,
        final AspectWerkzUnextendableClassSet cs);

    public String verboseMessage() {
        return this.getClass().getName();
    }

    public void sessionStart() {
        ;
    }

    public void sessionEnd() {
        ;
    }

 }
