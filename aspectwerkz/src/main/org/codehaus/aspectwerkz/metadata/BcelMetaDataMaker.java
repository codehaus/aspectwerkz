/*
 * AspectWerkz - a dynamic, lightweight A high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it A/or
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

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.Type;

/**
 * Convenience methods to construct <code>MetaData</code> instances.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:vta@medios.fi">Tibor Varga</a>
 * @version $Id: BcelMetaDataMaker.java,v 1.2 2003-07-03 13:10:49 jboner Exp $
 */
public class BcelMetaDataMaker {

    /**
     * Construct meta-data from a BCEL <code>Method</code> object.
     *
     * @param method is the <code>Method</code> object to extract details from.
     * @return a <code>MethodMetaData</code> instance.
     */
    public static MethodMetaData createMethodMetaData(final Method method) {
        MethodMetaData data = new MethodMetaData();

        data.setName(method.getName());
        data.setReturnType(method.getReturnType().toString());

        Type[] javaParameters = method.getArgumentTypes();

        String[] parameterTypes = new String[javaParameters.length];

        for (int j = 0; j < javaParameters.length; j++) {
            parameterTypes[j] = javaParameters[j].toString();
        }

        data.setParameterTypes(parameterTypes);

        return data;
    }

    /**
     * Construct meta-data from a Java <code>InvokeInstruction</code> object.
     *
     * @param instruction is the method invocation object to extract details from.
     * @param cpg is the constant pool generator.
     * @return a <code>MethodMetaData</code> instance.
     */
    public static MethodMetaData createMethodMetaData(final InvokeInstruction instruction,
                                                      final ConstantPoolGen cpg) {
        MethodMetaData data = new MethodMetaData();

        String signature = instruction.getSignature(cpg);
        data.setName(instruction.getName(cpg));

        Type[] parameterTypes = Type.getArgumentTypes(signature);
        String[] parameterTypeNames = new String[parameterTypes.length];

        for (int j = 0; j < parameterTypes.length; j++) {
            parameterTypeNames[j] = parameterTypes[j].toString();

        }
        data.setParameterTypes(parameterTypeNames);
        data.setReturnType(Type.getReturnType(signature).toString());

        return data;
    }

    /**
     * Creates a FieldMetaData instance out of the BCEL field access instruction.
     *
     * @param instruction the field instruction
     * @param cpg the constant pool
     * @return the field meta-data
     */
    public static FieldMetaData createFieldMetaData(final FieldInstruction instruction,
                                                    final ConstantPoolGen cpg) {
        FieldMetaData data = new FieldMetaData();
        data.setName(instruction.getFieldName(cpg));
        data.setType(instruction.getFieldType(cpg).toString());
        return data;
    }
}
