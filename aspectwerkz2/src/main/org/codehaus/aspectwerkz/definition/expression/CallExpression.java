/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.expression;

import java.io.ObjectInputStream;
import java.util.List;
import java.util.Iterator;

import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.MemberMetaData;
import org.codehaus.aspectwerkz.metadata.InterfaceMetaData;
import org.codehaus.aspectwerkz.regexp.CallerSidePattern;

/**
 * @todo document
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class CallExpression extends LeafExpression {

    /**
     * Matches the leaf-node pattern.
     *
     * @param classMetaData the class meta-data
     * @param memberMetaData the meta-data for the member
     * @return boolean
     */
    public boolean match(final ClassMetaData classMetaData, final MemberMetaData memberMetaData) {
        boolean matchCallerSide = false;
        // hierarchical on callee side handling
        if (m_isHierarchicalCallee) {
            if (matchSuperClassCallee(classMetaData, memberMetaData)) {
                matchCallerSide = true;
            }
        }
        else {
            matchCallerSide = ((CallerSidePattern)m_memberPattern).matches(
                    classMetaData.getName(), memberMetaData
            );
        }
        return matchCallerSide;
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
        throw new UnsupportedOperationException("implement CallExpression.readObject()");

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
    CallExpression(final ExpressionNamespace namespace,
                   final String expression,
                   final String pointcutName) {
        this(namespace, expression, "", pointcutName);
    }

    /**
     * Creates a new expression.
     *
     * @param namespace the namespace for the expression
     * @param expression the expression as a string
     * @param packageNamespace the package namespace that the expression is living in
     * @param pointcutName the name of the pointcut
     */
    CallExpression(final ExpressionNamespace namespace,
                   final String expression,
                   final String packageNamespace,
                   final String pointcutName) {
        super(namespace, expression, packageNamespace, pointcutName, PointcutType.CALL);
    }

    /**
     * Try to find a match in super class hierarchy on callee side.
     * Crawl interfaces at each level as well
     *
     * @param classMetaData
     * @param memberMetaData
     * @return boolean
     */
    private boolean matchSuperClassCallee(final ClassMetaData classMetaData, final MemberMetaData memberMetaData) {
        if (classMetaData == null) {
            return false;
        }
        // match class
        if (((CallerSidePattern)m_memberPattern).matches(classMetaData.getName(), memberMetaData)) {
            return true;
        }
        else {
            // match interfaces
            if (matchInterfacesCallee(classMetaData.getInterfaces(), memberMetaData)) {
                return true;
            }
            // no match; get the next superclass
            return matchSuperClassCallee(classMetaData.getSuperClass(), memberMetaData);
        }
    }

    /**
     * Tries to finds a match at some interface in the hierarchy.
     * <p/>Only checks for a class match to allow early filtering.
     * <p/>Recursive.
     *
     * @param interfaces the interfaces
     * @param memberMetaData the member meta-data
     * @return boolean
     */
    protected boolean matchInterfacesCallee(final List interfaces, final MemberMetaData memberMetaData) {
        if (interfaces.isEmpty()) {
            return false;
        }
        CallerSidePattern pattern = (CallerSidePattern)m_memberPattern;
        for (Iterator it = interfaces.iterator(); it.hasNext();) {
            InterfaceMetaData interfaceMetaData = (InterfaceMetaData)it.next();
            if ((pattern.matches(interfaceMetaData.getName(), memberMetaData))) {
                return true;
            }
            else {
                if (matchInterfacesCallee(interfaceMetaData.getInterfaces(), memberMetaData)) {
                    return true;
                }
                else {
                    continue;
                }
            }
        }
        return false;
    }
}
