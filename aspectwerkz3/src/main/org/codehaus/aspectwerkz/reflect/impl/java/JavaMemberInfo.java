/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.reflect.impl.java;

import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ClassInfoRepository;
import org.codehaus.aspectwerkz.reflect.MemberInfo;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public abstract class JavaMemberInfo implements MemberInfo {
    /**
     * The member.
     */
    protected final Member m_member;

    /**
     * The declaring type.
     */
    protected final ClassInfo m_declaringType;

    /**
     * The attributes.
     */
    protected final List m_attributes = new ArrayList();

    /**
     * The class info repository.
     */
    protected final ClassInfoRepository m_classInfoRepository;

    /**
     * Creates a new member meta data instance.
     *
     * @param member
     * @param declaringType
     */
    public JavaMemberInfo(final Member member, final JavaClassInfo declaringType) {
        m_member = member;
        m_declaringType = declaringType;
        m_classInfoRepository = ClassInfoRepository.getRepository(member.getDeclaringClass().getClassLoader());
    }

    /**
     * Returns the attributes.
     *
     * @return the attributes
     */
    public List getAttributes() {
        return m_attributes;
    }

    /**
     * Adds an attribute.
     *
     * @param attribute the attribute
     */
    public void addAttribute(final Object attribute) {
        m_attributes.add(attribute);
    }

    /**
     * Returns the name.
     *
     * @return the name
     */
    public String getName() {
        return m_member.getName();
    }

    /**
     * Returns the modifiers.
     *
     * @return the modifiers
     */
    public int getModifiers() {
        return m_member.getModifiers();
    }

    /**
     * Returns the declaring type.
     *
     * @return the declaring type
     */
    public ClassInfo getDeclaringType() {
        return m_declaringType;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JavaMemberInfo)) {
            return false;
        }

        final JavaMemberInfo javaMemberInfo = (JavaMemberInfo)o;

        if ((m_attributes != null) ? (!m_attributes.equals(javaMemberInfo.m_attributes))
                                   : (javaMemberInfo.m_attributes != null)) {
            return false;
        }
        if ((m_classInfoRepository != null) ? (!m_classInfoRepository.equals(javaMemberInfo.m_classInfoRepository))
                                            : (javaMemberInfo.m_classInfoRepository != null)) {
            return false;
        }
        if ((m_declaringType != null) ? (!m_declaringType.equals(javaMemberInfo.m_declaringType))
                                      : (javaMemberInfo.m_declaringType != null)) {
            return false;
        }
        if ((m_member != null) ? (!m_member.equals(javaMemberInfo.m_member)) : (javaMemberInfo.m_member != null)) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = ((m_member != null) ? m_member.hashCode() : 0);
        result = (29 * result) + ((m_declaringType != null) ? m_declaringType.hashCode() : 0);
        result = (29 * result) + ((m_attributes != null) ? m_attributes.hashCode() : 0);
        result = (29 * result) + ((m_classInfoRepository != null) ? m_classInfoRepository.hashCode() : 0);
        return result;
    }
}
