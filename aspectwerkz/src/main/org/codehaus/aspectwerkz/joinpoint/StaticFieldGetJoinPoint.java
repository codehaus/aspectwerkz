/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.ObjectInputStream;

import org.codehaus.aspectwerkz.pointcut.GetPointcut;
import org.codehaus.aspectwerkz.IndexTuple;

/**
 * Matches well defined point of execution in the program where a field is
 * accessed. Stores meta data from the join point. I.e. a reference to original
 * object A method etc. Handles the invocation of the advices added to the
 * join point.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class StaticFieldGetJoinPoint extends FieldJoinPoint {

    /**
     * A reference to the target class.
     */
    protected Class m_targetClass;

    /**
     * The serial version uid for the class.
     */
    private static final long serialVersionUID = 1790390896888275229L;

    /**
     * Creates a new StaticFieldGetJoinPoint object.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param targetClass the target class
     * @param signature the field signature
     */
    public StaticFieldGetJoinPoint(final String uuid,
                                   final Class targetClass,
                                   final String signature) {
        super(uuid, signature);
        if (targetClass == null) throw new IllegalArgumentException("target class can not be null");
        m_targetClass = targetClass;
        createMetaData();
    }

    /**
     * Returns the target object.
     *
     * @return the target object
     */
    public Object getTargetInstance() {
        return null;
    }

    /**
     * Returns the target class.
     *
     * @return the target class
     */
    public Class getTargetClass() {
        return m_targetClass;
    }

    /**
     * Loads the advices for this pointcut.
     */
    protected void loadAdvices() {
        synchronized (m_preAdvices) {
            synchronized (m_postAdvices) {
                List preAdvices = new ArrayList();
                List postAdvices = new ArrayList();

                List pointcuts = m_system.getGetPointcuts(m_classMetaData, m_fieldMetaData);

                for (Iterator it = pointcuts.iterator(); it.hasNext();) {
                    GetPointcut fieldPointcut = (GetPointcut)it.next();

                    IndexTuple[] preAdviceIndexes = fieldPointcut.getPreAdviceIndexes();
                    for (int j = 0; j < preAdviceIndexes.length; j++) {
                        preAdvices.add(preAdviceIndexes[j]);
                    }

                    IndexTuple[] postAdviceIndexes = fieldPointcut.getPostAdviceIndexes();
                    for (int j = 0; j < postAdviceIndexes.length; j++) {
                        postAdvices.add(postAdviceIndexes[j]);
                    }
                }

                m_preAdvices = new IndexTuple[preAdvices.size()];
                int i = 0;
                for (Iterator it = preAdvices.iterator(); it.hasNext(); i++) {
                    m_preAdvices[i] = (IndexTuple)it.next();
                }
                m_postAdvices = new IndexTuple[postAdvices.size()];
                i = 0;
                for (Iterator it = postAdvices.iterator(); it.hasNext(); i++) {
                    m_postAdvices[i] = (IndexTuple)it.next();
                }

                m_initialized = true;
            }
        }
    }

    /**
     * Provides custom deserialization.
     *
     * @param stream the object input stream containing the serialized object
     * @throws java.lang.Exception in case of failure
     */
    private void readObject(final ObjectInputStream stream) throws Exception {
        ObjectInputStream.GetField fields = stream.readFields();
        m_targetClass = (Class)fields.get("m_targetClass", null);
    }
}
