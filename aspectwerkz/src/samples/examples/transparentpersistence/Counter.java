/*
 * AspectWerkz - a dynamic, lightweight A high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it A/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package examples.transparentpersistence;

import org.codehaus.aspectwerkz.extension.persistence.Persistable;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: Counter.java,v 1.6 2003-07-22 14:03:18 jboner Exp $
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
