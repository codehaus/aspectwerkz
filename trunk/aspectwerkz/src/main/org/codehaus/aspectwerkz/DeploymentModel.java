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
package org.codehaus.aspectwerkz;

import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;

/**
 * Enum containing the different deployment model types.
 * Used to be type-safe but that added to much overhead (0.00004 ms/call)
 * compared to the current implementation.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: DeploymentModel.java,v 1.3 2003-07-03 13:10:49 jboner Exp $
 */
public final class DeploymentModel {
    public static final int PER_JVM = 0;
    public static final int PER_CLASS = 1;
    public static final int PER_INSTANCE = 2;
    public static final int PER_THREAD = 3;

    /**
     * Returns the deployment model for the string type.
     *
     * @param type the string type
     * @return the matched deployment type
     */
    public static int getDeploymentModelAsInt(final String type) {
        if (type == null || type.equals(AspectWerkzDefinition.PER_JVM)) {
            return PER_JVM;
        }
        else if (type.equals(AspectWerkzDefinition.PER_CLASS)) {
            return PER_CLASS;
        }
        else if (type.equals(AspectWerkzDefinition.PER_INSTANCE)) {
            return PER_INSTANCE;
        }
        else if (type.equals(AspectWerkzDefinition.PER_THREAD)) {
            return PER_THREAD;
        }
        else {
            throw new RuntimeException("invalid deployment model: " + type);
        }
    }
}
