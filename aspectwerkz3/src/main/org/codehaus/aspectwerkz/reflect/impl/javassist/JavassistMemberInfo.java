/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.reflect.impl.javassist;

import org.codehaus.aspectwerkz.annotation.instrumentation.AttributeExtractor;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ClassInfoRepository;
import org.codehaus.aspectwerkz.reflect.MemberInfo;
import java.util.List;
import java.lang.ref.WeakReference;

import javassist.CtMember;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public abstract class JavassistMemberInfo implements MemberInfo {
    /**
     * The member.
     */
    protected final CtMember m_member;

    /**
     * The declaring type.
     */
    protected final ClassInfo m_declaringType;

    /**
     * The annotations.
     */
    protected List m_annotations = null;

    /**
     * The class info repository.
     */
    protected final ClassInfoRepository m_classInfoRepository;

    /**
     * The class loader that loaded the declaring class.
     */
    protected transient final WeakReference m_loaderRef;

    /**
     * The annotation extractor.
     */
    protected AttributeExtractor m_attributeExtractor;

    /**
     * Creates a new method meta data instance.
     *
     * @param member
     * @param declaringType
     * @param loader
     * @param attributeExtractor
     */
    JavassistMemberInfo(final CtMember member, final JavassistClassInfo declaringType, final ClassLoader loader,
                        final AttributeExtractor attributeExtractor) {
        if (member == null) {
            throw new IllegalArgumentException("class can not be null");
        }
        if (declaringType == null) {
            throw new IllegalArgumentException("declaring type can not be null");
        }
        if (loader == null) {
            throw new IllegalArgumentException("class loader can not be null");
        }
        m_member = member;
        m_declaringType = declaringType;
        m_loaderRef = new WeakReference(loader);
        m_classInfoRepository = ClassInfoRepository.getRepository(loader);
        m_attributeExtractor = attributeExtractor;
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
