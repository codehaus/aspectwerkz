/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import java.util.List;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

import javassist.CtClass;
import javassist.Modifier;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.NotFoundException;
import javassist.CtField;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import javassist.expr.FieldAccess;

import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.JavassistMetaDataMaker;
import org.codehaus.aspectwerkz.metadata.FieldMetaData;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.definition.DefinitionLoader;
import org.codehaus.aspectwerkz.transform.TransformationUtil;

/**
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class FieldSetGetTransformer implements Transformer {

    /**
     * List with the definitions.
     */
    private List m_definitions;

    /**
     * Creates a new instance of the transformer.
     */
    public FieldSetGetTransformer() {
        m_definitions = DefinitionLoader.getDefinitions();
    }

    /**
     * Transforms the call side pointcuts.
     *
     * @param context the transformation context
     * @param klass the class set.
     */
    public void transform(final Context context, final Klass klass)
            throws NotFoundException, CannotCompileException {

        // loop over all the definitions
        for (Iterator it = m_definitions.iterator(); it.hasNext();) {
            final SystemDefinition definition = (SystemDefinition)it.next();

            final CtClass ctClass = klass.getCtClass();
            final ClassMetaData classMetaData = JavassistMetaDataMaker.createClassMetaData(ctClass);

            // filter caller classes
            if (classFilter(definition, classMetaData, ctClass)) {
                return;
            }

            final Set fieldJoinPoints = new HashSet();

            ctClass.instrument(new ExprEditor() {
                public void edit(FieldAccess fieldAccess) throws CannotCompileException {
                    try {
                        CtBehavior where = null;
                        try {
                            where = fieldAccess.where();
                        }
                        catch (RuntimeException e) {
                            // <clinit> access leads to a bug in Javassist
                            where = ctClass.getClassInitializer();
                        }

                        // filter caller context
                        if (methodFilter(where)) {
                            return;
                        }

                        // get field accessed information
                        final String fieldName = fieldAccess.getFieldName();
                        final String fieldSignature = fieldAccess.getField().getType().getName() + " " + fieldName;
                        FieldMetaData fieldMetaData = JavassistMetaDataMaker.createFieldMetaData(
                                fieldAccess.getField()
                        );

                        addCalleeMethodDeclaringClassField(ctClass, fieldAccess.getField());

                        if ((fieldAccess.isReader() && !getFieldFilter(definition, classMetaData, fieldMetaData)) ||
                                !setFieldFilter(definition, classMetaData, fieldMetaData)) {
/*
      $0 	The object containing the field accessed by the expression. This is not equivalent to this.
      this represents the object that the method including the expression is invoked on.
      $0 is null if the field is static.


      $1 	The value that would be stored in the field if the expression is write access.
      Otherwise, $1 is not available.

      $_ 	The resulting value of the field access if the expression is read access.
      Otherwise, the value stored in $_ is discarded.

      $r 	The type of the field if the expression is read access.
      Otherwise, $r is void.

      $class     	A java.lang.Class object representing the class declaring the field.
      $type 	A java.lang.Class object representing the field type.
      $proceed     	The name of a virtual method executing the original field access. .

The other identifiers such as $w, $args and $$ are also available.

If the expression is read access, a value must be assigned to $_ in the source text.
The type of $_ is the type of the field.

*/
                            StringBuffer body = new StringBuffer();
                            if (fieldAccess.isReader()) {
                                body.append("$_ = ");
                                body.append(TransformationUtil.JOIN_POINT_MANAGER_FIELD);
                                body.append('.');
                                body.append("proceedWithGetJoinPoint");
                            }
                            else {
                                body.append(TransformationUtil.JOIN_POINT_MANAGER_FIELD);
                                body.append('.');
                                body.append("proceedWithSetJoinPoint");
                            }
                            body.append('(');
                            body.append(TransformationUtil.calculateHash(fieldAccess.getField()));
                            body.append(',');
                            if (fieldAccess.isWriter()) {
                                body.append("$args,");
                            }
                            if (Modifier.isStatic(where.getModifiers())) {
                                body.append("(Object)null");
                            }
                            else {
                                body.append("$0");
                            }
                            body.append(",(Class)");
                            body.append(
                                    TransformationUtil.STATIC_CLASS_FIELD +
                                    TransformationUtil.DELIMITER + "field" +
                                    TransformationUtil.DELIMITER +
                                    fieldAccess.getField().getDeclaringClass().getName().replace('.', '_')
                            );
                            body.append(',');
                            if (fieldAccess.isWriter()) {
                                body.append(TransformationUtil.JOIN_POINT_TYPE_FIELD_SET);
                            }
                            else {
                                body.append(TransformationUtil.JOIN_POINT_TYPE_FIELD_GET);
                            }
                            body.append(",\"");
                            body.append(fieldSignature);
                            body.append("\");");

                            System.out.println("body.toString() = " + body.toString());

                            fieldAccess.replace(body.toString());
                            context.markAsAdvised();
                        }
                    }
                    catch (NotFoundException nfe) {
                        nfe.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * Creates a new static class field, for the declaring class of the callee method.
     *
     * @param ctClass the class
     * @param ctField the field
     */
    private void addCalleeMethodDeclaringClassField(final CtClass ctClass, final CtField ctField)
            throws NotFoundException, CannotCompileException {

        String fieldName = TransformationUtil.STATIC_CLASS_FIELD +
                TransformationUtil.DELIMITER + "field" +
                TransformationUtil.DELIMITER +
                ctField.getDeclaringClass().getName().replace('.', '_');

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
            ctClass.addField(field, "java.lang.Class.forName(\"" + ctField.getDeclaringClass().getName() + "\")");
        }
    }

    /**
     * Filters the classes to be transformed.
     *
     * @param definition the definition
     * @param classMetaData the meta-data for the class
     * @param ctClass the class to filter
     * @return boolean true if the method should be filtered away
     */
    private boolean classFilter(final SystemDefinition definition,
                                final ClassMetaData classMetaData,
                                final CtClass ctClass) {
        if (ctClass.isInterface() ||
                TransformationUtil.hasSuperClass(classMetaData, "org.codehaus.aspectwerkz.attribdef.aspect.Aspect")) {
            return true;
        }
        String className = ctClass.getName();
        if (definition.inExcludePackage(className)) {
            return true;
        }
        if (!definition.inIncludePackage(className)) {
            return true;
        }
        if (definition.hasSetPointcut(classMetaData) || definition.hasGetPointcut(classMetaData)) {
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
    private boolean methodFilter(final CtBehavior method) {
        return Modifier.isNative(method.getModifiers())
                || Modifier.isAbstract(method.getModifiers())
                || method.getName().startsWith(TransformationUtil.ASPECTWERKZ_PREFIX);
    }

    /**
     * Filters the PUTFIELD's to be transformed.
     *
     * @param definition the definition
     * @param classMetaData the class to filter
     * @param fieldMetaData the field to filter
     * @return
     */
    private boolean setFieldFilter(final SystemDefinition definition,
                                   final ClassMetaData classMetaData,
                                   final FieldMetaData fieldMetaData) {
        if (fieldMetaData.getName().startsWith(TransformationUtil.ASPECTWERKZ_PREFIX)) {
            return true;
        }
        if (definition.hasSetPointcut(classMetaData, fieldMetaData)) {
            return false;
        }
        return true;
    }

    /**
     * Filters the GETFIELD's to be transformed.
     *
     * @param definition the definition
     * @param classMetaData the class to filter
     * @param fieldMetaData the field to filter
     * @return
     */
    private boolean getFieldFilter(final SystemDefinition definition,
                                   final ClassMetaData classMetaData,
                                   final FieldMetaData fieldMetaData) {
        if (fieldMetaData.getName().startsWith(TransformationUtil.ASPECTWERKZ_PREFIX)) {
            return true;
        }
        if (definition.hasGetPointcut(classMetaData, fieldMetaData)) {
            return false;
        }
        return true;
    }
}
