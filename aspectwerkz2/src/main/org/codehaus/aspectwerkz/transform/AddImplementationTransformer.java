/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.definition.SystemDefinitionContainer;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;

import java.util.Iterator;
import java.util.List;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;

/**
 * Adds an Introductions to classes.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class AddImplementationTransformer implements Transformer
{
    public AddImplementationTransformer()
    {
    }

    /**
     * Adds introductions to a class.
     *
     * @param context the transformation context
     * @param klass   the class
     */
    public void transform(final Context context, final Klass klass)
        throws NotFoundException
    {
        List definitions = SystemDefinitionContainer.getDefinitionsContext();

        // loop over all the definitions
        for (Iterator it = definitions.iterator(); it.hasNext();)
        {
            SystemDefinition definition = (SystemDefinition) it.next();

            final CtClass ctClass = klass.getCtClass();
            ClassMetaData classMetaData = context.getMetaDataMaker()
                                                 .createClassMetaData(ctClass);

            if (classFilter(ctClass, classMetaData, definition))
            {
                continue;
            }

            IntroductionTransformer.addMethodIntroductions(definition, context,
                classMetaData, ctClass, this);
        }
    }

    /**
     * Creates a proxy method for the introduces method.
     *
     * @param ctClass        the class gen
     * @param methodMetaData the meta-data for the method
     * @param mixinIndex     the mixin index
     * @param methodIndex    the method index
     * @param definition     the definition
     */
    public void createProxyMethod(final CtClass ctClass,
        final MethodMetaData methodMetaData, final int mixinIndex,
        final int methodIndex, final SystemDefinition definition)
    {
        try
        {
            String methodName = methodMetaData.getName();
            String[] parameters = methodMetaData.getParameterTypes();
            String returnType = methodMetaData.getReturnType();
            String[] exceptionTypes = methodMetaData.getExceptionTypes();

            final String[] parameterNames = new String[parameters.length];
            final CtClass[] bcelParameterTypes = new CtClass[parameters.length];
            final CtClass[] bcelExceptionTypes = new CtClass[exceptionTypes.length];
            final CtClass javassistReturnType = ctClass.getClassPool().get(returnType);

            if (javassistReturnType == null)
            {
                return; // we have a constructor => skip
            }

            for (int i = 0; i < parameters.length; i++)
            {
                bcelParameterTypes[i] = ctClass.getClassPool().get(parameters[i]);
                parameterNames[i] = "arg" + i;
            }

            for (int i = 0; i < exceptionTypes.length; i++)
            {
                bcelExceptionTypes[i] = ctClass.getClassPool().get(exceptionTypes[i]);
            }

            if (isMethodStatic(methodMetaData))
            {
                return; // introductions can't be static (not for the moment at least)
            }

            if (JavassistHelper.hasMethod(ctClass, methodName,
                    bcelParameterTypes))
            {
                return;
            }

            addAspectManagerField(ctClass, definition);

            StringBuffer body = new StringBuffer("{");

            if (parameters.length > 0)
            {
                body.append("Object[] aobj = $args;");
            }

            body.append("return ($r)");
            body.append(TransformationUtil.ASPECT_MANAGER_FIELD);
            body.append(".").append(TransformationUtil.GET_MIXIN_METHOD);
            body.append("(").append(mixinIndex).append(")");
            body.append(".").append(TransformationUtil.INVOKE_MIXIN_METHOD);
            body.append("(").append(methodIndex).append(",");

            if (parameters.length > 0)
            {
                body.append("aobj").append(",");
            }

            body.append("this").append(");");
            body.append("}");

            CtMethod method = CtNewMethod.make(javassistReturnType, methodName,
                    bcelParameterTypes, bcelExceptionTypes, body.toString(),
                    ctClass);

            method.setModifiers(Modifier.PUBLIC);
            ctClass.addMethod(method);
        }
        catch (Exception e)
        {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Adds a new <code>JoinPointManager</code> field to the advised class.
     *
     * @param ctClass
     * @param definition
     */
    private void addAspectManagerField(final CtClass ctClass,
        final SystemDefinition definition)
        throws NotFoundException, CannotCompileException
    {
        if (!JavassistHelper.hasField(ctClass,
                TransformationUtil.ASPECT_MANAGER_FIELD))
        {
            CtField field = new CtField(ctClass.getClassPool().get(TransformationUtil.ASPECT_MANAGER_CLASS),
                    TransformationUtil.ASPECT_MANAGER_FIELD, ctClass);

            field.setModifiers(Modifier.STATIC | Modifier.PRIVATE
                | Modifier.FINAL);

            StringBuffer body = new StringBuffer();

            body.append(TransformationUtil.SYSTEM_LOADER_CLASS);
            body.append("#getSystem(");
            body.append(TransformationUtil.STATIC_CLASS_FIELD);
            body.append('.');
            body.append("getClassLoader())");
            body.append('.');
            body.append(TransformationUtil.GET_ASPECT_MANAGER_METHOD);
            body.append("(\"");
            body.append(definition.getUuid());
            body.append("\");");

            //TODO  AVAOPC
            /*
            what about having several field to access the AspectManager
            whose system is introducing methods ?
            should we have a simpler TF model
            and hardcode the AspectManager index ??
            [problem for undeploy of a system]
            */
            ctClass.addField(field, body.toString());
        }
    }

    /**
     * Checks if a method is static or not.
     *
     * @param methodMetaData the meta-data for the method
     * @return boolean
     */
    private static boolean isMethodStatic(final MethodMetaData methodMetaData)
    {
        int modifiers = methodMetaData.getModifiers();

        if ((modifiers & Modifier.STATIC) != 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Filters the classes to be transformed.
     *
     * @param cg            the class to filter
     * @param classMetaData the class meta-data
     * @param definition    the definition
     * @return boolean true if the method should be filtered away
     */
    private boolean classFilter(final CtClass cg,
        final ClassMetaData classMetaData, final SystemDefinition definition)
    {
        if (cg.isInterface())
        {
            return true;
        }

        String className = cg.getName().replace('/', '.');

        if (definition.inExcludePackage(className))
        {
            return true;
        }

        if (definition.inIncludePackage(className)
            && definition.hasIntroductions(classMetaData))
        {
            return false;
        }

        return true;
    }

    /**
     * Callback method. Is being called before each transformation.
     */
    public void sessionStart()
    {
    }

    /**
     * Callback method. Is being called after each transformation.
     */
    public void sessionEnd()
    {
    }

    /**
     * Callback method. Prints a log/status message at each transformation.
     *
     * @return a log string
     */
    public String verboseMessage()
    {
        return this.getClass().getName();
    }
}
