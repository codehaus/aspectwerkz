/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.inlining.weaver;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

import org.objectweb.asm.*;
import org.codehaus.aspectwerkz.transform.Context;
import org.codehaus.aspectwerkz.transform.TransformationConstants;
import org.codehaus.aspectwerkz.transform.inlining.ContextImpl;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.definition.InterfaceIntroductionDefinition;
import org.codehaus.aspectwerkz.definition.MixinDefinition;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.expression.PointcutType;
import org.codehaus.aspectwerkz.reflect.ClassInfo;

/**
 * Adds an interface to the target class.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class AddInterfaceVisitor extends ClassAdapter implements TransformationConstants {

    private final ContextImpl m_ctx;
    private final ClassInfo m_classInfo;

    /**
     * Creates a new add interface class adapter.
     *
     * @param cv
     * @param classInfo
     * @param ctx
     */
    public AddInterfaceVisitor(final ClassVisitor cv,
                               final ClassInfo classInfo,
                               final Context ctx) {
        super(cv);
        m_classInfo = classInfo;
        m_ctx = (ContextImpl) ctx;
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
    public void visit(final int version,
                      final int access,
                      final String name,
                      final String superName,
                      final String[] interfaces,
                      final String sourceFile) {
        ExpressionContext ctx = new ExpressionContext(PointcutType.WITHIN, m_classInfo, m_classInfo);
        classFilter(m_classInfo, ctx, m_ctx.getDefinitions());

        Set interfacesToAdd = new HashSet();
        Set systemDefinitions = m_ctx.getDefinitions();
        for (Iterator it = systemDefinitions.iterator(); it.hasNext();) {
            SystemDefinition systemDefinition = (SystemDefinition) it.next();
            for (Iterator it2 = systemDefinition.getInterfaceIntroductionDefinitions(ctx).iterator(); it2.hasNext();) {
                interfacesToAdd.addAll(((InterfaceIntroductionDefinition) it2.next()).getInterfaceClassNames());
            }
            for (Iterator it2 = systemDefinition.getMixinDefinitions(ctx).iterator(); it2.hasNext();) {
                interfacesToAdd.addAll(((MixinDefinition) it2.next()).getInterfaceClassNames());
            }
        }

        for (int i = 0; i < interfaces.length; i++) {
            interfacesToAdd.add(interfaces[i]);
        }
        String[] newInterfaceArray = (String[]) interfacesToAdd.toArray(new String[interfacesToAdd.size()]);

        for (int i = 0; i < newInterfaceArray.length; i++) {
            newInterfaceArray[i] = newInterfaceArray[i].replace('.', '/');

        }
        super.visit(version, access, name, superName, newInterfaceArray, sourceFile);
        m_ctx.markAsAdvised();
    }

    /**
     * Filters the classes to be transformed.
     *
     * @param classInfo  the class to filter
     * @param ctx        the context
     * @param definitions a set with the definitions
     * @return boolean true if the method should be filtered away
     */
    public static boolean classFilter(final ClassInfo classInfo,
                                      final ExpressionContext ctx,
                                      final Set definitions) {
        for (Iterator it = definitions.iterator(); it.hasNext();) {
            SystemDefinition systemDef = (SystemDefinition)it.next();
            if (classInfo.isInterface()) {
                return true;
            }
            String className = classInfo.getName().replace('/', '.');
            if (systemDef.inExcludePackage(className)) {
                return true;
            }
            if (!systemDef.inIncludePackage(className)) {
                return true;
            }
            if (systemDef.hasMixin(ctx) || systemDef.hasIntroducedInterface(ctx)) {
                return false;
            }
        }
        return true;
    }
}