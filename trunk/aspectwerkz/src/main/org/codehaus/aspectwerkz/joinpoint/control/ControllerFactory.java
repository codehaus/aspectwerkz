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
package org.codehaus.aspectwerkz.joinpoint.control;

import org.codehaus.aspectwerkz.ContextClassLoader;

/**
 * Factory for the join point controllers.
 *
 * @author <a href="mailto:"">Stefan Finkenzeller</a>
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
