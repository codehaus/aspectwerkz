/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.delegation;

import org.codehaus.aspectwerkz.definition.InterfaceIntroductionDefinition;
import org.codehaus.aspectwerkz.definition.IntroductionDefinition;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.expression.PointcutType;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.impl.javassist.JavassistClassInfo;
import org.codehaus.aspectwerkz.transform.Context;
import org.codehaus.aspectwerkz.transform.Transformer;

import java.util.Iterator;
import java.util.List;

import javassist.CtClass;
import javassist.NotFoundException;

/**
 * Adds an interfaces to classes.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public final class AddInterfaceTransformer implements Transformer {
    /**
     * Adds an interfaces to the classes specified.
     * 
     * @param context the transformation context
     * @param klass the class
     */
    public void transform(final Context context, final Klass klass) {
        List definitions = context.getDefinitions();

        // loop over all the definitions
        for (Iterator it = definitions.iterator(); it.hasNext();) {
            SystemDefinition definition = (SystemDefinition) it.next();
            final CtClass ctClass = klass.getCtClass();
            ClassInfo classInfo = JavassistClassInfo.getClassInfo(ctClass, context.getLoader());
            ExpressionContext ctx = new ExpressionContext(PointcutType.ANY, classInfo, classInfo);
            if (classFilter(ctClass, ctx, definition)) {
                continue;
            }
            addInterfaceIntroductions(definition, ctClass, context, ctx);
        }
    }

    /**
     * Adds the interface introductions to the class.
     * 
     * @param definition the definition
     * @param ctClass the class
     * @param context the TF context
     * @param ctx the context
     */
    private void addInterfaceIntroductions(
        final SystemDefinition definition,
        final CtClass ctClass,
        final Context context,
        final ExpressionContext ctx) {
        boolean isClassAdvised = false;
        List interfaceIntroDefs = definition.getInterfaceIntroductionDefinitions(ctx);
        for (Iterator it = interfaceIntroDefs.iterator(); it.hasNext();) {
            InterfaceIntroductionDefinition introductionDef = (InterfaceIntroductionDefinition) it.next();
            List interfaceClassNames = introductionDef.getInterfaceClassNames();
            if (addInterfaces(interfaceClassNames, ctClass)) {
                isClassAdvised = true;
            }
        }
        List introDefs = definition.getIntroductionDefinitions(ctx);
        for (Iterator it = introDefs.iterator(); it.hasNext();) {
            IntroductionDefinition introductionDef = (IntroductionDefinition) it.next();
            List interfaceClassNames = introductionDef.getInterfaceClassNames();
            if (addInterfaces(interfaceClassNames, ctClass)) {
                isClassAdvised = true;
            }
        }
        if (isClassAdvised) {
            context.markAsAdvised();

            // weaved class might use the added interface to match pointcuts so mark the class info as dirty
            JavassistClassInfo.markDirty(ctClass, context.getLoader());
        }
    }

    /**
     * Adds the interfaces to the to target class.
     * 
     * @param interfaceClassNames
     * @param ctClass
     * @return
     */
    private boolean addInterfaces(final List interfaceClassNames, final CtClass ctClass) {
        boolean isClassAdvised = false;
        for (Iterator it = interfaceClassNames.iterator(); it.hasNext();) {
            String className = (String) it.next();
            if (implementsInterface(ctClass, className)) {
                continue;
            }
            if (className != null) {
                try {
                    ctClass.addInterface(ctClass.getClassPool().get(className));
                } catch (NotFoundException e) {
                    throw new WrappedRuntimeException(e);
                }
                isClassAdvised = true;
            }
        }
        return isClassAdvised;
    }

    /**
     * Checks if a class implements an interface.
     * 
     * @param ctClass ConstantUtf8 constant
     * @return true if the class implements the interface
     */
    private boolean implementsInterface(final CtClass ctClass, final String interfaceName) {
        try {
            CtClass[] interfaces = ctClass.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                if (interfaces[i].getName().replace('/', '.').equals(interfaceName)) {
                    return true;
                }
            }
            return false;
        } catch (NotFoundException e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Filters the classes to be transformed.
     * 
     * @param ctClass the class to filter
     * @param ctx the context
     * @param definition the definition
     * @return boolean true if the method should be filtered away
     */
    public static boolean classFilter(
        final CtClass ctClass,
        final ExpressionContext ctx,
        final SystemDefinition definition) {
        if (ctClass.isInterface()) {
            return true;
        }
        String className = ctClass.getName().replace('/', '.');
        if (definition.inExcludePackage(className)) {
            return true;
        }
        if (!definition.inIncludePackage(className)) {
            return true;
        }
        if (definition.isIntroduced(ctx) || definition.isInterfaceIntroduced(ctx)) {
            return false;
        }
        return true;
    }
}