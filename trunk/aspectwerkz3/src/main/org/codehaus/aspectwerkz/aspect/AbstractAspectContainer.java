/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.aspect;

import org.codehaus.aspectwerkz.CrossCuttingInfo;
import org.codehaus.aspectwerkz.transform.ReflectHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.List;
import java.lang.reflect.Method;

/**
 * Abstract base class for the aspect container implementations.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @FIXME remove the prototype pattern impl
 * @FIXME rename createAspect methods to getAspect or aspectOf(..)
 */
public abstract class AbstractAspectContainer implements AspectContainer {

    public static final int ASPECT_CONSTRUCTION_TYPE_UNKNOWN = 0;
    public static final int ASPECT_CONSTRUCTION_TYPE_DEFAULT = 1;
    public static final int ASPECT_CONSTRUCTION_TYPE_CROSS_CUTTING_INFO = 2;
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[]{};

    /**
     * An array with the single cross-cutting info, needed to save one array creation per invocation.
     */
    protected final Object[] ARRAY_WITH_SINGLE_CROSS_CUTTING_INFO = new Object[1];

    /**
     * The aspect construction type.
     */
    protected int m_constructionType = ASPECT_CONSTRUCTION_TYPE_UNKNOWN;

    /**
     * Introduction container containing introduction declared by this aspect, keys by introduction names
     */
    protected final Map m_introductionContainers = new HashMap();

    /**
     * The cross-cutting info prototype.
     */
    protected final CrossCuttingInfo m_infoPrototype;

    /**
     * The aspect instance prototype.
     */
    protected final Object m_aspectPrototype;

    /**
     * Holds a reference to the sole per JVM aspect instance.
     */
    protected Object m_perJvm;

    /**
     * Holds references to the per class aspect instances.
     */
    protected final Map m_perClass = new WeakHashMap();

    /**
     * Holds references to the per instance aspect instances.
     */
    protected final Map m_perInstance = new WeakHashMap();

    /**
     * Holds references to the per thread aspect instances.
     */
    protected final Map m_perThread = new WeakHashMap();

    /**
     * The advice repository.
     */
    protected Method[] m_adviceRepository = new Method[0];

    /**
     * Creates a new aspect container strategy.
     *
     * @param crossCuttingInfo the cross-cutting info
     */
    public AbstractAspectContainer(final CrossCuttingInfo crossCuttingInfo) {
        if (crossCuttingInfo == null) {
            throw new IllegalArgumentException("cross-cutting info can not be null");
        }
        m_infoPrototype = crossCuttingInfo;
        ARRAY_WITH_SINGLE_CROSS_CUTTING_INFO[0] = m_infoPrototype;
        m_aspectPrototype = createAspect();
        createAdviceRepository();
    }

    /**
     * Returns the cross-cutting info.
     *
     * @return the cross-cutting info
     */
    public CrossCuttingInfo getCrossCuttingInfo() {
        return m_infoPrototype;
    }

    /**
     * Creates a new perJVM cross-cutting instance, if it already exists then return it.
     *
     * @return the cross-cutting instance
     */
    public Object createPerJvmAspect() {
        if (m_perJvm == null) {
            m_perJvm = createAspect();
        }
        return m_perJvm;
    }

    /**
     * Creates a new perClass cross-cutting instance, if it already exists then return it.
     *
     * @param callingClass
     * @return the cross-cutting instance
     */
    public Object createPerClassAspect(final Class callingClass) {
        synchronized (m_perClass) {
            if (!m_perClass.containsKey(callingClass)) {
                m_perClass.put(callingClass, createAspect());
            }
        }
        return m_perClass.get(callingClass);
    }

    /**
     * Creates a new perInstance cross-cutting instance, if it already exists then return it.
     *
     * @param callingInstance
     * @return the cross-cutting instance
     */
    public Object createPerInstanceAspect(final Object callingInstance) {
        if (callingInstance == null) {
            return m_perJvm;
        }
        synchronized (m_perInstance) {
            if (!m_perInstance.containsKey(callingInstance)) {
                m_perInstance.put(callingInstance, createAspect());
            }
        }
        return m_perInstance.get(callingInstance);
    }

    /**
     * Creates a new perThread cross-cutting instance, if it already exists then return it.
     *
     * @param thread the thread for the aspect
     * @return the cross-cutting instance
     */
    public Object createPerThreadAspect(final Thread thread) {
        synchronized (m_perThread) {
            if (!m_perThread.containsKey(thread)) {
                m_perThread.put(thread, createAspect());
            }
        }
        return m_perThread.get(thread);
    }

    /**
     * Attach the introduction container to this aspect container to mirror the "aspect contains 0-n introduction"
     *
     * @param name           of the introduction
     * @param introContainer introduction container
     */
    public void addIntroductionContainer(final String name, final IntroductionContainer introContainer) {
        m_introductionContainers.put(name, introContainer);
    }

    /**
     * Returns the introduction container of given name (introduction name) or null if not linked.
     *
     * @param name of the introduction
     * @return introduction container
     */
    public IntroductionContainer getIntroductionContainer(final String name) {
        return (IntroductionContainer) m_introductionContainers.get(name);
    }

    /**
     * Returns a specific advice by index.
     *
     * @param index the index
     * @return the advice
     */
    public Method getAdviceMethod(final int index) {
        if (index < 0) {
            throw new IllegalArgumentException("advice index can not be less than 0");
        }
        return m_adviceRepository[index];
    }

    /**
     * Creates a repository for the advice methods.
     */
    protected void createAdviceRepository() {
        synchronized (m_adviceRepository) {
            List methodList = ReflectHelper.createSortedMethodList(m_infoPrototype.getAspectClass());
            m_adviceRepository = new Method[methodList.size()];
            for (int i = 0; i < m_adviceRepository.length; i++) {
                Method method = (Method) methodList.get(i);
                method.setAccessible(true);
                m_adviceRepository[i] = method;
            }
        }
    }

    /**
     * To be implemented by the concrete aspect containers. <p/>Should return a new aspect instance.
     *
     * @return a new aspect instance
     */
    protected abstract Object createAspect();
}