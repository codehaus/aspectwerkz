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
import java.io.ObjectOutputStream;
import java.lang.ref.SoftReference;

import org.codehaus.aspectwerkz.pointcut.FieldPointcut;

/**
 * Matches well defined point of execution in the program where a field is set.
 * Stores meta data from the join point. I.e. a reference to original object
 * A method etc. Handles the invocation of the advices added to the join point.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class MemberFieldSetJoinPoint extends FieldJoinPoint {

    /**
     * A soft reference to the target object.
     */
    protected SoftReference m_targetObjectReference;

    /**
     * The serial version uid for the class.
     */
    private static final long serialVersionUID = 3055780985379512486L;

    /**
     * Creates a new MemberFieldSetJoinPoint object.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param targetObject the target object
     * @param signature the field signature
     */
    public MemberFieldSetJoinPoint(final String uuid,
                                   final Object targetObject,
                                   final String signature) {
        super(uuid, signature);
        if (targetObject == null) throw new IllegalArgumentException("target object can not be null");
        m_targetObjectReference = new SoftReference(targetObject);
        createMetaData();
    }

    /**
     * Returns the target object.
     *
     * @return the target object
     */
    public Object getTargetObject() {
        return m_targetObjectReference.get();
    }

    /**
     * Returns the target class.
     *
     * @return the target class
     */
    public Class getTargetClass() {
        return m_targetObjectReference.get().getClass();
    }

    /**
     * Loads the advices for this pointcut.
     */
    protected void loadAdvices() {
        synchronized (m_preAdvices) {
            synchronized (m_postAdvices) {

                List preAdvices = new ArrayList();
                List postAdvices = new ArrayList();

                List pointcuts = m_system.getSetFieldPointcuts(m_classMetaData, m_fieldMetaData);

                for (Iterator it = pointcuts.iterator(); it.hasNext();) {
                    FieldPointcut fieldPointcut = (FieldPointcut)it.next();

                    int[] preAdviceIndexes = fieldPointcut.getPreAdviceIndexes();
                    for (int j = 0; j < preAdviceIndexes.length; j++) {
                        preAdvices.add(new Integer(preAdviceIndexes[j]));
                    }

                    int[] postAdviceIndexes = fieldPointcut.getPostAdviceIndexes();
                    for (int j = 0; j < postAdviceIndexes.length; j++) {
                        postAdvices.add(new Integer(postAdviceIndexes[j]));
                    }
                }

                m_preAdvices = new int[preAdvices.size()];
                int i = 0;
                for (Iterator it = preAdvices.iterator(); it.hasNext(); i++) {
                    m_preAdvices[i] = ((Integer)it.next()).intValue();
                }
                m_postAdvices = new int[postAdvices.size()];
                i = 0;
                for (Iterator it = postAdvices.iterator(); it.hasNext(); i++) {
                    m_postAdvices[i] = ((Integer)it.next()).intValue();
                }

                m_initialized = true;
            }
        }
    }

    /**
     * Provides custom serialization.
     *
     * @param stream the object output stream that should write the serialized object
     * @throws java.lang.Exception in case of failure
     */
    private void writeObject(final ObjectOutputStream stream) throws Exception {
        ObjectOutputStream.PutField fields = stream.putFields();
        fields.put("m_targetObjectReference", m_targetObjectReference.get());
        stream.writeFields();
    }

    /**
     * Provides custom deserialization.
     *
     * @param stream the object input stream containing the serialized object
     * @throws java.lang.Exception in case of failure
     */
    private void readObject(final ObjectInputStream stream) throws Exception {
        ObjectInputStream.GetField fields = stream.readFields();
        m_targetObjectReference = new SoftReference(fields.get("m_targetObjectReference", null));
    }
}
