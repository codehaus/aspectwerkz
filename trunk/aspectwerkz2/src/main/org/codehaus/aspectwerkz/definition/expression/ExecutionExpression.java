/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.expression;

import java.io.ObjectInputStream;

import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.ConstructorMetaData;
import org.codehaus.aspectwerkz.metadata.MemberMetaData;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.regexp.ConstructorPattern;
import org.codehaus.aspectwerkz.regexp.MethodPattern;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @todo document
 */
public class ExecutionExpression extends LeafExpression {

    /**
     * Matches the leaf-node pattern.
     *
     * @param classMetaData  the class meta-data
     * @param memberMetaData the meta-data for the member
     * @return boolean
     */
    public boolean match(final ClassMetaData classMetaData, final MemberMetaData memberMetaData) {
        if (!match(classMetaData)) {
            return false;
        }
        if (memberMetaData instanceof MethodMetaData && m_memberPattern instanceof MethodPattern) {
            return ((MethodPattern)m_memberPattern).matches((MethodMetaData)memberMetaData);
        }
        else if (memberMetaData instanceof ConstructorMetaData && m_memberPattern instanceof ConstructorPattern) {
            return ((ConstructorPattern)m_memberPattern).matches((ConstructorMetaData)memberMetaData);
        }
        else {
            return false;
        }
    }

    /**
     * Provides custom deserialization.
     *
     * @param stream the object input stream containing the serialized object
     * @throws java.lang.Exception in case of failure
     * @todo implement
     */
    private void readObject(final ObjectInputStream stream) throws Exception {
        ObjectInputStream.GetField fields = stream.readFields();
        throw new UnsupportedOperationException("implement ExecutionExpression.readObject()");

//        m_expression = (String)fields.get("m_expression", null);
//        m_cflowExpression = (String)fields.get("m_cflowExpression", null);
//        m_pointcutRefs = (List)fields.get("m_pointcutRefs", null);
//        m_methodPointcutPatterns = (Map)fields.get("m_methodPointcutPatterns", null);
//        m_setFieldPointcutPatterns = (Map)fields.get("m_setFieldPointcutPatterns", null);
//        m_getFieldPointcutPatterns = (Map)fields.get("m_getFieldPointcutPatterns", null);
//        m_throwsPointcutPatterns = (Map)fields.get("m_throwsPointcutPatterns", null);
//        m_callerSidePointcutPatterns = (Map)fields.get("m_callerSidePointcutPatterns", null);
//
//        createJexlExpression();
//        createJexlCFlowExpression();
    }

    /**
     * Creates a new expression.
     *
     * @param namespace    the namespace for the expression
     * @param expression   the expression as a string
     * @param pointcutName the name of the pointcut
     */
    ExecutionExpression(
            final ExpressionNamespace namespace,
            final String expression,
            final String pointcutName) {
        this(namespace, expression, "", pointcutName);

    }

    /**
     * Creates a new expression.
     *
     * @param namespace        the namespace for the expression
     * @param expression       the expression as a string
     * @param packageNamespace the package namespace that the expression is living in
     * @param pointcutName     the name of the pointcut
     */
    ExecutionExpression(
            final ExpressionNamespace namespace,
            final String expression,
            final String packageNamespace,
            final String pointcutName) {
        super(namespace, expression, packageNamespace, pointcutName, PointcutType.EXECUTION);
    }
}
