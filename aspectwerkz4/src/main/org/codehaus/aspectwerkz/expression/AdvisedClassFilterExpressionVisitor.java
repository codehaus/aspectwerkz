/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
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
import org.codehaus.aspectwerkz.reflect.StaticInitializationInfo;
import org.codehaus.aspectwerkz.util.Util;
import org.codehaus.aspectwerkz.annotation.AnnotationInfo;
import org.codehaus.backport175.reader.bytecode.AnnotationElement;

import java.util.List;
import java.util.Iterator;

/**
 * The advised class filter visitor.
 * <p/>
 * Visit() methods are returning Boolean.TRUE/FALSE or null when decision cannot be taken.
 * Using null allow composition of OR/AND with NOT in the best way.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 * @author Michael Nascimento
 * @author <a href="mailto:the_mindstorm@evolva.ro">Alex Popescu</a>
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
                                               final String namespace, final Node root) {
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

        // only the last node might be the pattern, others are annotations
        Node patternNode = node.jjtGetChild(node.jjtGetNumChildren() - 1);
        boolean checkPattern = !(patternNode instanceof ASTAttribute);
        
        if(checkPattern) {
            if (context.hasWithinPointcut() || context.hasExecutionPointcut()) {
                if (context.hasExecutionPointcut()) {
                    // reflectionInfo was given
                    return patternNode.jjtAccept(this, context.getReflectionInfo());
                } else {
                    // only withinInfo was given
                    return patternNode.jjtAccept(this, context.getWithinReflectionInfo()); 
                }
            } else {
                return Boolean.FALSE;
            }
        } else {
        	return null;
        }
    }

    public Object visit(ASTCall node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        
        // only the last node might be the pattern, others are annotations
        Node patternNode = node.jjtGetChild(node.jjtGetNumChildren() - 1);
        boolean checkPattern = !(patternNode instanceof ASTAttribute);
        
        if(checkPattern) {
            if (context.hasWithinPointcut() || context.hasCallPointcut()) {
                if (context.hasReflectionInfo()) {
                        return patternNode.jjtAccept(this, context.getReflectionInfo());
                } else {
                    return null;
                }
            } else {
                return Boolean.FALSE;
            }
        } else {
        	return null;
        }
    }

    public Object visit(ASTSet node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        
        // only the last node might be the pattern, others are annotations
        Node patternNode = node.jjtGetChild(node.jjtGetNumChildren() - 1);
        boolean checkPattern = !(patternNode instanceof ASTAttribute);
        
        // for set evaluation, the reflection info may be null at the early matching phase
        // when we will allow for field interception within non declaring class
        if(checkPattern) {
            if (context.hasWithinPointcut() || context.hasSetPointcut()) {
                if (context.hasReflectionInfo()) {
                        return patternNode.jjtAccept(this, context.getReflectionInfo());
                } else {
                    return null;
                }
            } else {
                return Boolean.FALSE;
            }
        } else {
        	return null;
        }
    }

    public Object visit(ASTGet node, Object data) {
        ExpressionContext context = (ExpressionContext) data;

        // only the last node might be the pattern, others are annotations
        Node patternNode = node.jjtGetChild(node.jjtGetNumChildren() - 1);
        boolean checkPattern = !(patternNode instanceof ASTAttribute);
        
        // for get evaluation, the reflection info may be null at the early matching phase
        // since we allow for field interception within non declaring class
        if(checkPattern) {
            if (context.hasWithinPointcut() || context.hasGetPointcut()) {
                if (context.hasReflectionInfo()) {
                	return patternNode.jjtAccept(this, context.getReflectionInfo());
                } else {
                    return null;
                }
            } else {
                return Boolean.FALSE;
            }
        } else {
        	return null;
        }
    }

    public Object visit(ASTHandler node, Object data) {
        return null;
    }

    public Object visit(ASTStaticInitialization node, Object data) {
        ExpressionContext context = (ExpressionContext) data;

        if (context.hasStaticInitializationPointcut() && context.hasWithinReflectionInfo()) {
        	ReflectionInfo reflectInfo = context.getWithinReflectionInfo();
            if (reflectInfo instanceof StaticInitializationInfo) {
                reflectInfo = ((StaticInitializationInfo) reflectInfo).getDeclaringType();
            }
        	if (reflectInfo instanceof ClassInfo) {
                // In an annotated subtree, the last child node represents the pattern
                Node patternNode = node.jjtGetChild(node.jjtGetNumChildren() - 1);
                if (!(patternNode instanceof ASTAttribute)) {
                    Boolean matchPattern = (Boolean) patternNode.jjtAccept(this, reflectInfo);
                    if (Boolean.FALSE.equals(matchPattern)) {
                        return Boolean.FALSE;
                    }
                }

                // match on the annotations since the pattern was not there or matched
            	boolean matchedAnnotations = visitAttributes(node, reflectInfo);
            	if (!matchedAnnotations) {
            		return Boolean.FALSE;
            	} else {
                    return null;//match but early phase
                }
        	} else {
        		return Boolean.FALSE;
        	}
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTWithinCode node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        ReflectionInfo withinInfo = context.getWithinReflectionInfo();

        if (node.isStaticInitializer()) {
            // transform the node in a within node to do an exact match on the within info
            //TODO would be worth to do the fastNode creation only once somewhere
            ASTWithin fastNode = new ASTWithin(0);
            for (int i = 0; i < node.jjtGetChild(0).jjtGetNumChildren(); i++) {
                  fastNode.jjtAddChild(node.jjtGetChild(0).jjtGetChild(i), i);
            }
            return super.visit(fastNode, data);
        } else {
	        Node patternNode = node.jjtGetChild(node.jjtGetNumChildren() - 1);
	        boolean checkPattern = !(patternNode instanceof ASTAttribute);
	
	        if (checkPattern) {
	            if (withinInfo instanceof MemberInfo) {
	                return patternNode.jjtAccept(this, withinInfo);
	            } else if (withinInfo instanceof ClassInfo) {
	                Boolean matchDeclaringType = (Boolean) patternNode.jjtAccept(this, withinInfo);
	                if (Boolean.FALSE.equals(matchDeclaringType)) {
	                    return Boolean.FALSE;
	                } else {
	                    // may be we match now but not later?
	                    return null;
	                }
	            } else {
	                return null;
	            }
	        } else {
	            return null;
	        }
        }
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
//    public Object visit(ASTClassPattern node, Object data) {
//        if(null == data) {
//            return null;
//        } else if( !(data instanceof ClassInfo) ) {
//        	return Boolean.FALSE;
//        }
//
//        ClassInfo classInfo = (ClassInfo) data;
//        if (node.getTypePattern().matchType(classInfo) && visitAttributes(node, classInfo)) {
//            return Boolean.TRUE;
//        } else {
//            return Boolean.FALSE;
//        }
//    }

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
        boolean matchAnnotation = false;
        AnnotationElement.Annotation[] annotations = (AnnotationElement.Annotation[]) data;
        for (int i = 0; i < annotations.length; i++) {
            AnnotationElement.Annotation annotation = annotations[i];
            if (annotation.getInterfaceName().equals(node.getName())) {
                matchAnnotation = true;
            }
        }
        if (node.isNot()) {
            return Util.booleanValueOf(!matchAnnotation);
        } else {
            return Util.booleanValueOf(matchAnnotation);
        }
    }

    public Object visit(ASTModifier node, Object data) {
        // TODO
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
}
