/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.definition.SystemDefinitionContainer;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.FieldMetaData;
import org.codehaus.aspectwerkz.metadata.JavassistMetaDataMaker;

import java.util.Iterator;
import java.util.List;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtField;
import javassist.Modifier;
import javassist.NotFoundException;

import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;

/**
 * Advises SET and GET join points.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class FieldSetGetTransformer implements Transformer
{
    /**
     * The join point index.
     */
    private int m_joinPointIndex;

    /**
     * Creates a new instance of the transformer.
     */
    public FieldSetGetTransformer()
    {
    }

    /**
     * Transforms the call side pointcuts.
     *
     * @param context the transformation context
     * @param klass   the class set.
     */
    public void transform(final Context context, final Klass klass)
        throws NotFoundException, CannotCompileException
    {
        List definitions = SystemDefinitionContainer.getDefinitionsContext();

        m_joinPointIndex = TransformationUtil.getJoinPointIndex(klass
                .getCtClass()); //TODO thread safe and reentrant

        // loop over all the definitions
        for (Iterator it = definitions.iterator(); it.hasNext();)
        {
            final SystemDefinition definition = (SystemDefinition) it.next();

            final CtClass ctClass = klass.getCtClass();
            final ClassMetaData classMetaData = context.getMetaDataMaker()
                                                       .createClassMetaData(ctClass);

            // filter caller classes
            if (classFilter(definition, classMetaData, ctClass))
            {
                continue;
            }

            ctClass.instrument(new ExprEditor()
                {
                    public void edit(FieldAccess fieldAccess)
                        throws CannotCompileException
                    {
                        try
                        {
                            CtBehavior where = null;

                            try
                            {
                                where = fieldAccess.where();
                            }
                            catch (RuntimeException e)
                            {
                                // <clinit> access leads to a bug in Javassist
                                where = ctClass.getClassInitializer();
                            }

                            // filter caller context
                            if (methodFilter(where))
                            {
                                return;
                            }

                            // get field accessed information
                            final String fieldName = fieldAccess.getFieldName();
                            final String fieldSignature = fieldAccess.getField()
                                                                     .getType()
                                                                     .getName()
                                                                     .replace('/',
                                    '.') + ' ' + fieldName;
                            FieldMetaData fieldMetaData = JavassistMetaDataMaker
                                .createFieldMetaData(fieldAccess.getField());

                            // handle GET
                            if (fieldAccess.isReader()
                                && !getFieldFilter(definition, classMetaData,
                                    fieldMetaData))
                            {
                                // check the declaring class for the field is not the same as target class,
                                // if that is the case then we have have class loaded and set in the ___AW_clazz already
                                String declaringClassFieldName = TransformationUtil.STATIC_CLASS_FIELD;
                                CtClass declaringClass = fieldAccess.getField()
                                                                    .getDeclaringClass();

                                if (!declaringClass.getName().replace('/', '.')
                                                   .equals(where.getDeclaringClass()
                                                                .getName()
                                                                .replace('/',
                                            '.')))
                                {
                                    declaringClassFieldName = addFieldAccessDeclaringClassField(declaringClass,
                                            fieldAccess.getField());
                                }

                                //TODO ALEX might need to review since SET is not handled gracefully that way
                                StringBuffer body = new StringBuffer();
                                StringBuffer callBody = new StringBuffer();

                                callBody.append(TransformationUtil.JOIN_POINT_MANAGER_FIELD);
                                callBody.append('.');
                                callBody.append(TransformationUtil.PROCEED_WITH_GET_JOIN_POINT_METHOD);
                                callBody.append('(');
                                callBody.append(TransformationUtil
                                    .calculateHash(fieldAccess.getField()));
                                callBody.append(',');
                                callBody.append(m_joinPointIndex);

                                if (Modifier.isStatic(
                                        fieldAccess.getField().getModifiers()))
                                {
                                    callBody.append(", (Object)null, ");
                                }
                                else
                                {
                                    callBody.append(", $0, ");
                                }

                                callBody.append(declaringClassFieldName);
                                callBody.append(",\"");
                                callBody.append(fieldSignature);
                                callBody.append("\");");

                                // handles advice returns null and fiel is primitive type
                                if (!fieldAccess.getField().getType()
                                                .isPrimitive())
                                {
                                    body.append("$_ = ($r)");
                                    body.append(callBody.toString());
                                }
                                else
                                {
                                    String localResult = TransformationUtil.ASPECTWERKZ_PREFIX
                                        + "res";

                                    body.append("{ Object ").append(localResult)
                                        .append(" = ");
                                    body.append(callBody.toString());
                                    body.append("if (").append(localResult)
                                        .append(" != null)");
                                    body.append("$_ = ($r) ").append(localResult)
                                        .append("; else ");
                                    body.append("$_ = ");
                                    body.append(JavassistHelper
                                        .getDefaultPrimitiveValue(
                                            fieldAccess.getField().getType()));
                                    body.append("; }");
                                }

                                fieldAccess.replace(body.toString());
                                context.markAsAdvised();

                                m_joinPointIndex++;
                            }

                            // handle SET
                            if (fieldAccess.isWriter()
                                && !setFieldFilter(definition, classMetaData,
                                    fieldMetaData))
                            {
                                // check the declaring class for the field is not the same as target class,
                                // if that is the case then we have have class loaded and set in the ___AW_clazz already
                                String declaringClassFieldName = TransformationUtil.STATIC_CLASS_FIELD;
                                CtClass declaringClass = fieldAccess.getField()
                                                                    .getDeclaringClass();

                                if (!declaringClass.getName().replace('/', '.')
                                                   .equals(where.getDeclaringClass()
                                                                .getName()
                                                                .replace('/',
                                            '.')))
                                {
                                    declaringClassFieldName = addFieldAccessDeclaringClassField(declaringClass,
                                            fieldAccess.getField());
                                }

                                //TODO ALEX think about null advice
                                StringBuffer body = new StringBuffer();

                                body.append(TransformationUtil.JOIN_POINT_MANAGER_FIELD);
                                body.append('.');
                                body.append(TransformationUtil.PROCEED_WITH_SET_JOIN_POINT_METHOD);
                                body.append('(');
                                body.append(TransformationUtil.calculateHash(
                                        fieldAccess.getField()));
                                body.append(',');
                                body.append(m_joinPointIndex);

                                if (Modifier.isStatic(
                                        fieldAccess.getField().getModifiers()))
                                {
                                    body.append(", $args, (Object)null, ");
                                }
                                else
                                {
                                    body.append(", $args, $0, ");
                                }

                                body.append(declaringClassFieldName);
                                body.append(",\"");
                                body.append(fieldSignature);
                                body.append("\");");

                                fieldAccess.replace(body.toString());
                                context.markAsAdvised();

                                m_joinPointIndex++;
                            }
                        }
                        catch (NotFoundException nfe)
                        {
                            nfe.printStackTrace();
                        }
                    }
                });
        }

        TransformationUtil.setJoinPointIndex(klass.getCtClass(),
            m_joinPointIndex);
    }

    /**
     * Creates a new static class field, for the declaring class of the field that is accessed/modified.
     *
     * @param ctClass the class
     * @param ctField the field
     * @return the name of the field
     */
    private String addFieldAccessDeclaringClassField(final CtClass ctClass,
        final CtField ctField)
        throws NotFoundException, CannotCompileException
    {
        String fieldName = TransformationUtil.STATIC_CLASS_FIELD
            + TransformationUtil.DELIMITER + "field"
            + TransformationUtil.DELIMITER
            + ctField.getDeclaringClass().getName().replace('.', '_');

        boolean hasField = false;
        CtField[] fields = ctClass.getDeclaredFields();

        for (int i = 0; i < fields.length; i++)
        {
            CtField field = fields[i];

            if (field.getName().equals(fieldName))
            {
                hasField = true;

                break;
            }
        }

        if (!hasField)
        {
            CtField field = new CtField(ctClass.getClassPool().get("java.lang.Class"),
                    fieldName, ctClass);

            field.setModifiers(Modifier.STATIC | Modifier.PRIVATE
                | Modifier.FINAL);
            ctClass.addField(field,
                "java.lang.Class#forName(\""
                + ctField.getDeclaringClass().getName().replace('/', '.')
                + "\")");
        }

        return fieldName;
    }

    /**
     * Filters the classes to be transformed.
     *
     * @param definition    the definition
     * @param classMetaData the meta-data for the class
     * @param ctClass       the class to filter
     * @return boolean true if the method should be filtered away
     */
    private boolean classFilter(final SystemDefinition definition,
        final ClassMetaData classMetaData, final CtClass ctClass)
    {
        if (ctClass.isInterface())
        {
            return true;
        }

        String className = ctClass.getName().replace('/', '.');

        if (definition.inExcludePackage(className))
        {
            return true;
        }

        if (!definition.inIncludePackage(className))
        {
            return true;
        }

        if (definition.hasSetPointcut(classMetaData)
            || definition.hasGetPointcut(classMetaData))
        {
            return false;
        }

        return true;
    }

    /**
     * Filters the methods.
     *
     * @param method the method to filter
     * @return boolean true if the method should be filtered away
     */
    private boolean methodFilter(final CtBehavior method)
    {
        return Modifier.isNative(method.getModifiers())
        || Modifier.isAbstract(method.getModifiers())
        || method.getName().startsWith(TransformationUtil.ASPECTWERKZ_PREFIX);
    }

    /**
     * Filters the PUTFIELD's to be transformed.
     *
     * @param definition    the definition
     * @param classMetaData the class to filter
     * @param fieldMetaData the field to filter
     * @return
     */
    private boolean setFieldFilter(final SystemDefinition definition,
        final ClassMetaData classMetaData, final FieldMetaData fieldMetaData)
    {
        if (fieldMetaData.getName().startsWith(TransformationUtil.ASPECTWERKZ_PREFIX))
        {
            return true;
        }

        if (Modifier.isFinal(fieldMetaData.getModifiers()))
        {
            return true;
        }

        if (definition.hasSetPointcut(classMetaData, fieldMetaData))
        {
            return false;
        }

        return true;
    }

    /**
     * Filters the GETFIELD's to be transformed.
     *
     * @param definition    the definition
     * @param classMetaData the class to filter
     * @param fieldMetaData the field to filter
     * @return
     */
    private boolean getFieldFilter(final SystemDefinition definition,
        final ClassMetaData classMetaData, final FieldMetaData fieldMetaData)
    {
        if (fieldMetaData.getName().startsWith(TransformationUtil.ASPECTWERKZ_PREFIX))
        {
            return true;
        }

        if (definition.hasGetPointcut(classMetaData, fieldMetaData))
        {
            return false;
        }

        return true;
    }
}
