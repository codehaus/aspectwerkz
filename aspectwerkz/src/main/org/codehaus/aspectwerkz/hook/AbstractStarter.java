/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.hook;

import java.io.IOException;
import java.io.File;

/**
 * Base class for JVM Process based starter.
 *
 * Base implementation to lauch a JVM given java options, main class and args
 * in a separate process.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @version $Id: AbstractStarter.java,v 1.2 2003-07-23 14:20:32 avasseur Exp $
 */
abstract class AbstractStarter {

    protected String opt;

    protected String main;

    protected AbstractStarter(String opt, String main) {
        this.opt = opt;
        this.main = main;
    }

    /**
     * return command line that launched the target process
     */
    public String getCommandLine() {
        StringBuffer command = new StringBuffer();
        command.append(System.getProperty("java.home"));
        command.append(File.separatorChar).append("bin").append(File.separatorChar).append("java");
        command.append(" ").append(opt);
        command.append(" ").append(main);
        return command.toString();
    }

    /**
     * launchs target process
     */
    public Process launchVM() throws IOException {
        System.out.println(getCommandLine());
        return Runtime.getRuntime().exec(getCommandLine());
    }

}
