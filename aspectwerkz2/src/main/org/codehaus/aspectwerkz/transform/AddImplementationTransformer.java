/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import java.util.Iterator;

import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.JavassistMetaDataMaker;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.definition.DefinitionLoader;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.Modifier;
import javassist.CtNewMethod;
import javassist.CtMethod;

/**
 * Adds an Introductions to classes.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class AddImplementationTransformer implements Transformer {

    /**
     * Adds introductions to a class.
     *
     * @param context the transformation context
     * @param klass the class
     */
    public void transform(final Context context, final Klass klass) throws NotFoundException {

        // loop over all the definitions
        for (Iterator it = DefinitionLoader.getDefinitions().iterator(); it.hasNext();) {
            SystemDefinition definition = (SystemDefinition)it.next();

            final CtClass cg = klass.getCtClass();
            ClassMetaData classMetaData = JavassistMetaDataMaker.createClassMetaData(cg);
            if (classFilter(cg, classMetaData, definition)) {
                return;
            }

            IntroductionTransformer.addMethodIntroductions(definition, context, classMetaData, cg, this);
        }
    }

    /**
     * Creates a proxy method for the introduces method.
     *
     * @param cg the class gen
     * @param methodMetaData the meta-data for the method
     * @param mixinIndex the mixin index
     * @param methodIndex the method index
     * @param uuid the uuid for the weave model
     */
    public void createProxyMethod(final CtClass cg,
                                  final MethodMetaData methodMetaData,
                                  final int mixinIndex,
                                  final int methodIndex,
                                  final String uuid) {
        try {
            String methodName = methodMetaData.getName();
            String[] parameters = methodMetaData.getParameterTypes();
            String returnType = methodMetaData.getReturnType();
            String[] exceptionTypes = methodMetaData.getExceptionTypes();
            int modifiers = methodMetaData.getModifiers();

            final String[] parameterNames = new String[parameters.length];
            final CtClass[] bcelParameterTypes = new CtClass[parameters.length];
            final CtClass[] bcelExceptionTypes = new CtClass[exceptionTypes.length];
            final CtClass bcelReturnType = cg.getClassPool().get(returnType);
            if (bcelReturnType == null) {
                return; // we have a constructor => skip
            }

            for (int i = 0; i < parameters.length; i++) {
                bcelParameterTypes[i] = cg.getClassPool().get(parameters[i]);
                parameterNames[i] = "arg" + i;
            }

            for (int i = 0; i < exceptionTypes.length; i++) {
                bcelExceptionTypes[i] = cg.getClassPool().get(exceptionTypes[i]);
            }

            if (isMethodStatic(methodMetaData)) {
                return; // introductions can't be static (not for the moment at least)
            }

            StringBuffer body = new StringBuffer("{");
            if (parameters.length > 0) {
                body.append("Object[] aobj = $args;");
            }
            body.append("return ($r)");
            body.append(TransformationUtil.SYSTEM_LOADER_CLASS);
            body.append(".").append(TransformationUtil.RETRIEVE_SYSTEM_METHOD);
            body.append("(\"").append(uuid).append("\")");
            body.append(".").append(TransformationUtil.RETRIEVE_MIXIN_METHOD);
            body.append("(").append(mixinIndex).append(")");
            body.append(".").append(TransformationUtil.INVOKE_MIXIN_METHOD);
            body.append("(").append(methodIndex).append(",");
            if (parameters.length > 0) {
                body.append("aobj").append(",");
            }
            body.append("this").append(");");
            body.append("}");

            CtMethod method = CtNewMethod.make(bcelReturnType,
                    methodName,
                    bcelParameterTypes,
                    bcelExceptionTypes,
                    body.toString(),
                    cg);
            method.setModifiers(Modifier.PUBLIC);
            cg.addMethod(method);
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Checks if a method is static or not.
     *
     * @param methodMetaData the meta-data for the method
     * @return boolean
     */
    private static boolean isMethodStatic(final MethodMetaData methodMetaData) {
        int modifiers = methodMetaData.getModifiers();
        if ((modifiers & Modifier.STATIC) != 0) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Filters the classes to be transformed.
     *
     * @param cg the class to filter
     * @param classMetaData the class meta-data
     * @param definition the definition
     * @return boolean true if the method should be filtered away
     */
    private boolean classFilter(final CtClass cg,
                                final ClassMetaData classMetaData,
                                final SystemDefinition definition) {
        if (cg.isInterface() || TransformationUtil.hasSuperClass(classMetaData, "org.codehaus.aspectwerkz.aspect.Aspect")) {
            return true;
        }
        String className = cg.getName();
        if (definition.inExcludePackage(className)) {
            return true;
        }
        if (definition.inIncludePackage(className) &&
                definition.hasIntroductions(classMetaData)) {
            return false;
        }
        return true;
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
     * Callback method. Prints a log/status message at
     * each transformation.
     *
     * @return a log string
     */
    public String verboseMessage() {
        return this.getClass().getName();
    }
}
