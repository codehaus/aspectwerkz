/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.codehaus.aspectwerkz.pointcut.FieldPointcut;
import org.codehaus.aspectwerkz.pointcut.MethodPointcut;
import org.codehaus.aspectwerkz.pointcut.ThrowsPointcut;
import org.codehaus.aspectwerkz.pointcut.CallerSidePointcut;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.FieldMetaData;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.regexp.PointcutPatternTuple;
import org.codehaus.aspectwerkz.regexp.MethodPattern;
import org.codehaus.aspectwerkz.util.SequencedHashMap;

/**
 * Manages pointcuts and introductions defined by this aspect.
 *
 * @TODO: the getMethodPointcuts, getCallerSidePointcuts etc. (from xmldef) are used but not intiutive for users of attribdef. Better names for them would be getExecutionPointcuts and getCallPointcuts. How do we find a common denominator? Different AspectMetaData classes?
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AspectMetaData {

    /**
     * Holds references to all the the method pointcuts.
     */
    protected final List m_methodPointcuts = new ArrayList();

    /**
     * Holds references to all the the getField pointcuts.
     */
    protected final List m_getFieldPointcuts = new ArrayList();

    /**
     * Holds references to all the the setField pointcuts.
     */
    protected final List m_setFieldPointcuts = new ArrayList();

    /**
     * Holds references to all the the throws pointcuts.
     */
    protected final List m_throwsPointcuts = new ArrayList();

    /**
     * Holds references to all the the caller side pointcuts.
     */
    protected final List m_callerSidePointcuts = new ArrayList();

    /**
     * Maps the method to all the cflow method it should care about.
     */
    private final Map m_methodToCFlowMethodsMap = new SequencedHashMap();

    /**
     * Holds references to all the the introductions.
     */
    protected String[] m_introductions = new String[0];

    /**
     * The name of the aspect.
     */
    protected final String m_name;

    /**
     * The UUID for the AspectWerkz system.
     */
    protected final String m_uuid;

    /**
     * Creates a new aspect.
     *
     * @param name the name of the aspect
     */
    public AspectMetaData(final String name) {
        this(System.DEFAULT_SYSTEM, name);
    }

    /**
     * Creates a new aspect.
     *
     * @param uuid the UUID for the AspectWerkz system
     * @param name the name of the aspect
     */
    public AspectMetaData(final String uuid, final String name) {
        if (uuid == null) throw new IllegalArgumentException("uuid can not be null");
        if (name == null) throw new IllegalArgumentException("name can not be null");
        m_uuid = uuid;
        m_name = name;
    }

    /**
     * Returns the name of the aspect.
     *
     * @return the aspect name
     */
    public String getName() {
        return m_name;
    }

    /**
     * Adds an introduction to the open class.
     *
     * @param introduction the name of the introduction to add
     */
    public final void addIntroduction(final String introduction) {
        synchronized (m_introductions) {
            final String[] tmp = new String[m_introductions.length + 1];
            java.lang.System.arraycopy(m_introductions, 0, tmp, 0, m_introductions.length);
            tmp[m_introductions.length] = introduction;
            m_introductions = new String[m_introductions.length + 1];
            java.lang.System.arraycopy(tmp, 0, m_introductions, 0, tmp.length);
        }
    }

    /**
     * Adds an array with introductions to the open class.<br/>
     *
     * @param introductions the introductions to add
     */
    public final void addIntroductions(final String[] introductions) {
        synchronized (m_introductions) {
            final String[] clone = new String[introductions.length];
            java.lang.System.arraycopy(introductions, 0, clone, 0, introductions.length);
            final String[] tmp = new String[m_introductions.length + introductions.length];
            int i;
            for (i = 0; i < m_introductions.length; i++) {
                tmp[i] = m_introductions[i];
            }
            for (int j = 0; j < clone.length; i++, j++) {
                tmp[i] = clone[j];
            }
            m_introductions = new String[tmp.length];
            java.lang.System.arraycopy(tmp, 0, m_introductions, 0, tmp.length);
        }
    }

    /**
     * Adds a new method pointcut to the class.
     *
     * @param methodPointcut the method pointcut to add
     */
    public void addMethodPointcut(final MethodPointcut methodPointcut) {
        synchronized (m_methodPointcuts) {
            m_methodPointcuts.add(methodPointcut);
        }
    }

    /**
     * Adds a new method pointcuts to the class.
     *
     * @param methodPointcuts an arrayt with the method pointcut to add
     */
    public void addMethodPointcuts(final MethodPointcut[] methodPointcuts) {
        synchronized (m_methodPointcuts) {
            for (int i = 0; i < methodPointcuts.length; i++) {
                m_methodPointcuts.add(methodPointcuts[i]);
            }
        }
    }

    /**
     * Adds a new throws pointcut to the class.
     *
     * @param throwsPointcut the throws pointcut to add
     */
    public void addThrowsPointcut(final ThrowsPointcut throwsPointcut) {
        synchronized (m_throwsPointcuts) {
            m_throwsPointcuts.add(throwsPointcut);
        }
    }

    /**
     * Adds an array with new throws pointcuts to the class.
     *
     * @param throwsPointcuts the throws pointcuts to add
     */
    public void addThrowsPointcuts(final ThrowsPointcut[] throwsPointcuts) {
        synchronized (m_throwsPointcuts) {
            for (int i = 0; i < throwsPointcuts.length; i++) {
                m_throwsPointcuts.add(throwsPointcuts[i]);
            }
        }
    }

    /**
     * Adds a new get field pointcut to the class.
     *
     * @param fieldPointcut the field pointcut to add
     */
    public final void addGetFieldPointcut(final FieldPointcut fieldPointcut) {
        synchronized (m_getFieldPointcuts) {
            m_getFieldPointcuts.add(fieldPointcut);
        }
    }

    /**
     * Adds an array with new get field pointcuts to the class.
     *
     * @param fieldPointcuts the field pointcuts to add
     */
    public void addGetFieldPointcuts(final FieldPointcut[] fieldPointcuts) {
        synchronized (m_getFieldPointcuts) {
            for (int i = 0; i < fieldPointcuts.length; i++) {
                m_getFieldPointcuts.add(fieldPointcuts[i]);
            }
        }
    }

    /**
     * Adds a new set field pointcut to the class.
     *
     * @param fieldPointcut the field pointcut to add
     */
    public void addSetFieldPointcut(final FieldPointcut fieldPointcut) {
        synchronized (m_setFieldPointcuts) {
            m_setFieldPointcuts.add(fieldPointcut);
        }
    }

    /**
     * Adds an array with new set field pointcuts to the class.
     *
     * @param fieldPointcuts the field pointcuts to add
     */
    public void addSetFieldPointcuts(final FieldPointcut[] fieldPointcuts) {
        synchronized (m_setFieldPointcuts) {
            for (int i = 0; i < fieldPointcuts.length; i++) {
                m_setFieldPointcuts.add(fieldPointcuts[i]);
            }
        }
    }

    /**
     * Adds a new caller side pointcut to the class.
     *
     * @param callerSidePointcut the caller side pointcut to add
     */
    public final void addCallerSidePointcut(final CallerSidePointcut callerSidePointcut) {
        synchronized (m_callerSidePointcuts) {
            m_callerSidePointcuts.add(callerSidePointcut);
        }
    }

    /**
     * Adds an array with new caller side pointcuts to the class.
     *
     * @param callerSidePointcuts the caller side pointcuts to add
     */
    public final void addCallerSidePointcuts(final CallerSidePointcut[] callerSidePointcuts) {
        synchronized (m_callerSidePointcuts) {
            for (int i = 0; i < callerSidePointcuts.length; i++) {
                m_callerSidePointcuts.add(callerSidePointcuts[i]);
            }
        }
    }

    /**
     * Adds a new method pattern to the method->cflow-method map.
     *
     * @param patternTuple the method pointcut definition
     * @param cflowPatternTuple the cflow pointcut definition
     */
    public void addMethodToCFlowMethodMap(final PointcutPatternTuple patternTuple,
                                          final PointcutPatternTuple cflowPatternTuple) {
        List cflowPatterns = (List)m_methodToCFlowMethodsMap.get(patternTuple);
        if (cflowPatterns != null) {
            cflowPatterns.add(cflowPatternTuple);
        }
        else {
            cflowPatterns = new ArrayList();
            cflowPatterns.add(cflowPatternTuple);
            m_methodToCFlowMethodsMap.put(patternTuple, cflowPatterns);
        }
    }

    /**
     * Returns the introductions for the open class.
     *
     * @return an array with the introductions for the class
     */
    public String[] getIntroductions() {
        return m_introductions;
    }

    /**
     * Returns the method pointcut for a specific uuid and expression.
     *
     * @param uuid the uuid
     * @param expression the expression
     * @return the method pointcut
     */
    public MethodPointcut getMethodPointcut(final String uuid, final String expression) {
        MethodPointcut pointcut = null;
        for (Iterator it = m_methodPointcuts.iterator(); it.hasNext();) {
            pointcut = (MethodPointcut)it.next();
            if (m_uuid.equals(uuid) && pointcut.getExpression().equals(expression)) {
                break;
            }
        }
        return pointcut;
    }

    /**
     * Returns all the pointcuts for the method join point specified.
     *
     * @param classMetaData the meta-data for the class
     * @param methodMetaData the meta-data for the method
     * @return the pointcuts
     */
    public List getMethodPointcuts(final ClassMetaData classMetaData,
                                   final MethodMetaData methodMetaData) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");

        List pointcutList = new ArrayList();
        for (Iterator it = m_methodPointcuts.iterator(); it.hasNext();) {
            MethodPointcut pointcut = (MethodPointcut)it.next();
            if (pointcut.matches(classMetaData, methodMetaData)) {
                pointcutList.add(pointcut);
            }
        }
        return pointcutList;
    }

    /**
     * Returns the get field pointcut for a specific uuid and expression.
     *
     * @param uuid the uuid
     * @param expression the expression
     * @return the method pointcut
     */
    public FieldPointcut getGetFieldPointcut(final String uuid, final String expression) {
        FieldPointcut pointcut = null;
        for (Iterator it = m_getFieldPointcuts.iterator(); it.hasNext();) {
            pointcut = (FieldPointcut)it.next();
            if (m_uuid.equals(uuid) && pointcut.getExpression().equals(expression)) {
                break;
            }
        }
        return pointcut;
    }

    /**
     * Returns all the pointcuts for the method join point specified.
     *
     * @param classMetaData the meta-data for the class
     * @param fieldMetaData the meta-data for the field
     * @return the pointcuts
     */
    public List getGetFieldPointcuts(final ClassMetaData classMetaData,
                                     final FieldMetaData fieldMetaData) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (fieldMetaData == null) throw new IllegalArgumentException("field meta-data can not be null");

        List pointcutList = new ArrayList();
        for (Iterator it = m_getFieldPointcuts.iterator(); it.hasNext();) {
            final FieldPointcut pointcut = (FieldPointcut)it.next();
            if (pointcut.matches(classMetaData, fieldMetaData)) {
                pointcutList.add(pointcut);
            }
        }
        return pointcutList;
    }

    /**
     * Returns the set field pointcut for a specific uuid and expression.
     *
     * @param uuid the uuid
     * @param expression the expression
     * @return the method pointcut
     */
    public FieldPointcut getSetFieldPointcut(final String uuid, final String expression) {
        FieldPointcut pointcut = null;
        for (Iterator it = m_setFieldPointcuts.iterator(); it.hasNext();) {
            pointcut = (FieldPointcut)it.next();
            if (m_uuid.equals(uuid) && pointcut.getExpression().equals(expression)) {
                break;
            }
        }
        return pointcut;
    }

    /**
     * Returns all the pointcuts for the method join point specified.
     *
     * @param classMetaData the meta-data for the class
     * @param fieldMetaData the meta-data for the field
     * @return the pointcuts
     */
    public List getSetFieldPointcuts(final ClassMetaData classMetaData,
                                     final FieldMetaData fieldMetaData) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (fieldMetaData == null) throw new IllegalArgumentException("field meta-data can not be null");

        List pointcutList = new ArrayList();
        for (Iterator it = m_setFieldPointcuts.iterator(); it.hasNext();) {
            final FieldPointcut pointcut = (FieldPointcut)it.next();
            if (pointcut.matches(classMetaData, fieldMetaData)) {
                pointcutList.add(pointcut);
            }
        }
        return pointcutList;
    }

    /**
     * Returns the throws pointcut for a specific uuid and expression.
     *
     * @param uuid the uuid
     * @param expression the expression
     * @return the method pointcut
     */
    public ThrowsPointcut getThrowsPointcut(final String uuid, final String expression) {
        ThrowsPointcut pointcut = null;
        for (Iterator it = m_throwsPointcuts.iterator(); it.hasNext();) {
            pointcut = (ThrowsPointcut)it.next();
            if (m_uuid.equals(uuid) && pointcut.getExpression().equals(expression)) {
                break;
            }
        }
        return pointcut;
    }

    /**
     * Returns the pointcut for the method/exception join point specified.
     *
     * @param classMetaData the meta-data for the class
     * @param methodMetaData the method meta-data
     * @return the pointcut
     */
    public List getThrowsPointcuts(final ClassMetaData classMetaData,
                                   final MethodMetaData methodMetaData) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");

        List pointcutList = new ArrayList();
        for (Iterator it = m_throwsPointcuts.iterator(); it.hasNext();) {
            final ThrowsPointcut pointcut = (ThrowsPointcut)it.next();
            if (pointcut.matches(classMetaData, methodMetaData)) {
                pointcutList.add(pointcut);
            }
        }
        return pointcutList;
    }

    /**
     * Returns the caller side pointcut for a specific uuid and expression.
     *
     * @param uuid the uuid
     * @param expression the expression
     * @return the method pointcut
     */
    public CallerSidePointcut getCallerSidePointcut(final String uuid, final String expression) {
        CallerSidePointcut pointcut = null;
        for (Iterator it = m_callerSidePointcuts.iterator(); it.hasNext();) {
            pointcut = (CallerSidePointcut)it.next();
            if (m_uuid.equals(uuid) && pointcut.getExpression().equals(expression)) {
                break;
            }
        }
        return pointcut;
    }

    /**
     * Returns all the pointcuts for the caller side join point specified.
     *
     * @param className the class name
     * @param methodMetaData the meta-data for the method
     * @return the pointcuts
     */
    public List getCallerSidePointcuts(final String className,
                                       final MethodMetaData methodMetaData) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");

        List pointcutList = new ArrayList();
        for (Iterator it = m_callerSidePointcuts.iterator(); it.hasNext();) {
            final CallerSidePointcut pointcut = (CallerSidePointcut)it.next();
            if (pointcut.matches(className, methodMetaData)) {
                pointcutList.add(pointcut);
            }
        }
        return pointcutList;
    }

    /**
     * Returns the cflow pointcut for a specific uuid and expression.
     *
     * @param uuid the uuid
     * @param expression the expression
     * @return the method pointcut
     */
    public FieldPointcut getCFlowPointcut(final String uuid, final String expression) {
       throw new UnsupportedOperationException("not implemented yet");
    }

    /**
     * Returns all the pointcuts for the cflow join point specified.
     *
     * @param className the name of the class
     * @param methodMetaData the meta-data for the method
     * @return the pointcuts
     */
    public List getCFlowPointcuts(final String className,
                                  final MethodMetaData methodMetaData) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");

        List pointcutList = new ArrayList();
        for (Iterator it = m_methodToCFlowMethodsMap.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            PointcutPatternTuple methodPatternTuple = (PointcutPatternTuple)entry.getKey();
            if (methodPatternTuple.getClassPattern().matches(className) &&
                    ((MethodPattern)methodPatternTuple.getPattern()).matches(methodMetaData)) {
                pointcutList.addAll((List)entry.getValue());
            }
        }
        return pointcutList;
    }

    /**
     * Checks if a specific method/exceptoin join point has a specific throws
     * pointcut configured.
     *
     * @param classMetaData the meta-data for the class
     * @param methodMetaData the meta-data for the method
     * @param exception the name pattern of the exception
     * @return boolean
     */
    public boolean hasThrowsPointcut(final ClassMetaData classMetaData,
                                     final MethodMetaData methodMetaData,
                                     final String exception) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");
        if (exception == null) throw new IllegalArgumentException("exception class name can not be null");

        for (Iterator it = m_throwsPointcuts.iterator(); it.hasNext();) {
            final ThrowsPointcut pointcut = (ThrowsPointcut)it.next();
            if (pointcut.matches(classMetaData, methodMetaData, exception)) {
                return true;
            }
        }
        return false;
    }

    // --- over-ridden methods ---

    public String toString() {
        return "["
                + super.toString()
                + ": "
                + "," + m_name
                + "," + m_introductions
                + "," + m_methodPointcuts
                + "," + m_getFieldPointcuts
                + "," + m_setFieldPointcuts
                + "," + m_callerSidePointcuts
                + "]";
    }

    public int hashCode() {
        int result = 17;
        result = 37 * result + hashCodeOrZeroIfNull(m_name);
        result = 37 * result + hashCodeOrZeroIfNull(m_introductions);
        result = 37 * result + hashCodeOrZeroIfNull(m_methodPointcuts);
        result = 37 * result + hashCodeOrZeroIfNull(m_getFieldPointcuts);
        result = 37 * result + hashCodeOrZeroIfNull(m_setFieldPointcuts);
        result = 37 * result + hashCodeOrZeroIfNull(m_callerSidePointcuts);
        return result;
    }

    protected static int hashCodeOrZeroIfNull(final Object o) {
        if (null == o) return 19;
        return o.hashCode();
    }

    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof AspectMetaData)) return false;
        final AspectMetaData obj = (AspectMetaData)o;
        return areEqualsOrBothNull(obj.m_name, this.m_name)
                && areEqualsOrBothNull(obj.m_introductions, this.m_introductions)
                && areEqualsOrBothNull(obj.m_methodPointcuts, this.m_methodPointcuts)
                && areEqualsOrBothNull(obj.m_getFieldPointcuts, this.m_getFieldPointcuts)
                && areEqualsOrBothNull(obj.m_setFieldPointcuts, this.m_setFieldPointcuts)
                && areEqualsOrBothNull(obj.m_callerSidePointcuts, this.m_callerSidePointcuts);
    }

    protected static boolean areEqualsOrBothNull(final Object o1, final Object o2) {
        if (null == o1) return (null == o2);
        return o1.equals(o2);
    }
}
