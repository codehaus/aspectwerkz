/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import gnu.trove.TObjectIntHashMap;
import org.codehaus.aspectwerkz.aspect.CFlowSystemAspect;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.expression.ExpressionInfo;
import org.codehaus.aspectwerkz.expression.ExpressionVisitor;
import org.codehaus.aspectwerkz.util.SequencedHashMap;
import org.codehaus.aspectwerkz.transform.AspectWerkzPreProcessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstraction of the system definition, defines the aspect system.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class SystemDefinition {
    public static final String PER_JVM = "perJVM";

    public static final String PER_CLASS = "perClass";

    public static final String PER_INSTANCE = "perInstance";

    public static final String PER_THREAD = "perThread";

    /**
     * Empty hash map.
     */
    public static final Map EMPTY_HASH_MAP = new HashMap();

    /**
     * Holds the indexes for the aspects. The aspect indexes are needed here (instead of in the AspectWerkz class like
     * the advice indexes) since they need to be available to the transformers before the AspectWerkz system has been
     * initialized.
     */
    private final TObjectIntHashMap m_aspectIndexes = new TObjectIntHashMap();

    /**
     * Holds the indexes for the mixins. The mixin indexes are needed here (instead of in the AspectWerkz class like the
     * advice indexes) since they need to be available to the transformers before the AspectWerkz system has been
     * initialized.
     */
    private final TObjectIntHashMap m_introductionIndexes = new TObjectIntHashMap();

    /**
     * Maps the aspects to it's name.
     */
    private final Map m_aspectMap = new SequencedHashMap();

    /**
     * Maps the mixins to it's name.
     */
    private final Map m_introductionMap = new HashMap();

    /**
     * Maps the interface mixins to it's name.
     */
    private final Map m_interfaceIntroductionMap = new HashMap();

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
     * The parameters passed to the aspects.
     */
    private final Map m_parametersToAspects = new HashMap();

    /**
     * Creates a new instance, creates and sets the system cflow aspect.
     */
    public SystemDefinition(final String uuid) {
        setUuid(uuid);
        AspectDefinition systemAspect = new AspectDefinition(
                CFlowSystemAspect.CLASS_NAME,
                CFlowSystemAspect.CLASS_NAME,
                m_uuid
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
     * Returns a collection with the introduction definitions registered.
     *
     * @return the introduction definitions
     */
    public Collection getIntroductionDefinitions() {
        Collection clone = new ArrayList(m_introductionMap.size());
        for (Iterator it = m_introductionMap.values().iterator(); it.hasNext();) {
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
            adviceDefs.addAll(aspectDef.getAroundAdvices());
            adviceDefs.addAll(aspectDef.getBeforeAdvices());
            adviceDefs.addAll(aspectDef.getAfterAdvices());
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
     * Returns a specific advice definition.
     *
     * @param name the name of the advice definition
     * @return the advice definition
     */
    public AdviceDefinition getAdviceDefinition(final String name) {
        Collection adviceDefs = getAdviceDefinitions();
        for (Iterator it = adviceDefs.iterator(); it.hasNext();) {
            AdviceDefinition adviceDef = (AdviceDefinition) it.next();
            if (adviceDef.getName().equals(name)) {
                return adviceDef;
            }
        }
        return null;
    }

    /**
     * Returns the introduction definitions for a specific class.
     *
     * @param ctx the expression context
     * @return a list with the introduction definitions
     */
    public List getIntroductionDefinitions(final ExpressionContext ctx) {
        final List introDefs = new ArrayList();
        for (Iterator it = m_introductionMap.values().iterator(); it.hasNext();) {
            IntroductionDefinition introDef = (IntroductionDefinition) it.next();
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
        for (Iterator it = m_interfaceIntroductionMap.values().iterator(); it.hasNext();) {
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
        return interfaceIntroductionDefs;
    }

    /**
     * Returns the index for a specific introduction.
     *
     * @param aspectName the name of the aspect
     * @return the index
     */
    public int getAspectIndexByName(final String aspectName) {
        if (aspectName == null) {
            throw new IllegalArgumentException("aspect name can not be null");
        }
        int index = m_aspectIndexes.get(aspectName);
        if (index < 1) {
            throw new RuntimeException(
                    "aspect [" + aspectName + "] does not exist, failed in retrieving aspect index"
            );
        }
        return index;
    }

    /**
     * Returns the index for a specific introduction.
     *
     * @param mixinName the name of the mixin
     * @return the index
     */
    public int getMixinIndexByName(final String mixinName) {
        if (mixinName == null) {
            throw new IllegalArgumentException("mixin name can not be null");
        }
        int index = m_introductionIndexes.get(mixinName);
        if (index < 1) {
            throw new RuntimeException("mixin [" + mixinName + "] does not exist, failed in retrieving mixin index");
        }
        return index;
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
        if (m_aspectIndexes.containsKey(aspectDef.getName())) {
            return;
        }
        synchronized (m_aspectMap) {
            synchronized (m_aspectIndexes) {
                final int index = m_aspectMap.values().size() + 1;
                m_aspectIndexes.put(aspectDef.getName(), index);
                m_aspectMap.put(aspectDef.getName(), aspectDef);
            }
        }
    }

    /**
     * Adds a new mixin definition.
     *
     * @param introDef the mixin definition
     */
    public void addIntroductionDefinition(final IntroductionDefinition introDef) {
        if (introDef == null) {
            throw new IllegalArgumentException("introduction definition can not be null");
        }
        if (m_introductionIndexes.containsKey(introDef.getName())) {
            IntroductionDefinition def = (IntroductionDefinition) m_introductionMap.get(introDef.getName());
            def.addExpressionInfos(introDef.getExpressionInfos());
            return;
        }
        synchronized (m_introductionMap) {
            synchronized (m_introductionIndexes) {
                final int index = m_introductionMap.values().size() + 1;
                m_introductionIndexes.put(introDef.getName(), index);
                m_introductionMap.put(introDef.getName(), introDef);
            }
        }
    }

    /**
     * Adds a new pure interface mixin definition.
     *
     * @param introDef the mixin definition
     */
    public void addInterfaceIntroductionDefinition(final InterfaceIntroductionDefinition introDef) {
        if (introDef == null) {
            throw new IllegalArgumentException("introduction definition can not be null");
        }
        synchronized (m_interfaceIntroductionMap) {
            m_interfaceIntroductionMap.put(introDef.getName(), introDef);
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
     * Checks if there exists an advice with the name specified.
     *
     * @param name the name of the advice
     * @return boolean
     */
    public boolean hasAdvice(final String name) {
        Collection adviceDefs = getAdviceDefinitions();
        for (Iterator it = adviceDefs.iterator(); it.hasNext();) {
            AdviceDefinition adviceDef = (AdviceDefinition) it.next();
            if (adviceDef.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if there exists an introduction with the name specified.
     *
     * @param name the name of the introduction
     * @return boolean
     */
    public boolean hasIntroduction(final String name) {
        return m_introductionMap.containsKey(name);
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
     * Checks if a class has an introduction.
     *
     * @param ctx the expression context
     * @return boolean
     */
    public boolean hasIntroduction(final ExpressionContext ctx) {
        if (ctx == null) {
            throw new IllegalArgumentException("context can not be null");
        }
        for (Iterator it = m_introductionMap.values().iterator(); it.hasNext();) {
            IntroductionDefinition introDef = (IntroductionDefinition) it.next();
            ExpressionInfo[] expressionInfos = introDef.getExpressionInfos();
            for (int i = 0; i < expressionInfos.length; i++) {
                ExpressionInfo expressionInfo = expressionInfos[i];
                ExpressionVisitor expression = expressionInfo.getExpression();
                if (expression.match(ctx)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a method has an pointcut.
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
            for (Iterator it2 = aspectDef.getAllAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition) it2.next();
                ExpressionVisitor expression = adviceDef.getExpressionInfo().getExpression();

                if (expression.match(ctx)) {
                    if (AspectWerkzPreProcessor.VERBOSE) {
                        System.out.println(
                                "match: " + expression.toString() + " @ " + aspectDef.getName() + "/" +
                                adviceDef.getName()
                        );
                        System.out.println("\tfor     " + ctx.getReflectionInfo().toString());
                        System.out.println("\twithin  " + ctx.getWithinReflectionInfo().toString());
                        System.out.println("\ttype    " + ctx.getPointcutType().toString());
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
            for (Iterator it2 = aspectDef.getAllAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition) it2.next();
                ExpressionInfo expressionInfo = adviceDef.getExpressionInfo();
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
            List advices = aspectDef.getAllAdvices();
            for (Iterator it2 = advices.iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition) it2.next();
                final ExpressionInfo expressionInfo = adviceDef.getExpressionInfo();
                for (int i = 0; i < ctxs.length; i++) {
                    ExpressionContext ctx = ctxs[i];
                    if (expressionInfo.getAdvisedClassFilterExpression().match(ctx)
                        || expressionInfo.getAdvisedCflowClassFilterExpression().match(ctx)) {
                        if (AspectWerkzPreProcessor.VERBOSE) {
                            System.out.println(
                                    "early match: " + expressionInfo.toString() + " @ " +
                                    aspectDef.getName() +
                                    "/" +
                                    adviceDef.getName()
                            );
                            System.out.println("\tfor    " + ctx.getReflectionInfo());
                            System.out.println("\twithin " + ctx.getWithinReflectionInfo());
                            System.out.println("\ttype   " + ctx.getPointcutType().toString());
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
            List advices = aspectDef.getAllAdvices();
            for (Iterator it2 = advices.iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition) it2.next();
                if (adviceDef.getExpressionInfo().getAdvisedClassFilterExpression().match(ctx)
                    || adviceDef.getExpressionInfo().getAdvisedCflowClassFilterExpression().match(ctx)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class has an introduction.
     *
     * @param ctxs an array with the expression contexts
     * @return boolean
     */
    public boolean isIntroduced(final ExpressionContext[] ctxs) {
        if (ctxs == null) {
            throw new IllegalArgumentException("context array can not be null");
        }
        for (Iterator it = m_introductionMap.values().iterator(); it.hasNext();) {
            IntroductionDefinition introDef = (IntroductionDefinition) it.next();
            ExpressionInfo[] expressionInfos = introDef.getExpressionInfos();
            for (int i = 0; i < expressionInfos.length; i++) {
                ExpressionInfo expressionInfo = expressionInfos[i];
                for (int j = 0; j < ctxs.length; j++) {
                    if (expressionInfo.getAdvisedClassFilterExpression().match(ctxs[j])) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class has an introduction.
     *
     * @param ctx the expression context
     * @return boolean
     */
    public boolean isIntroduced(final ExpressionContext ctx) {
        if (ctx == null) {
            throw new IllegalArgumentException("context can not be null");
        }
        for (Iterator it = m_introductionMap.values().iterator(); it.hasNext();) {
            IntroductionDefinition introDef = (IntroductionDefinition) it.next();
            ExpressionInfo[] expressionInfos = introDef.getExpressionInfos();
            for (int i = 0; i < expressionInfos.length; i++) {
                ExpressionInfo expressionInfo = expressionInfos[i];
                if (expressionInfo.getAdvisedClassFilterExpression().match(ctx)) {
                    return true;
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
    public boolean isInterfaceIntroduced(final ExpressionContext ctx) {
        if (ctx == null) {
            throw new IllegalArgumentException("context can not be null");
        }
        for (Iterator it = m_interfaceIntroductionMap.values().iterator(); it.hasNext();) {
            InterfaceIntroductionDefinition introDef = (InterfaceIntroductionDefinition) it.next();
            ExpressionInfo[] expressionInfos = introDef.getExpressionInfos();
            for (int i = 0; i < expressionInfos.length; i++) {
                ExpressionInfo expressionInfo = expressionInfos[i];
                if (expressionInfo.getAdvisedClassFilterExpression().match(ctx)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Adds a new parameter for the aspect.
     *
     * @param aspectName the name of the aspect
     * @param key        the key
     * @param value      the value
     * @TODO: should perhaps move to the aspect def instead of being separated from the aspect def concept?
     */
    public void addParameter(final String aspectName, final String key, final String value) {
        Map parameters;
        if (m_parametersToAspects.containsKey(aspectName)) {
            parameters = (Map) m_parametersToAspects.get(aspectName);
            parameters.put(key, value);
        } else {
            parameters = new HashMap();
            parameters.put(key, value);
            m_parametersToAspects.put(aspectName, parameters);
        }
    }

    /**
     * Returns parameters for the aspect.
     *
     * @param aspectName the name of the aspect
     * @return parameters
     */
    public Map getParameters(final String aspectName) {
        if (m_parametersToAspects.containsKey(aspectName)) {
            return (Map) m_parametersToAspects.get(aspectName);
        } else {
            return EMPTY_HASH_MAP;
        }
    }
}