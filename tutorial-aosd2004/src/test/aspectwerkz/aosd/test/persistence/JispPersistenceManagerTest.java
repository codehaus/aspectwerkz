/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.test.persistence;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import aspectwerkz.aosd.definition.JispDefinition;
import aspectwerkz.aosd.user.User;
import aspectwerkz.aosd.addressbook.AddressBook;
import aspectwerkz.aosd.addressbook.Contact;
import aspectwerkz.aosd.persistence.jisp.JispPersistenceManager;
import aspectwerkz.aosd.persistence.PersistenceManager;
import aspectwerkz.aosd.persistence.PersistenceManagerException;

/**
 * Tests for the security handling.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class JispPersistenceManagerTest extends TestCase {

    public void testStoreAndRetrieve() throws PersistenceManagerException {
        PersistenceManager pm = JispPersistenceManager.getInstance();

//        User user = (User)pm.retrieve(User.class, "jonaspasswd");
//        if (user == null) {
//            user = new User("jonas", "passwd");
//        }
//
//        AddressBook addressBook = user.getAddressBook();

        AddressBook addressBook = (AddressBook)pm.retrieve(AddressBook.class, "jonas.passwd");
        if (addressBook == null) {
            addressBook = new AddressBook("jonas.passwd");
        }

        System.out.println("number of contacts in addressbook = " + addressBook.getContacts().size());

        Contact contact = addressBook.findContact("donald", "duck");
        System.out.println("number of email addresses for contact = " + contact.getEmailAddresses().size());
        if (contact.isNull()) {
            contact = new Contact("donald", "duck");
        }

        contact.addEmailAddress("donald@duck.com");
        addressBook.addContact(contact);

        pm.store(addressBook);
    }

    public JispPersistenceManagerTest(String name) {
        super(name);
        JispDefinition definition = new JispDefinition();
        definition.setName("aosd2004");
        definition.setDbPath("./_jisp");
        definition.setCreateDbOnStartup(false);
        JispDefinition.PersistentObjectDefinition objectDef = new JispDefinition.PersistentObjectDefinition();

        //objectDef.setClassname(User.class.getName());
        objectDef.setClassname(AddressBook.class.getName());

        JispDefinition.PersistentObjectDefinition.Index index = new JispDefinition.PersistentObjectDefinition.Index();
        index.setName("string.btree");
        index.setKeyMethod("getOwnerKey");
        objectDef.addIndex(index);
        definition.addPersistentObjectDefinition(objectDef);
        JispDefinition.BTreeIndexDefinition btreeIndex = new JispDefinition.BTreeIndexDefinition();
        btreeIndex.setName("string.btree");
        btreeIndex.setKeyType("com.coyotegulch.jisp.StringKey32");
        btreeIndex.setOrder(23);
        definition.addBtreeIndex(btreeIndex);
        JispPersistenceManager.getInstance().initialize(Thread.currentThread().getContextClassLoader(), definition);
    }

    public static Test suite() {
        return new TestSuite(JispPersistenceManagerTest.class);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }
}
