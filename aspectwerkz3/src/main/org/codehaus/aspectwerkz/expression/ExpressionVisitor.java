/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.expression;

import org.codehaus.aspectwerkz.annotation.AnnotationInfo;
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
import org.codehaus.aspectwerkz.reflect.ConstructorInfo;
import org.codehaus.aspectwerkz.reflect.FieldInfo;
import org.codehaus.aspectwerkz.reflect.MemberInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.reflect.ReflectionInfo;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.codehaus.aspectwerkz.expression.ast.ASTHasField;
import org.codehaus.aspectwerkz.expression.ast.ASTHasMethod;

/**
 * The expression visitor.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur </a>
 */
public class ExpressionVisitor implements ExpressionParserVisitor {

    protected ASTRoot m_root;
    protected String m_expression;
    protected String m_namespace;

    /**
     * The expressionInfo this visitor is built on for expression with signature Caution: Can be null for visitor that
     * don't need this information.
     */
    protected ExpressionInfo m_expressionInfo;

    /**
     * Creates a new expression.
     * 
     * @param expressionInfo the expressionInfo this visitor is built on for expression with signature
     * @param expression the expression as a string
     * @param namespace the namespace
     * @param root the AST root
     */
    public ExpressionVisitor(final ExpressionInfo expressionInfo,
                             final String expression,
                             final String namespace,
                             final ASTRoot root) {
        m_expressionInfo = expressionInfo;
        m_expression = expression;
        m_namespace = namespace;
        m_root = root;
    }

    /**
     * Matches the expression context.
     * 
     * @param context
     * @return
     */
    public boolean match(final ExpressionContext context) {
        return ((Boolean) visit(m_root, context)).booleanValue();
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
            Boolean match = (Boolean) node.jjtGetChild(i).jjtAccept(this, data);
            if (match.equals(Boolean.TRUE)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public Object visit(ASTAnd node, Object data) {
        int nrOfChildren = node.jjtGetNumChildren();
        for (int i = 0; i < nrOfChildren; i++) {
            Boolean match = (Boolean) node.jjtGetChild(i).jjtAccept(this, data);
            if (match.equals(Boolean.FALSE)) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    public Object visit(ASTNot node, Object data) {
        Node child = node.jjtGetChild(0);
        Boolean match = (Boolean) child.jjtAccept(this, data);
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
        ExpressionContext context = (ExpressionContext) data;
        ExpressionNamespace namespace = ExpressionNamespace.getNamespace(m_namespace);
        ExpressionVisitor expression = namespace.getExpression(node.getName());
        return new Boolean(expression.match(context));
    }

    public Object visit(ASTExecution node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        if (context.hasExecutionPointcut() && (context.hasMethodInfo() || context.hasConstructorInfo())) {
            return node.jjtGetChild(0).jjtAccept(this, context.getReflectionInfo());
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTCall node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        if (context.hasCallPointcut() && (context.hasMethodInfo() || context.hasConstructorInfo())) {
            return node.jjtGetChild(0).jjtAccept(this, context.getReflectionInfo());
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTSet node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        if (context.hasSetPointcut() && context.hasFieldInfo()) {
            return node.jjtGetChild(0).jjtAccept(this, context.getReflectionInfo());
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTGet node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        if (context.hasGetPointcut() && context.hasFieldInfo()) {
            return node.jjtGetChild(0).jjtAccept(this, context.getReflectionInfo());
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTHandler node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        if (context.hasHandlerPointcut() && context.hasClassInfo()) {
            return node.jjtGetChild(0).jjtAccept(this, context.getReflectionInfo());
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTStaticInitialization node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        if (context.hasStaticInitializationPointcut() && context.hasClassInfo()) {
            return node.jjtGetChild(0).jjtAccept(this, context.getReflectionInfo());
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTWithin node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        if (context.hasWithinReflectionInfo()) {
            ReflectionInfo withinInfo = context.getWithinReflectionInfo();
            if (withinInfo instanceof MemberInfo) {
                return node.jjtGetChild(0).jjtAccept(this, ((MemberInfo) withinInfo).getDeclaringType());
            } else if (withinInfo instanceof ClassInfo) {
                return node.jjtGetChild(0).jjtAccept(this, withinInfo);
            }
        }
        return Boolean.FALSE;
    }

    public Object visit(ASTWithinCode node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        if (context.hasWithinReflectionInfo()) {
            return node.jjtGetChild(0).jjtAccept(this, context.getWithinReflectionInfo());
        } else {
            return Boolean.FALSE;
        }
    }


    public Object visit(ASTHasMethod node, Object data) {
        ExpressionContext context = (ExpressionContext) data;

        ReflectionInfo info = context.getWithinReflectionInfo();
        ClassInfo classInfo = (info instanceof MemberInfo) ?
            ((MemberInfo)info).getDeclaringType() : (ClassInfo)info;

        Node childNode = node.jjtGetChild(0);
        MethodInfo[] methodInfos = classInfo.getMethods();

        for (int i = 0; i < methodInfos.length; i++) {
            if (Boolean.TRUE.equals(childNode.jjtAccept(this, methodInfos[i]))) {
                return Boolean.TRUE;
            }
        }

        ConstructorInfo[] constructorInfos = classInfo.getConstructors();

        for (int i = 0; i < constructorInfos.length; i++) {
            if (Boolean.TRUE.equals(childNode.jjtAccept(this, constructorInfos[i]))) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    public Object visit(ASTHasField node, Object data) {
        ExpressionContext context = (ExpressionContext) data;

        ReflectionInfo info = context.getWithinReflectionInfo();
        ClassInfo classInfo = (info instanceof MemberInfo) ?
            ((MemberInfo)info).getDeclaringType() : (ClassInfo)info;

        Node childNode = node.jjtGetChild(0);
        FieldInfo[] fieldInfos = classInfo.getFields();

        for (int i = 0; i < fieldInfos.length; i++) {
            if (Boolean.TRUE.equals(childNode.jjtAccept(this, fieldInfos[i]))) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    public Object visit(ASTCflow node, Object data) {
        return Boolean.TRUE;
    }

    public Object visit(ASTCflowBelow node, Object data) {
        return Boolean.TRUE;
    }

    // ============ Patterns =============
    public Object visit(ASTClassPattern node, Object data) {
        ClassInfo classInfo = (ClassInfo) data;
        TypePattern typePattern = node.getTypePattern();
        if (ClassInfoHelper.matchType(typePattern, classInfo)
            && visitAttributes(node, classInfo)
            && visitModifiers(node, classInfo)) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTMethodPattern node, Object data) {
        if (data instanceof MethodInfo) {
            MethodInfo methodInfo = (MethodInfo) data;
            if (node.getMethodNamePattern().matches(methodInfo.getName())
                && ClassInfoHelper.matchType(node.getDeclaringTypePattern(), methodInfo.getDeclaringType())
                && ClassInfoHelper.matchType(node.getReturnTypePattern(), methodInfo.getReturnType())
                && visitAttributes(node, methodInfo)
                && visitModifiers(node, methodInfo)
                && visitParameters(node, methodInfo.getParameterTypes())) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    public Object visit(ASTConstructorPattern node, Object data) {
        if (data instanceof ConstructorInfo) {
            ConstructorInfo constructorMetaData = (ConstructorInfo) data;
            if (ClassInfoHelper.matchType(node.getDeclaringTypePattern(), constructorMetaData.getDeclaringType())
                && visitAttributes(node, constructorMetaData)
                && visitModifiers(node, constructorMetaData)
                && visitParameters(node, constructorMetaData.getParameterTypes())) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public Object visit(ASTFieldPattern node, Object data) {
        if (data instanceof FieldInfo) {
            FieldInfo fieldInfo = (FieldInfo) data;
            if (node.getFieldNamePattern().matches(fieldInfo.getName())
                && ClassInfoHelper.matchType(node.getDeclaringTypePattern(), fieldInfo.getDeclaringType())
                && ClassInfoHelper.matchType(node.getFieldTypePattern(), fieldInfo.getType())
                && visitAttributes(node, fieldInfo)
                && visitModifiers(node, fieldInfo)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public Object visit(ASTParameter node, Object data) {
        ClassInfo parameterType = (ClassInfo) data;
        if (ClassInfoHelper.matchType(node.getDeclaringClassPattern(), parameterType)) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTArgs node, Object data) {
        ExpressionContext ctx = (ExpressionContext) data;
        if (node.jjtGetNumChildren() <= 0) {
            // args(EMPTY)
            return (getParametersCount(ctx) == 0) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            // check for ".." as first node
            int expressionParameterCount = node.jjtGetNumChildren();// the number of node minus eager one.
            //TODO support several eager nodes
            boolean isFirstArgEager = ((ASTArgParameter) node.jjtGetChild(0)).getTypePattern().isEagerWildCard();
            boolean isLastArgEager = ((ASTArgParameter) node.jjtGetChild(node.jjtGetNumChildren() - 1))
                    .getTypePattern().isEagerWildCard();
            // args(..)
            if (isFirstArgEager && expressionParameterCount == 1) {
                return Boolean.TRUE;
            }
            int contextParametersCount = getParametersCount(ctx);
            if (isFirstArgEager) {
                expressionParameterCount--;
                if (contextParametersCount >= expressionParameterCount) {
                    // do a match from last to first, break when args() nodes are exhausted
                    for (int i = 0; (i < contextParametersCount) && (expressionParameterCount - i >= 0); i++) {
                        ctx.setCurrentTargetArgsIndex(contextParametersCount - 1 - i);
                        if (Boolean.TRUE.equals((Boolean) node.jjtGetChild(expressionParameterCount - i).jjtAccept(
                            this,
                            ctx))) {
                            ;//go on with "next" arg
                        } else {
                            return Boolean.FALSE;
                        }
                    }
                    return Boolean.TRUE;
                } else {
                    //args() as more args than context we try to match
                    return Boolean.FALSE;
                }
            } else if (isLastArgEager) {
                expressionParameterCount--;
                if (contextParametersCount >= expressionParameterCount) {
                    // do a match from first to last, break when args() nodes are exhausted
                    for (int i = 0; (i < contextParametersCount) && (i < expressionParameterCount); i++) {
                        ctx.setCurrentTargetArgsIndex(i);
                        if (Boolean.TRUE.equals((Boolean) node.jjtGetChild(i).jjtAccept(this, ctx))) {
                            ;//go on with next arg
                        } else {
                            return Boolean.FALSE;
                        }
                    }
                    return Boolean.TRUE;
                } else {
                    return Boolean.FALSE;
                }
            } else {
                // no eager wildcard in args()
                // check that args length are equals
                if (expressionParameterCount == contextParametersCount) {
                    for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                        ctx.setCurrentTargetArgsIndex(i);
                        if (Boolean.TRUE.equals((Boolean) node.jjtGetChild(i).jjtAccept(this, ctx))) {
                            ;//go on with next arg
                        } else {
                            return Boolean.FALSE;
                        }
                    }
                    return Boolean.TRUE;
                } else {
                    return Boolean.FALSE;
                }
            }
        }
    }

    public Object visit(ASTArgParameter node, Object data) {
        TypePattern typePattern = node.getTypePattern();
        TypePattern realPattern = typePattern;

        // check if the arg is in the pointcut signature. In such a case, use the declared type
        //TODO can we improve that with a lazy attach of the realTypePattern to the node
        // and a method that always return the real pattern
        // It must be lazy since args are not added at info ctor time [can be refactored..]
        // do some filtering first to avoid unnecessary map lookup
        
        int pointcutArgIndex = -1;
        if (typePattern.getPattern().indexOf(".") < 0) {
            String boundedType = m_expressionInfo.getArgumentType(typePattern.getPattern());
            if (boundedType != null) {
                pointcutArgIndex = m_expressionInfo.getArgumentIndex(typePattern.getPattern());
                realPattern = TypePattern.compileTypePattern(boundedType, SubtypePatternType.NOT_HIERARCHICAL);
            }
        }
        // grab parameter from context
        ExpressionContext ctx = (ExpressionContext) data;
        ClassInfo argInfo = null;
        try {
            if (ctx.getReflectionInfo() instanceof MethodInfo) {
                argInfo = ((MethodInfo) ctx.getReflectionInfo()).getParameterTypes()[ctx.getCurrentTargetArgsIndex()];
            } else if (ctx.getReflectionInfo() instanceof ConstructorInfo) {
                argInfo = ((ConstructorInfo) ctx.getReflectionInfo()).getParameterTypes()[ctx
                        .getCurrentTargetArgsIndex()];
            } else if (ctx.getReflectionInfo() instanceof FieldInfo) {
                argInfo = ((FieldInfo) ctx.getReflectionInfo()).getType();
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // ExpressionContext args are exhausted
            return Boolean.FALSE;
        }
        if (ClassInfoHelper.matchType(realPattern, argInfo)) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTAttribute node, Object data) {
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
        ReflectionInfo refInfo = (ReflectionInfo) data;
        int modifiersToMatch = refInfo.getModifiers();
        int modifierPattern = node.getModifier();
        if (node.isNot()) {
            if ((modifierPattern & Modifier.PUBLIC) != 0) {
                if (((modifiersToMatch & Modifier.PUBLIC) == 0)) {
                    return Boolean.TRUE;
                } else {
                    return Boolean.FALSE;
                }
            } else if ((modifierPattern & Modifier.PROTECTED) != 0) {
                if ((modifiersToMatch & Modifier.PROTECTED) == 0) {
                    return Boolean.TRUE;
                } else {
                    return Boolean.FALSE;
                }
            } else if ((modifierPattern & Modifier.PRIVATE) != 0) {
                if ((modifiersToMatch & Modifier.PRIVATE) == 0) {
                    return Boolean.TRUE;
                } else {
                    return Boolean.FALSE;
                }
            } else if ((modifierPattern & Modifier.STATIC) != 0) {
                if ((modifiersToMatch & Modifier.STATIC) == 0) {
                    return Boolean.TRUE;
                } else {
                    return Boolean.FALSE;
                }
            } else if ((modifierPattern & Modifier.SYNCHRONIZED) != 0) {
                if ((modifiersToMatch & Modifier.SYNCHRONIZED) == 0) {
                    return Boolean.TRUE;
                } else {
                    return Boolean.FALSE;
                }
            } else if ((modifierPattern & Modifier.FINAL) != 0) {
                if ((modifiersToMatch & Modifier.FINAL) == 0) {
                    return Boolean.TRUE;
                } else {
                    return Boolean.FALSE;
                }
            } else if ((modifierPattern & Modifier.TRANSIENT) != 0) {
                if ((modifiersToMatch & Modifier.TRANSIENT) == 0) {
                    return Boolean.TRUE;
                } else {
                    return Boolean.FALSE;
                }
            } else if ((modifierPattern & Modifier.VOLATILE) != 0) {
                if ((modifiersToMatch & Modifier.VOLATILE) == 0) {
                    return Boolean.TRUE;
                } else {
                    return Boolean.FALSE;
                }
            } else if ((modifierPattern & Modifier.STRICT) != 0) {
                if ((modifiersToMatch & Modifier.STRICT) == 0) {
                    return Boolean.TRUE;
                } else {
                    return Boolean.FALSE;
                }
            } else {
                return Boolean.FALSE;
            }
        } else {
            if ((modifierPattern & Modifier.PUBLIC) != 0) {
                if (((modifiersToMatch & Modifier.PUBLIC) == 0)) {
                    return Boolean.FALSE;
                } else {
                    return Boolean.TRUE;
                }
            } else if ((modifierPattern & Modifier.PROTECTED) != 0) {
                if ((modifiersToMatch & Modifier.PROTECTED) == 0) {
                    return Boolean.FALSE;
                } else {
                    return Boolean.TRUE;
                }
            } else if ((modifierPattern & Modifier.PRIVATE) != 0) {
                if ((modifiersToMatch & Modifier.PRIVATE) == 0) {
                    return Boolean.FALSE;
                } else {
                    return Boolean.TRUE;
                }
            } else if ((modifierPattern & Modifier.STATIC) != 0) {
                if ((modifiersToMatch & Modifier.STATIC) == 0) {
                    return Boolean.FALSE;
                } else {
                    return Boolean.TRUE;
                }
            } else if ((modifierPattern & Modifier.SYNCHRONIZED) != 0) {
                if ((modifiersToMatch & Modifier.SYNCHRONIZED) == 0) {
                    return Boolean.FALSE;
                } else {
                    return Boolean.TRUE;
                }
            } else if ((modifierPattern & Modifier.FINAL) != 0) {
                if ((modifiersToMatch & Modifier.FINAL) == 0) {
                    return Boolean.FALSE;
                } else {
                    return Boolean.TRUE;
                }
            } else if ((modifierPattern & Modifier.TRANSIENT) != 0) {
                if ((modifiersToMatch & Modifier.TRANSIENT) == 0) {
                    return Boolean.FALSE;
                } else {
                    return Boolean.TRUE;
                }
            } else if ((modifierPattern & Modifier.VOLATILE) != 0) {
                if ((modifiersToMatch & Modifier.VOLATILE) == 0) {
                    return Boolean.FALSE;
                } else {
                    return Boolean.TRUE;
                }
            } else if ((modifierPattern & Modifier.STRICT) != 0) {
                if ((modifiersToMatch & Modifier.STRICT) == 0) {
                    return Boolean.FALSE;
                } else {
                    return Boolean.TRUE;
                }
            } else {
                return Boolean.TRUE;
            }
        }
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
            // unless there is one single node with the eager wildcard pattern '..' -> parse
            if (parameterNodes.size() > parameterTypes.length) {
                if (parameterNodes.size() == 1) {
                    ASTParameter param = (ASTParameter) parameterNodes.get(0);
                    if (param.getDeclaringClassPattern().isEagerWildCard()) {
                        return true;
                    }
                }
                return false;
            }

            // iterate over the parameter nodes
            int j = 0;
            for (Iterator iterator = parameterNodes.iterator(); iterator.hasNext();) {
                ASTParameter parameter = (ASTParameter) iterator.next();
                if (parameter.getDeclaringClassPattern().isEagerWildCard()) {
                    return true;
                }
                if (Boolean.TRUE.equals(parameter.jjtAccept(this, parameterTypes[j++]))) {
                    continue;
                } else {
                    return false;
                }
            }
        } else if (parameterTypes.length != 0) { // no nodes but parameters to parse
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

    /**
     * Returns the number of parameters to the target method/constructor else -1.
     *
     * @param ctx
     * @return
     */
    private int getParametersCount(final ExpressionContext ctx) {
        ReflectionInfo reflectionInfo = ctx.getReflectionInfo();
        if (reflectionInfo instanceof MethodInfo) {
            return ((MethodInfo) reflectionInfo).getParameterTypes().length;
        } else if (reflectionInfo instanceof ConstructorInfo) {
            return ((ConstructorInfo) reflectionInfo).getParameterTypes().length;
        } else if (reflectionInfo instanceof FieldInfo) {
            return 1;//field set support for args()
        } else {
            return -1;
        }
    }
}