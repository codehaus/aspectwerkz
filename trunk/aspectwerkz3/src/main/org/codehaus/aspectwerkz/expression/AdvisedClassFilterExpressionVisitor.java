/**************************************************************************************
 * Copyright (c) Jonas BonŽr, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.expression;

import org.codehaus.aspectwerkz.expression.ast.ASTAnd;
import org.codehaus.aspectwerkz.expression.ast.ASTAttribute;
import org.codehaus.aspectwerkz.expression.ast.ASTCall;
import org.codehaus.aspectwerkz.expression.ast.ASTCflow;
import org.codehaus.aspectwerkz.expression.ast.ASTCflowBelow;
import org.codehaus.aspectwerkz.expression.ast.ASTClassPattern;
import org.codehaus.aspectwerkz.expression.ast.ASTConstructorPattern;
import org.codehaus.aspectwerkz.expression.ast.ASTExecution;
import org.codehaus.aspectwerkz.expression.ast.ASTExpression;
import org.codehaus.aspectwerkz.expression.ast.ASTFieldPattern;
import org.codehaus.aspectwerkz.expression.ast.ASTGet;
import org.codehaus.aspectwerkz.expression.ast.ASTHandler;
import org.codehaus.aspectwerkz.expression.ast.ASTMethodPattern;
import org.codehaus.aspectwerkz.expression.ast.ASTModifier;
import org.codehaus.aspectwerkz.expression.ast.ASTNot;
import org.codehaus.aspectwerkz.expression.ast.ASTOr;
import org.codehaus.aspectwerkz.expression.ast.ASTParameter;
import org.codehaus.aspectwerkz.expression.ast.ASTPointcutReference;
import org.codehaus.aspectwerkz.expression.ast.ASTRoot;
import org.codehaus.aspectwerkz.expression.ast.ASTSet;
import org.codehaus.aspectwerkz.expression.ast.ASTStaticInitialization;
import org.codehaus.aspectwerkz.expression.ast.ASTWithin;
import org.codehaus.aspectwerkz.expression.ast.ASTWithinCode;
import org.codehaus.aspectwerkz.expression.ast.ExpressionParserVisitor;
import org.codehaus.aspectwerkz.expression.ast.Node;
import org.codehaus.aspectwerkz.expression.ast.SimpleNode;
import org.codehaus.aspectwerkz.expression.ast.ASTArgs;
import org.codehaus.aspectwerkz.expression.ast.ASTArgParameter;
import org.codehaus.aspectwerkz.expression.regexp.TypePattern;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ClassInfoHelper;
import org.codehaus.aspectwerkz.reflect.MemberInfo;
import org.codehaus.aspectwerkz.reflect.ReflectionInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.annotation.AnnotationInfo;

import java.util.List;
import java.util.Iterator;

/**
 * The advised class filter visitor.
 *
 * Visit() methods are returning Boolean.TRUE/FALSE or null when decision cannot be taken.
 * Using null allow composition of OR/AND with NOT in the best way.
 *
 * FIXME - poorly optimized:
 * if pcA = within(X) and pcB = pcA AND call(* foo(..)), then ALL classes gets matched !
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class AdvisedClassFilterExpressionVisitor implements ExpressionParserVisitor {
    protected final ASTRoot m_root;

    protected final String m_expression;

    protected final String m_namespace;

    /**
     * Creates a new expression.
     * 
     * @param expression the expression as a string
     * @param namespace the namespace
     * @param root the AST root
     */
    public AdvisedClassFilterExpressionVisitor(final String expression, final String namespace, final ASTRoot root) {
        m_root = root;
        m_expression = expression;
        m_namespace = namespace;
    }

    /**
     * Matches the expression context.
     * 
     * @param context
     * @return
     */
    public boolean match(final ExpressionContext context) {
        Boolean match = ((Boolean) visit(m_root, context));
        if (match != null) {
            return match.booleanValue();
        } else {
            return true;
        }
    }

    // ============ Boot strap =============
    public Object visit(SimpleNode node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    public Object visit(ASTRoot node, Object data) {
        Node child = node.jjtGetChild(0);

//        // if 'call' or 'handler' but no 'within*' then return true
//        if (child instanceof ASTCall || child instanceof ASTHandler) {
//            return Boolean.TRUE;
//        }
        Boolean match = (Boolean) child.jjtAccept(this, data);
        return match;
    }

    public Object visit(ASTExpression node, Object data) {
        Node child = node.jjtGetChild(0);

//        // if 'call' or 'handler' but no 'within*' then return true
//        if (child instanceof ASTCall || child instanceof ASTHandler) {
//            return Boolean.TRUE;
//        }
        Boolean match = (Boolean) child.jjtAccept(this, data);
        return match;
    }

    // ============ Logical operators =============
    public Object visit(ASTOr node, Object data) {
        Boolean matchL = (Boolean) node.jjtGetChild(0).jjtAccept(this, data);
        Boolean matchR = (Boolean) node.jjtGetChild(1).jjtAccept(this, data);
        if (matchL != null && matchR !=null) {
            // regular OR
            if (matchL.equals(Boolean.TRUE) || matchR.equals(Boolean.TRUE)) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        } else {
            // one or both is/are undetermined
            // OR cannot be resolved
            return null;
        }
    }

    public Object visit(ASTAnd node, Object data) {
        Boolean matchL = (Boolean) node.jjtGetChild(0).jjtAccept(this, data);
        Boolean matchR = (Boolean) node.jjtGetChild(1).jjtAccept(this, data);
        if (matchL != null && matchR !=null) {
            // regular AND
            if (matchL.equals(Boolean.TRUE) && matchR.equals(Boolean.TRUE)) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        } else if (matchL != null && matchL.equals(Boolean.FALSE)) {
            // one is undetermined and the other is false, so result is false
            return Boolean.FALSE;
        } else if (matchR != null && matchR.equals(Boolean.FALSE)) {
            // one is undetermined and the other is false, so result is false
            return Boolean.FALSE;
        } else {
            // both are undetermined, or one is true and the other undetermined
            return null;
        }

//        boolean hasCallOrHandlerPc = false;
//        boolean hasWithinPc = false;
//        boolean hasNotPc = false;
//        int notPcIndex = -1;
//
//        // handle 'call', with and without 'within*'
//        int nrOfChildren = node.jjtGetNumChildren();
//        for (int i = 0; i < nrOfChildren; i++) {
//            Node child = node.jjtGetChild(i);
//            if (child instanceof ASTNot) {
//                hasNotPc = true;
//                notPcIndex = i;
//            } else if (child instanceof ASTCall || child instanceof ASTHandler) {
//                hasCallOrHandlerPc = true;
//            } else if (child instanceof ASTWithin || child instanceof ASTWithinCode) {
//                hasWithinPc = true;
//            }
//        }
//
//        // check the child of the 'not' node
//        if (hasCallOrHandlerPc && hasNotPc) {
//            Node childChild = node.jjtGetChild(notPcIndex).jjtGetChild(0);
//            if (childChild instanceof ASTWithin || childChild instanceof ASTWithinCode) {
//                if (Boolean.TRUE.equals(childChild.jjtAccept(this, data))) {
//                    return Boolean.FALSE;
//                } else {
//                    return Boolean.TRUE;
//                }
//            }
//        } else if (hasCallOrHandlerPc && !hasWithinPc) {
//            return Boolean.TRUE;
//        }
//
//        // if not a 'call' or 'handler' pointcut
//        for (int i = 0; i < nrOfChildren; i++) {
//            Boolean match = (Boolean) node.jjtGetChild(i).jjtAccept(this, data);
//            if (match.equals(Boolean.TRUE)) {
//                return Boolean.TRUE;
//            }
//        }
//        return Boolean.FALSE;
    }

    public Object visit(ASTNot node, Object data) {
//        // the NOT is not evaluated unless on within expressions
//        if (node.jjtGetChild(0) instanceof ASTWithin) {
//            Boolean match = (Boolean) node.jjtGetChild(0).jjtAccept(this, data);
//            if (match.equals(Boolean.TRUE)) {
//                return Boolean.FALSE;
//            } else {
//                return Boolean.TRUE;
//            }
//        }
//
        Boolean match = (Boolean) node.jjtGetChild(0).jjtAccept(this, data);
        if (match !=null) {
            // regular NOT
            if (match.equals(Boolean.TRUE)) {
                return Boolean.FALSE;
            } else {
                return Boolean.TRUE;
            }
        } else {
            return null;
        }
    }

    // ============ Pointcut types =============
    public Object visit(ASTPointcutReference node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        ExpressionNamespace namespace = ExpressionNamespace.getNamespace(m_namespace);
        AdvisedClassFilterExpressionVisitor expression = namespace.getAdvisedClassExpression(node.getName());
        return new Boolean(expression.match(context));
    }

    public Object visit(ASTExecution node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        if (context.hasAnyPointcut() || context.hasExecutionPointcut()) {
            return node.jjtGetChild(0).jjtAccept(this, context.getReflectionInfo());
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTCall node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        if (context.hasAnyPointcut() || context.hasCallPointcut()) {
            return node.jjtGetChild(0).jjtAccept(this, context.getReflectionInfo());
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTSet node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        if (context.hasAnyPointcut() || context.hasSetPointcut()) {
            return node.jjtGetChild(0).jjtAccept(this, context.getReflectionInfo());
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTGet node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        if (context.hasAnyPointcut() || context.hasGetPointcut()) {
            return node.jjtGetChild(0).jjtAccept(this, context.getReflectionInfo());
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTHandler node, Object data) {
        return Boolean.TRUE;
    }

    public Object visit(ASTStaticInitialization node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        if (context.hasAnyPointcut() || context.hasStaticInitializationPointcut()) {
            return node.jjtGetChild(0).jjtAccept(this, context.getReflectionInfo());
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTWithin node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        ReflectionInfo reflectionInfo = context.getReflectionInfo();
        if (reflectionInfo instanceof MemberInfo) {
            return node.jjtGetChild(0).jjtAccept(this, ((MemberInfo) reflectionInfo).getDeclaringType());
        } else if (reflectionInfo instanceof ClassInfo) {
            return node.jjtGetChild(0).jjtAccept(this, reflectionInfo);
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTWithinCode node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        return node.jjtGetChild(0).jjtAccept(this, context.getReflectionInfo());
    }

    public Object visit(ASTCflow node, Object data) {
        return Boolean.FALSE;//FIXME
    }

    public Object visit(ASTCflowBelow node, Object data) {
        return Boolean.FALSE;//FIXME
    }

    public Object visit(ASTArgs node, Object data) {
        return Boolean.TRUE;
    }

    // ============ Patterns =============
    public Object visit(ASTClassPattern node, Object data) {
        ClassInfo classInfo = (ClassInfo) data;
        TypePattern typePattern = node.getTypePattern();
        if (ClassInfoHelper.matchType(typePattern, classInfo) && visitAttributes(node, classInfo)) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTMethodPattern node, Object data) {
        if (data instanceof ClassInfo) {
            ClassInfo classInfo = (ClassInfo) data;
            if (ClassInfoHelper.matchType(node.getDeclaringTypePattern(), classInfo)) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        } else if (data instanceof MethodInfo) {
            MethodInfo methodInfo = (MethodInfo) data;
            if (ClassInfoHelper.matchType(node.getDeclaringTypePattern(), methodInfo.getDeclaringType())) {
                return null;// it might not match further because of modifiers etc
            }
            return Boolean.FALSE;
        }
        return Boolean.FALSE;
    }

    public Object visit(ASTConstructorPattern node, Object data) {
        if (data instanceof ClassInfo) {
            ClassInfo classInfo = (ClassInfo) data;
            if (ClassInfoHelper.matchType(node.getDeclaringTypePattern(), classInfo)) {
                // we matched but the actual match result may be false
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public Object visit(ASTFieldPattern node, Object data) {
        if (data instanceof ClassInfo) {
            ClassInfo classInfo = (ClassInfo) data;
            if (ClassInfoHelper.matchType(node.getDeclaringTypePattern(), classInfo)) {
                // we matched but the actual match result may be false
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public Object visit(ASTParameter node, Object data) {
        ClassInfo parameterType = (ClassInfo) data;
        if (ClassInfoHelper.matchType(node.getDeclaringClassPattern(), parameterType)) {
            // we matched but the actual match result may be false
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTArgParameter node, Object data) {
        // never called
        return Boolean.TRUE;
    }

    public Object visit(ASTAttribute node, Object data) {
        // called for class level annotation matching f.e. in a within context
        List annotations = (List) data;
        for (Iterator it = annotations.iterator(); it.hasNext();) {
            AnnotationInfo annotation = (AnnotationInfo) it.next();
            if (annotation.getName().equals(node.getName())) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public Object visit(ASTModifier node, Object data) {
        // ??
        return null;
    }

    /**
     * Returns the string representation of the AST.
     * 
     * @return
     */
    public String toString() {
        return m_expression;
    }

    protected boolean visitAttributes(SimpleNode node, ReflectionInfo refInfo) {
        int nrChildren = node.jjtGetNumChildren();
        if (nrChildren != 0) {
            for (int i = 0; i < nrChildren; i++) {
                Node child = node.jjtGetChild(i);
                if (child instanceof ASTAttribute) {
                    List annotations = refInfo.getAnnotations();
                    if (Boolean.TRUE.equals(child.jjtAccept(this, annotations))) {
                        continue;
                    } else {
                        return false;
                    }
                }
            }
        }
        return true;
    }

}