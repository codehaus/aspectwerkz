/**************************************************************************************
 * Copyright (c) Jonas BonŽr, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.inlining;

/**
 * A structure that keeps required information needed to regenerate a JIT joinpoint. The weaver emits this
 * information so that we can add initalization code to the weaved class. Note that EmittedJP are really Emitted -
 * and can be a subset of actual JP (f.e. call, where information is lost in between each weave phase).
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public final class EmittedJoinPoint {

    public final static int NO_LINE_NUMBER = -1;

    private final int joinPointType;
    private final String callerClassName;
    private final String callerMethodName;
    private final String callerMethodDesc;
    private final int callerMethodModifiers;
    private final String calleeClassName;
    private final String calleeMemberName;
    private final String calleeMemberDesc;
    private final int calleeMemberModifiers;
    private final int joinPointHash;
    private final String joinPointClassName;
    private final int lineNumber;

    /**
     * Creates a new instance.
     *
     * @param joinPointType
     * @param callerClassName
     * @param callerMethodName
     * @param callerMethodDesc
     * @param callerMethodModifiers
     * @param calleeClassName
     * @param calleeMemberName
     * @param calleeMemberDesc
     * @param calleeMemberModifiers
     * @param joinPointHash
     * @param joinPointClassName
     */
    public EmittedJoinPoint(final int joinPointType,
                            final String callerClassName,
                            final String callerMethodName,
                            final String callerMethodDesc,
                            final int callerMethodModifiers,
                            final String calleeClassName,
                            final String calleeMemberName,
                            final String calleeMemberDesc,
                            final int calleeMemberModifiers,
                            final int joinPointHash,
                            final String joinPointClassName,
                            final int lineNumber) {
        this.joinPointType = joinPointType;
        this.callerClassName = callerClassName;
        this.callerMethodName = callerMethodName;
        this.callerMethodDesc = callerMethodDesc;
        this.callerMethodModifiers = callerMethodModifiers;
        this.calleeClassName = calleeClassName;
        this.calleeMemberName = calleeMemberName;
        this.calleeMemberDesc = calleeMemberDesc;
        this.calleeMemberModifiers = calleeMemberModifiers;
        this.joinPointHash = joinPointHash;
        this.joinPointClassName = joinPointClassName;
        this.lineNumber = lineNumber;
    }

    public int getJoinPointType() {
        return joinPointType;
    }

    public String getCallerClassName() {
        return callerClassName;
    }

    public String getCallerMethodName() {
        return callerMethodName;
    }

    public String getCallerMethodDesc() {
        return callerMethodDesc;
    }

    public int getCallerMethodModifiers() {
        return callerMethodModifiers;
    }

    public String getCalleeClassName() {
        return calleeClassName;
    }

    public String getCalleeMemberName() {
        return calleeMemberName;
    }

    public String getCalleeMemberDesc() {
        return calleeMemberDesc;
    }

    public int getCalleeMemberModifiers() {
        return calleeMemberModifiers;
    }

    public int getJoinPointHash() {
        return joinPointHash;
    }

    public String getJoinPointClassName() {
        return joinPointClassName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EmittedJoinPoint)) {
            return false;
        }

        final EmittedJoinPoint emittedJoinPoint = (EmittedJoinPoint) o;

        if (calleeMemberModifiers != emittedJoinPoint.calleeMemberModifiers) {
            return false;
        }
        if (callerMethodModifiers != emittedJoinPoint.callerMethodModifiers) {
            return false;
        }
        if (joinPointHash != emittedJoinPoint.joinPointHash) {
            return false;
        }
        if (joinPointType != emittedJoinPoint.joinPointType) {
            return false;
        }
        if (!calleeClassName.equals(emittedJoinPoint.calleeClassName)) {
            return false;
        }
        if (!calleeMemberDesc.equals(emittedJoinPoint.calleeMemberDesc)) {
            return false;
        }
        if (!calleeMemberName.equals(emittedJoinPoint.calleeMemberName)) {
            return false;
        }
        if (!callerClassName.equals(emittedJoinPoint.callerClassName)) {
            return false;
        }
        if (!callerMethodDesc.equals(emittedJoinPoint.callerMethodDesc)) {
            return false;
        }
        if (!callerMethodName.equals(emittedJoinPoint.callerMethodName)) {
            return false;
        }
        if (!joinPointClassName.equals(emittedJoinPoint.joinPointClassName)) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = joinPointType;
        result = 29 * result + callerClassName.hashCode();
        result = 29 * result + callerMethodName.hashCode();
        result = 29 * result + callerMethodDesc.hashCode();
        result = 29 * result + callerMethodModifiers;
        result = 29 * result + calleeClassName.hashCode();
        result = 29 * result + calleeMemberName.hashCode();
        result = 29 * result + calleeMemberDesc.hashCode();
        result = 29 * result + calleeMemberModifiers;
        result = 29 * result + joinPointHash;
        result = 29 * result + joinPointClassName.hashCode();
        return result;
    }
}