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
import org.codehaus.aspectwerkz.util.Strings;

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

    public ArgsIndexVisitor(final ExpressionInfo expressionInfo,
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
        ArgsIndexVisitor expression = namespace.getExpressionInfo(node.getName()).getArgsIndexMapper();
        Boolean match = new Boolean(expression.match(context));

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
                //                System.out.println(" transitive arg" + adviceArgIndex + " " + adviceParamName + " -> " + exprArgIndex
                // + " -> " + targetArgIndex);
                sourceToTargetArgIndexes.put(adviceArgIndex, targetArgIndex);
            }
            context.m_exprIndexToTargetIndex = sourceToTargetArgIndexes;

            //debug:
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
            //            System.out.println("XXXARGS targetArg at match: " + ctx.getCurrentTargetArgsIndex() + " is pc expr arg "
            // + pointcutArgIndex
            //                + " @ " + m_expressionInfo.getExpressionAsString());
            ctx.m_exprIndexToTargetIndex.put(pointcutArgIndex, ctx.getCurrentTargetArgsIndex());
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

}