/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.cflow;

import java.util.Stack;

/**
 * An abstraction for the JIT gen cflow aspects.
 * <p/>
 * A concrete JIT gen cflow aspect *class* will be generated per
 * cflow sub expression with a consistent naming scheme aka cflowID.
 * <p/>
 * The concrete cflow class will extends this one and implements two static methods.
 * See the sample nested class.
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public abstract class AbstractCflowSystemAspect {

    //TODO do we really need a stack ? I think that an int increment wrapped in a ThreadLocal
    // will be ok. The stack might only be needed for perCflow deployments
    public ThreadLocal m_cflowStackLocal = new ThreadLocal() {
        protected Object initialValue() {
            return new Stack();
        }
    };

    /**
     * before advice when entering this cflow
     */
    public void enter() {
        ((Stack)m_cflowStackLocal.get()).push(Boolean.TRUE);
    }

    /**
     * after finally advice when exiting this cflow
     */
    public void exit() {
        ((Stack)m_cflowStackLocal.get()).pop();
    }

    /**
     * @return true if in the cflow
     */
    public boolean inCflow() {
        return ((Stack)m_cflowStackLocal.get()).size() > 0;
    }

    /**
     * @return true if in the cflowbelow
     */
    public boolean inCflowBelow() {
        return ((Stack)m_cflowStackLocal.get()).size() == 1;
    }

    /**
     * Sample jit cflow aspect that will gets generated.
     *
     * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
     */
    private static class Cflow_sample extends AbstractCflowSystemAspect {

        private static AbstractCflowSystemAspect INSTANCE;

        public Cflow_sample() {
            super();
            INSTANCE = this;
        }

        /**
         * this method will be invoked by the JIT joinpoint
         */
        public static boolean isInCflow() {
            return INSTANCE.inCflow();
        }

        /**
         * this method will be invoked by the JIT joinpoint
         */
        public static boolean isInCflowBelow() {
            return INSTANCE.inCflowBelow();
        }
    }

}
