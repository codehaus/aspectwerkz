/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.expression;

import org.codehaus.aspectwerkz.expression.ast.ASTRoot;
import org.codehaus.aspectwerkz.expression.ast.ASTPointcutReference;
import org.codehaus.aspectwerkz.expression.ast.ASTArgParameter;
import org.codehaus.aspectwerkz.expression.ast.ASTArgs;
import org.codehaus.aspectwerkz.expression.ast.ASTThis;
import org.codehaus.aspectwerkz.expression.ast.ASTTarget;
import org.codehaus.aspectwerkz.util.Strings;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ClassInfoHelper;
import org.codehaus.aspectwerkz.reflect.impl.asm.AsmClassInfo;
import org.codehaus.aspectwerkz.ContextClassLoader;

import java.util.Iterator;

import gnu.trove.TIntIntHashMap;

/**
 * A visitor to compute the args index of the target (matching) method/constructor which match the advice args. Note:
 * extends the ExpressionVisitor. We should allow for optimization (all=TRUE) by assuming that args(..) does not depends
 * of the matching context. The "(String a, String b):methodX && args(a,b) -OR- methodY && args(b,a)" expression should
 * not be allowed then. TODO check support for anonymous pc
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class ArgsIndexVisitor extends ExpressionVisitor {

    /**
     * Classloader used to perform type checks (for target / this bindings)
     * A strong reference is enough since this visitor is not be referenced.
     */
    private ClassLoader m_classLoader;

    /**
     * Update the given context with its runtime information (this, target, args).
     * It should be called for each advice.
     *
     * @param expressionInfo
     * @param context
     */
    public static void updateContextForRuntimeInformation(final ExpressionInfo expressionInfo,
                                                          final ExpressionContext context,
                                                          final ClassLoader loader) {
        ArgsIndexVisitor visitor = new ArgsIndexVisitor(expressionInfo, expressionInfo.toString(),
                                                        expressionInfo.getNamespace(),
                                                        expressionInfo.getExpression().getASTRoot());
        visitor.m_classLoader = loader;
        visitor.match(context);
    }

    private ArgsIndexVisitor(final ExpressionInfo expressionInfo,
                            final String expression,
                            final String namespace,
                            final ASTRoot root) {
        super(expressionInfo, expression, namespace, root);
    }

    //-- overrided methods to compute the args index mapping --//

    public Object visit(ASTPointcutReference node, Object data) {
        // do the sub expression visit
        ExpressionContext context = (ExpressionContext) data;
        ExpressionNamespace namespace = ExpressionNamespace.getNamespace(m_namespace);
        ExpressionInfo expressionInfo = namespace.getExpressionInfo(node.getName());

        ArgsIndexVisitor referenced = new ArgsIndexVisitor(expressionInfo, expressionInfo.toString(),
                                                           expressionInfo.getNamespace(),
                                                           expressionInfo.getExpression().getASTRoot());
        context.resetRuntimeState();
        Boolean match = referenced.matchUndeterministic(context);

        // update the this and target bounded name from this last visit
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            String referenceCallArg = ((ASTArgParameter) node.jjtGetChild(i)).getTypePattern().getPattern();
            String referentArg = expressionInfo.getArgumentNameAtIndex(i);
            if (referentArg.equals(context.m_targetBoundedName)) {
                context.m_targetBoundedName = referenceCallArg;
                assertIsInstanceOf(expressionInfo.getArgumentType(referentArg),
                                   m_expressionInfo.getArgumentType(referenceCallArg));
            } else if (referentArg.equals(context.m_thisBoundedName)) {
                context.m_thisBoundedName = referenceCallArg;
                assertIsInstanceOf(expressionInfo.getArgumentType(referentArg),
                                   m_expressionInfo.getArgumentType(referenceCallArg));
            }
        }

        // update the context mapping from this last visit
        // did we visit some args(<name>) nodes ?
        if (!context.m_exprIndexToTargetIndex.isEmpty()) {
            TIntIntHashMap sourceToTargetArgIndexes = new TIntIntHashMap();
            int index = 0;
            for (Iterator it = m_expressionInfo.getArgumentNames().iterator(); it.hasNext(); index++) {
                String adviceParamName = (String) it.next();
                //look for adviceParamName in the expression name and get its index
                int exprArgIndex = ArgsIndexVisitor.getExprArgIndex(m_expression, adviceParamName);
                if (exprArgIndex < 0) {
                    //param of advice not found in pc signature - f.e. "joinPoint"
                    continue;
                }
                int adviceArgIndex = m_expressionInfo.getArgumentIndex(adviceParamName);
                int targetArgIndex = context.m_exprIndexToTargetIndex.get(exprArgIndex);
                sourceToTargetArgIndexes.put(adviceArgIndex, targetArgIndex);
            }
            context.m_exprIndexToTargetIndex = sourceToTargetArgIndexes;

            // debug:
            //            if (m_expressionInfo.m_isAdviceBindingWithArgs) {
            //                System.out.println("XXXARGS transitive map for an advice is @ " +
            //                        m_expression + " for " + context.getReflectionInfo().getName());
            //                for (int i = 0; i < sourceToTargetArgIndexes.keys().length; i++) {
            //                    int adviceArgIndex = sourceToTargetArgIndexes.keys()[i];
            //                    int targetMethodIndex = sourceToTargetArgIndexes.get(adviceArgIndex);
            //                    System.out.println(" " + adviceArgIndex + " - " + targetMethodIndex);
            //                }
            //            }
        }
        return match;
    }

    public Object visit(ASTArgs node, Object data) {
        return super.visit(node, data);
    }

    public Object visit(ASTArgParameter node, Object data) {
        // do the visit
        Boolean match = (Boolean) super.visit(node, data);

        // get the pointcut signature arg index of the arg we are visiting
        int pointcutArgIndex = -1;
        if (node.getTypePattern().getPattern().indexOf(".") < 0) {
            pointcutArgIndex = m_expressionInfo.getArgumentIndex(node.getTypePattern().getPattern());
        }

        // if match and we are visiting a parameter binding (not a type matching)
        if (pointcutArgIndex >= 0 && Boolean.TRUE.equals(match)) {
            ExpressionContext ctx = (ExpressionContext) data;
            ctx.m_exprIndexToTargetIndex.put(pointcutArgIndex, ctx.getCurrentTargetArgsIndex());
        }
        return match;
    }

    public Object visit(ASTThis node, Object data) {
        // if the this(..) node identifier appears in the pointcut signature, we have a bounded type
        if (m_expressionInfo.getArgumentType(node.getIdentifier()) != null) {
            ExpressionContext ctx = (ExpressionContext) data;
            if (ctx.m_thisBoundedName == null) {
                ctx.m_thisBoundedName = node.getIdentifier();
            } else if (ctx.m_thisBoundedName != node.getIdentifier()) {
                throw new DefinitionException("this(..) seems to be bounded to different bounded entities in \""
                        + m_expressionInfo.toString() + "\" in " + m_expressionInfo.getNamespace()
                        + " : found " + ctx.m_targetBoundedName + " and " + node.getIdentifier());
            }
        }
        return super.visit(node, data);
    }

    public Object visit(ASTTarget node, Object data) {
        // if the target(..) node identifier appears in the pointcut signature, we have a bounded type
        if (m_expressionInfo.getArgumentType(node.getIdentifier()) != null) {
            ExpressionContext ctx = (ExpressionContext) data;
            if (ctx.m_targetBoundedName == null) {
                ctx.m_targetBoundedName = node.getIdentifier();
            } else if (ctx.m_targetBoundedName != node.getIdentifier()) {
                throw new DefinitionException("target(..) seems to be bounded to different bounded entities in \""
                        + m_expressionInfo.toString() + "\" in " + m_expressionInfo.getNamespace()
                        + " : found " + ctx.m_targetBoundedName + " and " + node.getIdentifier());
            }
        }
        // keep track if the result was undetermined: we will need a runtime check
        Object match = super.visit(node, data);
        if (match == null) {
            ((ExpressionContext)data).m_targetWithRuntimeCheck = true;
        }
        return match;
    }

    /**
     * Get the parameter index from a "call side" like signature like pc(a, b) => index(a) = 0, or -1 if not found
     *
     * @param expression
     * @param adviceParamName
     * @return
     */
    private static int getExprArgIndex(String expression, String adviceParamName) {
        //TODO - support for anonymous pointcut with args
        int paren = expression.indexOf('(');
        if (paren > 0) {
            String params = expression.substring(paren + 1, expression.lastIndexOf(')')).trim();
            String[] parameters = Strings.splitString(params, ",");
            int paramIndex = 0;
            for (int i = 0; i < parameters.length; i++) {
                String parameter = parameters[i].trim();
                if (parameter.length() > 0) {
                    if (adviceParamName.equals(parameter)) {
                        return paramIndex;
                    } else {
                        paramIndex++;
                    }
                }
            }
        }
        return -1;
    }

    private void assertIsInstanceOf(String className, String superClassName) {
        if (className.equals(superClassName)) {
            ;//fine
        } else {
            // advice(Foo f) for pc(f) with pc(Object o) for example
            // we need to ensure that Foo is an instance of Object
            ClassInfo classInfo = AsmClassInfo.getClassInfo(className, m_classLoader);
            boolean instanceOf = ClassInfoHelper.instanceOf(classInfo, superClassName);
            if ( ! instanceOf ) {
                throw new DefinitionException("Attempt to reference a pointcut with incompatible object type: for \""
                    + m_expression + "\" , " + className + " is not an instance of " + superClassName + "."
                    + " Error occured in " + m_namespace);
            }
        }
    }
}