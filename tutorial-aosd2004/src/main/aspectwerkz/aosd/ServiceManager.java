/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd;

import aspectwerkz.aosd.definition.JispDefinition;
import aspectwerkz.aosd.definition.SecurityDefinition;
import aspectwerkz.aosd.user.User;
import aspectwerkz.aosd.persistence.jisp.JispPersistenceManager;
//import aspectwerkz.aosd.app.service.CustomerManagerImpl;
//import aspectwerkz.aosd.app.domain.Customer;
import aspectwerkz.aosd.security.SecurityManagerFactory;
import aspectwerkz.aosd.security.SecurityManagerType;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

/**
 * Terrible boot strap class with the definition loading hard-coded.
 * <p/>
 * Should in a real application of course be in an external file, preferably an XML definition file.
 * <p/>
 * Initialize the services by invoking <code>ServiceManager.startServices()</code>.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class ServiceManager {

    public static void startServices() {
        startPersistenceManager();
        startSecurityManager();
    }

    public static void startSecurityManager() {
        // initialize the security manager
        try {
             SecurityDefinition securityDef = new SecurityDefinition();

             SecurityDefinition.Role role = new SecurityDefinition.Role();
             role.setName("jboner");
             securityDef.addRole(role);


             ServiceManager.class.getMethod("startSecurityManager", new Class[]{});

//             SecurityDefinition.Permission permission1 = new SecurityDefinition.Permission();
//             permission1.setRole("jboner");
//             permission1.setKlass(CustomerManagerImpl.class);
//             permission1.setMethod(CustomerManagerImpl.class.getMethod("getName", new Class[]{}));
//             securityDef.addPermission(permission1);

//             SecurityDefinition.Permission permission2 = new SecurityDefinition.Permission();
//             permission2.setRole("jboner");
//             permission2.setKlass(CustomerManagerImpl.class);
//             permission2.setMethod(CustomerManagerImpl.class.getMethod("updateCustomerName", new Class[]{Customer.class}));
//             securityDef.addPermission(permission2);

             SecurityManagerFactory.getInstance(SecurityManagerType.JAAS).initialize(securityDef);
         }
         catch (NoSuchMethodException e) {
             throw new WrappedRuntimeException(e);
         }
    }

    public static void startPersistenceManager() {
        JispDefinition definition = new JispDefinition();
        definition.setName("aosd2004");
        definition.setDbPath("/temp/_jisp");
        definition.setCreateDbOnStartup(false);

        JispDefinition.PersistentObjectDefinition objectDef = new JispDefinition.PersistentObjectDefinition();
        objectDef.setClassname(User.class.getName());

        JispDefinition.PersistentObjectDefinition.Index index = new JispDefinition.PersistentObjectDefinition.Index();
        index.setName("string.btree");
        index.setKeyMethod("getKey");
        objectDef.addIndex(index);
        definition.addPersistentObjectDefinition(objectDef);

        JispDefinition.BTreeIndexDefinition btreeIndex = new JispDefinition.BTreeIndexDefinition();
        btreeIndex.setName("string.btree");
        btreeIndex.setKeyType("com.coyotegulch.jisp.StringKey32");
        btreeIndex.setOrder(23);
        definition.addBtreeIndex(btreeIndex);

        JispPersistenceManager.getInstance().initialize(Thread.currentThread().getContextClassLoader(), definition);
    }
}
