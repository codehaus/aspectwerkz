/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import java.util.Iterator;
import java.util.List;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.NotFoundException;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.bytecode.CodeAttribute;
import org.codehaus.aspectwerkz.definition.DefinitionLoader;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.metadata.ClassMetaDataImpl;
import org.codehaus.aspectwerkz.metadata.ConstructorMetaDataImpl;
import org.codehaus.aspectwerkz.metadata.JavassistMetaDataMaker;

/**
 * Advises constructor EXECUTION join points.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class ConstructorExecutionTransformer implements Transformer {

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
    public ConstructorExecutionTransformer() {
        m_definitions = DefinitionLoader.getDefinitions();
    }

    /**
     * Makes the member method transformations.
     *
     * @param context the transformation context
     * @param klass   the class set.
     */
    public void transform(final Context context, final Klass klass) throws Exception {
        m_joinPointIndex = TransformationUtil.getJoinPointIndex(klass.getCtClass());
        for (Iterator it = m_definitions.iterator(); it.hasNext();) {
            SystemDefinition definition = (SystemDefinition)it.next();

            final CtClass ctClass = klass.getCtClass();
            ClassMetaDataImpl classMetaData = JavassistMetaDataMaker.createClassMetaData(ctClass);
            if (classFilter(definition, classMetaData, ctClass, false)) {
                return;
            }

            final CtConstructor[] constructors = ctClass.getConstructors();
            for (int i = 0; i < constructors.length; i++) {
                ConstructorMetaDataImpl constructorMetaData = JavassistMetaDataMaker.createConstructorMetaData(
                        constructors[i]
                );
                CtConstructor constructor = constructors[i];
                if (constructorFilter(definition, classMetaData, constructorMetaData)) {
                    continue;
                }
                context.markAsAdvised();

//                createStaticMethodWithConstructorBody(ctClass, constructor, i);

                addPrefixToConstructor(ctClass, constructor);
                int constructorHash = TransformationUtil.calculateHash(constructor);
                createWrapperConstructor(constructor, constructorHash);
            }
        }
        TransformationUtil.setJoinPointIndex(klass.getCtClass(), m_joinPointIndex);
    }

    /**
     * Creates a wrapper constructor for the original constructor specified. This constructor has the same signature as
     * the original constructor and catches the invocation for further processing by the framework before redirecting to
     * the original constructor.
     *
     * @param originalConstructor the original constructor
     * @param constructorHash     the constructor hash
     */
    private void createWrapperConstructor(final CtConstructor originalConstructor, final int constructorHash)
            throws CannotCompileException, NotFoundException {

        StringBuffer body = new StringBuffer();
        body.append('{');
        if (originalConstructor.getParameterTypes().length > 0) {
            body.append("Object[] args = $args; ");
        }
        else {
            body.append("Object[] args = null; ");
        }
        body.append("Object nullObject = null;");

        body.append("return ($r)");
        body.append(TransformationUtil.JOIN_POINT_MANAGER_FIELD);
        body.append('.');
        body.append(TransformationUtil.PROCEED_WITH_EXECUTION_JOIN_POINT_METHOD);
        body.append('(');
        body.append(constructorHash);
        body.append(',');
        body.append(m_joinPointIndex);
        body.append(',');
        body.append("args, this,");
        body.append(TransformationUtil.JOIN_POINT_TYPE_CONSTRUCTOR_EXECUTION);
        body.append("); }");

        m_joinPointIndex++;

        originalConstructor.setBody(body.toString());
    }

    /**
     *
     * @param ctClass     the class
     * @param constructor the current method
     * @return the new prefixed constructor
     */
//    private void createStaticMethodWithConstructorBody(
//            final CtClass ctClass,
//            final CtConstructor constructor,
//            final int methodSequence) throws NotFoundException, CannotCompileException {
//
//        String prefixedMethodName = TransformationUtil.getPrefixedMethodName(
//                constructor.getName(), methodSequence, ctClass.getName()
//        );
//
//        CtMethod method = CtNewMethod.make(
//                CtClass.voidType, prefixedMethodName, constructor.getParameterTypes(),
//                constructor.getExceptionTypes(), null, ctClass
//        );
//        method.setModifiers(Modifier.STATIC | Modifier.FINAL);
//
//        CodeAttribute codeAttribute = constructor.getMethodInfo().getCodeAttribute();
//        method.getMethodInfo().setCodeAttribute(codeAttribute);
//
////        CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
////        codeAttribute.setMaxLocals(codeAttribute.getMaxLocals() + 1);
//
//        ctClass.addMethod(method);
//    }

    /**
     * Adds a prefix to the original constructor. To make it callable only from within the framework itself.
     *
     * @param ctClass     the class
     * @param constructor the current method
     * @return the new prefixed constructor
     */
    private void addPrefixToConstructor(final CtClass ctClass, final CtConstructor constructor)
            throws NotFoundException, CannotCompileException {

        int accessFlags = constructor.getModifiers();

        CtClass[] parameterTypes = constructor.getParameterTypes();
        CtClass[] newParameterTypes = new CtClass[parameterTypes.length + 1];
        for (int i = 0; i < parameterTypes.length; i++) {
            newParameterTypes[i] = parameterTypes[i];
        }
        newParameterTypes[parameterTypes.length] =
        ClassPool.getDefault().get(TransformationUtil.JOIN_POINT_MANAGER_CLASS);
        CtConstructor newConstructor = CtNewConstructor.make(
                newParameterTypes,
                constructor.getExceptionTypes(),
                CtNewConstructor.PASS_NONE,
                null,
                CtMethod.ConstParameter.string(constructor.getSignature()),
                ctClass
        );
        newConstructor.setBody(constructor, null);
        newConstructor.setModifiers(accessFlags);
        CodeAttribute codeAttribute = newConstructor.getMethodInfo().getCodeAttribute();
        codeAttribute.setMaxLocals(codeAttribute.getMaxLocals() + 1);

        ctClass.addConstructor(newConstructor);
    }

    /**
     * Filters the classes to be transformed.
     *
     * @param definition    the definition
     * @param classMetaData the meta-data for the class
     * @param ctClass       the class to filter
     * @return boolean true if the method should be filtered away
     */
    private boolean classFilter(
            final SystemDefinition definition,
            final ClassMetaDataImpl classMetaData,
            final CtClass ctClass,
            final boolean isActivatePhase) {
        if (ctClass.isInterface() ||
            TransformationUtil.hasSuperClass(classMetaData, "org.codehaus.aspectwerkz.aspect.Aspect")) {
            return true;
        }
        String className = ctClass.getName();
        if (definition.inExcludePackage(className)) {
            return true;
        }
        if (!definition.inIncludePackage(className)) {
            return true;
        }
//        if (definition.inPreparePackage(className) && !isActivatePhase) {
//            return true; //TODO remove
//        }
        if (definition.hasExecutionPointcut(classMetaData)) {
            return false;
        }
        return true;
    }

    /**
     * Filters the methods to be transformed.
     *
     * @param definition          the definition
     * @param classMetaData       the class meta-data
     * @param constructorMetaData the constructor metadata
     * @return boolean
     */
    private boolean constructorFilter(
            final SystemDefinition definition,
            final ClassMetaDataImpl classMetaData,
            final ConstructorMetaDataImpl constructorMetaData) {
        if (definition.hasExecutionPointcut(classMetaData, constructorMetaData)) {
            return false;
        }
        else {
            return true;
        }
    }
}
