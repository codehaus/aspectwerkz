/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import org.codehaus.aspectwerkz.aspect.CFlowSystemAspect;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.expression.ExpressionInfo;
import org.codehaus.aspectwerkz.expression.ExpressionVisitor;
import org.codehaus.aspectwerkz.util.SequencedHashMap;
import org.codehaus.aspectwerkz.transform.AspectWerkzPreProcessor;
import org.codehaus.aspectwerkz.reflect.impl.java.JavaClassInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TODO clean up - remove methods not used, refactor etc.
 * <p/>
 * Abstraction of the system definition, defines the aspect system.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class SystemDefinition {

    /**
     * Empty hash map.
     */
    public static final Map EMPTY_HASH_MAP = new HashMap();

    /**
     * Maps the aspects to it's name.
     */
    private final Map m_aspectMap = new SequencedHashMap();

    /**
     * Maps the mixins to it's name.
     */
    private final Map m_mixinMap = new SequencedHashMap();

    /**
     * The UUID for this definition.
     */
    private String m_uuid = "default";

    /**
     * The include packages.
     */
    private final Set m_includePackages = new HashSet();

    /**
     * The exclude packages.
     */
    private final Set m_excludePackages = new HashSet();

    /**
     * The prepare packages.
     */
    private final Set m_preparePackages = new HashSet();

    /**
     * All prepared pointcuts defined in the system.
     */
    private final Map m_deploymentScopes = new HashMap();

    /**
     * Returns the system definition with a specific id in a specific class loader.
     *
     * @param loader the class loader that the definition lives in
     * @param id     the id of the definition
     * @return the system definition
     */
    public static SystemDefinition getDefinitionFor(final ClassLoader loader, final String id) {
        return SystemDefinitionContainer.getDefinitionFor(loader, id);
    }

    /**
     * Returns the system definitions in a specific class loader.
     *
     * @param loader the class loader that the definition lives in
     * @return a set with the system definitions
     */
    public static Set getDefinitionsFor(final ClassLoader loader) {
        return SystemDefinitionContainer.getDefinitionsFor(loader);
    }

    /**
     * Creates a new instance, creates and sets the system cflow aspect.
     */
    public SystemDefinition(final String uuid) {
        setUuid(uuid);
        AspectDefinition systemAspect = new AspectDefinition(
                CFlowSystemAspect.class.getName(),
                JavaClassInfo.getClassInfo(CFlowSystemAspect.class),
                this
        );
        systemAspect.setDeploymentModel(CFlowSystemAspect.DEPLOYMENT_MODEL);
        m_aspectMap.put(CFlowSystemAspect.CLASS_NAME, systemAspect);
    }

    /**
     * Sets the UUID for the definition.
     *
     * @param uuid the UUID
     */
    private void setUuid(final String uuid) {
        m_uuid = uuid;
    }

    /**
     * Returns the UUID for the definition.
     *
     * @return the UUID
     */
    public String getUuid() {
        return m_uuid;
    }

    /**
     * Returns the include packages.
     *
     * @return the include packages
     */
    public Set getIncludePackages() {
        return m_includePackages;
    }

    /**
     * Returns the exclude packages.
     *
     * @return the exclude packages
     */
    public Set getExcludePackages() {
        return m_excludePackages;
    }

    /**
     * Returns a collection with the aspect definitions registered.
     *
     * @return the aspect definitions
     */
    public Collection getAspectDefinitions() {
        Collection clone = new ArrayList(m_aspectMap.size());
        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            clone.add(it.next());
        }
        return clone;
    }

    /**
     * Returns a collection with the mixin definitions registered.
     *
     * @return the mixin definitions
     */
    public Collection getMixinDefinitions() {
        Collection clone = new ArrayList(m_mixinMap.size());
        for (Iterator it = m_mixinMap.values().iterator(); it.hasNext();) {
            clone.add(it.next());
        }
        return clone;
    }

    /**
     * Returns a collection with the advice definitions registered.
     *
     * @return the advice definitions
     */
    public Collection getAdviceDefinitions() {
        final Collection adviceDefs = new ArrayList();
        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition) it.next();
            adviceDefs.addAll(aspectDef.getAroundAdviceDefinitions());
            adviceDefs.addAll(aspectDef.getBeforeAdviceDefinitions());
            adviceDefs.addAll(aspectDef.getAfterAdviceDefinitions());
        }
        return adviceDefs;
    }

    /**
     * Returns a specific aspect definition.
     *
     * @param name the name of the aspect definition
     * @return the aspect definition
     */
    public AspectDefinition getAspectDefinition(final String name) {
        return (AspectDefinition) m_aspectMap.get(name);
    }

    /**
     * Returns the mixin definitions matching a specfic expression.
     *
     * @param ctx the expression context
     * @return a list with the mixin definitions
     */
    public List getMixinDefinitions(final ExpressionContext ctx) {
        final List introDefs = new ArrayList();
        for (Iterator it = m_mixinMap.values().iterator(); it.hasNext();) {
            MixinDefinition introDef = (MixinDefinition) it.next();
            for (int i = 0; i < introDef.getExpressionInfos().length; i++) {
                if (introDef.getExpressionInfos()[i].getExpression().match(ctx)) {
                    introDefs.add(introDef);
                }
            }
        }
        return introDefs;
    }

    /**
     * Returns the interface introductions for a certain class merged with the implementation based introductions as
     * well.
     *
     * @param ctx the expression context
     * @return the names
     */
    public List getInterfaceIntroductionDefinitions(final ExpressionContext ctx) {
        if (ctx == null) {
            throw new IllegalArgumentException("context can not be null");
        }
        List interfaceIntroductionDefs = new ArrayList();
        for (Iterator iterator = m_aspectMap.values().iterator(); iterator.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition) iterator.next();
            for (Iterator it = aspectDef.getInterfaceIntroductionDefinitions().iterator(); it.hasNext();) {
                InterfaceIntroductionDefinition introDef = (InterfaceIntroductionDefinition) it.next();
                ExpressionInfo[] expressionInfos = introDef.getExpressionInfos();
                for (int i = 0; i < expressionInfos.length; i++) {
                    ExpressionInfo expressionInfo = expressionInfos[i];
                    ExpressionVisitor expression = expressionInfo.getExpression();
                    if (expression.match(ctx)) {
                        interfaceIntroductionDefs.add(introDef);
                    }
                }
            }
        }
        return interfaceIntroductionDefs;
    }

    /**
     * Adds a new aspect definition.
     *
     * @param aspectDef the aspect definition
     */
    public void addAspect(final AspectDefinition aspectDef) {
        if (aspectDef == null) {
            throw new IllegalArgumentException("aspect definition can not be null");
        }
        synchronized (m_aspectMap) {
            if (m_aspectMap.containsKey(aspectDef.getName())) {
                return;
            }
            m_aspectMap.put(aspectDef.getName(), aspectDef);
        }
    }

    /**
     * Adds a new aspect definition, overwrites the previous one with the same name (if there is one).
     *
     * @param aspectDef the aspect definition
     */
    public void addAspectOverwriteIfExists(final AspectDefinition aspectDef) {
        if (aspectDef == null) {
            throw new IllegalArgumentException("aspect definition can not be null");
        }
        synchronized (m_aspectMap) {
            m_aspectMap.put(aspectDef.getName(), aspectDef);
        }
    }

    /**
     * Adds a new mixin definition.
     *
     * @param mixinDef the mixin definition
     */
    public void addMixinDefinition(final MixinDefinition mixinDef) {
        if (mixinDef == null) {
            throw new IllegalArgumentException("mixin definition can not be null");
        }
        synchronized (m_mixinMap) {
            if (m_mixinMap.containsKey(mixinDef.getMixinImpl().getName())) {
                MixinDefinition def = (MixinDefinition) m_mixinMap.get(mixinDef.getMixinImpl().getName());
                def.addExpressionInfos(mixinDef.getExpressionInfos());
                return;
            }
            m_mixinMap.put(mixinDef.getMixinImpl().getName(), mixinDef);
        }
    }

    /**
     * Adds a new include package.
     *
     * @param includePackage the new include package
     */
    public void addIncludePackage(final String includePackage) {
        synchronized (m_includePackages) {
            m_includePackages.add(includePackage + '.');
        }
    }

    /**
     * Adds a new exclude package.
     *
     * @param excludePackage the new exclude package
     */
    public void addExcludePackage(final String excludePackage) {
        synchronized (m_excludePackages) {
            m_excludePackages.add(excludePackage + '.');
        }
    }

    /**
     * Adds a new prepare package.
     *
     * @param preparePackage the new prepare package
     */
    public void addPreparePackage(final String preparePackage) {
        synchronized (m_preparePackages) {
            m_preparePackages.add(preparePackage + '.');
        }
    }

    /**
     * Returns the prepare packages.
     *
     * @return
     */
    public Set getPreparePackages() {
        return m_preparePackages;
    }

    /**
     * Checks if a class should be included.
     *
     * @param className the name or the class
     * @return boolean
     */
    public boolean inIncludePackage(final String className) {
        if (className == null) {
            throw new IllegalArgumentException("class name can not be null");
        }
        if (m_includePackages.isEmpty()) {
            return true;
        }
        for (Iterator it = m_includePackages.iterator(); it.hasNext();) {
            String packageName = (String) it.next();
            if (className.startsWith(packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a class should be excluded.
     *
     * @param className the name or the class
     * @return boolean
     */
    public boolean inExcludePackage(final String className) {
        if (className == null) {
            throw new IllegalArgumentException("class name can not be null");
        }
        for (Iterator it = m_excludePackages.iterator(); it.hasNext();) {
            String packageName = (String) it.next();
            if (className.startsWith(packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a class is in prepare declaration
     *
     * @param className the name or the class
     * @return boolean
     */
    public boolean inPreparePackage(String className) {
        if (className == null) {
            throw new IllegalArgumentException("class name can not be null");
        }
        for (Iterator it = m_preparePackages.iterator(); it.hasNext();) {
            String packageName = (String) it.next();
            if (className.startsWith(packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a context has a pointcut.
     *
     * @param ctx the expression context
     * @return boolean
     */
    public boolean hasPointcut(final ExpressionContext ctx) {
        if (ctx == null) {
            throw new IllegalArgumentException("context can not be null");
        }
        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition) it.next();
            for (Iterator it2 = aspectDef.getAdviceDefinitions().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition) it2.next();
                final ExpressionInfo expressionInfo = adviceDef.getExpressionInfo();
                if (expressionInfo == null) {
                    continue;
                }
                ExpressionVisitor expression = expressionInfo.getExpression();

                if (expression.match(ctx)) {
                    if (AspectWerkzPreProcessor.DETAILS) {
                        System.out.println(
                                "[TRACE - match: " + expression + " @ "
                                + aspectDef.getQualifiedName() + "/" +
                                adviceDef.getName()
                        );
                        System.out.println("[       for     " + ctx.getReflectionInfo());
                        System.out.println("[       within  " + ctx.getWithinReflectionInfo());
                        System.out.println("[       type    " + ctx.getPointcutType().toString());
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a method has an cflow pointcut.
     *
     * @param ctx the expression context
     * @return boolean
     */
    public boolean hasCflowPointcut(final ExpressionContext ctx) {
        if (ctx == null) {
            throw new IllegalArgumentException("context can not be null");
        }
        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition) it.next();
            for (Iterator it2 = aspectDef.getAdviceDefinitions().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition) it2.next();
                ExpressionInfo expressionInfo = adviceDef.getExpressionInfo();
                if (expressionInfo == null) {
                    continue;
                }
                if (expressionInfo.hasCflowPointcut() && expressionInfo.getCflowExpression().match(ctx)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class is advised.
     *
     * @param ctxs an array with the expression contexts
     * @return boolean
     */
    public boolean isAdvised(final ExpressionContext[] ctxs) {
        if (ctxs == null) {
            throw new IllegalArgumentException("context array can not be null");
        }
        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition) it.next();
            List advices = aspectDef.getAdviceDefinitions();
            for (Iterator it2 = advices.iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition) it2.next();
                for (int i = 0; i < ctxs.length; i++) {
                    ExpressionContext ctx = ctxs[i];
                    final ExpressionInfo expressionInfo = adviceDef.getExpressionInfo();
                    if (expressionInfo == null) {
                        continue;
                    }
                    if (expressionInfo.getAdvisedClassFilterExpression().match(ctx) ||
                        expressionInfo.getAdvisedCflowClassFilterExpression().match(ctx)) {
                        if (AspectWerkzPreProcessor.DETAILS) {
                            System.out.println(
                                    "[TRACE - earlymatch: " + expressionInfo + " @ "
                                    + aspectDef.getQualifiedName() + "/" +
                                    adviceDef.getName()
                            );
                            System.out.println("[       for          " + ctx.getReflectionInfo());
                            System.out.println("[       within       " + ctx.getWithinReflectionInfo());
                            System.out.println("[       type         " + ctx.getPointcutType().toString());
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class is advised.
     *
     * @param ctx the expression context
     * @return boolean
     */
    public boolean isAdvised(final ExpressionContext ctx) {
        if (ctx == null) {
            throw new IllegalArgumentException("context can not be null");
        }
        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition) it.next();
            List advices = aspectDef.getAdviceDefinitions();
            for (Iterator it2 = advices.iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition) it2.next();
                final ExpressionInfo expressionInfo = adviceDef.getExpressionInfo();
                if (expressionInfo == null) {
                    continue;
                }
                if (expressionInfo.getAdvisedClassFilterExpression().match(ctx) ||
                    expressionInfo.getAdvisedCflowClassFilterExpression().match(ctx)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class has an mixin.
     *
     * @param ctxs an array with the expression contexts
     * @return boolean
     */
    public boolean hasMixin(final ExpressionContext[] ctxs) {
        if (ctxs == null) {
            throw new IllegalArgumentException("context array can not be null");
        }
        for (Iterator it = m_mixinMap.values().iterator(); it.hasNext();) {
            MixinDefinition introDef = (MixinDefinition) it.next();
            ExpressionInfo[] expressionInfos = introDef.getExpressionInfos();
            for (int i = 0; i < expressionInfos.length; i++) {
                ExpressionInfo expressionInfo = expressionInfos[i];
                for (int j = 0; j < ctxs.length; j++) {
                    if (expressionInfo.getExpression().match(ctxs[j])) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class has an mixin.
     *
     * @param ctx the expression context
     * @return boolean
     */
    public boolean hasMixin(final ExpressionContext ctx) {
        if (ctx == null) {
            throw new IllegalArgumentException("context can not be null");
        }
        for (Iterator it = m_mixinMap.values().iterator(); it.hasNext();) {
            MixinDefinition introDef = (MixinDefinition) it.next();
            ExpressionInfo[] expressionInfos = introDef.getExpressionInfos();
            for (int i = 0; i < expressionInfos.length; i++) {
                ExpressionInfo expressionInfo = expressionInfos[i];
                if (expressionInfo.getExpression().match(ctx)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class is advised with an interface introduction.
     *
     * @param ctxs the expression contexts
     * @return boolean
     */
    public boolean hasIntroducedInterface(final ExpressionContext[] ctxs) {
        if (ctxs == null) {
            throw new IllegalArgumentException("context array can not be null");
        }
        for (Iterator iterator = m_aspectMap.values().iterator(); iterator.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition) iterator.next();
            for (Iterator it = aspectDef.getInterfaceIntroductionDefinitions().iterator(); it.hasNext();) {
                InterfaceIntroductionDefinition introDef = (InterfaceIntroductionDefinition) it.next();
                ExpressionInfo[] expressionInfos = introDef.getExpressionInfos();
                for (int i = 0; i < expressionInfos.length; i++) {
                    ExpressionInfo expressionInfo = expressionInfos[i];
                    for (int j = 0; j < ctxs.length; j++) {
                        if (expressionInfo.getExpression().match(ctxs[i])) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class is advised with an interface introduction.
     *
     * @param ctx the expression context
     * @return boolean
     */
    public boolean hasIntroducedInterface(final ExpressionContext ctx) {
        if (ctx == null) {
            throw new IllegalArgumentException("context can not be null");
        }
        for (Iterator iterator = m_aspectMap.values().iterator(); iterator.hasNext();) {
            AspectDefinition aspectDefinition = (AspectDefinition) iterator.next();
            for (Iterator it = aspectDefinition.getInterfaceIntroductionDefinitions().iterator(); it.hasNext();) {
                InterfaceIntroductionDefinition introDef = (InterfaceIntroductionDefinition) it.next();
                ExpressionInfo[] expressionInfos = introDef.getExpressionInfos();
                for (int i = 0; i < expressionInfos.length; i++) {
                    ExpressionInfo expressionInfo = expressionInfos[i];
                    if (expressionInfo.getExpression().match(ctx)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns a collection with all deployment scopes in the system.
     *
     * @return a collection with all deployment scopes  in the system
     */
    public Collection getDeploymentScopes() {
        return m_deploymentScopes.values();
    }

    /**
     * Returns the deployment scope with the name specified.
     *
     * @param name the name of the deployment scope
     * @return the deployment scope with the name specified
     */
    public DeploymentScope getDeploymentScope(final String name) {
        return (DeploymentScope) m_deploymentScopes.get(name);
    }

    /**
     * Adds a deployment scope to the system.
     *
     * @param deploymentScope the deployment scope
     */
    public void addDeploymentScope(final DeploymentScope deploymentScope) {
        m_deploymentScopes.put(deploymentScope.getName(), deploymentScope);
    }

    public boolean equals(Object o) {
        return ((SystemDefinition) o).m_uuid.equals(m_uuid);
    }

    public int hashCode() {
        return m_uuid.hashCode();
    }
}