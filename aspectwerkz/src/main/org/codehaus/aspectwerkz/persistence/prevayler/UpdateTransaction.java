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

import java.io.Serializable;

import org.prevayler.Transaction;

import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.util.SerializationUtils;
import org.codehaus.aspectwerkz.persistence.prevayler.PrevalentSystem;
import org.codehaus.aspectwerkz.persistence.ModifiedField;

/**
 * Updates an object in the db.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: UpdateTransaction.java,v 1.3 2003-06-09 07:04:13 jboner Exp $
 */
public class UpdateTransaction implements Transaction {

    /**
     * The modified field.
     */
    private final ModifiedField m_modifiedField;

    /**
     * The index for the object to update.
     */
    private final Object m_index;

    /**
     * The deployment model for the object to store.
     */
    private final int m_deploymentModel;

    /**
     * Creates a new instance.
     *
     * @param modifiedField the modified field
     * @param index the index for the object to update
     * @param deploymentModel the deployment model for the object
     */
    public UpdateTransaction(final ModifiedField modifiedField,
                             final Object index,
                             final int deploymentModel) {
        if (modifiedField == null) throw new IllegalArgumentException("modified field can not be null");
        if (deploymentModel != DeploymentModel.PER_JVM && index == null) throw new IllegalArgumentException("index can not be null");

        // make a deep copy of the modified field
        m_modifiedField = new ModifiedField(modifiedField.getName(),
                SerializationUtils.clone((Serializable)modifiedField.getValue()));
        m_index = index;
        m_deploymentModel = deploymentModel;
    }

    /**
     * Performs the update.
     *
     * @param prevalentSystem the prevalent system holding the object
     */
    public void executeOn(final Object prevalentSystem) {
        switch (m_deploymentModel) {

            case DeploymentModel.PER_JVM:
                ((PrevalentSystem)prevalentSystem).
                        updatePerJvm(m_modifiedField);
                break;

            case DeploymentModel.PER_CLASS:
                ((PrevalentSystem)prevalentSystem).
                        updatePerClass(m_modifiedField, m_index);
                break;

            case DeploymentModel.PER_INSTANCE:
                ((PrevalentSystem)prevalentSystem).
                        updatePerInstance(m_modifiedField, m_index);
                break;

            default:
                throw new IllegalArgumentException("invalid deployment model type");
        }
    }
}
