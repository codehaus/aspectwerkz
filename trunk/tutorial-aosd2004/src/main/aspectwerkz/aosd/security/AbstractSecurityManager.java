package aspectwerkz.aosd.security;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.lang.reflect.Method;
/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
import java.security.Principal;

import org.codehaus.aspectwerkz.transform.TransformationUtil;
import org.codehaus.aspectwerkz.metadata.ReflectionMetaDataMaker;

import aspectwerkz.aosd.context.Context;
import aspectwerkz.aosd.definition.Definition;
import aspectwerkz.aosd.definition.SecurityDefinition;

/**
 * Abstract base implementation of the <code>SecurityManager</code> interface.
 * <p/>To be inherited by custome implementations.
 * <p/>Handles the security and ACL in the system.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public abstract class AbstractSecurityManager implements SecurityManager {

    /**
     * The ACL, stores the permissions for each role.
     */
    protected final Map m_accessControlList = new HashMap();

    /**
     * Marks the security manager as initialized.
     */
    protected boolean m_initialized = false;

    /**
     * To be implemented by the custom implementations using this base class.
     * <p/>Implements the authentication of the client.
     *
     * @param context the user context
     */
    public abstract void authenticate(final Context context);

    /**
     * Initializes the security manager.
     *
     * @param definition the persistence definition
     */
    public synchronized void initialize(final Definition definition) {
        if (definition == null) return;
        if (m_initialized) return;
        createACL((SecurityDefinition)definition);
        m_initialized = true;
    }

    /**
     * Checks if a specific role has access to a specific methodToCheck.
     *
     * @param principal the principal to to check access for
     * @param classToCheck the class to check access at
     * @param methodToCheck the methodToCheck to check access at
     * @return boolean
     */
    public boolean checkPermission(final Principal principal,
                                   final Class classToCheck,
                                   final Method methodToCheck) {
        //System.out.println("check " + classToCheck.getName() + " " + methodToCheck.getName());
        if (!m_initialized) throw new IllegalStateException("security manager is not initialized");
        if (principal == null || classToCheck == null || methodToCheck == null) return false;

        final Collection permissions = (Collection)m_accessControlList.get(principal.getName());
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }
        for (Iterator it = permissions.iterator(); it.hasNext();) {
            SecurityDefinition.Permission permission = (SecurityDefinition.Permission)it.next();
            Method method = permission.getMethod();
            Class klass = permission.getKlass();

            boolean matches = false;
            String prefixedMethodName = TransformationUtil.ORIGINAL_METHOD_PREFIX + method.getName();
            //System.out.println("try " + klass.getName() + " " + prefixedMethodName);
            if ((classToCheck.getName().equals(klass.getName()) || hasInterface(classToCheck, klass.getName()))
                    && methodToCheck.getName().startsWith(prefixedMethodName)) {
                matches = true;
                Class[] parameterTypes1 = method.getParameterTypes();
                Class[] parameterTypes2 = methodToCheck.getParameterTypes();
                if (parameterTypes1.length != parameterTypes2.length) {
                    matches = false;
                    continue;
                }
                for (int i = 0; i < parameterTypes1.length; i++) {
                    if (!parameterTypes1[i].equals(parameterTypes2[i])) {
                        matches = false;
                        continue;
                    }
                }
            }
            if (matches) {
                return matches;
            }
        }
        return false;
    }

    /**
     * Checks if klass implements interfaceName
     *
     * @param klass
     * @param interfaceName
     * @return
     */
    private static boolean hasInterface(Class klass, String interfaceName) {
        //TODO support for more level and super class
        for (int i = 0; i < klass.getInterfaces().length; i++) {
            if (klass.getInterfaces()[i].getName().equals(interfaceName)) {
                return true;
            }
        }
        return false;
    }
    /**
     * Creates a mapping with roles to methods.
     *
     * @param definition the security definition
     */
    protected void createACL(final SecurityDefinition definition) {
        if (definition == null) throw new IllegalArgumentException("security definition can not be null");

        Collection roles = definition.getRoles();
        Collection permissions = definition.getPermissions();

        for (Iterator it = roles.iterator(); it.hasNext();) {
            final SecurityDefinition.Role role = (SecurityDefinition.Role)it.next();
            m_accessControlList.put(role.getName(), new ArrayList());
        }
        for (Iterator it = permissions.iterator(); it.hasNext();) {
            final SecurityDefinition.Permission permission = (SecurityDefinition.Permission)it.next();
            Collection permissionsForRole = (Collection)m_accessControlList.get(permission.getRole());
            if (permissionsForRole == null) {
                permissionsForRole = new ArrayList();
                m_accessControlList.put(permission.getRole(), permissionsForRole);
            }
            permissionsForRole.add(permission);
        }
    }
}

