/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.xmldef.transform;

import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.codehaus.aspectwerkz.transformj.Context;
import org.codehaus.aspectwerkz.transformj.AddImplementationTransformer;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.xmldef.definition.AspectWerkzDefinitionImpl;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.MethodComparator;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import javassist.CtClass;
import javassist.NotFoundException;

/**
 * Handles the xmldef specific algorithms for adding the introductions.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class IntroductionTransformerJ {

    /**
     * Adds the interface introductions to the class.
     *
     * @param definition the definition
     * @param cg the class gen
     * @param cpg the constant pool gen
     */
    public static void addInterfaceIntroductions(final AspectWerkzDefinition definition,
                                                 final CtClass cg,
                                                 final Context context) {
        AspectWerkzDefinitionImpl def = (AspectWerkzDefinitionImpl)definition;

        CtClass[] interfaces = null;
        try {
            interfaces = cg.getInterfaces();
        } catch (NotFoundException e) {
            throw new WrappedRuntimeException(e);
        }

        boolean isClassAdvised = false;
        ClassMetaData classMetaData = new ClassMetaData();
        classMetaData.setName(cg.getName());
        for (Iterator it2 = def.getIntroductionNames(classMetaData).iterator(); it2.hasNext();) {

            String introductionName = (String)it2.next();
            String interfaceName = def.getIntroductionInterfaceName(introductionName);

            boolean addInterface = true;

            for (int l = 0; l < interfaces.length; l++) {
                if (IntroductionTransformerJ.implementsInterface(cg, interfaceName)) {
                    addInterface = false;
                    break;
                }
            }
            if (addInterface) {
                if (interfaceName == null || interfaceName.equals("")) {
                    throw new DefinitionException("trying to weave null interface to " + cg.getName() + ": definition file is not consistentadd");
                }
                try {
                cg.addInterface(cg.getClassPool().get(interfaceName));
                } catch (NotFoundException e) {
                    throw new WrappedRuntimeException(e);
                }
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
     * @param cg the class gen
     * @param cpg the constant pool gen
     * @param factory the instruction objectfactory
     * @param transformer the transformer
     */
    public static void addMethodIntroductions(final AspectWerkzDefinition definition,
                                              final Context context,
                                              final CtClass cg,
                                              final AddImplementationTransformer transformer) {
        AspectWerkzDefinitionImpl def = (AspectWerkzDefinitionImpl)definition;

        boolean isClassAdvised = false;
        ClassMetaData metaData = new ClassMetaData();
        metaData.setName(cg.getName());
        for (Iterator it = def.getIntroductionNames(metaData).iterator(); it.hasNext();) {

            String introductionName = (String)it.next();
            String introductionImplName = def.getIntroductionImplName(introductionName);

            if (introductionImplName == null) {
                continue;
            }

            int introductionIndex = 0;
            List methodMetaDataList = Collections.synchronizedList(new ArrayList());
            try {
                introductionIndex = def.getIntroductionIndex(introductionName);

                // get the method meta-data for the class
                boolean match = false;
                Map metaDataRepository = context.getMetaDataRepository();
                for (Iterator it2 = metaDataRepository.values().iterator(); it2.hasNext();) {
                    if (match) break;
                    Set metaDataSet = (Set)it2.next();
                    for (Iterator it3 = metaDataSet.iterator(); it3.hasNext();) {
                        ClassMetaData classMetaData = (ClassMetaData)it3.next();
                        if (classMetaData.getName().equals(introductionImplName)) {
                            methodMetaDataList = classMetaData.getMethods();
                            match = true;
                            break;
                        }
                    }
                }
                if (methodMetaDataList == null) {
                    throw new RuntimeException("no meta-data for introduction " + introductionImplName + " could be found in repository");
                }
            }
            catch (Exception e) {
                throw new DefinitionException("trying to weave introduction with null or empty string as name to class " + cg.getName() + ": definition file is not consistent");
            }

            if (methodMetaDataList == null) {
                continue; // interface introduction
            }

            // the iterator is on a list and the loop body does list.remove
            // which is forbidden
            List methodMetaDataListFiltered = new ArrayList();
            for (Iterator it2 = methodMetaDataList.iterator(); it2.hasNext();) {
                MethodMetaData methodMetaData = (MethodMetaData)it2.next();

                // remove the ___AW_getUuid, ___AW_getMetaData, ___AW_addMetaData and class$ methods
                // as well as some other methods before sorting the method list
                if (!(
                        methodMetaData.getName().equals("equals") ||
                        methodMetaData.getName().equals("hashCode") ||
                        methodMetaData.getName().equals("getClass") ||
                        methodMetaData.getName().equals("toString") ||
                        methodMetaData.getName().equals("wait") ||
                        methodMetaData.getName().equals("notify") ||
                        methodMetaData.getName().equals("notifyAll") ||
                        methodMetaData.getName().equals(
                                TransformationUtil.GET_UUID_METHOD) ||
                        methodMetaData.getName().equals(
                                TransformationUtil.GET_UUID_METHOD) ||
                        methodMetaData.getName().equals(
                                TransformationUtil.GET_META_DATA_METHOD) ||
                        methodMetaData.getName().equals(
                                TransformationUtil.SET_META_DATA_METHOD) ||
                        methodMetaData.getName().equals(
                                TransformationUtil.CLASS_LOOKUP_METHOD) ||
                        methodMetaData.getName().startsWith(
                                TransformationUtil.ORIGINAL_METHOD_PREFIX))) {
                    methodMetaDataListFiltered.add(methodMetaData);
                }
            }

            // sort the list so that we can enshure that the indexes are in synch
            // see AbstractIntroductionContainerStrategy#AbstractIntroductionContainerStrategy
            Collections.sort(methodMetaDataListFiltered, MethodComparator.
                    getInstance(MethodComparator.METHOD_META_DATA));

            int methodIndex = -1; // start with -1 since the method array is 0 indexed
            for (Iterator it2 = methodMetaDataListFiltered.iterator(); it2.hasNext();) {
                MethodMetaData methodMetaData = (MethodMetaData)it2.next();
                if (methodMetaData.getReturnType() == null || methodMetaData.getName().equals("<init>")) {
                    continue;
                }
                methodIndex++;
                transformer.createProxyMethod(
                        cg,
                        methodMetaData,
                        introductionIndex,
                        methodIndex,
                        def.getUuid()
                );
                isClassAdvised = true;
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
    private static boolean implementsInterface(final CtClass cu, final String interfaceName) {
        try {
        CtClass[] interfaces = cu.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (interfaces[i].getName().equals(interfaceName))
                return true;
        }
        return false;
        } catch (NotFoundException e) {
            throw new WrappedRuntimeException(e);
        }
    }
}
