/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import java.util.Iterator;
import java.util.List;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.NewExpr;
import org.codehaus.aspectwerkz.definition.DefinitionLoader;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.ConstructorMetaData;
import org.codehaus.aspectwerkz.metadata.JavassistMetaDataMaker;

/**
 * Advises constructor CALL join points.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class ConstructorCallTransformer implements Transformer {

    /**
     * List with the definitions.
     */
    private List m_definitions;

    /**
     * Creates a new instance of the transformer.
     */
    public ConstructorCallTransformer() {
        m_definitions = DefinitionLoader.getDefinitions();
    }

    /**
     * Transforms the call side pointcuts.
     *
     * @param context the transformation context
     * @param klass   the class set.
     */
    public void transform(final Context context, final Klass klass) throws NotFoundException, CannotCompileException {
        for (Iterator it = m_definitions.iterator(); it.hasNext();) {
            final SystemDefinition definition = (SystemDefinition)it.next();

            final CtClass ctClass = klass.getCtClass();
            ClassMetaData classMetaData = JavassistMetaDataMaker.createClassMetaData(ctClass);
            if (classFilter(definition, classMetaData, ctClass)) {
                return;
            }

            ctClass.instrument(
                    new ExprEditor() {
                        public void edit(NewExpr newExpr) throws CannotCompileException {
                            try {
                                CtBehavior where = null;
                                try {
                                    where = newExpr.where();
                                }
                                catch (RuntimeException e) {
                                    // <clinit> access leads to a bug in Javassist
                                    where = ctClass.getClassInitializer();
                                }

                                // filter caller methods
                                if (methodFilterCaller(where)) {
                                    return;
                                }

                                CtConstructor ctConstructor = newExpr.getConstructor();
                                String calleeClassName = newExpr.getClassName();

                                // filter callee classes
                                if (!definition.inIncludePackage(calleeClassName)) {
                                    return;
                                }
                                // filter the constructors
                                if (constructorFilter(ctConstructor)) {
                                    return;
                                }

                                // create the class meta-data
                                ClassMetaData calleeSideClassMetaData;
                                try {
                                    calleeSideClassMetaData =
                                    JavassistMetaDataMaker.createClassMetaData(
                                            context.getClassPool().get(calleeClassName)
                                    );
                                }
                                catch (NotFoundException e) {
                                    throw new WrappedRuntimeException(e);
                                }

                                // create the method meta-data
                                ConstructorMetaData constructorMetaData =
                                        JavassistMetaDataMaker.createConstructorMetaData(newExpr.getConstructor());

                                // is this a caller side method pointcut?
                                if (definition.isPickedOutByCallPointcut(calleeSideClassMetaData, constructorMetaData)) {

                                    // check the callee class is not the same as target class, if that is the case
                                    // then we have have class loaded and set in the ___AW_clazz already
                                    String declaringClassMethodName = TransformationUtil.STATIC_CLASS_FIELD;

                                    CtClass declaringClass = ctConstructor.getDeclaringClass();
                                    if (!declaringClass.getName().equals(where.getDeclaringClass().getName())) {
                                        declaringClassMethodName =
                                        addCalleeMethodDeclaringClassField(ctClass, ctConstructor);
                                    }

                                    // call the wrapper method instead of the callee method
                                    StringBuffer body = new StringBuffer();
                                    body.append("{$_ = ($r)");
                                    body.append(TransformationUtil.JOIN_POINT_MANAGER_FIELD);
                                    body.append('.');
                                    body.append(TransformationUtil.PROCEED_WITH_CALL_JOIN_POINT_METHOD);
                                    body.append('(');
                                    body.append(TransformationUtil.calculateHash(ctConstructor));
                                    if (Modifier.isStatic(where.getModifiers())) {
                                        body.append(", $args, (Object)null, (Class)");
                                    }
                                    else {
                                        body.append(", $args, this, (Class)");
                                    }
                                    body.append(declaringClassMethodName);
                                    body.append(',');
                                    body.append(TransformationUtil.JOIN_POINT_TYPE_CONSTRUCTOR_CALL);
                                    body.append(",\"");
                                    body.append(ctConstructor.getSignature());
                                    body.append("\"); }");

                                    newExpr.replace(body.toString());
                                    context.markAsAdvised();
                                }
                            }
                            catch (NotFoundException nfe) {
                                nfe.printStackTrace();
                                // TODO: should we swallow this exception?
                            }
                        }
                    }
            );
        }
    }

    /**
     * Creates a new static class field, for the declaring class of the constructor.
     *
     * @param ctClass       the class
     * @param ctConstructor the constructor
     * @return the name of the field
     */
    private String addCalleeMethodDeclaringClassField(final CtClass ctClass, final CtConstructor ctConstructor)
            throws NotFoundException, CannotCompileException {

        String fieldName = TransformationUtil.STATIC_CLASS_FIELD +
                           TransformationUtil.DELIMITER + "init" +
                           TransformationUtil.DELIMITER +
                           ctConstructor.getDeclaringClass().getName().replace('.', '_');

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
            CtField field = new CtField(
                    ctClass.getClassPool().get("java.lang.Class"),
                    fieldName,
                    ctClass
            );
            field.setModifiers(Modifier.STATIC | Modifier.PRIVATE | Modifier.FINAL);
            ctClass.addField(
                    field, "java.lang.Class.forName(\"" + ctConstructor.getDeclaringClass().getName() + "\")"
            );
        }
        return fieldName;
    }

    /**
     * Filters the classes to be transformed.
     *
     * @param definition
     * @param classMetaData the meta-data for the class
     * @param cg            the class to filter
     * @return boolean true if the method should be filtered away
     */
    private boolean classFilter(
            final SystemDefinition definition,
            final ClassMetaData classMetaData,
            final CtClass cg) {
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
        if (definition.hasCallPointcut(classMetaData)) {
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
     * Filters the constructor.
     *
     * @param constructor the name of method to filter
     * @return boolean true if the method should be filtered away
     * @TODO: create metadata instance and check with the system
     */
    private boolean constructorFilter(final CtConstructor constructor) {
        if (constructor.equals("<clinit>") ||
            constructor.getName().startsWith(TransformationUtil.ORIGINAL_METHOD_PREFIX) ||
            constructor.getName().equals(TransformationUtil.GET_META_DATA_METHOD) ||
            constructor.getName().equals(TransformationUtil.SET_META_DATA_METHOD) ||
            constructor.getName().equals(TransformationUtil.CLASS_LOOKUP_METHOD) ||
            constructor.getName().equals(TransformationUtil.GET_UUID_METHOD)) {
            return true;
        }
        else {
            return false;
        }
    }
}
