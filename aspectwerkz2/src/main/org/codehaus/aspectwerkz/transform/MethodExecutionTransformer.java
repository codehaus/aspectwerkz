/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import java.util.Arrays;

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
import org.codehaus.aspectwerkz.MethodComparator;
import org.codehaus.aspectwerkz.definition.SystemDefinitionContainer;
import gnu.trove.TObjectIntHashMap;

/**
 * Advises method EXECUTION join points.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class MethodExecutionTransformer implements Transformer {

    //TODO refactor in type pattern
    public final static int STATUS_SKIP = 1;
    public final static int STATUS_HASNOPOINTCUT = 2;
    public final static int STATUS_HASPOINTCUT = 3;

    /**
     * List with the definitions.
     */
    private List m_definitions;

    /**
     * The join point index.
     */
    private int m_joinPointIndex;

    /**
     * Creates a new instance of the transformer.
     */
    public MethodExecutionTransformer() {
        //m_definitions = DefinitionLoader.getDefinitions();
    }

    /**
     * Makes the member method transformations.
     *
     * @param context the transformation context
     * @param klass   the class set.
     */
    public void transform(final Context context, final Klass klass) throws Exception {
        m_definitions = SystemDefinitionContainer.getDefinitionsContext();

        m_joinPointIndex = TransformationUtil.getJoinPointIndex(klass.getCtClass()); // TODO not thread safe
        for (Iterator it = m_definitions.iterator(); it.hasNext();) {
            SystemDefinition definition = (SystemDefinition)it.next();

            final CtClass ctClass = klass.getCtClass();
            ClassMetaData classMetaData = JavassistMetaDataMaker.createClassMetaData(ctClass);
            if (classFilter(definition, classMetaData, ctClass)) {
                continue;//AVAOPC
            }

            final CtMethod[] methods = ctClass.getDeclaredMethods();

            // Compute the method sequence number no matter the method is advised to support multi weaving.
            // Javassist.getDeclaredMethods() does not always return methods in the same order so we have
            // to sort the method list before computation.
            // TODO: filter init/clinit/prefixed methods
            final List sortedMethods = Arrays.asList(methods);
            Collections.sort(sortedMethods, JavassistMethodComparator.getInstance());

            final TObjectIntHashMap methodSequences = new TObjectIntHashMap();
            final List sorteMethodTuples = new ArrayList(sortedMethods.size());
            for (Iterator methodsIt = sortedMethods.iterator(); methodsIt.hasNext();) {
                CtMethod method = (CtMethod)methodsIt.next();
                MethodMetaData methodMetaData = JavassistMetaDataMaker.createMethodMetaData(method);

                int sequence = 1;
                if (methodSequences.containsKey(method.getName())) {
                    sequence = methodSequences.get(method.getName());
                    methodSequences.remove(method.getName());
                    sequence++;
                }
                methodSequences.put(method.getName(), sequence);

                MethodSequenceTuple tuple = new MethodSequenceTuple(method, sequence);
                tuple.setStatus(methodFilter(definition, classMetaData, methodMetaData, method));
                // todo filter out "skip" status
                sorteMethodTuples.add(tuple);
            }

            final List wrapperMethods = new ArrayList();
            boolean isClassAdvised = false;
            for (Iterator i = sorteMethodTuples.iterator(); i.hasNext();) {
                MethodSequenceTuple tuple = (MethodSequenceTuple)i.next();
                if (tuple.getStatus() != STATUS_HASPOINTCUT) {
                    continue;
                }
                CtMethod method = tuple.getMethod();

                final int methodSequence = tuple.getSequence();
                final int methodHash = TransformationUtil.calculateHash(method);

                // there was no empty method already
                final String prefixedMethodName = TransformationUtil.getPrefixedMethodName(
                        method.getName(), methodSequence, ctClass.getName().replace('/', '.')
                );
                if (JavassistHelper.hasMethod(ctClass, prefixedMethodName)) {
                    CtMethod wrapperMethod = ctClass.getDeclaredMethod(prefixedMethodName);
                    if (JavassistHelper.isAnnotatedEmpty(wrapperMethod)) {
                        // create the non empty wrapper to access its body
                        CtMethod nonEmptyWrapper = createWrapperMethod(ctClass, method, methodHash);
                        wrapperMethod.setBody(method, null);
                        method.setBody(nonEmptyWrapper, null);
                        JavassistHelper.setAnnotatedNotEmpty(wrapperMethod);
                        isClassAdvised = true;
                    }
                    else {
                        // multi weaving
                        continue;
                    }
                }
                else {
                    // new execution pointcut
                    CtMethod wrapperMethod = createWrapperMethod(ctClass, method, methodHash);
                    wrapperMethods.add(wrapperMethod);
                    addPrefixToMethod(ctClass, method, methodSequence);
                    isClassAdvised = true;
                }
            }

            if (isClassAdvised) {
                context.markAsAdvised();

                // add the wrapper methods
                for (Iterator it2 = wrapperMethods.iterator(); it2.hasNext();) {
                    ctClass.addMethod((CtMethod)it2.next());
                }
            }

            // handles pointcut unweaving
            // looping on the original methods is enough since we will look for method with no pc
            // thus that have not been changed in the previous transformation steps
            for (Iterator i = sorteMethodTuples.iterator(); i.hasNext();) {
                MethodSequenceTuple tuple = (MethodSequenceTuple)i.next();
                //System.out.println(" tuple " + tuple.getAdvice().getName() + " : " + tuple.getStatus());
                if (tuple.getStatus() != STATUS_HASNOPOINTCUT) {
                    continue;
                }
                CtMethod method = tuple.getMethod();
                //System.out.println("FOUND NO PC = " + method.getName());
                final String prefixedMethodName = TransformationUtil.getPrefixedMethodName(
                        method.getName(), tuple.getSequence(), ctClass.getName().replace('/', '.')
                );
                // do we have a wrapper method, which is NOT marked empty
                if (JavassistHelper.hasMethod(ctClass, prefixedMethodName)) {
                    CtMethod wrapperMethod = ctClass.getDeclaredMethod(prefixedMethodName);
                    if (JavassistHelper.isAnnotatedNotEmpty(wrapperMethod)) {
                        //System.out.println("FOUND A real  Wrapper but NO PC = " + method.getName());
                        CtMethod emptyWrapperMethod = JavassistHelper.createEmptyWrapperMethod(
                                ctClass, method, tuple.getSequence()
                        );
                        method.setBody(wrapperMethod, null);
                        wrapperMethod.setBody(emptyWrapperMethod, null);
                        JavassistHelper.setAnnotatedEmpty(wrapperMethod);
                        context.markAsAdvised();
                    }
                }
            }

        }
        TransformationUtil.setJoinPointIndex(klass.getCtClass(), m_joinPointIndex);
    }

    /**
     * Creates a wrapper method for the original method specified. This method has the same signature as the original
     * method and catches the invocation for further processing by the framework before redirecting to the original
     * method.
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
        body.append('{');

        callBody.append(TransformationUtil.JOIN_POINT_MANAGER_FIELD);
        callBody.append('.');
        callBody.append(TransformationUtil.PROCEED_WITH_EXECUTION_JOIN_POINT_METHOD);
        callBody.append('(');
        callBody.append(methodHash);
        callBody.append(", ");
        callBody.append(m_joinPointIndex);
        callBody.append(", args, ");
        if (Modifier.isStatic(originalMethod.getModifiers())) {
            callBody.append("nullObject");
            body.append("Object nullObject = null;");
        }
        else {
            callBody.append("this");
        }
        callBody.append(',');
        callBody.append(TransformationUtil.JOIN_POINT_TYPE_METHOD_EXECUTION);
        callBody.append(");");

        if (originalMethod.getParameterTypes().length > 0) {
            body.append("Object[] args = $args; ");
        }
        else {
            body.append("Object[] args = null; ");
        }

        if (originalMethod.getReturnType() == CtClass.voidType) {
            // special handling for void return type leads to cleaner bytecode generation with Javassist
            body.append(callBody.toString()).append("}");
        }
        else if (!originalMethod.getReturnType().isPrimitive()) {
            body.append("return ($r)");
            body.append(callBody.toString());
            body.append("}");
        }
        else {
            String localResult = TransformationUtil.ASPECTWERKZ_PREFIX + "res";
            body.append("Object ").append(localResult).append(" = ");
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

        m_joinPointIndex++;
        JavassistHelper.setAnnotatedNotEmpty(method);

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
        String prefixedMethodName = TransformationUtil.getPrefixedMethodName(
                ctMethod.getName(), methodSequence, cg.getName()
        );
        ctMethod.setName(prefixedMethodName);
        ctMethod.setModifiers(accessFlags);
    }

    /**
     * Filters the classes to be transformed.
     * <p/>
     * TODO: when a class had execution pointcut that were removed it must be unweaved, thus not filtered out How to
     * handle that ? cache lookup ? or custom class level attribute ?
     *
     * @param definition    the definition
     * @param classMetaData the meta-data for the class
     * @param cg            the class to filter
     * @return boolean true if the method should be filtered away
     */
    private boolean classFilter(
            final SystemDefinition definition,
            final ClassMetaData classMetaData,
            final CtClass cg) {
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
        if (definition.hasExecutionPointcut(classMetaData)) {
            return false;
        }
        if (definition.inPreparePackage(className)) {
            return false;//no early filtering for prepared Class to allow RuW
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
    private int methodFilter(
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
            return STATUS_SKIP;
        }
        else if (definition.hasExecutionPointcut(classMetaData, methodMetaData)) {
            return STATUS_HASPOINTCUT;
        }
        else {
            return STATUS_HASNOPOINTCUT;
        }
    }

    /**
     * Filters the methods that have no more execution pointcut and that could have some
     *
     * @param definition    the definition
     * @param classMetaData the class meta-data
     * @param method        the method to filter
     * @return boolean
     */
    private boolean methodHasNoPointcut(
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
            return false;
        }
        else if (definition.hasExecutionPointcut(classMetaData, methodMetaData)) {
            return false;
        }
        else {
            return true;
        }
    }
}

/**
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
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
