/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
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
import org.codehaus.aspectwerkz.definition.expression.PointcutType;

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
     * Holds the names of the before advices.
     */
    protected String[] m_beforeNames = new String[0];

    /**
     * Holds the names of the after advices.
     */
    protected String[] m_afterNames = new String[0];

    /**
     * Holds the indexes of the before advices.
     */
    protected IndexTuple[] m_beforeIndexes = new IndexTuple[0];

    /**
     * Holds the indexes of the after advices.
     */
    protected IndexTuple[] m_afterIndexes = new IndexTuple[0];

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
     * Adds a before advice to the pointcut.
     *
     * @param advice the name of the advice to add
     */
    public void addBeforeAdvice(final String advice) {
        if (advice == null || advice.trim().length() == 0) throw new IllegalArgumentException("name of advice to add can not be null or an empty string");
        synchronized (m_beforeNames) {
            synchronized (m_beforeIndexes) {
                final String[] tmp = new String[m_beforeNames.length + 1];
                java.lang.System.arraycopy(m_beforeNames, 0, tmp, 0, m_beforeNames.length);

                tmp[m_beforeNames.length] = advice;

                m_beforeNames = new String[m_beforeNames.length + 1];
                java.lang.System.arraycopy(tmp, 0, m_beforeNames, 0, tmp.length);

                m_beforeIndexes = new IndexTuple[m_beforeNames.length];
                for (int i = 0, j = m_beforeNames.length; i < j; i++) {
                    m_beforeIndexes[i] = SystemLoader.getSystem(m_uuid).
                            getAspectManager().getAdviceIndexFor(m_beforeNames[i]);
                }
            }
        }
    }

    /**
     * Adds after advice to the pointcut.
     *
     * @param advice the name of the advice to add
     */
    public void addAfterAdvice(final String advice) {
        if (advice == null || advice.trim().length() == 0) throw new IllegalArgumentException("name of advice to add can not be null or an empty string");
        synchronized (m_afterNames) {
            synchronized (m_afterIndexes) {
                final String[] tmp = new String[m_afterNames.length + 1];
                java.lang.System.arraycopy(m_afterNames, 0, tmp, 0, m_afterNames.length);

                tmp[m_afterNames.length] = advice;

                m_afterNames = new String[m_afterNames.length + 1];
                java.lang.System.arraycopy(tmp, 0, m_afterNames, 0, tmp.length);

                m_afterIndexes = new IndexTuple[m_afterNames.length];
                for (int i = 0, j = m_afterNames.length; i < j; i++) {
                    m_afterIndexes[i] = SystemLoader.getSystem(m_uuid).
                            getAspectManager().getAdviceIndexFor(m_afterNames[i]);
                }
            }
        }
    }

    /**
     * Removes a before advice from the pointcut.
     *
     * @param advice the name of the before advice to remove
     */
    public void removeBeforeAdvice(final String advice) {
        if (advice == null || advice.trim().length() == 0) throw new IllegalArgumentException("name of advice to remove can not be null or an empty string");
        synchronized (m_beforeNames) {
            synchronized (m_beforeIndexes) {
                int index = -1;
                for (int i = 0; i < m_beforeNames.length; i++) {
                    if (m_beforeNames[i].equals(advice)) {
                        index = i;
                        break;
                    }
                }
                if (index == -1) throw new RuntimeException("can not remove before advice with the name " + advice + ": no such advice");

                final String[] names = new String[m_beforeNames.length - 1];
                int j, k;
                for (j = 0, k = 0; j < index; j++, k++) {
                    names[j] = m_beforeNames[j];
                }
                j++;
                for (; j < m_beforeNames.length; j++, k++) {
                    names[k] = m_beforeNames[j];
                }
                m_beforeNames = new String[names.length];
                java.lang.System.arraycopy(names, 0, m_beforeNames, 0, names.length);

                final IndexTuple[] indexes = new IndexTuple[m_beforeIndexes.length - 1];
                for (j = 0, k = 0; j < index; j++, k++) {
                    indexes[j] = m_beforeIndexes[j];
                }
                j++;
                for (; j < m_beforeIndexes.length; j++, k++) {
                    indexes[k] = m_beforeIndexes[j];
                }
                m_beforeIndexes = new IndexTuple[indexes.length];
                java.lang.System.arraycopy(indexes, 0, m_beforeIndexes, 0, indexes.length);
            }
        }
    }

    /**
     * Removes a after advice from the pointcut.
     *
     * @param advice the name of the before advice to remove
     */
    public void removeAfterAdvice(final String advice) {
        if (advice == null || advice.trim().length() == 0) throw new IllegalArgumentException("name of advice to remove can not be null or an empty string");
        synchronized (m_afterNames) {
            synchronized (m_afterIndexes) {
                int index = -1;
                for (int i = 0; i < m_afterNames.length; i++) {
                    if (m_afterNames[i].equals(advice)) {
                        index = i;
                        break;
                    }
                }
                if (index == -1) throw new RuntimeException("can not remove after advice with the name " + advice + ": no such advice");

                final String[] names = new String[m_afterNames.length - 1];
                int j, k;
                for (j = 0, k = 0; j < index; j++, k++) {
                    names[j] = m_afterNames[j];
                }
                j++;
                for (; j < m_afterNames.length; j++, k++) {
                    names[k] = m_afterNames[j];
                }
                m_afterNames = new String[names.length];
                java.lang.System.arraycopy(names, 0, m_afterNames, 0, names.length);

                final IndexTuple[] indexes = new IndexTuple[m_afterIndexes.length - 1];
                for (j = 0, k = 0; j < index; j++, k++) {
                    indexes[j] = m_afterIndexes[j];
                }
                j++;
                for (; j < m_afterIndexes.length; j++, k++) {
                    indexes[k] = m_afterIndexes[j];
                }
                m_afterIndexes = new IndexTuple[indexes.length];
                java.lang.System.arraycopy(indexes, 0, m_afterIndexes, 0, indexes.length);
            }
        }
    }

    /**
     * Checks if the pointcuts has a certain before advice.
     *
     * @param advice the advice to check for existence
     * @return boolean
     */
    public boolean hasBeforeAdvice(final String advice) {
        for (int i = 0; i < m_beforeNames.length; i++) {
            if (m_beforeNames[i].equals(advice)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the pointcuts has a certain after advice.
     *
     * @param advice the advice to check for existence
     * @return boolean
     */
    public boolean hasAfterAdvice(final String advice) {
        for (int i = 0; i < m_afterNames.length; i++) {
            if (m_afterNames[i].equals(advice)) {
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
    public NameIndexTuple[] getBeforeAdviceIndexTuples() {
        synchronized (m_beforeIndexes) {
            synchronized (m_beforeNames) {
                final NameIndexTuple[] tuples = new NameIndexTuple[m_beforeNames.length];
                for (int i = 0; i < m_beforeNames.length; i++) {
                    tuples[i] = new NameIndexTuple(m_beforeNames[i], m_beforeIndexes[i]);
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
    public void setBeforeAdviceIndexTuples(final NameIndexTuple[] tuple) {
        synchronized (m_beforeIndexes) {
            synchronized (m_beforeNames) {
                m_beforeNames = new String[tuple.length];
                m_beforeIndexes = new IndexTuple[tuple.length];
                for (int i = 0; i < tuple.length; i++) {
                    m_beforeNames[i] = tuple[i].getName();
                    m_beforeIndexes[i] = tuple[i].getIndex();
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
    public NameIndexTuple[] getAfterAdviceIndexTuples() {
        synchronized (m_afterIndexes) {
            synchronized (m_afterNames) {
                final NameIndexTuple[] tuples = new NameIndexTuple[m_afterNames.length];
                for (int i = 0; i < m_afterNames.length; i++) {
                    tuples[i] = new NameIndexTuple(m_afterNames[i], m_afterIndexes[i]);
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
    public void setAfterAdviceIndexTuples(final NameIndexTuple[] tuple) {
        synchronized (m_afterIndexes) {
            synchronized (m_afterNames) {
                m_afterNames = new String[tuple.length];
                m_afterIndexes = new IndexTuple[tuple.length];
                for (int i = 0; i < tuple.length; i++) {
                    m_afterNames[i] = tuple[i].getName();
                    m_afterIndexes[i] = tuple[i].getIndex();
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
     * Returns a list with the indexes for the before advices for the pointcut.
     *
     * @return the before advice indexes
     */
    public IndexTuple[] getBeforeAdviceIndexes() {
        return m_beforeIndexes;
    }

    /**
     * Returns a list with the names for the before advices for the pointcut.
     *
     * @return the before advice names
     */
    public String[] getBeforeAdviceNames() {
        return m_beforeNames;
    }

    /**
     * Returns a list with the indexes for the after advices for the pointcut.
     *
     * @return the before advice indexes
     */
    public IndexTuple[] getAfterAdviceIndexes() {
        return m_afterIndexes;
    }

    /**
     * Returns a list with the names for the after advices for the pointcut.
     *
     * @return the after advice names
     */
    public String[] getAfterAdviceNames() {
        return m_afterNames;
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
     * Sets the before advices.
     * Caution: the index A name arrays have to be in synch.
     *
     * @param indexes the new before advice index array
     * @param names the new before advice names array
     */
    public void setBeforeAdvices(final IndexTuple[] indexes, final String[] names) {
        synchronized (m_beforeIndexes) {
            synchronized (m_beforeNames) {
                m_beforeIndexes = indexes;
                m_beforeNames = names;
            }
        }
    }

    /**
     * Sets the after advices.
     * Caution: the index A name arrays have to be in synch.
     *
     * @param indexes the new after advice index array
     * @param names the new after advice names array
     */
    public void setAfterAdvices(final IndexTuple[] indexes, final String[] names) {
        synchronized (m_afterIndexes) {
            synchronized (m_afterNames) {
                m_afterIndexes = indexes;
                m_afterNames = names;
            }
        }
    }

    /**
     * Returns the pointcut type.
     *
     * @return the pointcut type
     */
    public PointcutType getPointcutType() {
        return PointcutType.GET;
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
        m_beforeNames = (String[])fields.get("m_beforeNames", null);
        m_afterNames = (String[])fields.get("m_afterNames", null);
        m_beforeIndexes = (IndexTuple[])fields.get("m_beforeIndexes", null);
        m_afterIndexes = (IndexTuple[])fields.get("m_aftercIndexes", null);
        m_uuid = (String)fields.get("m_uuid", null);
    }
}
