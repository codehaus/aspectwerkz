/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.delegation;

import gnu.trove.TObjectIntHashMap;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.expression.PointcutType;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.reflect.impl.javassist.JavassistClassInfo;
import org.codehaus.aspectwerkz.reflect.impl.javassist.JavassistMethodInfo;
import org.codehaus.aspectwerkz.transform.Context;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import org.codehaus.aspectwerkz.transform.Transformer;
import org.codehaus.aspectwerkz.transform.TransformationConstants;
import org.codehaus.aspectwerkz.transform.TransformationConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;

/**
 * Advises method EXECUTION join points.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class MethodExecutionTransformer implements Transformer {

    /**
     * Makes the member method transformations.
     * 
     * @param context the transformation context
     * @param klass the class set.
     */
    public void transform(final Context context, final Klass klass) throws Exception {
        List definitions = context.getDefinitions();

        //m_joinPointIndex =
        // TransformationUtil.getJoinPointIndex(klass.getCtClass()); // TODO not
        // thread safe

        final CtClass ctClass = klass.getCtClass();
        ClassInfo classInfo = JavassistClassInfo.getClassInfo(ctClass, context.getLoader());
        
        if (classFilter(definitions, new ExpressionContext(PointcutType.EXECUTION, classInfo, classInfo), ctClass)) {
            return;
        }
        final CtMethod[] methods = ctClass.getDeclaredMethods();

        // Compute the method sequence number no matter the method is advised to
        // support multi weaving.
        // Javassist.getDeclaredMethods() does not always return methods in the
        // same order so we have
        // to sort the method list before computation.
        // @TODO: filter init/clinit/prefixed methods
        final List sortedMethods = Arrays.asList(methods);
        Collections.sort(sortedMethods, JavassistMethodComparator.getInstance());
        final TObjectIntHashMap methodSequences = new TObjectIntHashMap();
        final List sortedMethodTuples = new ArrayList(sortedMethods.size());
        for (Iterator methodsIt = sortedMethods.iterator(); methodsIt.hasNext();) {
            CtMethod method = (CtMethod) methodsIt.next();
            MethodInfo methodInfo = JavassistMethodInfo.getMethodInfo(method, context.getLoader());
            int sequence = 1;
            if (methodSequences.containsKey(method.getName())) {
                sequence = methodSequences.get(method.getName());
                methodSequences.remove(method.getName());
                sequence++;
            }
            methodSequences.put(method.getName(), sequence);
            ExpressionContext ctx = new ExpressionContext(PointcutType.EXECUTION, methodInfo, methodInfo);
            MethodSequenceTuple tuple = new MethodSequenceTuple(method, sequence);
            int status = methodFilter(definitions, ctx, method);
            tuple.setStatus(status);

            // @TODO filter out "skip" status
            sortedMethodTuples.add(tuple);
        }
        final List wrapperMethods = new ArrayList();
        boolean isClassAdvised = false;
        for (Iterator i = sortedMethodTuples.iterator(); i.hasNext();) {
            MethodSequenceTuple tuple = (MethodSequenceTuple) i.next();
            if (tuple.getStatus() != STATUS_HAS_POINTCUT) {
                continue;
            }
            CtMethod method = tuple.getMethod();
            final int methodSequence = tuple.getSequence();
            final int methodHash = JavassistHelper.calculateHash(method);

            // there was no empty method already
            final String prefixedMethodName = TransformationUtil.getPrefixedOriginalMethodName(
                method.getName(),
                methodSequence,
                ctClass.getName().replace('/', '.'));
            if (JavassistHelper.hasMethod(ctClass, prefixedMethodName)) {
                CtMethod wrapperMethod = ctClass.getDeclaredMethod(prefixedMethodName);
                if (JavassistHelper.isAnnotatedEmpty(wrapperMethod)) {
                    // create the non empty wrapper to access its body
                    CtMethod nonEmptyWrapper = createWrapperMethod(ctClass, method, methodHash, klass);
                    wrapperMethod.setBody(method, null);
                    method.setBody(nonEmptyWrapper, null);
                    JavassistHelper.setAnnotatedNotEmpty(wrapperMethod);
                    isClassAdvised = true;
                } else {
                    // multi weaving
                    continue;
                }
            } else {
                // new execution pointcut
                CtMethod wrapperMethod = createWrapperMethod(ctClass, method, methodHash, klass);
                wrapperMethods.add(wrapperMethod);
                addPrefixToMethod(ctClass, method, methodSequence);
                isClassAdvised = true;
            }
        }
        if (isClassAdvised) {
            context.markAsAdvised();

            // add the wrapper methods
            for (Iterator it2 = wrapperMethods.iterator(); it2.hasNext();) {
                ctClass.addMethod((CtMethod) it2.next());
            }
        }

        // handles pointcut unweaving
        // looping on the original methods is enough since we will look for
        // method with no pc
        // thus that have not been changed in the previous transformation steps
        for (Iterator i = sortedMethodTuples.iterator(); i.hasNext();) {
            MethodSequenceTuple tuple = (MethodSequenceTuple) i.next();

            //System.out.println(" tuple " + tuple.getAdvice().getName() + " :
            // " + tuple.getStatus());
            if (tuple.getStatus() != STATUS_HAS_NO_POINTCUT) {
                continue;
            }
            CtMethod method = tuple.getMethod();

            //System.out.println("FOUND NO PC = " + method.getName());
            final String prefixedMethodName = TransformationUtil.getPrefixedOriginalMethodName(method.getName(), tuple
                    .getSequence(), ctClass.getName().replace('/', '.'));

            // do we have a wrapper method, which is NOT marked empty
            if (JavassistHelper.hasMethod(ctClass, prefixedMethodName)) {
                CtMethod wrapperMethod = ctClass.getDeclaredMethod(prefixedMethodName);
                if (JavassistHelper.isAnnotatedNotEmpty(wrapperMethod)) {
                    //System.out.println("FOUND A real Wrapper but NO PC = " +
                    // method.getName());
                    CtMethod emptyWrapperMethod = JavassistHelper.createEmptyWrapperMethod(ctClass, method, tuple
                            .getSequence());
                    method.setBody(wrapperMethod, null);
                    wrapperMethod.setBody(emptyWrapperMethod, null);
                    JavassistHelper.setAnnotatedEmpty(wrapperMethod);
                    context.markAsAdvised();
                }
            }
        }

        //}//end of def loop
        //TransformationUtil.setJoinPointIndex(klass.getCtClass(),
        // m_joinPointIndex);
        klass.flushJoinPointIndex();
    }

    /**
     * Creates a wrapper method for the original method specified. This method has the same signature as the original
     * method and catches the invocation for further processing by the framework before redirecting to the original
     * method.
     * 
     * @param ctClass the ClassGen
     * @param originalMethod the current method
     * @param methodHash the method hash
     * @return the wrapper method
     */
    private CtMethod createWrapperMethod(
        final CtClass ctClass,
        final CtMethod originalMethod,
        final int methodHash,
        final Klass klass) throws NotFoundException, CannotCompileException {
        StringBuffer body = new StringBuffer();
        StringBuffer callBody = new StringBuffer();
        body.append('{');
        callBody.append(TransformationConstants.JOIN_POINT_MANAGER_FIELD);
        callBody.append('.');
        callBody.append(TransformationConstants.PROCEED_WITH_EXECUTION_JOIN_POINT_METHOD);
        callBody.append('(');
        callBody.append(methodHash);
        callBody.append(", ");
        callBody.append(klass.getJoinPointIndex());
        callBody.append(", args, ");
        if (Modifier.isStatic(originalMethod.getModifiers())) {
            callBody.append("nullObject");
            body.append("Object nullObject = null;");
        } else {
            callBody.append("this");
        }
        callBody.append(',');
        callBody.append(TransformationConstants.JOIN_POINT_TYPE_METHOD_EXECUTION);
        callBody.append(");");
        if (originalMethod.getParameterTypes().length > 0) {
            body.append("Object[] args = $args; ");
        } else {
            body.append("Object[] args = null; ");
        }
        if (originalMethod.getReturnType() == CtClass.voidType) {
            // special handling for void return type leads to cleaner bytecode
            // generation with Javassist
            body.append(callBody.toString()).append("}");
        } else if (!originalMethod.getReturnType().isPrimitive()) {
            body.append("return ($r)");
            body.append(callBody.toString());
            body.append("}");
        } else {
            String localResult = TransformationConstants.ASPECTWERKZ_PREFIX + "res";
            body.append("Object ").append(localResult).append(" = ");
            body.append(callBody.toString());
            body.append("if (").append(localResult).append(" != null)");
            body.append("return ($r) ").append(localResult).append("; else ");
            body.append("return ");
            body.append(JavassistHelper.getDefaultPrimitiveValue(originalMethod.getReturnType()));
            body.append("; }");
        }
        CtMethod method;
        if (Modifier.isStatic(originalMethod.getModifiers())) {
            method = JavassistHelper.makeStatic(
                originalMethod.getReturnType(),
                originalMethod.getName(),
                originalMethod.getParameterTypes(),
                originalMethod.getExceptionTypes(),
                body.toString(),
                ctClass);
        } else {
            method = CtNewMethod.make(originalMethod.getReturnType(), originalMethod.getName(), originalMethod
                    .getParameterTypes(), originalMethod.getExceptionTypes(), body.toString(), ctClass);
            method.setModifiers(originalMethod.getModifiers());
        }
        JavassistHelper.copyCustomAttributes(method, originalMethod);
        klass.incrementJoinPointIndex();
        JavassistHelper.setAnnotatedNotEmpty(method);
        return method;
    }

    /**
     * Adds a prefix to the original method. To make it callable only from within the framework itself.
     * 
     * @param cg class gen
     * @param ctMethod the current method
     * @param methodSequence the methods sequence number
     */
    private void addPrefixToMethod(final CtClass cg, final CtMethod ctMethod, final int methodSequence) {
        // change the method access flags (should always be set to protected)
        int accessFlags = ctMethod.getModifiers();
        String prefixedMethodName = TransformationUtil.getPrefixedOriginalMethodName(ctMethod.getName(), methodSequence, cg
                .getName());
        ctMethod.setName(prefixedMethodName);
        ctMethod.setModifiers(accessFlags);
    }

    /**
     * Filters the classes to be transformed.
     * 
     * @param definitions the definitions
     * @param ctx the context
     * @param cg the class to filter
     * @return boolean true if the method should be filtered away
     */
    private boolean classFilter(final List definitions, final ExpressionContext ctx, final CtClass cg) {
        if (cg.isInterface()) {
            return true;
        }
        for (Iterator defs = definitions.iterator(); defs.hasNext();) {
            if (classFilter((SystemDefinition) defs.next(), ctx, cg)) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Filters the classes to be transformed. <p/>TODO: when a class had execution pointcut that were removed it must be
     * unweaved, thus not filtered out How to handle that ? cache lookup ? or custom class level attribute ?
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
        if (definition.inPreparePackage(className)) {
            return false; //no early filtering for prepared Class to allow RuW
        }
        return true;
    }

    /**
     * Filters the methods to be transformed.
     * 
     * @param definitions
     * @param ctx
     * @param method
     * @return
     */
    public static int methodFilter(final List definitions, final ExpressionContext ctx, final CtMethod method) {
        if (Modifier.isAbstract(method.getModifiers())
            || Modifier.isNative(method.getModifiers())
            || method.getName().equals("<init>")
            || method.getName().equals("<clinit>")
            || method.getName().startsWith(TransformationConstants.ORIGINAL_METHOD_PREFIX)
            || method.getName().equals(TransformationConstants.CLASS_LOOKUP_METHOD)) {
            return STATUS_SKIP;
        }
        for (Iterator defs = definitions.iterator(); defs.hasNext();) {
            if (((SystemDefinition) defs.next()).hasPointcut(ctx)) {
                return STATUS_HAS_POINTCUT;
            } else {
                continue;
            }
        }
        return STATUS_HAS_NO_POINTCUT;
    }
}

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */

class MethodSequenceTuple {
    private CtMethod m_method;

    private int m_sequence;

    private int m_status = MethodExecutionTransformer.STATUS_SKIP;

    public MethodSequenceTuple(CtMethod method, int sequence) {
        m_method = method;
        m_sequence = sequence;
    }

    public CtMethod getMethod() {
        return m_method;
    }

    public int getSequence() {
        return m_sequence;
    }

    public void setStatus(int status) {
        m_status = status;
    }

    public int getStatus() {
        return m_status;
    }

    public String toString() {
        return m_method.getName() + " : " + m_status;
    }
}