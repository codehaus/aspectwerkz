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
package org.codehaus.aspectwerkz.extension.persistence;

import org.codehaus.aspectwerkz.extension.persistence.PersistenceManager;
import org.codehaus.aspectwerkz.extension.service.ServiceManager;
import org.codehaus.aspectwerkz.extension.service.ServiceType;

import org.codehaus.aspectwerkz.advice.PostAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.FieldJoinPoint;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

/**
 * This advice checks for dirty fields and if found updates the dirty object
 * in the db.
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: DirtyFieldCheckAdvice.java,v 1.1.1.1 2003-05-11 15:13:12 jboner Exp $
 */
public final class DirtyFieldCheckAdvice extends PostAdvice {

    /**
     * The unique name for the advice.
     */
    public static final String NAME =
            "aspectwerkz/extension/persistence/DirtyFieldCheckAdvice";

    /**
     * The class name for the advice.
     */
    public static final String CLASS =
            "org.codehaus.aspectwerkz.extension.persistence.DirtyFieldCheckAdvice";

    /**
     * The pattern for the fields it should apply to.
     */
    public static final String PATTERN = ".*";

    /**
     * The persistence manager to use.
     */
    private static PersistenceManager s_persistenceManager;

    /**
     * Creates a new instance.
     */
    public DirtyFieldCheckAdvice() {
        super();
        s_persistenceManager = (PersistenceManager)ServiceManager.
                getService(ServiceType.PERSISTENCE_MANAGER);
    }

    /**
     * Is executed when a field has become dirty.
     *
     * @param joinPoint the current join point
     */
    public void execute(final JoinPoint joinPoint) {
        FieldJoinPoint jp = (FieldJoinPoint)joinPoint;
        try {
            s_persistenceManager.store(jp.getTargetObject());
        }
        catch (PersistenceManagerException e) {
            throw new WrappedRuntimeException(e);
        }
    }
}
