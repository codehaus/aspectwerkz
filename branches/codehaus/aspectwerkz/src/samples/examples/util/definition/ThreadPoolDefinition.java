/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
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
package examples.util.definition;

import examples.util.definition.Definition;

/**
 * Definition for the asynchronous concern.
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: ThreadPoolDefinition.java,v 1.1.1.1 2003-05-11 15:15:40 jboner Exp $
 */
public class ThreadPoolDefinition implements Definition {

    public int getMaxSize() {
        return m_maxSize;
    }

    public void setMaxSize(final int maxSize) {
        m_maxSize = maxSize;
    }

    public int getMinSize() {
        return m_minSize;
    }

    public void setMinSize(final int minSize) {
        m_minSize = minSize;
    }

    public int getInitSize() {
        return _initSize;
    }

    public void setInitSize(final int initSize) {
        _initSize = initSize;
    }

    public int getKeepAliveTime() {
        return m_keepAliveTime;
    }

    public void setKeepAliveTime(final int keepAliveTime) {
        m_keepAliveTime = keepAliveTime;
    }

    public boolean getWaitWhenBlocked() {
        return m_waitWhenBlocked;
    }

    public void setWaitWhenBlocked(final boolean waitWhenBlocked) {
        m_waitWhenBlocked = waitWhenBlocked;
    }

    public boolean getBounded() {
        return m_bounded;
    }

    public void setBounded(final boolean bounded) {
        m_bounded = bounded;
    }

    private int m_maxSize;
    private int m_minSize;
    private int _initSize;
    private int m_keepAliveTime;
    private boolean m_waitWhenBlocked = true;
    private boolean m_bounded = true;
}
