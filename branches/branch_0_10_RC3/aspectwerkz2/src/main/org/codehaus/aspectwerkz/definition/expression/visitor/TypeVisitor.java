/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.expression.visitor;

import org.codehaus.aspectwerkz.definition.expression.Expression;
import org.codehaus.aspectwerkz.definition.expression.ExpressionNamespace;
import org.codehaus.aspectwerkz.definition.expression.PointcutType;
import org.codehaus.aspectwerkz.definition.expression.ast.AndNode;
import org.codehaus.aspectwerkz.definition.expression.ast.BooleanLiteral;
import org.codehaus.aspectwerkz.definition.expression.ast.ExpressionParserVisitor;
import org.codehaus.aspectwerkz.definition.expression.ast.ExpressionScript;
import org.codehaus.aspectwerkz.definition.expression.ast.FalseNode;
import org.codehaus.aspectwerkz.definition.expression.ast.Identifier;
import org.codehaus.aspectwerkz.definition.expression.ast.NotNode;
import org.codehaus.aspectwerkz.definition.expression.ast.OrNode;
import org.codehaus.aspectwerkz.definition.expression.ast.SimpleNode;
import org.codehaus.aspectwerkz.definition.expression.ast.TrueNode;
import org.codehaus.aspectwerkz.definition.expression.ast.Anonymous;
import org.codehaus.aspectwerkz.exception.DefinitionException;

import java.util.Set;
import java.util.Collections;
import java.util.Iterator;
import java.util.HashSet;

/**
 * Determine expression type<br/>
 * Visit' data is TypeVisitorContext
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class TypeVisitor implements ExpressionParserVisitor {

    public Object visit(SimpleNode node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    public Object visit(ExpressionScript node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    public Object visit(OrNode node, Object data) {
        Set leftTypes = getLeftHS(node, data);
        Set rightTypes = getRightHS(node, data);
        // merge types
        leftTypes.addAll(rightTypes);
        return leftTypes;
    }

//    public Object visit(InNode node, Object data) {
//        // assert RHS is of CFLOW type
//        // note: anonymous type like "IN true" is assumed valid
//        Set rhs = getRightHS(node, data);
//        if ( ! rhs.contains(PointcutType.CFLOW)) {
//            throw new RuntimeException("IN type not valid");
//        }
//        // resulting type is unchanged
//        return getLeftHS(node, data);
//    }
//
//    public Object visit(NotInNode node, Object data) {
//        // assert RHS is of CFLOW type
//        // note: anonymous type like "IN true" is assumed valid
//        Set rhs = getRightHS(node, data);
//        if ( ! rhs.contains(PointcutType.CFLOW)) {
//            throw new RuntimeException("NOT IN type not valid");
//        }
//        // resulting type is unchanged
//        return getLeftHS(node, data);
//    }

    public Object visit(AndNode node, Object data) {
        Set leftTypes = (Set)getLeftHS(node, data);
        Set rightTypes = (Set)getRightHS(node, data);

        if (PointcutType.isCflowTypeOnly(rightTypes)) {
            leftTypes.add(PointcutType.CFLOW);
            return leftTypes;
        } else if (PointcutType.isCflowTypeOnly(leftTypes)) {
            rightTypes.add(PointcutType.CFLOW);
            return rightTypes;
        }

        // build the intersection since there is TYPED_1 AND TYPED_2
        // where nor TYPED_1 neither TYPED_2 are pure cflow expression
        Set intersect = new HashSet();
        for (Iterator types = rightTypes.iterator(); types.hasNext();) {
            Object type = types.next();
            if (leftTypes.contains(type)) {
                intersect.add(type);
            }
        }
        if (intersect.isEmpty()) {
            throw new RuntimeException("AND types do not intersect");
        }
        return intersect;
    }

    public Object visit(NotNode node, Object data) {
        return getLeftHS(node, data);
    }

    public Object visit(Identifier node, Object data) {
        ExpressionNamespace space = (ExpressionNamespace)data;
        Expression expression = space.getExpression(node.name);
        if (expression != null) {
            //((TypeVisitorContext)data).addTypes(expression.getType());
            //return expression.getType();
            //TODO AVO allow nested lookup of type ??
            Set set = new HashSet();
            set.addAll(expression.getTypes());
            return set;
        }
        else {
            throw new DefinitionException("No such registered expression: " + node.name + " in " + space.getNamespaceKey());
        }
    }

    public Object visit(BooleanLiteral node, Object data) {
        return getLeftHS(node, data);
    }

    public Object visit(TrueNode node, Object data) {
        return new HashSet();//TODO
    }

    public Object visit(FalseNode node, Object data) {
        return new HashSet();//TODO
    }

    public Object visit(Anonymous node, Object data) {
        String expr = node.name;
        Set type = new HashSet();
        if (expr.startsWith("execution(")) {
            type.add(PointcutType.EXECUTION);
        } else if (expr.startsWith("call(")) {
            type.add(PointcutType.CALL);
        } else if (expr.startsWith("cflow(")) {
            type.add(PointcutType.CFLOW);
        } else if (expr.startsWith("set(")) {
            type.add(PointcutType.SET);
        } else if (expr.startsWith("get(")) {
            type.add(PointcutType.GET);
        } else if (expr.startsWith("class(")) {
            type.add(PointcutType.CLASS);
        } else if (expr.startsWith("handler(")) {
            type.add(PointcutType.HANDLER);
        } else if (expr.startsWith("attribute(")) {
            //type.add(PointcutType.ATTRIBUTE);//TODO AV17 not needed ? attribute crosscuts type
            type.add(PointcutType.CALL);
            type.add(PointcutType.EXECUTION);
            //TODO AV17: attr(..) will lead to expensive CALL pc lookup !?
        }
        return type;
    }

    //------------------------


    private Set getLeftHS(SimpleNode node, Object data) {
        return (Set)node.jjtGetChild(0).jjtAccept(this, data);
    }

    private Set getRightHS(SimpleNode node, Object data) {
        return (Set)node.jjtGetChild(1).jjtAccept(this, data);
    }

//    private PointcutType getResultingType(SimpleNode node, ExpressionParserVisitor visitor, Object data) {
//        PointcutType lhs = getLeftHS(node, this, data);
//        PointcutType rhs = getRightHS(node, this, data);
//
//        if (node.jjtGetChild(0) instanceof BooleanLiteral) {
//            // ignore lhs literal
//            return rhs;
//        }
//        else if (node.jjtGetChild(1) instanceof BooleanLiteral) {
//            // ignore rhs literal
//            return lhs;
//        }
//        else {
//            if (rhs != null && rhs.equals(lhs)) {
//                return rhs;
//            }
//            else {
//                return null;
//            }
//        }
//    }
}
