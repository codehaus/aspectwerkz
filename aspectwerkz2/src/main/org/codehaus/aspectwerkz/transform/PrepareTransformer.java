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
 * Prepare class for further hotswap for execution pointcut TODO support for constructor pointcuts
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class PrepareTransformer /*extends MethodExecutionTransformer*/ implements Transformer {

    /**
     * Creates a new instance of the transformer.
     */
    public PrepareTransformer() {
        DefinitionLoader.getDefinitions();
    }

    /**
     * Add the class static field, the joinpoint manager, and add method stubs
     *
     * @param context the transformation context
     * @param klass   the class set.
     */
    public void transform(final Context context, final Klass klass) throws NotFoundException, CannotCompileException {

        // loop over all the definitions
        for (Iterator it = DefinitionLoader.getDefinitions().iterator(); it.hasNext();) {
            SystemDefinition definition = (SystemDefinition)it.next();

            final CtClass ctClass = klass.getCtClass();
            ClassMetaData classMetaData = JavassistMetaDataMaker.createClassMetaData(ctClass);

            // do we need to prepare the class
            if (classFilter(definition, classMetaData, ctClass)) {
                return;
            }

            final CtMethod[] methods = ctClass.getDeclaredMethods();

            // build the method lookup list
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

                CtMethod wrapperMethod = createEmptyWrapperMethod(ctClass, method, methodSequence);
                if (wrapperMethod != null) {
                    isClassAdvised = true;
                    wrapperMethods.add(wrapperMethod);
                }
            }

            if (isClassAdvised) {
                context.markAsPrepared();
                context.markAsAdvised();

                // add the wrapper methods
                for (Iterator it2 = wrapperMethods.iterator(); it2.hasNext();) {
                    ctClass.addMethod((CtMethod)it2.next());
                }
            }
        }
    }

    /**
     * Creates an empty wrapper method to allow HotSwap without schema change
     *
     * @param ctClass        the ClassGen
     * @param originalMethod the current method
     * @param methodSequence the method hash
     * @return the wrapper method
     */
    private CtMethod createEmptyWrapperMethod(
            final CtClass ctClass,
            final CtMethod originalMethod,
            final int methodSequence)
            throws NotFoundException, CannotCompileException {

        String wrapperMethodName = TransformationUtil.getPrefixedMethodName(
                originalMethod, methodSequence, ctClass.getName()
        );

        // check if methods does not already exists
        if (JavassistHelper.hasMethod(ctClass, wrapperMethodName)) {
            return null;
        }

        // determine the method access flags (should always be set to protected)
        int accessFlags = originalMethod.getModifiers();
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

        // add an empty body
        StringBuffer body = new StringBuffer();
        if (originalMethod.getReturnType() == CtClass.voidType) {
            // special handling for void return type leads to cleaner bytecode generation with Javassist
            body.append("{}");
        }
        else if (!originalMethod.getReturnType().isPrimitive()) {
            body.append("{ return null;}");
        }
        else {
            body.append("{ return ");
            body.append(JavassistHelper.getDefaultPrimitiveValue(originalMethod.getReturnType()));
            body.append("; }");
        }

        CtMethod method = null;
        if (Modifier.isStatic(originalMethod.getModifiers())) {
            method = JavassistHelper.makeStatic(
                    originalMethod.getReturnType(),
                    wrapperMethodName,
                    originalMethod.getParameterTypes(),
                    originalMethod.getExceptionTypes(),
                    body.toString(),
                    ctClass
            );
        }
        else {
            method = CtNewMethod.make(
                    originalMethod.getReturnType(),
                    wrapperMethodName,
                    originalMethod.getParameterTypes(),
                    originalMethod.getExceptionTypes(),
                    body.toString(),
                    ctClass
            );
            method.setModifiers(accessFlags);
        }

        // add a method level attribute so that we remember it is an empty method
        JavassistHelper.setAnnotatedEmpty(method);

        return method;
    }

    /**
     * Filters the classes to be transformed. Takes only "prepare" declarations into account
     *
     * @param definition    the definition
     * @param classMetaData the meta-data for the class
     * @param cg            the class to filter
     * @return boolean true if the method should be filtered away
     */
    private boolean classFilter(
            final SystemDefinition definition,
            final ClassMetaData classMetaData,
            final CtClass cg) throws NotFoundException {
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
        if (definition.inPreparePackage(className)) {
            return false;
        }
        return true;
    }

    /**
     * Filters the methods to be transformed. Does not check execution pointcuts
     *
     * @param definition     the definition
     * @param classMetaData  the class meta-data
     * @param methodMetaData the method meta-data
     * @param method         the method to filter
     * @return boolean
     */
    private boolean methodFilter(
            final SystemDefinition definition,
            final ClassMetaData classMetaData,
            final MethodMetaData methodMetaData,
            final CtMethod method) {
        if (Modifier.isAbstract(method.getModifiers()) || Modifier.isNative(method.getModifiers()) ||
            method.getName().equals("<init>") ||
            method.getName().equals("<clinit>") ||
            method.getName().startsWith(TransformationUtil.ORIGINAL_METHOD_PREFIX) ||
            method.getName().equals(TransformationUtil.GET_META_DATA_METHOD) ||
            method.getName().equals(TransformationUtil.SET_META_DATA_METHOD) ||
            method.getName().equals(TransformationUtil.CLASS_LOOKUP_METHOD) ||
            method.getName().equals(TransformationUtil.GET_UUID_METHOD)) {
            return true;
        }
        return false;
    }

}

