/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.unitofwork.jta;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;

import aspectwerkz.aosd.definition.TransactionDefinition;
import org.codehaus.aspectwerkz.definition.attribute.Attributes;
import org.codehaus.aspectwerkz.definition.attribute.CustomAttribute;

/**
 * Manages the transaction definition for each method.
 * <p/>Caches the transaction definitions in a memory sensitive cache.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class TransactionDefinitionController {

    /**
     * Memory sensitive cache for the transaction definitions.
     */
    private static final Map s_txDefCache = new WeakHashMap();

    /**
     * Retrieves the transaction definition for a specific method.
     *
     * @param method the method to retrieve transaction definition for
     * @return the transaction definition
     */
    public static TransactionDefinition getTransactionDefinition(final Method method) {
        if (method == null) throw new IllegalArgumentException("method can not be null");

        if (s_txDefCache.containsKey(method)) {
            return (TransactionDefinition)s_txDefCache.get(method);
        }

        TransactionDefinition txDef = new TransactionDefinition();
        Object[] methodAttributes = Attributes.getAttributes(method);

        for (int i = 0; i < methodAttributes.length; i++) {
            Object methodAttr = methodAttributes[i];
            if (methodAttr instanceof CustomAttribute) {
                CustomAttribute customAttr = (CustomAttribute)methodAttr;
                if (customAttr.getName().equalsIgnoreCase(TransactionDefinition.TRANSACTION)) {
                    if (customAttr.getValue().equalsIgnoreCase(TransactionDefinition.TX_REQUIRES)) {
                        txDef.setTransactionType(TransactionDefinition.TX_REQUIRES);
                    }
                    else if (customAttr.getValue().equalsIgnoreCase(TransactionDefinition.TX_REQUIRES_NEW)) {
                        txDef.setTransactionType(TransactionDefinition.TX_REQUIRES_NEW);
                    }
                }
            }
        }
        s_txDefCache.put(method, txDef);

        return txDef;
    }
}
