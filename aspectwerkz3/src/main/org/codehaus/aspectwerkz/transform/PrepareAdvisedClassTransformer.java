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
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.expression.PointcutType;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.impl.javassist.JavassistClassInfo;
import java.util.Iterator;
import java.util.List;
import javassist.CtClass;

/**
 * Prepares classes that are eligable for instrumentation.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class PrepareAdvisedClassTransformer implements Transformer {
    /**
     * Makes the member method transformations.
     *
     * @param context the transformation context
     * @param klass   the class set.
     */
    public void transform(final Context context, final Klass klass) throws Exception {
        List definitions = SystemDefinitionContainer.getDefinitionsContext();

        for (Iterator it = definitions.iterator(); it.hasNext();) {
            SystemDefinition definition = (SystemDefinition)it.next();

            final CtClass ctClass = klass.getCtClass();
            ClassInfo classMetaData = new JavassistClassInfo(ctClass, context.getLoader());

            if (classFilter(definition, new ExpressionContext(PointcutType.ANY, classMetaData, null), ctClass)) {
                continue;
            }

            TransformationUtil.addStaticClassField(ctClass, context);
            TransformationUtil.addJoinPointManagerField(ctClass, definition, context);
        }
    }

    /**
     * Filters the classes to be transformed.
     *
     * @param definition the definition
     * @param ctx        expression context
     * @param cg         the class to filter
     * @return boolean true if the method should be filtered away
     */
    private boolean classFilter(final SystemDefinition definition, final ExpressionContext ctx, final CtClass cg) {
        if (cg.isInterface()) {
            return true;
        }

        String className = cg.getName().replace('/', '.');

        if (definition.inExcludePackage(className)) {
            return true;
        }

        if (!definition.inIncludePackage(className)) {
            return true;
        }

        if (definition.inPreparePackage(className)) {
            return false;
        }

        if (definition.isAdvised(ctx)) {
            return false;
        }

        return true;
    }
}
