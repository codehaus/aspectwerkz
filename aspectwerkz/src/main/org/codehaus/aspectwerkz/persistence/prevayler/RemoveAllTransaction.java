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
package org.codehaus.aspectwerkz.persistence.prevayler;

import org.prevayler.Transaction;
import org.codehaus.aspectwerkz.DeploymentModel;

/**
 * Removes all the objects housed by a speficic aspect component from the db.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: RemoveAllTransaction.java,v 1.2 2003-06-09 07:04:13 jboner Exp $
 */
public class RemoveAllTransaction implements Transaction {

    /**
     * The UUID for the aspect component holding the object to remove.
     */
    private final Object m_aspectComponentUuid;

    /**
     * The deployment model for the object to remove.
     */
    private final int m_deploymentModel;

    /**
     * Creates a new instance.
     *
     * @param aspectComponentUuid the modified field
     * @param deploymentModel the deployment model for the object
     */
    public RemoveAllTransaction(final Object aspectComponentUuid,
                                final int deploymentModel) {
        if (aspectComponentUuid == null) throw new IllegalArgumentException("aspect component UUID can not be null");
        m_aspectComponentUuid = aspectComponentUuid;
        m_deploymentModel = deploymentModel;
    }

    /**
     * Performs the removal of the objects.
     *
     * @param prevalentSystem the prevalent system holding the object
     */
    public void executeOn(final Object prevalentSystem) {
        switch (m_deploymentModel) {

            case DeploymentModel.PER_JVM:
                ((PrevalentSystem)prevalentSystem).
                        removeAllPerJvm(m_aspectComponentUuid);
                break;

            case DeploymentModel.PER_CLASS:
                ((PrevalentSystem)prevalentSystem).
                        removeAllPerClass(m_aspectComponentUuid);
                break;

            case DeploymentModel.PER_INSTANCE:
                ((PrevalentSystem)prevalentSystem).
                        removeAllPerInstance(m_aspectComponentUuid);
                break;

            default:
                throw new IllegalArgumentException("invalid deployment model type");
        }
    }
}
