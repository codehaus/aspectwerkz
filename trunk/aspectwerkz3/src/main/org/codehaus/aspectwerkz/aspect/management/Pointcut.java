/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.aspect.management;

import org.codehaus.aspectwerkz.IndexTuple;
import org.codehaus.aspectwerkz.NameIndexTuple;
import org.codehaus.aspectwerkz.expression.ExpressionInfo;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of the pointcut concept. I.e. an abstraction of a well defined point of execution in the program.
 * <p/>
 * Could matches one or many as long at it is well defined.<br/> Stores the advices for the specific pointcut.
 * <p/>
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @TODO change addXXAdvice to allow 'aspectName, adviceName' params
 */
public class Pointcut implements Serializable {
    /**
     * The expression for the pointcut.
     */
    protected ExpressionInfo m_expressionInfo;

    /**
     * The names of the around advices.
     */
    protected String[] m_aroundAdviceNames = new String[0];

    /**
     * The names of the around advices.
     */
    protected String[] m_beforeAdviceNames = new String[0];

    /**
     * The names of the around advices.
     */
    protected String[] m_afterAdviceNames = new String[0];

    /**
     * The indexes of the around advices.
     */
    protected IndexTuple[] m_aroundAdviceIndexes = new IndexTuple[0];

    /**
     * The indexes of the before advices.
     */
    protected IndexTuple[] m_beforeAdviceIndexes = new IndexTuple[0];

    /**
     * The indexes of the after advices.
     */
    protected IndexTuple[] m_afterAdviceIndexes = new IndexTuple[0];

    /**
     * The AspectManager for the AspectWerkz system.
     */
    protected final AspectManager m_aspectManager;

    /**
     * Creates a new pointcut.
     *
     * @param aspectManager  the aspectManager for the AspectWerkz system
     * @param expressionInfo the pattern for the pointcut
     */
    public Pointcut(final AspectManager aspectManager, final ExpressionInfo expressionInfo) {
        if (aspectManager == null) {
            throw new IllegalArgumentException("aspect manager can not be null");
        }
        if (expressionInfo == null) {
            throw new IllegalArgumentException("expression info can not be null");
        }
        m_aspectManager = aspectManager;
        m_expressionInfo = expressionInfo;
    }

    /**
     * Adds an advice to the pointcut.
     *
     * @param advice the name of the advice to add
     */
    public void addAroundAdvice(final String advice) {
        if ((advice == null) || (advice.trim().length() == 0)) {
            throw new IllegalArgumentException("name of advice to add can not be null or an empty string");
        }

        // system reinitialization can lead to redundancy
        // TODO: ALEX - make the HotSwap with def slower
        for (int i = 0; i < m_aroundAdviceNames.length; i++) {
            if (advice.equals(m_aroundAdviceNames[i])) {
                return;
            }
        }
        synchronized (m_aroundAdviceNames) {
            synchronized (m_aroundAdviceIndexes) {
                final String[] tmp = new String[m_aroundAdviceNames.length + 1];
                System.arraycopy(m_aroundAdviceNames, 0, tmp, 0, m_aroundAdviceNames.length);
                tmp[m_aroundAdviceNames.length] = advice;
                m_aroundAdviceNames = new String[m_aroundAdviceNames.length + 1];
                System.arraycopy(tmp, 0, m_aroundAdviceNames, 0, tmp.length);

                // update the indexes
                m_aroundAdviceIndexes = new IndexTuple[m_aroundAdviceNames.length];
                for (int i = 0, j = m_aroundAdviceNames.length; i < j; i++) {
                    m_aroundAdviceIndexes[i] = m_aspectManager.getAdviceIndexFor(m_aroundAdviceNames[i]);
                }
            }
        }
    }

    /**
     * Adds an advice to the pointcut.
     *
     * @param advice the name of the advice to add
     */
    public void addBeforeAdvice(final String advice) {
        if ((advice == null) || (advice.trim().length() == 0)) {
            throw new IllegalArgumentException("name of advice to add can not be null or an empty string");
        }
        synchronized (m_beforeAdviceNames) {
            synchronized (m_beforeAdviceIndexes) {
                final String[] tmp = new String[m_beforeAdviceNames.length + 1];
                System.arraycopy(m_beforeAdviceNames, 0, tmp, 0, m_beforeAdviceNames.length);
                tmp[m_beforeAdviceNames.length] = advice;
                m_beforeAdviceNames = new String[m_beforeAdviceNames.length + 1];
                System.arraycopy(tmp, 0, m_beforeAdviceNames, 0, tmp.length);

                // update the indexes
                m_beforeAdviceIndexes = new IndexTuple[m_beforeAdviceNames.length];
                for (int i = 0, j = m_beforeAdviceNames.length; i < j; i++) {
                    m_beforeAdviceIndexes[i] = m_aspectManager.getAdviceIndexFor(m_beforeAdviceNames[i]);
                }
            }
        }
    }

    /**
     * Adds an advice to the pointcut.
     *
     * @param advice the name of the advice to add
     */
    public void addAfterAdvice(final String advice) {
        if ((advice == null) || (advice.trim().length() == 0)) {
            throw new IllegalArgumentException("name of advice to add can not be null or an empty string");
        }
        synchronized (m_afterAdviceNames) {
            synchronized (m_afterAdviceIndexes) {
                final String[] tmp = new String[m_afterAdviceNames.length + 1];
                System.arraycopy(m_afterAdviceNames, 0, tmp, 0, m_afterAdviceNames.length);
                tmp[m_afterAdviceNames.length] = advice;
                m_afterAdviceNames = new String[m_afterAdviceNames.length + 1];
                System.arraycopy(tmp, 0, m_afterAdviceNames, 0, tmp.length);

                // update the indexes
                m_afterAdviceIndexes = new IndexTuple[m_afterAdviceNames.length];
                for (int i = 0, j = m_afterAdviceNames.length; i < j; i++) {
                    m_afterAdviceIndexes[i] = m_aspectManager.getAdviceIndexFor(m_afterAdviceNames[i]);
                }
            }
        }
    }

    /**
     * Removes an advice from the pointcut.
     *
     * @param advice the name of the advice to remove
     */
    public void removeAroundAdvice(final String advice) {
        if ((advice == null) || (advice.trim().length() == 0)) {
            throw new IllegalArgumentException("name of advice to remove can not be null or an empty string");
        }
        synchronized (m_aroundAdviceNames) {
            synchronized (m_aroundAdviceIndexes) {
                int index = -1;
                for (int i = 0; i < m_aroundAdviceNames.length; i++) {
                    if (m_aroundAdviceNames[i].equals(advice)) {
                        index = i;
                        break;
                    }
                }
                if (index == -1) {
                    throw new RuntimeException("can not remove advice with the name " + advice + ": no such advice");
                }
                final String[] names = new String[m_aroundAdviceNames.length - 1];
                int j;
                int k;
                for (j = 0, k = 0; j < index; j++, k++) {
                    names[j] = m_aroundAdviceNames[j];
                }
                j++;
                for (; j < m_aroundAdviceNames.length; j++, k++) {
                    names[k] = m_aroundAdviceNames[j];
                }
                m_aroundAdviceNames = new String[names.length];
                System.arraycopy(names, 0, m_aroundAdviceNames, 0, names.length);
                final IndexTuple[] indexes = new IndexTuple[m_aroundAdviceIndexes.length - 1];
                for (j = 0, k = 0; j < index; j++, k++) {
                    indexes[j] = m_aroundAdviceIndexes[j];
                }
                j++;
                for (; j < m_aroundAdviceIndexes.length; j++, k++) {
                    indexes[k] = m_aroundAdviceIndexes[j];
                }
                m_aroundAdviceIndexes = new IndexTuple[indexes.length];
                System.arraycopy(indexes, 0, m_aroundAdviceIndexes, 0, indexes.length);
            }
        }
    }

    /**
     * Removes an advice from the pointcut.
     *
     * @param advice the name of the advice to remove
     */
    public void removeBeforeAdvice(final String advice) {
        if ((advice == null) || (advice.trim().length() == 0)) {
            throw new IllegalArgumentException("name of advice to remove can not be null or an empty string");
        }
        synchronized (m_beforeAdviceNames) {
            synchronized (m_beforeAdviceIndexes) {
                int index = -1;
                for (int i = 0; i < m_beforeAdviceNames.length; i++) {
                    if (m_beforeAdviceNames[i].equals(advice)) {
                        index = i;
                        break;
                    }
                }
                if (index == -1) {
                    throw new RuntimeException("can not remove advice with the name " + advice + ": no such advice");
                }
                final String[] names = new String[m_beforeAdviceNames.length - 1];
                int j;
                int k;
                for (j = 0, k = 0; j < index; j++, k++) {
                    names[j] = m_beforeAdviceNames[j];
                }
                j++;
                for (; j < m_beforeAdviceNames.length; j++, k++) {
                    names[k] = m_beforeAdviceNames[j];
                }
                m_beforeAdviceNames = new String[names.length];
                System.arraycopy(names, 0, m_beforeAdviceNames, 0, names.length);
                final IndexTuple[] indexes = new IndexTuple[m_beforeAdviceIndexes.length - 1];
                for (j = 0, k = 0; j < index; j++, k++) {
                    indexes[j] = m_beforeAdviceIndexes[j];
                }
                j++;
                for (; j < m_beforeAdviceIndexes.length; j++, k++) {
                    indexes[k] = m_beforeAdviceIndexes[j];
                }
                m_beforeAdviceIndexes = new IndexTuple[indexes.length];
                System.arraycopy(indexes, 0, m_beforeAdviceIndexes, 0, indexes.length);
            }
        }
    }

    /**
     * Removes an advice from the pointcut.
     *
     * @param advice the name of the advice to remove
     */
    public void removeAfterAdvice(final String advice) {
        if ((advice == null) || (advice.trim().length() == 0)) {
            throw new IllegalArgumentException("name of advice to remove can not be null or an empty string");
        }
        synchronized (m_afterAdviceNames) {
            synchronized (m_afterAdviceIndexes) {
                int index = -1;
                for (int i = 0; i < m_afterAdviceNames.length; i++) {
                    if (m_afterAdviceNames[i].equals(advice)) {
                        index = i;
                        break;
                    }
                }
                if (index == -1) {
                    throw new RuntimeException("can not remove advice with the name " + advice + ": no such advice");
                }
                final String[] names = new String[m_afterAdviceNames.length - 1];
                int j;
                int k;
                for (j = 0, k = 0; j < index; j++, k++) {
                    names[j] = m_afterAdviceNames[j];
                }
                j++;
                for (; j < m_afterAdviceNames.length; j++, k++) {
                    names[k] = m_afterAdviceNames[j];
                }
                m_afterAdviceNames = new String[names.length];
                System.arraycopy(names, 0, m_afterAdviceNames, 0, names.length);
                final IndexTuple[] indexes = new IndexTuple[m_afterAdviceIndexes.length - 1];
                for (j = 0, k = 0; j < index; j++, k++) {
                    indexes[j] = m_afterAdviceIndexes[j];
                }
                j++;
                for (; j < m_afterAdviceIndexes.length; j++, k++) {
                    indexes[k] = m_afterAdviceIndexes[j];
                }
                m_afterAdviceIndexes = new IndexTuple[indexes.length];
                System.arraycopy(indexes, 0, m_afterAdviceIndexes, 0, indexes.length);
            }
        }
    }

    /**
     * Checks if the pointcuts has a certain advice.
     *
     * @param advice the advice to check for existence
     * @return boolean
     */
    public boolean hasAroundAdvice(final String advice) {
        for (int i = 0; i < m_aroundAdviceNames.length; i++) {
            if (m_aroundAdviceNames[i].equals(advice)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the pointcuts has a certain advice.
     *
     * @param advice the advice to check for existence
     * @return boolean
     */
    public boolean hasBeforeAdvice(final String advice) {
        for (int i = 0; i < m_beforeAdviceNames.length; i++) {
            if (m_beforeAdviceNames[i].equals(advice)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the pointcuts has a certain advice.
     *
     * @param advice the advice to check for existence
     * @return boolean
     */
    public boolean hasAfterAdvice(final String advice) {
        for (int i = 0; i < m_afterAdviceNames.length; i++) {
            if (m_afterAdviceNames[i].equals(advice)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the advices in the form of an array with advice/index tuples. To be used when a reordering of the advices
     * is necessary.<br/> For addition of an advice see <code>addAdviceTestMethod(..)</code>.<br/> For removal of an
     * advice see <code>removeAdviceTestMethod(..)</code>.
     *
     * @return the current advice/index tuples as a list
     */
    public List getAroundAdviceIndexTuples() {
        synchronized (m_aroundAdviceIndexes) {
            synchronized (m_aroundAdviceNames) {
                final List advices = new ArrayList(m_aroundAdviceNames.length);
                for (int i = 0; i < m_aroundAdviceNames.length; i++) {
                    advices.add(new NameIndexTuple(m_aroundAdviceNames[i], m_aroundAdviceIndexes[i]));
                }
                return advices;
            }
        }
    }

    /**
     * Returns the advices in the form of an array with advice/index tuples. To be used when a reordering of the advices
     * is necessary.<br/> For addition of an advice see <code>addAdviceTestMethod(..)</code>.<br/> For removal of an
     * advice see <code>removeAdviceTestMethod(..)</code>.
     *
     * @return the current advice/index tuples as a list
     */
    public List getBeforeAdviceIndexTuples() {
        synchronized (m_beforeAdviceIndexes) {
            synchronized (m_beforeAdviceNames) {
                final List advices = new ArrayList(m_beforeAdviceNames.length);
                for (int i = 0; i < m_beforeAdviceNames.length; i++) {
                    advices.add(new NameIndexTuple(m_beforeAdviceNames[i], m_beforeAdviceIndexes[i]));
                }
                return advices;
            }
        }
    }

    /**
     * Returns the advices in the form of an array with advice/index tuples. To be used when a reordering of the advices
     * is necessary.<br/> For addition of an advice see <code>addAdviceTestMethod(..)</code>.<br/> For removal of an
     * advice see <code>removeAdviceTestMethod(..)</code>.
     *
     * @return the current advice/index tuples as a list
     */
    public List getAfterAdviceIndexTuples() {
        synchronized (m_afterAdviceIndexes) {
            synchronized (m_afterAdviceNames) {
                final List advices = new ArrayList(m_afterAdviceNames.length);
                for (int i = 0; i < m_afterAdviceNames.length; i++) {
                    advices.add(new NameIndexTuple(m_afterAdviceNames[i], m_afterAdviceIndexes[i]));
                }
                return advices;
            }
        }
    }

    /**
     * Sets the advices. To be used when a reordering of the advices is necessary.<br/> For addition of an advice see
     * <code>addAdviceTestMethod(..)</code>.<br/> For removal of an advice see <code>removeAdviceTestMethod(..)</code>.
     *
     * @param advices the new advice/index tuple array
     */
    public void setAroundAdviceIndexTuples(final List advices) {
        synchronized (m_aroundAdviceIndexes) {
            synchronized (m_aroundAdviceNames) {
                m_aroundAdviceNames = new String[advices.size()];
                m_aroundAdviceIndexes = new IndexTuple[advices.size()];
                int i = 0;
                for (Iterator it = advices.iterator(); it.hasNext(); i++) {
                    try {
                        NameIndexTuple tuple = (NameIndexTuple)it.next();
                        m_aroundAdviceNames[i] = tuple.getName();
                        m_aroundAdviceIndexes[i] = tuple.getIndex();
                    } catch (ClassCastException e) {
                        throw new RuntimeException("advice list must only contain AdviceIndexTuples");
                    }
                }
            }
        }
    }

    /**
     * Sets the advices. To be used when a reordering of the advices is necessary.<br/> For addition of an advice see
     * <code>addAdviceTestMethod(..)</code>.<br/> For removal of an advice see <code>removeAdviceTestMethod(..)</code>.
     *
     * @param advices the new advice/index tuple array
     */
    public void setBeforeAdviceIndexTuples(final List advices) {
        synchronized (m_beforeAdviceIndexes) {
            synchronized (m_beforeAdviceNames) {
                m_beforeAdviceNames = new String[advices.size()];
                m_beforeAdviceIndexes = new IndexTuple[advices.size()];
                int i = 0;
                for (Iterator it = advices.iterator(); it.hasNext(); i++) {
                    try {
                        NameIndexTuple tuple = (NameIndexTuple)it.next();
                        m_beforeAdviceNames[i] = tuple.getName();
                        m_beforeAdviceIndexes[i] = tuple.getIndex();
                    } catch (ClassCastException e) {
                        throw new RuntimeException("advice list must only contain AdviceIndexTuples");
                    }
                }
            }
        }
    }

    /**
     * Sets the advices. To be used when a reordering of the advices is necessary.<br/> For addition of an advice see
     * <code>addAdviceTestMethod(..)</code>.<br/> For removal of an advice see <code>removeAdviceTestMethod(..)</code>.
     *
     * @param advices the new advice/index tuple array
     */
    public void setAfterAdviceIndexTuples(final List advices) {
        synchronized (m_afterAdviceIndexes) {
            synchronized (m_afterAdviceNames) {
                m_afterAdviceNames = new String[advices.size()];
                m_afterAdviceIndexes = new IndexTuple[advices.size()];
                int i = 0;
                for (Iterator it = advices.iterator(); it.hasNext(); i++) {
                    try {
                        NameIndexTuple tuple = (NameIndexTuple)it.next();
                        m_afterAdviceNames[i] = tuple.getName();
                        m_afterAdviceIndexes[i] = tuple.getIndex();
                    } catch (ClassCastException e) {
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
    public IndexTuple getAroundAdviceIndex(final int index) {
        return m_aroundAdviceIndexes[index];
    }

    /**
     * Returns a specific advice index.
     *
     * @return the advice index
     */
    public IndexTuple getBeforeAdviceIndex(final int index) {
        return m_beforeAdviceIndexes[index];
    }

    /**
     * Returns a specific advice index.
     *
     * @return the advice index
     */
    public IndexTuple getAfterAdviceIndex(final int index) {
        return m_afterAdviceIndexes[index];
    }

    /**
     * Returns a list with the indexes for the around advices for the pointcut.
     *
     * @return the advices
     */
    public IndexTuple[] getAroundAdviceIndexes() {
        return m_aroundAdviceIndexes;
    }

    /**
     * Returns a list with the indexes for the before advices for the pointcut.
     *
     * @return the advices
     */
    public IndexTuple[] getBeforeAdviceIndexes() {
        return m_beforeAdviceIndexes;
    }

    /**
     * Returns a list with the indexes for the after advices for the pointcut.
     *
     * @return the advices
     */
    public IndexTuple[] getAfterAdviceIndexes() {
        return m_afterAdviceIndexes;
    }

    /**
     * Returns a list with the names for the advices for the pointcut.
     *
     * @return the advices
     */
    public String[] getAroundAdviceNames() {
        return m_aroundAdviceNames;
    }

    /**
     * Returns the expression for the pointcut.
     *
     * @return the expression
     */
    public ExpressionInfo getExpressionInfo() {
        return m_expressionInfo;
    }

    /**
     * Returns the aspect manager.
     *
     * @return the aspect manager
     */
    public AspectManager getAspectManager() {
        return m_aspectManager;
    }

    /**
     * Provides custom deserialization.
     *
     * @param stream the object input stream containing the serialized object
     * @throws java.lang.Exception in case of failure
     * @TODO needs aspectManager recovery
     */
    private void readObject(final ObjectInputStream stream) throws Exception {
        ObjectInputStream.GetField fields = stream.readFields();
        m_expressionInfo = (ExpressionInfo)fields.get("m_annotation", null);
        m_aroundAdviceNames = (String[])fields.get("m_aroundAdviceNames", null);
        m_aroundAdviceIndexes = (IndexTuple[])fields.get("m_aroundAdviceIndexes", null);
        m_beforeAdviceNames = (String[])fields.get("m_beforeAdviceNames", null);
        m_beforeAdviceIndexes = (IndexTuple[])fields.get("m_beforeAdviceIndexes", null);
        m_afterAdviceNames = (String[])fields.get("m_afterAdviceNames", null);
        m_afterAdviceIndexes = (IndexTuple[])fields.get("m_afterAdviceIndexes", null);
    }
}
