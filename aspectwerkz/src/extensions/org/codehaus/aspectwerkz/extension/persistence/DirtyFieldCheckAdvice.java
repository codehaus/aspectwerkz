/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.extension.persistence;

import org.codehaus.aspectwerkz.extension.persistence.PersistenceManager;
import org.codehaus.aspectwerkz.extension.service.ServiceManager;
import org.codehaus.aspectwerkz.extension.service.ServiceType;

import org.codehaus.aspectwerkz.xmldef.advice.PostAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.FieldJoinPoint;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

/**
 * This advice checks for dirty fields A if found updates the dirty object
 * in the db.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public final class DirtyFieldCheckAdvice extends PostAdvice {

    /**
     * The unique name for the advice.
     */
    public static final String NAME =
            "aspectwerkz_extension_persistence_DirtyFieldCheckAdvice";

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
            s_persistenceManager.store(jp.getTargetInstance());
        }
        catch (PersistenceManagerException e) {
            throw new WrappedRuntimeException(e);
        }
    }
}
