/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.attribdef.transform;

import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.Constants;

import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import org.codehaus.aspectwerkz.transform.Context;
import org.codehaus.aspectwerkz.transform.AddImplementationTransformer;
import org.codehaus.aspectwerkz.attribdef.definition.InterfaceIntroductionDefinition;
import org.codehaus.aspectwerkz.attribdef.definition.MethodIntroductionDefinition;
import org.codehaus.aspectwerkz.attribdef.definition.AspectWerkzDefinitionImpl;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;

/**
 * Handles the attribdef specific algorithms for adding the introductions.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
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
            String className = introductionDef.getInterfaceClassName();

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

        Map metaDataRepository = context.getMetaDataRepository();
        List introductionDefs = def.getIntroductionDefinitionsForClass(classMetaData);

        boolean isClassAdvised = false;
        for (Iterator it = introductionDefs.iterator(); it.hasNext();) {
            MethodIntroductionDefinition introDef = (MethodIntroductionDefinition)it.next();
            try {
                // get the method meta-data for the introduced method
                for (Iterator it2 = metaDataRepository.values().iterator(); it2.hasNext();) {
                    Set metaDataRep = ((Set)it2.next());
                    for (Iterator it3 = metaDataRep.iterator(); it3.hasNext();) {

                        // get the meta-data for the aspect
                        ClassMetaData aspectMetaData = (ClassMetaData)it3.next();

                        List methods = aspectMetaData.getAllMethods();
                        for (Iterator it4 = methods.iterator(); it4.hasNext();) {
                            MethodMetaData methodMetaData = (MethodMetaData)it4.next();
                            // try to find the meta-data for the introduced method
                            if ((methodMetaData.getName().equals(introDef.getMethod().getName()) &&
                                    (methodMetaData.getModifiers() & Constants.ACC_PUBLIC) != 0 &&
                                    !(methodMetaData.getReturnType() == null ||
                                    methodMetaData.getName().equals("<init>")))) {

                                int mixinIndex = def.getAspectIndexByName(
                                        aspectMetaData.getName()
                                );
                                int methodIndex = introDef.getMethodIndex();

                                transformer.createProxyMethod(
                                        cg, cpg, factory,
                                        methodMetaData,
                                        mixinIndex,
                                        methodIndex,
                                        definition.getUuid()
                                );
                                isClassAdvised = true;
                            }
                        }
                    }
                }
            }
            catch (Exception e) {
                throw new DefinitionException("can not weave introduction [" + introDef.getName() + "] for [" + cg.getClassName() + "]: definition is not valid, due to: " + e.getMessage());
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
