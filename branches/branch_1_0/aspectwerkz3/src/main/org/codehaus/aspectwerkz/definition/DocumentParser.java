/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.util.Strings;
import org.codehaus.aspectwerkz.aspect.AdviceType;
import org.codehaus.aspectwerkz.reflect.impl.java.JavaClassInfo;
import org.codehaus.aspectwerkz.expression.regexp.Pattern;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.annotation.AspectAnnotationParser;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.transform.ReflectHelper;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Parses the XML definition using <tt>dom4j</tt>.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class DocumentParser {

    /**
     * Parses aspect class names.
     *
     * @param document the defintion as a document
     * @return the aspect class names
     */
    public static List parseAspectClassNames(final Document document) {
        final List aspectClassNames = new ArrayList();
        for (Iterator it1 = document.getRootElement().elementIterator("system"); it1.hasNext();) {
            Element system = (Element) it1.next();
            final String packageName = getBasePackage(system);
            for (Iterator it11 = system.elementIterator("aspect"); it11.hasNext();) {
                String className = null;
                Element aspect = (Element) it11.next();
                for (Iterator it2 = aspect.attributeIterator(); it2.hasNext();) {
                    Attribute attribute = (Attribute) it2.next();
                    final String name = attribute.getName().trim();
                    final String value = attribute.getValue().trim();
                    if (name.equalsIgnoreCase("class")) {
                        className = value;
                    }
                }
                String aspectClassName = packageName + className;
                aspectClassNames.add(aspectClassName);
            }
            for (Iterator it11 = system.elementIterator("package"); it11.hasNext();) {
                final Element packageElement = ((Element) it11.next());
                final String packageName1 = getPackage(packageElement);
                for (Iterator it12 = packageElement.elementIterator("aspect"); it12.hasNext();) {
                    String className = null;
                    Element aspect = (Element) it12.next();
                    for (Iterator it2 = aspect.attributeIterator(); it2.hasNext();) {
                        Attribute attribute = (Attribute) it2.next();
                        final String name = attribute.getName().trim();
                        final String value = attribute.getValue().trim();
                        if (name.equalsIgnoreCase("class")) {
                            className = value;
                        }
                    }
                    String aspectClassName = packageName1 + className;
                    aspectClassNames.add(aspectClassName);
                }
            }
        }
        return aspectClassNames;
    }

    /**
     * Parses the definition DOM document.
     *
     * @param loader   the current class loader
     * @param document the defintion as a document
     * @return the definitions
     */
    public static List parse(final ClassLoader loader, final Document document) {
        final Element root = document.getRootElement();

        // parse the transformation scopes
        long s = System.currentTimeMillis();
        List o = parseSystemElements(loader, root);
        System.out.println("DocumentParser.parse " + (System.currentTimeMillis()-s));
        return o;
    }

    /**
     * Parses the <tt>system</tt> elements.
     *
     * @param loader the current class loader
     * @param root   the root element
     */
    private static List parseSystemElements(final ClassLoader loader, final Element root) {
        final List systemDefs = new ArrayList();
        for (Iterator it1 = root.elementIterator("system"); it1.hasNext();) {
            Element system = (Element) it1.next();
            SystemDefinition definition = parseSystemElement(loader, system, getBasePackage(system));
            if (definition != null) {
                systemDefs.add(definition);
            }
        }
        return systemDefs;
    }

    /**
     * Parses the <tt>system</tt> elements.
     *
     * @param loader        the current class loader
     * @param systemElement the system element
     * @param basePackage   the base package
     * @return the definition for the system
     */
    private static SystemDefinition parseSystemElement(final ClassLoader loader,
                                                       final Element systemElement,
                                                       final String basePackage) {
        String uuid = systemElement.attributeValue("id");
        if ((uuid == null) || uuid.equals("")) {
            throw new DefinitionException("system UUID must be specified");
        }
        final SystemDefinition definition = new SystemDefinition(uuid);

        // parse the global pointcuts
        List globalPointcuts = parseGlobalPointcuts(systemElement);

        // parse the include, exclude and prepare elements
        parseIncludePackageElements(systemElement, definition, basePackage);
        parseExcludePackageElements(systemElement, definition, basePackage);
        parsePrepareElements(systemElement, definition, basePackage);

        // parse without package elements
        parseAspectElements(loader, systemElement, definition, basePackage, globalPointcuts);

        // parse with package elements
        parsePackageElements(loader, systemElement, definition, basePackage, globalPointcuts);
        return definition;
    }

    /**
     * Parses the global pointcuts.
     *
     * @param systemElement the system element
     * @return a list with the pointcuts
     */
    private static List parseGlobalPointcuts(final Element systemElement) {
        final List globalPointcuts = new ArrayList();
        for (Iterator it11 = systemElement.elementIterator("pointcut"); it11.hasNext();) {
            PointcutInfo pointcutInfo = new PointcutInfo();
            Element globalPointcut = (Element) it11.next();
            for (Iterator it2 = globalPointcut.attributeIterator(); it2.hasNext();) {
                Attribute attribute = (Attribute) it2.next();
                final String name = attribute.getName().trim();
                final String value = attribute.getValue().trim();
                if (name.equalsIgnoreCase("name")) {
                    pointcutInfo.name = value;
                } else if (name.equalsIgnoreCase("expression")) {
                    pointcutInfo.expression = value;
                }
            }
            // pointcut CDATA is expression unless already specified as an attribute
            if (pointcutInfo.expression == null) {
                pointcutInfo.expression = globalPointcut.getTextTrim();
            }
            globalPointcuts.add(pointcutInfo);
        }
        return globalPointcuts;
    }

    /**
     * Parses the definition DOM document.
     *
     * @param loader          the current class loader
     * @param systemElement   the system element
     * @param definition      the definition
     * @param basePackage     the base package
     * @param globalPointcuts the global pointcuts
     */
    private static void parsePackageElements(final ClassLoader loader,
                                             final Element systemElement,
                                             final SystemDefinition definition,
                                             final String basePackage,
                                             final List globalPointcuts) {
        for (Iterator it1 = systemElement.elementIterator("package"); it1.hasNext();) {
            final Element packageElement = ((Element) it1.next());
            final String packageName = basePackage + getPackage(packageElement);
            parseAspectElements(loader, packageElement, definition, packageName, globalPointcuts);
        }
    }

    /**
     * Parses the <tt>aspect</tt> elements.
     *
     * @param loader          the current class loader
     * @param systemElement   the system element
     * @param definition      the definition object
     * @param packageName     the package name
     * @param globalPointcuts the global pointcuts
     */
    private static void parseAspectElements(final ClassLoader loader,
                                            final Element systemElement,
                                            final SystemDefinition definition,
                                            final String packageName,
                                            final List globalPointcuts) {
        for (Iterator it1 = systemElement.elementIterator("aspect"); it1.hasNext();) {
            String aspectName = null;
            String className = null;
            String deploymentModel = null;
            String containerClassName = null;
            Element aspect = (Element) it1.next();
            for (Iterator it2 = aspect.attributeIterator(); it2.hasNext();) {
                Attribute attribute = (Attribute) it2.next();
                final String name = attribute.getName().trim();
                final String value = attribute.getValue().trim();
                if (name.equalsIgnoreCase("class")) {
                    className = value;
                } else if (name.equalsIgnoreCase("deployment-model")) {
                    deploymentModel = value;
                } else if (name.equalsIgnoreCase("name")) {
                    aspectName = value;
                } else if (name.equalsIgnoreCase("container")) {
                    containerClassName = value;
                }
            }
            String aspectClassName = packageName + className;
            if (aspectName == null) {
                aspectName = aspectClassName;
            }

            // create the aspect definition
            AspectDefinition aspectDef = new AspectDefinition(aspectName, aspectClassName, definition.getUuid());
            Class aspectClass;
            try {
                aspectClass = loadAspectClass(loader, aspectClassName);
            } catch (Exception e) {
                System.out.println("loader: " + loader);
                System.out.println("aspectClassName: " + aspectClassName);
                System.err.println(
                        "Warning: could not load aspect "
                        + aspectClassName
                        + " from "
                        + loader
                        + "due to: "
                        + e.toString()
                );
                e.printStackTrace();
                continue;
            }

            // add the global pointcuts to the aspect
            for (Iterator it = globalPointcuts.iterator(); it.hasNext();) {
                PointcutInfo pointcutInfo = (PointcutInfo) it.next();
                DefinitionParserHelper.createAndAddPointcutDefToAspectDef(
                        pointcutInfo.name,
                        pointcutInfo.expression,
                        aspectDef
                );
            }
            parsePointcutElements(aspect, aspectDef); //needed to support undefined named pointcut
            // in Attributes AW-152
            AspectAnnotationParser.parse(aspectClass, aspectDef, definition);

            // XML definition settings always overrides attribute definition settings
            aspectDef.setDeploymentModel(deploymentModel);
            aspectDef.setName(aspectName);
            aspectDef.setContainerClassName(containerClassName);

            // parse the aspect info
            parseParameterElements(aspect, definition, aspectDef);
            parsePointcutElements(aspect, aspectDef); //reparse pc for XML override (AW-152)
            parseAdviceElements(aspect, aspectDef, aspectClass);
            parseIntroductionElements(aspect, aspectDef, aspectClass, packageName);

            // register introduction of aspect into the system
            for (Iterator mixins = aspectDef.getInterfaceIntroductions().iterator(); mixins.hasNext();) {
                definition.addInterfaceIntroductionDefinition((InterfaceIntroductionDefinition) mixins.next());
            }
            for (Iterator mixins = aspectDef.getIntroductions().iterator(); mixins.hasNext();) {
                definition.addIntroductionDefinition((IntroductionDefinition) mixins.next());
            }
            definition.addAspect(aspectDef);
        }
    }

    /**
     * Loads the aspect class.
     *
     * @param loader          the class loader
     * @param aspectClassName the name of the class implementing the aspect
     * @return the class
     */
    private static Class loadAspectClass(final ClassLoader loader, final String aspectClassName) {
        Class aspectClass;
        try {
            aspectClass = loader.loadClass(aspectClassName);
        } catch (Exception e) {
            e.printStackTrace();
            throw new WrappedRuntimeException(e);
        }
        return aspectClass;
    }

    /**
     * Parses the aspectElement parameters. <p/>TODO: should perhaps move the parameters to the aspect def instead of
     * the system def
     *
     * @param aspectElement the aspect element
     * @param def           the system definition
     * @param aspectDef     the aspect def
     */
    private static void parseParameterElements(final Element aspectElement,
                                               final SystemDefinition def,
                                               final AspectDefinition aspectDef) {
        for (Iterator it2 = aspectElement.elementIterator(); it2.hasNext();) {
            Element parameterElement = (Element) it2.next();
            if (parameterElement.getName().trim().equals("param")) {
                def.addParameter(
                        aspectDef.getName(), parameterElement.attributeValue("name"), parameterElement
                                                                                      .attributeValue("value")
                );
            }
        }
    }

    /**
     * Parses the pointcuts.
     *
     * @param aspectElement the aspect element
     * @param aspectDef     the system definition
     */
    private static void parsePointcutElements(final Element aspectElement, final AspectDefinition aspectDef) {
        for (Iterator it2 = aspectElement.elementIterator(); it2.hasNext();) {
            Element pointcutElement = (Element) it2.next();
            if (pointcutElement.getName().trim().equals("pointcut")) {
                String name = pointcutElement.attributeValue("name");
                String expression = pointcutElement.attributeValue("expression");
                // pointcut CDATA is expression unless already specified as an attribute
                if (expression == null) {
                    expression = pointcutElement.getTextTrim();
                }
                DefinitionParserHelper.createAndAddPointcutDefToAspectDef(name, expression, aspectDef);
            }
        }
    }

    /**
     * Parses the advices.
     *
     * @param aspectElement the aspect element
     * @param aspectDef     the system definition
     * @param aspectClass   the aspect class
     */
    private static void parseAdviceElements(final Element aspectElement,
                                            final AspectDefinition aspectDef,
                                            final Class aspectClass) {
        List methodList = ReflectHelper.createCompleteSortedMethodList(aspectClass);
        for (Iterator it2 = aspectElement.elementIterator(); it2.hasNext();) {
            Element adviceElement = (Element) it2.next();
            if (adviceElement.getName().trim().equals("advice")) {
                String name = adviceElement.attributeValue("name");
                String type = adviceElement.attributeValue("type");
                String bindTo = adviceElement.attributeValue("bind-to");
                String adviceName = /*aspectClass.getName() + '.' +*/ name;
                int methodIndex = 0;
                Method method = null;
                for (Iterator it3 = methodList.iterator(); it3.hasNext(); methodIndex++) {
                    Method methodCurrent = (Method) it3.next();
                    //if (methodCurrent.getName().equals(name)) {
                    if (matchMethodAsAdvice(methodCurrent, name)) {
                        method = methodCurrent;
                        break;
                    }
                }
                if (method == null) {
                    throw new DefinitionException(
                            "Could not find advice method " + name + " in " + aspectClass.getName()
                    );
                }
                createAndAddAdviceDefsToAspectDef(type, bindTo, adviceName, method, methodIndex, aspectDef);
                for (Iterator it1 = adviceElement.elementIterator("bind-to"); it1.hasNext();) {
                    Element bindToElement = (Element) it1.next();
                    String pointcut = bindToElement.attributeValue("pointcut");
                    createAndAddAdviceDefsToAspectDef(type, pointcut, adviceName, method, methodIndex, aspectDef);
                }
            }
        }
    }

    /**
     * Parses the introduction.
     *
     * @param aspectElement the aspect element
     * @param aspectDef     the system definition
     * @param aspectClass   the aspect class
     * @param packageName
     */
    private static void parseIntroductionElements(final Element aspectElement,
                                                  final AspectDefinition aspectDef,
                                                  final Class aspectClass,
                                                  final String packageName) {
        for (Iterator it2 = aspectElement.elementIterator(); it2.hasNext();) {
            Element introduceElement = (Element) it2.next();
            if (introduceElement.getName().trim().equals("introduce")) {
                String klass = introduceElement.attributeValue("class");
                String name = introduceElement.attributeValue("name");
                String bindTo = introduceElement.attributeValue("bind-to");
                String deploymentModel = introduceElement.attributeValue("deployment-model");

                // deployment-model defaults to perJVM
                if ((deploymentModel == null) || (deploymentModel.length() <= 0)) {
                    deploymentModel = DeploymentModel.getDeploymentModelAsString(DeploymentModel.PER_JVM);
                }

                // default name = FQN
                if ((name == null) || (name.length() <= 0)) {
                    name = packageName + klass;
                }

                // load the mixin to determine if it is a pure interface introduction
                Class mixin;
                try {
                    mixin = aspectClass.getClassLoader().loadClass(packageName + klass);
                } catch (ClassNotFoundException e) {
                    throw new DefinitionException(
                            "could not find mixin implementation: "
                            + packageName
                            + klass
                            + " "
                            + e.getMessage()
                    );
                }

                // pure interface introduction
                if (mixin.isInterface()) {
                    DefinitionParserHelper.createAndAddInterfaceIntroductionDefToAspectDef(
                            bindTo, name, packageName
                                          + klass, aspectDef
                    );

                    // handles nested "bind-to" elements
                    for (Iterator it1 = introduceElement.elementIterator("bind-to"); it1.hasNext();) {
                        Element bindToElement = (Element) it1.next();
                        String pointcut = bindToElement.attributeValue("pointcut");
                        DefinitionParserHelper.createAndAddInterfaceIntroductionDefToAspectDef(
                                pointcut,
                                name,
                                packageName + klass,
                                aspectDef
                        );
                    }
                } else {
                    // mixin introduction
                    Class[] introduced = mixin.getInterfaces();
                    String[] introducedInterfaceNames = new String[introduced.length];
                    for (int i = 0; i < introduced.length; i++) {
                        introducedInterfaceNames[i] = introduced[i].getName();
                    }
                    DefinitionParserHelper.createAndAddIntroductionDefToAspectDef(
                            mixin,
                            bindTo,
                            deploymentModel,
                            aspectDef
                    );

                    // handles nested "bind-to" elements
                    for (Iterator it1 = introduceElement.elementIterator("bind-to"); it1.hasNext();) {
                        Element bindToElement = (Element) it1.next();
                        String pointcut = bindToElement.attributeValue("pointcut");
                        DefinitionParserHelper.createAndAddIntroductionDefToAspectDef(
                                mixin,
                                pointcut,
                                deploymentModel,
                                aspectDef
                        );
                    }
                }
            }
        }
    }

    /**
     * Creates the advice definitions and adds them to the aspect definition.
     *
     * @param type        the type of advice
     * @param bindTo      the pointcut expresion
     * @param name        the name of the advice
     * @param method      the method implementing the advice
     * @param methodIndex the method index
     * @param aspectDef   the aspect definition
     */
    private static void createAndAddAdviceDefsToAspectDef(final String type,
                                                          final String bindTo,
                                                          final String name,
                                                          final Method method,
                                                          final int methodIndex,
                                                          final AspectDefinition aspectDef) {
        try {
            if (type.equalsIgnoreCase("around")) {
                final String aspectName = aspectDef.getName();
                AdviceDefinition adviceDef = DefinitionParserHelper.createAdviceDefinition(
                        name,
                        AdviceType.AROUND,
                        bindTo,
                        null,
                        aspectName,
                        aspectDef.getClassName(),
                        method,
                        methodIndex,
                        aspectDef
                );
                aspectDef.addAroundAdvice(adviceDef);

            } else if (type.equalsIgnoreCase("before")) {
                final String aspectName = aspectDef.getName();
                AdviceDefinition adviceDef = DefinitionParserHelper.createAdviceDefinition(
                        name,
                        AdviceType.BEFORE,
                        bindTo,
                        null,
                        aspectName,
                        aspectDef.getClassName(),
                        method,
                        methodIndex,
                        aspectDef
                );
                aspectDef.addBeforeAdvice(adviceDef);

            } else if (type.startsWith("after")) {
                String specialArgumentType = null;
                AdviceType adviceType = AdviceType.AFTER;
                if (type.startsWith("after returning(")) {
                    adviceType = AdviceType.AFTER_RETURNING;
                    int start = type.indexOf('(');
                    int end = type.indexOf(')');
                    specialArgumentType = type.substring(start + 1, end).trim();
                } else if (type.startsWith("after throwing(")) {
                    adviceType = AdviceType.AFTER_THROWING;
                    int start = type.indexOf('(');
                    int end = type.indexOf(')');
                    specialArgumentType = type.substring(start + 1, end).trim();
                } else if (type.startsWith("after finally")) {
                    adviceType = AdviceType.AFTER_FINALLY;
                }
                if (specialArgumentType != null && specialArgumentType.indexOf(' ') > 0) {
                    throw new DefinitionException(
                            "argument to after (returning/throwing) can only be a type (parameter name binding should be done using args(..))"
                    );
                }
                final String aspectName = aspectDef.getName();
                AdviceDefinition adviceDef = DefinitionParserHelper.createAdviceDefinition(
                        name,
                        adviceType,
                        bindTo,
                        specialArgumentType,
                        aspectName,
                        aspectDef.getClassName(),
                        method,
                        methodIndex,
                        aspectDef
                );

                aspectDef.addAfterAdvice(adviceDef);
            }
        } catch (DefinitionException e) {
            System.err.println(
                    "WARNING: unable to register advice "
                    + aspectDef.getName()
                    + "."
                    + name
                    + " at pointcut \""
                    + bindTo
                    + "\" due to: "
                    + e.getMessage()
            );
            // TODO ALEX - better handling of reg issue (f.e. skip the whole aspect, in DocumentParser, based on DefinitionE
        }
    }

    /**
     * Retrieves and returns the package.
     *
     * @param packageElement the package element
     * @return the package as a string ending with DOT, or empty string
     */
    private static String getPackage(final Element packageElement) {
        String packageName = "";
        for (Iterator it2 = packageElement.attributeIterator(); it2.hasNext();) {
            Attribute attribute = (Attribute) it2.next();
            if (attribute.getName().trim().equalsIgnoreCase("name")) {
                packageName = attribute.getValue().trim();
                if (packageName.endsWith(".*")) {
                    packageName = packageName.substring(0, packageName.length() - 1);
                } else if (packageName.endsWith(".")) {
                    ; // skip
                } else {
                    packageName += ".";
                }
                break;
            } else {
                continue;
            }
        }
        return packageName;
    }

    /**
     * Parses the <tt>include</tt> elements.
     *
     * @param root        the root element
     * @param definition  the definition object
     * @param packageName the package name
     */
    private static void parseIncludePackageElements(final Element root,
                                                    final SystemDefinition definition,
                                                    final String packageName) {
        for (Iterator it1 = root.elementIterator("include"); it1.hasNext();) {
            String includePackage = "";
            Element includeElement = (Element) it1.next();
            for (Iterator it2 = includeElement.attributeIterator(); it2.hasNext();) {
                Attribute attribute = (Attribute) it2.next();
                if (attribute.getName().trim().equalsIgnoreCase("package")) {
                    // handle base package
                    if (packageName.endsWith(".*")) {
                        includePackage = packageName.substring(0, packageName.length() - 2);
                    } else if (packageName.endsWith(".")) {
                        includePackage = packageName.substring(0, packageName.length() - 1);
                    }

                    // handle exclude package
                    includePackage = packageName + attribute.getValue().trim();
                    if (includePackage.endsWith(".*")) {
                        includePackage = includePackage.substring(0, includePackage.length() - 2);
                    } else if (includePackage.endsWith(".")) {
                        includePackage = includePackage.substring(0, includePackage.length() - 1);
                    }
                    break;
                } else {
                    continue;
                }
            }
            if (includePackage.length() != 0) {
                definition.addIncludePackage(includePackage);
            }
        }
    }

    /**
     * Parses the <tt>exclude</tt> elements.
     *
     * @param root        the root element
     * @param definition  the definition object
     * @param packageName the package name
     */
    private static void parseExcludePackageElements(final Element root,
                                                    final SystemDefinition definition,
                                                    final String packageName) {
        for (Iterator it1 = root.elementIterator("exclude"); it1.hasNext();) {
            String excludePackage = "";
            Element excludeElement = (Element) it1.next();
            for (Iterator it2 = excludeElement.attributeIterator(); it2.hasNext();) {
                Attribute attribute = (Attribute) it2.next();
                if (attribute.getName().trim().equalsIgnoreCase("package")) {
                    // handle base package
                    if (packageName.endsWith(".*")) {
                        excludePackage = packageName.substring(0, packageName.length() - 2);
                    } else if (packageName.endsWith(".")) {
                        excludePackage = packageName.substring(0, packageName.length() - 1);
                    }

                    // handle exclude package
                    excludePackage = packageName + attribute.getValue().trim();
                    if (excludePackage.endsWith(".*")) {
                        excludePackage = excludePackage.substring(0, excludePackage.length() - 2);
                    } else if (excludePackage.endsWith(".")) {
                        excludePackage = excludePackage.substring(0, excludePackage.length() - 1);
                    }
                    break;
                } else {
                    continue;
                }
            }
            if (excludePackage.length() != 0) {
                definition.addExcludePackage(excludePackage);
            }
        }
    }

    /**
     * Parses the <tt>prepare</tt> elements.
     *
     * @param root        the root element
     * @param definition  the definition object
     * @param packageName the base package name
     */
    public static void parsePrepareElements(final Element root,
                                            final SystemDefinition definition,
                                            final String packageName) {
        for (Iterator it1 = root.elementIterator("prepare"); it1.hasNext();) {
            String preparePackage = "";
            Element prepareElement = (Element) it1.next();
            for (Iterator it2 = prepareElement.attributeIterator(); it2.hasNext();) {
                Attribute attribute = (Attribute) it2.next();
                if (attribute.getName().trim().equals("package")) {
                    // handle base package
                    if (packageName.endsWith(".*")) {
                        preparePackage = packageName.substring(0, packageName.length() - 2);
                    } else if (packageName.endsWith(".")) {
                        preparePackage = packageName.substring(0, packageName.length() - 1);
                    }

                    // handle prepare package
                    preparePackage = packageName + attribute.getValue().trim();
                    if (preparePackage.endsWith(".*")) {
                        preparePackage = preparePackage.substring(0, preparePackage.length() - 2);
                    } else if (preparePackage.endsWith(".")) {
                        preparePackage = preparePackage.substring(0, preparePackage.length() - 1);
                    }
                    break;
                } else {
                    continue;
                }
            }
            if (preparePackage.length() != 0) {
                definition.addPreparePackage(preparePackage);
            }
        }
    }

    /**
     * Retrieves and returns the base package for a system element
     *
     * @param system a system element
     * @return the base package
     */
    private static String getBasePackage(final Element system) {
        String basePackage = "";
        for (Iterator it2 = system.attributeIterator(); it2.hasNext();) {
            Attribute attribute = (Attribute) it2.next();
            if (attribute.getName().trim().equalsIgnoreCase("base-package")) {
                basePackage = attribute.getValue().trim();
                if (basePackage.endsWith(".*")) {
                    basePackage = basePackage.substring(0, basePackage.length() - 1);
                } else if (basePackage.endsWith(".")) {
                    ; // skip
                } else {
                    basePackage += ".";
                }
                break;
            } else {
                continue;
            }
        }
        return basePackage;
    }

    /**
     * Struct with pointcut info.
     */
    private static class PointcutInfo {
        public String name;

        public String expression;
    }

    /**
     * Check if a method from an aspect class match a given advice signature.
     * <br/>
     * If the signature is just a method name, then we have a match even if JoinPoint is sole method parameter.
     * Else we match both method name and parameters type, with abbreviation support (java.lang.* and JoinPoint)
     *
     * @param method
     * @param adviceSignature
     * @return
     */
    private static boolean matchMethodAsAdvice(Method method, String adviceSignature) {
        // grab components from adviceSignature
        //TODO catch AOOBE for better syntax error reporting
        String[] signatureElements = Strings.extractMethodSignature(adviceSignature);
        // check method name
        if (!method.getName().equals(signatureElements[0])) {
            return false;
        }
        // check number of args
        if (method.getParameterTypes().length * 2 != signatureElements.length - 1) {
            // we still match if method has JoinPoint has sole parameter
            // and adviceSignature has none
            if (signatureElements.length == 1
                && method.getParameterTypes().length == 1
                && method.getParameterTypes()[0].getName().equals(JoinPoint.class.getName())) {
                return true;
            } else {
                return false;
            }
        }
        int argIndex = 0;
        for (int i = 1; i < signatureElements.length; i++) {
            String paramType = signatureElements[i++];
            String paramName = signatureElements[i];
            String methodParamType = JavaClassInfo.convertJavaArrayTypeNameToHumanTypeName(
                    method.getParameterTypes()[argIndex++].getName().replace('/', '.')
            );
            // handle shortcuts for java.lang.* and JoinPoint
            String paramTypeResolved = (String) Pattern.ABBREVIATIONS.get(paramType);
            if (methodParamType.equals(paramType) || methodParamType.equals(paramTypeResolved)) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }
}