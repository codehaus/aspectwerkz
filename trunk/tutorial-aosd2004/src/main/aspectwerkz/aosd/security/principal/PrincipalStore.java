/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.security.principal;

import javax.security.auth.Subject;

import aspectwerkz.aosd.context.Context;

/**
 * Provides storage for the client principals, credentials and security subjects.
 * Stores on a thread local basis.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public final class PrincipalStore {

    private static final ThreadLocal s_context = new ThreadLocal();
    private static final ThreadLocal s_subject = new ThreadLocal();

    /**
     * Returns the context.
     *
     * @return the context
     */
    public static Context getContext() {
        return (Context)s_context.get();
    }

    /**
     * Sets the context.
     *
     * @param context the context
     */
    public static void setContext(final Context context) {
        s_context.set(context);
    }

    /**
     * Returns the subject.
     *
     * @return the subject
     */
    public static Subject getSubject() {
        return (Subject)s_subject.get();
    }

    /**
     * Sets the subject.
     *
     * @param subject the subject
     */
    public static void setSubject(final Subject subject) {
        s_subject.set(subject);
    }
}

