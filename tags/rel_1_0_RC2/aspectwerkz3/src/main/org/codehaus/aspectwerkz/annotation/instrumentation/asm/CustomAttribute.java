/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation.instrumentation.asm;

import org.codehaus.aspectwerkz.annotation.instrumentation.AttributeEnhancer;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ByteVector;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;

/**
 * Custom annotation wrapper annotation.
 * This Java 1.5 style annotation is used to wrap user defined / 1.3 / 1.4 annotations instead of using a custom attribute.
 * This allow to add user defined annotations as RuntimeInvisibleAnnotations and thus to support several annotations and
 * annotations introduction.
 * </br>
 * See AW-234.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public interface CustomAttribute {

    public final static String TYPE = "Lorg/codehaus/aspectwerkz/annotation/instrumentation/asm/CustomAttribute;";

    /**
     * Default value is a String, containing the BASE64 encoding of the serialized data of the user
     * custom annotation (proxy instance).
     *
     * @return
     */
    public String value();

}