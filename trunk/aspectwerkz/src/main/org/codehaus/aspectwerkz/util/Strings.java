/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.util;

/**
 * Utility methods for strings.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class Strings {

    /**
     * Replaces all occurences of a substring inside a string.
     *
     * @param str the string to search and replace in
     * @param oldToken the string to search for
     * @param newToken the string to replace newToken
     * @return the new string
     */
    public static String replaceSubString(final String str,
                                          final String oldToken,
                                          final String newToken) {
        return replaceSubString(str, oldToken, newToken, -1);
    }

    /**
     * Replaces all occurences of a substring inside a string.
     *
     * @param str the string to search and replace in
     * @param oldToken the string to search for
     * @param newToken the string to replace newToken
     * @param max maximum number of values to replace (-1 => no maximum)
     * @return the new string
     */
    public static String replaceSubString(final String str,
                                          final String oldToken,
                                          final String newToken,
                                          int max) {
        if (str == null || oldToken == null || newToken == null || oldToken.length() == 0) {
            return str;
        }

        StringBuffer buf = new StringBuffer(str.length());
        int start = 0, end = 0;
        while ((end = str.indexOf(oldToken, start)) != -1) {
            buf.append(str.substring(start, end)).append(newToken);
            start = end + oldToken.length();

            if (--max == 0) {
                break;
            }
        }
        buf.append(str.substring(start));
        return buf.toString();
    }

    /**
     * Private constructor to prevent instantiability.
     */
    private Strings() {
    }
}
