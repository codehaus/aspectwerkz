/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transformj;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.JavassistMetaDataMaker;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.definition.DefinitionLoader;
import org.codehaus.aspectwerkz.transformj.Transformer;
import org.codehaus.aspectwerkz.transformj.Context;
import org.codehaus.aspectwerkz.transformj.Klass;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import javassist.CtClass;
import javassist.Modifier;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.NotFoundException;
import javassist.CtField;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

/**
 * Advises caller side method invocations.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AdviseCallerSideMethodTransformer implements Transformer {

    /**
     * The definition.
     */
    private final AspectWerkzDefinition m_definition;

    /**
     * Constructor.
     */
    public AdviseCallerSideMethodTransformer() {
        super();
        // TODO: fix loop over definitions
        m_definition = (AspectWerkzDefinition)DefinitionLoader.getDefinitionsForTransformation().get(0);
    }

    /**
     * Transforms the call side pointcuts.
     *
     * @param context the transformation context
     * @param klass the class set.
     */
    public void transform(final Context context, final Klass klass) throws NotFoundException, CannotCompileException {
        m_definition.loadAspects(context.getLoader());

        final CtClass cg = klass.getClassGen();
        final ClassMetaData classMetaData = JavassistMetaDataMaker.createClassMetaData(cg);

        // filter caller classes
        if (classFilter(classMetaData, cg)) {
            return;
        }

        final Set callerSideJoinPoints = new HashSet();
        final Map methodSequences = new HashMap();

        // anonym ExprEditor
        ExprEditor callerSideEd = new ExprEditor() {

            public boolean isClassAdvised = false;

            public void edit(MethodCall m) throws CannotCompileException {
                try {
                    CtBehavior callerBehavior = m.where();
                    //todo caution: <clinit> might fail

                    // filter caller methods
                    if (methodFilterCaller(callerBehavior)) {
                        return;
                    }

                    // get the callee method name, signature and class name
                    final String calleeMethodName = m.getMethodName();
                    final String calleeClassName = m.getClassName();
                    final String calleeMethodSignature = m.getMethod().getSignature();

                    // filter callee classes
                    if (!m_definition.inIncludePackage(calleeClassName)) {
                        return;
                    }

                    // filter callee methods
                    if (methodFilterCallee(calleeMethodName)) {
                        return;
                    }

                    // create the class meta-data
                    ClassMetaData calleeSideClassMetaData;
                    try {
                        calleeSideClassMetaData = JavassistMetaDataMaker.createClassMetaData(context.getClassPool().get(calleeClassName));
                    }
                    catch (NotFoundException e) {
                        throw new WrappedRuntimeException(e);
                    }

                    // create the method meta-data
                    MethodMetaData calleeSideMethodMetaData = JavassistMetaDataMaker.createMethodMetaData(
                            m.getMethod()
                    );

                    // is this a caller side method pointcut?
                    if (m_definition.isPickedOutByCallPointcut(calleeSideClassMetaData, calleeSideMethodMetaData)) {

                        // get the caller method name and signature
                        String callerMethodName = callerBehavior.getName();
                        String callerMethodSignature = callerBehavior.getSignature();

                        // take care of identification of overloaded methods
                        // by inserting a sequence number
                        if (methodSequences.containsKey(calleeMethodName)) {
                            int sequence = ((Integer)methodSequences.get(calleeMethodName)).intValue();

                            methodSequences.remove(calleeMethodName);
                            sequence++;
                            methodSequences.put(calleeMethodName, new Integer(sequence));
                        }
                        else {
                            methodSequences.put(calleeMethodName, new Integer(1));
                        }
                        int methodSequence = ((Integer)methodSequences.get(calleeMethodName)).intValue();

                        if (!isClassAdvised) {
                            isClassAdvised = true;
                            createStaticClassField(cg);
                        }

                        // create JP field first
                        final String joinPointPrefix =  TransformationUtil.CALLER_SIDE_JOIN_POINT_PREFIX;
                        final StringBuffer joinPoint = getJoinPointName(joinPointPrefix, calleeMethodName, methodSequence);

                        // skip the creation of the join point if we already have one
                        if (!callerSideJoinPoints.contains(joinPoint.toString())) {
                            callerSideJoinPoints.add(joinPoint.toString());
                            addStaticJoinPointField(cg,
                                                    joinPoint.toString(),
                                                    callerMethodName,
                                                    callerMethodSignature,
                                                    calleeClassName+TransformationUtil.CALL_SIDE_DELIMITER+calleeMethodName,
                                                    calleeMethodSignature, m_definition.getUuid()
                            );
                        }

                        StringBuffer code = new StringBuffer("{");
                        code.append(joinPoint).append(".pre();");
                        code.append("$_ = $proceed($$);");
                        code.append(joinPoint).append(".post();");
                        code.append("}");

                        m.replace(code.toString());
                        context.markAsAdvised();
                    }
                } catch (NotFoundException nfe) {
                    nfe.printStackTrace();
                }
            }
        };

        cg.instrument(callerSideEd);
    }

    /**
     * Filters the classes to be transformed.
     *
     * @param classMetaData the meta-data for the class
     * @param cg the class to filter
     * @return boolean true if the method should be filtered away
     */
    private boolean classFilter(final ClassMetaData classMetaData, final CtClass cg) {
        if (cg.isInterface() ||
                TransformationUtil.hasSuperClass(classMetaData, "org.codehaus.aspectwerkz.attribdef.aspect.Aspect") ||
                TransformationUtil.hasSuperClass(classMetaData, "org.codehaus.aspectwerkz.xmldef.advice.AroundAdvice") ||
                TransformationUtil.hasSuperClass(classMetaData, "org.codehaus.aspectwerkz.xmldef.advice.PreAdvice") ||
                TransformationUtil.hasSuperClass(classMetaData, "org.codehaus.aspectwerkz.xmldef.advice.PostAdvice")) {
            return true;
        }
        String className = cg.getName();
        if (m_definition.inExcludePackage(className)) {
            return true;
        }
        if (!m_definition.inIncludePackage(className)) {
            return true;
        }
        if (m_definition.hasCallPointcut(classMetaData)) {
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
    private boolean methodFilterCaller(final CtBehavior method) {
        if (Modifier.isNative(method.getModifiers()) ||
                Modifier.isInterface(method.getModifiers()) ||
                method.getName().equals(TransformationUtil.GET_META_DATA_METHOD) ||
                method.getName().equals(TransformationUtil.SET_META_DATA_METHOD) ||
                method.getName().equals(TransformationUtil.CLASS_LOOKUP_METHOD) ||
                method.getName().equals(TransformationUtil.GET_UUID_METHOD)) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Filters the callee methods.
     *
     * @param methodName the name of method to filter
     * @return boolean true if the method should be filtered away
     */
    private boolean methodFilterCallee(final String methodName) {
        if (methodName.equals("<init>") ||
                methodName.equals("<clinit>") ||
                methodName.startsWith(TransformationUtil.ORIGINAL_METHOD_PREFIX) ||
                methodName.equals(TransformationUtil.GET_META_DATA_METHOD) ||
                methodName.equals(TransformationUtil.SET_META_DATA_METHOD) ||
                methodName.equals(TransformationUtil.CLASS_LOOKUP_METHOD) ||
                methodName.equals(TransformationUtil.GET_UUID_METHOD)) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Returns the name of the join point.
     *
     * @param joinPointPrefix the prefix
     * @param methodName the name of the method
     * @param methodSequence the sequence number for the method
     * @return the name of the join point
     */
    private static StringBuffer getJoinPointName(final String joinPointPrefix,
                                                 final String methodName,
                                                 final int methodSequence) {
        final StringBuffer joinPoint = new StringBuffer();
        joinPoint.append(joinPointPrefix);
        joinPoint.append(methodName);
        joinPoint.append(TransformationUtil.DELIMITER);
        joinPoint.append(methodSequence);
        return joinPoint;
    }

    /**
     * Create a static Class field in the target class
     *
     * @param cg ClassGen
     * @throws NotFoundException
     * @throws CannotCompileException
     */
    private void createStaticClassField(final CtClass cg)
    throws NotFoundException, CannotCompileException {
        final String className = cg.getName();

        CtField field = new CtField(cg.getClassPool().get("java.lang.Class"),
                TransformationUtil.STATIC_CLASS_FIELD,
                cg);
        field.setModifiers(Modifier.STATIC | Modifier.PRIVATE);
        cg.addField(field, "java.lang.Class.forName(\""+className+"\")");
    }

    /**
     * Add a JP field
     *
     * @param cg class
     * @param joinPoint field name
     * @param callerMethodName
     * @param callerMethodSignature
     * @param fullCalleeMethodName
     * @param calleeMethodSignature
     * @param uuid
     * @throws NotFoundException
     * @throws CannotCompileException
     */
    private void addStaticJoinPointField(final CtClass cg,
                                         final String joinPoint,
                                         final String callerMethodName,
                                         final String callerMethodSignature,
                                         final String fullCalleeMethodName,
                                         final String calleeMethodSignature,
                                         final String uuid)
    throws NotFoundException, CannotCompileException {
        // is the JP already there
        try {
            cg.getField(joinPoint);
            return;
        } catch (NotFoundException e) {
            ;//go on to add it
        }

        CtField field = new CtField(
                cg.getClassPool().get(TransformationUtil.CALLER_SIDE_JOIN_POINT_CLASS),
                joinPoint,
                cg);
        field.setModifiers(Modifier.PRIVATE | Modifier.FINAL | Modifier.STATIC);
        StringBuffer code = new StringBuffer("");
        code.append("new ").append(TransformationUtil.CALLER_SIDE_JOIN_POINT_CLASS).append("(");
        code.append("\"").append(uuid).append("\"");
        code.append(", ").append(TransformationUtil.STATIC_CLASS_FIELD);
        code.append(", \"").append(callerMethodName).append("\"");
        code.append(", \"").append(callerMethodSignature).append("\"");
        code.append(", \"").append(fullCalleeMethodName).append("\"");
        code.append(", \"").append(calleeMethodSignature).append("\"");
        code.append(");");

        cg.addField(field, code.toString());
    }

}
