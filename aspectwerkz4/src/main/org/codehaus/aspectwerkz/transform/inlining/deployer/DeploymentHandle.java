/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.inlining.deployer;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.lang.ref.WeakReference;

import org.codehaus.aspectwerkz.util.UuidGenerator;
import org.codehaus.aspectwerkz.definition.AdviceDefinition;
import org.codehaus.aspectwerkz.definition.SystemDefinitionContainer;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.definition.AspectDefinition;
import org.codehaus.aspectwerkz.expression.ExpressionInfo;

/**
 * Universal Unique IDentifier (UUID) for a deployment event.
 * <p/>
 * Can be stored by the user to allow access to a specific deployment event.
 * <p/>
 * Visibility for all methods are package private, user should only use it as a handle.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public final class DeploymentHandle {

    private final String UUID;
    private final Map m_definitionChangeElements = new HashMap();
    private final WeakReference m_loaderRef;
    private final WeakReference m_classRef;

    /**
     * Creates a new handle.
     *
     * @param clazz the class of the entity being deployed
     */
    DeploymentHandle(final Class clazz, final ClassLoader loader) {
        if (clazz == null) {
            throw new IllegalArgumentException("class can not be null");
        }
        if (loader == null) {
            throw new IllegalArgumentException("loader can not be null");
        }
        UUID = UuidGenerator.generate(clazz);
        m_loaderRef = new WeakReference(loader);
        m_classRef = new WeakReference(clazz);
    }

    void registerDefinitionChange(final AdviceDefinition adviceDef, final ExpressionInfo oldExpression) {
        m_definitionChangeElements.put(
                adviceDef.getQualifiedName(),
                new DefinitionChangeElement(adviceDef, oldExpression)
        );
    }

    Class getAspectClass() {
        return (Class) m_classRef.get();
    }

    Map getDefintionChangeElements() {
        return m_definitionChangeElements;
    }

    void revertChanges() {
        final ClassLoader loader = (ClassLoader) m_loaderRef.get();
        // hotdeployment is done thru the virtual system, so reverts changes as well
        SystemDefinition systemDef = SystemDefinitionContainer.getVirtualDefinitionAt(loader);
        for (Iterator it2 = systemDef.getAspectDefinitions().iterator(); it2.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition) it2.next();
            for (Iterator it3 = aspectDef.getAfterAdviceDefinitions().iterator(); it3.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition) it3.next();
                DefinitionChangeElement changeElement =
                        (DefinitionChangeElement) m_definitionChangeElements.get(adviceDef.getQualifiedName());
                if (changeElement != null) {
                    changeElement.getAdviceDef().setExpressionInfo(changeElement.getOldExpression());
                }
            }
        }
    }

    public String toString() {
        return new StringBuffer().append("DeploymentHandle [").
                append(UUID.toString()).append(',').
                append(((Class) m_classRef.get()).getName()).append(',').
                append((ClassLoader) m_loaderRef.get()).append(']').toString();
    }

    public int hashCode() {
        return UUID.hashCode();
    }

    public boolean equals(Object o) {
        return ((DeploymentHandle) o).UUID.equals(UUID);
    }

    /**
     * Holds the definition change of one advice.
     *
     * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
     */
    static class DefinitionChangeElement {
        private final AdviceDefinition m_adviceDef;
        private final ExpressionInfo m_oldExpression;

        public DefinitionChangeElement(final AdviceDefinition adviceDef, final ExpressionInfo oldExpression) {
            m_adviceDef = adviceDef;
            m_oldExpression = oldExpression;
        }

        public ExpressionInfo getOldExpression() {
            return m_oldExpression;
        }

        public AdviceDefinition getAdviceDef() {
            return m_adviceDef;
        }
    }
}
