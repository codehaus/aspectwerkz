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
package org.codehaus.aspectwerkz.pointcut;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.codehaus.aspectwerkz.AspectWerkz;
import org.codehaus.aspectwerkz.advice.AdviceIndexTuple;

/**
 * Abstract implementation of the pointcut concept.
 * I.e. an abstraction of a well defined point of execution in the program.<br/>
 * Could match one or many as long at it is well defined.<br/>
 * Stores the advices for the specific pointcut.
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: AbstractPointcut.java,v 1.1.1.1 2003-05-11 15:14:49 jboner Exp $
 */
public abstract class AbstractPointcut implements Pointcut {

    /**
     * The name of the pointcut.
     */
    protected final String m_name;

    /**
     * Defines if the the pointcut is thread-safe or not.
     */
    protected final boolean m_isThreadSafe;

    /**
     * The names of the advices.
     */
    protected String[] m_names = new String[0];

    /**
     * The indexes of the advices.
     */
    protected int[] m_indexes = new int[0];

    /**
     * Creates a new pointcut.
     *
     * @param name the name of the pointcut
     */
    public AbstractPointcut(final String name) {
        m_name = name;
        m_isThreadSafe = false;
    }

    /**
     * Creates a new pointcut.
     *
     * @param name the name of the pointcut
     * @param isThreadSafe the thread safe type
     */
    public AbstractPointcut(final String name, final boolean isThreadSafe) {
        if (name == null || name.trim().length() == 0) throw new IllegalArgumentException("name of pointcut can not be null or an empty string");
        m_name = name;
        m_isThreadSafe = isThreadSafe;
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
                    m_indexes[i] = AspectWerkz.getAdviceIndexFor(m_names[i]);
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
            if (advicesToAdd[i] == null || advicesToAdd[i].trim().length() == 0) throw new IllegalArgumentException("advice name to add can not be null or an empty string");
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
                    m_indexes[j] = AspectWerkz.getAdviceIndexFor(m_names[j]);
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
        if (advice == null || advice.trim().length() == 0) throw new IllegalArgumentException("advice name to remove can not be null or an empty string");
        synchronized (m_names) {
            synchronized (m_indexes) {
                int index = -1;
                for (int i = 0; i < m_names.length; i++) {
                    if (m_names[i].equals(advice)) {
                        index = i;
                        break;
                    }
                }
                if (index == -1) throw new RuntimeException("no such advice");

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
     * Returns the name of the pointcut.
     *
     * @return the name
     */
    public String getName() {
        return m_name;
    }

    /**
     * Checks if the pointcut is thread safe.
     *
     * @return boolean
     */
    public boolean isThreadSafe() {
        return m_isThreadSafe;
    }

    // --- over-ridden methods ---

//    public String toString() {
//        return "["
//                + super.toString()
//                + ": "
//                + m_name
//                + "," + m_names
//                + "," + m_jispIndexes
//                + "]";
//    }
//
//    public int hashCode() {
//        int result = 17;
//        result = 37 * result + hashCodeOrZeroIfNull(m_name);
//        result = 37 * result + hashCodeOrZeroIfNull(m_names);
//        result = 37 * result + hashCodeOrZeroIfNull(m_jispIndexes);
//        return result;
//    }
//
//    protected static int hashCodeOrZeroIfNull(final Object o) {
//        if (null == o) return 19;
//        return o.hashCode();
//    }
//
//    protected static boolean areEqualsOrBothNull(final Object o1, final Object o2) {
//        if (null == o1) return (null == o2);
//        return o1.equals(o2);
//    }
}