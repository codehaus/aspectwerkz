/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.expression;


/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @todo document
 * <p/>
 * TO BE REMOVED
 */
public class RootExpression /*extends Expression*/ {
//
//    /**
//     * Matches the leaf-node pattern.
//     *
//     * @param classMetaData the class meta-data
//     * @param memberMetaData the meta-data for the member
//     * @return boolean
//     */
//    protected boolean matchPattern(final ClassMetaData classMetaData, final MemberMetaData memberMetaData) {
//        // first match the class
//        if (!match(classMetaData)) {
//            return false;
//        }
//        // second match the member
//        if (m_type.equals(PointcutType.EXECUTION)) {
//            return ((MethodPattern)m_memberPattern).matches((MethodMetaData)memberMetaData);
//        }
//        else if (m_type.equals(PointcutType.CALL)) {
//            return ((CallerSidePattern)m_memberPattern).matches((MethodMetaData)memberMetaData);
//        }
//        else if (m_type.equals(PointcutType.GET) || m_type.equals(PointcutType.SET)) {
//            return ((FieldPattern)m_memberPattern).matches((FieldMetaData)memberMetaData);
//        }
//        else if (m_type.equals(PointcutType.CFLOW)) {
//            throw new UnsupportedOperationException("cflow pointcuts are not implemented yet, go ahead and do it");
//        }
//        else if (m_type.equals(PointcutType.THROWS)) {
//            return ((ThrowsPattern)m_memberPattern).matches((MethodMetaData)memberMetaData);
//        }
//        else if (m_type.equals(PointcutType.CLASS)) {
//            // TODO: is it correct to match the membermetadata as well at this point for the class pointcut?
//            return ((ClassPattern)m_memberPattern).matches(((ClassMetaData)memberMetaData).getName());
//        }
//        else {
//            throw new ExpressionException("pointcut type not supported: " + m_type);
//        }
//    }
//
//    /**
//     * Provides custom deserialization.
//     *
//     * @todo implement
//     *
//     * @param stream the object input stream containing the serialized object
//     * @throws java.lang.Exception in case of failure
//     */
//    private void readObject(final ObjectInputStream stream) throws Exception {
//        ObjectInputStream.GetField fields = stream.readFields();
//
////        m_expression = (String)fields.get("m_expression", null);
////        m_cflowExpression = (String)fields.get("m_cflowExpression", null);
////        m_pointcutRefs = (List)fields.get("m_pointcutRefs", null);
////        m_methodPointcutPatterns = (Map)fields.get("m_methodPointcutPatterns", null);
////        m_setFieldPointcutPatterns = (Map)fields.get("m_setFieldPointcutPatterns", null);
////        m_getFieldPointcutPatterns = (Map)fields.get("m_getFieldPointcutPatterns", null);
////        m_throwsPointcutPatterns = (Map)fields.get("m_throwsPointcutPatterns", null);
////        m_callerSidePointcutPatterns = (Map)fields.get("m_callerSidePointcutPatterns", null);
////
////        createJexlExpression();
////        createJexlCFlowExpression();
//    }
//
//    /**
//     * Creates a new expression.
//     *
//     * @param namespace the namespace for the expression
//     * @param expression the expression as a string
//     */
//    RootExpression(final String namespace, final String expression) {
//        super(namespace, expression, null, null);
//    }
//
//    /**
//     * Creates a new expression.
//     *
//     * @param namespace the namespace for the expression
//     * @param expression the expression as a string
//     * @param type the expression type
//     */
//    RootExpression(final String namespace, final String expression, final PointcutType type) {
//        super(namespace, expression, null, type);
//    }
}
