/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.cflow;

import java.util.Stack;

/**
 * An abstraction for the JIT gen cflow aspects
 *
 * A concrete JIT gen cflow aspect *class* should be generated per
 * cflow with a consistent naming scheme aka cflowID.
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public abstract class AbstractCflowSystemAspect {

    public Stack m_cflowStack = new Stack();

    public void enter() {
        m_cflowStack.push(Boolean.TRUE);
    }

    public void exit() {
        m_cflowStack.pop();
    }

    public boolean isInCflow() {
        return m_cflowStack.size() > 0;
    }

    public boolean isInCflowBelow() {
        return m_cflowStack.size() == 1;
    }

    public int getCflowID() {
        int indexOf = getClass().getName().lastIndexOf('_');
        if (indexOf > 0) {
            return Integer.parseInt(getClass().getName().substring(indexOf+1));
        } else {
            return 0;
        }
    }
}
