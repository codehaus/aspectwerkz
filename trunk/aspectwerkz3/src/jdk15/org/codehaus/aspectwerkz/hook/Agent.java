/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.hook;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.ClassFileTransformer;

/**
 * Java 1.5 preMain agent
 * Can be used with -javaagent:org.codehaus.aspectwerkz.hook.Agent
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class Agent {

    private static Instrumentation s_instrumentation;

    public static ClassFileTransformer s_transformer = new PreProcessorAdapter();

    public Agent() {}

    public static void premain(String options, Instrumentation instrumentation) {
        s_instrumentation = instrumentation;
        s_instrumentation.addTransformer(s_transformer);
    }

    public static Instrumentation getInstrumentation() {
        return s_instrumentation;
    }

}
