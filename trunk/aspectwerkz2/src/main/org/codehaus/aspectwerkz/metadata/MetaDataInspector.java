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
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class MetaDataInspector {

    public static boolean hasField(ClassMetaData classMetaData, String fieldName) {
        for (Iterator fields = classMetaData.getFields().iterator(); fields.hasNext();) {
            if (((FieldMetaData)fields.next()).getName().equals(fieldName)) {
                return true;
            }
        }
        return false;
    }
}
