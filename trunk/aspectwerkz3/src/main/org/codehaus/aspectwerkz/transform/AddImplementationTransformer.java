/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import org.codehaus.aspectwerkz.definition.IntroductionDefinition;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.definition.SystemDefinitionContainer;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.expression.PointcutType;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.reflect.impl.javassist.JavassistClassInfo;
import java.util.Iterator;
import java.util.List;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;

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
     * @param klass   the class
     */
    public void transform(final Context context, final Klass klass) throws NotFoundException {
        List definitions = context.getDefinitions();

        // loop over all the definitions
        for (Iterator it = definitions.iterator(); it.hasNext();) {
            SystemDefinition definition = (SystemDefinition)it.next();
            final CtClass ctClass = klass.getCtClass();
            ClassInfo classInfo = new JavassistClassInfo(ctClass, context.getLoader());
            ExpressionContext ctx = new ExpressionContext(PointcutType.ANY, classInfo, classInfo);
            if (classFilter(ctClass, ctx, definition)) {
                continue;
            }
            addMethodIntroductions(definition, context, ctx, ctClass, this);
        }
    }

    /**
     * Adds introductions to the class.
     *
     * @param definition  the definition
     * @param context     the transformation context
     * @param ctx         the context
     * @param ctClass     the class gen
     * @param transformer the transformer
     */
    private void addMethodIntroductions(final SystemDefinition definition, final Context context,
                                        final ExpressionContext ctx, final CtClass ctClass,
                                        final AddImplementationTransformer transformer) {
        List introductionDefs = definition.getIntroductionDefinitions(ctx);
        boolean isClassAdvised = false;
        for (Iterator it = introductionDefs.iterator(); it.hasNext();) {
            IntroductionDefinition introDef = (IntroductionDefinition)it.next();
            int methodIndex = 0;
            List methodsToIntroduce = introDef.getMethodsToIntroduce();
            for (Iterator mit = methodsToIntroduce.iterator(); mit.hasNext(); methodIndex++) {
                MethodInfo methodToIntroduce = (MethodInfo)mit.next();
                transformer.createProxyMethod(ctClass, methodToIntroduce,
                                              definition.getMixinIndexByName(introDef.getName()), methodIndex,
                                              definition, context);
                isClassAdvised = true;
            }
        }
        if (isClassAdvised) {
            context.markAsAdvised();
        }
    }

    /**
     * Creates a proxy method for the introduces method.
     *
     * @param ctClass     the class gen
     * @param methodInfo  the info for the method
     * @param mixinIndex  the mixin index
     * @param methodIndex the method index
     * @param definition  the definition
     * @param context     the context
     */
    private void createProxyMethod(final CtClass ctClass, final MethodInfo methodInfo, final int mixinIndex,
                                   final int methodIndex, final SystemDefinition definition, final Context context) {
        try {
            String methodName = methodInfo.getName();
            ClassInfo[] parameters = methodInfo.getParameterTypes();
            ClassInfo returnType = methodInfo.getReturnType();
            ClassInfo[] exceptionTypes = methodInfo.getExceptionTypes();
            final String[] parameterNames = new String[parameters.length];
            final CtClass[] bcelParameterTypes = new CtClass[parameters.length];
            final CtClass[] bcelExceptionTypes = new CtClass[exceptionTypes.length];
            final CtClass javassistReturnType = ctClass.getClassPool().get(returnType.getName());
            if (javassistReturnType == null) {
                return; // we have a constructor => skip
            }
            for (int i = 0; i < parameters.length; i++) {
                bcelParameterTypes[i] = ctClass.getClassPool().get(parameters[i].getName());
                parameterNames[i] = "arg" + i;
            }
            for (int i = 0; i < exceptionTypes.length; i++) {
                bcelExceptionTypes[i] = ctClass.getClassPool().get(exceptionTypes[i].getName());
            }
            if (TransformationUtil.isMethodStatic(methodInfo)) {
                return; // introductions can't be static (not for the moment at least)
            }
            if (JavassistHelper.hasMethod(ctClass, methodName, bcelParameterTypes)) {
                return;
            }
            JavassistHelper.addStaticClassField(ctClass, context);
            JavassistHelper.addAspectManagerField(ctClass, definition, context);
            StringBuffer body = new StringBuffer("{");
            if (parameters.length > 0) {
                body.append("Object[] aobj = $args;");
            }
            body.append("return ($r)");
            body.append(TransformationUtil.ASPECT_MANAGER_FIELD);
            body.append(".").append(TransformationUtil.GET_MIXIN_METHOD);
            body.append("(").append(mixinIndex).append(")");
            body.append(".").append(TransformationUtil.INVOKE_MIXIN_METHOD);
            body.append("(").append(methodIndex).append(",");
            if (parameters.length > 0) {
                body.append("aobj").append(",");
            }
            body.append("this").append(");");
            body.append("}");
            CtMethod method = CtNewMethod.make(javassistReturnType, methodName, bcelParameterTypes, bcelExceptionTypes,
                                               body.toString(), ctClass);
            method.setModifiers(Modifier.PUBLIC);
            ctClass.addMethod(method);
        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Filters the classes to be transformed.
     *
     * @param cg         the class to filter
     * @param ctx        the context
     * @param definition the definition
     * @return boolean true if the method should be filtered away
     */
    private boolean classFilter(final CtClass cg, final ExpressionContext ctx, final SystemDefinition definition) {
        if (cg.isInterface()) {
            return true;
        }
        String className = cg.getName().replace('/', '.');
        if (definition.inExcludePackage(className)) {
            return true;
        }
        if (definition.inExcludePackage(className)) {
            return true;
        }
        if (!definition.inIncludePackage(className)) {
            return true;
        }
        if (definition.isIntroduced(ctx)) {
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
     * Callback method. Prints a log/status message at each transformation.
     *
     * @return a log string
     */
    public String verboseMessage() {
        return this.getClass().getName();
    }
}
