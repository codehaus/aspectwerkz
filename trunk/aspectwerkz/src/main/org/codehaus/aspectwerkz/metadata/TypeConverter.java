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
package org.codehaus.aspectwerkz.metadata;

import com.thoughtworks.qdox.model.Type;

/**
 * Methods to convert Class to Java type names. Handles array types and the constructor "return" type.
 *
 * @author <a href="mailto:vta@medios.fi">Tibor Varga</a>
 * @version $Id: TypeConverter.java,v 1.1 2003-06-26 19:30:14 jboner Exp $
 */
public class TypeConverter {

    /**
     * Converts an array of Classes to their Java language declaration equivalents.
     *
     * @param types is the array of <code>Class</code> objects.
     * @return an array of Strings representing the given types. For <code>null</code> types,
     * this method returns "void"s.
     */
    public static String[] convertTypeToJava(final Class[] types) {
        String[] parameterTypeNames = new String[types.length];

        for (int i = 0; i < types.length; i++) {
            parameterTypeNames[i] = convertTypeToJava(types[i]);
        }

        return parameterTypeNames;
    }

    /**
     * Converts a Class to its Java language declaration equivalent.
     *
     * @param type is the <code>Class</code> object.
     * @return a Strings representing the given types. For <code>null</code> type,
     * this method returns "void".
     */
    public static String convertTypeToJava(final Class type) {
        String rv = null;

        // constructor return type can be null
        if (type != null) {
            StringBuffer dim = new StringBuffer();
            Class componentType = type.getComponentType();

            for (Class nestedType = type; nestedType.isArray();
                 nestedType = nestedType.getComponentType()) {
                dim.append("[]");
            }
            // Found a component type => we had an array
            if (dim.length() > 0) {
                rv = componentType.getName() + dim;
            }
            else {
                rv = type.getName();
            }
        }
        else {
            rv = "void";
        }
        return rv;
    }

    public static String convertTypeToJava(final Type type) {
        StringBuffer dim = new StringBuffer();

        if (type.isArray()) {
            for (int i = type.getDimensions(); i > 0; --i) {
                dim.append("[]");
            }
        }

        return type.getValue() + dim;
    }
}
