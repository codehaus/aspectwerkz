/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.reflect.impl.asm;

import org.codehaus.aspectwerkz.annotation.instrumentation.asm.CustomAttribute;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.MemberInfo;
import org.objectweb.asm.Attribute;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * ASM implementation of the MemberInfo interface.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public abstract class AsmMemberInfo implements MemberInfo {
    /**
     * The member.
     */
    protected final MemberStruct m_member;

    /**
     * The class loader wrapped in a weak ref.
     */
    protected final WeakReference m_loaderRef;

    /**
     * The declaring type name.
     */
    protected final String m_declaringTypeName;

    /**
     * The declaring type.
     */
    protected ClassInfo m_declaringType;

    /**
     * The annotations.
     */
    protected List m_annotations = null;

    /**
     * The class info repository.
     */
    protected final AsmClassInfoRepository m_classInfoRepository;

    /**
     * Creates a new member meta data instance.
     * 
     * @param member
     * @param declaringType
     * @param loader
     */
    AsmMemberInfo(final MemberStruct member, final String declaringType, final ClassLoader loader) {
        if (member == null) {
            throw new IllegalArgumentException("member can not be null");
        }
        if (declaringType == null) {
            throw new IllegalArgumentException("declaring type can not be null");
        }
        m_member = member;
        m_loaderRef = new WeakReference(loader);
        m_declaringTypeName = declaringType.replace('/', '.');
        m_classInfoRepository = AsmClassInfoRepository.getRepository(loader);
    }

    /**
     * Returns the name.
     * 
     * @return the name
     */
    public String getName() {
        return m_member.name;
    }

    /**
     * Returns the modifiers.
     * 
     * @return the modifiers
     */
    public int getModifiers() {
        return m_member.modifiers;
    }

    /**
     * Returns the declaring type.
     * 
     * @return the declaring type
     */
    public ClassInfo getDeclaringType() {
        if (m_declaringType == null) {
            m_declaringType = m_classInfoRepository.getClassInfo(m_declaringTypeName);//AsmClassInfo.createClassInfoFromStream(m_declaringTypeName, (ClassLoader) m_loaderRef.get());
        }
        return m_declaringType;
    }

    /**
     * Returns the annotations.
     * 
     * @return the annotations
     */
    public List getAnnotations() {
        if (m_annotations == null) {
            m_annotations = new ArrayList();
            addAnnotations(m_member.attrs);
        }
        return m_annotations;
    }

    /**
     * Retrieves and adds the annotations.
     * 
     * @param attrs
     */
    private void addAnnotations(final Attribute attrs) {
        Attribute attributes = attrs;
        while (attributes != null) {
            if (attributes instanceof CustomAttribute) {
                CustomAttribute customAttribute = (CustomAttribute) attributes;
                byte[] bytes = customAttribute.getBytes();
                try {
                    m_annotations.add(new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject());
                } catch (Exception e) {
                    System.err.println("WARNING: could not deserialize annotation due to: " + e.toString());
                }
            }
            attributes = attributes.next;
        }
    }
}