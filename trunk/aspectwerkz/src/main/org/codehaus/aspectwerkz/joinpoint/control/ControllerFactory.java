/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.control;

import org.codehaus.aspectwerkz.ContextClassLoader;

/**
 * Factory for the join point controllers.
 *
 * @author <a href="mailto:stefan.finkenzeller@gmx.net">Stefan Finkenzeller</a>
 */
public class ControllerFactory {

    /**
     * The name of the default controller implementation class.
     */
    public static final String DEFAULT_CONTROLLER = "org.codehaus.aspectwerkz.joinpoint.control.DefaultJoinPointController";

    /**
     * Controller factory that creates joinpoint controllers to create the execution model
     * of advices.<P>
     *
     * The contract is that there will always be at least a DefaultJoinPointController meaning
     * joinpoints will always have a controller (never null)
     *
     * @param classname fully qualified class name of the controller class. If null or invalid, DefaultJoinPointController is returned
     * @return instance of a joinpointcontroller
     */
    public static final JoinPointController createController(String classname) {
        try {
            if (classname == null) classname = DEFAULT_CONTROLLER;
            Class cl = ContextClassLoader.loadClass(classname);
            return (JoinPointController)cl.newInstance();
        }
        catch (Exception e) {
            // TODO: log that the controller is not found and that the default is used
            return new DefaultJoinPointController();
        }
    }
}
