/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.delegation;

import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.expression.PointcutType;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.MemberInfo;
import org.codehaus.aspectwerkz.reflect.impl.javassist.JavassistClassInfo;
import org.codehaus.aspectwerkz.reflect.impl.javassist.JavassistConstructorInfo;
import org.codehaus.aspectwerkz.reflect.impl.javassist.JavassistMethodInfo;
import org.codehaus.aspectwerkz.transform.Context;
import org.codehaus.aspectwerkz.transform.Klass;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import org.codehaus.aspectwerkz.transform.Transformer;

import java.util.Iterator;
import java.util.List;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.Handler;

/**
 * Advises HANDLER join points.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class HandlerTransformer implements Transformer {
    /**
     * The join point index.
     */

    //AXprivate int m_joinPointIndex;
    /**
     * Transforms the call side pointcuts.
     * 
     * @param context
     *            the transformation context
     * @param klass
     *            the class set.
     */
    public void transform(final Context context, final Klass klass)
            throws NotFoundException, CannotCompileException {
        List definitions = context.getDefinitions();

        //AXm_joinPointIndex =
        // TransformationUtil.getJoinPointIndex(klass.getCtClass()); //TODO
        // thread safe reentrant
        for (Iterator it = definitions.iterator(); it.hasNext();) {
            final SystemDefinition definition = (SystemDefinition) it.next();
            final CtClass ctClass = klass.getCtClass();
            ClassInfo classInfo = JavassistClassInfo.getClassInfo(ctClass,
                    context.getLoader());
            if (classFilter(definition, new ExpressionContext(
                    PointcutType.HANDLER, classInfo, classInfo), ctClass)) {
                continue;
            }
            ctClass.instrument(new ExprEditor() {
                public void edit(Handler handlerExpr)
                        throws CannotCompileException {
                    try {
                        CtClass exceptionClass = null;
                        try {
                            exceptionClass = handlerExpr.getType();
                        } catch (NullPointerException e) {
                            return;
                        }
                        CtBehavior where = null;
                        try {
                            where = handlerExpr.where();
                        } catch (RuntimeException e) {
                            // <clinit> access leads to a bug in Javassist
                            where = ctClass.getClassInitializer();
                        }
                        MemberInfo withinMethodInfo = null;
                        if (where instanceof CtMethod) {
                            withinMethodInfo = JavassistMethodInfo
                                    .getMethodInfo((CtMethod) where, context
                                            .getLoader());
                        } else if (where instanceof CtConstructor) {
                            withinMethodInfo = JavassistConstructorInfo
                                    .getConstructorInfo((CtConstructor) where,
                                            context.getLoader());
                        }
                        ClassInfo exceptionClassInfo = JavassistClassInfo
                                .getClassInfo(exceptionClass, context
                                        .getLoader());
                        ExpressionContext ctx = new ExpressionContext(
                                PointcutType.HANDLER, exceptionClassInfo,
                                withinMethodInfo);
                        if (definition.hasPointcut(ctx)) {
                            // call the wrapper method instead of the callee
                            // method
                            StringBuffer body = new StringBuffer();
                            body
                                    .append(TransformationUtil.JOIN_POINT_MANAGER_FIELD);
                            body.append('.');
                            body
                                    .append(TransformationUtil.PROCEED_WITH_HANDLER_JOIN_POINT_METHOD);
                            body.append('(');

                            // TODO: unique hash is needed, based on: executing
                            // class, executing method, catch clause (and
                            // sequence number?)
                            body.append(JavassistHelper
                                    .calculateHash(exceptionClass));
                            body.append(',');
                            body.append(klass.getJoinPointIndex());
                            if (Modifier.isStatic(where.getModifiers())) {
                                body.append(", $1, (Object)null, \"");
                            } else {
                                body.append(", $1, this, \"");
                            }

                            // TODO: use a better signature (or remove)
                            body.append(exceptionClass.getName().replace('/',
                                    '.'));
                            body.append("\");");
                            handlerExpr.insertBefore(body.toString());
                            context.markAsAdvised();
                            klass.incrementJoinPointIndex();
                        }
                    } catch (NotFoundException nfe) {
                        nfe.printStackTrace();
                    }
                }
            });
        }

        //TransformationUtil.setJoinPointIndex(klass.getCtClass(),
        // m_joinPointIndex);
        klass.flushJoinPointIndex();
        context.setBytecode(klass.getBytecode());
    }

    /**
     * Filters the classes to be transformed.
     * 
     * @param definition
     *            the definition
     * @param ctx
     *            the context
     * @param cg
     *            the class to filter
     * @return boolean true if the method should be filtered away
     */
    public static boolean classFilter(final SystemDefinition definition,
            final ExpressionContext ctx, final CtClass cg) {
        if (cg.isInterface()) {
            return true;
        }
        String className = cg.getName().replace('/', '.');
        if (definition.inExcludePackage(className)) {
            return true;
        }
        if (!definition.inIncludePackage(className)) {
            return true;
        }
        if (definition.isAdvised(ctx)) {
            return false;
        }
        return true;
    }
}