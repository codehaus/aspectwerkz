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
package org.codehaus.aspectwerkz.util;

import java.security.SecureRandom;
import java.net.InetAddress;

/**
 * Generates a UUID.<p/>
 * A Universally Unique Identifier (UUID) is a 128 bit number generated
 * according to an algorithm that is garanteed to be unique in time A
 * space from all other UUIDs.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: UuidGenerator.java,v 1.3 2003-07-03 13:10:50 jboner Exp $
 */
public class UuidGenerator {

    /**
     * Random seeder.
     */
    private static SecureRandom s_seeder = null;

    /**
     * Mid value, needed for calculation.
     */
    private static String s_midValue = null;

    /**
     * Defines if the generator is initialized or not.
     */
    private static boolean s_initialized = false;

    /**
     * Returns a unique uuid.
     *
     * @param obj the calling object (this)
     * @return a unique uuid
     */
    public static String generate(Object obj) {
        if (!s_initialized) initialize(obj);

        long timeNow = System.currentTimeMillis();

        // get int value as unsigned
        int timeLow = (int)timeNow & 0xFFFFFFFF;

        int node = s_seeder.nextInt();
        return (hexFormat(timeLow, 8) + s_midValue + hexFormat(node, 8));
    }

    /**
     * Initializes the factory.
     *
     * @param obj
     */
    private synchronized static void initialize(final Object obj) {
        try {
            InetAddress inet = InetAddress.getLocalHost();
            byte[] bytes = inet.getAddress();
            String hexInetAddress = hexFormat(getInt(bytes), 8);

            String thisHashCode = hexFormat(System.identityHashCode(obj), 8);
            s_midValue = hexInetAddress + thisHashCode;
            s_seeder = new SecureRandom();
            s_seeder.nextInt();
        } catch (java.net.UnknownHostException e) {
            throw new Error("can not initialize the UuidGenerator generator");
        }
        s_initialized = true;
    }

    /**
     * Utility method.
     *
     * @param abyte
     * @return
     */
    private static int getInt(final byte[] abyte) {
        int i = 0;
        int j = 24;
        for (int k = 0; j >= 0; k++) {
            int l = abyte[k] & 0xff;
            i += l << j;
            j -= 8;
        }
        return i;
    }

    /**
     * Utility method.
     *
     * @param i
     * @param j
     * @return
     */
    private static String hexFormat(final int i, final int j) {
        String s = Integer.toHexString(i);
        return padHex(s, j) + s;
    }

    /**
     * Utility method.
     *
     * @param str
     * @param i
     * @return
     */
    private static String padHex(final String str, final int i) {
        StringBuffer buf = new StringBuffer();
        if (str.length() < i) {
            for (int j = 0; j < i - str.length(); j++) {
                buf.append('0');
            }
        }
        return buf.toString();
    }

    /**
     * Private constructor to prevent subclassing
     */
    private UuidGenerator() {
    }
}
