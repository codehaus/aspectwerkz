/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.inlining.weaver;

import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Set;
import java.util.List;

import org.objectweb.asm.*;
import org.codehaus.aspectwerkz.transform.Context;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import org.codehaus.aspectwerkz.transform.TransformationConstants;
import org.codehaus.aspectwerkz.transform.inlining.ContextImpl;
import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.definition.MixinDefinition;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.expression.PointcutType;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;

/**
 * TODO document
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class AddMixinMethodsVisitor extends ClassAdapter implements TransformationConstants {

    private final ContextImpl m_ctx;
    private String m_declaringTypeName;
    private final ClassInfo m_classInfo;
    private final Set m_addedMethods;
    private ExpressionContext m_expressionContext;

    /**
     * Creates a new class adapter.
     *
     * @param cv
     * @param classInfo
     * @param ctx
     * @param addedMethods
     */
    public AddMixinMethodsVisitor(final ClassVisitor cv,
                                final ClassInfo classInfo,
                                final Context ctx,
                                final Set addedMethods) {
        super(cv);
        m_classInfo = classInfo;
        m_ctx = (ContextImpl) ctx;
        m_addedMethods = addedMethods;
        m_expressionContext = new ExpressionContext(PointcutType.WITHIN, m_classInfo, m_classInfo);
    }

    /**
     * Visits the class.
     *
     * @param access
     * @param name
     * @param superName
     * @param interfaces
     * @param sourceFile
     */
    public void visit(final int version, final int access,
                      final String name,
                      final String superName,
                      final String[] interfaces,
                      final String sourceFile) {
        m_declaringTypeName = name;
        super.visit(version, access, name, superName, interfaces, sourceFile);

        Set systemDefs = m_ctx.getDefinitions();
        for (Iterator it = systemDefs.iterator(); it.hasNext();) {
            SystemDefinition systemDef = (SystemDefinition) it.next();
            List mixinDefs = systemDef.getMixinDefinitions(m_expressionContext);
            for (Iterator it2 = mixinDefs.iterator(); it2.hasNext();) {
                MixinDefinition mixinDef = (MixinDefinition) it2.next();
                List methodsToIntroduce = mixinDef.getMethodsToIntroduce();
                for (Iterator it3 = methodsToIntroduce.iterator(); it3.hasNext();) {
                    MethodInfo methodInfo = (MethodInfo) it3.next();
                    cv.visitMethod(
                            ACC_PUBLIC | ACC_FINAL,
                            methodInfo.getName(),
                            methodInfo.getSignature(),
                            null,
                            null
                    );
                }
            }
        }
    }



    /**
     * Filters the classes to be transformed.
     *
     * @param classInfo  the class to filter
     * @param ctx        the context
     * @param definition the definition
     * @return boolean true if the method should be filtered away
     */
    public static boolean classFilter(final ClassInfo classInfo,
                                      final ExpressionContext ctx,
                                      final SystemDefinition definition) {
        if (classInfo.isInterface()) {
            return true;
        }
        String className = classInfo.getName().replace('/', '.');
        if (definition.inExcludePackage(className)) {
            return true;
        }
        if (!definition.inIncludePackage(className)) {
            return true;
        }
        if (definition.hasMixin(ctx)) {
            return false;
        }
        return true;
    }
}