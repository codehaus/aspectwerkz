/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.extension.hotswap;

import java.util.Iterator;

import org.codehaus.aspectwerkz.transform.inlining.compiler.JoinPointFactory;
import org.codehaus.aspectwerkz.transform.inlining.deployer.Redefiner;
import org.codehaus.aspectwerkz.transform.inlining.deployer.ChangeSet;

/**
 * Redefines classes using Java 1.4 HotSwap.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public class HotSwapRedefiner implements Redefiner {

    /**
     * Redefines all classes affected by the change set according to the rules defined in the change set.
     *
     * @param changeSet
     */
    public void redefine(final ChangeSet changeSet) {
        for (Iterator it = changeSet.getElements().iterator(); it.hasNext();) {
            ChangeSet.Element changeSetElement = (ChangeSet.Element) it.next();
            final byte[] bytecode = JoinPointFactory.redefineJoinPoint(changeSetElement.getCompilationInfo());
            HotSwapClient.hotswap(changeSetElement.getJoinPointInfo().getJoinPointClass(), bytecode);
        }
    }
}
