/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.inlining.compiler;


import org.codehaus.aspectwerkz.expression.ExpressionVisitor;
import org.codehaus.aspectwerkz.expression.Undeterministic;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.expression.ExpressionNamespace;
import org.codehaus.aspectwerkz.expression.ExpressionInfo;
import org.codehaus.aspectwerkz.expression.ast.ASTOr;
import org.codehaus.aspectwerkz.expression.ast.ASTAnd;
import org.codehaus.aspectwerkz.expression.ast.ASTNot;
import org.codehaus.aspectwerkz.expression.ast.ASTTarget;
import org.codehaus.aspectwerkz.expression.ast.ASTPointcutReference;
import org.codehaus.aspectwerkz.expression.ast.ASTExecution;
import org.codehaus.aspectwerkz.expression.ast.ASTCall;
import org.codehaus.aspectwerkz.expression.ast.ASTSet;
import org.codehaus.aspectwerkz.expression.ast.ASTGet;
import org.codehaus.aspectwerkz.expression.ast.ASTHandler;
import org.codehaus.aspectwerkz.expression.ast.ASTStaticInitialization;
import org.codehaus.aspectwerkz.expression.ast.ASTWithin;
import org.codehaus.aspectwerkz.expression.ast.ASTWithinCode;
import org.codehaus.aspectwerkz.expression.ast.ASTHasMethod;
import org.codehaus.aspectwerkz.expression.ast.ASTHasField;
import org.codehaus.aspectwerkz.expression.ast.ASTThis;
import org.codehaus.aspectwerkz.expression.ast.ASTCflow;
import org.codehaus.aspectwerkz.expression.ast.ASTCflowBelow;
import org.codehaus.aspectwerkz.expression.ast.ASTArgs;
import org.codehaus.aspectwerkz.transform.inlining.compiler.AbstractJoinPointCompiler;
import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;
import org.codehaus.aspectwerkz.transform.TransformationConstants;
import org.codehaus.aspectwerkz.cflow.CflowCompiler;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Constants;

/**
 * Visit an expression and push on the bytecode stack the boolean expression that corresponds to the residual
 * part for the target(CALLEE) filtering and cflow / cflowbelow runtime checks
 * <p/>
 * TODO: for now OR / AND / NOT are turned in IAND etc, ie "&" and not "&&" that is more efficient but is using labels.
 * <p/>
 * Note: we have to override here (and maintain) every visit Method that visit a node that appears in an expression
 * (f.e. set , get, etc, but not ASTParameter), since we cannot rely on AND/OR/NOT nodes to push the boolean expressions.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class RuntimeCheckVisitor extends ExpressionVisitor implements Constants {

    private AbstractJoinPointCompiler m_compiler;

    private CodeVisitor cv;

    private ExpressionInfo m_expressionInfo;

    private boolean m_isOptimizedJoinPoint;

    private int m_joinPointIndex;

    private int m_calleeIndex;

    /**
     * Create a new visitor given a specific AdviceInfo
     *
     * @param compiler             we are working for
     * @param cv                   of the method block we are compiling
     * @param info                 expression info
     * @param isOptimizedJoinPoint
     * @param joinPointIndex
     */
    public RuntimeCheckVisitor(final AbstractJoinPointCompiler compiler, final CodeVisitor cv,
                               final ExpressionInfo info, final boolean isOptimizedJoinPoint,
                               final int joinPointIndex, final int calleeIndex) {
        super(
                info,
                info.toString(),
                info.getNamespace(),
                info.getExpression().getASTRoot()
        );
        m_compiler = compiler;
        m_expressionInfo = info;
        m_isOptimizedJoinPoint = isOptimizedJoinPoint;
        m_joinPointIndex = joinPointIndex;
        m_calleeIndex = calleeIndex;
        this.cv = cv;
    }

    /**
     * Push the boolean typed expression on the stack.
     *
     * @param context
     */
    public void pushCheckOnStack(ExpressionContext context) {
        super.match(context);
    }

    /**
     * Handles OR expression
     *
     * @param node
     * @param data
     * @return
     */
    public Object visit(ASTOr node, Object data) {
        Boolean matchL = (Boolean) node.jjtGetChild(0).jjtAccept(this, data);
        Boolean matchR = (Boolean) node.jjtGetChild(1).jjtAccept(this, data);
        Boolean intermediate = Undeterministic.or(matchL, matchR);
        cv.visitInsn(IOR);
        for (int i = 2; i < node.jjtGetNumChildren(); i++) {
            Boolean matchNext = (Boolean) node.jjtGetChild(i).jjtAccept(this, data);
            intermediate = Undeterministic.or(intermediate, matchNext);
            cv.visitInsn(IOR);
        }
        return intermediate;
    }

    public Object visit(ASTAnd node, Object data) {
        Boolean matchL = (Boolean) node.jjtGetChild(0).jjtAccept(this, data);
        Boolean matchR = (Boolean) node.jjtGetChild(1).jjtAccept(this, data);
        Boolean intermediate = Undeterministic.and(matchL, matchR);
        cv.visitInsn(IAND);
        for (int i = 2; i < node.jjtGetNumChildren(); i++) {
            Boolean matchNext = (Boolean) node.jjtGetChild(i).jjtAccept(this, data);
            intermediate = Undeterministic.and(intermediate, matchNext);
            cv.visitInsn(IAND);
        }
        return intermediate;
    }

    public Object visit(ASTNot node, Object data) {
        Boolean match = (Boolean) node.jjtGetChild(0).jjtAccept(this, data);
        cv.visitInsn(INEG);
        return Undeterministic.not(match);
    }

    public Object visit(ASTTarget node, Object data) {
        Boolean match = (Boolean) super.visit(node, data);
        if (match != null) {
            push(match);
        } else {
            // runtime check
            String boundedTypeDesc = AsmHelper.convertReflectDescToTypeDesc(node.getBoundedType(m_expressionInfo));
            m_compiler.loadCallee(cv, m_isOptimizedJoinPoint, m_joinPointIndex, m_calleeIndex);
            cv.visitTypeInsn(INSTANCEOF, boundedTypeDesc.substring(1, boundedTypeDesc.length() - 1));
        }
        return match;
    }

    public Object visit(ASTThis node, Object data) {
        Boolean match = (Boolean) super.visit(node, data);
        push(match);
        return match;
    }

    public Object visit(ASTCflow node, Object data) {
        // runtime check
        String cflowClassName = CflowCompiler.getCflowAspectClassName(node.hashCode());
        cv.visitMethodInsn(
                INVOKESTATIC,
                cflowClassName,
                TransformationConstants.IS_IN_CFLOW_METOD_NAME,
                TransformationConstants.IS_IN_CFLOW_METOD_SIGNATURE
        );
        return (Boolean) super.visit(node, data);
    }

    public Object visit(ASTCflowBelow node, Object data) {
        // runtime check
        //TODO: cflowbelow ID will differ from cflow one.. => not optimized
        String cflowClassName = CflowCompiler.getCflowAspectClassName(node.hashCode());
        cv.visitMethodInsn(
                INVOKESTATIC,
                cflowClassName,
                TransformationConstants.IS_IN_CFLOW_METOD_NAME,
                TransformationConstants.IS_IN_CFLOW_METOD_SIGNATURE
        );
        return (Boolean) super.visit(node, data);
    }

    public Object visit(ASTArgs node, Object data) {
        Boolean match = (Boolean) super.visit(node, data);
        push(match);
        return match;
    }

    public Object visit(ASTPointcutReference node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        ExpressionNamespace namespace = ExpressionNamespace.getNamespace(m_namespace);
        ExpressionVisitor expression = namespace.getExpression(node.getName());

        // build a new RuntimeCheckVisitor to visit the sub expression
        RuntimeCheckVisitor referenced = new RuntimeCheckVisitor(
                m_compiler, cv, expression.getExpressionInfo(),
                m_isOptimizedJoinPoint, m_joinPointIndex,
                m_calleeIndex
        );
        return referenced.matchUndeterministic(context);
    }

    public Object visit(ASTExecution node, Object data) {
        Boolean match = (Boolean) super.visit(node, data);
        push(match);
        return match;
    }

    public Object visit(ASTCall node, Object data) {
        Boolean match = (Boolean) super.visit(node, data);
        push(match);
        return match;
    }

    public Object visit(ASTSet node, Object data) {
        Boolean match = (Boolean) super.visit(node, data);
        push(match);
        return match;
    }

    public Object visit(ASTGet node, Object data) {
        Boolean match = (Boolean) super.visit(node, data);
        push(match);
        return match;
    }

    public Object visit(ASTHandler node, Object data) {
        Boolean match = (Boolean) super.visit(node, data);
        push(match);
        return match;
    }

    public Object visit(ASTStaticInitialization node, Object data) {
        Boolean match = (Boolean) super.visit(node, data);
        push(match);
        return match;
    }

    public Object visit(ASTWithin node, Object data) {
        Boolean match = (Boolean) super.visit(node, data);
        push(match);
        return match;
    }

    public Object visit(ASTWithinCode node, Object data) {
        Boolean match = (Boolean) super.visit(node, data);
        push(match);
        return match;
    }

    public Object visit(ASTHasMethod node, Object data) {
        Boolean match = (Boolean) super.visit(node, data);
        push(match);
        return match;
    }

    public Object visit(ASTHasField node, Object data) {
        Boolean match = (Boolean) super.visit(node, data);
        push(match);
        return match;
    }


    private void push(Boolean b) {
        if (b == null) {
            throw new Error("attempt to push an undetermined match result");
        } else if (b.booleanValue()) {
            cv.visitInsn(ICONST_1);
        } else {
            cv.visitInsn(ICONST_M1);
        }
    }
}
