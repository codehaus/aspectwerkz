/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.aspect;

import org.codehaus.aspectwerkz.AspectContext;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;


/**
 * Abstract base class for the aspect container implementations.
 * @TODO: allow any type of constructor, to support ctor based dependency injection
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public abstract class AbstractAspectContainer implements AspectContainer {

    public static final int ASPECT_CONSTRUCTION_TYPE_UNKNOWN = 0;
    public static final int ASPECT_CONSTRUCTION_TYPE_DEFAULT = 1;
    public static final int ASPECT_CONSTRUCTION_TYPE_ASPECT_CONTEXT = 2;
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[]{};

    /**
     * An array with the single aspect context, needed to save one array creation per invocation.
     */
    protected final Object[] ARRAY_WITH_SINGLE_ASPECT_CONTEXT = new Object[1];

    /**
     * The aspect construction type.
     */
    protected int m_constructionType = ASPECT_CONSTRUCTION_TYPE_UNKNOWN;

    /**
     * Introduction container containing introduction declared by this aspect, keys by introduction names
     */
    protected final Map m_introductionContainers = new HashMap();

    /**
     * The aspect context prototype.
     */
    protected final AspectContext m_aspectContext;

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
     * Maps the advice infos to the hash codes of the the matching advice method.
     */
    protected final Map m_adviceInfos = new HashMap();

    /**
     * Creates a new aspect container strategy.
     *
     * @param aspectContext the context
     */
    public AbstractAspectContainer(final AspectContext aspectContext) {
        if (aspectContext == null) {
            throw new IllegalArgumentException("cross-cutting info can not be null");
        }

        m_aspectContext = aspectContext;
        ARRAY_WITH_SINGLE_ASPECT_CONTEXT[0] = m_aspectContext;
    }

    /**
     * Returns the context.
     *
     * @return the context
     */
    public AspectContext getContext() {
        return m_aspectContext;
    }

    /**
     * Creates a new perJVM cross-cutting instance, if it already exists then return it.
     *
     * @return the cross-cutting instance
     */
    public Object aspectOf() {
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
    public Object aspectOf(final Class callingClass) {
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
    public Object aspectOf(final Object callingInstance) {
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
    public Object aspectOf(final Thread thread) {
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
     * To be implemented by the concrete aspect containers. <p/>Should return a new aspect instance.
     *
     * @return a new aspect instance
     */
    protected abstract Object createAspect();
}