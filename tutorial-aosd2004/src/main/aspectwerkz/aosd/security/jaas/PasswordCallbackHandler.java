/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.security.jaas;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import java.io.IOException;

/**
 * A simple call back handler to supply the username / password to JAAS.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class PasswordCallbackHandler implements CallbackHandler {

    /**
     * The username.
     */
    private final String m_username;

    /**
     * The password.
     */
    private final char[] m_password;

    /**
     * Creates a new password callback handler.
     *
     * @param username
     * @param password
     */
    public PasswordCallbackHandler(final String username, final String password) {
        m_username = username;
        m_password = password.toCharArray();
    }

    /**
     * Standard implementation of the CallbackHandler interface
     *
     * @param callbacks an array with the callbacks
     * @throws java.io.IOException
     * @throws javax.security.auth.callback.UnsupportedCallbackException
     */
    public void handle(final Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof NameCallback) {
                ((NameCallback)callbacks[i]).setName(m_username);
            }
            else if (callbacks[i] instanceof PasswordCallback) {
                ((PasswordCallback)callbacks[i]).setPassword(m_password);
            }
            else {
                throw new UnsupportedCallbackException(
                        callbacks[i],
                        "unrecognized callback - only supports username/password"
                );
            }
        }
    }
}
