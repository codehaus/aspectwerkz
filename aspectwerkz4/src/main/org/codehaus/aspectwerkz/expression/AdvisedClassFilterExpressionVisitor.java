/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
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
import org.codehaus.aspectwerkz.expression.ast.ASTHasField;
import org.codehaus.aspectwerkz.expression.ast.ASTHasMethod;
import org.codehaus.aspectwerkz.expression.ast.ASTTarget;
import org.codehaus.aspectwerkz.expression.ast.ASTThis;
import org.codehaus.aspectwerkz.expression.ast.ASTNot;
import org.codehaus.aspectwerkz.expression.regexp.TypePattern;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.MemberInfo;
import org.codehaus.aspectwerkz.reflect.ReflectionInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.reflect.ConstructorInfo;
import org.codehaus.aspectwerkz.reflect.FieldInfo;
import org.codehaus.aspectwerkz.reflect.ClassInfoHelper;
import org.codehaus.aspectwerkz.annotation.AnnotationInfo;
import org.codehaus.aspectwerkz.util.Util;

import java.util.List;
import java.util.Iterator;

/**
 * The advised class filter visitor.
 * <p/>
 * Visit() methods are returning Boolean.TRUE/FALSE or null when decision cannot be taken.
 * Using null allow composition of OR/AND with NOT in the best way.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 * @author Michael Nascimento
 */
public class AdvisedClassFilterExpressionVisitor extends ExpressionVisitor implements ExpressionParserVisitor {

    /**
     * Creates a new expression.
     *
     * @param expression the expression as a string
     * @param namespace  the namespace
     * @param root       the AST root
     */
    public AdvisedClassFilterExpressionVisitor(final ExpressionInfo expressionInfo, final String expression,
                                               final String namespace, final ASTRoot root) {
        super(expressionInfo, expression, namespace, root);
    }

    // ============ Boot strap =============
    public Object visit(SimpleNode node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    public Object visit(ASTRoot node, Object data) {
        Node child = node.jjtGetChild(0);
        Boolean match = (Boolean) child.jjtAccept(this, data);
        return match;
    }

    public Object visit(ASTExpression node, Object data) {
        Node child = node.jjtGetChild(0);
        Boolean match = (Boolean) child.jjtAccept(this, data);
        return match;
    }

    public Object visit(ASTNot node, Object data) {
        return super.visit(node,data);
    }

    // ============ Pointcut types =============
    public Object visit(ASTPointcutReference node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        ExpressionNamespace namespace = ExpressionNamespace.getNamespace(m_namespace);
        AdvisedClassFilterExpressionVisitor expression = namespace.getAdvisedClassExpression(node.getName());
        return expression.matchUndeterministic(context);
    }

    public Object visit(ASTExecution node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        // for execution evaluation, we always have the reflection info available
        if (context.hasWithinPointcut() || context.hasExecutionPointcut()) {
            return node.jjtGetChild(0).jjtAccept(this, context.getReflectionInfo());
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTCall node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        // for call evaluation, the reflection info may be null at the early matching phase
        if (context.hasWithinPointcut() || context.hasCallPointcut()) {
            if (context.hasReflectionInfo()) {
                return node.jjtGetChild(0).jjtAccept(this, context.getReflectionInfo());
            } else {
                return null;
            }
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTSet node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        // for set evaluation, the reflection info may be null at the early matching phase
        // when we will allow for field interception within non declaring class
        if (context.hasWithinPointcut() || context.hasSetPointcut()) {
            if (context.hasReflectionInfo()) {
                return node.jjtGetChild(0).jjtAccept(this, context.getReflectionInfo());
            } else {
                return null;
            }
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTGet node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        // for get evaluation, the reflection info may be null at the early matching phase
        // when we will allow for field interception within non declaring class
        if (context.hasWithinPointcut() || context.hasGetPointcut()) {
            if (context.hasReflectionInfo()) {
                return node.jjtGetChild(0).jjtAccept(this, context.getReflectionInfo());
            } else {
                return null;
            }
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTHandler node, Object data) {
        //FIXME
        return Boolean.TRUE;
    }

    public Object visit(ASTStaticInitialization node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        if (context.hasWithinPointcut() || context.hasStaticInitializationPointcut()) {
            return node.jjtGetChild(0).jjtAccept(this, context.getReflectionInfo());
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTWithin node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        ReflectionInfo reflectionInfo = context.getWithinReflectionInfo();
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
        return node.jjtGetChild(0).jjtAccept(this, context.getWithinReflectionInfo());
    }

    public Object visit(ASTCflow node, Object data) {
        return null;
    }

    public Object visit(ASTCflowBelow node, Object data) {
        return null;
    }

    public Object visit(ASTArgs node, Object data) {
        return null;
    }

    public Object visit(ASTHasMethod node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        return node.jjtGetChild(0).jjtAccept(this, context.getWithinReflectionInfo());
    }

    public Object visit(ASTHasField node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        return node.jjtGetChild(0).jjtAccept(this, context.getWithinReflectionInfo());
    }

    public Object visit(ASTTarget node, Object data) {
        return null;// is that good enough ? For execution PC we would optimize some
    }

    public Object visit(ASTThis node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        if (context.hasWithinReflectionInfo()) {
            ReflectionInfo withinInfo = context.getWithinReflectionInfo();
            if (withinInfo instanceof MemberInfo) {
                return Util.booleanValueOf(
                        ClassInfoHelper.instanceOf(
                                ((MemberInfo) withinInfo).getDeclaringType(),
                                node.getBoundedType(m_expressionInfo)
                        )
                );
            } else if (withinInfo instanceof ClassInfo) {
                return Util.booleanValueOf(
                        ClassInfoHelper.instanceOf((ClassInfo) withinInfo, node.getBoundedType(m_expressionInfo))
                );
            }
        }
        return Boolean.FALSE;
    }

    // ============ Patterns =============
    public Object visit(ASTClassPattern node, Object data) {
        ClassInfo classInfo = (ClassInfo) data;
        if (node.getTypePattern().matchType(classInfo) && visitAttributes(node, classInfo)) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTMethodPattern node, Object data) {
        if (data instanceof ClassInfo) {
            ClassInfo classInfo = (ClassInfo) data;
            if (node.getDeclaringTypePattern().matchType(classInfo)) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        } else if (data instanceof MethodInfo) {
            MethodInfo methodInfo = (MethodInfo) data;
            if (node.getDeclaringTypePattern().matchType(methodInfo.getDeclaringType())) {
                return null;// it might not match further because of modifiers etc
            }
            return Boolean.FALSE;
        }
        return Boolean.FALSE;
    }

    public Object visit(ASTConstructorPattern node, Object data) {
        if (data instanceof ClassInfo) {
            ClassInfo classInfo = (ClassInfo) data;
            if (node.getDeclaringTypePattern().matchType(classInfo)) {
                // we matched but the actual match result may be false
                return Boolean.TRUE;
            }
        } else if (data instanceof ConstructorInfo) {
            ConstructorInfo constructorInfo = (ConstructorInfo) data;
            if (node.getDeclaringTypePattern().matchType(constructorInfo.getDeclaringType())) {
                return null;// it might not match further because of modifiers etc
            }
            return Boolean.FALSE;
        }
        return Boolean.FALSE;
    }

    public Object visit(ASTFieldPattern node, Object data) {
        if (data instanceof ClassInfo) {
            ClassInfo classInfo = (ClassInfo) data;
            if (node.getDeclaringTypePattern().matchType(classInfo)) {
                // we matched but the actual match result may be false
                return Boolean.TRUE;
            }
        } else if (data instanceof FieldInfo) {
            FieldInfo fieldInfo = (FieldInfo) data;
            if (node.getDeclaringTypePattern().matchType(fieldInfo.getDeclaringType())) {
                return null;// it might not match further because of modifiers etc
            }
            return Boolean.FALSE;
        }
        return Boolean.FALSE;
    }

    public Object visit(ASTParameter node, Object data) {
        ClassInfo parameterType = (ClassInfo) data;
        if (node.getDeclaringClassPattern().matchType(parameterType)) {
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

    public static Boolean matchUnderterministicAnd(Boolean lhs, Boolean rhs) {
        if (lhs != null && rhs != null) {
            // regular AND
            if (lhs.equals(Boolean.TRUE) && rhs.equals(Boolean.TRUE)) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        } else if (lhs != null && lhs.equals(Boolean.FALSE)) {
            // one is undetermined and the other is false, so result is false
            return Boolean.FALSE;
        } else if (rhs != null && rhs.equals(Boolean.FALSE)) {
            // one is undetermined and the other is false, so result is false
            return Boolean.FALSE;
        } else {
            // both are undetermined, or one is true and the other undetermined
            return null;
        }
    }

    public static Boolean matchUndeterministicOr(Boolean lhs, Boolean rhs) {
        if (lhs != null && rhs != null) {
            // regular OR
            if (lhs.equals(Boolean.TRUE) || rhs.equals(Boolean.TRUE)) {
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
}