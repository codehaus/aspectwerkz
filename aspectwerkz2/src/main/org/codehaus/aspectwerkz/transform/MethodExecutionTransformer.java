/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import org.codehaus.aspectwerkz.definition.DefinitionLoader;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.JavassistMetaDataMaker;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;

/**
 * Advises method EXECUTION join points.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class MethodExecutionTransformer implements Transformer {

    /**
     * List with the definitions.
     */
    private List m_definitions;

    /**
     * Creates a new instance of the transformer.
     */
    public MethodExecutionTransformer() {
        m_definitions = DefinitionLoader.getDefinitions();
    }

    /**
     * Makes the member method transformations.
     *
     * @param context the transformation context
     * @param klass   the class set.
     */
    public void transform(final Context context, final Klass klass) throws Exception {
        for (Iterator it = m_definitions.iterator(); it.hasNext();) {

            SystemDefinition definition = (SystemDefinition)it.next();

            final CtClass ctClass = klass.getCtClass();
            ClassMetaData classMetaData = JavassistMetaDataMaker.createClassMetaData(ctClass);
            if (classFilter(definition, classMetaData, ctClass, false)) {
                return;
            }

            final CtMethod[] methods = ctClass.getDeclaredMethods();

            // build and sort the method lookup list
            final List methodLookupList = new ArrayList();
            for (int i = 0; i < methods.length; i++) {
                MethodMetaData methodMetaData = JavassistMetaDataMaker.createMethodMetaData(methods[i]);
                if (methodFilter(definition, classMetaData, methodMetaData, methods[i])) {
                    continue;
                }
                methodLookupList.add(methods[i]);
            }

            final Map methodSequences = new HashMap();
            final List wrapperMethods = new ArrayList();
            boolean isClassAdvised = false;
            for (Iterator i = methodLookupList.iterator(); i.hasNext();) {
                CtMethod method = (CtMethod)i.next();
                isClassAdvised = true;

                // take care of identification of overloaded methods by inserting a sequence number
                if (methodSequences.containsKey(method.getName())) {
                    int sequence = ((Integer)methodSequences.get(method.getName())).intValue();
                    methodSequences.remove(method.getName());
                    sequence++;
                    methodSequences.put(method.getName(), new Integer(sequence));
                }
                else {
                    methodSequences.put(method.getName(), new Integer(1));
                }

                final int methodSequence = ((Integer)methodSequences.get(method.getName())).intValue();
                final int methodHash = TransformationUtil.calculateHash(method);

                // there was no empty method already
                final String prefixedMethodName = TransformationUtil.getPrefixedMethodName(method, methodSequence, ctClass.getName());
                if (JavassistHelper.hasMethod(ctClass, prefixedMethodName)) {
                    CtMethod wrapperMethod = ctClass.getDeclaredMethod(prefixedMethodName);
                    if (wrapperMethod.getAttribute(TransformationUtil.EMPTY_WRAPPER_ATTRIBUTE) != null) {
                        CtMethod nonEmptyWrapper = createWrapperMethod(ctClass, method, methodHash);
                        wrapperMethod.setBody(nonEmptyWrapper, null);
                        // swap wrapper and original bodies
                        JavassistHelper.swapBodies(wrapperMethod, method);
                    } else {
                        // multi weaving
                        continue;
                    }
                } else {
                    // new execution pointcut

                    CtMethod wrapperMethod = createWrapperMethod(ctClass, method, methodHash);
                    wrapperMethods.add(wrapperMethod);

                    addPrefixToMethod(ctClass, method, methodSequence);
                }
            }

            if (isClassAdvised) {
                context.markAsAdvised();

                // add the wrapper methods
                for (Iterator it2 = wrapperMethods.iterator(); it2.hasNext();) {
                    ctClass.addMethod((CtMethod)it2.next());
                }
            }
        }
    }

    /**
     * Creates a wrapper method for the original method specified. This method has the same signature as the original
     * method and catches the invocation for further processing by the framework before redirecting to the original
     * method.
     * <p/>
     * Genereates code similar to this:
     * <pre>
     *        return (ReturnType)___AW_joinPointManager.proceedWithExecutionJoinPoint(
     *            joinPointHash, new Object[]{parameter}, this,
     *            JoinPointType.METHOD_EXECUTION, joinPointSignature
     *        );
     * </pre>
     *
     * @param ctClass        the ClassGen
     * @param originalMethod the current method
     * @param methodHash     the method hash
     * @return the wrapper method
     */
    private CtMethod createWrapperMethod(
            final CtClass ctClass,
            final CtMethod originalMethod,
            final int methodHash)
            throws NotFoundException, CannotCompileException {

        StringBuffer body = new StringBuffer();
        StringBuffer callBody = new StringBuffer();
        callBody.append(TransformationUtil.JOIN_POINT_MANAGER_FIELD);
        callBody.append('.');
        callBody.append(TransformationUtil.PROCEED_WITH_EXECUTION_JOIN_POINT_METHOD);
        callBody.append('(');
        callBody.append(methodHash);
        callBody.append(", ");
        callBody.append("$args");
        callBody.append(", ");
        if (Modifier.isStatic(originalMethod.getModifiers())) {
            callBody.append("(Object)null");
        }
        else {
            callBody.append("this");
        }
        callBody.append(',');
        callBody.append(TransformationUtil.JOIN_POINT_TYPE_METHOD_EXECUTION);
        callBody.append(",\"");
        callBody.append(originalMethod.getSignature());
        callBody.append("\");");

        if (originalMethod.getReturnType() == CtClass.voidType) {
            // special handling for void return type leads to cleaner bytecode generation with Javassist
            body.append("{").append(callBody.toString()).append("}");
        }
        else if (!originalMethod.getReturnType().isPrimitive()) {
            body.append("{ return ($r)");
            body.append(callBody.toString());
            body.append("}");
        }
        else {
            String localResult = TransformationUtil.ASPECTWERKZ_PREFIX + "res";
            body.append("{Object ").append(localResult).append(" = ");
            body.append(callBody.toString());
            body.append("if (").append(localResult).append(" != null)");
            body.append("return ($r) ").append(localResult).append("; else ");
            body.append("return ");
            body.append(JavassistHelper.getDefaultPrimitiveValue(originalMethod.getReturnType()));
            body.append("; }");
        }

        CtMethod method = null;
        if (Modifier.isStatic(originalMethod.getModifiers())) {
            method = JavassistHelper.makeStatic(
                    originalMethod.getReturnType(),
                    originalMethod.getName(),
                    originalMethod.getParameterTypes(),
                    originalMethod.getExceptionTypes(),
                    body.toString(),
                    ctClass
            );
        }
        else {
            method = CtNewMethod.make(
                    originalMethod.getReturnType(),
                    originalMethod.getName(),
                    originalMethod.getParameterTypes(),
                    originalMethod.getExceptionTypes(),
                    body.toString(),
                    ctClass
            );
            method.setModifiers(originalMethod.getModifiers());
        }

        return method;
    }

    /**
     * Adds a prefix to the original method. To make it callable only from within the framework itself.
     *
     * @param cg             class gen
     * @param ctMethod       the current method
     * @param methodSequence the methods sequence number
     */
    private void addPrefixToMethod(final CtClass cg, final CtMethod ctMethod, final int methodSequence) {
        // change the method access flags (should always be set to protected)
        int accessFlags = ctMethod.getModifiers();
        if ((accessFlags & Modifier.PROTECTED) == 0) {
            // set the protected flag
            accessFlags |= Modifier.PROTECTED;
        }
        if ((accessFlags & Modifier.PRIVATE) != 0) {
            // clear the private flag
            accessFlags &= ~Modifier.PRIVATE;
        }
        if ((accessFlags & Modifier.PUBLIC) != 0) {
            // clear the public flag
            accessFlags &= ~Modifier.PUBLIC;
        }

        String prefixedMethodName = TransformationUtil.getPrefixedMethodName(ctMethod, methodSequence, cg.getName());
        ctMethod.setName(prefixedMethodName);
        ctMethod.setModifiers(accessFlags);
    }

    /**
     * Filters the classes to be transformed.
     *
     * @param definition    the definition
     * @param classMetaData the meta-data for the class
     * @param cg            the class to filter
     * @return boolean true if the method should be filtered away
     */
    private boolean classFilter(
            final SystemDefinition definition,
            final ClassMetaData classMetaData,
            final CtClass cg,
            final boolean isActivatePhase) {
        if (cg.isInterface() ||
            TransformationUtil.hasSuperClass(classMetaData, "org.codehaus.aspectwerkz.aspect.Aspect")) {
            return true;
        }
        String className = cg.getName();
        if (definition.inExcludePackage(className)) {
            return true;
        }
        if (!definition.inIncludePackage(className)) {
            return true;
        }
//        if (definition.inPreparePackage(className) && !isActivatePhase) {
//            return true; //TODO REMOVE
//        }
        if (definition.hasExecutionPointcut(classMetaData)) {
            return false;
        }
        return true;
    }

    /**
     * Filters the methods to be transformed.
     *
     * @param definition    the definition
     * @param classMetaData the class meta-data
     * @param method        the method to filter
     * @return boolean
     */
    private boolean methodFilter(
            final SystemDefinition definition,
            final ClassMetaData classMetaData,
            final MethodMetaData methodMetaData,
            final CtMethod method) {
        if (Modifier.isAbstract(method.getModifiers()) ||
            Modifier.isNative(method.getModifiers()) ||
            method.getName().equals("<init>") ||
            method.getName().equals("<clinit>") ||
            method.getName().startsWith(TransformationUtil.ORIGINAL_METHOD_PREFIX) ||
            method.getName().equals(TransformationUtil.GET_META_DATA_METHOD) ||
            method.getName().equals(TransformationUtil.SET_META_DATA_METHOD) ||
            method.getName().equals(TransformationUtil.CLASS_LOOKUP_METHOD) ||
            method.getName().equals(TransformationUtil.GET_UUID_METHOD)) {
            return true;
        }
        else if (definition.hasExecutionPointcut(classMetaData, methodMetaData)) {
            return false;
        }
        else {
            return true;
        }
    }
}
