/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.hook;

import java.util.StringTokenizer;

/**
 * Starts a target process adding a dir in -Xbootclasspath/p: option
 *
 * Target process is launched using <i>
 * $JAVA_HOME/bin/java [opt] [main]</i><br/>
 * and [opt] is patched to use [bootDir] in -Xbootclasspath/p: option.<br/>
 * This is suitable for java 1.3.<br/>
 * This can be use with java 1.4 to avoid running in JDWP mode.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @version $Id: BootClasspathStarter.java,v 1.2 2003-07-23 14:20:32 avasseur Exp $
 */
public class BootClasspathStarter extends AbstractStarter {

    private String bootDir;

    public BootClasspathStarter(String opt, String main, String bootDir) {
        super(opt, main);
        this.bootDir = bootDir;

        patchBootclasspath();
    }

    /**
     * add dir in first position of -Xbootclasspath/p option for target VM
     */
    private void patchBootclasspath() {
        // prepend dir in -Xbootclasspath/p:
        if (opt.indexOf("-Xbootclasspath/p:") < 0) {
            opt = "-Xbootclasspath/p:"+bootDir+" "+opt;
        } else {
            StringBuffer newOptionsB = new StringBuffer();
            StringTokenizer parser = new StringTokenizer(opt, " ");
            while (parser.hasMoreTokens()) {
                String elem = parser.nextToken();
                if (elem.startsWith("-Xbootclasspath/p:")) {
                    newOptionsB.append("-Xbootclasspath/p:").append(bootDir);
                    newOptionsB.append((System.getProperty("os.name","").toLowerCase().indexOf("windows")>=0)?";":":");
                    newOptionsB.append(elem.substring("-Xbootclasspath/p:".length()+elem.indexOf("-Xbootclasspath/p:")));
                } else {
                    newOptionsB.append(elem);
                }
                newOptionsB.append(" ");
            }
            opt = newOptionsB.toString();
        }
    }
}
