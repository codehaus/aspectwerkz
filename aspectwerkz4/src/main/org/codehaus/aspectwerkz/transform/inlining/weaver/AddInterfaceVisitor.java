/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.inlining.weaver;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import org.objectweb.asm.*;
import org.codehaus.aspectwerkz.transform.Context;
import org.codehaus.aspectwerkz.transform.TransformationConstants;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import org.codehaus.aspectwerkz.transform.inlining.ContextImpl;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.definition.InterfaceIntroductionDefinition;
import org.codehaus.aspectwerkz.definition.MixinDefinition;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.expression.PointcutType;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ClassInfoHelper;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.reflect.impl.asm.AsmClassInfo;

/**
 * Adds an interface to the target class.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
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
        if (classFilter(m_classInfo, ctx, m_ctx.getDefinitions())) {
            super.visit(version, access, name, superName, interfaces, sourceFile);
            return;
        }

        final Set interfacesToAdd = new HashSet();
        final Set systemDefinitions = m_ctx.getDefinitions();
        for (Iterator it = systemDefinitions.iterator(); it.hasNext();) {
            SystemDefinition systemDefinition = (SystemDefinition) it.next();
            final List interfaceIntroDefs = systemDefinition.getInterfaceIntroductionDefinitions(ctx);
            for (Iterator it2 = interfaceIntroDefs.iterator(); it2.hasNext();) {
                final InterfaceIntroductionDefinition interfaceIntroDef = (InterfaceIntroductionDefinition) it2.next();
                interfacesToAdd.addAll(interfaceIntroDef.getInterfaceClassNames());
            }
            final List mixinDefinitions = systemDefinition.getMixinDefinitions(ctx);
            for (Iterator it2 = mixinDefinitions.iterator(); it2.hasNext();) {
                final MixinDefinition mixinDef = (MixinDefinition) it2.next();
                final List interfaceList = mixinDef.getInterfaceClassNames();
                for (Iterator it3 = interfaceList.iterator(); it3.hasNext();) {
                    interfacesToAdd.add((String) it3.next());
                }
            }
        }

        for (int i = 0; i < interfaces.length; i++) {
            interfacesToAdd.add(interfaces[i]);
        }

        if (TransformationUtil.hasMethodClash(interfacesToAdd, m_ctx.getLoader())) {
            super.visit(version, access, name, superName, interfaces, sourceFile);
            return;
        }

        int i = 0;
        final String[] newInterfaceArray = new String[interfacesToAdd.size()];
        for (Iterator it = interfacesToAdd.iterator(); it.hasNext();) {
            newInterfaceArray[i++] = (String) it.next();
        }

        for (int j = 0; j < newInterfaceArray.length; j++) {
            newInterfaceArray[j] = newInterfaceArray[j].replace('.', '/');

        }
        super.visit(version, access, name, superName, newInterfaceArray, sourceFile);
        m_ctx.markAsAdvised();
    }

    /**
     * Filters the classes to be transformed.
     *
     * @param classInfo   the class to filter
     * @param ctx         the context
     * @param definitions a set with the definitions
     * @return boolean true if the method should be filtered away
     */
    public static boolean classFilter(final ClassInfo classInfo,
                                      final ExpressionContext ctx,
                                      final Set definitions) {
        for (Iterator it = definitions.iterator(); it.hasNext();) {
            SystemDefinition systemDef = (SystemDefinition) it.next();
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