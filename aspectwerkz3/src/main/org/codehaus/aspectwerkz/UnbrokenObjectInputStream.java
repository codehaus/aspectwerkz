/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * Fixes the ObjectInputStream class, which does not always resolve the class correctly in complex
 * class loader hierarchies.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class UnbrokenObjectInputStream extends ObjectInputStream {

    /**
     * Creates a a new instance.
     *
     * @throws IOException
     * @throws SecurityException
     */
    public UnbrokenObjectInputStream() throws IOException, SecurityException {
        super();
    }

    /**
     * Creates a new instance.
     *
     * @param in the input stream to deserialize the object from.
     * @throws IOException
     */
    public UnbrokenObjectInputStream(final InputStream in) throws IOException {
        super(in);
    }

    /**
     * Overrides the parents resolveClass method and resolves the class using the context class loader
     * instead of Class.forName().
     */
    protected Class resolveClass(final ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(desc.getName());
        } catch (ClassNotFoundException ex) {
            return super.resolveClass(desc);
        }
    }
}
