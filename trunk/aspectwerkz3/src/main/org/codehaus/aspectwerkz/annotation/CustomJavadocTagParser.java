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
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.annotation.JavadocTagParser;
import org.apache.xmlbeans.impl.jam.internal.elements.ElementContext;
import org.apache.xmlbeans.impl.jam.mutable.MAnnotatedElement;
import org.apache.xmlbeans.impl.jam.mutable.MAnnotation;
import org.apache.xmlbeans.impl.jam.provider.JamServiceContext;
import org.codehaus.aspectwerkz.annotation.expression.DumpVisitor;
import org.codehaus.aspectwerkz.annotation.expression.ast.ExpressionParser;
import org.codehaus.aspectwerkz.annotation.expression.ast.ParseException;
import org.codehaus.aspectwerkz.util.Strings;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Custom JAM Javadoc tag parser.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class CustomJavadocTagParser extends JavadocTagParser {
    private JamServiceContext m_ctx;
    private ExpressionParser m_parser;

    public void init(JamServiceContext ctx) {
        super.init(ctx);
        if (ctx == null) {
            throw new IllegalArgumentException("JAM context can not be null");
        }
        if (m_ctx != null) {
            throw new IllegalStateException("CustomJavadocTagParser.init(JamServiceContext) called twice");
        }
        m_ctx = ctx;
        m_parser = new ExpressionParser(System.in); // can be only one
    }

    /**
     * Parses the tag.
     *
     * @param target
     * @param tag
     */
    public void parse(MAnnotatedElement target, Tag tag) {
        MAnnotation[] anns = createAnnotations(target, tag);
        String tagText = tag.text();
        tagText = Strings.removeFormattingCharacters(tagText);
        String annotation = null;

        //        try {
        //            annotation = tag.name() + "(\"" + tagText + "\")";
        ////            annotation = tag.name() + '(' + tagText + ')';
        //            System.out.println("annotation = " + annotation);
        //            DumpVisitor.dumpAST(m_parser.parse(annotation));
        //        } catch (ParseException e) {
        //            System.err.println("could not parse annotation: " + annotation);
        //        }
        if (tagText == null) {
            return;
        }
        tagText = tagText.trim();
        if (tagText.length() == 0) {
            return;
        }

        // first: grab the simple value (if there is one)
        tagText = Strings.removeFormattingCharacters(tagText);
        int firstEquals = tagText.indexOf('=');
        if (firstEquals > 0) {
            String firstNamedValue = tagText.substring(0, firstEquals);
            int lastSpace = firstNamedValue.lastIndexOf(' ');
            if (lastSpace > 0) {
                String simpleValue = firstNamedValue.substring(0, lastSpace);
                firstNamedValue = firstNamedValue.substring(lastSpace + 1, firstNamedValue.length());
                for (int i = 0; i < anns.length; i++) {
                    anns[i].setSimpleValue(JAnnotation.SINGLE_VALUE_NAME, simpleValue, getStringType());
                }
            }
            tagText = firstNamedValue + tagText.substring(firstEquals, tagText.length());
        }

        // second: handle the key:value pairs
        Properties props = new Properties();
        parseAssignments(props, tagText); //FIXME no need to create Properties here
        if (props.size() > 0) {
            Enumeration names = props.propertyNames();
            while (names.hasMoreElements()) {
                String name = (String)names.nextElement();
                String property = props.getProperty(name);
                if (property == null) {
                    continue;
                }
                setValue(anns, name, property);
            }
        } else {
            setSingleValueText(anns, tag);
        }
    }

    /**
     * Parse a line that contains assignments, taking into account - newlines (ignore them) - double quotes (the value
     * is everything in-between) - // (everything after is ignored) - multiple assignments on the same line
     * <p/>
     * This method contributed by Cedric Beust
     *
     * @param out  This variable will contain a list of properties representing the line once parsed.
     * @param line The line to be parsed
     */
    private void parseAssignments(Properties out, String line) {
        line = removeComments(line);
        while ((null != line) && (-1 != line.indexOf("="))) {
            int keyStart = -1;
            int keyEnd = -1;
            int ind = 0;

            // Skip stuff before the key
            char c = line.charAt(ind);
            while (isBlank(c)) {
                ind++;
                c = line.charAt(ind);
            }
            keyStart = ind;
            while (isLegal(line.charAt(ind))) {
                ind++;
            }
            keyEnd = ind;
            String key = line.substring(keyStart, keyEnd);
            ind = line.indexOf("=");
            if (ind == -1) {
                return; //FIXME let's be a little conservative, just for now

                //throw new IllegalStateException("'=' expected: "+line);
            }
            ind++;

            // Skip stuff after the equal sign
            try {
                c = line.charAt(ind);
            } catch (StringIndexOutOfBoundsException ex) {
                ex.printStackTrace();
            }
            while (isBlank(c)) {
                ind++;
                c = line.charAt(ind);
            }
            String value;
            int valueStart = -1;
            int valueEnd = -1;
            if (c == '"') {
                valueStart = ++ind;
                while ('"' != line.charAt(ind)) {
                    ind++;
                }
                valueEnd = ind;
            } else {
                valueStart = ind++;
                while ((ind < line.length()) && isLegal(line.charAt(ind))) {
                    ind++;
                }
                valueEnd = ind;
            }
            value = line.substring(valueStart, valueEnd);
            if (ind < line.length()) {
                line = line.substring(ind + 1);
            } else {
                line = null;
            }
            out.setProperty(key, value);
        }
    }

    /**
     * Remove all the texts between "//" and '\n'
     * <p/>
     * This method contributed by Cedric Beust
     */
    private String removeComments(String value) {
        String result = new String();
        int size = value.length();
        String current = value;
        int currentIndex = 0;
        int beginning = current.indexOf("//");

        // Ignore if it's between double quotes
        int doubleQuotesIndex = current.indexOf("\"");
        if ((-1 != doubleQuotesIndex) && (doubleQuotesIndex < beginning)) {
            // do nothing
            result = value;
        } else {
            while ((currentIndex < size) && (beginning != -1)) {
                beginning = value.indexOf("//", currentIndex);
                if (-1 != beginning) {
                    if ((beginning > 0) && (value.charAt(beginning - 1) == ':')) {
                        //this is a quick fix for problem of unquoted url values.  for
                        //now, just say it's not a comment if preceded by ':'.  should
                        //review this later
                        currentIndex = beginning + 2;
                        continue;
                    }
                    int end = value.indexOf('\n', beginning);
                    if (-1 == end) {
                        end = size;
                    }

                    // We have identified a portion to remove, copy the one we want to
                    // keep
                    result = result + value.substring(currentIndex, beginning).trim() + "\n";
                    current = value.substring(end);
                    currentIndex = end;
                }
            }
            result += current;
        }
        return result.trim();
    }

    private boolean isBlank(char c) {
        return (c == ' ') || (c == '\t') || (c == '\n');
    }

    private boolean isLegal(char c) {
        return (!isBlank(c)) && (c != '=');
    }
}
