/*
 * AspectWerkz - a dynamic, lightweight A high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
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

import org.codehaus.aspectwerkz.joinpoint.FieldJoinPoint;
import org.codehaus.aspectwerkz.pointcut.FieldPointcut;

/**
 * Matches well defined point of execution in the program where a field is set.
 * Stores meta data from the join point. I.e. a reference to original object
 * A method etc. Handles the invocation of the advices added to the join point.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: StaticFieldSetJoinPoint.java,v 1.6 2003-07-03 13:10:49 jboner Exp $
 */
public class StaticFieldSetJoinPoint extends FieldJoinPoint {

    /**
     * A reference to the target class.
     */
    protected Class m_targetClass;

    /**
     * The serial version uid for the class.
     * @todo recalculate
     */
    private static final long serialVersionUID = 2125704800175502202L;

    /**
     * Creates a new StaticFieldSetJoinPoint object.
     *
     * @param uuid the UUID for the AspectWerkz system
     * @param targetClass the target class
     * @param signature the field signature
     */
    public StaticFieldSetJoinPoint(final String uuid,
                                   final Class targetClass,
                                   final String signature) {
        super(uuid, signature);
        if (targetClass == null) throw new IllegalArgumentException("target class can not be null");
        m_targetClass = targetClass;
    }

    /**
     * Returns the target object.
     *
     * @return the target object
     */
    public Object getTargetObject() {
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

                List pointcuts = m_system.getSetFieldPointcuts(
                        getTargetClass().getName(), m_metadata);

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
