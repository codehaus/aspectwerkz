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
package org.codehaus.aspectwerkz.persistence;

import java.lang.reflect.Field;

import org.codehaus.aspectwerkz.MetaDataKeys;
import org.codehaus.aspectwerkz.MetaDataEnhanceable;
import org.codehaus.aspectwerkz.advice.PostAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.FieldJoinPoint;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

/**
 * This advice checks for dirty fields and if found updates the dirty object
 * in the db.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: DirtyFieldCheckAdvice.java,v 1.2 2003-06-09 07:04:13 jboner Exp $
 */
public final class DirtyFieldCheckAdvice extends PostAdvice {

    /**
     * The unique name for the advice.
     */
    public static final String NAME = "aspectwerkz/persistence/DirtyFieldCheckAdvice";

    /**
     * The class name for the advice.
     */
    public static final String CLASS = "org.codehaus.aspectwerkz.persistence.DirtyFieldCheckAdvice";

    /**
     * The pattern for the fields it should apply to.
     */
    public static final String PATTERN = "* *";

    /**
     * The persistence manager to use.
     */
    private static PersistenceManager s_persistenceManager;

    /**
     * Loads the persistence manager.
     */
    static {
        s_persistenceManager = PersistenceManagerFactory.getFactory(
                PersistenceManagerFactory.getPersistenceManagerType()).
                createPersistenceManager();
    }

    /**
     * Creates a new instance.
     */
    public DirtyFieldCheckAdvice() {
        super();
    }

    /**
     * Is executed when a field has become dirty.
     *
     * @param joinPoint the current join point
     */
    public void execute(final JoinPoint joinPoint) {
        FieldJoinPoint jp = (FieldJoinPoint)joinPoint;

        String fieldName = jp.getFieldName();
        Object targetObject = jp.getTargetObject();
        Class targetClass = jp.getTargetClass();

        Object index = ((MetaDataEnhanceable)targetObject).
                ___hidden$getMetaData(MetaDataKeys.TARGET_OBJECT_UUID);

        Object aspectComponentUuid = ((MetaDataEnhanceable)targetObject).
                ___hidden$getMetaData(MetaDataKeys.ASPECT_COMPONENT_UUID);

        int deploymentModel = ((Integer)((MetaDataEnhanceable)targetObject).
                ___hidden$getMetaData(MetaDataKeys.DEPLOYMENT_MODEL)).intValue();

        Object newValue = null;
        try {
            Field field = targetClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            newValue = field.get(targetObject);
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }

        s_persistenceManager.update(
                new ModifiedField(fieldName, newValue),
                index,
                aspectComponentUuid,
                deploymentModel);
    }
}
