/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
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

}
