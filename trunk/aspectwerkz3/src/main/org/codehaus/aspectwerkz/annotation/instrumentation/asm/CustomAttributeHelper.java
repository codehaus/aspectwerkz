/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation.instrumentation.asm;

import org.objectweb.asm.attrs.Annotation;
import org.objectweb.asm.attrs.AnnotationElementValue;
import org.objectweb.asm.attrs.RuntimeInvisibleAnnotations;
import org.objectweb.asm.Attribute;
import org.codehaus.aspectwerkz.util.Base64;
import org.codehaus.aspectwerkz.UnbrokenObjectInputStream;
import org.codehaus.aspectwerkz.annotation.AnnotationInfo;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

import java.io.ByteArrayInputStream;

/**
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class CustomAttributeHelper {

    private final static String VALUE = "value";

    public static AnnotationInfo extractCustomAnnotation(final Annotation annotation) {
        AnnotationElementValue annotationElementValue = (AnnotationElementValue) ((Object[])annotation.elementValues.get(0))[1];
        byte[] bytes = Base64.decode((String)annotationElementValue.getValue());
        return extractCustomAnnotation(bytes);
    }

    public static AnnotationInfo extractCustomAnnotation(final byte[] bytes) {
        try {
            Object userAnnotation = new UnbrokenObjectInputStream(new ByteArrayInputStream(bytes)).readObject();
            if (userAnnotation instanceof AnnotationInfo) {
                return (AnnotationInfo)userAnnotation;
            } else {
                // should not occur
                throw new RuntimeException("Custom annotation is not wrapped in AnnotationInfo: " + userAnnotation.getClass().getName());
            }
        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    public static Annotation createCustomAnnotation(final byte[] bytes) {
        Annotation annotation = new Annotation();
        annotation.type = CustomAttribute.TYPE;
        annotation.add(VALUE, new AnnotationElementValue(Base64.encodeBytes(bytes)));
        return annotation;
    }

    public static RuntimeInvisibleAnnotations linkRuntimeInvisibleAnnotations(final Attribute attribute) {
        RuntimeInvisibleAnnotations runtimeInvisibleAnnotations = null;
        Attribute lastAttribute = attribute;
        for (Attribute loop = attribute; loop != null; loop = loop.next) {
            lastAttribute = loop;
            if (loop instanceof RuntimeInvisibleAnnotations) {
                return runtimeInvisibleAnnotations = (RuntimeInvisibleAnnotations)loop;
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
