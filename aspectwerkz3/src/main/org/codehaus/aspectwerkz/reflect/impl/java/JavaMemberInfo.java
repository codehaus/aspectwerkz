/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
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
import java.util.List;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
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
    protected List m_annotations = null;

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
    JavaMemberInfo(final Member member, final JavaClassInfo declaringType) {
        if (member == null) {
            throw new IllegalArgumentException("member can not be null");
        }
        if (declaringType == null) {
            throw new IllegalArgumentException("declaring type can not be null");
        }
        m_member = member;
        m_declaringType = declaringType;
        m_classInfoRepository = ClassInfoRepository.getRepository(member.getDeclaringClass().getClassLoader());
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
}
