/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bon�r. All rights reserved.
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
package org.codehaus.aspectwerkz.pointcut;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import org.codehaus.aspectwerkz.AspectWerkz;
import org.codehaus.aspectwerkz.advice.AdviceIndexTuple;

/**
 * Abstract implementation of the pointcut concept.
 * I.e. an abstraction of a well defined point of execution in the program.<br/>
 * Could matches one or many as long at it is well defined.<br/>
 * Stores the advices for the specific pointcut.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @version $Id: AbstractPointcut.java,v 1.8 2003-06-30 15:55:25 jboner Exp $
 */
public abstract class AbstractPointcut implements Pointcut {

    /**
     * The expression for the pointcut.
     */
    protected final String m_expression;

    /**
     * The pointcut definitions referenced in the m_expression.
     * Mapped to the name of the pointcut definition.
     */
    protected final Map m_pointcutPatterns = new HashMap();

    /**
     * The names of the advices.
     */
    protected String[] m_names = new String[0];

    /**
     * The indexes of the advices.
     */
    protected int[] m_indexes = new int[0];

    /**
     * The UUID for the AspectWerkz system.
     */
    protected final String m_uuid;

    /**
     * The pointcut controller.
     */
//    protected PointcutController m_controller;

    /**
     * Creates a new pointcut.
     *
     * @param uuid the UUID for the AspectWerkz system
     * @param pattern the pattern for the pointcut
     */
    public AbstractPointcut(final String uuid,
                            final String pattern) {
        if (uuid == null) throw new IllegalArgumentException("uuid can not be null");
        if (pattern == null || pattern.trim().length() == 0) throw new IllegalArgumentException("pattern of pointcut can not be null or an empty string");
        m_uuid = uuid;
        m_expression = pattern;
    }

    /**
     * Adds an advice to the pointcut.
     *
     * @param advice the name of the advice to add
     */
    public void addAdvice(final String advice) {
        if (advice == null || advice.trim().length() == 0) throw new IllegalArgumentException("name of advice to add can not be null or an empty string");
        synchronized (m_names) {
            synchronized (m_indexes) {
                final String[] tmp = new String[m_names.length + 1];
                System.arraycopy(m_names, 0, tmp, 0, m_names.length);

                tmp[m_names.length] = advice;

                m_names = new String[m_names.length + 1];
                System.arraycopy(tmp, 0, m_names, 0, tmp.length);

                // update the indexes
                m_indexes = new int[m_names.length];
                for (int i = 0, j = m_names.length; i < j; i++) {
                    m_indexes[i] = AspectWerkz.getSystem(m_uuid).getAdviceIndexFor(m_names[i]);
                }
            }
        }
    }

    /**
     * Adds an array of advices to the pointcut.<br/>
     * Makes a defensive copy.
     *
     * @param advicesToAdd the name of the advices to add
     */
    public void addAdvices(final String[] advicesToAdd) {
        for (int i = 0; i < advicesToAdd.length; i++) {
            if (advicesToAdd[i] == null || advicesToAdd[i].trim().length() == 0) throw new IllegalArgumentException("name of advice to add can not be null or an empty string");
        }
        synchronized (m_names) {
            synchronized (m_indexes) {
                final String[] clone = new String[advicesToAdd.length];
                System.arraycopy(advicesToAdd, 0, clone, 0, advicesToAdd.length);

                final String[] tmp = new String[m_names.length + advicesToAdd.length];
                System.arraycopy(m_names, 0, tmp, 0, m_names.length);
                System.arraycopy(clone, 0, tmp, m_names.length, tmp.length);

                m_names = new String[tmp.length];
                System.arraycopy(tmp, 0, m_names, 0, tmp.length);

                m_indexes = new int[m_names.length];
                for (int j = 0; j < m_names.length; j++) {
                    m_indexes[j] = AspectWerkz.getSystem(m_uuid).
                            getAdviceIndexFor(m_names[j]);
                }
            }
        }
    }

    /**
     * Removes an advice from the pointcut.
     *
     * @param advice the name of the advice to remove
     */
    public void removeAdvice(final String advice) {
        if (advice == null || advice.trim().length() == 0) throw new IllegalArgumentException("name of advice to remove can not be null or an empty string");
        synchronized (m_names) {
            synchronized (m_indexes) {
                int index = -1;
                for (int i = 0; i < m_names.length; i++) {
                    if (m_names[i].equals(advice)) {
                        index = i;
                        break;
                    }
                }
                if (index == -1) throw new RuntimeException("can not remove advice with the name " + advice + ": no such advice");

                final String[] names = new String[m_names.length - 1];
                int j, k;
                for (j = 0, k = 0; j < index; j++, k++) {
                    names[j] = m_names[j];
                }
                j++;
                for (; j < m_names.length; j++, k++) {
                    names[k] = m_names[j];
                }
                m_names = new String[names.length];
                System.arraycopy(names, 0, m_names, 0, names.length);

                final int[] indexes = new int[m_indexes.length - 1];
                for (j = 0, k = 0; j < index; j++, k++) {
                    indexes[j] = m_indexes[j];
                }
                j++;
                for (; j < m_indexes.length; j++, k++) {
                    indexes[k] = m_indexes[j];
                }
                m_indexes = new int[indexes.length];
                System.arraycopy(indexes, 0, m_indexes, 0, indexes.length);
            }
        }
    }

    /**
     * Checks if the pointcuts has a certain advice.
     *
     * @param advice the advice to check for existence
     * @return boolean
     */
    public boolean hasAdvice(final String advice) {
        for (int i = 0; i < m_names.length; i++) {
            if (m_names[i].equals(advice)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the advices in the form of an array with advice/index tuples.
     * To be used when a reordering of the advices is necessary.<br/>
     * For addition of an advice see <code>addAdviceTestMethod(..)</code>.<br/>
     * For removal of an advice see <code>removeAdviceTestMethod(..)</code>.
     *
     * @return the current advice/index tuples as a list
     */
    public List getAdviceIndexTuples() {
        synchronized (m_indexes) {
            synchronized (m_names) {
                final List advices = new ArrayList(m_names.length);
                for (int i = 0; i < m_names.length; i++) {
                    advices.add(new AdviceIndexTuple(m_names[i], m_indexes[i]));
                }
                return advices;
            }
        }
    }

    /**
     * Sets the advices. To be used when a reordering of the advices is necessary.<br/>
     * For addition of an advice see <code>addAdviceTestMethod(..)</code>.<br/>
     * For removal of an advice see <code>removeAdviceTestMethod(..)</code>.
     *
     * @param advices the new advice/index tuple array
     */
    public void setAdviceIndexTuples(final List advices) {
        synchronized (m_indexes) {
            synchronized (m_names) {
                m_names = new String[advices.size()];
                m_indexes = new int[advices.size()];
                int i = 0;
                for (Iterator it = advices.iterator(); it.hasNext(); i++) {
                    try {
                        AdviceIndexTuple tuple = (AdviceIndexTuple)it.next();
                        m_names[i] = tuple.getName();
                        m_indexes[i] = tuple.getIndex();
                    }
                    catch (ClassCastException e) {
                        throw new RuntimeException("advice list must only contain AdviceIndexTuples");
                    }
                }
            }
        }
    }

    /**
     * Returns a specific advice index.
     *
     * @return the advice index
     */
    public int getAdviceIndex(final int index) {
        return m_indexes[index];
    }

    /**
     * Returns a list with the indexes for the advices for the pointcut.
     *
     * @return the advices
     */
    public int[] getAdviceIndexes() {
        return m_indexes;
    }

    /**
     * Returns a list with the names for the advices for the pointcut.
     *
     * @return the advices
     */
    public String[] getAdviceNames() {
        return m_names;
    }

    /**
     * Returns the expression for the pointcut.
     *
     * @return the expression
     */
    public String getExpression() {
        return m_expression;
    }

    /**
     * Sets the pointcut controller.
     *
     * @param controller the pointcut controller
     */
//    public void setController(final PointcutController controller) {
//        m_controller = controller;
//    }

    /**
     * Returns the pointcut controller.
     *
     * @return the pointcut controller
     */
//    public PointcutController getController() {
//        return m_controller;
//    }
}