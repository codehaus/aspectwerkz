/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import java.util.Iterator;
import java.util.List;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.Modifier;
import javassist.NotFoundException;

import org.codehaus.aspectwerkz.definition.DefinitionLoader;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.JavassistMetaDataMaker;
import org.codehaus.aspectwerkz.metadata.MetaDataInspector;

/**
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class PrepareAdvisedClassTransformer implements Transformer {

    /**
     * List with the definitions.
     */
    private List m_definitions;

    /**
     * Creates a new instance of the transformer.
     */
    public PrepareAdvisedClassTransformer() {
        m_definitions = DefinitionLoader.getDefinitions();
    }

    /**
     * Makes the member method transformations.
     *
     * @param context the transformation context
     * @param klass   the class set.
     */
    public void transform(final Context context, final Klass klass) throws Exception {
        for (Iterator it = m_definitions.iterator(); it.hasNext();) {
            SystemDefinition definition = (SystemDefinition)it.next();

            final CtClass ctClass = klass.getCtClass();
            ClassMetaData classMetaData = JavassistMetaDataMaker.createClassMetaData(ctClass);

            if (classFilter(definition, classMetaData, ctClass)) {
                return;
            }

            if (!MetaDataInspector.hasField(classMetaData, TransformationUtil.STATIC_CLASS_FIELD)) {
                addStaticClassField(ctClass);
                context.markAsAdvised();
            }
            if (!MetaDataInspector.hasField(classMetaData, TransformationUtil.JOIN_POINT_MANAGER_FIELD)) {
                addJoinPointManagerField(ctClass, definition);
                context.markAsAdvised();
            }
        }
    }

    /**
     * Creates a new static class field.
     *
     * @param ctClass the class
     */
    private void addStaticClassField(final CtClass ctClass) throws NotFoundException, CannotCompileException {
        CtField field = new CtField(
                ctClass.getClassPool().get("java.lang.Class"),
                       TransformationUtil.STATIC_CLASS_FIELD,
                ctClass
        );
        field.setModifiers(Modifier.STATIC | Modifier.PRIVATE | Modifier.FINAL);
        ctClass.addField(field, "java.lang.Class.forName(\"" + ctClass.getName().replace('/', '.') + "\")");
    }

    /**
     * Adds a new <code>JoinPointManager</code> field to the advised class.
     *
     * @param ctClass
     * @param definition
     */
    private void addJoinPointManagerField(final CtClass ctClass, final SystemDefinition definition)
            throws NotFoundException, CannotCompileException {

        CtField field = new CtField(
                ctClass.getClassPool().get(TransformationUtil.JOIN_POINT_MANAGER_CLASS),
                TransformationUtil.JOIN_POINT_MANAGER_FIELD,
                ctClass
        );
        field.setModifiers(Modifier.STATIC | Modifier.PRIVATE | Modifier.FINAL);
        StringBuffer body = new StringBuffer();
        body.append(TransformationUtil.JOIN_POINT_MANAGER_CLASS);
        body.append('.');
        body.append(TransformationUtil.GET_JOIN_POINT_MANAGER);
        body.append('(');
        body.append(TransformationUtil.STATIC_CLASS_FIELD);
        body.append(", \"");
        body.append(definition.getUuid());
        body.append("\")");
        ctClass.addField(field, body.toString());
    }

    /**
     * Filters the classes to be transformed.
     *
     * @param definition    the definition
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
        if (definition.inPreparePackage(className)) {
            return false;
        }
        if (definition.hasExecutionPointcut(classMetaData) ||
            definition.hasCallPointcut(classMetaData) ||
            definition.hasGetPointcut(classMetaData) ||
            definition.hasSetPointcut(classMetaData) ||
            definition.hasHandlerPointcut(classMetaData) ||
            definition.hasIntroductions(classMetaData)) {
            return false;
        }
        return true;
    }
}
