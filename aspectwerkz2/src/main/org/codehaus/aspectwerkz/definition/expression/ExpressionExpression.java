/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.expression;

import org.codehaus.aspectwerkz.definition.expression.ast.ExpressionParser;
import org.codehaus.aspectwerkz.definition.expression.ast.ExpressionParserVisitor;
import org.codehaus.aspectwerkz.definition.expression.ast.SimpleNode;
import org.codehaus.aspectwerkz.definition.expression.visitor.AnonymousInflateVisitor;
import org.codehaus.aspectwerkz.definition.expression.visitor.CflowEvaluateVisitor;
import org.codehaus.aspectwerkz.definition.expression.visitor.CflowExpressionContext;
import org.codehaus.aspectwerkz.definition.expression.visitor.CflowExtractVisitor;
import org.codehaus.aspectwerkz.definition.expression.visitor.CflowIdentifierLookupVisitor;
import org.codehaus.aspectwerkz.definition.expression.visitor.CflowIdentifierLookupVisitorContext;
import org.codehaus.aspectwerkz.definition.expression.visitor.EarlyEvaluateVisitor;
import org.codehaus.aspectwerkz.definition.expression.visitor.EvaluateVisitor;
import org.codehaus.aspectwerkz.definition.expression.visitor.IdentifierLookupVisitor;
import org.codehaus.aspectwerkz.definition.expression.visitor.TypeVisitor;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.MemberMetaData;

import java.io.StringReader;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Class for sub-expression
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class ExpressionExpression extends Expression
{
    /**
     * Type guessing visitor
     */
    private static ExpressionParserVisitor TYPE_VISITOR = new TypeVisitor();

    /**
     * Identifier lookup visitor
     */
    private static ExpressionParserVisitor IDENTIFIER_VISITOR = new IdentifierLookupVisitor();

    /**
     * Identifier involved in IN and NOT IN lookup visitor
     */
    private static ExpressionParserVisitor CFLOWIDENTIFIER_VISITOR = new CflowIdentifierLookupVisitor();

    /**
     * Expression evaluation, ignores Cflow
     */
    private static ExpressionParserVisitor EVALUATE_VISITOR = new EvaluateVisitor();

    /**
     * Expression early evaluation, ignores Cflow
     */
    private static ExpressionParserVisitor EARLYEVALUATE_VISITOR = new EarlyEvaluateVisitor();
    private static ExpressionParserVisitor CFLOWEXTRACT_VISITOR = new CflowExtractVisitor();
    private static ExpressionParserVisitor CFLOWEVALUATE_VISITOR = new CflowEvaluateVisitor();
    private static ExpressionParserVisitor ANONYMOUSINFLATE_VISITOR = new AnonymousInflateVisitor();

    /**
     * Map with the references to the pointcuts referenced in the IN or NOT IN parts of expression
     */
    protected final Map m_cflowExpressionRefs = new HashMap();

    /**
     * AST root
     */
    private SimpleNode root;

    /**
     * Create an anonymous expression
     *
     * @param namespace
     * @param expression
     */
    public ExpressionExpression(ExpressionNamespace namespace, String expression)
    {
        this(namespace, expression, "");
    }

    /**
     * Create a named expression (for expression nesting)
     *
     * @param namespace
     * @param expression
     * @param name
     */
    public ExpressionExpression(ExpressionNamespace namespace,
        String expression, String name)
    {
        super(namespace, expression, "", name, null);

        try
        {
            ExpressionParser parser = new ExpressionParser(new StringReader(
                        expression));

            root = parser.ExpressionScript();

            // inflate anonymous expressions and register anonymous leaf (3x faster)
            StringBuffer inflated = (StringBuffer) root.jjtAccept(ANONYMOUSINFLATE_VISITOR,
                    m_namespace);
            ExpressionParser parserInflate = new ExpressionParser(new StringReader(
                        inflated.toString()));
            SimpleNode newRoot = parserInflate.ExpressionScript();

            // swap
            root = newRoot;
        }
        catch (Throwable t)
        {
            throw new RuntimeException("unparsable[" + expression + "]/" + name
                + ": " + t.getMessage());
        }

        // support for polymorphic expression
        Set types = determineTypeFromAST();

        if (types.isEmpty())
        {
            m_types.add(PointcutType.ANY);

            //AVCF throw new RuntimeException("unable to determine type from " + expression);
        }

        m_types.addAll(types);

        initializeCflowExpressionMapFromAST();
    }

    /**
     * Type determination of AST
     *
     * @return Set of PointcutType of expression
     */
    private Set determineTypeFromAST()
    {
        return (Set) root.jjtAccept(TYPE_VISITOR, m_namespace);
    }

    /**
     * Build a new expression with only cflow to be evaluated. All other elements are evaluated
     * TODO: do we need to support cflow(a within b)
     *
     * @param classMetaData
     * @param memberMetaData
     * @param assumedType
     * @return simplified expression
     */
    public Expression extractCflowExpression(ClassMetaData classMetaData,
        MemberMetaData memberMetaData, PointcutType assumedType)
    {
        ExpressionContext ctx = new ExpressionContext(assumedType, m_namespace,
                classMetaData, memberMetaData, null);

        StringBuffer extracted = (StringBuffer) root.jjtAccept(CFLOWEXTRACT_VISITOR,
                ctx);
        Expression expression = m_namespace.createExpression(extracted.toString());

        return expression;
    }

    /**
     * Cflow identifier lookup
     */
    private void initializeCflowExpressionMapFromAST()
    {
        CflowIdentifierLookupVisitorContext context = new CflowIdentifierLookupVisitorContext(m_namespace);

        root.jjtAccept(CFLOWIDENTIFIER_VISITOR, context);

        String cflowName = null;

        for (Iterator i = context.getNames().iterator(); i.hasNext();)
        {
            cflowName = (String) i.next();
            m_cflowExpressionRefs.put(cflowName,
                m_namespace.getExpression(cflowName));
        }

        for (Iterator i = context.getAnonymous().iterator(); i.hasNext();)
        {
            CflowExpression anonymousCflow = (CflowExpression) i.next();

            m_cflowExpressionRefs.put(anonymousCflow.getName(), anonymousCflow);

            // referenced it for further inflated referencing
            m_namespace.registerExpression(anonymousCflow);

            // TODO name is used in StartupManager, see getCflowExpressions
            // and will not accept anonymous expr or autonamed ones
            //throw new RuntimeException("anonymous cflow cannot be registered");
            //m_cflowExpressionRefs.put("", (CflowExpression)i.next());
        }

        //        if (m_cflowExpressionRefs.size() > 1) {
        //            throw new RuntimeException("complex cflow expression not supported yet");
        //        }
    }

    /**
     * Early partial match of classMetaData
     *
     * @param classMetaData
     * @param assumedType
     * @return
     */
    public boolean match(final ClassMetaData classMetaData,
        PointcutType assumedType)
    {
        ExpressionContext ctx = new ExpressionContext(assumedType, m_namespace,
                classMetaData, null, null);

        return ((Boolean) root.jjtAccept(EARLYEVALUATE_VISITOR, ctx))
        .booleanValue();
    }

    /**
     * Early partial match of classMetaData
     *
     * @param classMetaData
     * @return
     */
    public boolean match(final ClassMetaData classMetaData)
    {
        for (Iterator types = m_types.iterator(); types.hasNext();)
        {
            if (match(classMetaData, (PointcutType) types.next()))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the expression matches a certain join point as regards the IN and NOT IN parts if any. Each IN / NOT IN
     * part is evaluated independantly from the boolean algebra (TF time)
     * <p/>
     * <p/>Only checks for a class match to allow early filtering. <p/>Only does a qualified guess, does not evaluate
     * the whole expression since doing it only on class level would give the false results.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public boolean matchInOrNotIn(final ClassMetaData classMetaData)
    {
        for (Iterator it = m_cflowExpressionRefs.values().iterator();
            it.hasNext();)
        {
            Expression expression = (Expression) it.next();

            if (expression.match(classMetaData))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the expression matches a certain join point as regards IN / NOT IN parts Each IN / NOT IN part is
     * evaluated independantly from the boolean algebra (TF time)
     *
     * @param classMetaData  the class meta-data
     * @param memberMetaData the meta-data for the member
     * @return boolean
     */
    public boolean matchInOrNotIn(final ClassMetaData classMetaData,
        final MemberMetaData memberMetaData)
    {
        // never match NullMetaData
        if (isNullMetaData(memberMetaData))
        {
            return false;
        }

        for (Iterator it = m_cflowExpressionRefs.values().iterator();
            it.hasNext();)
        {
            Expression expression = (Expression) it.next();

            if (expression.match(classMetaData, memberMetaData))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the expression matches a certain join point. <p/>Special case in the API which tries to match exception
     * types as well.
     *
     * @param classMetaData  the class meta-data
     * @param memberMetaData the meta-data for the member
     * @param exceptionType  the exception type (null => match all)
     * @param assumedType
     * @return boolean
     */
    public boolean match(final ClassMetaData classMetaData,
        final MemberMetaData memberMetaData, final String exceptionType,
        final PointcutType assumedType)
    {
        //        if (exceptionType != null) {
        //            throw new RuntimeException("cannot evaluate exception");//AVO??? of type [" + m_type.toString() + ']');
        //        }
        // never match NullMetaData
        if (isNullMetaData(memberMetaData))
        {
            return false;
        }

        //TODO AVO ???
        ExpressionContext ctx = new ExpressionContext(assumedType, m_namespace,
                classMetaData, memberMetaData, exceptionType);

        //System.out.println("matching " + getExpression() + "  for " + assumedType.toString());
        return ((Boolean) root.jjtAccept(EVALUATE_VISITOR, ctx)).booleanValue();
    }

    /**
     * Checks if the expression matches a certain join point. <p/>Special case in the API which tries to match exception
     * types as well.
     *
     * @param classMetaData
     * @param memberMetaData
     * @param exceptionType
     * @return
     */
    public boolean match(final ClassMetaData classMetaData,
        final MemberMetaData memberMetaData, final String exceptionType)
    {
        // never match NullMetaData
        if (isNullMetaData(memberMetaData))
        {
            return false;
        }

        if (m_types.size() > 1)
        {
            return false;

            //throw new RuntimeException("Composed expression must be matched with an assumed type");//AVO
        }
        else
        {
            return match(classMetaData, memberMetaData, exceptionType,
                (PointcutType) m_types.toArray()[0]);
        }
    }

    /**
     * Checks if the expression matches a certain join point.
     *
     * @param classMetaData  the class meta-data
     * @param memberMetaData the meta-data for the member
     * @param assumedType
     * @return boolean
     */
    public boolean match(final ClassMetaData classMetaData,
        final MemberMetaData memberMetaData, PointcutType assumedType)
    {
        // never match NullMetaData
        if (isNullMetaData(memberMetaData))
        {
            return false;
        }

        return match(classMetaData, memberMetaData, null, assumedType);
    }

    /**
     * Checks if the expression matches a certain join point.
     *
     * @param classMetaData  the class meta-data
     * @param memberMetaData the meta-data for the member
     * @return boolean
     */
    public boolean match(final ClassMetaData classMetaData,
        final MemberMetaData memberMetaData)
    {
        // never match NullMetaData
        if (isNullMetaData(memberMetaData))
        {
            return false;
        }

        if (m_types.size() > 1)
        {
            if ((m_types.size() == 2) && m_types.contains(PointcutType.CFLOW))
            {
                PointcutType type = null;

                for (Iterator types = m_types.iterator(); types.hasNext();)
                {
                    type = (PointcutType) types.next();

                    if (!type.equals(PointcutType.CFLOW))
                    {
                        break;
                    }
                }

                return match(classMetaData, memberMetaData, type);
            }
            else
            {
                return false; // composite type
            }

            //throw new RuntimeException("Composed expression must be matched with an assumed type");//AVO
        }
        else
        {
            return match(classMetaData, memberMetaData,
                (PointcutType) m_types.toArray()[0]);
        }
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
    public boolean matchCflow(Set classNameMethodMetaDataTuples)
    {
        CflowExpressionContext ctx = new CflowExpressionContext(m_namespace,
                classNameMethodMetaDataTuples);

        return ((Boolean) root.jjtAccept(CFLOWEVALUATE_VISITOR, ctx))
        .booleanValue();
    }

    /**
     * Return a Map(name->Expression) of expression involved in the IN and NOT IN sub-expression of this Expression (can
     * be empty)
     *
     * @return Map(name->Expression)
     */
    public Map getCflowExpressions()
    {
        return m_cflowExpressionRefs;
    }

    public SimpleNode getRoot()
    {
        return root;
    }
}
