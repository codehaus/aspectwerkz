/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import org.apache.bcel.generic.ClassGen;

import java.util.Arrays;

/**
 * Utility wrapping class level operation
 *
 * Note: derived from JMangler ExtensionSet, needs refactoring
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class AspectWerkzExtensionSet {

    /**
     * Add the given interface to the given class representation
     * @param cg ClassGen representation
     * @param interf FQN of the interface
     */
    public void addInterfaceToClass(ClassGen cg, String interf) {
        //@todo review log
        AspectWerkzPreProcessor.log("es.addIntf to " + cg.getClassName() + ": " + interf);
        //@todo: check for readonly class ??
        if ( ! Arrays.asList(cg.getInterfaceNames()).contains(interf))
            cg.addInterface(interf);
    }

    /**
     * Add the given method implementation to the given class representation
     * @param cg ClassGen representation
     * @param method method implementation
     */
    public void addMethod(ClassGen cg, org.apache.bcel.classfile.Method method) {
        //@todo review log
        AspectWerkzPreProcessor.log("es.addMethod to " + cg.getClassName() + ": " + method.toString());
        //@todo: check for read only ??
        if (cg.containsMethod(method.getName(), method.getSignature()) == null)
            cg.addMethod(method);
    }

    /**
     * Add the given field implementation to the given class representation
     * @param cg ClassGen representation
     * @param field field implementation
     */
    public void addField(ClassGen cg, org.apache.bcel.classfile.Field field) {
        //@todo review log
        AspectWerkzPreProcessor.log("es.addField to " + cg.getClassName() + ": " + field.toString());
        //@todo: check for read only ??
        if ( ! cg.containsField(field))
            cg.addField(field);
    }
}
