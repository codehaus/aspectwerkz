/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.test.unitofwork;

import java.lang.reflect.Method;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import aspectwerkz.aosd.app.service.CustomerManager;
import aspectwerkz.aosd.app.service.CustomerManagerImpl;
import aspectwerkz.aosd.app.domain.Customer;
import aspectwerkz.aosd.app.domain.DomainObjectFactory;
import aspectwerkz.aosd.app.facade.Registry;

import aspectwerkz.aosd.unitofwork.Transactional;
import aspectwerkz.aosd.unitofwork.UnitOfWork;
import aspectwerkz.aosd.definition.SecurityDefinition;
import aspectwerkz.aosd.security.SecurityManagerFactory;
import aspectwerkz.aosd.security.SecurityManagerType;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

/**
 * Tests for the UnitOfWork implementation.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class UnitOfWorkTest extends TestCase {

    // ===== UnitOfWork related tests =====

    public void testCommit() {
        CustomerManager cm = Registry.getCustomerManager();
        Customer c = DomainObjectFactory.newCustomer("jboner", "jboner");
        cm.updateCustomerName(c);
        assertEquals("dummy", c.getFirstName());
        assertEquals("dummy", c.getLastName());
    }

    public void testRollback() {
        CustomerManager cm = Registry.getCustomerManager();
        Customer c = DomainObjectFactory.newCustomer("donald", "duck");
        try {
            // 1. starts a new UnitOfWork
            // 2. create an address instance
            // 3. modifies the customer instance
            // 4. throws an exception that should cause rollback
            cm.updateCustomerNameForceRollback(c);
            fail("expected exception.");
        }
        catch (Exception e) {
            assertEquals("donald", c.getFirstName());
            assertEquals("duck", c.getLastName());
            return;
        }
        fail();
    }

    public void testSetRollbackOnly() {
        CustomerManager cm = Registry.getCustomerManager();
        Customer c = DomainObjectFactory.newCustomer("jboner", "jboner");
        cm.updateCustomerNameSetRollbackOnly(c);
        // Old values should be kept
        assertEquals("jboner", c.getFirstName());
        assertEquals("jboner", c.getLastName());
    }

//    public void testReadTransactionAttributes() {
//        Method[] methods = CustomerManagerImpl.class.getMethods();
//        assertEquals(TransactionDefinition.TX_REQUIRES, TransactionDefinitionController.getTransactionDefinition(methods[0]).getTransactionType());
//        assertEquals(TransactionDefinition.TX_REQUIRES, TransactionDefinitionController.getTransactionDefinition(methods[1]).getTransactionType());
//        assertEquals(TransactionDefinition.TX_REQUIRES, TransactionDefinitionController.getTransactionDefinition(methods[2]).getTransactionType());
//    }

    // ===== life-cycle related tests =====

    public void testCreateThroughFactory() {
        try {
            Customer c = DomainObjectFactory.newCustomer("jboner", "jboner");
        }
        catch (Throwable e) {
            fail(e.toString());
        }
    }

    public void testCreate() {
        try {
            Customer c = DomainObjectFactory.newCustomer(/*new Customer(*/"jboner", "jboner");
            ((Transactional)c).create();
        }
        catch (Throwable e) {
            fail(e.toString());
        }
    }

    public void testRemoveCreated() {
        try {
            Customer c = DomainObjectFactory.newCustomer(/*new Customer(*/"jboner", "jboner");
            ((Transactional)c).create();
            ((Transactional)c).remove();
        }
        catch (Throwable e) {
            fail(e.toString());
        }
    }

    public void testCreateDuplicate() {
        try {
            Customer c = DomainObjectFactory.newCustomer(/*new Customer(*/"jboner", "jboner");
            ((Transactional)c).create();
            ((Transactional)c).create();
            fail("exception expected");
        }
        catch (Throwable e) {
        }
    }

    public void testCreateRemoved() {
        try {
            Customer c = DomainObjectFactory.newCustomer(/*new Customer(*/"jboner", "jboner");
            ((Transactional)c).remove();
            ((Transactional)c).create();
            fail("exception expected");
        }
        catch (Throwable e) {
        }
    }

    public void testCreateDirty() {
        try {
            Customer c = DomainObjectFactory.newCustomer(/*new Customer(*/"jboner", "jboner");
            ((Transactional)c).markDirty();
            ((Transactional)c).create();
            fail("exception expected");
        }
        catch (Throwable e) {
        }
    }

    public void testRemoveNonCreated() {
        try {
            Customer c = DomainObjectFactory.newCustomer(/*new Customer(*/"jboner", "jboner");
            ((Transactional)c).remove();
        }
        catch (Throwable e) {
            fail(e.toString());
        }
    }

    public void testRemoveDirty() {
        try {
            Customer c = DomainObjectFactory.newCustomer(/*new Customer(*/"jboner", "jboner");
            ((Transactional)c).markDirty();
            ((Transactional)c).remove();
        }
        catch (Throwable e) {
            fail(e.toString());
        }
    }

//
//    public void testNestedMethodsRollback() {
//        System.out.println("testNestedMethodsRollback()");
//        CustomerManager cm = Registry.getCustomerManager();
//        Customer c = new Customer("jboner", "jboner");
//        Address a = new Address();
//        a.setCity("Salzburg");
//        a.setCountry("Salzburg");
//        c.setAddress(a);
//        try {
//            // This nested chain of methods should restoreObject.
//            ///All but the middle one (nestedMethod2) which is set to requires_new.
//            cm.nestedMethod1(c);
//        }
//        catch (Exception e) {
//            // Old values should be kept
//            assertEquals("jboner", c.getFirstName());
//            assertEquals("jboner", c.getLastName());
//            // New values should be set
//            assertEquals("Uppsala", c.getAddress().getCity());
//            assertEquals("Sverige", c.getAddress().getCountry());
//            return;
//        }
//        fail();
//    }

//    public void testModifiedCollectionRollback() {
//        System.out.println("testModifiedCollectionRollback()");
//        CustomerManager cm = Facade.getCustomerManager();
//        Customer c = new Customer("jboner", "jboner");
//        try {
//            cm.updateCustomerCollection(c);
//        }
//        catch (Exception e) {
//            List list = c.getList();
//            assertEquals(0, list.size());
//            return;
//        }
//        fail();
//    }

    public void tearDown() {
        UnitOfWork.dispose();
    }

    public UnitOfWorkTest(String name) {
        super(name);

        try {
            SecurityDefinition definition = new SecurityDefinition();

            SecurityDefinition.Role role = new SecurityDefinition.Role();
            role.setName("jboner");
            definition.addRole(role);

            SecurityDefinition.Permission permission1 = new SecurityDefinition.Permission();
            permission1.setRole("jboner");
            permission1.setKlass(CustomerManagerImpl.class);
            permission1.setMethod(CustomerManagerImpl.class.getMethod("getName", new Class[]{}));
            definition.addPermission(permission1);

            SecurityDefinition.Permission permission2 = new SecurityDefinition.Permission();
            permission2.setRole("jboner");
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
        return new TestSuite(UnitOfWorkTest.class);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }
}
