/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.test.security;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import aspectwerkz.aosd.context.Context;
import aspectwerkz.aosd.context.UserContext;
import aspectwerkz.aosd.definition.SecurityDefinition;
import aspectwerkz.aosd.security.principal.SimplePrincipal;
import aspectwerkz.aosd.security.principal.PrincipalStore;
import aspectwerkz.aosd.security.SecurityManagerFactory;
import aspectwerkz.aosd.security.SecurityManagerType;

import aspectwerkz.aosd.app.facade.Registry;
import aspectwerkz.aosd.app.service.CustomerManager;
import aspectwerkz.aosd.app.service.CustomerManagerImpl;
import aspectwerkz.aosd.app.domain.Customer;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

/**
 * Tests for the security handling.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class SecurityHandlingTest extends TestCase {

    private static final String PRINCIPAL = "jboner";
    private static final String CREDENTIAL = "jboner";

    public void testAuthenticationSuccess() {
        Context ctx = new UserContext();
        ctx.put(Context.PRINCIPAL, new SimplePrincipal(PRINCIPAL));
        ctx.put(Context.CREDENTIAL, new SimplePrincipal(CREDENTIAL));
        PrincipalStore.setContext(ctx);

        try {
            CustomerManager cm = Registry.getCustomerManager();
            assertNotNull(cm);
        }
        catch (Throwable throwable) {
            fail(throwable.toString());
        }
    }

    public void testAuthenticateInvalidPrincipal() {
        Context ctx = new UserContext();
        ctx.put(Context.PRINCIPAL, new SimplePrincipal("dummy"));
        ctx.put(Context.CREDENTIAL, new SimplePrincipal(CREDENTIAL));
        PrincipalStore.setContext(ctx);

        try {
            CustomerManager cm = Registry.getCustomerManager();
            assertNotNull(cm);
            cm.getName();
            fail("exception should have been thrown");
        }
        catch (Throwable throwable) {
        }
    }

    public void testAuthenticateInvalidCredential() {
        Context ctx = new UserContext();
        ctx.put(Context.PRINCIPAL, new SimplePrincipal(PRINCIPAL));
        ctx.put(Context.CREDENTIAL, new SimplePrincipal("dummy"));
        PrincipalStore.setContext(ctx);

        try {
            CustomerManager cm = Registry.getCustomerManager();
            assertNotNull(cm);
            cm.getName();
            fail("exception should have been thrown");
        }
        catch (Throwable throwable) {
        }
    }

    public void testAuthorizedMethod() {
        Context ctx = new UserContext();
        ctx.put(Context.PRINCIPAL, new SimplePrincipal(PRINCIPAL));
        ctx.put(Context.CREDENTIAL, new SimplePrincipal(CREDENTIAL));
        PrincipalStore.setContext(ctx);

        try {
            CustomerManager cm = Registry.getCustomerManager();
            assertNotNull(cm);
            assertEquals("CustomerManagerImpl", cm.getName());
        }
        catch (Throwable throwable) {
            fail(throwable.toString());
        }
    }

    public void testNotAuthorizedMethod() {
        Context ctx = new UserContext();
        ctx.put(Context.PRINCIPAL, new SimplePrincipal(PRINCIPAL));
        ctx.put(Context.CREDENTIAL, new SimplePrincipal(CREDENTIAL));
        PrincipalStore.setContext(ctx);

        try {
            CustomerManager cm = Registry.getCustomerManager();
            assertNotNull(cm);
            cm.getForbiddenMethod();
            fail("exception should have been thrown");
        }
        catch (Throwable throwable) {
        }
    }

    public void testPerformance() {
        try {
            Context ctx = new UserContext();
            ctx.put(Context.PRINCIPAL, new SimplePrincipal(PRINCIPAL));
            ctx.put(Context.CREDENTIAL, new SimplePrincipal(CREDENTIAL));
            PrincipalStore.setContext(ctx);

            CustomerManager cm = Registry.getCustomerManager();

            int numberOfInvocations = 10000;
            System.gc();
            long startMemory = Runtime.getRuntime().freeMemory();
            long startTime = System.currentTimeMillis();

            for (int i = 0; i < numberOfInvocations; i++) {
                String name = cm.getName();
            }

            long time = System.currentTimeMillis() - startTime;
            long memory = startMemory - Runtime.getRuntime().freeMemory();

            double timePerInvocation = time / (double)numberOfInvocations;
            double memoryPerInvocation = memory / (double)numberOfInvocations;

            System.out.println("\nTime per invocation: " + timePerInvocation);
            System.out.println("Memory per invocation: " + memoryPerInvocation);
        }
        catch (Exception ne) {
            ne.printStackTrace();
            fail();
        }
    }

    public SecurityHandlingTest(String name) {
        super(name);

        try {
            SecurityDefinition definition = new SecurityDefinition();

            SecurityDefinition.Role role = new SecurityDefinition.Role();
            role.setName(PRINCIPAL);
            definition.addRole(role);

            SecurityDefinition.Permission permission1 = new SecurityDefinition.Permission();
            permission1.setRole(PRINCIPAL);
            permission1.setKlass(CustomerManagerImpl.class);
            permission1.setMethod(CustomerManagerImpl.class.getMethod("getName", new Class[]{}));
            definition.addPermission(permission1);

            SecurityDefinition.Permission permission2 = new SecurityDefinition.Permission();
            permission2.setRole(PRINCIPAL);
            permission2.setKlass(CustomerManagerImpl.class);
            permission2.setMethod(CustomerManagerImpl.class.getMethod("updateCustomerName", new Class[]{Customer.class}));
            definition.addPermission(permission2);

            SecurityManagerFactory.getInstance(SecurityManagerType.JAAS).initialize(definition);
        }
        catch (NoSuchMethodException e) {
            throw new WrappedRuntimeException(e);
        }
    }

    public static Test suite() {
        return new TestSuite(SecurityHandlingTest.class);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }
}
