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
package org.codehaus.aspectwerkz.util;

/**
 * Utility methods for strings.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: Strings.java,v 1.1 2003-07-19 20:36:17 jboner Exp $
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
