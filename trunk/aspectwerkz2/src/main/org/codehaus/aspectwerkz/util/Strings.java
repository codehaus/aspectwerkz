/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
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
     * @param str      the string to search and replace in
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
     * @param str      the string to search and replace in
     * @param oldToken the string to search for
     * @param newToken the string to replace newToken
     * @param max      maximum number of values to replace (-1 => no maximum)
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
     * String split on multicharacter delimiter.
     * <p/>
     * Written by Tim Quinn (tim.quinn@honeywell.com)
     */
    public static final String[] splitString(String sS, String sD) {
        String[] aRet;
        int iLast, iFrom, iFound, iRecords;

        //Return Blank Array if sS == "")
        if (sS.equals(""))
            return new String[0];

        //Count Field Entries
        iFrom = 0;
        iRecords = 0;
        while (true) {
            iFound = sS.indexOf(sD, iFrom);
            if (iFound == -1) {
                break;
            }
            iRecords++;
            iFrom = iFound + sD.length();
        }
        iRecords = iRecords + 1;

        // Populate aRet[]
        aRet = new String[iRecords];
        if (iRecords == 1) {
            aRet[0] = sS;
        } else {
            iLast = 0;
            iFrom = 0;
            iFound = 0;
            for (int i = 0; i < iRecords; i++) {
                iFound = sS.indexOf(sD, iFrom);
                if (iFound == -1) //At End
                    aRet[i] = sS.substring(iLast + sD.length(), sS.length());
                else if (iFound == 0) //At Beginning
                    aRet[i] = "";
                else //Somewhere in middle
                    aRet[i] = sS.substring(iFrom, iFound);
                iLast = iFound;
                iFrom = iFound + sD.length();
            }
        }
        return aRet;
    }

    /**
     * Private constructor to prevent instantiability.
     */
    private Strings() {
    }
}
