/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.definition;

import java.util.ArrayList;
import java.util.Collection;
import java.lang.reflect.Method;

/**
 * Holds the definition of the security concern.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class SecurityDefinition implements Definition {

    private Collection m_roles = new ArrayList();
    private Collection m_authenticationPoints = new ArrayList();
    private Collection m_authorizationPoints = new ArrayList();
    private Collection m_permissions = new ArrayList();

    public Collection getRoles() {
        return m_roles;
    }

    public void addRole(final Role role) {
        m_roles.add(role);
    }

    public Collection getAuthenticationPoints() {
        return m_authenticationPoints;
    }

    public void addAuthenticationPoint(final AuthenticationPoint authenticationPoint) {
        m_authenticationPoints.add(authenticationPoint);
    }

    public Collection getAuthorizationPoints() {
         return m_authorizationPoints;
     }

     public void addAuthorizationPoint(final AuthorizationPoint authorizationPoint) {
         m_authorizationPoints.add(authorizationPoint);
     }

     public Collection getPermissions() {
        return m_permissions;
    }

    public void addPermission(final Permission permission) {
        m_permissions.add(permission);
    }

    /**
     * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
     */
    public final static class Role implements Definition {

        private String m_name = null;
        private String m_description = null;

        public String getName() {
            return m_name;
        }

        public void setName(final String name) {
            m_name = name;
        }

        public String getDescription() {
            return m_description;
        }

        public void setDescription(final String description) {
            m_description = description;
        }
    }

    /**
     * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
     */
    public final static class AuthenticationPoint implements Definition {

        private String m_interfacePattern = null;
        private String m_methodPattern = null;

        public String getInterfacePattern() {
            return m_interfacePattern;
        }

        public void setInterfacePattern(final String interfacePattern) {
            m_interfacePattern = interfacePattern;
        }

        public String getMethodPattern() {
            return m_methodPattern;
        }

        public void setMethodPattern(final String methodPattern) {
            m_methodPattern = methodPattern;
        }
    }

    /**
     * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
     */
    public final static class AuthorizationPoint implements Definition {

        private String m_interfacePattern = null;
        private String m_methodPattern = null;

        public String getInterfacePattern() {
            return m_interfacePattern;
        }

        public void setInterfacePattern(final String interfacePattern) {
            m_interfacePattern = interfacePattern;
        }

        public String getMethodPattern() {
            return m_methodPattern;
        }

        public void setMethodPattern(final String methodPattern) {
            m_methodPattern = methodPattern;
        }
    }

    /**
     * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
     */
    public final static class Permission implements Definition {

        private Class m_klass = null;
        private Method m_method = null;
        private String m_role = null;

        public String getRole() {
            return m_role;
        }

        public void setRole(final String role) {
            m_role = role;
        }

        public Class getKlass() {
            return m_klass;
        }

        public void setKlass(final Class klass) {
            m_klass = klass;
        }

        public Method getMethod() {
            return m_method;
        }

        public void setMethod(final Method method) {
            m_method = method;
        }
    }
}
