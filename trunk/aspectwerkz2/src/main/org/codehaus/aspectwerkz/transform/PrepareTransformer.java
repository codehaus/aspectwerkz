/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;

import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.JavassistMetaDataMaker;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.definition.DefinitionLoader;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.CtField;
import javassist.CannotCompileException;
import javassist.CtNewMethod;

/**
 * Transforms class for preparation state
 * Class granularity
 * TODO runtime weaving and class preparation to HotSwap
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class PrepareTransformer implements Transformer {

    /**
     * Makes the static method transformations.
     *
     * @todo refactor so that we don't have to loop over all the methods twice (and create a method meta-data object twice)
     *
     * @param context the transformation context
     * @param klass the class set.
     */
    public void transform(final Context context, final Klass klass) throws NotFoundException, CannotCompileException {

        // loop over all the definitions
        for (Iterator it = DefinitionLoader.getDefinitions().iterator(); it.hasNext();) {
            SystemDefinition definition = (SystemDefinition)it.next();

            final CtClass cg = klass.getCtClass();
            ClassMetaData classMetaData = JavassistMetaDataMaker.createClassMetaData(cg);

            if (classFilter(definition, classMetaData, cg)) {
                return;
            }

            final CtMethod[] methods = cg.getDeclaredMethods();

            // get the index for the <clinit> method (if there is one)
            boolean noClinitMethod = true;
            int indexClinit = -1;
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals("<clinit>")) {
                    indexClinit = i;
                    noClinitMethod = false;
                    break;
                }
            }

            // build and sort the method lookup list
            final List methodLookupList = new ArrayList();
            for (int i = 0; i < methods.length; i++) {
                CtMethod method = methods[i];

                MethodMetaData methodMetaData = JavassistMetaDataMaker.createMethodMetaData(methods[i]);
                if (methodFilter(definition, classMetaData, methodMetaData, method)) {
                    continue;
                }
                methodLookupList.add(methods[i]);

                // TODO: does not work, needs to be thought through more
                // if advised swap add the prefixed one as well to enable second-round instrumentation
//                String originalPrefixedName =
//                        TransformationUtil.ORIGINAL_METHOD_PREFIX +
//                        methods[i].getName();
//                Method[] declaredMethods = cg.getMethods();
//                for (int j = 0; j < declaredMethods.length; j++) {
//                    Method declaredMethod = declaredMethods[j];
//                    if (declaredMethod.getName().startsWith(originalPrefixedName)) {
//                        methodLookupList.add(declaredMethod);
//                    }
//                }
            }

            Collections.sort(methodLookupList, JavassistMethodComparator.getInstance());

            final Map methodSequences = new HashMap();
            final List newMethods = new ArrayList();
            CtMethod clInitMethod = null;
            boolean isClassAdvised = false;
            boolean firstProxy = true;
            boolean firstStaticProxy = true;
            for (int i = 0; i < methods.length; i++) {
                CtMethod method = methods[i];
                MethodMetaData methodMetaData = JavassistMetaDataMaker.createMethodMetaData(method);

                if (methodFilter(definition, classMetaData, methodMetaData, method)) {
                    continue;
                }

                isClassAdvised = true;

                // take care of identification of overloaded methods by
                // inserting a sequence number
                if (methodSequences.containsKey(method.getName())) {
                    int sequence = ((Integer)methodSequences.get(methods[i].getName())).intValue();
                    methodSequences.remove(method.getName());
                    sequence++;
                    methodSequences.put(method.getName(), new Integer(sequence));
                }
                else {
                    methodSequences.put(method.getName(), new Integer(1));
                }

                final int methodLookupId = methodLookupList.indexOf(method);
                final int methodSequence = ((Integer)methodSequences.get(method.getName())).intValue();

                // non static
                if (!Modifier.isStatic(method.getModifiers())) {
                    if (firstProxy) {
                        firstProxy = false;
                        addJoinPointContainerField(cg);
                    }
                }
                else {
                    if (firstStaticProxy) {
                        firstStaticProxy = false;
                        createStaticClassField(cg);
                        addStaticJoinPointContainerField(cg);
                    }
                }

                // add a prefix to the original method
                String formerName = method.getName();
                int formerModifiers = method.getModifiers();
                addPrefixToMethod(cg, method, methodLookupId, methodSequence, definition.getUuid());

                // create a proxy method for the original method
                newMethods.add(createPreparedProxyMethod(
                        cg,
                        method,
                        formerName,
                        formerModifiers,
                        methodLookupId,
                        methodSequence,
                        definition.getUuid()
                ));

            }

            if (isClassAdvised) {
                context.markAsAdvised();

                // if we have transformed methods, create the static class field
                //createStaticClassField(cg);

                // add the proxy methods
                for (Iterator it2 = newMethods.iterator(); it2.hasNext();) {
                    cg.addMethod((CtMethod)it2.next());
                }

                context.markAsPrepared();
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
        }
        catch (NotFoundException e) {
            ;//go on
        }

        CtField field = new CtField(
                cg.getClassPool().get(TransformationUtil.THREAD_LOCAL_CLASS),
                joinPointContainer,
                cg);
        field.setModifiers(Modifier.PRIVATE | Modifier.FINAL);
        cg.addField(field, "new " + TransformationUtil.THREAD_LOCAL_CLASS + "()");
    }

    private void addStaticJoinPointContainerField(final CtClass cg)
            throws NotFoundException, CannotCompileException {

        final String joinPoint = "_RT_smjp";

        try {
            cg.getField(joinPoint);
            return;
        }
        catch (NotFoundException e) {
            ;//go on to add it
        }

        CtField field = new CtField(
                cg.getClassPool().get(TransformationUtil.THREAD_LOCAL_CLASS),
                joinPoint,
                cg);
        field.setModifiers(Modifier.PRIVATE | Modifier.FINAL | Modifier.STATIC);
        cg.addField(field, "new " + TransformationUtil.THREAD_LOCAL_CLASS + "()");
    }

    /**
     * Creates a static join point field.
     *
     * @param cp the ConstantPoolGen
     * @param cg the ClassGen
     * @param mg the MethodGen
     * @param methodSequence the methods sequence number
     */
    private void addStaticJoinPointField(final CtClass cg,
                                         final CtMethod mg,
                                         final int methodSequence) throws NotFoundException, CannotCompileException {
        final String joinPoint = getJoinPointName(mg, methodSequence);

        try {
            cg.getField(joinPoint);
            return;
        }
        catch (NotFoundException e) {
            ;//go on to add it
        }

        CtField field = new CtField(
                cg.getClassPool().get(TransformationUtil.THREAD_LOCAL_CLASS),
                joinPoint,
                cg);
        field.setModifiers(Modifier.PRIVATE | Modifier.FINAL | Modifier.STATIC);
        cg.addField(field, "new " + TransformationUtil.THREAD_LOCAL_CLASS + "()");//TODO jcache
    }

    /**
     * Creates a new static class field.
     *
     * @param cp the ConstantPoolGen
     * @param cg the ClassGen
     * @param clInit the constructor for the class
     * @param factory the objectfactory
     * @return the modified clinit method
     */
    private void createStaticClassField(
            final CtClass cg) throws NotFoundException, CannotCompileException {
        final String className = cg.getName();

        CtField field = new CtField(cg.getClassPool().get("java.lang.Class"),
                TransformationUtil.STATIC_CLASS_FIELD,
                cg);
        field.setModifiers(Modifier.STATIC | Modifier.PRIVATE);
        cg.addField(field, "java.lang.Class.forName(\"" + className + "\")");
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
    private void addPrefixToMethod(final CtClass cg,
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
     * @param cp the ConstantPoolGen
     * @param cg the ClassGen
     * @param originalMethod the current method
     * @param factory the objectfactory
     * @param methodId the id of the current method in the lookup tabl
     * @param methodSequence the methods sequence number
     * @param accessFlags the access flags for the original method
     * @param uuid the UUID for the weave model
     * @param controllerClassName the class name of the controller class to use
     * @return the proxy method
     */
    private CtMethod createPreparedProxyMethod(
            final CtClass cg,
            final CtMethod originalMethod,
            String formerName,
            int formerModifiers,
            final int methodId,
            final int methodSequence,
            final String uuid
            ) throws NotFoundException, CannotCompileException {
        String joinPoint = getJoinPointName(originalMethod, methodSequence);

        StringBuffer body = new StringBuffer("{");
        //if (originalMethod.getReturnType() == CtClass.voidType) {
        body.append("return ").append(originalMethod.getName()).append("($$);");
        //} else {
        //    body.append("return ($r)mmjp.proceed();");
        //}
        body.append("}");
        CtMethod method = null;
        if (Modifier.isStatic(originalMethod.getModifiers())) {
            // j bug
            method = JavassistHelper.makeStatic(
                    originalMethod.getReturnType(),
                    formerName,
                    originalMethod.getParameterTypes(),
                    originalMethod.getExceptionTypes(),
                    body.toString(),
                    cg);
        }
        else {
            method = CtNewMethod.make(
                    originalMethod.getReturnType(),
                    formerName,
                    originalMethod.getParameterTypes(),
                    originalMethod.getExceptionTypes(),
                    body.toString(),
                    cg);
        }
        method.setModifiers(formerModifiers);
        //cg.addMethod(method); // done later
        return method;
    }

    private CtMethod createProxyMethodWithContainer(
            final CtClass cg,
            final CtMethod originalMethod,
            final int methodId,
            final int methodSequence,
            final String uuid,
            final String controllerClassName) throws NotFoundException, CannotCompileException {

        String joinPoint = getJoinPointName(originalMethod, methodSequence);

        StringBuffer body = new StringBuffer("{");
        body.append("if (_RT_smjp == null)");
        body.append("_RT_smjp = new ").append(TransformationUtil.THREAD_LOCAL_CLASS).append("();");
        body.append("java.util.Map cont = (java.util.Map)_RT_smjp.get();");
        body.append("if (cont == null)");
        body.append("cont = new java.util.HashMap();");
        body.append("Object obj = cont.get(\"").append(joinPoint).append("\");");
        body.append("if (obj == null) {");
        body.append("obj = new ").append(TransformationUtil.STATIC_METHOD_JOIN_POINT_CLASS).append("(");
        body.append("\"").append(uuid).append("\",");
        body.append("(Class)").append(TransformationUtil.STATIC_CLASS_FIELD).append(",");//casting needed for j lookup
        body.append("(int)").append(methodId).append(",");//casting needed for j lookup
        body.append("(String)").append(controllerClassName);//casting needed for j lookup
        body.append(");");
        body.append("cont.put(\"").append(joinPoint).append("\", obj);");
        body.append("_RT_smjp.set(cont);");
        body.append("}");//endif
        body.append(TransformationUtil.STATIC_METHOD_JOIN_POINT_CLASS).append(" smjp");
        body.append(" = (").append(TransformationUtil.STATIC_METHOD_JOIN_POINT_CLASS).append(")obj;");
        if (originalMethod.getParameterTypes().length > 0) {
            //System.out.println(originalMethod.getParameterTypes());
            body.append("smjp.setParameters($args);");
        }
        if (originalMethod.getReturnType() == CtClass.voidType) {
            body.append("smjp.proceed();");
        }
        else {
            body.append("return ($r)smjp.proceed();");
        }
        body.append("}");
        CtMethod method = JavassistHelper.makeStatic(//CtNewMethod.make boggus with static method
                originalMethod.getReturnType(),
                originalMethod.getName(), //TODO rename correctly handled by j ?
                originalMethod.getParameterTypes(),
                originalMethod.getExceptionTypes(),
                body.toString(),
                cg);
        method.setModifiers(originalMethod.getModifiers());
        //cg.addMethod(method); // done later
        return method;
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
    private boolean classFilter(final SystemDefinition definition,
                                final ClassMetaData classMetaData,
                                final CtClass cg) throws NotFoundException {
        if (cg.isInterface() || cg.getSuperclass().getName().equals("org.codehaus.aspectwerkz.aspect.Aspect")) {
            return true;
            //TODO complex inherit not supported
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
     * Filters the methods to be transformed.
     *
     * @param definition the definition
     * @param classMetaData the class meta-data
     * @param methodMetaData the method meta-data
     * @param method the method to filter
     * @return boolean
     */
    private boolean methodFilter(final SystemDefinition definition,
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

    /**
     * Returns the name of the join point.
     *
     * @param method the method
     * @param methodSequence the method sequence
     * @return the name of the join point
     */
    private String getJoinPointName(final CtMethod method,
                                    final int methodSequence) {
        final StringBuffer joinPoint = new StringBuffer();
        joinPoint.append(TransformationUtil.STATIC_METHOD_JOIN_POINT_PREFIX);
        joinPoint.append(method.getName());
        joinPoint.append(TransformationUtil.DELIMITER);
        joinPoint.append(methodSequence);
        return joinPoint.toString();
    }

    /**
     * Returns the prefixed method name.
     *
     * @param method the method
     * @param methodSequence the method sequence
     * @param className FQN of the declaring class
     * @return the name of the join point
     */
    private static String getPrefixedMethodName(final CtMethod method,
                                                int methodLookupId,
                                                final int methodSequence,
                                                final String className) {
        final StringBuffer methodName = new StringBuffer();
        methodName.append(TransformationUtil.ORIGINAL_METHOD_PREFIX);
        methodName.append(methodLookupId);
        methodName.append(TransformationUtil.DELIMITER);
        methodName.append(method.getName());
        methodName.append(TransformationUtil.DELIMITER);
        methodName.append(methodSequence);
        methodName.append(TransformationUtil.DELIMITER);
        methodName.append(className.replace('.', '_'));
        return methodName.toString();
    }
}

