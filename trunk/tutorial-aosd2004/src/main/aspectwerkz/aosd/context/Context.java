/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.context;

import java.io.Serializable;

/**
 * The base interface for all contexts.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface Context extends Serializable {

    public final static String PRINCIPAL = "PRINCIPAL";
    public final static String CREDENTIAL = "CREDENTIAL";
    public final static String PARAMETERS = "PARAMETERS";

    /**
     * Retrieve an item from Context.
     *
     * @param key the key into context
     * @return the object
     * @exception aspectwerkz.aosd.context.ContextException if object not found.
     */
    Object get(Object key) throws ContextException;

    /**
     * Adds items to Context.
     *
     * @param key the items key
     * @param value the item
     * @exception java.lang.IllegalStateException if context is read only
     */
    void put(Object key, Object value) throws IllegalStateException;

    /**
     * Make the context read-only.
     * Any attempt to write to the context via put()
     * will result in an IllegalStateException.
     */
    void markReadOnly();

    /**
     * Checks if the context is read only
     *
     * @return boolean
     */
    boolean isReadOnly();
}






