/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.expression;

import org.codehaus.aspectwerkz.regexp.ClassPattern;
import org.codehaus.aspectwerkz.regexp.Pattern;
import org.codehaus.aspectwerkz.regexp.PatternTuple;
import org.codehaus.aspectwerkz.exception.ExpressionException;
import org.codehaus.aspectwerkz.definition.PatternFactory;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.InterfaceMetaData;
import org.codehaus.aspectwerkz.metadata.MemberMetaData;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for leaf expression (pattern)
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public abstract class LeafExpression extends Expression {

    /**
     * Hierarchical flag.
     */
    protected boolean m_isHierarchical = false;

    /**
     * The compiled class pattern.
     */
    protected ClassPattern m_classPattern;

    /**
     * The compiled member pattern.
     */
    protected Pattern m_memberPattern;

    protected LeafExpression(final ExpressionNamespace namespace,
                        final String expression,
                        final String packageNamespace,
                        final String pointcutName,
                        final PointcutType type) {
        super(namespace, expression, packageNamespace, pointcutName, type);

        compilePattern();
    }

    /**
     * Checks if the expression is hierachical.
     *
     * @return boolean
     */
    public boolean isHierarchical() {
        return m_isHierarchical;
    }

    /**
     * Compiles the pattern for the expression.
     */
    protected void compilePattern() {
        PatternTuple tuple = null;
        if (m_type == null) {
            throw new ExpressionException("pointcut type in context can not be null");
        }
        if (m_type.equals(PointcutType.EXECUTION)) {
            tuple = PatternFactory.createMethodPatternTuple(m_expression, m_package);
            m_memberPattern = Pattern.compileMethodPattern(tuple.getMemberPattern());
            m_isHierarchical = tuple.isHierarchical();
            m_classPattern = Pattern.compileClassPattern(tuple.getCalleeClassPattern());
        }
        else if (m_type.equals(PointcutType.CALL)) {
            tuple = PatternFactory.createCallPatternTuple(m_expression, m_package);
            m_memberPattern = Pattern.compileCallerSidePattern(tuple.getMemberPattern());
            m_isHierarchical = tuple.isHierarchical();
            m_classPattern = Pattern.compileClassPattern(tuple.getCallerClassPattern());
        }
        else if (m_type.equals(PointcutType.SET) || m_type.equals(PointcutType.GET)) {
            tuple = PatternFactory.createFieldPatternTuple(m_expression, m_package);
            m_memberPattern = Pattern.compileFieldPattern(tuple.getMemberPattern());
            m_isHierarchical = tuple.isHierarchical();
            m_classPattern = Pattern.compileClassPattern(tuple.getCalleeClassPattern());
        }
        else if (m_type.equals(PointcutType.THROWS)) {
            tuple = PatternFactory.createThrowsPatternTuple(m_expression, m_package);
            m_memberPattern = Pattern.compileThrowsPattern(tuple.getMemberPattern());
            m_isHierarchical = tuple.isHierarchical();
            m_classPattern = Pattern.compileClassPattern(tuple.getCalleeClassPattern());
        }
        else if (m_type.equals(PointcutType.CFLOW)) {
            // cflow compiled as caller side pattern
            tuple = PatternFactory.createCallPatternTuple(m_expression, m_package);
            m_memberPattern = Pattern.compileCallerSidePattern(tuple.getMemberPattern());
            m_isHierarchical = tuple.isHierarchical();
            m_classPattern = Pattern.compileClassPattern(tuple.getCalleeClassPattern());
        }
        else if (m_type.equals(PointcutType.CLASS)) {
            tuple = PatternFactory.createClassPatternTuple(m_expression, m_package);
            m_isHierarchical = tuple.isHierarchical();
            m_classPattern = Pattern.compileClassPattern(tuple.getCalleeClassPattern());
        }
    }

    /**
     * Match class pattern only
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public boolean match(final ClassMetaData classMetaData) {
        boolean matchesClassPattern = false;
        if (m_isHierarchical) {
            if (matchSuperClasses(classMetaData)) {
                matchesClassPattern = true;
            }
        }
        else {
            matchesClassPattern = m_classPattern.matches(classMetaData.getName());
        }
        return matchesClassPattern;
    }

    /**
     * Match one part appearing in IN / NOT IN sub-expression
     * Makes sense only with CallExpression
     *
     * @param classMetaData
     * @return true if match
     */
    public boolean matchInOrNotIn(final ClassMetaData classMetaData) {
        if ( ! m_type.equals(PointcutType.CFLOW) )
            throw new RuntimeException("matchIn called on non CflowExpression " + m_type.toString());
        return match(classMetaData);
    }

    /**
     * Match one part appearing in IN / NOT IN sub-expression
     * Makes sense only with CallExpression
     *
     * @param classMetaData
     * @return true if match
     */
    public boolean matchInOrNotIn(final ClassMetaData classMetaData, final MemberMetaData memberMetaData) {
        if ( ! m_type.equals(PointcutType.CFLOW) )
            throw new RuntimeException("matchIn called on non CflowExpression " + m_type.toString());
        return match(classMetaData, memberMetaData);
    }

    /**
     * Tries to finds a match at some superclass in the hierarchy.
     * <p/>Only checks for a class match to allow early filtering.
     * <p/>Recursive.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    protected boolean matchSuperClasses(final ClassMetaData classMetaData) {
        if (classMetaData == null) {
            return false;
        }
        // match the class/super class
        if (m_classPattern.matches(classMetaData.getName())) {
            return true;
        }
        else {
            // match the interfaces for the class
            if (matchInterfaces(classMetaData.getInterfaces(), classMetaData)) {
                return true;
            }
            // no match; get the next superclass
            return matchSuperClasses(classMetaData.getSuperClass());
        }
    }

    /**
     * Tries to finds a match at some interface in the hierarchy.
     * <p/>Only checks for a class match to allow early filtering.
     * <p/>Recursive.
     *
     * @param interfaces the interfaces
     * @param classMetaData the class meta-data
     * @return boolean
     */
    protected boolean matchInterfaces(final List interfaces, final ClassMetaData classMetaData) {
        if (interfaces.isEmpty()) {
            return false;
        }
        for (Iterator it = interfaces.iterator(); it.hasNext();) {
            InterfaceMetaData interfaceMD = (InterfaceMetaData)it.next();
            if (m_classPattern.matches(interfaceMD.getName())) {
                return true;
            }
            else {
                if (matchInterfaces(interfaceMD.getInterfaces(), classMetaData)) {
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
     * Checks if the expression matches a certain join point.
     * <p/>Special case in the API which tries to match exception types as well.
     *
     * Overrided by ThrowsExpression
     *
     * @todo handles the special case with ThrowsExpressions which needs to match on exception type (which breaks clean the API), how to handle this in a cleaner way?
     *
     * @param classMetaData the class meta-data
     * @param memberMetaData the meta-data for the member
     * @param exceptionType the exception type (null => match all)
     * @return boolean
     */
    public boolean match(final ClassMetaData classMetaData,
                         final MemberMetaData memberMetaData,
                         final String exceptionType) {
        return match(classMetaData, memberMetaData);
    }

    public Map getCflowExpressions() {
        return new HashMap();
    }

}
