/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.aspect;

import org.codehaus.aspectwerkz.CrossCuttingInfo;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Implements the default aspect container strategy.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class DefaultAspectContainerStrategy extends AbstractAspectContainer {
    /**
     * The constructor for the aspect.
     */
    protected Constructor m_aspectConstructor = null;

    /**
     * Creates a new aspect container strategy.
     * 
     * @param crossCuttingInfo the cross-cutting info
     */
    public DefaultAspectContainerStrategy(final CrossCuttingInfo crossCuttingInfo) {
        super(crossCuttingInfo);
    }

    /**
     * Creates a new aspect instance.
     * 
     * @return the new aspect instance
     */
    protected Object createAspect() {
        if (m_aspectConstructor == null) {
            m_aspectConstructor = findConstructor();
        }
        try {
            switch (m_constructionType) {
                case ASPECT_CONSTRUCTION_TYPE_DEFAULT:
                    return m_aspectConstructor.newInstance(EMPTY_OBJECT_ARRAY);
                case ASPECT_CONSTRUCTION_TYPE_CROSS_CUTTING_INFO:
                    return m_aspectConstructor.newInstance(arrayWithSingleCrossCuttingInfo);
                default:
                    throw new RuntimeException(
                        "aspect ["
                            + m_aspectPrototype.getClass().getName()
                            + "] does not have a valid constructor (either default no-arg or one that takes a CrossCuttingInfo type as its only parameter)");
            }
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new WrappedRuntimeException(e.getTargetException());
        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Grabs the correct constructor for the aspect.
     * 
     * @return the constructor for the aspect
     */
    protected Constructor findConstructor() {
        Constructor aspectConstructor = null;
        Class aspectClass = m_infoPrototype.getAspectClass();
        Constructor[] constructors = aspectClass.getDeclaredConstructors();
        for (int i = 0; i < constructors.length; i++) {
            Constructor constructor = constructors[i];
            Class[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length == 0) {
                m_constructionType = ASPECT_CONSTRUCTION_TYPE_DEFAULT;
                aspectConstructor = constructor;
            } else if ((parameterTypes.length == 1)
                && parameterTypes[0].equals(CrossCuttingInfo.class)) {
                m_constructionType = ASPECT_CONSTRUCTION_TYPE_CROSS_CUTTING_INFO;
                aspectConstructor = constructor;
                break;
            }
        }
        if (m_constructionType == ASPECT_CONSTRUCTION_TYPE_UNKNOWN) {
            throw new RuntimeException(
                "aspect ["
                    + aspectClass.getName()
                    + "] does not have a valid constructor (either default no-arg or one that takes a CrossCuttingInfo type as its only parameter)");
        }
        return aspectConstructor;
    }
}