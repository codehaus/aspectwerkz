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

import org.codehaus.aspectwerkz.transform.TransformationUtil;

import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;
import com.thoughtworks.qdox.model.Type;

/**
 * Convenience methods to construct <code>MetaData</code> instances.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:vta@medios.fi">Tibor Varga</a>
 * @version $Id: QDoxMetaDataMaker.java,v 1.1 2003-06-26 19:30:13 jboner Exp $
 */
public class QDoxMetaDataMaker {

    /**
     * Construct meta-data from a QDox <code>JavaMethod</code> object.
     *
     * @param method is the <code>JavaMethod</code> object to extract details from.
     * @return a <code>MethodMetaData</code> instance.
     */
    public static MethodMetaData createMethodMetaData(final JavaMethod method) {
        MethodMetaData data = new MethodMetaData();

        data.setName(method.getName());
        data.setModifiers(TransformationUtil.getModifiersAsInt(method.getModifiers()));

        Type returnType = method.getReturns();
        if (returnType != null) {
            data.setReturnType(TypeConverter.convertTypeToJava(returnType));
        }

        JavaParameter[] parameters = method.getParameters();
        String[] parameterTypes = new String[parameters.length];

        for (int j = 0; j < parameters.length; j++) {
            parameterTypes[j] = TypeConverter.convertTypeToJava(parameters[j].getType());
        }

        data.setParameterTypes(parameterTypes);

        Type[] exceptions = method.getExceptions();
        String[] exceptionTypes = new String[exceptions.length];
        for (int j = 0; j < exceptions.length; j++) {
            exceptionTypes[j] = TypeConverter.convertTypeToJava(exceptions[j]);
        }

        data.setExceptionTypes(exceptionTypes);

        return data;
    }

    /**
     * Create a new <code>FieldMetaData</code> based on the QDox
     * <code>JavaField</code passed as parameter.
     *
     * @param field the QDox field
     * @return the field meta-data
     */
    public static FieldMetaData createFieldMetaData(final JavaField field) {
        final FieldMetaData data = new FieldMetaData();

        data.setName(field.getName());
        data.setModifiers(TransformationUtil.
                getModifiersAsInt(field.getModifiers()));
        data.setType(TypeConverter.convertTypeToJava(field.getType()));

        return data;
    }
}
