/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.web;

import aspectwerkz.aosd.ServiceManager;

import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;

import org.codehaus.aspectwerkz.joinpoint.management.JoinPointManager;

/**
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class InitServlet extends HttpServlet {

    public void init() throws ServletException {
        super.init();
        System.out.println("Start services");
        ServiceManager.startServices();
    }

    private static Class me;
    private static JoinPointManager jpm;
    static {
        try {
        me = Class.forName("aspectwerkz.aosd.web.InitServlet");

        System.out.println("got " + me);

        jpm = JoinPointManager.getJoinPointManager(me, "aosd");

            System.out.println("got " + jpm);

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
