/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;

import com.sun.javadoc.Tag;
import org.apache.xmlbeans.impl.jam.JAnnotation;
import org.apache.xmlbeans.impl.jam.annotation.JavadocTagParser;
import org.apache.xmlbeans.impl.jam.mutable.MAnnotatedElement;
import org.apache.xmlbeans.impl.jam.mutable.MAnnotation;
import org.apache.xmlbeans.impl.jam.provider.JamServiceContext;
import org.codehaus.aspectwerkz.util.Strings;
import java.util.Iterator;
import java.util.Map;

/**
 * Custom JAM Javadoc tag parser.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class CustomJavadocTagParser extends JavadocTagParser {
    private JamServiceContext m_ctx;

    /**
     * Initializes the parser.
     *
     * @param ctx
     */
    public void init(JamServiceContext ctx) {
        super.init(ctx);
        if (ctx == null) {
            throw new IllegalArgumentException("JAM context can not be null");
        }
        if (m_ctx != null) {
            throw new IllegalStateException("CustomJavadocTagParser.init(JamServiceContext) called twice");
        }
        m_ctx = ctx;
    }

    /**
     * Parses the annotation.
     *
     * @param target
     * @param tag
     */
    public void parse(MAnnotatedElement target, Tag tag) {
        MAnnotation[] anns = createAnnotations(target, tag);
        String tagText = Strings.removeFormattingCharacters(tag.text());
        String annotationName = tag.name();
        String annotationNameNoAt = annotationName.substring(1, annotationName.length());
        if (tagText == null) {
            return;
        }
        tagText = tagText.trim();
        if (tagText.length() == 0) {
            return;
        }

        // untyped system annotations
        for (int i = 0; i < AnnotationC.SYSTEM_ANNOTATIONS.length; i++) {
            String annotation = AnnotationC.SYSTEM_ANNOTATIONS[i];
            if (annotation.equals(annotationNameNoAt)) {
                for (int j = 0; j < anns.length; j++) {
                    anns[j].setSimpleValue(JAnnotation.SINGLE_VALUE_NAME, tagText, getStringType());
                }
            }
        }

        // user defined annotations (typed and untyped)
        for (Iterator it = AnnotationC.ANNOTATION_DEFINITION.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            String name = ((String)entry.getKey()).trim();
            if (name.equals(annotationNameNoAt)) {
                String className = ((String)entry.getValue()).trim();
                if (className.equals("")) {
                    // untyped
                    for (int j = 0; j < anns.length; j++) {
                        anns[j].setSimpleValue(JAnnotation.SINGLE_VALUE_NAME, tagText, getStringType());
                    }
                } else {
                    // typed
                    StringBuffer annotation = new StringBuffer();
                    annotation.append(tag.name());
                    annotation.append('(');
                    annotation.append(tagText);
                    annotation.append(')');
                    for (int j = 0; j < anns.length; j++) {
                        anns[j].setSimpleValue(JAnnotation.SINGLE_VALUE_NAME, annotation.toString(), getStringType());
                    }
                }
            }
        }
    }
}
