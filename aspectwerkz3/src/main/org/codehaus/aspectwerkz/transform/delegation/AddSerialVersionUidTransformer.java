/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.delegation;

import org.codehaus.aspectwerkz.transform.Context;
import org.codehaus.aspectwerkz.transform.Transformer;

/**
 * Add serial ver UID field
 * 
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class AddSerialVersionUidTransformer implements Transformer {
    /**
     * Compute and add serial ver uid fiel
     * 
     * @param context
     * @param klass
     * @throws Exception
     */
    public void transform(Context context, Klass klass) throws Exception {
        if (JavassistHelper.isSerialVerUidNeeded(klass.getCtClass())) {
            long initialSerialVerUid = JavassistHelper.calculateSerialVerUid(klass.getInitialCtClass());
            JavassistHelper.setSerialVersionUID(klass.getCtClass(), initialSerialVerUid);
        }
    }
}