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

import javassist.CtClass;
import javassist.NotFoundException;
import org.codehaus.aspectwerkz.definition.InterfaceIntroductionDefinition;
import org.codehaus.aspectwerkz.definition.IntroductionDefinition;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;

/**
 * Handles the attribdef specific algorithms for adding the introductions.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class IntroductionTransformer {

    /**
     * Adds the interface introductions to the class.
     *
     * @param definition    the definition
     * @param cg            the class gen
     * @param context       the TF context
     * @param classMetaData the class meta-data
     */
    public static void addInterfaceIntroductions(
            final SystemDefinition definition,
            final CtClass cg,
            final Context context,
            final ClassMetaData classMetaData) {
        boolean isClassAdvised = false;
        List introDefs = definition.getInterfaceIntroductions(classMetaData);
        for (Iterator it = introDefs.iterator(); it.hasNext();) {
            InterfaceIntroductionDefinition introductionDef = (InterfaceIntroductionDefinition)it.next();
            for (Iterator iit = introductionDef.getInterfaceClassNames().iterator(); iit.hasNext();) {
                String className = (String)iit.next();

                if (implementsInterface(cg, className)) {
                    continue;
                }

                if (className != null) {
                    try {
                        cg.addInterface(cg.getClassPool().get(className));
                    }
                    catch (NotFoundException e) {
                        throw new WrappedRuntimeException(e);
                    }
                    isClassAdvised = true;
                }
            }
        }

        if (isClassAdvised) {
            context.markAsAdvised();
        }
    }

    /**
     * Adds introductions to the class.
     *
     * @param definition    the definition
     * @param context       the transformation context
     * @param classMetaData the class meta-data
     * @param ctClass       the class gen
     * @param transformer   the transformer
     */
    public static void addMethodIntroductions(
            final SystemDefinition definition,
            final Context context,
            final ClassMetaData classMetaData,
            final CtClass ctClass,
            final AddImplementationTransformer transformer) {

        List introductionDefs = definition.getIntroductionDefinitions(classMetaData);
        boolean isClassAdvised = false;
        for (Iterator it = introductionDefs.iterator(); it.hasNext();) {
            IntroductionDefinition introDef = (IntroductionDefinition)it.next();
            int methodIndex = 0;
            for (Iterator mit = introDef.getMethodIntroductions().iterator(); mit.hasNext(); methodIndex++) {
                int mixinIndex = definition.getMixinIndexByName(introDef.getName());
                isClassAdvised = true;
                //TODO any use case for a method already implemented ?
                transformer.createProxyMethod(
                        ctClass,
                        (MethodMetaData)mit.next(),
                        mixinIndex,
                        methodIndex,
                        definition
                );
            }
        }

        if (isClassAdvised) {
            context.markAsAdvised();
        }
    }

    /**
     * Checks if a class implements an interface.
     *
     * @param ctClass ConstantUtf8 constant
     * @return true if the class implements the interface
     */
    private static boolean implementsInterface(final CtClass ctClass, final String interfaceName) {
        try {
            CtClass[] interfaces = ctClass.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                if (interfaces[i].getName().replace('/', '.').equals(interfaceName)) {
                    return true;
                }
            }
            return false;
        }
        catch (NotFoundException e) {
            throw new WrappedRuntimeException(e);
        }
    }
}
