/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.expression;

import java.io.ObjectInputStream;
import java.util.Iterator;

import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.MemberMetaData;
import org.codehaus.aspectwerkz.definition.attribute.CustomAttribute;

/**
 * Attribute leaf expression
 *
 * TODO not used / experimental
 * TODO does not handles NullMetaData
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AttributeExpression extends LeafExpression {

    /**
     * Matches the leaf-node pattern.
     *
     * @param classMetaData  the class meta-data
     * @param memberMetaData the meta-data for the member
     * @return boolean
     */
    public boolean match(final ClassMetaData classMetaData, final MemberMetaData memberMetaData) {
        // looks in classMetaData first
        for (Iterator attrs = classMetaData.getAttributes().iterator(); attrs.hasNext();) {
            if (((CustomAttribute)attrs.next()).getName().equals(m_expression)) {
                return true;
            }
        }
        // looks in memberMetaData
        if (memberMetaData != null) {
            for (Iterator attrs = memberMetaData.getAttributes().iterator(); attrs.hasNext();) {
                if (((CustomAttribute)attrs.next()).getName().equals(m_expression)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Provides custom deserialization.
     *
     * @param stream the object input stream containing the serialized object
     * @throws java.lang.Exception in case of failure
     * @todo implement
     */
    private void readObject(final ObjectInputStream stream) throws Exception {
        throw new UnsupportedOperationException("implement AttributeExpression.readObject()");
    }

    /**
     * Creates a new expression.
     *
     * @param namespace    the namespace for the expression
     * @param expression   the expression as a string
     * @param pointcutName the name of the pointcut
     */
    AttributeExpression(
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
    AttributeExpression(
            final ExpressionNamespace namespace,
            final String expression,
            final String packageNamespace,
            final String pointcutName) {
        super(namespace, expression, packageNamespace, pointcutName, PointcutType.ATTRIBUTE);
    }

}
