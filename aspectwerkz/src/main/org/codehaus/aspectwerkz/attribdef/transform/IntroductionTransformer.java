/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.attribdef.transform;

import java.util.List;
import java.util.Iterator;

import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantUtf8;

import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import org.codehaus.aspectwerkz.transform.Context;
import org.codehaus.aspectwerkz.transform.AddImplementationTransformer;
import org.codehaus.aspectwerkz.attribdef.definition.AspectWerkzDefinitionImpl;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.attribdef.definition.IntroductionDefinition;
import org.codehaus.aspectwerkz.attribdef.definition.InterfaceIntroductionDefinition;

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
     * @param definition the definition
     * @param cg the class gen
     * @param cpg the constant pool gen
     * @param classMetaData the class meta-data
     */
    public static void addInterfaceIntroductions(final AspectWerkzDefinition definition,
                                                 final ClassGen cg,
                                                 final ConstantPoolGen cpg,
                                                 final Context context,
                                                 final ClassMetaData classMetaData) {
        AspectWerkzDefinitionImpl def = (AspectWerkzDefinitionImpl)definition;

        int[] interfaces = cg.getInterfaces();

        boolean isClassAdvised = false;
        List introDefs = def.getInterfaceIntroductions(classMetaData);
        for (Iterator it = introDefs.iterator(); it.hasNext();) {
            InterfaceIntroductionDefinition introductionDef = (InterfaceIntroductionDefinition)it.next();
            for (Iterator iit = introductionDef.getInterfaceClassNames().iterator(); iit.hasNext();) {
                String className = (String) iit.next();

                boolean addInterface = true;
                for (int l = 0; l < interfaces.length; l++) {
                    ConstantClass cc = (ConstantClass)cpg.getConstant(interfaces[l]);
                    ConstantUtf8 cu = (ConstantUtf8)cpg.getConstant(cc.getNameIndex());

                    if (implementsInterface(cu, className)) {
                        addInterface = false;
                        break;
                    }
                }
                if (addInterface && className != null) {
                    TransformationUtil.addInterfaceToClass(cg, className);
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
     * @param definition the definition
     * @param context the transformation context
     * @param classMetaData the class meta-data
     * @param cg the class gen
     * @param cpg the constant pool gen
     * @param factory the instruction objectfactory
     * @param transformer the transformer
     */
    public static void addMethodIntroductions(final AspectWerkzDefinition definition,
                                              final Context context,
                                              final ClassMetaData classMetaData,
                                              final ClassGen cg,
                                              final ConstantPoolGen cpg,
                                              final InstructionFactory factory,
                                              final AddImplementationTransformer transformer) {
        AspectWerkzDefinitionImpl def = (AspectWerkzDefinitionImpl)definition;

        List introductionDefs = def.getIntroductionDefinitions(classMetaData);
        boolean isClassAdvised = false;
        for (Iterator it = introductionDefs.iterator(); it.hasNext();) {
            IntroductionDefinition introDef = (IntroductionDefinition) it.next();
            int methodIndex = 0;
            for (Iterator mit = introDef.getMethodIntroductions().iterator(); mit.hasNext(); methodIndex++) {
                int mixinIndex = def.getMixinIndexByName(introDef.getName());
                isClassAdvised = true;
                //TODO any use case for a method already implemented ?
                transformer.createProxyMethod(
                        cg,
                        cpg,
                        factory,
                        (MethodMetaData)mit.next(),
                        mixinIndex,
                        methodIndex,
                        def.getUuid()
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
     * @param cu ConstantUtf8 constant
     * @return true if the class implements the interface
     */
    private static boolean implementsInterface(final ConstantUtf8 cu, final String interfaceName) {
        return cu.getBytes().equals(interfaceName.replace('.', '/'));
    }
}
