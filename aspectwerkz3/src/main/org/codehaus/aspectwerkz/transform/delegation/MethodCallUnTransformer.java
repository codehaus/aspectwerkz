/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
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
import org.codehaus.aspectwerkz.reflect.impl.javassist.JavassistClassInfo;
import org.codehaus.aspectwerkz.transform.Context;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import org.codehaus.aspectwerkz.transform.Transformer;

import java.util.Iterator;
import java.util.List;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.CodeIterator;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

/**
 * Advises method CALL join points.
 * 
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public class MethodCallUnTransformer implements Transformer {
    /**
     * Transforms the call side pointcuts.
     * 
     * @param context the transformation context
     * @param klass the class set.
     */
    public void transform(final Context context, final Klass klass) throws NotFoundException, CannotCompileException {
        List definitions = context.getDefinitions();
        for (Iterator it = definitions.iterator(); it.hasNext();) {
            final SystemDefinition definition = (SystemDefinition) it.next();
            final CtClass ctClass = klass.getCtClass();
            ClassInfo classInfo = JavassistClassInfo.getClassInfo(ctClass, context.getLoader());
            if (classFilter(definition, new ExpressionContext(PointcutType.CALL, classInfo, classInfo), ctClass)) {
                return;
            }
            ctClass.instrument(new ExprEditor() {
                public void edit(MethodCall methodCall) throws CannotCompileException {
                    try {
                        CtBehavior where = null;
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
                        if (!(calleeMethod.getName().equals("proceedWithCallJoinPoint") && calleeClassName
                                .equals("org.codehaus.aspectwerkz.joinpoint.management.JoinPointManager"))) {
                            return;
                        }
                        System.out.println("found smtg to REMOVE");
                        System.out.println("calleeMethod = " + calleeMethod.getName());
                        System.out.println("calleeClassName = " + calleeClassName);
                        System.out.println("methodCall = " + methodCall.indexOfBytecode());
                        methodCall.replace("{java.lang.System.out.println($args[0]); $_=null;}");
                        try {
                            CodeIterator it = where.getMethodInfo().getCodeAttribute().iterator();
                            it.move(methodCall.indexOfBytecode() - 5);
                            System.out.println("it.get() = " + it.get());
                            it.next();
                            System.out.println("it.get() = " + it.get());
                            it.next();
                            System.out.println("it.get() = " + it.get());
                            it.next();
                            System.out.println("it.get() = " + it.get());
                            it.next();
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                        return;

                        //
                        //                                // filter callee methods
                        //                                if (methodFilterCallee(calleeMethod)) {
                        //                                    return;
                        //                                }
                        //
                        //                                // create the class meta-data
                        //                                ClassMetaData calleeSideClassMetaData;
                        //                                try {
                        //                                    calleeSideClassMetaData =
                        //                                    JavassistMetaDataMaker.createClassMetaData(
                        //                                            context.getClassPool().get(calleeClassName)
                        //                                    );
                        //                                }
                        //                                catch (NotFoundException e) {
                        //                                    throw new WrappedRuntimeException(e);
                        //                                }
                        //
                        //                                // create the method meta-data
                        //                                MethodMetaData calleeSideMethodMetaData =
                        // JavassistMetaDataMaker.createMethodMetaData(
                        //                                        methodCall.getMethod()
                        //                                );
                        //
                        //                                // is this a caller side method pointcut?
                        //                                if (definition.isPickedOutByCallPointcut(
                        //                                        calleeSideClassMetaData, calleeSideMethodMetaData
                        //                                )) {
                        //
                        //// // TODO: should this caller data be passed to the
                        // join point? It is possible.
                        //// String callerMethodName =
                        // callerBehaviour.getName();
                        //// String callerMethodSignature =
                        // callerBehaviour.getSignature();
                        //// CtClass[] callerMethodParameterTypes =
                        // callerBehaviour.getParameterTypes();
                        //// int callerMethodModifiers =
                        // callerBehaviour.getModifiers();
                        //
                        //                                    // check the callee class is not the same as target
                        // class, if that is the case
                        //                                    // then we have have class loaded and set in the
                        // ___AW_clazz already
                        //                                    String declaringClassMethodName =
                        // TransformationUtil.STATIC_CLASS_FIELD;
                        //                                    CtMethod method = methodCall.getMethod();
                        //                                    CtClass declaringClass = method.getDeclaringClass();
                        //                                    if (!declaringClass.getName().replace('/',
                        // '.').equals(where.getDeclaringClass().getName().replace('/',
                        // '.'))) {
                        //                                        declaringClassMethodName =
                        // addCalleeMethodDeclaringClassField(ctClass, method);
                        //                                    }
                        //
                        //                                    // call the wrapper method instead of the callee
                        // method
                        //                                    StringBuffer body = new StringBuffer();
                        //                                    StringBuffer callBody = new StringBuffer();
                        //
                        //                                    callBody.append(TransformationUtil.JOIN_POINT_MANAGER_FIELD);
                        //                                    callBody.append('.');
                        //                                    callBody.append(TransformationUtil.PROCEED_WITH_CALL_JOIN_POINT_METHOD);
                        //                                    callBody.append('(');
                        //                                    callBody.append(TransformationUtil.calculateHash(method));
                        //                                    callBody.append(',');
                        //                                    callBody.append(1);
                        //                                    callBody.append(", args");
                        //                                    callBody.append(", $0, declaringClassMethodName, ");
                        //                                    callBody.append(TransformationUtil.JOIN_POINT_TYPE_METHOD_CALL);
                        //                                    callBody.append(");");
                        //
                        //                                    body.append('{');
                        //                                    if (method.getParameterTypes().length > 0) {
                        //                                        body.append("Object[] args = $args; ");
                        //                                    }
                        //                                    else {
                        //                                        body.append("Object[] args = null; ");
                        //                                    }
                        //                                    body.append("Class declaringClassMethodName = ");
                        //                                    body.append(declaringClassMethodName);
                        //                                    body.append("; ");
                        //
                        //                                    if (methodCall.getMethod().getReturnType() ==
                        // CtClass.voidType) {
                        //                                        body.append("$_ =
                        // ").append(callBody.toString()).append("}");
                        //                                    }
                        //                                    else if
                        // (!methodCall.getMethod().getReturnType().isPrimitive())
                        // {
                        //                                        body.append("$_ = ($r)");
                        //                                        body.append(callBody.toString());
                        //                                        body.append("}");
                        //                                    }
                        //                                    else {
                        //                                        String localResult =
                        // TransformationUtil.ASPECTWERKZ_PREFIX + "res";
                        //                                        body.append("Object ").append(localResult).append(" =
                        // ");
                        //                                        body.append(callBody.toString());
                        //                                        body.append("if (").append(localResult).append(" !=
                        // null)");
                        //                                        body.append("$_ = ($r)
                        // ").append(localResult).append("; else ");
                        //                                        body.append("$_ = ");
                        //                                        body.append(
                        //                                                JavassistHelper.getDefaultPrimitiveValue(
                        //                                                        methodCall.getMethod().getReturnType()
                        //                                                )
                        //                                        );
                        //                                        body.append("; }");
                        //                                    }
                        //
                        //                                    methodCall.replace(body.toString());
                        //                                    context.markAsAdvised();
                        //
                        //                                    //1++;
                        //                                }
                    } catch (NotFoundException nfe) {
                        nfe.printStackTrace();

                        // TODO: should we swallow this exception?
                    }
                }
            });
        }

        //TransformationUtil.setJoinPointIndex(klass.getCtClass(), 1);
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
        String fieldName = TransformationUtil.STATIC_CLASS_FIELD
            + TransformationUtil.DELIMITER
            + "method"
            + TransformationUtil.DELIMITER
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
     * Filters the callee methods.
     * 
     * @param method the name of method to filter
     * @return boolean true if the method should be filtered away
     * @TODO: create metadata instance and check with the system
     */
    public static boolean methodFilterCallee(final CtMethod method) {
        if (method.getName().equals("<init>")
            || method.getName().equals("<clinit>")
            || method.getName().startsWith(TransformationUtil.ORIGINAL_METHOD_PREFIX)
            || method.getName().equals(TransformationUtil.GET_META_DATA_METHOD)
            || method.getName().equals(TransformationUtil.SET_META_DATA_METHOD)
            || method.getName().equals(TransformationUtil.CLASS_LOOKUP_METHOD)
            || method.getName().equals(TransformationUtil.GET_UUID_METHOD)) {
            return true;
        } else {
            return false;
        }
    }
}