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

import java.lang.reflect.Field;

/**
 * Convenience methods to construct <code>MetaData</code> instances.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:vta@medios.fi">Tibor Varga</a>
 * @version $Id: ReflectionMetaDataMaker.java,v 1.1 2003-06-26 19:30:13 jboner Exp $
 */
public class ReflectionMetaDataMaker {

    /**
     * Construct meta-data from its basic details.
     *
     * @param methodName is the name of the method.
     * @param parameterTypes is the list of parameter types.
     * @param returnType is the return type.
     * @return a <code>MethodMetaData</code> instance.
     */
    public static MethodMetaData createMethodMetaData(final String methodName,
                                                      final Class[] parameterTypes,
                                                      final Class returnType) {
        MethodMetaData data = new MethodMetaData();

        data = new MethodMetaData();
        data.setName(methodName);
        data.setParameterTypes(TypeConverter.convertTypeToJava(parameterTypes));
        data.setReturnType(TypeConverter.convertTypeToJava(returnType));

        return data;
    }

    /**
     * Construct meta-data from a Java <code>Method</code> object.
     *
     * @param method is the <code>Method</code> object to extract details from.
     * @return a <code>MethodMetaData</code> instance.
     */
    public static MethodMetaData createMethodMetaData(final java.lang.reflect.Method method) {
        MethodMetaData data = new MethodMetaData();

        data.setName(method.getName());
        data.setModifiers(method.getModifiers());
        data.setReturnType(TypeConverter.convertTypeToJava(method.getReturnType()));
        data.setParameterTypes(TypeConverter.convertTypeToJava(method.getParameterTypes()));
        data.setExceptionTypes(TypeConverter.convertTypeToJava(method.getExceptionTypes()));

        return data;
    }

    /**
     *
     * Construct meta-data from its basic details.
     *
     * @param fieldName is the name of the field.
     * @param typeName is the type of the field.
     * @return a <code>FieldMetaData</code> instance.
     */
    public static FieldMetaData createFieldMetaData(final String fieldName,
                                                    final String typeName) {
        FieldMetaData data = new FieldMetaData();

        data.setName(fieldName);
        data.setType(typeName);

        return data;
    }

    /**
     * Create a new <code>FieldMetaData</code>.
     *
     * @param field the field
     * @return the field meta-data
     */
    public static FieldMetaData createFieldMetaData(final Field field) {
        final FieldMetaData fieldMetaData = new FieldMetaData();

        fieldMetaData.setName(field.getName());
        fieldMetaData.setModifiers(field.getModifiers());
        fieldMetaData.setType(TypeConverter.convertTypeToJava(field.getType()));

        return fieldMetaData;
    }
}
