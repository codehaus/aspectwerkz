/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.expression.PointcutType;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ConstructorInfo;
import org.codehaus.aspectwerkz.reflect.MemberInfo;
import org.codehaus.aspectwerkz.reflect.impl.javassist.JavassistClassInfo;
import org.codehaus.aspectwerkz.reflect.impl.javassist.JavassistConstructorInfo;
import org.codehaus.aspectwerkz.reflect.impl.javassist.JavassistMethodInfo;

import java.util.Iterator;
import java.util.List;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.NewExpr;

/**
 * Advises constructor CALL join points.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class ConstructorCallTransformer implements Transformer {
    /**
     * The join point index.
     */

    //AXprivate int m_joinPointIndex;

    /**
     * Transforms the call side pointcuts.
     *
     * @param context the transformation context
     * @param klass   the class set.
     */
    public void transform(final Context context, final Klass klass) throws NotFoundException, CannotCompileException {
        List definitions = context.getDefinitions();

        //AXm_joinPointIndex = TransformationUtil.getJoinPointIndex(klass.getCtClass()); //TODO is not thread safe / reentrant
        for (Iterator it = definitions.iterator(); it.hasNext();) {
            final SystemDefinition definition = (SystemDefinition)it.next();
            final CtClass ctClass = klass.getCtClass();
            ClassInfo classInfo = JavassistClassInfo.getClassInfo(ctClass, context.getLoader());
            if (classFilter(definition, new ExpressionContext(PointcutType.CALL, classInfo, classInfo), ctClass)) {
                continue;
            }
            ctClass.instrument(
                    new ExprEditor() {
                        public void edit(NewExpr newExpr) throws CannotCompileException {
                            try {
                                CtBehavior where = null;
                                try {
                                    where = newExpr.where();
                                } catch (RuntimeException e) {
                                    // <clinit> access leads to a bug in Javassist
                                    where = ctClass.getClassInitializer();
                                }

                                // filter caller methods
                                if (methodFilterCaller(where)) {
                                    return;
                                }
                                CtConstructor ctConstructor = newExpr.getConstructor();
                                String calleeClassName = newExpr.getClassName();

                                // filter callee classes
                                if (!definition.inIncludePackage(calleeClassName)) {
                                    return;
                                }

                                // filter the constructors
                                if (constructorFilter(ctConstructor)) {
                                    return;
                                }

                                // create the caller method info
                                MemberInfo withinMethodInfo = null;
                                boolean isWithinInfoAMethod = true;
                                if (where instanceof CtMethod) {
                                    withinMethodInfo = JavassistMethodInfo.getMethodInfo(
                                            (CtMethod)where,
                                            context.getLoader()
                                    );
                                } else if (where instanceof CtConstructor) {
                                    withinMethodInfo =
                                    JavassistConstructorInfo.getConstructorInfo(
                                            (CtConstructor)where,
                                            context.getLoader()
                                    );
                                    isWithinInfoAMethod = false;
                                }

                                // create the constructor info
                                CtConstructor constructor = newExpr.getConstructor();
                                ConstructorInfo calleeSideConstructorInfo = JavassistConstructorInfo.getConstructorInfo(
                                        constructor,
                                        context
                                        .getLoader()
                                );
                                ExpressionContext ctx = new ExpressionContext(
                                        PointcutType.CALL, calleeSideConstructorInfo,
                                        withinMethodInfo
                                );

                                // is this a caller side method pointcut?
                                if (definition.hasPointcut(ctx)) {
                                    // check the callee class is not the same as target class, if that is the case
                                    // then we have have class loaded and set in the ___AW_clazz already
                                    String declaringClassMethodName = TransformationUtil.STATIC_CLASS_FIELD;
                                    CtClass declaringClass = ctConstructor.getDeclaringClass();
                                    if (!declaringClass.getName().replace('/', '.').equals(
                                            where.getDeclaringClass()
                                            .getName()
                                            .replace('/', '.')
                                    )) {
                                        declaringClassMethodName =
                                        addCalleeMethodDeclaringClassField(ctClass, ctConstructor);
                                    }

                                    // call the wrapper method instead of the callee method
                                    StringBuffer body = new StringBuffer();
                                    body.append('{');
                                    if (ctConstructor.getParameterTypes().length > 0) {
                                        body.append("Object[] args = $args; ");
                                    } else {
                                        body.append("Object[] args = null; ");
                                    }
                                    body.append("Class declaringClass = ");
                                    body.append(declaringClassMethodName);
                                    body.append("; ");
                                    if (Modifier.isStatic(where.getModifiers())) {
                                        body.append("Object nullObject = null;");
                                    }
                                    body.append("$_ = ($r)");
                                    body.append(TransformationUtil.JOIN_POINT_MANAGER_FIELD);
                                    body.append('.');
                                    body.append(TransformationUtil.PROCEED_WITH_CALL_JOIN_POINT_METHOD);
                                    body.append('(');
                                    body.append(TransformationUtil.calculateHash(ctConstructor));
                                    body.append(',');
                                    body.append(klass.getJoinPointIndex());
                                    body.append(", args, ");
                                    body.append(TransformationUtil.STATIC_CLASS_FIELD);
                                    if (Modifier.isStatic(where.getModifiers())) {
                                        body.append(", nullObject, ");
                                    } else {
                                        body.append(", this, ");
                                    }
                                    body.append("declaringClass, $0, \"");
                                    body.append(where.getName());
                                    body.append("\",\"");
                                    body.append(where.getSignature());
                                    body.append("\",");
                                    body.append(TransformationUtil.JOIN_POINT_TYPE_CONSTRUCTOR_CALL);
                                    body.append("); }");
                                    newExpr.replace(body.toString());
                                    context.markAsAdvised();
                                    klass.incrementJoinPointIndex();
                                }
                            } catch (NotFoundException nfe) {
                                nfe.printStackTrace();

                                // TODO: should we swallow this exception?
                            }
                        }
                    }
            );
        }

        //TransformationUtil.setJoinPointIndex(klass.getCtClass(), m_joinPointIndex);
        klass.flushJoinPointIndex();
    }

    /**
     * Creates a new static class field, for the declaring class of the constructor.
     *
     * @param ctClass       the class
     * @param ctConstructor the constructor
     * @return the name of the field
     */
    private String addCalleeMethodDeclaringClassField(final CtClass ctClass, final CtConstructor ctConstructor)
            throws NotFoundException, CannotCompileException {
        String fieldName = TransformationUtil.STATIC_CLASS_FIELD + TransformationUtil.DELIMITER + "init"
                           + TransformationUtil.DELIMITER
                           + ctConstructor.getDeclaringClass().getName().replace('.', '_');
        boolean hasField = false;
        CtField[] fields = ctClass.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            CtField field = fields[i];
            if (field.getName().equals(fieldName)) {
                hasField = true;
                break;
            }
        }
        if (!hasField) {
            CtField field = new CtField(ctClass.getClassPool().get("java.lang.Class"), fieldName, ctClass);
            field.setModifiers(Modifier.STATIC | Modifier.PRIVATE | Modifier.FINAL);
            ctClass.addField(
                    field,
                    "java.lang.Class#forName(\""
                    + ctConstructor.getDeclaringClass().getName().replace('/', '.') + "\")"
            );
        }
        return fieldName;
    }

    /**
     * Filters the classes to be transformed.
     *
     * @param definition
     * @param ctx        the context
     * @param cg         the class to filter
     * @return boolean true if the method should be filtered away
     */
    public static boolean classFilter(
            final SystemDefinition definition, final ExpressionContext ctx, final CtClass cg) {
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

    /**
     * Filters the caller methods.
     *
     * @param method the method to filter
     * @return boolean true if the method should be filtered away
     */
    public static boolean methodFilterCaller(final CtBehavior method) {
        if (Modifier.isNative(method.getModifiers()) || Modifier.isInterface(method.getModifiers())
            || method.getName().equals(TransformationUtil.GET_META_DATA_METHOD)
            || method.getName().equals(TransformationUtil.SET_META_DATA_METHOD)
            || method.getName().equals(TransformationUtil.CLASS_LOOKUP_METHOD)
            || method.getName().equals(TransformationUtil.GET_UUID_METHOD)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Filters the constructor.
     *
     * @param constructor the name of method to filter
     * @return boolean true if the method should be filtered away
     */
    public static boolean constructorFilter(final CtConstructor constructor) {
        if (constructor.getName().startsWith(TransformationUtil.ASPECTWERKZ_PREFIX)) {
            return true;
        } else {
            return false;
        }
    }
}
