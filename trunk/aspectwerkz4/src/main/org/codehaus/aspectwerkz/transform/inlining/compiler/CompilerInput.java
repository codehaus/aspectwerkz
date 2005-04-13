/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.inlining.compiler;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class CompilerInput {

    public String joinPointClassName;

    public int joinPointInstanceIndex;

    public boolean isOptimizedJoinPoint;

    /**
     * Index on stack of the first target method arg (0 or 1, depends of static target or not
     */
    public int argStartIndex;

    public int callerIndex;
    public String callerClassSignature;

    public int calleeIndex;
    public String calleeClassSignature;

    public CompilerInput() {}

    public CompilerInput(CompilerInput input) {
        joinPointClassName = input.joinPointClassName;
        joinPointInstanceIndex = input.joinPointInstanceIndex;
        isOptimizedJoinPoint = input.isOptimizedJoinPoint;
        argStartIndex = input.argStartIndex;
        callerIndex = input.callerIndex;
        callerClassSignature = input.callerClassSignature;
        calleeIndex = input.calleeIndex;
        calleeClassSignature = input.calleeClassSignature;
    }
}
