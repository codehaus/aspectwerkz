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
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.reflect.impl.javassist.JavassistClassInfo;
import org.codehaus.aspectwerkz.reflect.impl.javassist.JavassistClassInfoRepository;
import org.codehaus.aspectwerkz.reflect.impl.javassist.JavassistConstructorInfo;
import org.codehaus.aspectwerkz.reflect.impl.javassist.JavassistMethodInfo;
import org.codehaus.aspectwerkz.transform.Context;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import org.codehaus.aspectwerkz.transform.Transformer;
import org.codehaus.aspectwerkz.transform.TransformationConstants;
import org.codehaus.aspectwerkz.transform.TransformationConstants;

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
import javassist.expr.MethodCall;

/**
 * Advises method CALL join points.
 * 
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class MethodCallTransformer implements Transformer {
    /**
     * The join point index.
     */

    //AXprivate int m_joinPointIndex;
    /**
     * Transforms the call side pointcuts.
     * 
     * @param context the transformation context
     * @param klass the class set.
     */
    public void transform(final Context context, final Klass klass) throws NotFoundException, CannotCompileException {
        List definitions = context.getDefinitions();

        //AXm_joinPointIndex =
        // TransformationUtil.getJoinPointIndex(klass.getCtClass()); //TODO
        // thread safe reentrant
        for (Iterator it = definitions.iterator(); it.hasNext();) {
            final SystemDefinition definition = (SystemDefinition) it.next();
            final CtClass ctClass = klass.getCtClass();
            ClassInfo classInfo = JavassistClassInfo.getClassInfo(ctClass, context.getLoader());
            if (classFilter(definition, new ExpressionContext(PointcutType.CALL, null, classInfo), ctClass)) {
                continue;
            }
            ctClass.instrument(new ExprEditor() {
                public void edit(MethodCall methodCall) throws CannotCompileException {
                    // AW-228, super.callSomething(..) is not a valid join point
                    if (methodCall.isSuper()) {
                        return;
                    }
                    try {
                        CtBehavior where;
                        try {
                            where = methodCall.where();
                        } catch (RuntimeException e) {
                            // <clinit> access leads to a bug in Javassist
                            where = ctClass.getClassInitializer();
                        }

                        // filter caller methods
                        if (methodFilterCaller(where)) {
                            return;
                        }

                        // get the callee method name, signature and class name
                        CtMethod calleeMethod = methodCall.getMethod();
                        String calleeClassName = methodCall.getClassName();

                        // filter callee classes
                        if (!definition.inIncludePackage(calleeClassName)) {
                            return;
                        }

                        // filter callee methods
                        if (methodFilterCallee(calleeMethod)) {
                            return;
                        }
                        JavassistClassInfoRepository classInfoRepository = JavassistClassInfoRepository
                                .getRepository(context.getLoader());

                        // TODO: callee side class info is NOT used, make use of it
                        ClassInfo calleeSideClassInfo = classInfoRepository.getClassInfo(calleeClassName);
                        if (calleeSideClassInfo == null) {
                            calleeSideClassInfo = JavassistClassInfo.getClassInfo(ctClass.getClassPool().get(
                                calleeClassName), context.getLoader());
                        }

                        // create the caller method info, used for 'within' and
                        // 'withincode'
                        MemberInfo withinMemberInfo = null;
                        if (where instanceof CtMethod) {
                            withinMemberInfo = JavassistMethodInfo.getMethodInfo((CtMethod) where, context.getLoader());
                        } else if (where instanceof CtConstructor) {
                            withinMemberInfo = JavassistConstructorInfo.getConstructorInfo(
                                (CtConstructor) where,
                                context.getLoader());
                        }

                        // create the callee method info
                        MethodInfo calleeSideMethodInfo = JavassistMethodInfo.getMethodInfo(
                            methodCall.getMethod(),
                            context.getLoader());
                        ExpressionContext ctx = new ExpressionContext(
                            PointcutType.CALL,
                            calleeSideMethodInfo,
                            withinMemberInfo);
                        if (definition.hasPointcut(ctx) || definition.hasCflowPointcut(ctx)) {
                            // check the callee class is not the same as target
                            // class, if that is the case
                            // then we have have class loaded and set in the
                            // ___AW_clazz already
                            String declaringClassMethodName = TransformationConstants.STATIC_CLASS_FIELD;
                            CtMethod method = methodCall.getMethod();
                            CtClass declaringClass = method.getDeclaringClass();
                            if (!declaringClass.getName().replace('/', '.').equals(
                                where.getDeclaringClass().getName().replace('/', '.'))) {
                                declaringClassMethodName = addCalleeMethodDeclaringClassField(ctClass, method);
                            }

                            // call the wrapper method instead of the callee
                            // method
                            StringBuffer body = new StringBuffer();
                            StringBuffer callBody = new StringBuffer();
                            callBody.append(TransformationConstants.JOIN_POINT_MANAGER_FIELD);
                            callBody.append('.');
                            callBody.append(TransformationConstants.PROCEED_WITH_CALL_JOIN_POINT_METHOD);
                            callBody.append('(');
                            callBody.append(JavassistHelper.calculateHash(method));
                            callBody.append(',');
                            callBody.append(klass.getJoinPointIndex());
                            callBody.append(", args, ");
                            callBody.append(TransformationConstants.STATIC_CLASS_FIELD);
                            if (Modifier.isStatic(where.getModifiers())) {
                                callBody.append(", nullObject, ");
                            } else {
                                callBody.append(", this, ");
                            }
                            callBody.append("declaringClass, $0, \"");
                            callBody.append(where.getName());
                            callBody.append("\",\"");
                            callBody.append(where.getSignature());
                            callBody.append("\",");
                            callBody.append(TransformationConstants.JOIN_POINT_TYPE_METHOD_CALL);
                            callBody.append(");");
                            body.append('{');
                            if (method.getParameterTypes().length > 0) {
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
                            if (methodCall.getMethod().getReturnType() == CtClass.voidType) {
                                body.append("$_ = ").append(callBody.toString()).append("}");
                            } else if (!methodCall.getMethod().getReturnType().isPrimitive()) {
                                body.append("$_ = ($r)");
                                body.append(callBody.toString());
                                body.append("}");
                            } else {
                                String localResult = TransformationConstants.ASPECTWERKZ_PREFIX + "res";
                                body.append("Object ").append(localResult).append(" = ");
                                body.append(callBody.toString());
                                body.append("if (").append(localResult).append(" != null)");
                                body.append("$_ = ($r) ").append(localResult).append("; else ");
                                body.append("$_ = ");
                                body.append(JavassistHelper.getDefaultPrimitiveValue(methodCall.getMethod()
                                        .getReturnType()));
                                body.append("; }");
                            }
                            methodCall.replace(body.toString());
                            context.markAsAdvised();
                            klass.incrementJoinPointIndex();
                        }
                    } catch (NotFoundException nfe) {
                        nfe.printStackTrace();

                        // TODO: should we swallow this exception?
                    }
                }
            });
        }

        //AxTransformationUtil.setJoinPointIndex(klass.getCtClass(),
        // m_joinPointIndex);
        klass.flushJoinPointIndex();
    }

    /**
     * Creates a new static class field, for the declaring class of the callee method.
     * 
     * @param ctClass the class
     * @param ctMethod the method
     * @return the name of the field
     */
    private String addCalleeMethodDeclaringClassField(final CtClass ctClass, final CtMethod ctMethod) throws NotFoundException,
            CannotCompileException {
        String fieldName = TransformationConstants.STATIC_CLASS_FIELD
            + TransformationConstants.DELIMITER
            + "method"
            + TransformationConstants.DELIMITER
            + ctMethod.getDeclaringClass().getName().replace('.', '_');
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
            ctClass.addField(field, "java.lang.Class#forName(\""
                + ctMethod.getDeclaringClass().getName().replace('/', '.')
                + "\")");
        }
        return fieldName;
    }

    /**
     * Filters the classes to be transformed.
     * 
     * @param definition the definition
     * @param ctx the context
     * @param cg the class to filter
     * @return boolean true if the method should be filtered away
     */
    public static boolean classFilter(final SystemDefinition definition, final ExpressionContext ctx, final CtClass cg) {
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
        if (Modifier.isNative(method.getModifiers())
            || Modifier.isInterface(method.getModifiers())
            || method.getName().equals(TransformationConstants.CLASS_LOOKUP_METHOD)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Filters the callee methods.
     * 
     * @param method the name of method to filter
     * @return boolean true if the method should be filtered away
     * @TODO: create metadata instance and check with the system
     */
    public static boolean methodFilterCallee(final CtMethod method) {
        if (method.getName().equals("<init>")
            || method.getName().equals("<clinit>")
            || method.getName().startsWith(TransformationConstants.ORIGINAL_METHOD_PREFIX)
            || method.getName().equals(TransformationConstants.CLASS_LOOKUP_METHOD)) {
            return true;
        } else {
            return false;
        }
    }
}