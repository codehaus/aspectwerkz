/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

/**
 * Interface for code transformer components.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface AspectWerkzCodeTransformerComponent {

    /**
     * Transforms the class.
     *
     * @param context the transformation context
     * @param klass the class
     */
    public void transformCode(final Context context, final Klass klass);

    /**
     * Callback method. Is being called before each transformation.
     */
    public void sessionStart();

    /**
     * Callback method. Is being called after each transformation.
     */
    public void sessionEnd();

    /**
     * Callback method. Prints a log/status message at each transformation.
     *
     * @return a log string
     */
    public String verboseMessage();
}
