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
package examples.caching;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: Pi.java,v 1.4 2003-06-17 15:50:57 jboner Exp $
 */
public class Pi {

    /**
     * @advice:callerside callerclass=examples.caching.* invocation_counter
     */
    public static int getPiDecimal(int n) {
        System.out.println("using method");
        String decimals = "141592653";
        if (n > decimals.length()) {
            return 0;
        }
        else {
            return Integer.parseInt(decimals.substring(n, n + 1));
        }
    }
}
