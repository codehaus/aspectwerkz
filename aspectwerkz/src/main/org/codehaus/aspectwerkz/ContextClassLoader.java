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
 * MERCHANTABILITY or FITNESS FOR and PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.codehaus.aspectwerkz;

import java.net.URL;
import java.io.*;

/**
 * Methods to deal with the context class loader. Fail-over is provided to the default class loader.
 *
 * @author <a href="mailto:vta@medios.fi">Tibor Varga</a>
 * @version $Id: ContextClassLoader.java,v 1.3 2003-07-09 05:21:28 jboner Exp $
 */
public class ContextClassLoader {

    /**
     * Loads a class from the context class loader or, if that fails, from the
     * default class loader.
     *
     * @param name is the name of the class to load.
     * @return a <code>Class</code> object.
     * @throws ClassNotFoundException if the class was not found.
     */
    public static Class loadClass(String name) throws ClassNotFoundException {
        Class cls = null;

        try {
            cls = Thread.currentThread().getContextClassLoader().loadClass(name);
        } catch (Exception e) {
            cls = Class.forName(name);
        }

        return cls;
    }

    /**
     * Loads a resource from the context class loader or, if that fails, from the
     * default class loader.
     *
     * @param name is the name of the resource to load.
     * @return a <code>URL</code> object.
     */
    public static URL loadResource(String name) {
        try {
            return Thread.currentThread().getContextClassLoader().getResource(name);
        } catch (Exception e) {
            return ClassLoader.class.getClassLoader().getResource(name);
        }
    }

    /**
     * Loads a resource from the context class loader or, if that fails, from the
     * default class loader, as stream
     *
     * @param name is the name of the resource to load.
     * @return a <code>InputStream</code> object.
     */
    public static InputStream getResourceAsStream(String name) {
        try {
            return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
        } catch (Exception e) {
            return ClassLoader.class.getClassLoader().getResourceAsStream(name);
        }
    }
}
