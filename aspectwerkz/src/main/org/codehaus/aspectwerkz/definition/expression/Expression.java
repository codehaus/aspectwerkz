/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.expression;

import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.List;
import java.util.Iterator;
import java.io.Serializable;

import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;

import org.codehaus.aspectwerkz.util.Strings;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.MemberMetaData;
import org.codehaus.aspectwerkz.metadata.InterfaceMetaData;
import org.codehaus.aspectwerkz.exception.ExpressionException;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.regexp.PatternTuple;
import org.codehaus.aspectwerkz.regexp.Pattern;
import org.codehaus.aspectwerkz.regexp.ClassPattern;
import org.codehaus.aspectwerkz.definition.PatternFactory;

/**
 * Base class for the expression AST.
 * <p/>Evaluates nested pointcut patterns with unlimited depth.
 * <p/>Uses the composite pattern.
 *
 * @TODO: implement readObject() for the subclasses
 * @TODO: add serialVersionUID field to the subclasses
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public abstract class Expression implements Serializable {

    /**
     * Default namespace for the expressions.
     */
    protected static final String DEFAULT_NAMESPACE = "DEFAULT_NAMESPACE";

    /**
     * Map with all the expression templates in the definition.
     * <p/>
     * The map consists of namespace:map pairs in which the maps contains name:expression pairs.
     */
    protected static final Map s_expressionTemplates = new HashMap();

    /**
     * The name of the pointcut for this expression (if the expression is a top
     * level expression, e.g. is bound to a named pointcut).
     */
    protected String m_name;

    /**
     * The namespace for the expression.
     */
    protected String m_namespace;

    /**
     * The string representation of the expression.
     */
    protected String m_expression;

    /**
     * The expression type.
     */
    protected PointcutType m_type;

    /**
     * The package namespace that the expression is living in.
     */
    protected String m_package = "";

    /**
     * The JEXL representation of the expression.
     */
    protected transient org.apache.commons.jexl.Expression m_jexlExpr;

    /**
     * Hierarchical flag.
     */
    protected boolean m_isHierarchical = false;

    /**
     * Map with the references to the pointcuts referenced in the expression.
     */
    protected final Map m_expressionRefs = new HashMap();

    /**
     * Defines if the expression is a leaf in the AST or a regular node,
     * e.g. if the expression is a pattern or an expression.
     */
    protected boolean m_isLeafNode = false;

    /**
     * The compiled class pattern.
     */
    protected ClassPattern m_classPattern;

    /**
     * The compiled member pattern.
     */
    protected Pattern m_memberPattern;

    /**
     * Create new expression based on the type in the context.
     *
     * @param namespace the namespace for the expression
     * @param expression the expression string
     * @param packageNamespace the package namespace that the expression is living in
     * @param name the name of the pointcut
     * @param type the pointcut type
     * @return the expression (needs to be casted)
     */
    public static Expression createExpression(final String namespace,
                                              final String expression,
                                              final String packageNamespace,
                                              final String name,
                                              final PointcutType type) {
        Expression expr = null;
        if (type.equals(PointcutType.EXECUTION)) {
            expr = Expression.createExecutionExpression(
                    namespace, expression, packageNamespace, name
            );
        }
        else if (type.equals(PointcutType.CALL)) {
            expr = Expression.createCallExpression(
                    namespace, expression, packageNamespace, name
            );
        }
        else if (type.equals(PointcutType.SET)) {
            expr = Expression.createSetExpression(
                    namespace, expression, packageNamespace, name
            );
        }
        else if (type.equals(PointcutType.GET)) {
            expr = Expression.createGetExpression(
                    namespace, expression, packageNamespace, name
            );
        }
        else if (type.equals(PointcutType.THROWS)) {
            expr = Expression.createThrowsExpression(
                    namespace, expression, packageNamespace, name
            );
        }
        else if (type.equals(PointcutType.CFLOW)) {
            expr = Expression.createCflowExpression(
                    namespace, expression, packageNamespace, name
            );
        }
        else if (type.equals(PointcutType.CLASS)) {
            expr = Expression.createClassExpression(
                    namespace, expression, packageNamespace, name
            );
        }
        else {
            throw new ExpressionException("poincut type is not supported: " + type);
        }
        return expr;
    }

    /**
     * Creates a new expression template.
     *
     * @param namespace the namespace for the expression
     * @param expression the expression string
     * @param packageNamespace the package namespace
     * @param name the name of the expression
     * @param type the type of the expression
     * @return the expression template
     */
    public static ExpressionTemplate createExpressionTemplate(final String namespace,
                                                              final String expression,
                                                              final String packageNamespace,
                                                              final String name,
                                                              final PointcutType type) {
        return new ExpressionTemplate(namespace, expression, packageNamespace, name, type);
    }

    /**
     * Create new root expression.
     *
     * @param expression the expression string
     * @return the expression
     */
    public static RootExpression createRootExpression(final String namespace,
                                                      final String expression) {
        return new RootExpression(namespace, expression);
    }

    /**
     * Create new root expression.
     *
     * @param namespace the namespace for the expression
     * @param expression the expression string
     * @param type the pointcut type
     * @return the expression
     */
    public static RootExpression createRootExpression(final String namespace,
                                                      final String expression,
                                                      final PointcutType type) {
        return new RootExpression(namespace, expression, type);
    }

    /**
     * Create new execution expression.
     *
     * @param namespace the namespace for the expression
     * @param expression the expression string
     * @param packageNamespace the package namespace that the expression is living in
     * @param name the name of the pointcut
     * @return the expression
     */
    public static ExecutionExpression createExecutionExpression(final String namespace,
                                                                final String expression,
                                                                final String packageNamespace,
                                                                final String name) {
        return new ExecutionExpression(namespace, expression, packageNamespace, name);
    }

    /**
     * Create new call expression.
     *
     * @param namespace the namespace for the expression
     * @param expression the expression string
     * @param packageNamespace the package namespace that the expression is living in
     * @param name the name of the pointcut
     * @return the expression
     */
    public static CallExpression createCallExpression(final String namespace,
                                                      final String expression,
                                                      final String packageNamespace,
                                                      final String name) {
        return new CallExpression(namespace, expression, packageNamespace, name);
    }

    /**
     * Create new set expression.
     *
     * @param namespace the namespace for the expression
     * @param expression the expression string
     * @param packageNamespace the package namespace that the expression is living in
     * @param name the name of the pointcut
     * @return the expression
     */
    public static SetExpression createSetExpression(final String namespace,
                                                    final String expression,
                                                    final String packageNamespace,
                                                    final String name) {
        return new SetExpression(namespace, expression, packageNamespace, name);
    }

    /**
     * Create new get expression.
     *
     * @param namespace the namespace for the expression
     * @param expression the expression string
     * @param packageNamespace the package namespace that the expression is living in
     * @param name the name of the pointcut
     * @return the expression
     */
    public static GetExpression createGetExpression(final String namespace,
                                                    final String expression,
                                                    final String packageNamespace,
                                                    final String name) {
        return new GetExpression(namespace, expression, packageNamespace, name);
    }

    /**
     * Create new throws expression.
     *
     * @param namespace the namespace for the expression
     * @param expression the expression string
     * @param packageNamespace the package namespace that the expression is living in
     * @param name the name of the pointcut
     * @return the expression
     */
    public static ThrowsExpression createThrowsExpression(final String namespace,
                                                          final String expression,
                                                          final String packageNamespace,
                                                          final String name) {
        return new ThrowsExpression(namespace, expression, packageNamespace, name);
    }

    /**
     * Create new cflow expression.
     *
     * @param namespace the namespace for the expression
     * @param expression the expression string
     * @param packageNamespace the package namespace that the expression is living in
     * @param name the name of the pointcut
     * @return the expression
     */
    public static CflowExpression createCflowExpression(final String namespace,
                                                        final String expression,
                                                        final String packageNamespace,
                                                        final String name) {
        return new CflowExpression(namespace, expression, packageNamespace, name);
    }

    /**
     * Create new class expression.
     *
     * @param namespace the namespace for the expression
     * @param expression the expression string
     * @param packageNamespace the package namespace that the expression is living in
     * @param name the name of the pointcut
     * @return the expression
     */
    public static ClassExpression createClassExpression(final String namespace,
                                                        final String expression,
                                                        final String packageNamespace,
                                                        final String name) {
        return new ClassExpression(namespace, expression, packageNamespace, name);
    }

    /**
     * Registers an expression template.
     *
     * @param expression the expression to add
     */
    public static void registerExpressionTemplate(final ExpressionTemplate expression) {
        String namespace = expression.getNamespace();
        if (namespace == null || namespace.equals("")) {
            namespace = DEFAULT_NAMESPACE;
        }
        synchronized (s_expressionTemplates) {
            Map expressions;
            if (s_expressionTemplates.get(namespace) == null) {
                expressions = new HashMap();
                s_expressionTemplates.put(namespace, expressions);
            }
            else {
                expressions = (Map)s_expressionTemplates.get(namespace);
            }
            expressions.put(expression.getName(), expression);
        }
    }

    /**
     * Finds and returns an expression template by its name.
     *
     * @param namespace the namespace for the expression
     * @param expressionName the name of the expression
     */
    public static ExpressionTemplate getExpressionTemplate(String namespace, final String expressionName) {
        if (namespace == null || namespace.equals("")) {
            namespace = DEFAULT_NAMESPACE;
        }
        Map expressions = (Map)s_expressionTemplates.get(namespace);
        if (expressions != null) {
            return (ExpressionTemplate)expressions.get(expressionName);
        }
        else {
            return null;
        }
    }

    /**
     * Creates a new expression.
     *
     * @param namespace the namespace for the expression
     * @param expression the expression as a string
     * @param name the name of the expression
     * @param type the expression type
     */
    Expression(final String namespace,
               final String expression,
               final String name,
               final PointcutType type) {
        this(namespace, expression, "", name, type);
    }

    /**
     * Creates a new expression.
     *
     * @param namespace the namespace for the expression
     * @param expression the expression as a string
     * @param packageNamespace the package namespace that the expression is living in
     * @param name the name of the expression
     * @param type the expression type
     */
    Expression(final String namespace,
               final String expression,
               final String packageNamespace,
               final String name,
               final PointcutType type) {
        if (namespace == null) throw new IllegalArgumentException("namespace can not be null");
        if (expression == null) throw new IllegalArgumentException("expression can not be null");

        m_namespace = namespace;
        m_expression = deEscapeExpression(expression);
        m_name = name;
        m_type = type;
        if (packageNamespace == null) {
            m_package = packageNamespace;
        }
        else {
            m_package = "";
        }

        checkIfLeafNode();

        if (isLeafNode()) {
            compilePattern();
        }
        else {
            validateAST();
            buildAST();
            createJexlExpression();
        }
    }

    /**
     * Checks if the expression pattern matches a certain join point.
     *
     * @param classMetaData the class meta-data
     * @param memberMetaData the meta-data for the member
     * @return boolean
     */
    protected abstract boolean matchPattern(final ClassMetaData classMetaData, final MemberMetaData memberMetaData);

    /**
     * Returns the name for the expression (if available, else null).
     *
     * @return the name
     */
    public String getName() {
        return m_name;
    }

    /**
     * Returns the expression pattern as a string.
     *
     * @return the expression pattern as a string
     */
    public String getExpression() {
        return m_expression;
    }

    /**
     * Returns the namespace for the expression.
     *
     * @return the namespace for the expression
     */
    public String getNamespace() {
        return m_namespace;
    }

    /**
     * Returns the expression type.
     *
     * @return the expression type
     */
    public PointcutType getType() {
        return m_type;
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
     * Checks if the expression is a leaf in the AST or a regular node.
     * E.g. if the expression is a pattern or an expression.
     *
     * @return boolean
     */
    public boolean isLeafNode() {
        return m_isLeafNode;
    }

    /**
     * Checks if the expression matches a certain join point.
     * <p/>Only checks for a class match to allow early filtering.
     * <p/>Only does a qualified guess, does not evaluate the whole expression since doing it only on class
     * level would give the false results.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public boolean match(final ClassMetaData classMetaData) {
        if (isLeafNode()) {
            return matchPattern(classMetaData);
        }
        else {
            try {
                for (Iterator it = m_expressionRefs.values().iterator(); it.hasNext();) {
                    Expression expression = (Expression)it.next();
                    if (expression.match(classMetaData)) {
                        return true;
                    }
                }
                return false;
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        }
    }

    /**
     * Checks if the expression matches a certain join point.
     *
     * @param classMetaData the class meta-data
     * @param memberMetaData the meta-data for the member
     * @return boolean
     */
    public boolean match(final ClassMetaData classMetaData, final MemberMetaData memberMetaData) {
        if (isLeafNode()) {
            return matchPattern(classMetaData, memberMetaData);
        }
        else {
            try {
                JexlContext jexlContext = JexlHelper.createContext();

                for (Iterator it = m_expressionRefs.entrySet().iterator(); it.hasNext();) {
                    Map.Entry entry = (Map.Entry)it.next();

                    String name = (String)entry.getKey();
                    Expression expression = (Expression)entry.getValue();

                    if (expression instanceof CflowExpression) {
                        jexlContext.getVars().put(name, Boolean.TRUE);
                        continue;
                    }

                    // try to find a match somewhere in the class hierarchy (interface or super class)
                    if (expression.match(classMetaData, memberMetaData)) {
                        jexlContext.getVars().put(name, Boolean.TRUE);
                    }
                    else {
                        jexlContext.getVars().put(name, Boolean.FALSE);
                    }
                }
                return evaluateExpression(jexlContext);
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        }
    }

    /**
     * Checks if the expression matches a certain join point.
     * <p/>Special case in the API which tries to match exception types as well.
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
        if (exceptionType == null) {
            match(classMetaData, memberMetaData);
        }
        if (isLeafNode() && this instanceof ThrowsExpression) {
            return ((ThrowsExpression)this).matchPattern(classMetaData, memberMetaData, exceptionType);
        }
        else {
            try {
                JexlContext jexlContext = JexlHelper.createContext();

                for (Iterator it = m_expressionRefs.entrySet().iterator(); it.hasNext();) {
                    Map.Entry entry = (Map.Entry)it.next();

                    String name = (String)entry.getKey();
                    Expression expression = (Expression)entry.getValue();

                    if (expression instanceof CflowExpression) {
                        jexlContext.getVars().put(name, Boolean.TRUE);
                        continue;
                    }

                    // try to find a match somewhere in the class hierarchy (interface or super class)
                    if (expression.match(classMetaData, memberMetaData, exceptionType)) {
                        jexlContext.getVars().put(name, Boolean.TRUE);
                    }
                    else {
                        jexlContext.getVars().put(name, Boolean.FALSE);
                    }
                }
                return evaluateExpression(jexlContext);
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        }
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
     * Match leaf node pattern.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    protected boolean matchPattern(final ClassMetaData classMetaData) {
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
     * Evaluates the expression.
     *
     * @param jexlContext the JEXL context
     * @return boolean
     * @throws java.lang.Exception upon failure in expression
     */
    protected boolean evaluateExpression(JexlContext jexlContext) throws Exception {
        Boolean result = (Boolean)m_jexlExpr.evaluate(jexlContext);
        if (result == null || !result.booleanValue()) {
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * Checks if the expression is a leaf node in the AST.
     */
    protected void checkIfLeafNode() {
        if (m_expression.indexOf('.') != -1 || m_expression.indexOf('*') != -1) {
            m_isLeafNode = true;
        }
        else {
            m_isLeafNode = false;
        }
    }

    /**
     * Compiles the pattern for the expression.
     *
     * @TODO: impl. CFLOW
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
     * Creates a JEXL representation of the expression.
     */
    protected void createJexlExpression() {
        try {
            m_jexlExpr = ExpressionFactory.createExpression(m_expression);
        }
        catch (Exception e) {
            throw new ExpressionException("could not create jexl expression from: " + m_expression);
        }
    }

    /**
     * Validates the AST.
     */
    protected void validateAST() {
        PointcutType pointcutType = null;
        PointcutType previousType = null;
        PointcutType currentType = null;

        StringTokenizer tokenizer = getPointcutRefTokenizer();
        while (tokenizer.hasMoreTokens()) {

            String pointcutRef = tokenizer.nextToken();
            ExpressionTemplate template = getExpressionTemplate(m_namespace, pointcutRef);

            if (template == null) {
                throw new ExpressionException("referenced pointcut [" + pointcutRef + "] does not exist");
            }
            currentType = template.getType();

            if (hasTypeMisMatch(previousType, currentType)) {
                StringBuffer msg = new StringBuffer();
                msg.append("nested expressions needs to be of the same type: [");
                msg.append(template.getExpression());
                msg.append("] : [");
                msg.append(m_expression);
                msg.append(']');
                throw new ExpressionException(msg.toString());
            }

            if (pointcutType == null && !currentType.equals(PointcutType.CFLOW)) {
                pointcutType = currentType;
            }
            previousType = currentType;
        }
        if (m_type == null) {
            m_type = pointcutType;
        }
    }

    /**
     * Builds up an abstract syntax tree (AST) of the expression.
     */
    protected void buildAST() {
        StringTokenizer tokenizer = getPointcutRefTokenizer();
        while (tokenizer.hasMoreTokens()) {
            String pointcutRef = tokenizer.nextToken();

            ExpressionTemplate template = getExpressionTemplate(m_namespace, pointcutRef);
            PointcutType type = template.getType();

            final Expression expression;
            if (type.equals(PointcutType.EXECUTION)) {
                expression = Expression.createExecutionExpression(
                        template.getNamespace(),
                        template.getExpression(),
                        template.getPackage(),
                        pointcutRef
                );
            }
            else if (type.equals(PointcutType.CALL)) {
                expression = Expression.createCallExpression(
                        template.getNamespace(),
                        template.getExpression(),
                        template.getPackage(),
                        pointcutRef
                );
            }
            else if (type.equals(PointcutType.GET)) {
                expression = Expression.createGetExpression(
                        template.getNamespace(),
                        template.getExpression(),
                        template.getPackage(),
                        pointcutRef
                );
            }
            else if (type.equals(PointcutType.SET)) {
                expression = Expression.createSetExpression(
                        template.getNamespace(),
                        template.getExpression(),
                        template.getPackage(),
                        pointcutRef
                );
            }
            else if (type.equals(PointcutType.CFLOW)) {
                expression = Expression.createCflowExpression(
                        template.getNamespace(),
                        template.getExpression(),
                        template.getPackage(),
                        pointcutRef
                );
            }
            else if (type.equals(PointcutType.THROWS)) {
                expression = Expression.createThrowsExpression(
                        template.getNamespace(),
                        template.getExpression(),
                        template.getPackage(),
                        pointcutRef
                );
            }
            else if (type.equals(PointcutType.CLASS)) {
                expression = Expression.createClassExpression(
                        template.getNamespace(),
                        template.getExpression(),
                        template.getPackage(),
                        pointcutRef
                );
            }
            else {
                throw new ExpressionException("pointcut type not supported: " + type);
            }
            m_expressionRefs.put(pointcutRef, expression);
        }
    }

    /**
     * Checks that the pointcut types matches.
     *
     * @TODO: does now only allow expressions to be build up of pointcuts of the SAME type, else throws an exception, this need to be fixed
     *
     * @param previousType
     * @param currentType
     * @return true if we have a mismatch
     */
    private boolean hasTypeMisMatch(final PointcutType previousType, final PointcutType currentType) {
        if (previousType == null) return false;
        if (previousType.equals(currentType)) return false;
        if (previousType.equals(PointcutType.CFLOW)) return false;
        if (currentType.equals(PointcutType.CFLOW)) return false;
        return true;
    }

    /**
     * Returns a StringTokenizer with the pointcut references.
     *
     * @return the pointcut references in a tokenizer
     */
    protected StringTokenizer getPointcutRefTokenizer() {
        String exprRefList = Strings.replaceSubString(m_expression, "&&", " ");
        exprRefList = Strings.replaceSubString(exprRefList, "||", " ");
        exprRefList = Strings.replaceSubString(exprRefList, "!", " ");
        exprRefList = Strings.replaceSubString(exprRefList, "(", " ");
        exprRefList = Strings.replaceSubString(exprRefList, ")", " ");
        StringTokenizer tokenizer = new StringTokenizer(exprRefList, " ");
        return tokenizer;
    }

    /**
     * Deescape the expression. Substitutes 'AND' with '&&' and 'OR' with '||'.
     *
     * @param expression the expression
     * @return the deescaped exprsesion
     */
    protected String deEscapeExpression(String expression) {
        String tmp = Strings.replaceSubString(expression, " AND ", " && ");
        tmp = Strings.replaceSubString(tmp, " and ", " && ");
        tmp = Strings.replaceSubString(tmp, " OR ", " || ");
        tmp = Strings.replaceSubString(tmp, " or ", " || ");
        return tmp;
    }

    /**
     * Overridden toString.
     *
     * @return the string representation of the Expression instance
     */
    public String toString() {
        return "[" + super.toString() + ": " +
                m_name + "," +
                m_namespace + "," +
                m_package + "," +
                m_expression + "]";
    }
}
