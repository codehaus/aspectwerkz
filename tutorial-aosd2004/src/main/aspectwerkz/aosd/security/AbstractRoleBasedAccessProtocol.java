/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.security;

import java.util.Iterator;
import java.security.Principal;
import java.security.PrivilegedExceptionAction;
import javax.security.auth.Subject;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.Signature;
import org.codehaus.aspectwerkz.joinpoint.MethodSignature;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.aspect.Aspect;
import org.codehaus.aspectwerkz.Pointcut;

import aspectwerkz.aosd.context.Context;
import aspectwerkz.aosd.security.principal.PrincipalStore;

/**
 * @Aspect perThread
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public abstract class AbstractRoleBasedAccessProtocol extends Aspect {

    /**
     * The security manager to use.
     * @todo should be configurable
     */
    protected final SecurityManager m_securityManager =
            SecurityManagerFactory.getInstance(SecurityManagerType.JAAS);

    /**
     * To be defined by the concrete aspect.
     */
    Pointcut authenticationPoints;

    /**
     * To be defined by the concrete aspect.
     */
    Pointcut authorizationPoints;

    /**
     * Authenticates the user.
     *
     * @Around authenticationPoints
     */
    public Object authenticateUser(final JoinPoint joinPoint) throws Throwable {
        Subject subject = PrincipalStore.getSubject();
        try {
            // no subject => authentication required
            if (subject == null) {
                subject = authenticate();
            }
        }
        catch (Exception e) {
            Signature signature = joinPoint.getSignature();
            StringBuffer msg = new StringBuffer();
            msg.append("authentication denied at ");
            msg.append(joinPoint.getTargetClass().getName());
            msg.append('.');
            msg.append(signature.getName());
            msg.append(" for user [");
            msg.append(PrincipalStore.getContext());
            msg.append("] due to: ");
            msg.append(e.toString());
            throw new SecurityException(msg.toString());
        }

        // if authenticated => check permission to invoke method
        Object result = Subject.doAsPrivileged(
                subject,
                new PrivilegedExceptionAction() {
                    public Object run() throws Exception {
                        final Object result;
                        try {
                            result = joinPoint.proceed();
                        }
                        catch (Throwable throwable) {
                            throw new WrappedRuntimeException(throwable);
                        }
                        return result;
                    };
                },
                null
        );
        return result;
    }

    /**
     * Authorizes the user.
     *
     * @Around authorizationPoints
     */
    public Object authorizeUser(final JoinPoint joinPoint) throws Throwable {
        if (checkPermission(joinPoint)) {
            // user is authorized => proceed with the method invocation
            return joinPoint.proceed();
        }
        else {
            // not authorized, bail out
            Signature signature = joinPoint.getSignature();
            StringBuffer msg = new StringBuffer();
            msg.append("authorization denied at ");
            msg.append(joinPoint.getTargetClass().getName());
            msg.append('.');
            msg.append(signature.getName());
            msg.append(" for user [");
            msg.append(PrincipalStore.getContext());
            msg.append(']');
            throw new SecurityException(msg.toString());
        }
    }

    /**
     * Authenticates the user.
     *
     * @return the subject
     */
    protected Subject authenticate() {
        Context context = PrincipalStore.getContext();
        m_securityManager.authenticate(context);
        return PrincipalStore.getSubject();
    }

    /**
     * Checks access for the method specified.
     *
     * @param joinPoint the join point for this access control
     * @return boolean
     */
    protected boolean checkPermission(final JoinPoint joinPoint) {
        Subject subject = PrincipalStore.getSubject();
        if (subject == null) {
            return false;
        }
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        for (Iterator it = subject.getPrincipals().iterator(); it.hasNext();) {
            Principal principal = (Principal)it.next();
            if (m_securityManager.checkPermission(
                    principal,
                    joinPoint.getTargetClass(),
                    signature.getMethod())) {
                return true;
            }
        }
        return false;
    }
}
