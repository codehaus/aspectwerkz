/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.inlining.deployer;

import org.codehaus.aspectwerkz.util.ContextClassLoader;

/**
 * Factory for the different redefiner implementations.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class RedefinerFactory {
    private static final String HOT_SWAP_REDEFINER_CLASS_NAME =
            "org.codehaus.aspectwerkz.extension.hotswap.HotSwapRedefiner";

    /**
     * Creates a new redefiner instance.
     *
     * @return the redefiner instance
     */
    public static Redefiner newRedefiner(final Type type) {
        if (type.equals(Type.HOTSWAP)) {
            try {
                Class redefinerClass = ContextClassLoader.loadClass(HOT_SWAP_REDEFINER_CLASS_NAME);
                return (Redefiner) redefinerClass.newInstance();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(
                        "redefiner class [HotSwapRedefiner] could not be found on classpath, make sure you have the aspectwerkz extensions jar file in your classpath"
                );
            } catch (Exception e) {
                throw new RuntimeException("redefiner class [HotSwapRedefiner] could not be instantiated");
            }

        } else if (type.equals(Type.JVMTI)) {
            throw new UnsupportedOperationException("JVMTI is not supported yet");
        } else {
            throw new UnsupportedOperationException("unknown redefiner type: " + type.toString());
        }
    }

    /**
     * Type-safe enum for the different redefiner implementations.
     *
     * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
     */
    public static class Type {
        public static final Type HOTSWAP = new Type("HOTSWAP");
        public static final Type JVMTI = new Type("JVMTI");

        private final String m_name;

        private Type(String name) {
            m_name = name;
        }

        public String toString() {
            return m_name;
        }
    }
}
