/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.inlining.spi;

import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.definition.AspectDefinition;
import org.codehaus.aspectwerkz.transform.inlining.AdviceMethodInfo;
import org.codehaus.aspectwerkz.transform.inlining.AspectInfo;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.ClassWriter;

/**
 * TODO document
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public interface AspectModel {
    /**
     * Returns the aspect model type, which is an id for the the special aspect model, can be anything as long
     * as it is unique.
     *
     * @return the aspect model type id
     */
    String getAspectModelType();

    /**
     * Defines the aspect and adds definition to the aspect definition.
     *
     * @param classInfo
     * @param aspectDef
     * @param loader
     */
    void defineAspect(ClassInfo classInfo, AspectDefinition aspectDef, ClassLoader loader);

    /**
     * Returns info about the closure class, name and type (interface or class).
     *
     * @return the closure class info
     */
    AroundClosureClassInfo getAroundClosureClassInfo();

    /**
     * Creates the methods required to implement or extend to implement the closure for the specific aspect model type.
     *
     * @param cw
     * @param className
     */
    void createMandatoryMethods(ClassWriter cw, String className);

    /**
     * Creates invocation of the super class for the around closure.
     * <p/>
     * E.g. the invocation of super(..) in the constructor.
     * <p/>
     * Only needed to be implemented if the around closure base class is really a base class and not an interface.
     *
     * @param cv
     */
    void createInitAroundClosureSuperClass(CodeVisitor cv);

    /**
     * Creates host (field in the jit jointpoint class f.e.) for an aspect instance.
     *
     * @param cw
     * @param aspectInfo
     * @param joinPointClassName
     */
    void createAspectHost(ClassWriter cw, AspectInfo aspectInfo, String joinPointClassName);

    /**
     * Creates instantiation of an aspect instance.
     *
     * @param cv
     * @param aspectInfo
     * @param joinPointClassName
     */
    void createAspectInstantiation(CodeVisitor cv, AspectInfo aspectInfo, String joinPointClassName);

    /**
     * Handles the arguments to the after advice.
     *
     * @param cv
     * @param adviceMethodInfo
     */
    void createBeforeAdviceArgumentHandling(CodeVisitor cv, AdviceMethodInfo adviceMethodInfo);

    /**
     * Handles the arguments to the after advice.
     *
     * @param cv
     * @param adviceMethodInfo
     */
    void createAfterAdviceArgumentHandling(CodeVisitor cv, AdviceMethodInfo adviceMethodInfo);

    /**
     * Should return true if the aspect model requires that Runtime Type Information (RTTI) is build up
     * for the join point. Needed for reflective systems and systems that does not support f.e. args() binding.
     *
     * @return
     */
    boolean requiresRttiInfo();

    /**
     * Checks if a method is an advice method of a regular one.
     *
     * @param method
     * @param adviceName
     * @return
     */
//    boolean matchMethodAsAdvice(MethodInfo method, String adviceName);

    /**
     * Info about the around closure class or interface for this specific aspect model.
     *
     * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
     */
    public static class AroundClosureClassInfo {
        public static final String INTERFACE = "interface";
        public static final String CLASS = "class";

        private final String m_className;
        private final String m_type;

        public AroundClosureClassInfo(final String className, final String type) {
            m_className = className;
            m_type = type;
        }

        public String getClassName() {
            return m_className;
        }

        public boolean isClass() {
            return m_type.equals(CLASS);
        }

        public boolean isInterface() {
            return m_type.equals(INTERFACE);
        }
    }
}
