/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import java.util.Iterator;

import javassist.CtClass;

import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.definition.DefinitionLoader;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.JavassistMetaDataMaker;
import org.codehaus.aspectwerkz.transform.TransformationUtil;

/**
 * Adds an interfaces to classes.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public final class AddInterfaceTransformer implements Transformer {

    /**
     * Adds an interfaces to the classes specified.
     *
     * @param context the transformation context
     * @param klass the class
     */
    public void transform(final Context context, final Klass klass) {
        // loop over all the definitions
        for (Iterator it = DefinitionLoader.getDefinitions().iterator(); it.hasNext();) {
            SystemDefinition definition = (SystemDefinition)it.next();

            final CtClass ctClass = klass.getCtClass();
            ClassMetaData classMetaData = JavassistMetaDataMaker.createClassMetaData(ctClass);

            if (classFilter(ctClass, classMetaData, definition)) {
                return;
            }
            IntroductionTransformer.addInterfaceIntroductions(definition, ctClass, context, classMetaData);
        }
    }

    /**
     * Filters the classes to be transformed.
     *
     * @param ctClass the class to filter
     * @param classMetaData the class meta-data
     * @param definition the definition
     * @return boolean true if the method should be filtered away
     */
    private boolean classFilter(final CtClass ctClass,
                                final ClassMetaData classMetaData,
                                final SystemDefinition definition) {
        if (ctClass.isInterface() ||
                TransformationUtil.hasSuperClass(classMetaData, "org.codehaus.aspectwerkz.aspect.Aspect")) {
            return true;
        }
        String className = ctClass.getName();
        if (definition.inExcludePackage(className)) {
            return true;
        }
        if (definition.inIncludePackage(className) && definition.hasIntroductions(classMetaData)) {
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
