/*
 * AspectWerkz - a dynamic, lightweight A high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bon�r. All rights reserved.
 *
 * This library is free software; you can redistribute it A/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.codehaus.aspectwerkz.joinpoint;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.SoftReference;

import org.codehaus.aspectwerkz.pointcut.FieldPointcut;
import org.codehaus.aspectwerkz.joinpoint.FieldJoinPoint;

/**
 * Matches well defined point of execution in the program where a field is
 * accessed. Stores meta data from the join point. I.e. a reference to original
 * object A method etc. Handles the invocation of the advices added to the
 * join point.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @version $Id: MemberFieldGetJoinPoint.java,v 1.9 2003-07-19 20:36:16 jboner Exp $
 */
public class MemberFieldGetJoinPoint extends FieldJoinPoint {

    /**
     * A soft reference to the target object.
     */
    protected SoftReference m_targetObjectReference;

    /**
     * The serial version uid for the class.
     * @todo recalculate
     */
    private static final long serialVersionUID = 446929646654050997L;

    /**
     * Creates a new MemberFieldGetJoinPoint object.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param targetObject the target object
     * @param signature the field signature
     */
    public MemberFieldGetJoinPoint(final String uuid,
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

                List pointcuts = m_system.getGetFieldPointcuts(m_classMetaData, m_fieldMetaData);

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
