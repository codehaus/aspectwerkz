/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.expression;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.aspectwerkz.definition.PatternFactory;
import org.codehaus.aspectwerkz.exception.ExpressionException;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.MemberMetaData;
import org.codehaus.aspectwerkz.metadata.CflowMetaData;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.InterfaceMetaData;
import org.codehaus.aspectwerkz.regexp.ClassPattern;
import org.codehaus.aspectwerkz.regexp.Pattern;
import org.codehaus.aspectwerkz.regexp.PatternTuple;

/**
 * Base class for leaf expression (pattern)
 * A Leaf expression is singled type, and a convenience accessor is provided
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public abstract class LeafExpression extends Expression {

    /**
     * Hierarchical flag.
     */
    protected boolean m_isHierarchical = false;

    /**
     * Hierarchical flag for callee side on Call expressions
     */
    protected boolean m_isHierarchicalCallee = false;

    /**
     * The compiled class pattern.
     */
    protected ClassPattern m_classPattern;

    /**
     * The compiled member pattern.
     */
    protected Pattern m_memberPattern;

    /**
     * Strong typed single type
     */
    protected PointcutType m_type;

    /**
     * Creates a new leaf expression.
     *
     * @param namespace
     * @param expression
     * @param packageNamespace
     * @param pointcutName
     * @param type
     */
    protected LeafExpression(
            final ExpressionNamespace namespace,
            final String expression,
            final String packageNamespace,
            final String pointcutName,
            final PointcutType type) {
        super(namespace, expression, packageNamespace, pointcutName, type);
        m_type = type;
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
     * Checks if the expression is hierachical on Callee side.
     *
     * @return boolean
     */
    public boolean isHierarchicalCallee() {
        return m_isHierarchicalCallee;
    }

    /**
     * Match class pattern only
     *
     * @param classMetaData the class meta-data
     * @param assumedType the assumed type we match with
     * @return boolean
     */
    public boolean match(final ClassMetaData classMetaData, PointcutType assumedType) {
        if (!m_type.equals(PointcutType.CFLOW)) {//TODO AV needed for leaf ?
            if (assumedType.equals(PointcutType.ANY) && !m_type.equals(assumedType)) {
                return false;
            }
        }
        //else {
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
        //}
    }

    /**
     * Match class pattern only
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public boolean match(final ClassMetaData classMetaData) {
        return match(classMetaData, m_type);
    }

    /**
     * Match class and member pattern
     *
     * @param classMetaData the class meta-data
     * @param memberMetaData the member meta-data
     * @param assumedType the assumed type we match with
     * @return boolean
     */
    public boolean match(final ClassMetaData classMetaData, final MemberMetaData memberMetaData, PointcutType assumedType) {
        if (!m_type.equals(PointcutType.CFLOW)) {//TODO AV needed for leaf ?
            if (!assumedType.equals(PointcutType.ANY) && !m_type.equals(assumedType)) {
                return false;
            }
        }
//        else {
            return match(classMetaData, memberMetaData);// implemented by subclasses
//        }
    }

    /**
     * Match one part appearing in IN / NOT IN sub-expression Makes sense only with CallExpression
     *
     * @param classMetaData
     * @return true if match
     */
    public boolean matchInOrNotIn(final ClassMetaData classMetaData) {
        if (!m_type.equals(PointcutType.CFLOW)) {
            return false;
        }
        return match(classMetaData, PointcutType.CFLOW);
    }

    /**
     * Match one part appearing in IN / NOT IN sub-expression Makes sense only with CallExpression
     *
     * @param classMetaData
     * @return true if match
     */
    public boolean matchInOrNotIn(final ClassMetaData classMetaData, final MemberMetaData memberMetaData) {
        if (!m_type.equals(PointcutType.CFLOW)) {
            return false;
        }
        return match(classMetaData, memberMetaData, PointcutType.CFLOW);
    }

    /**
     * Checks if the expression matches a certain join point. <p/>Special case in the API which tries to match exception
     * types as well.
     * <p/>
     * Overrided by ThrowsExpression
     *
     * @param classMetaData  the class meta-data
     * @param memberMetaData the meta-data for the member
     * @param exceptionType  the exception type (null => match all)
     * @param assumedType the assumed type we match with
     * @return boolean
     * @todo handles the special case with ThrowsExpressions which needs to match on exception type (which breaks clean
     * the API), how to handle this in a cleaner way?
     */
    public boolean match(
            final ClassMetaData classMetaData,
            final MemberMetaData memberMetaData,
            final String exceptionType,
            final PointcutType assumedType) {
        return match(classMetaData, memberMetaData, assumedType);
    }

    /**
     * Checks if the expression matches a certain join point. <p/>Special case in the API which tries to match exception
     * types as well.
     * <p/>
     * Overrided by ThrowsExpression
     *
     * @param classMetaData  the class meta-data
     * @param memberMetaData the meta-data for the member
     * @param exceptionType  the exception type (null => match all)
     * @return boolean
     */
    public boolean match(
            final ClassMetaData classMetaData,
            final MemberMetaData memberMetaData,
            final String exceptionType) {
        return match(classMetaData, memberMetaData, exceptionType, m_type);
    }

    /**
     * Returns the cflow expressions.
     *
     * @return the cflow expressions
     */
    public Map getCflowExpressions() {
        return new HashMap();
    }

    /**
     * Build a new expression with only cflow to be evaluated. All other elements are evaluated
     * TODO: ALEX AVCF: should return TRUE | FALSE | this depending on match/nomatch and instanceof CFlowExpr
     *
     * @param classMetaData
     * @param memberMetaData
     * @param assumedType
     * @return simplified expression
     */
    public Expression extractCflowExpression(ClassMetaData classMetaData, MemberMetaData memberMetaData, PointcutType assumedType) {
        return this;
    }

    /**
     * Checks if the expression matches a cflow stack.
     * This assumes the expression is a cflow extracted expression (like "true AND cflow")
     *
     * Note: we need to evaluate each cflow given the stack
     * and not evaluate each stack element separately
     * to support complex cflow composition
     *
     * @param classNameMethodMetaDataTuples the meta-data for the cflow stack
     * @return boolean
     */
    public boolean matchCflow(Set classNameMethodMetaDataTuples) {
        if (! (this instanceof CflowExpression)) {
            throw new RuntimeException("problem in clow extracted expression");
        } else {
            for (Iterator tuples = classNameMethodMetaDataTuples.iterator(); tuples.hasNext();) {
                CflowMetaData tuple = (CflowMetaData)tuples.next();
                if (match(tuple.getClassMetaData(), tuple.getMethodMetaData())) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Tries to finds a match at some superclass in the hierarchy. <p/>Only checks for a class match to allow early
     * filtering. <p/>Recursive.
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
     * Tries to finds a match at some interface in the hierarchy. <p/>Only checks for a class match to allow early
     * filtering. <p/>Recursive.
     *
     * @param interfaces    the interfaces
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
     * Compiles the pattern for the expression.
     */
    protected void compilePattern() {
        PatternTuple tuple = null;
        if (m_type == null) {
            throw new ExpressionException("pointcut type in context can not be null");
        }
        if (m_type.equals(PointcutType.EXECUTION)) {
            if (Pattern.isConstructor(m_expression)) {
                tuple = PatternFactory.createConstructorPatternTuple(m_expression, m_package);
                m_memberPattern = Pattern.compileConstructorPattern(tuple.getMemberPattern());
            }
            else {
                tuple = PatternFactory.createMethodPatternTuple(m_expression, m_package);
                m_memberPattern = Pattern.compileMethodPattern(tuple.getMemberPattern());
            }
            m_isHierarchical = tuple.isHierarchical();
            m_classPattern = Pattern.compileClassPattern(tuple.getCalleeClassPattern());
        }
        else if (m_type.equals(PointcutType.CALL)) {
            if (Pattern.isConstructor(m_expression)) {
                tuple = PatternFactory.createCallPatternTuple(Pattern.CONSTRUCTOR, m_expression, m_package);
                m_memberPattern = Pattern.compileCallerSidePattern(Pattern.CONSTRUCTOR, tuple.getMemberPattern());
            }
            else {
                tuple = PatternFactory.createCallPatternTuple(Pattern.METHOD, m_expression, m_package);
                m_memberPattern = Pattern.compileCallerSidePattern(Pattern.METHOD, tuple.getMemberPattern());
            }
            m_isHierarchical = tuple.isHierarchical();
            m_isHierarchicalCallee = tuple.isHierarchicalCallee();
            m_classPattern = Pattern.compileClassPattern(tuple.getCallerClassPattern());
        }
        else if (m_type.equals(PointcutType.SET) || m_type.equals(PointcutType.GET)) {
            tuple = PatternFactory.createFieldPatternTuple(m_expression, m_package);
            m_memberPattern = Pattern.compileFieldPattern(tuple.getMemberPattern());
            m_isHierarchical = tuple.isHierarchical();
            m_classPattern = Pattern.compileClassPattern(tuple.getCalleeClassPattern());
        }
        else if (m_type.equals(PointcutType.CFLOW)) {
            // cflow compiled as caller side pattern
            if (Pattern.isConstructor(m_expression)) {
                tuple = PatternFactory.createCallPatternTuple(Pattern.CONSTRUCTOR, m_expression, m_package);
                m_memberPattern = Pattern.compileCallerSidePattern(Pattern.CONSTRUCTOR, tuple.getMemberPattern());
            }
            else {
                tuple = PatternFactory.createCallPatternTuple(Pattern.METHOD, m_expression, m_package);
                m_memberPattern = Pattern.compileCallerSidePattern(Pattern.METHOD, tuple.getMemberPattern());
            }
            m_isHierarchical = tuple.isHierarchical();
            m_classPattern = Pattern.compileClassPattern(tuple.getCalleeClassPattern());
        }
        else if (m_type.equals(PointcutType.HANDLER)) {
            tuple = PatternFactory.createClassPatternTuple(m_expression, m_package);
            m_isHierarchical = tuple.isHierarchical();
            m_classPattern = Pattern.compileClassPattern(tuple.getCalleeClassPattern());
        }
        else if (m_type.equals(PointcutType.CLASS)) {
            tuple = PatternFactory.createClassPatternTuple(m_expression, m_package);
            m_isHierarchical = tuple.isHierarchical();
            m_classPattern = Pattern.compileClassPattern(tuple.getCalleeClassPattern());
        }
    }
}
