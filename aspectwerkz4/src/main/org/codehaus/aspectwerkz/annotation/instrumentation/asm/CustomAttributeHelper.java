/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation.instrumentation.asm;

import org.objectweb.asm.attrs.Annotation;
import org.objectweb.asm.attrs.RuntimeInvisibleAnnotations;
import org.objectweb.asm.Attribute;
import org.codehaus.aspectwerkz.util.Base64;
import org.codehaus.aspectwerkz.util.UnbrokenObjectInputStream;
import org.codehaus.aspectwerkz.annotation.AnnotationInfo;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

import java.io.ByteArrayInputStream;

/**
 * Helper class to wrap a custom annotation proxy (1.3/1.4 javadoc annotation) in a RuntimeInvisibleAnnotations.
 * <br/>
 * The proxy is wrapped in a AnnotationInfo object which is serialized
 * and base64 encoded (ASM issue on array types in RIV).
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class CustomAttributeHelper {

    /**
     * Annotation parameter - as if it was a single value Tiger annotation
     */
    private final static String VALUE = "value";

    /**
     * Extract the AnnotationInfo from the bytecode Annotation representation.
     *
     * @param annotation must be a valid RIV, of type CustomAttribute.TYPE
     * @return
     */
    public static AnnotationInfo extractCustomAnnotation(final Annotation annotation) {
        byte[] bytes = Base64.decode((String) ((Object[]) annotation.elementValues.get(0))[1]);
        return extractCustomAnnotation(bytes);
    }

    /**
     * Extract the AnnotationInfo from the base64 encoded serialized version.
     *
     * @param bytes
     * @return
     */
    private static AnnotationInfo extractCustomAnnotation(final byte[] bytes) {
        try {
            Object userAnnotation = new UnbrokenObjectInputStream(new ByteArrayInputStream(bytes)).readObject();
            if (userAnnotation instanceof AnnotationInfo) {
                return (AnnotationInfo)userAnnotation;
            } else {
                // should not occur
                throw new RuntimeException(
                        "Custom annotation is not wrapped in AnnotationInfo: " + userAnnotation.getClass().getName() +
                        " [" + AnnotationInfo.class.getClassLoader().toString() + " / " +
                        userAnnotation.getClass().getClassLoader().toString() + " / " +
                        Thread.currentThread().getContextClassLoader()
                );
            }
        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Create an Annotation bytecode representation from the serialized version of the custom annotation proxy
     *
     * @param bytes
     * @return
     */
    public static Annotation createCustomAnnotation(final byte[] bytes) {
        Annotation annotation = new Annotation();
        annotation.type = CustomAttribute.TYPE;
        annotation.add(VALUE, Base64.encodeBytes(bytes));
        return annotation;
    }

    /**
     * Helper method to find the first RuntimeInvisibleAnnotations attribute in an Attribute chain.
     * <br/>If no such RIV exists, a new one is created (empty) and added last in the chain.
     * <br/>If the chain is null, a new sole RIV (empty) is created
     *
     * @param attribute
     * @return the RuntimeInvisibleAnnotations to add Annotation to
     */
    public static RuntimeInvisibleAnnotations linkRuntimeInvisibleAnnotations(final Attribute attribute) {
        RuntimeInvisibleAnnotations runtimeInvisibleAnnotations = null;
        Attribute lastAttribute = attribute;
        for (Attribute loop = attribute; loop != null; loop = loop.next) {
            lastAttribute = loop;
            if (loop instanceof RuntimeInvisibleAnnotations) {
                return runtimeInvisibleAnnotations = (RuntimeInvisibleAnnotations) loop;
            }
        }
        // not found, link a new one to lastAttribute
        runtimeInvisibleAnnotations = new RuntimeInvisibleAnnotations();
        runtimeInvisibleAnnotations.next = null;
        if (attribute != null) {
            // if arg is null, we are just adding this annotation as the sole attribute
            lastAttribute.next = runtimeInvisibleAnnotations;
        } else {
            //attribute = runtimeInvisibleAnnotations;
        }
        return runtimeInvisibleAnnotations;
    }
}
