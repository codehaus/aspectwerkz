/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.pointcut;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.io.ObjectInputStream;

import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.JexlHelper;
import org.apache.commons.jexl.JexlContext;

import org.codehaus.aspectwerkz.AspectWerkz;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.regexp.CallerSidePattern;
import org.codehaus.aspectwerkz.regexp.PointcutPatternTuple;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.InterfaceMetaData;
import org.codehaus.aspectwerkz.definition.PointcutDefinition;
import org.codehaus.aspectwerkz.NameIndexTuple;
import org.codehaus.aspectwerkz.IndexTuple;

/**
 * Implements the pointcut concept for caller side method access.
 * A pointcut is an abstraction of a well defined point of execution in the program.<br/>
 * Could matches one or many points as long as they are well defined.<br/>
 * Stores the advices for this specific pointcut.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class CallerSidePointcut {

    /**
     * The pattern for the pointcut.
     */
    protected String m_expression;

    /**
     * The Jexl expression.
     */
    protected transient Expression m_jexlExpr;

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
     * Creates a new caller side pointcut.
     *
     * @param pattern the pattern of the pointcut
     */
    public CallerSidePointcut(final String pattern) {
        this(AspectWerkz.DEFAULT_SYSTEM, pattern);
    }

    /**
     * Creates a new caller side pointcut.
     *
     * @param uuid the UUID for the AspectWerkz system
     * @param pattern the pattern for the pointcut
     */
    public CallerSidePointcut(final String uuid, final String pattern) {
        if (uuid == null) throw new IllegalArgumentException("uuid can not be null");
        if (pattern == null || pattern.trim().length() == 0) throw new IllegalArgumentException("pattern of pointcut can not be null or an empty string");
        m_uuid = uuid;
        m_expression = pattern;
        try {
            m_jexlExpr = ExpressionFactory.createExpression(m_expression);
        }
        catch (Exception e) {
            throw new RuntimeException("could not create jexl expression from: " + m_expression);
        }
    }

    /**
     * Adds a new pointcut definition.
     *
     * @param pointcut the pointcut definition
     */
    public void addPointcutDef(final PointcutDefinition pointcut) {
        m_pointcutDefs.put(pointcut.getName(), new PointcutPatternTuple(
                pointcut.getRegexpClassPattern(),
                pointcut.getRegexpPattern(),
                pointcut.isHierarchical()));
    }

    /**
     * Adds a pre advice to the pointcut.
     *
     * @param advice the name of the advice to add
     */
    public void addPreAdvice(final String advice) {
        if (advice == null || advice.trim().length() == 0) throw new IllegalArgumentException("name of advice to add can not be null or an empty string");
        synchronized (m_preNames) {
            synchronized (m_preIndexes) {
                final String[] tmp = new String[m_preNames.length + 1];
                System.arraycopy(m_preNames, 0, tmp, 0, m_preNames.length);

                tmp[m_preNames.length] = advice;

                m_preNames = new String[m_preNames.length + 1];
                System.arraycopy(tmp, 0, m_preNames, 0, tmp.length);

                m_preIndexes = new IndexTuple[m_preNames.length];
                for (int i = 0, j = m_preNames.length; i < j; i++) {
                    m_preIndexes[i] = AspectWerkz.getSystem(m_uuid).getAdviceIndexFor(m_preNames[i]);
                }
            }
        }
    }

    /**
     * Adds post advice to the pointcut.
     *
     * @param advice the name of the advice to add
     */
    public void addPostAdvice(final String advice) {
        if (advice == null || advice.trim().length() == 0) throw new IllegalArgumentException("name of advice to add can not be null or an empty string");
        synchronized (m_postNames) {
            synchronized (m_postIndexes) {
                final String[] tmp = new String[m_postNames.length + 1];
                System.arraycopy(m_postNames, 0, tmp, 0, m_postNames.length);

                tmp[m_postNames.length] = advice;

                m_postNames = new String[m_postNames.length + 1];
                System.arraycopy(tmp, 0, m_postNames, 0, tmp.length);

                m_postIndexes = new IndexTuple[m_postNames.length];
                for (int i = 0, j = m_postNames.length; i < j; i++) {
                    m_postIndexes[i] = AspectWerkz.getSystem(m_uuid).
                            getAdviceIndexFor(m_postNames[i]);
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
                System.arraycopy(advicesToAdd, 0, clone, 0, advicesToAdd.length);

                final String[] tmp = new String[
                        m_preNames.length + advicesToAdd.length];
                System.arraycopy(m_preNames, 0, tmp, 0, m_preNames.length);
                System.arraycopy(clone, 0, tmp, m_preNames.length, tmp.length);

                m_preNames = new String[tmp.length];
                System.arraycopy(tmp, 0, m_preNames, 0, tmp.length);

                m_preIndexes = new IndexTuple[m_preNames.length];
                for (int j = 0; j < m_preNames.length; j++) {
                    m_preIndexes[j] = AspectWerkz.getSystem(m_uuid).
                            getAdviceIndexFor(m_preNames[j]);
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
                System.arraycopy(advicesToAdd, 0, clone, 0, advicesToAdd.length);

                final String[] tmp = new String[m_postNames.length + advicesToAdd.length];
                System.arraycopy(m_postNames, 0, tmp, 0, m_postNames.length);
                System.arraycopy(clone, 0, tmp, m_postNames.length, tmp.length);

                m_postNames = new String[tmp.length];
                System.arraycopy(tmp, 0, m_postNames, 0, tmp.length);

                m_postIndexes = new IndexTuple[m_postNames.length];
                for (int j = 0; j < m_postNames.length; j++) {
                    m_postIndexes[j] = AspectWerkz.getSystem(m_uuid).
                            getAdviceIndexFor(m_postNames[j]);
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
                System.arraycopy(names, 0, m_preNames, 0, names.length);

                final IndexTuple[] indexes = new IndexTuple[m_preIndexes.length - 1];
                for (j = 0, k = 0; j < index; j++, k++) {
                    indexes[j] = m_preIndexes[j];
                }
                j++;
                for (; j < m_preIndexes.length; j++, k++) {
                    indexes[k] = m_preIndexes[j];
                }
                m_preIndexes = new IndexTuple[indexes.length];
                System.arraycopy(indexes, 0, m_preIndexes, 0, indexes.length);
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
                System.arraycopy(names, 0, m_postNames, 0, names.length);

                final IndexTuple[] indexes = new IndexTuple[m_postIndexes.length - 1];
                for (j = 0, k = 0; j < index; j++, k++) {
                    indexes[j] = m_postIndexes[j];
                }
                j++;
                for (; j < m_postIndexes.length; j++, k++) {
                    indexes[k] = m_postIndexes[j];
                }
                m_postIndexes = new IndexTuple[indexes.length];
                System.arraycopy(indexes, 0, m_postIndexes, 0, indexes.length);
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
     * Returns the expression for the pointcut.
     *
     * @return the expression
     */
    public String getExpression() {
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
     * Checks if the pointcut matches a certain join point.
     *
     * @param className the class name
     * @param methodMetaData the meta-data for the method
     * @return boolean
     */
    public boolean matches(final String className,
                           final MethodMetaData methodMetaData) {
        try {
            JexlContext jexlContext = JexlHelper.createContext();

            matchPointcutPatterns(jexlContext, className, methodMetaData);

            // evaluate expression
            Boolean result = (Boolean)m_jexlExpr.evaluate(jexlContext);
            if (result == null || !result.booleanValue()) {
                return false;
            }
            else {
                return true;
            }
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Tries to finds a match at some superclass in the hierarchy.
     * Recursive.
     *
     * @param jexlContext the Jexl context
     * @param name the name of the pointcut to evaluate
     * @param classMetaData the class meta-data
     * @param methodMetaData the method meta-data
     * @param pointcutPattern the pointcut pattern
     * @return boolean
     */
    public static boolean matchCallerSidePointcutSuperClasses(
            final JexlContext jexlContext,
            final String name,
            final ClassMetaData classMetaData,
            final MethodMetaData methodMetaData,
            final PointcutPatternTuple pointcutPattern) {

        if (classMetaData == null) {
            return false;
        }

        // match the class/super class
        if (((CallerSidePattern)pointcutPattern.getPattern()).
                matches(classMetaData.getName(), methodMetaData)) {
            jexlContext.getVars().put(name, Boolean.TRUE);
            return true;
        }
        else {
            // match the interfaces for the class
            if (matchCallerSidePointcutInterfaces(
                    jexlContext, name, classMetaData.getInterfaces(),
                    classMetaData, methodMetaData, pointcutPattern)) {
                return true;
            }

            // no match; get the next superclass
            return matchCallerSidePointcutSuperClasses(
                    jexlContext, name, classMetaData.getSuperClass(),
                    methodMetaData, pointcutPattern);
        }
    }

    /**
     * Tries to finds a match at some interface in the hierarchy.
     * Recursive.
     *
     * @param jexlContext the Jexl context
     * @param name the name of the pointcut to evaluate
     * @param interfaces the interfaces
     * @param classMetaData the class meta-data
     * @param methodMetaData the method meta-data
     * @param pointcutPattern the pointcut pattern
     * @return boolean
     */
    private static boolean matchCallerSidePointcutInterfaces(
            final JexlContext jexlContext,
            final String name,
            final List interfaces,
            final ClassMetaData classMetaData,
            final MethodMetaData methodMetaData,
            final PointcutPatternTuple pointcutPattern) {

        if (interfaces.isEmpty()) {
            return false;
        }

        for (Iterator it = interfaces.iterator(); it.hasNext();) {
            InterfaceMetaData interfaceMD = (InterfaceMetaData)it.next();
            if (((CallerSidePattern)pointcutPattern.getPattern()).
                    matches(interfaceMD.getName(), methodMetaData)) {
                jexlContext.getVars().put(name, Boolean.TRUE);
                return true;
            }
            else {
                if (matchCallerSidePointcutInterfaces(
                        jexlContext, name, interfaceMD.getInterfaces(),
                        classMetaData, methodMetaData, pointcutPattern)) {
                    return true;
                }
                else {
                    continue;
                }
            }
        }
        return false;
    }

    /**
     * Matches the method pointcut patterns.
     *
     * @param jexlContext the Jexl context
     * @param className the class name
     * @param methodMetaData the method meta-data
     */
    private void matchPointcutPatterns(final JexlContext jexlContext,
                                       final String className,
                                       final MethodMetaData methodMetaData) {
        for (Iterator it = m_pointcutDefs.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            String name = (String)entry.getKey();
            PointcutPatternTuple pointcutPattern = (PointcutPatternTuple)entry.getValue();

            if (((CallerSidePattern)pointcutPattern.getPattern()).
                    matches(className, methodMetaData)) {
                jexlContext.getVars().put(name, Boolean.TRUE);
            }
            else {
                jexlContext.getVars().put(name, Boolean.FALSE);
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

        m_expression = (String)fields.get("m_expression", null);
        m_pointcutDefs = (Map)fields.get("m_pointcutDefs", null);
        m_preNames = (String[])fields.get("m_preNames", null);
        m_postNames = (String[])fields.get("m_postNames", null);
        m_preIndexes = (IndexTuple[])fields.get("m_preIndexes", null);
        m_postIndexes = (IndexTuple[])fields.get("m_postIndexes", null);
        m_uuid = (String)fields.get("m_uuid", null);

        try {
            m_jexlExpr = ExpressionFactory.createExpression(m_expression);
        }
        catch (Exception e) {
            throw new RuntimeException("could not create jexl expression from: " + m_expression);
        }
    }
}
