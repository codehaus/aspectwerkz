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
     * @param aspectClassInfo
     * @param aspectDef
     * @param loader
     */
    void defineAspect(ClassInfo aspectClassInfo, AspectDefinition aspectDef, ClassLoader loader);

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
    void createInvocationOfAroundClosureSuperClass(CodeVisitor cv);

    /**
     * Creates aspect reference field (field in the jit jointpoint class f.e.) for an aspect instance.
     *
     * @param cw
     * @param aspectInfo
     * @param joinPointClassName
     */
    void createAspectReferenceField(ClassWriter cw, AspectInfo aspectInfo, String joinPointClassName);

    /**
     * Creates instantiation of an aspect instance.
     *
     * @param cv
     * @param aspectInfo
     * @param joinPointClassName
     */
    void createAspectInstantiation(CodeVisitor cv, AspectInfo aspectInfo, String joinPointClassName);

    /**
     * Handles the arguments to the around advice.
     *
     * @param cv
     * @param adviceMethodInfo
     */
    void createAroundAdviceArgumentHandling(CodeVisitor cv, AdviceMethodInfo adviceMethodInfo);

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
    boolean requiresReflectiveInfo();

    /**
     * Info about the around closure class or interface for this specific aspect model.
     *
     * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
     */
    public static class AroundClosureClassInfo {
        private final String m_superClassName;
        private final String[] m_interfaceNames;
        public AroundClosureClassInfo(final String superClassName, final String[] interfaceNames) {
            m_superClassName = superClassName;
            m_interfaceNames = interfaceNames;
        }

        public String getSuperClassName() {
            return m_superClassName;
        }

        public String[] getInterfaceNames() {
            return m_interfaceNames;
        }

        /**
         * Type safe enum for the around closure class type.
         */
        public static class Type {
            public static final Type INTERFACE = new Type("INTERFACE");
            public static final Type CLASS = new Type("CLASS");
            private final String m_name;
            private Type(String name) {
                m_name = name;
            }
            public String toString() {
                return m_name;
            }
        }

    }
}
