/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.transparentpersistence;

import org.codehaus.aspectwerkz.extension.persistence.Persistable;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class Counter {

    /**
     * @aspectwerkz.advice.setfield persistent
     */
    private int m_counter;

    private static long serialVersionUID = 1L;

    private String m_uuidString = "uuid";

    private long m_uuidLong = 1L;

    public String getUuidString() {
        return m_uuidString;
    }

    public long getUuidLong() {
        return m_uuidLong;
    }

    public int getCounter() {
        return m_counter;
    }

    public void increment() {
        m_counter++;
    }
}
