/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.inlining.deployer;

import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;
import java.lang.reflect.Method;
import java.io.InputStream;

import org.codehaus.aspectwerkz.expression.ExpressionInfo;
import org.codehaus.aspectwerkz.definition.AspectDefinition;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.definition.SystemDefinitionContainer;
import org.codehaus.aspectwerkz.definition.AdviceDefinition;
import org.codehaus.aspectwerkz.definition.DeploymentScope;
import org.codehaus.aspectwerkz.definition.XmlParser;
import org.codehaus.aspectwerkz.definition.DocumentParser;
import org.codehaus.aspectwerkz.joinpoint.management.AdviceInfoContainer;
import org.codehaus.aspectwerkz.joinpoint.management.JoinPointManager;
import org.codehaus.aspectwerkz.annotation.AspectAnnotationParser;
import org.codehaus.aspectwerkz.reflect.impl.asm.AsmClassInfo;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.transform.inlining.compiler.MatchingJoinPointInfo;
import org.codehaus.aspectwerkz.transform.inlining.compiler.JoinPointFactory;
import org.codehaus.aspectwerkz.transform.inlining.compiler.CompilationInfo;
import org.objectweb.asm.ClassReader;
import org.dom4j.Document;
import org.dom4j.DocumentException;

/**
 * Manages deployment and undeployment of aspects. Aspects can be deployed and undeployed into a running system(s).
 * <p/>
 * Supports annotation defined and XML defined aspects.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public class Deployer {

    /**
     * Deploys an annotation defined aspect.
     * <p/>
     * Deploys the aspect in all systems in the class loader that has loaded the aspect class.
     * <p/>
     * <b>CAUTION</b>: use a to own risk, the aspect might have a wider scope than your set of instrumented join points,
     * then the aspect will not be applied to all intended points, to play safe -
     * use <code>deploy(final Class aspect, final DeploymentScope preparedPointcut)</code>
     *
     * @param aspect the aspect class
     * @return a unique deployment handle for this deployment
     */
    public static DeploymentHandle deploy(final Class aspect) {
        return deploy(aspect, DeploymentScope.MATCH_ALL);
    }

    /**
     * Deploys an annotation defined aspect.
     * <p/>
     * Deploys the aspect in all systems in the class loader that is specified.
     * <p/>
     * <b>CAUTION</b>: use a to own risk, the aspect might have a wider scope than your set of instrumented join points,
     * then the aspect will not be applied to all intended points, to play safe -
     * use <code>deploy(final Class aspect, final DeploymentScope preparedPointcut)</code>
     *
     * @param aspect       the aspect class
     * @param deployLoader
     * @return a unique deployment handle for this deployment
     */
    public static DeploymentHandle deploy(final Class aspect, final ClassLoader deployLoader) {
        return deploy(aspect, DeploymentScope.MATCH_ALL, deployLoader);
    }

    /**
     * Deploys an annotation defined aspect in the scope defined by the prepared pointcut.
     * <p/>
     * Deploys the aspect in all systems in the class loader that has loaded the aspect class.
     *
     * @param aspect          the aspect class
     * @param deploymentScope
     * @return a unique deployment handle for this deployment
     */
    public static DeploymentHandle deploy(final Class aspect, final DeploymentScope deploymentScope) {
        return deploy(aspect, deploymentScope, Thread.currentThread().getContextClassLoader());
    }

    /**
     * TODO allow deployment in other systems than virtual system?
     * <p/>
     * Deploys an annotation defined aspect in the scope defined by the prepared pointcut.
     * <p/>
     * Deploys the aspect in the class loader that is specified.
     *
     * @param aspect          the aspect class
     * @param deploymentScope the prepared pointcut
     * @param deployLoader    the loader to deploy the aspect in
     * @return a unique deployment handle for this deployment
     */
    public static DeploymentHandle deploy(final Class aspect,
                                          final DeploymentScope deploymentScope,
                                          final ClassLoader deployLoader) {
        if (aspect == null) {
            throw new IllegalArgumentException("aspect to deploy can not be null");
        }
        if (deploymentScope == null) {
            throw new IllegalArgumentException("prepared pointcut can not be null");
        }
        if (deployLoader == null) {
            throw new IllegalArgumentException("class loader to deploy aspect in can not be null");
        }

        final String className = aspect.getName();
        logDeployment(className, deployLoader);

        final DeploymentHandle deploymentHandle = new DeploymentHandle(aspect, deployLoader);

        // create a new aspect def and fill it up with the annotation def from the aspect class
        final SystemDefinition systemDef = SystemDefinitionContainer.getVirtualDefinitionFor(deployLoader);
        final AspectDefinition newAspectDef = new AspectDefinition(className, className, systemDef);
        final Set newExpressions = getNewExpressionsForAspect(
                aspect, newAspectDef, systemDef, deploymentScope, deploymentHandle
        );

        redefine(newExpressions);
        return deploymentHandle;
    }

    /**
     * Deploys an XML defined aspect in the scope defined by the prepared pointcut.
     * <p/>
     * If the aspect class has annotations, those will be read but the XML definition will override the
     * annotation definition.
     * <p/>
     * Deploys the aspect in the class loader that has loaded the aspect.
     *
     * @param aspect the aspect class
     * @param xmlDef
     * @return
     */
    public static DeploymentHandle deploy(final Class aspect, final String xmlDef) {
        return deploy(aspect, xmlDef, DeploymentScope.MATCH_ALL);
    }

    /**
     * Deploys an XML defined aspect in the scope defined by the prepared pointcut.
     * <p/>
     * If the aspect class has annotations, those will be read but the XML definition will override the
     * annotation definition.
     * <p/>
     * Deploys the aspect in the class loader that has loaded the aspect.
     *
     * @param aspect          the aspect class
     * @param xmlDef
     * @param deploymentScope
     * @return
     */
    public static DeploymentHandle deploy(final Class aspect,
                                          final String xmlDef,
                                          final DeploymentScope deploymentScope) {
        return deploy(aspect, xmlDef, deploymentScope, aspect.getClassLoader());
    }

    /**
     * Deploys an XML defined aspect in the scope defined by the prepared pointcut.
     * <p/>
     * If the aspect class has annotations, those will be read but the XML definition will override the
     * annotation definition.
     * <p/>
     * Deploys the aspect in the class loader that is specified.
     *
     * @param aspect       the aspect class
     * @param xmlDef
     * @param deployLoader
     * @return
     */
    public static DeploymentHandle deploy(final Class aspect, final String xmlDef, final ClassLoader deployLoader) {
        return deploy(aspect, xmlDef, DeploymentScope.MATCH_ALL, deployLoader);
    }

    /**
     * TODO allow deployment in other systems than virtual system?
     * <p/>
     * Deploys an XML defined aspect in the scope defined by the prepared pointcut.
     * <p/>
     * If the aspect class has annotations, those will be read but the XML definition will override the
     * annotation definition.
     * <p/>
     * Deploys the aspect in the class loader that is specified.
     *
     * @param aspect          the aspect class
     * @param deploymentScope
     * @param xmlDef
     * @param deployLoader
     * @return
     */
    public static DeploymentHandle deploy(final Class aspect,
                                          final String xmlDef,
                                          final DeploymentScope deploymentScope,
                                          final ClassLoader deployLoader) {
        if (aspect == null) {
            throw new IllegalArgumentException("aspect to deploy can not be null");
        }
        if (deploymentScope == null) {
            throw new IllegalArgumentException("prepared pointcut can not be null");
        }
        if (xmlDef == null) {
            throw new IllegalArgumentException("xml definition can not be null");
        }
        if (deployLoader == null) {
            throw new IllegalArgumentException("class loader to deploy aspect in can not be null");
        }
        final String className = aspect.getName();
        logDeployment(className, deployLoader);

        final DeploymentHandle deploymentHandle = new DeploymentHandle(aspect, deployLoader);

        final SystemDefinition systemDef = SystemDefinitionContainer.getVirtualDefinitionFor(deployLoader);
        try {
            final Document document = XmlParser.createDocument(xmlDef);
            final AspectDefinition newAspectDef = DocumentParser.parseAspectDefinition(document, systemDef, aspect);
            final Set newExpressions = getNewExpressionsForAspect(
                    aspect, newAspectDef, systemDef, deploymentScope, deploymentHandle
            );

            redefine(newExpressions);
        } catch (DocumentException e) {
            throw new DefinitionException("XML definition for aspect is not well-formed: " + xmlDef);
        }
        return deploymentHandle;
    }

    /**
     * Undeploys an aspect from the same loader that has loaded the class.
     *
     * @param aspect the aspect class
     */
    public static void undeploy(final Class aspect) {
        undeploy(aspect, aspect.getClassLoader());
    }

    /**
     * Undeploys an aspect from a specific class loader.
     *
     * @param aspect the aspect class
     * @param loader the loader that you want to undeploy the aspect from
     */
    public static void undeploy(final Class aspect, final ClassLoader loader) {
        if (aspect == null) {
            throw new IllegalArgumentException("aspect to undeploy can not be null");
        }
        if (loader == null) {
            throw new IllegalArgumentException("loader to undeploy aspect from can not be null");
        }

        final String className = aspect.getName();
        logUndeployment(className, loader);

        Set systemDefs = SystemDefinitionContainer.getRegularAndVirtualDefinitionsFor(loader);
        for (Iterator it = systemDefs.iterator(); it.hasNext();) {
            SystemDefinition systemDef = (SystemDefinition) it.next();
            final AspectDefinition aspectDef = systemDef.getAspectDefinition(className);
            if (aspectDef != null) {

                final Set newExpressions = new HashSet();
                for (Iterator it2 = aspectDef.getAdviceDefinitions().iterator(); it2.hasNext();) {
                    AdviceDefinition adviceDef = (AdviceDefinition) it2.next();
                    ExpressionInfo oldExpression = adviceDef.getExpressionInfo();
                    if (oldExpression == null) { // if null, then already undeployed
                        continue;
                    }
                    adviceDef.setExpressionInfo(null);
                    newExpressions.add(oldExpression);
                }
                redefine(newExpressions);
            }
        }
    }

    /**
     * Undeploys an aspect in the same way that it has been deployed in in the previous deploy event
     * defined by the deployment handle.
     *
     * @param deploymentHandle the handle to the previous deployment event
     */
    public static void undeploy(final DeploymentHandle deploymentHandle) {
        if (deploymentHandle == null) {
            throw new IllegalArgumentException("deployment handle can not be null");
        }

        deploymentHandle.revertChanges();

        final Class aspectClass = deploymentHandle.getAspectClass();
        if (aspectClass == null) {
            return; // already undeployed
        }
        undeploy(aspectClass);
    }

    /**
     * Redefines all join points that are affected by the system redefinition.
     *
     * @param expressions the expressions that will pick out the join points that are affected
     */
    private synchronized static void redefine(final Set expressions) {
        final Set allMatchingJoinPoints = new HashSet();
        for (Iterator itExpr = expressions.iterator(); itExpr.hasNext();) {
            ExpressionInfo expression = (ExpressionInfo) itExpr.next();
            Set matchingJoinPoints = JoinPointFactory.getJoinPointsMatching(expression);
            allMatchingJoinPoints.addAll(matchingJoinPoints);
        }

        final ChangeSet changeSet = new ChangeSet();
        for (Iterator it = allMatchingJoinPoints.iterator(); it.hasNext();) {
            final MatchingJoinPointInfo joinPointInfo = (MatchingJoinPointInfo) it.next();

            final CompilationInfo compilationInfo = joinPointInfo.getCompilationInfo();
            compilationInfo.incrementRedefinitionCounter();

            changeSet.addElement(new ChangeSet.Element(compilationInfo, joinPointInfo));
        }

        doRedefine(changeSet);
    }

    /**
     * Do the redefinition of the existing join point and the compilation of the new join point.
     *
     * @param changeSet
     */
    private static void doRedefine(final ChangeSet changeSet) {
        for (Iterator it = changeSet.getElements().iterator(); it.hasNext();) {
            compileNewJoinPoint((ChangeSet.Element) it.next());
        }
        redefineInitialJoinPoints(changeSet);
    }

    /**
     * Compiles a completely new join point instance based on the new redefined model.
     *
     * @param changeSetElement the change set item
     */
    private static void compileNewJoinPoint(final ChangeSet.Element changeSetElement) {
        final CompilationInfo compilationInfo = changeSetElement.getCompilationInfo();
        final MatchingJoinPointInfo joinPointInfo = changeSetElement.getJoinPointInfo();
        final ClassLoader loader = joinPointInfo.getJoinPointClass().getClassLoader();
        final AdviceInfoContainer newAdviceContainer = JoinPointManager.getAdviceInfoContainerForJoinPoint(
                joinPointInfo.getExpressionContext(),
                loader
        );
        final CompilationInfo.Model redefinedModel = new CompilationInfo.Model(
                compilationInfo.getInitialModel().getEmittedJoinPoint(), // copy the reference since it is the same
                newAdviceContainer,
                compilationInfo.getRedefinitionCounter()
        );
        JoinPointFactory.compileJoinPointAndAttachToClassLoader(redefinedModel, loader);

        compilationInfo.setRedefinedModel(redefinedModel);
        JoinPointFactory.addCompilationInfo(joinPointInfo.getJoinPointClass(), compilationInfo);
    }

    /**
     * Redefines the intial (weaved in) join point to delegate to the newly compiled "real" join point which is
     * based on the new redefined model.
     *
     * @param changeSet the change set
     */
    private static void redefineInitialJoinPoints(final ChangeSet changeSet) {
        // TODO type should be pluggable
        RedefinerFactory.newRedefiner(RedefinerFactory.Type.HOTSWAP).redefine(changeSet);
    }

    /**
     * Returns a set with the new expressions for the advice in the aspect to deploy.
     *
     * @param aspectClass      s     * @param newAspectDef
     * @param systemDef
     * @param deploymentScope
     * @param deploymentHandle
     * @return a set with the new expressions
     */
    private static Set getNewExpressionsForAspect(final Class aspectClass,
                                                  final AspectDefinition newAspectDef,
                                                  final SystemDefinition systemDef,
                                                  final DeploymentScope deploymentScope,
                                                  final DeploymentHandle deploymentHandle) {
        final ClassLoader aspectLoader = aspectClass.getClassLoader();
        final String aspectName = aspectClass.getName();

        AspectAnnotationParser.parse(
                AsmClassInfo.getClassInfo(aspectName, aspectLoader),
                newAspectDef,
                aspectLoader
        );

        AspectDefinition aspectDef = systemDef.getAspectDefinition(aspectName);
        if (aspectDef != null) {
            // if in def already reuse some of the settings that can have been overridded by XML def
            newAspectDef.setContainerClassName(aspectDef.getContainerClassName());
            newAspectDef.setDeploymentModel(aspectDef.getDeploymentModel());
        }

        systemDef.addAspectOverwriteIfExists(newAspectDef);

        final Set newExpressions = new HashSet();
        for (Iterator it2 = newAspectDef.getAdviceDefinitions().iterator(); it2.hasNext();) {
            AdviceDefinition adviceDef = (AdviceDefinition) it2.next();
            ExpressionInfo oldExpression = adviceDef.getExpressionInfo();
            if (oldExpression == null) {
                continue;
            }
            deploymentHandle.registerDefinitionChange(adviceDef, oldExpression);

            final ExpressionInfo newExpression = deploymentScope.newExpressionInfo(oldExpression);
            adviceDef.setExpressionInfo(newExpression);
            newExpressions.add(newExpression);
        }
        return newExpressions;
    }

    /**
     * Imports a class from one class loader to another one.
     *
     * @param clazz    the class to import
     * @param toLoader the loader to import to
     */
    private static void importClassIntoLoader(final Class clazz, final ClassLoader toLoader) {
        final ClassLoader fromLoader = clazz.getClassLoader();
        if (toLoader == fromLoader) {
            return;
        }
        final String className = clazz.getName();
        try {
            toLoader.loadClass(className);
        } catch (ClassNotFoundException cnfe) {
            try {
                InputStream stream = null;
                byte[] bytes;
                try {
                    stream = fromLoader.getResourceAsStream(className.replace('.', '/') + ".class");
                    bytes = new ClassReader(stream).b;
                } finally {
                    try { stream.close();} catch(Exception e) {;}
                }
                Class klass = toLoader.loadClass("java.lang.ClassLoader");
                Method method = klass.getDeclaredMethod(
                        "defineClass",
                        new Class[]{String.class, byte[].class, int.class, int.class}
                );
                method.setAccessible(true);
                Object[] args = new Object[]{
                    clazz.getName(), bytes, new Integer(0), new Integer(bytes.length)
                };
                method.invoke(toLoader, args);
                method.setAccessible(false);
            } catch (Exception e) {
                throw new RuntimeException(
                        new StringBuffer().append("could not deploy aspect [").
                        append(className).append("] in class loader [").append(toLoader)
                        .append(']').toString()
                );
            }
        }
    }

    /**
     * Logs undeployment.
     * <p/>
     * TODO unified way or at least format for logging
     *
     * @param className
     * @param loader
     */
    private static void logUndeployment(final String className, final ClassLoader loader) {
        System.out.println(
                new StringBuffer().append("Deployer::INFO - undeploying aspect [").
                append(className).append("] from class loader [").
                append(loader).append(']').toString()
        );
    }

    /**
     * Logs deployment.
     * <p/>
     * TODO unified way or at least format for logging
     *
     * @param className
     * @param loader
     */
    private static void logDeployment(final String className, final ClassLoader loader) {
        System.out.println(
                new StringBuffer().append("Deployer::INFO - deploying aspect [").
                append(className).append("] in class loader [").
                append(loader).append(']').toString()
        );
    }
}
