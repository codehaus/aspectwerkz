package aspectwerkz.aosd.security.jaas;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.login.FailedLoginException;

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

import aspectwerkz.aosd.context.Context;
import aspectwerkz.aosd.security.SecurityManager;
import aspectwerkz.aosd.security.AbstractSecurityManager;
import aspectwerkz.aosd.security.SecurityException;
import aspectwerkz.aosd.security.jaas.LoginCallbackHandler;
import aspectwerkz.aosd.security.principal.PrincipalStore;

/**
 * An mplementation of the <code>SecurityManager</code> using JAAS.
 * <p/>Handles the security and ACL in the system.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class JaasSecurityManager extends AbstractSecurityManager {

    /**
     * The sole instance of the JAAS security manager.
     */
    private static final SecurityManager INSTANCE = new JaasSecurityManager();

    // TODO: make configurable
    /**
     * The authentication model for JAAS to use.
     */
    private static final String AUTHENTICATION_MODEL = "FileLogin"; // Messages.getString("security.authentication.model");

    /**
     * The maximum number of authentication attempts.
     */
    private static final int NR_OF_RETRIES = 3;                     // Integer.parseInt(Messages.getString("security.authentication.retries"));

    /**
     * The pause time between the authentication attempts.
     */
    private static final int SLEEP_TIME = 5000;                     // Integer.parseInt(Messages.getString("security.authentication.sleeptime"));

    /**
     * Returns the one and only JaasSecurityManager (singleton).
     *
     * @return the one and only JaasSecurityManager
     */
    public static SecurityManager getInstance() {
        return INSTANCE;
    }

    /**
     * Implements the authentication of the client.
     *
     * @TODO: move to JAAS spec. impl. class (not default security manager) + make a jaas package and put everything spec. to JAAS there
     *
     * @param context the user context
     */
    public void authenticate(final Context context) {
        if (!m_initialized) throw new IllegalStateException("security manager is not initialized");
        if (context == null) throw new SecurityException("the caller has not provided a user context: authentication rejected");

        LoginContext loginContext = null;
        try {
            loginContext = new LoginContext(
                    AUTHENTICATION_MODEL,
                    new LoginCallbackHandler(context)
            );
        }
        catch (LoginException e) {
            throw new WrappedRuntimeException(e);
        }

        int i;
        for (i = 0; i < NR_OF_RETRIES; i++) {
            try {
                loginContext.login();
                break;
            }
            catch (FailedLoginException e) {
                try {
                    Thread.currentThread().sleep(SLEEP_TIME);
                }
                catch (InterruptedException ie) {
                    Thread.currentThread().notifyAll();
                    Thread.currentThread().interrupt();
                    throw new WrappedRuntimeException(e);
                }
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        }
        if (i == NR_OF_RETRIES) {
            throw new SecurityException("user could not be authenticated: " + context.toString());
        }

        PrincipalStore.setSubject(loginContext.getSubject());

        // -- DEBUG START --
        // let's see what Principals we have
//        System.out.println("Authenticated user has the following Principals:");
//        Set principals = loginContext.getSubject().getPrincipals();
//        for (Iterator it = principals.iterator(); it.hasNext();) {
//            Principal p = (Principal)it.next();
//            System.out.println("\t" + p.toString());
//        }
//        System.out.println(
//                "User has " +
//                loginContext.getSubject().getPublicCredentials().size() +
//                " Public Credential(s)"
//        );
//        System.out.println(
//                "User has " +
//                loginContext.getSubject().getPrivateCredentials().size() +
//                " Private Credential(s)"
//        );
        // -- DEBUG END --
    }

    /**
     * Non public constructor, to prevent instantiability
     */
    private JaasSecurityManager() {
    }
}

