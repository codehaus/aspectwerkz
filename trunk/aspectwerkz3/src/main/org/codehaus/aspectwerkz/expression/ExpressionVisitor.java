/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.expression;

import org.codehaus.aspectwerkz.definition.attribute.CustomAttribute;
import org.codehaus.aspectwerkz.expression.ast.*;
import org.codehaus.aspectwerkz.expression.regexp.TypePattern;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ClassInfoHelper;
import org.codehaus.aspectwerkz.reflect.ConstructorInfo;
import org.codehaus.aspectwerkz.reflect.FieldInfo;
import org.codehaus.aspectwerkz.reflect.MemberInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.reflect.ReflectionInfo;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The expression visitor.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class ExpressionVisitor implements ExpressionParserVisitor {
    protected ASTRoot m_root;
    protected String m_expression;
    protected String m_namespace;

    /**
     * Creates a new expression.
     *
     * @param expression the expression as a string
     * @param namespace  the namespace
     * @param root       the AST root
     */
    public ExpressionVisitor(final String expression, final String namespace, final ASTRoot root) {
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
        return ((Boolean)visit(m_root, context)).booleanValue();
    }

    // ============ Boot strap =============
    public Object visit(SimpleNode node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    public Object visit(ASTRoot node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    public Object visit(ASTExpression node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    // ============ Logical operators =============
    public Object visit(ASTOr node, Object data) {
        int nrOfChildren = node.jjtGetNumChildren();
        for (int i = 0; i < nrOfChildren; i++) {
            Boolean match = (Boolean)node.jjtGetChild(i).jjtAccept(this, data);
            if (match.equals(Boolean.TRUE)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public Object visit(ASTAnd node, Object data) {
        int nrOfChildren = node.jjtGetNumChildren();
        for (int i = 0; i < nrOfChildren; i++) {
            Boolean match = (Boolean)node.jjtGetChild(i).jjtAccept(this, data);
            if (match.equals(Boolean.FALSE)) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    public Object visit(ASTNot node, Object data) {
        Node child = node.jjtGetChild(0);
        Boolean match = (Boolean)child.jjtAccept(this, data);
        if (child instanceof ASTCflow || child instanceof ASTCflowBelow) {
            return match;
        } else {
            if (match.equals(Boolean.TRUE)) {
                return Boolean.FALSE;
            } else {
                return Boolean.TRUE;
            }
        }
    }

    // ============ Pointcut types =============
    public Object visit(ASTPointcutReference node, Object data) {
        ExpressionContext context = (ExpressionContext)data;
        ExpressionNamespace namespace = ExpressionNamespace.getNamespace(m_namespace);
        ExpressionVisitor expression = namespace.getExpression(node.getName());
        return Boolean.valueOf(expression.match(context));
    }

    public Object visit(ASTExecution node, Object data) {
        ExpressionContext context = (ExpressionContext)data;
        if (context.hasExecutionPointcut() && (context.hasMethodInfo() || context.hasConstructorInfo())) {
            return node.jjtGetChild(0).jjtAccept(this, context.getReflectionInfo());
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTCall node, Object data) {
        ExpressionContext context = (ExpressionContext)data;
        if (context.hasCallPointcut() && (context.hasMethodInfo() || context.hasConstructorInfo())) {
            return node.jjtGetChild(0).jjtAccept(this, context.getReflectionInfo());
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTSet node, Object data) {
        ExpressionContext context = (ExpressionContext)data;
        if (context.hasSetPointcut() && context.hasFieldInfo()) {
            return node.jjtGetChild(0).jjtAccept(this, context.getReflectionInfo());
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTGet node, Object data) {
        ExpressionContext context = (ExpressionContext)data;
        if (context.hasGetPointcut() && context.hasFieldInfo()) {
            return node.jjtGetChild(0).jjtAccept(this, context.getReflectionInfo());
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTHandler node, Object data) {
        ExpressionContext context = (ExpressionContext)data;
        if (context.hasHandlerPointcut() && context.hasClassInfo()) {
            return node.jjtGetChild(0).jjtAccept(this, context.getReflectionInfo());
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTStaticInitialization node, Object data) {
        ExpressionContext context = (ExpressionContext)data;
        if (context.hasStaticInitializationPointcut() && context.hasClassInfo()) {
            return node.jjtGetChild(0).jjtAccept(this, context.getReflectionInfo());
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTWithin node, Object data) {
        ExpressionContext context = (ExpressionContext)data;
        if (context.hasWithinReflectionInfo()) {
            ReflectionInfo withinInfo = context.getWithinReflectionInfo();
            if (withinInfo instanceof MemberInfo) {
                return node.jjtGetChild(0).jjtAccept(this, ((MemberInfo)withinInfo).getDeclaringType());
            } else if (withinInfo instanceof ClassInfo) {
                return node.jjtGetChild(0).jjtAccept(this, withinInfo);
            }
        }
        return Boolean.FALSE;
    }

    public Object visit(ASTWithinCode node, Object data) {
        ExpressionContext context = (ExpressionContext)data;
        if (context.hasWithinReflectionInfo()) {
            return node.jjtGetChild(0).jjtAccept(this, context.getWithinReflectionInfo());
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTCflow node, Object data) {
        return Boolean.TRUE;
    }

    public Object visit(ASTCflowBelow node, Object data) {
        return Boolean.TRUE;
    }

    // ============ Patterns =============
    public Object visit(ASTClassPattern node, Object data) {
        ClassInfo classInfo = (ClassInfo)data;
        TypePattern typePattern = node.getTypePattern();
        if (ClassInfoHelper.matchType(typePattern, classInfo) && visitAttributes(node, classInfo)
            && visitModifiers(node, classInfo)) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTMethodPattern node, Object data) {
        if (data instanceof MethodInfo) { // TODO: is this safe check needed?
            MethodInfo methodInfo = (MethodInfo)data;
            if (node.getMethodNamePattern().matches(methodInfo.getName())
                && ClassInfoHelper.matchType(node.getDeclaringTypePattern(), methodInfo.getDeclaringType())
                && ClassInfoHelper.matchType(node.getReturnTypePattern(), methodInfo.getReturnType())
                && visitAttributes(node, methodInfo) && visitModifiers(node, methodInfo)
                && visitParameters(node, methodInfo.getParameterTypes())) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public Object visit(ASTConstructorPattern node, Object data) {
        if (data instanceof ConstructorInfo) { // TODO: is this safe check needed?
            ConstructorInfo constructorMetaData = (ConstructorInfo)data;
            if (ClassInfoHelper.matchType(node.getDeclaringTypePattern(), constructorMetaData.getDeclaringType())
                && visitAttributes(node, constructorMetaData) && visitModifiers(node, constructorMetaData)
                && visitParameters(node, constructorMetaData.getParameterTypes())) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public Object visit(ASTFieldPattern node, Object data) {
        if (data instanceof FieldInfo) { // TODO: is this safe check needed?
            FieldInfo fieldInfo = (FieldInfo)data;
            if (node.getFieldNamePattern().matches(fieldInfo.getName())
                && ClassInfoHelper.matchType(node.getDeclaringTypePattern(), fieldInfo.getDeclaringType())
                && ClassInfoHelper.matchType(node.getFieldTypePattern(), fieldInfo.getType())
                && visitAttributes(node, fieldInfo) && visitModifiers(node, fieldInfo)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public Object visit(ASTParameter node, Object data) {
        ClassInfo parameterType = (ClassInfo)data;
        if (ClassInfoHelper.matchType(node.getDeclaringClassPattern(), parameterType)) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTAttribute node, Object data) {
        List attributes = (List)data;
        for (Iterator it = attributes.iterator(); it.hasNext();) {
            CustomAttribute attribute = (CustomAttribute)it.next();
            if (attribute.getName().equals(node.getName())) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public Object visit(ASTModifier node, Object data) {
        ReflectionInfo refInfo = (ReflectionInfo)data;
        int modifiersToMatch = refInfo.getModifiers();
        int modifierPattern = node.getModifier();
        if (((modifierPattern & Modifier.PUBLIC) != 0) && ((modifiersToMatch & Modifier.PUBLIC) == 0)) {
            return Boolean.FALSE;
        }
        if (((modifierPattern & Modifier.PROTECTED) != 0) && ((modifiersToMatch & Modifier.PROTECTED) == 0)) {
            return Boolean.FALSE;
        }
        if (((modifierPattern & Modifier.PRIVATE) != 0) && ((modifiersToMatch & Modifier.PRIVATE) == 0)) {
            return Boolean.FALSE;
        }
        if (((modifierPattern & Modifier.STATIC) != 0) && ((modifiersToMatch & Modifier.STATIC) == 0)) {
            return Boolean.FALSE;
        }
        if (((modifierPattern & Modifier.SYNCHRONIZED) != 0) && ((modifiersToMatch & Modifier.SYNCHRONIZED) == 0)) {
            return Boolean.FALSE;
        }
        if (((modifierPattern & Modifier.FINAL) != 0) && ((modifiersToMatch & Modifier.FINAL) == 0)) {
            return Boolean.FALSE;
        }
        if (((modifierPattern & Modifier.TRANSIENT) != 0) && ((modifiersToMatch & Modifier.TRANSIENT) == 0)) {
            return Boolean.FALSE;
        }
        if (((modifierPattern & Modifier.VOLATILE) != 0) && ((modifiersToMatch & Modifier.VOLATILE) == 0)) {
            return Boolean.FALSE;
        }
        if (((modifierPattern & Modifier.STRICT) != 0) && ((modifiersToMatch & Modifier.STRICT) == 0)) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    protected boolean visitAttributes(SimpleNode node, ReflectionInfo refInfo) {
        int nrChildren = node.jjtGetNumChildren();
        if (nrChildren != 0) {
            for (int i = 0; i < nrChildren; i++) {
                Node child = node.jjtGetChild(i);
                if (child instanceof ASTAttribute) {
                    if (Boolean.TRUE.equals(child.jjtAccept(this, refInfo.getAnnotations()))) {
                        continue;
                    } else {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    protected boolean visitModifiers(SimpleNode node, ReflectionInfo refInfo) {
        int nrChildren = node.jjtGetNumChildren();
        if (nrChildren != 0) {
            for (int i = 0; i < nrChildren; i++) {
                Node child = node.jjtGetChild(i);
                if (child instanceof ASTModifier) {
                    if (Boolean.TRUE.equals(child.jjtAccept(this, refInfo))) {
                        continue;
                    } else {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    protected boolean visitParameters(SimpleNode node, ClassInfo[] parameterTypes) {
        int nrChildren = node.jjtGetNumChildren();
        if (nrChildren != 0) { // has nodes

            // collect the parameter nodes
            List parameterNodes = new ArrayList();
            for (int i = 0; i < nrChildren; i++) {
                Node child = node.jjtGetChild(i);
                if (child instanceof ASTParameter) {
                    parameterNodes.add(child);
                }
            }

            // if number of nodes is greater than the number of parameter types -> bail out
            // unless there is one single node with the eager wildcard pattern '..' -> match
            if (parameterNodes.size() > parameterTypes.length) {
                if (parameterNodes.size() == 1) {
                    ASTParameter param = (ASTParameter)parameterNodes.get(0);
                    if (param.getDeclaringClassPattern().isEagerWildCard()) {
                        return true;
                    }
                }
                return false;
            }

            // iterate over the parameter nodes
            int j = 0;
            for (Iterator iterator = parameterNodes.iterator(); iterator.hasNext();) {
                ASTParameter parameter = (ASTParameter)iterator.next();
                if (parameter.getDeclaringClassPattern().isEagerWildCard()) {
                    return true;
                }
                if (Boolean.TRUE.equals(parameter.jjtAccept(this, parameterTypes[j++]))) {
                    continue;
                } else {
                    return false;
                }
            }
        } else if (parameterTypes.length != 0) { // no nodes but parameters to match
            return false;
        }
        return true;
    }

    /**
     * Returns the string representation of the expression.
     *
     * @return
     */
    public String toString() {
        return m_expression;
    }
}
