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
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.Handler;

import org.codehaus.aspectwerkz.definition.DefinitionLoader;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.JavassistMetaDataMaker;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;

/**
 * Advises HANDLER join points.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class HandlerTransformer implements Transformer {

    /**
     * List with the definitions.
     */
    private List m_definitions;

    /**
     * Creates a new instance of the transformer.
     */
    public HandlerTransformer() {
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
            final ClassMetaData classMetaData = JavassistMetaDataMaker.createClassMetaData(ctClass);

            if (classFilter(definition, classMetaData, ctClass)) {
                return;
            }

            ctClass.instrument(
                    new ExprEditor() {
                        public void edit(Handler handlerExpr) throws CannotCompileException {
                            try {
                                CtClass exceptionClass = null;
                                try {
                                    exceptionClass = handlerExpr.getType();
                                }
                                catch (NullPointerException e) {
                                    return;
                                }

                                CtBehavior where = null;
                                try {
                                    where = handlerExpr.where();
                                }
                                catch (RuntimeException e) {
                                    // <clinit> access leads to a bug in Javassist
                                    where = ctClass.getClassInitializer();
                                }

                                MethodMetaData methodMetaData = null; //JavassistMetaDataMaker.createMethodMetaData(...);

                                ClassMetaData exceptionClassMetaData = JavassistMetaDataMaker.createClassMetaData(
                                        exceptionClass
                                );


                                // TODO: NO filtering on class and method is done (only exception class), needs to be implemented
                                if (!definition.hasHandlerPointcut(
                                        classMetaData, methodMetaData, exceptionClassMetaData
                                )) {
                                    return;
                                }

                                // call the wrapper method instead of the callee method
                                StringBuffer body = new StringBuffer();
                                body.append(TransformationUtil.JOIN_POINT_MANAGER_FIELD);
                                body.append('.');
                                body.append(TransformationUtil.PROCEED_WITH_HANDLER_JOIN_POINT_METHOD);
                                body.append('(');

                                // TODO: unique hash is needed, based on: executing class, executing method, catch clause (and sequence number?)
                                body.append(TransformationUtil.calculateHash(exceptionClass));
                                body.append(", $1, ");
                                if (Modifier.isStatic(where.getModifiers())) {
                                    body.append("(Object)null, \"");
                                }
                                else {
                                    body.append("this, \"");
                                }

                                // TODO: use a better signature (or remove)
                                body.append(exceptionClass.getName());
                                body.append("\");");

                                handlerExpr.insertBefore(body.toString());
                                context.markAsAdvised();
                            }
                            catch (NotFoundException nfe) {
                                nfe.printStackTrace();
                            }
                        }
                    }
            );
        }
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

        // TODO: the class filtering is NOT implemented, HOWTO? Support 'class.metod->excetionType' OR rely on the 'within' construct? I think I prefer the within option. 
        if (definition.hasHandlerPointcut(classMetaData)) {
            return false;
        }
        return true;
    }
}
