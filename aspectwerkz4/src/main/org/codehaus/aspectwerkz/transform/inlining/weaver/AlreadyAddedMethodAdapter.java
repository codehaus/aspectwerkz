/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.inlining.weaver;

import java.util.Set;

import org.codehaus.aspectwerkz.transform.TransformationConstants;
import org.codehaus.aspectwerkz.transform.inlining.AsmNullAdapter;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Constants;

/**
 * A read only visitor to gather wrapper methods and proxy methods
 * Makes use of the NullVisitors
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class AlreadyAddedMethodAdapter extends AsmNullAdapter.NullClassAdapter implements Constants, TransformationConstants {

    /**
     * Set of "<methodName><methodDesc>" strings populated with wrapper methods, prefixed originals
     * and ctor body wrappers to allow multiweaving support.
     */
    private final Set m_addedMethods;

    /**
     * Creates a new class adapter.
     *
     * @param wrappers
     */
    public AlreadyAddedMethodAdapter(final Set wrappers) {
        m_addedMethods = wrappers;
    }

    /**
     * Visits the methods.
     *
     * @param access
     * @param name
     * @param desc
     * @param exceptions
     * @param attrs
     * @return
     */
    public CodeVisitor visitMethod(final int access,
                                   final String name,
                                   final String desc,
                                   final String[] exceptions,
                                   final Attribute attrs) {
        if (name.startsWith(WRAPPER_METHOD_PREFIX)
            || name.startsWith(ORIGINAL_METHOD_PREFIX)) {
            m_addedMethods.add(getMethodKey(name, desc));
        }
        return super.visitMethod(access, name, desc, exceptions, attrs);
    }

    static String getMethodKey(String name, String desc) {
        StringBuffer sb = new StringBuffer(name);
        return sb.append(desc).toString();
    }
}
