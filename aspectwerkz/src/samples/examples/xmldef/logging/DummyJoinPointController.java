/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bon�r. All rights reserved.
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
package examples.logging;

import org.codehaus.aspectwerkz.joinpoint.MethodJoinPoint;
import org.codehaus.aspectwerkz.joinpoint.control.DefaultJoinPointController;
import org.codehaus.aspectwerkz.joinpoint.control.JoinPointController;

/**
 * Some other joinpoint controller for testing
 *
 * @author <a href="mailto:"">Stefan Finkenzeller</a>
 */
public class DummyJoinPointController extends DefaultJoinPointController {

    public int redundancies = -1;

    public void removeRedundancies(final MethodJoinPoint joinPoint) {
        if (redundancies == -1) {
            redundancies = clearAllRedundancies(joinPoint);
            System.out.println("DummyJoinPointController removed " + redundancies + " redundant advices on join point for method <" + joinPoint.getMethodName() + ">");
        }
    }

    public Object proceed(MethodJoinPoint jp) throws Throwable {
        removeRedundancies(jp);

        return super.proceed(jp);
    }

    /**
     * Clones the controller
     */
    public JoinPointController deepCopy() {
        DummyJoinPointController clone = new DummyJoinPointController();
        clone.m_currentAdviceIndex = m_currentAdviceIndex;
        clone.m_currentPointcutIndex = m_currentPointcutIndex;
        return clone;
    }

}
