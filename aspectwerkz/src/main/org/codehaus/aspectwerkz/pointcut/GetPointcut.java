/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.pointcut;

import java.util.Map;
import java.util.HashMap;
import java.io.ObjectInputStream;

import org.codehaus.aspectwerkz.NameIndexTuple;
import org.codehaus.aspectwerkz.IndexTuple;
import org.codehaus.aspectwerkz.SystemLoader;
import org.codehaus.aspectwerkz.definition.PointcutDefinition;
import org.codehaus.aspectwerkz.definition.expression.Expression;

/**
 * Implements the pointcut concept for field access.
 * Is an abstraction of a well defined point of execution in the program.<br/>
 * Could matches one or many points as long as they are well defined.<br/>
 * Stores the advices for this specific pointcut.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class GetPointcut {

    /**
     * The expression for the pointcut.
     */
    protected Expression m_expression;

    /**
     * The cflow pointcut expression.
     */
    protected String m_cflowExpression;

    /**
     * The pointcut definitions referenced in the m_expression.
     * Mapped to the name of the pointcut definition.
     */
    protected Map m_pointcutDefs = new HashMap();

    /**
     * The UUID for the AspectWerkz system.
     */
    protected String m_uuid;

    /**
     * Holds the names of the pre advices.
     */
    protected String[] m_preNames = new String[0];

    /**
     * Holds the names of the post advices.
     */
    protected String[] m_postNames = new String[0];

    /**
     * Holds the indexes of the pre advices.
     */
    protected IndexTuple[] m_preIndexes = new IndexTuple[0];

    /**
     * Holds the indexes of the post advices.
     */
    protected IndexTuple[] m_postIndexes = new IndexTuple[0];

    /**
     * Creates a get pointcut.
     *
     * @param uuid the UUID for the AspectWerkz system
     * @param expression the expression
     */
    public GetPointcut(final String uuid, final Expression expression) {
        if (uuid == null) throw new IllegalArgumentException("uuid can not be null");
        if (expression == null) throw new IllegalArgumentException("expression can not be null");
        m_uuid = uuid;
        m_expression = expression;
    }

    /**
     * Adds a new pointcut definition.
     *
     * @param pointcut the pointcut definition
     */
    public void addPointcutDef(final PointcutDefinition pointcut) {
        m_pointcutDefs.put(pointcut.getName(), pointcut);
    }

    /**
     * Adds a pre advice to the pointcut.
     *
     * @param advice the name of the advice to add
     */
    public void addBeforeAdvice(final String advice) {
        if (advice == null || advice.trim().length() == 0) throw new IllegalArgumentException("name of advice to add can not be null or an empty string");
        synchronized (m_preNames) {
            synchronized (m_preIndexes) {
                final String[] tmp = new String[m_preNames.length + 1];
                java.lang.System.arraycopy(m_preNames, 0, tmp, 0, m_preNames.length);

                tmp[m_preNames.length] = advice;

                m_preNames = new String[m_preNames.length + 1];
                java.lang.System.arraycopy(tmp, 0, m_preNames, 0, tmp.length);

                m_preIndexes = new IndexTuple[m_preNames.length];
                for (int i = 0, j = m_preNames.length; i < j; i++) {
                    m_preIndexes[i] = SystemLoader.getSystem(m_uuid).getAdviceIndexFor(m_preNames[i]);
                }
            }
        }
    }

    /**
     * Adds post advice to the pointcut.
     *
     * @param advice the name of the advice to add
     */
    public void addAfterAdvice(final String advice) {
        if (advice == null || advice.trim().length() == 0) throw new IllegalArgumentException("name of advice to add can not be null or an empty string");
        synchronized (m_postNames) {
            synchronized (m_postIndexes) {
                final String[] tmp = new String[m_postNames.length + 1];
                java.lang.System.arraycopy(m_postNames, 0, tmp, 0, m_postNames.length);

                tmp[m_postNames.length] = advice;

                m_postNames = new String[m_postNames.length + 1];
                java.lang.System.arraycopy(tmp, 0, m_postNames, 0, tmp.length);

                m_postIndexes = new IndexTuple[m_postNames.length];
                for (int i = 0, j = m_postNames.length; i < j; i++) {
                    m_postIndexes[i] = SystemLoader.getSystem(m_uuid).getAdviceIndexFor(m_postNames[i]);
                }
            }
        }
    }

    /**
     * Adds pre advices to the pointcut.
     *
     * @param advicesToAdd the advices to add
     */
    public void addPreAdvices(final String[] advicesToAdd) {
        for (int i = 0; i < advicesToAdd.length; i++) {
            if (advicesToAdd[i] == null || advicesToAdd[i].trim().length() == 0) throw new IllegalArgumentException("name of advice to add can not be null or an empty string");
        }
        synchronized (m_preNames) {
            synchronized (m_preIndexes) {

                final String[] clone = new String[advicesToAdd.length];
                java.lang.System.arraycopy(advicesToAdd, 0, clone, 0, advicesToAdd.length);

                final String[] tmp = new String[
                        m_preNames.length + advicesToAdd.length];
                java.lang.System.arraycopy(m_preNames, 0, tmp, 0, m_preNames.length);
                java.lang.System.arraycopy(clone, 0, tmp, m_preNames.length, tmp.length);

                m_preNames = new String[tmp.length];
                java.lang.System.arraycopy(tmp, 0, m_preNames, 0, tmp.length);

                m_preIndexes = new IndexTuple[m_preNames.length];
                for (int j = 0; j < m_preNames.length; j++) {
                    m_preIndexes[j] = SystemLoader.getSystem(m_uuid).getAdviceIndexFor(m_preNames[j]);
                }
            }
        }
    }

    /**
     * Adds post advices to the pointcut.
     *
     * @param advicesToAdd the advices to add
     */
    public void addPostAdvices(final String[] advicesToAdd) {
        for (int i = 0; i < advicesToAdd.length; i++) {
            if (advicesToAdd[i] == null || advicesToAdd[i].trim().length() == 0) throw new IllegalArgumentException("name of advice to add can not be null or an empty string");
        }
        synchronized (m_postNames) {
            synchronized (m_postIndexes) {

                final String[] clone = new String[advicesToAdd.length];
                java.lang.System.arraycopy(advicesToAdd, 0, clone, 0, advicesToAdd.length);

                final String[] tmp = new String[m_postNames.length + advicesToAdd.length];
                java.lang.System.arraycopy(m_postNames, 0, tmp, 0, m_postNames.length);
                java.lang.System.arraycopy(clone, 0, tmp, m_postNames.length, tmp.length);

                m_postNames = new String[tmp.length];
                java.lang.System.arraycopy(tmp, 0, m_postNames, 0, tmp.length);

                m_postIndexes = new IndexTuple[m_postNames.length];
                for (int j = 0; j < m_postNames.length; j++) {
                    m_postIndexes[j] = SystemLoader.getSystem(m_uuid).getAdviceIndexFor(m_postNames[j]);
                }
            }
        }
    }

    /**
     * Removes a pre advice from the pointcut.
     *
     * @param advice the name of the pre advice to remove
     */
    public void removePreAdvice(final String advice) {
        if (advice == null || advice.trim().length() == 0) throw new IllegalArgumentException("name of advice to remove can not be null or an empty string");
        synchronized (m_preNames) {
            synchronized (m_preIndexes) {
                int index = -1;
                for (int i = 0; i < m_preNames.length; i++) {
                    if (m_preNames[i].equals(advice)) {
                        index = i;
                        break;
                    }
                }
                if (index == -1) throw new RuntimeException("can not remove pre advice with the name " + advice + ": no such advice");

                final String[] names = new String[m_preNames.length - 1];
                int j, k;
                for (j = 0, k = 0; j < index; j++, k++) {
                    names[j] = m_preNames[j];
                }
                j++;
                for (; j < m_preNames.length; j++, k++) {
                    names[k] = m_preNames[j];
                }
                m_preNames = new String[names.length];
                java.lang.System.arraycopy(names, 0, m_preNames, 0, names.length);

                final IndexTuple[] indexes = new IndexTuple[m_preIndexes.length - 1];
                for (j = 0, k = 0; j < index; j++, k++) {
                    indexes[j] = m_preIndexes[j];
                }
                j++;
                for (; j < m_preIndexes.length; j++, k++) {
                    indexes[k] = m_preIndexes[j];
                }
                m_preIndexes = new IndexTuple[indexes.length];
                java.lang.System.arraycopy(indexes, 0, m_preIndexes, 0, indexes.length);
            }
        }
    }

    /**
     * Removes a post advice from the pointcut.
     *
     * @param advice the name of the pre advice to remove
     */
    public void removePostAdvice(final String advice) {
        if (advice == null || advice.trim().length() == 0) throw new IllegalArgumentException("name of advice to remove can not be null or an empty string");
        synchronized (m_postNames) {
            synchronized (m_postIndexes) {
                int index = -1;
                for (int i = 0; i < m_postNames.length; i++) {
                    if (m_postNames[i].equals(advice)) {
                        index = i;
                        break;
                    }
                }
                if (index == -1) throw new RuntimeException("can not remove post advice with the name " + advice + ": no such advice");

                final String[] names = new String[m_postNames.length - 1];
                int j, k;
                for (j = 0, k = 0; j < index; j++, k++) {
                    names[j] = m_postNames[j];
                }
                j++;
                for (; j < m_postNames.length; j++, k++) {
                    names[k] = m_postNames[j];
                }
                m_postNames = new String[names.length];
                java.lang.System.arraycopy(names, 0, m_postNames, 0, names.length);

                final IndexTuple[] indexes = new IndexTuple[m_postIndexes.length - 1];
                for (j = 0, k = 0; j < index; j++, k++) {
                    indexes[j] = m_postIndexes[j];
                }
                j++;
                for (; j < m_postIndexes.length; j++, k++) {
                    indexes[k] = m_postIndexes[j];
                }
                m_postIndexes = new IndexTuple[indexes.length];
                java.lang.System.arraycopy(indexes, 0, m_postIndexes, 0, indexes.length);
            }
        }
    }

    /**
     * Checks if the pointcuts has a certain pre advice.
     *
     * @param advice the advice to check for existence
     * @return boolean
     */
    public boolean hasPreAdvice(final String advice) {
        for (int i = 0; i < m_preNames.length; i++) {
            if (m_preNames[i].equals(advice)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the pointcuts has a certain post advice.
     *
     * @param advice the advice to check for existence
     * @return boolean
     */
    public boolean hasPostAdvice(final String advice) {
        for (int i = 0; i < m_postNames.length; i++) {
            if (m_postNames[i].equals(advice)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the advices in the form of an array with advice/index tuples.
     * To be used when a reordering of the advices is necessary.
     *
     * @return the current advice/index tuple array
     */
    public NameIndexTuple[] getPreAdviceIndexTuples() {
        synchronized (m_preIndexes) {
            synchronized (m_preNames) {
                final NameIndexTuple[] tuples = new NameIndexTuple[m_preNames.length];
                for (int i = 0; i < m_preNames.length; i++) {
                    tuples[i] = new NameIndexTuple(m_preNames[i], m_preIndexes[i]);
                }
                return tuples;
            }
        }
    }

    /**
     * Sets the advices. To be used when a reordering of the advices is necessary.
     *
     * @param tuple the new advice/index tuple array
     */
    public void setPreAdviceIndexTuples(final NameIndexTuple[] tuple) {
        synchronized (m_preIndexes) {
            synchronized (m_preNames) {
                m_preNames = new String[tuple.length];
                m_preIndexes = new IndexTuple[tuple.length];
                for (int i = 0; i < tuple.length; i++) {
                    m_preNames[i] = tuple[i].getName();
                    m_preIndexes[i] = tuple[i].getIndex();
                }
            }
        }
    }

    /**
     * Returns the advices in the form of an array with advice/index tuples.
     * To be used when a reordering of the advices is necessary.
     *
     * @return the current advice/index tuple array
     */
    public NameIndexTuple[] getPostAdviceIndexTuples() {
        synchronized (m_postIndexes) {
            synchronized (m_postNames) {
                final NameIndexTuple[] tuples = new NameIndexTuple[m_postNames.length];
                for (int i = 0; i < m_postNames.length; i++) {
                    tuples[i] = new NameIndexTuple(m_postNames[i], m_postIndexes[i]);
                }
                return tuples;
            }
        }
    }

    /**
     * Sets the advices. To be used when a reordering of the advices is necessary.
     *
     * @param tuple the new advice/index tuple array
     */
    public void setPostAdviceIndexTuples(final NameIndexTuple[] tuple) {
        synchronized (m_postIndexes) {
            synchronized (m_postNames) {
                m_postNames = new String[tuple.length];
                m_postIndexes = new IndexTuple[tuple.length];
                for (int i = 0; i < tuple.length; i++) {
                    m_postNames[i] = tuple[i].getName();
                    m_postIndexes[i] = tuple[i].getIndex();
                }
            }
        }
    }

    /**
     * Returns the cflow expression.
     *
     * @return the cflow expression
     */
    public String getCFlowExpression() {
        return m_cflowExpression;
    }

    /**
     * Sets the cflow expression.
     *
     * @param cflowExpression the cflow expression
     */
    public void setCFlowExpression(final String cflowExpression) {
        m_cflowExpression = cflowExpression;
    }

    /**
     * Returns a list with the indexes for the pre advices for the pointcut.
     *
     * @return the pre advice indexes
     */
    public IndexTuple[] getPreAdviceIndexes() {
        return m_preIndexes;
    }

    /**
     * Returns a list with the names for the pre advices for the pointcut.
     *
     * @return the pre advice names
     */
    public String[] getPreAdviceNames() {
        return m_preNames;
    }

    /**
     * Returns a list with the indexes for the post advices for the pointcut.
     *
     * @return the pre advice indexes
     */
    public IndexTuple[] getPostAdviceIndexes() {
        return m_postIndexes;
    }

    /**
     * Returns a list with the names for the post advices for the pointcut.
     *
     * @return the post advice names
     */
    public String[] getPostAdviceNames() {
        return m_postNames;
    }

    /**
     * Returns the expression of the pointcut.
     *
     * @return the expression
     */
    public Expression getExpression() {
        return m_expression;
    }

    /**
     * Sets the pre advices.
     * Caution: the index A name arrays have to be in synch.
     *
     * @param indexes the new pre advice index array
     * @param names the new pre advice names array
     */
    public void setPreAdvices(final IndexTuple[] indexes, final String[] names) {
        synchronized (m_preIndexes) {
            synchronized (m_preNames) {
                m_preIndexes = indexes;
                m_preNames = names;
            }
        }
    }

    /**
     * Sets the post advices.
     * Caution: the index A name arrays have to be in synch.
     *
     * @param indexes the new post advice index array
     * @param names the new post advice names array
     */
    public void setPostAdvices(final IndexTuple[] indexes, final String[] names) {
        synchronized (m_postIndexes) {
            synchronized (m_postNames) {
                m_postIndexes = indexes;
                m_postNames = names;
            }
        }
    }

    /**
     * Provides custom deserialization.
     *
     * @param stream the object input stream containing the serialized object
     * @throws java.lang.Exception in case of failure
     */
    private void readObject(final ObjectInputStream stream) throws Exception {
        ObjectInputStream.GetField fields = stream.readFields();

        m_expression = (Expression)fields.get("m_expression", null);
        m_pointcutDefs = (Map)fields.get("m_pointcutDefs", null);
        m_preNames = (String[])fields.get("m_preNames", null);
        m_postNames = (String[])fields.get("m_postNames", null);
        m_preIndexes = (IndexTuple[])fields.get("m_preIndexes", null);
        m_postIndexes = (IndexTuple[])fields.get("m_postIndexes", null);
        m_uuid = (String)fields.get("m_uuid", null);
    }
}
