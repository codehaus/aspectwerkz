/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

/**
 * Interface for the interface transformer components.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface AspectWerkzInterfaceTransformerComponent {

    /**
     * Transforms the class.
     *
     * @param context the transformation context
     * @param klass the class
     */
    public abstract void transformInterface(final Context context, final Klass klass);

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
