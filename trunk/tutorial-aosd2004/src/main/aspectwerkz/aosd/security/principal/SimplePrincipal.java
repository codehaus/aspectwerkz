/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.security.principal;

import java.security.Principal;
import java.io.Serializable;

/**
 * An implementation of a simple string based principal.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class SimplePrincipal implements Principal, Serializable {

    private String m_name;

    /**
     * Constructor. Sets the name of the principal.
     *
     * @param name
     */
    public SimplePrincipal(final String name) {
        if (name == null) throw new IllegalArgumentException("name can not be null");
        m_name = name;
    }

    /**
     * Gets the name.
     *
     * @return
     */
    public String getName() {
        return m_name;
    }

    /**
     * The overridden toString implementation.
     *
     * @return
     */
    public String toString() {
        return m_name;
    }

    /**
     * The overridden equals implementation.
     *
     * @param obj
     * @return
     */
    public boolean equals(final Object obj) {
        if (!(obj instanceof Principal)) {
            return false;
        }
        return m_name.equals(((Principal) obj).getName());
    }

    /**
     * The overridden hashCode implementation.
     *
     * @return
     */
    public int hashCode() {
        return m_name.hashCode();
    }
}

