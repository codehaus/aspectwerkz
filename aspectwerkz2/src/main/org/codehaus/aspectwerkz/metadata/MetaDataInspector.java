/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.metadata;

import java.util.Iterator;

/**
 * Inspects meta-data.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class MetaDataInspector {

    /**
     * Checks if a class has a certain field.
     *
     * @param classMetaData
     * @param fieldName
     * @return
     */
    public static boolean hasField(final ClassMetaData classMetaData, final String fieldName) {
        for (Iterator fields = classMetaData.getFields().iterator(); fields.hasNext();) {
            if (((FieldMetaData)fields.next()).getName().equals(fieldName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a class implements a certain interface.
     *
     * @param classMetaData
     * @param interfaceName
     * @return
     */
    public static boolean hasInterface(final ClassMetaData classMetaData, final String interfaceName) {
        for (Iterator interfaces = classMetaData.getInterfaces().iterator(); interfaces.hasNext();) {
            if (((InterfaceMetaData)interfaces.next()).getName().equals(interfaceName)) {
                return true;
            }
        }
        return false;
    }
}
