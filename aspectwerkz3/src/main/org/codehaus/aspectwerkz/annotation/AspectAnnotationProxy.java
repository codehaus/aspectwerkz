/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;

import org.codehaus.aspectwerkz.util.Strings;

/**
 * The aspect annotation proxy.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AspectAnnotationProxy extends UntypedAnnotationProxy {
    private String m_deploymentModel = "perJVM";

    public String deploymentModel() {
        return m_deploymentModel;
    }

    public void setValue(final String value) {
        String[] parts = Strings.splitString(value, " ");
        StringBuffer deploymentModel = new StringBuffer();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            int equals = part.indexOf('=');
            if (equals > 0) {
                String name = part.substring(0, equals);
                String param = part.substring(equals + 1, part.length());
                if (name.equalsIgnoreCase("name")) {
                    m_name = param;
                }
            } else {
                deploymentModel.append(' ');
                deploymentModel.append(part);
            }
        }
        String tmp = deploymentModel.toString().trim();
        if ((tmp != null) && !tmp.equals("")) {
            m_deploymentModel = tmp.trim();
        }
    }
}
