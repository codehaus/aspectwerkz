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
 * Creates a new object in the db.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: CreateTransaction.java,v 1.1 2003-06-17 16:23:24 jboner Exp $
 */
public class CreateTransaction implements Transaction {

    /**
     * The object to store.
     */
    private final Object m_object;

    /**
     * The index for the object to store.
     */
    private final Object m_index;

    /**
     * The deployment model for the object to store.
     */
    private final int m_deploymentModel;

    /**
     * Creates a new instance.
     *
     * @param object the object to store
     * @param index the index for the object
     * @param deploymentModel the deployment model for the object
     */
    public CreateTransaction(final Object object,
                             final Object index,
                             final int deploymentModel) {
        if (object == null) throw new IllegalArgumentException("object to store can not be null");
        if (deploymentModel != DeploymentModel.PER_JVM && index == null) throw new IllegalArgumentException("index can not be null");
        m_object = object;
        m_index = index;
        m_deploymentModel = deploymentModel;
    }

    /**
     * Executes the transaction.
     *
     * @param prevalentSystem the prevalent system where to store the object
     */
    public void executeOn(final Object prevalentSystem) {
        switch (m_deploymentModel) {

            case DeploymentModel.PER_JVM:
                ((PrevalentSystem)prevalentSystem).
                        createPerJvm(m_object);
                break;

            case DeploymentModel.PER_CLASS:
                ((PrevalentSystem)prevalentSystem).
                        createPerClass(m_object, m_index);
                break;

            case DeploymentModel.PER_INSTANCE:
                ((PrevalentSystem)prevalentSystem).
                        createPerInstance(m_object, m_index);
                break;

            default:
                throw new IllegalArgumentException("invalid deployment model type");
        }
    }
}
