/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transformj;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;

import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.BcelMetaDataMaker;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.JavassistMetaDataMaker;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.definition.DefinitionLoader;
import org.codehaus.aspectwerkz.transformj.Context;
import org.codehaus.aspectwerkz.transformj.Klass;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.CtField;
import javassist.CannotCompileException;
import javassist.CtNewMethod;

/**
 * Transforms member methods to become "aspect-aware".
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class AdviseMemberMethodTransformer implements Transformer, Activator {

    /**
     * The definitions.
     */
    private final List m_definitions;

    /**
     * Retrieves the weave model.
     */
    public AdviseMemberMethodTransformer() {
        super();
        m_definitions = DefinitionLoader.getDefinitionsForTransformation();
    }

    /**
     * Makes the member method transformations.
     *
     * @param context the transformation context
     * @param klass the class set.
     */
    public void transform(final Context context, final Klass klass) throws Exception {

        // loop over all the definitions
        for (Iterator it = m_definitions.iterator(); it.hasNext();) {
            AspectWerkzDefinition definition = (AspectWerkzDefinition)it.next();

            definition.loadAspects(context.getLoader());

            final CtClass cg = klass.getClassGen();
            ClassMetaData classMetaData = JavassistMetaDataMaker.createClassMetaData(cg);

            if (classFilter(definition, classMetaData, cg, false)) {
                return;
            }

            final CtMethod[] methods = cg.getDeclaredMethods();

            // get the indexes for the <init> methods
            //TODO
            List initIndexes = new ArrayList();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals("<init>")) {
                    initIndexes.add(new Integer(i));
                }
            }

            // build and sort the method lookup list
            final List methodLookupList = new ArrayList();
            for (int i = 0; i < methods.length; i++) {
                //MethodMetaData methodMetaData = JavassistMetaDataMaker.createMethodMetaData(methods[i]);
//                if (methodFilter(definition, classMetaData, methodMetaData, methods[i])) {
//                    java.lang.System.out.println("MMTF - TF - methodFiltered: " + methodMetaData.getName());
//                    continue;
//                }
                if (methodInternal(methods[i])) {
                    continue;
                }
                methodLookupList.add(methods[i]);
            }

            Collections.sort(methodLookupList, JavassistMethodComparator.getInstance());
            int ix = 0;
            for (Iterator i = methodLookupList.iterator(); i.hasNext(); ) {
                System.out.println("MMTF - TF - handling: " + ix + "  " + ((CtMethod)i.next()).getName());
                ix++;
            }

            final Map methodSequences = new HashMap();
            final List proxyMethods = new ArrayList();
            boolean isClassAdvised = false;
            boolean firstProxy = true;
            //for (int i = 0; i < methods.length; i++) {
            int methodIndex = -1;
            for (Iterator i = methodLookupList.iterator(); i.hasNext(); ) {
                CtMethod method = (CtMethod) i.next();
                methodIndex++;
                MethodMetaData methodMetaData = JavassistMetaDataMaker.createMethodMetaData(method);
                // filter the member methods
                if (methodFilter(definition, classMetaData, methodMetaData, method) ||
                        Modifier.isStatic(method.getModifiers())) {
                    continue;
                }

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

                final int methodLookupId = methodLookupList.indexOf(method);
                final int methodSequence = ((Integer)methodSequences.
                        get(method.getName())).intValue();

                // add jp field
                //addJoinPointField(cg, method, methodSequence);//not for RT model
                if (firstProxy) {
                    firstProxy = false;
                    addJoinPointContainerField(cg);
                }

                // get the join point controller
                final String controllerClassName = definition.getJoinPointController(
                        classMetaData,
                        methodMetaData
                );

                proxyMethods.add(createProxyMethodWithContainer(
                        cg, method,
                        methodLookupId,
                        methodSequence,
                        definition.getUuid(),
                        controllerClassName
                ));

                addPrefixToMethod(cg, method, methodLookupId, methodSequence, definition.getUuid());
            }

            if (isClassAdvised) {
                context.markAsAdvised();

                // add the proxy methods
                for (Iterator it2 = proxyMethods.iterator(); it2.hasNext();) {
                    cg.addMethod((CtMethod)it2.next());
                }
            }
        }
    }

    public void activate(final Context context, final Klass klass) throws Exception {

        // loop over all the definitions
        for (Iterator it = m_definitions.iterator(); it.hasNext();) {
            AspectWerkzDefinition definition = (AspectWerkzDefinition)it.next();

            definition.loadAspects(context.getLoader());

            final CtClass cg = klass.getClassGen();
            ClassMetaData classMetaData = JavassistMetaDataMaker.createClassMetaData(cg);

            if (classFilter(definition, classMetaData, cg, true)) {
                return;
            }

            final CtMethod[] methods = cg.getDeclaredMethods();

            // get the indexes for the <init> methods
            //TODO
            List initIndexes = new ArrayList();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals("<init>")) {
                    initIndexes.add(new Integer(i));
                }
            }

            // build and sort the method lookup list
            final List methodLookupList = new ArrayList();
            for (int i = 0; i < methods.length; i++) {
//                MethodMetaData methodMetaData = JavassistMetaDataMaker.createMethodMetaData(methods[i]);
//                if (methodFilter(definition, classMetaData, methodMetaData, methods[i])) {
//                    java.lang.System.out.println("MMTF - AC - methodFiltered: " + methodMetaData.getName());
//                    continue;
//                }
                if (methodInternal(methods[i])) {
                    continue;
                }
                methodLookupList.add(methods[i]);
            }

            Collections.sort(methodLookupList, JavassistMethodComparator.getInstance());
            int ix = 0;
            for (Iterator i = methodLookupList.iterator(); i.hasNext(); ) {
                System.out.println("MMTF - AC - handling: " + ix + "  " + ((CtMethod)i.next()).getName());
                ix++;
            }

            final Map methodSequences = new HashMap();
            final List proxyMethods = new ArrayList();
            boolean isClassAdvised = false;
            //for (int i = 0; i < methods.length; i++) {
            int methodIndex = -1;
            for (Iterator i = methodLookupList.iterator(); i.hasNext(); ) {
                CtMethod method = (CtMethod) i.next();
                methodIndex++;
                MethodMetaData methodMetaData = JavassistMetaDataMaker.createMethodMetaData(method);
                // filter the member methods
                if (methodFilter(definition, classMetaData, methodMetaData, method) ||
                        Modifier.isStatic(method.getModifiers())) {
                    continue;
                }

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

                final int methodLookupId = methodLookupList.indexOf(method);
                final int methodSequence = ((Integer)methodSequences.
                        get(method.getName())).intValue();

                // get the join point controller
                final String controllerClassName = definition.getJoinPointController(
                        classMetaData,
                        methodMetaData
                );

                activateMethod(
                        cg, method,
                        methodLookupId,
                        methodSequence,
                        definition.getUuid(),
                        controllerClassName
                );

            }

            if (isClassAdvised) {
                context.markAsAdvised();
            }
        }
    }


    /**
     * Add jp container field
     */
    private void addJoinPointContainerField(final CtClass cg) throws NotFoundException, CannotCompileException {
        final String joinPointContainer = "_RT_mmjp";

        try {
            cg.getField(joinPointContainer);
            return;
        } catch (NotFoundException e) {
            ;//go on
        }

        CtField field = new CtField(
                cg.getClassPool().get(TransformationUtil.THREAD_LOCAL_CLASS),
                joinPointContainer,
                cg);
        field.setModifiers(Modifier.PRIVATE | Modifier.FINAL);
        cg.addField(field, "new " + TransformationUtil.THREAD_LOCAL_CLASS+"()");
    }

    /**
     * Adds a join point member field.
     *
     * @param cp the ConstantPoolGen
     * @param cg the ClassGen
     * @param mg the MethodGen
     * @param methodSequence the methods sequence number
     */
    private void addJoinPointField(final CtClass cg,
                                   final CtMethod mg,
                                   final int methodSequence) throws NotFoundException, CannotCompileException {

        final String joinPoint = getJoinPointName(mg.getName(), methodSequence);

        try {
            cg.getField(joinPoint);
            return;
        } catch (NotFoundException e) {
            ;//go on to add it
        }

        CtField field = new CtField(
                cg.getClassPool().get(TransformationUtil.THREAD_LOCAL_CLASS),
                joinPoint,
                cg);
        field.setModifiers(Modifier.PRIVATE | Modifier.FINAL);
        cg.addField(field, "new " + TransformationUtil.THREAD_LOCAL_CLASS+"()");//TODO jcache
    }

    /**
     * Adds a prefix to the original method.
     * To make it callable only from within the framework itself.
     *
     * @param mg the MethodGen
     * @param method the current method
     * @param methodSequence the methods sequence number
     * @param uuid the definition UUID
     * @return the modified method
     */
    private void addPrefixToMethod(CtClass cg,
                                     final CtMethod mg,
                                     int methodLookupId,
                                     final int methodSequence,
                                     final String uuid) {

        // change the method access flags (should always be set to protected)
        int accessFlags = mg.getModifiers();
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

        mg.setName(getPrefixedMethodName(mg, methodLookupId, methodSequence, cg.getName()));

        mg.setModifiers(accessFlags);
    }

    /**
     * Creates a proxy method for the original method specified.
     * This method has the same signature as the original method and
     * catches the invocation for further processing by the framework
     * before redirecting to the original method.
     *
     * @todo pass the 'class' as a Class instance not a String to the join point. Add the class field to the class using BCEL (see AdviseStaticMethodTransformer.java)
     *
     * @param cp the ConstantPoolGen
     * @param cg the ClassGen
     * @param originalMethod the current method
     * @param factory the objectfactory
     * @param methodId the id of the current method in the lookup tabl
     * @param methodSequence the methods sequence number
     * @param accessFlags the access flags of the original method
     * @param uuid the uuid for the weave model defining the pointcut
     * @param controllerClassName the class name of the controller class to use
     * @return the proxy method
     */
    private CtMethod createProxyMethod(final CtClass cg,
                                     final CtMethod originalMethod,
                                     final int methodId,
                                     final int methodSequence,
                                     final String uuid,
                                     final String controllerClassName) throws NotFoundException, CannotCompileException {

        String joinPoint = getJoinPointName(originalMethod.getName(), methodSequence);

        StringBuffer body = new StringBuffer("{");
        body.append("if (").append(joinPoint).append(" == null)");
        body.append("   ").append(joinPoint).append(" = new ").append(TransformationUtil.THREAD_LOCAL_CLASS).append("();");
        body.append("Object obj = ").append(joinPoint).append(".get();");
        body.append("if (obj == null) {");
        body.append("obj = new ").append(TransformationUtil.MEMBER_METHOD_JOIN_POINT_CLASS).append("(");
            body.append("\"").append(uuid).append("\",");
            body.append("(Object)$0,");//casting needed for j lookup
            body.append("\"").append(cg.getName()).append("\",");
            body.append("(int)").append(methodId).append(",");//casting needed for j lookup
            body.append("(String)").append(controllerClassName);//casting needed for j lookup
        body.append(");");
        body.append(joinPoint).append(".set(obj);");
        body.append("}");//endif
        body.append(TransformationUtil.MEMBER_METHOD_JOIN_POINT_CLASS).append(" mmjp");
        body.append(" = (").append(TransformationUtil.MEMBER_METHOD_JOIN_POINT_CLASS).append(")obj;");
        if (originalMethod.getParameterTypes().length > 0) {
            body.append("mmjp.setParameters($args);");
        }
        if (originalMethod.getReturnType() == CtClass.voidType) {
            body.append("mmjp.proceed();");
        } else {
            body.append("return ($r)mmjp.proceed();");
        }
        body.append("}");
        //System.out.println(body);
        CtMethod method = CtNewMethod.make(
                originalMethod.getReturnType(),
                originalMethod.getName(),//TODO rename correctly handled by j ?
                originalMethod.getParameterTypes(),
                originalMethod.getExceptionTypes(),
                body.toString(),
                cg);
        method.setModifiers(originalMethod.getModifiers());
        //cg.addMethod(method); // done later
        return method;
    }

    private CtMethod createProxyMethodWithContainer(final CtClass cg,
                                     final CtMethod originalMethod,
                                     final int methodId,
                                     final int methodSequence,
                                     final String uuid,
                                     final String controllerClassName) throws NotFoundException, CannotCompileException {

        String joinPoint = getJoinPointName(originalMethod.getName(), methodSequence);

        StringBuffer body = new StringBuffer("{");
        body.append("if (_RT_mmjp == null)");
            body.append("_RT_mmjp = new ").append(TransformationUtil.THREAD_LOCAL_CLASS).append("();");
        body.append("java.util.Map cont = (java.util.Map)_RT_mmjp.get();");
        body.append("if (cont == null)");
            body.append("cont = new java.util.HashMap();");
        body.append("Object obj = cont.get(\"").append(joinPoint).append("\");");
        body.append("if (obj == null) {");
        body.append("obj = new ").append(TransformationUtil.MEMBER_METHOD_JOIN_POINT_CLASS).append("(");
            body.append("\"").append(uuid).append("\",");
            body.append("(Object)$0,");//casting needed for j lookup
            body.append("\"").append(cg.getName()).append("\",");
            body.append("(int)").append(methodId).append(",");//casting needed for j lookup
            body.append("(String)").append(controllerClassName);//casting needed for j lookup
        body.append(");");
        body.append("cont.put(\"").append(joinPoint).append("\", obj);");
        body.append("_RT_mmjp.set(cont);");
        body.append("}");//endif
        body.append(TransformationUtil.MEMBER_METHOD_JOIN_POINT_CLASS).append(" mmjp");
        body.append(" = (").append(TransformationUtil.MEMBER_METHOD_JOIN_POINT_CLASS).append(")obj;");
        if (originalMethod.getParameterTypes().length > 0) {
            body.append("mmjp.setParameters($args);");
        }
        if (originalMethod.getReturnType() == CtClass.voidType) {
            body.append("mmjp.proceed();");
        } else {
            body.append("return ($r)mmjp.proceed();");
        }
        body.append("}");
        //System.out.println(body);
        CtMethod method = CtNewMethod.make(
                originalMethod.getReturnType(),
                originalMethod.getName(),//TODO rename correctly handled by j ?
                originalMethod.getParameterTypes(),
                originalMethod.getExceptionTypes(),
                body.toString(),
                cg);
        method.setModifiers(originalMethod.getModifiers());
        //cg.addMethod(method); // done later
        return method;
    }

    private void activateMethod(final CtClass cg,
                                     final CtMethod originalMethod,
                                     final int methodId,
                                     final int methodSequence,
                                     final String uuid,
                                     final String controllerClassName) throws NotFoundException, CannotCompileException {

        String joinPoint = getJoinPointName(originalMethod.getName(), methodSequence);

        StringBuffer body = new StringBuffer("{");
        body.append("if (_RT_mmjp == null)");
            body.append("_RT_mmjp = new ").append(TransformationUtil.THREAD_LOCAL_CLASS).append("();");
        body.append("java.util.Map cont = (java.util.Map)_RT_mmjp.get();");
        body.append("if (cont == null)");
            body.append("cont = new java.util.HashMap();");
        body.append("Object obj = cont.get(\"").append(joinPoint).append("\");");
        body.append("if (obj == null) {");
        body.append("obj = new ").append(TransformationUtil.MEMBER_METHOD_JOIN_POINT_CLASS).append("(");
            body.append("\"").append(uuid).append("\",");
            body.append("(Object)$0,");//casting needed for j lookup
            body.append("\"").append(cg.getName()).append("\",");
            body.append("(int)").append(methodId).append(",");//casting needed for j lookup
            body.append("(String)").append(controllerClassName);//casting needed for j lookup
        body.append(");");
        body.append("cont.put(\"").append(joinPoint).append("\", obj);");
        body.append("_RT_mmjp.set(cont);");
        body.append("}");//endif
        body.append(TransformationUtil.MEMBER_METHOD_JOIN_POINT_CLASS).append(" mmjp");
        body.append(" = (").append(TransformationUtil.MEMBER_METHOD_JOIN_POINT_CLASS).append(")obj;");
        if (originalMethod.getParameterTypes().length > 0) {
            body.append("mmjp.setParameters($args);");
        }
        if (originalMethod.getReturnType() == CtClass.voidType) {
            body.append("mmjp.proceed();");
        } else {
            body.append("return ($r)mmjp.proceed();");
        }
        body.append("}");
        //System.out.println(body);
        originalMethod.setBody(body.toString());
    }

    /**
     * Callback method. Is being called before each transformation.
     */
    public void sessionStart() {
    }

    /**
     * Callback method. Is being called after each transformation.
     */
    public void sessionEnd() {
    }

    /**
     * Callback method. Prints a log/status message at each transformation.
     *
     * @return a log string
     */
    public String verboseMessage() {
        return this.getClass().getName();
    }

    /**
     * Filters the classes to be transformed.
     *
     * @param definition the definition
     * @param classMetaData the meta-data for the class
     * @param cg the class to filter
     * @return boolean true if the method should be filtered away
     */
    private boolean classFilter(final AspectWerkzDefinition definition,
                                final ClassMetaData classMetaData,
                                final CtClass cg, final boolean isActivatePhase) throws NotFoundException {
        if (cg.isInterface() ||
                cg.getSuperclass().getName().equals(org.codehaus.aspectwerkz.xmldef.advice.AroundAdvice.class.getName()) ||
                cg.getSuperclass().getName().equals(org.codehaus.aspectwerkz.xmldef.advice.PreAdvice.class.getName()) ||
                cg.getSuperclass().getName().equals(org.codehaus.aspectwerkz.xmldef.advice.PostAdvice.class.getName())) {
            return true;
            //TODO: complex inheritance not supported
        }
        String className = cg.getName();
        if (definition.inExcludePackage(className)) {
            return true;
        }
        if (!definition.inIncludePackage(className)) {
            return true;
        }
        if (definition.inPreparePackage(className) && !isActivatePhase) {
            return true;
        }
        if (definition.hasExecutionPointcut(classMetaData)
                || definition.hasThrowsPointcut(classMetaData)) {
            return false;
        }
        return true;
    }

    /**
     * Filters the methods to be transformed.
     *
     * @param definition the definition
     * @param classMetaData the class meta-data
     * @param method the method to filter
     * @return boolean
     */
    private boolean methodFilter(final AspectWerkzDefinition definition,
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
        else if (definition.hasThrowsPointcut(classMetaData, methodMetaData)) {
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * Filters the internal methods
     *
     * @param definition the definition
     * @param classMetaData the class meta-data
     * @param method the method to filter
     * @return boolean
     */
    private boolean methodInternal(final CtMethod method) {
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
        return false;
    }

    /**
     * Returns the name of the join point.
     *
     * @param method the method
     * @param methodSequence the method sequence
     * @return the name of the join point
     */
    private static String getJoinPointName(final String methodName, final int methodSequence) {
        final StringBuffer joinPoint = new StringBuffer();
        joinPoint.append(TransformationUtil.MEMBER_METHOD_JOIN_POINT_PREFIX);
        joinPoint.append(methodName);
        joinPoint.append(TransformationUtil.DELIMITER);
        joinPoint.append(methodSequence);
        return joinPoint.toString();
    }

    /**
     * Returns the prefixed method name.
     *
     * @param method the method
     * @param methodSequence the method sequence
     * @param className the class name
     * @return the name of the join point
     */
    private String getPrefixedMethodName(final CtMethod method,
                                         int methodLookupId,
                                         final int methodSequence,
                                         final String className) {
        final StringBuffer methodName = new StringBuffer();
        methodName.append(TransformationUtil.ORIGINAL_METHOD_PREFIX);
        /**/methodName.append(methodLookupId);
        /**/methodName.append(TransformationUtil.DELIMITER);
        methodName.append(method.getName());
        methodName.append(TransformationUtil.DELIMITER);
        methodName.append(methodSequence);
        methodName.append(TransformationUtil.DELIMITER);
        methodName.append(className.replace('.', '_'));
        return methodName.toString();
    }
}
