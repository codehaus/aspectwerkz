/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.security.jaas;

import java.io.IOException;

import java.security.Principal;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import aspectwerkz.aosd.context.Context;

/**
 *  A simple CallbackHandler implementation that checks authentication with username and password.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class LoginCallbackHandler implements CallbackHandler {

    /**
     * The context holding the principals and credentials.
     */
    private final Context m_context;

    /**
     * Creates a new instance and sets the clients context.
     *
     * @param ctx the client principals
     */
    public LoginCallbackHandler(final Context ctx) {
        m_context = ctx;
    }

    /**
     * Handles the callbacks.
     *
     * @param callbacks an array with the callbacks
     * @throws java.io.IOException
     * @throws javax.security.auth.callback.UnsupportedCallbackException
     */
    public void handle(final Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {

            if (callbacks[i] instanceof NameCallback) {
                NameCallback nc = (NameCallback)callbacks[i];
                try {
                    Principal principal = (Principal)m_context.get(Context.PRINCIPAL);
                    nc.setName(principal.getName());
                }
                catch (Exception e) {
                    throw new SecurityException("principal not specified in context: " + m_context);
                }
            }
            else if (callbacks[i] instanceof PasswordCallback) {

                PasswordCallback pc = (PasswordCallback)callbacks[i];
                try {
                    Principal credential = (Principal)m_context.get(Context.CREDENTIAL);
                    pc.setPassword(credential.getName().toCharArray());
                }
                catch (Exception e) {
                    throw new SecurityException("credential not specified in context: " + m_context);
                }
            }
            else {
                throw new UnsupportedCallbackException(callbacks[i], "unrecognized callback");
            }
        }
    }
}

