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
 * Prepare class for further hotswap for execution pointcut
 * TODO support for constructor pointcuts
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class PrepareTransformer extends MethodExecutionTransformer implements Transformer {

    /**
     * List with the definitions.
     */
    private List m_definitions;

    /**
     * Creates a new instance of the transformer.
     */
    public PrepareTransformer() {
        m_definitions = DefinitionLoader.getDefinitions();
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

                CtMethod wrapperMethod = createEmptyWrapperMethod(ctClass, method, methodSequence);
                if (wrapperMethod != null) {
                    wrapperMethods.add(wrapperMethod);
                }
            }

            if (isClassAdvised) {
                context.markAsPrepared();

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

        String wrapperMethodName = TransformationUtil.getPrefixedMethodName(originalMethod, methodSequence, ctClass.getName());

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

        return method;
    }

//    /**
//     * Add jp container field
//     */
//    private void addJoinPointContainerField(final CtClass cg) throws NotFoundException, CannotCompileException {
//        final String joinPointContainer = "_RT_mmjp";
//
//        try {
//            cg.getField(joinPointContainer);
//            return;
//        }
//        catch (NotFoundException e) {
//            ;//go on
//        }
//
//        CtField field = new CtField(
//                cg.getClassPool().get(TransformationUtil.THREAD_LOCAL_CLASS),
//                joinPointContainer,
//                cg
//        );
//        field.setModifiers(Modifier.PRIVATE | Modifier.FINAL);
//        cg.addField(field, "new " + TransformationUtil.THREAD_LOCAL_CLASS + "()");
//    }
//
//    private void addStaticJoinPointContainerField(final CtClass cg)
//            throws NotFoundException, CannotCompileException {
//
//        final String joinPoint = "_RT_smjp";
//
//        try {
//            cg.getField(joinPoint);
//            return;
//        }
//        catch (NotFoundException e) {
//            ;//go on to add it
//        }
//
//        CtField field = new CtField(
//                cg.getClassPool().get(TransformationUtil.THREAD_LOCAL_CLASS),
//                joinPoint,
//                cg
//        );
//        field.setModifiers(Modifier.PRIVATE | Modifier.FINAL | Modifier.STATIC);
//        cg.addField(field, "new " + TransformationUtil.THREAD_LOCAL_CLASS + "()");
//    }

//    /**
//     * Creates a static join point field.
//     *
//     * @param cp             the ConstantPoolGen
//     * @param cg             the ClassGen
//     * @param mg             the MethodGen
//     * @param methodSequence the methods sequence number
//     */
//    private void addStaticJoinPointField(
//            final CtClass cg,
//            final CtMethod mg,
//            final int methodSequence) throws NotFoundException, CannotCompileException {
//        final String joinPoint = getJoinPointName(mg, methodSequence);
//
//        try {
//            cg.getField(joinPoint);
//            return;
//        }
//        catch (NotFoundException e) {
//            ;//go on to add it
//        }
//
//        CtField field = new CtField(
//                cg.getClassPool().get(TransformationUtil.THREAD_LOCAL_CLASS),
//                joinPoint,
//                cg
//        );
//        field.setModifiers(Modifier.PRIVATE | Modifier.FINAL | Modifier.STATIC);
//        cg.addField(field, "new " + TransformationUtil.THREAD_LOCAL_CLASS + "()");//TODO jcache
//    }

//    /**
//     * Creates a new static class field.
//     *
//     * @param cp      the ConstantPoolGen
//     * @param cg      the ClassGen
//     * @param clInit  the constructor for the class
//     * @param factory the objectfactory
//     * @return the modified clinit method
//     */
//    private void createStaticClassField(final CtClass cg) throws NotFoundException, CannotCompileException {
//        final String className = cg.getName();
//
//        CtField field = new CtField(
//                cg.getClassPool().get("java.lang.Class"),
//                TransformationUtil.STATIC_CLASS_FIELD,
//                cg
//        );
//        field.setModifiers(Modifier.STATIC | Modifier.PRIVATE);
//        cg.addField(field, "java.lang.Class.forName(\"" + className + "\")");
//    }

//    /**
//     * Adds a prefix to the original method. To make it callable only from within the framework itself.
//     *
//     * @param mg             the MethodGen
//     * @param method         the current method
//     * @param methodSequence the methods sequence number
//     * @param uuid           the definition UUID
//     * @return the modified method
//     */
//    private void addPrefixToMethod(
//            final CtClass cg,
//            final CtMethod mg,
//            int methodLookupId,
//            final int methodSequence,
//            final String uuid) {
//
//        // change the method access flags (should always be set to protected)
//        int accessFlags = mg.getModifiers();
//        if ((accessFlags & Modifier.PROTECTED) == 0) {
//            // set the protected flag
//            accessFlags |= Modifier.PROTECTED;
//        }
//        if ((accessFlags & Modifier.PRIVATE) != 0) {
//            // clear the private flag
//            accessFlags &= ~Modifier.PRIVATE;
//        }
//        if ((accessFlags & Modifier.PUBLIC) != 0) {
//            // clear the public flag
//            accessFlags &= ~Modifier.PUBLIC;
//        }
//
//        mg.setName(getPrefixedMethodName(mg, methodLookupId, methodSequence, cg.getName()));
//        mg.setModifiers(accessFlags);
//    }

//    /**
//     * Creates a proxy method for the original method specified. This method has the same signature as the original
//     * method and catches the invocation for further processing by the framework before redirecting to the original
//     * method.
//     *
//     * @param cp                  the ConstantPoolGen
//     * @param cg                  the ClassGen
//     * @param originalMethod      the current method
//     * @param factory             the objectfactory
//     * @param methodId            the id of the current method in the lookup tabl
//     * @param methodSequence      the methods sequence number
//     * @param accessFlags         the access flags for the original method
//     * @param uuid                the UUID for the weave model
//     * @param controllerClassName the class name of the controller class to use
//     * @return the proxy method
//     */
//    private CtMethod createPreparedProxyMethod(
//            final CtClass cg,
//            final CtMethod originalMethod,
//            String formerName,
//            int formerModifiers,
//            final int methodId,
//            final int methodSequence,
//            final String uuid) throws NotFoundException, CannotCompileException {
//        String joinPoint = getJoinPointName(originalMethod, methodSequence);
//
//        StringBuffer body = new StringBuffer("{");
//        //if (originalMethod.getReturnType() == CtClass.voidType) {
//        body.append("return ").append(originalMethod.getName()).append("($$);");
//        //} else {
//        //    body.append("return ($r)mmjp.proceed();");
//        //}
//        body.append("}");
//        CtMethod method = null;
//        if (Modifier.isStatic(originalMethod.getModifiers())) {
//            // j bug
//            method = JavassistHelper.makeStatic(
//                    originalMethod.getReturnType(),
//                    formerName,
//                    originalMethod.getParameterTypes(),
//                    originalMethod.getExceptionTypes(),
//                    body.toString(),
//                    cg
//            );
//        }
//        else {
//            method = CtNewMethod.make(
//                    originalMethod.getReturnType(),
//                    formerName,
//                    originalMethod.getParameterTypes(),
//                    originalMethod.getExceptionTypes(),
//                    body.toString(),
//                    cg
//            );
//        }
//        method.setModifiers(formerModifiers);
//        //cg.addMethod(method); // done later
//        return method;
//    }

//    private CtMethod createProxyMethodWithContainer(
//            final CtClass cg,
//            final CtMethod originalMethod,
//            final int methodId,
//            final int methodSequence,
//            final String uuid,
//            final String controllerClassName) throws NotFoundException, CannotCompileException {
//
//        String joinPoint = getJoinPointName(originalMethod, methodSequence);
//
//        StringBuffer body = new StringBuffer("{");
//        body.append("if (_RT_smjp == null)");
//        body.append("_RT_smjp = new ").append(TransformationUtil.THREAD_LOCAL_CLASS).append("();");
//        body.append("java.util.Map cont = (java.util.Map)_RT_smjp.get();");
//        body.append("if (cont == null)");
//        body.append("cont = new java.util.HashMap();");
//        body.append("Object obj = cont.get(\"").append(joinPoint).append("\");");
//        body.append("if (obj == null) {");
//        body.append("obj = new ").append(TransformationUtil.STATIC_METHOD_JOIN_POINT_CLASS).append("(");
//        body.append("\"").append(uuid).append("\",");
//        body.append("(Class)").append(TransformationUtil.STATIC_CLASS_FIELD).append(",");//casting needed for j lookup
//        body.append("(int)").append(methodId).append(",");//casting needed for j lookup
//        body.append("(String)").append(controllerClassName);//casting needed for j lookup
//        body.append(");");
//        body.append("cont.put(\"").append(joinPoint).append("\", obj);");
//        body.append("_RT_smjp.set(cont);");
//        body.append("}");//endif
//        body.append(TransformationUtil.STATIC_METHOD_JOIN_POINT_CLASS).append(" smjp");
//        body.append(" = (").append(TransformationUtil.STATIC_METHOD_JOIN_POINT_CLASS).append(")obj;");
//        if (originalMethod.getParameterTypes().length > 0) {
//            //System.out.println(originalMethod.getParameterTypes());
//            body.append("smjp.setParameters($args);");
//        }
//        if (originalMethod.getReturnType() == CtClass.voidType) {
//            body.append("smjp.proceed();");
//        }
//        else {
//            body.append("return ($r)smjp.proceed();");
//        }
//        body.append("}");
//        CtMethod method = JavassistHelper.makeStatic(
//                //CtNewMethod.make boggus with static method
//                originalMethod.getReturnType(),
//                originalMethod.getName(), //TODO rename correctly handled by j ?
//                originalMethod.getParameterTypes(),
//                originalMethod.getExceptionTypes(),
//                body.toString(),
//                cg
//        );
//        method.setModifiers(originalMethod.getModifiers());
//        //cg.addMethod(method); // done later
//        return method;
//    }

    /**
     * Filters the classes to be transformed.
     * Takes only "prepare" declarations into account
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
     * Filters the methods to be transformed.
     * Does not check execution pointcuts
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

//    /**
//     * Returns the name of the join point.
//     *
//     * @param method         the method
//     * @param methodSequence the method sequence
//     * @return the name of the join point
//     */
//    private String getJoinPointName(
//            final CtMethod method,
//            final int methodSequence) {
//        final StringBuffer joinPoint = new StringBuffer();
//        joinPoint.append(TransformationUtil.STATIC_METHOD_JOIN_POINT_PREFIX);
//        joinPoint.append(method.getName());
//        joinPoint.append(TransformationUtil.DELIMITER);
//        joinPoint.append(methodSequence);
//        return joinPoint.toString();
//    }

//    /**
//     * Returns the prefixed method name.
//     *
//     * @param method         the method
//     * @param methodSequence the method sequence
//     * @param className      FQN of the declaring class
//     * @return the name of the join point
//     */
//    private static String getPrefixedMethodName(
//            final CtMethod method,
//            int methodLookupId,
//            final int methodSequence,
//            final String className) {
//        final StringBuffer methodName = new StringBuffer();
//        methodName.append(TransformationUtil.ORIGINAL_METHOD_PREFIX);
//        methodName.append(methodLookupId);
//        methodName.append(TransformationUtil.DELIMITER);
//        methodName.append(method.getName());
//        methodName.append(TransformationUtil.DELIMITER);
//        methodName.append(methodSequence);
//        methodName.append(TransformationUtil.DELIMITER);
//        methodName.append(className.replace('.', '_'));
//        return methodName.toString();
//    }
}

