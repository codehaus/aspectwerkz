/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.expression;

import java.io.ObjectInputStream;

import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.MemberMetaData;

/**
 * @todo document
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AttributeExpression extends Expression {

    /**
     * Matches the leaf-node pattern.
     *
     * @todo should AttributeMetaData be created and be a subclass of MemberMetaData, or should we pass in CMT, MMD and AttributeMetaData to know at which member the attribute is defined?
     *
     * @param classMetaData the class meta-data
     * @param memberMetaData the meta-data for the member
     * @return boolean
     */
    public boolean matchPattern(final ClassMetaData classMetaData, final MemberMetaData memberMetaData) {
        return false;
    }

    /**
     * Provides custom deserialization.
     *
     * @todo implement
     *
     * @param stream the object input stream containing the serialized object
     * @throws java.lang.Exception in case of failure
     */
    private void readObject(final ObjectInputStream stream) throws Exception {
        ObjectInputStream.GetField fields = stream.readFields();

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
     * @param namespace the namespace for the expression
     * @param expression the expression as a string
     * @param pointcutName the name of the pointcut
     */
    AttributeExpression(final String namespace,
                        final String expression,
                        final String pointcutName) {
        super(namespace, expression, pointcutName, PointcutType.ATTRIBUTE);
    }
}
