/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
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

import org.codehaus.aspectwerkz.Aspect;
import org.codehaus.aspectwerkz.Type;
import org.codehaus.aspectwerkz.AspectWerkz;
import org.codehaus.aspectwerkz.definition.metadata.FieldMetaData;
import org.codehaus.aspectwerkz.pointcut.FieldPointcut;
import org.codehaus.aspectwerkz.joinpoint.FieldJoinPoint;

/**
 * Matches well defined point of execution in the program where a field is
 * accessed. Stores meta data from the join point. I.e. a reference to original
 * object and method etc. Handles the invocation of the advices added to the
 * join point.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: MemberFieldGetJoinPoint.java,v 1.3 2003-06-09 08:24:49 jboner Exp $
 */
public class MemberFieldGetJoinPoint extends FieldJoinPoint {

    /**
     * A reference to the target object.
     */
    protected Object m_targetObject;

    /**
     * The serial version uid for the class.
     * @todo recalculate
     */
    private static final long serialVersionUID = -8040662762496348595L;

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
        m_targetObject = targetObject;
    }

    /**
     * Returns the target object.
     *
     * @return the target object
     */
    public Object getTargetObject() {
        return m_targetObject;
    }

    /**
     * Returns the target class.
     *
     * @return the target class
     */
    public Class getTargetClass() {
        return m_targetObject.getClass();
    }

    /**
     * Loads the advices for this pointcut.
     */
    protected void loadAdvices() {
        synchronized (m_preAdvices) {
            synchronized (m_postAdvices) {

                List preAdvices = new ArrayList();
                List postAdvices = new ArrayList();
                List aspects = m_system.getAspects(getTargetClass().getName());

                for (Iterator it = aspects.iterator(); it.hasNext();) {
                    Aspect aspect = (Aspect)it.next();

                    FieldPointcut[] pointcuts =
                            aspect.getGetFieldPointcuts(m_metadata);

                    for (int i = 0; i < pointcuts.length; i++) {
                        int[] advices = pointcuts[i].getPreAdviceIndexes();
                        for (int j = 0; j < advices.length; j++) {
                            preAdvices.add(new Integer(advices[j]));
                        }
                    }

                    for (int i = 0; i < pointcuts.length; i++) {
                        int[] advices = pointcuts[i].getPostAdviceIndexes();
                        for (int j = 0; j < advices.length; j++) {
                            postAdvices.add(new Integer(advices[j]));
                        }
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
     * Provides custom deserialization.
     *
     * @param stream the object input stream containing the serialized object
     * @throws java.lang.Exception in case of failure
     */
    private void readObject(final ObjectInputStream stream) throws Exception {
        ObjectInputStream.GetField fields = stream.readFields();
        m_targetObject = fields.get("m_targetObject", null);
    }
}
