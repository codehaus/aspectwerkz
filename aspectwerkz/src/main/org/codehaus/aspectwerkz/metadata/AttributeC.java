/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.metadata;

import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.DocumentHelper;
import org.dom4j.io.XMLWriter;
import org.dom4j.io.OutputFormat;

import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;

import org.codehaus.aspectwerkz.xmldef.definition.AspectWerkzDefinitionImpl;
import org.codehaus.aspectwerkz.xmldef.definition.PointcutDefinitionImpl;
import org.codehaus.aspectwerkz.xmldef.definition.AdviceDefinition;
import org.codehaus.aspectwerkz.xmldef.definition.IntroductionDefinition;
import org.codehaus.aspectwerkz.xmldef.definition.AttributeTag;
import org.codehaus.aspectwerkz.xmldef.definition.AspectDefinition;
import org.codehaus.aspectwerkz.xmldef.definition.IntroductionWeavingRule;
import org.codehaus.aspectwerkz.xmldef.definition.AdviceWeavingRule;
import org.codehaus.aspectwerkz.xmldef.definition.ControllerDefinition;
import org.codehaus.aspectwerkz.xmldef.definition.DefinitionValidator;
import org.codehaus.aspectwerkz.xmldef.advice.CFlowPreAdvice;
import org.codehaus.aspectwerkz.xmldef.advice.CFlowPostAdvice;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.util.UuidGenerator;
import org.codehaus.aspectwerkz.util.Strings;
import org.codehaus.aspectwerkz.definition.PointcutDefinition;
import org.codehaus.aspectwerkz.definition.DefinitionLoader;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;

/**
 * Parses a given source tree and compiles "runtime attributes" (set as JavaDoc tags throughout
 * the code) into an XML definition.
 * <p/>
 * Can be called from the command line.
 * <p/>
 * Validation is turned off by default. To turn it on feed the JVM with:
 * <code>-Daspectwerkz.definition.validate=true</code>
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AttributeC {

    public static final String METHOD_POINTCUT_NAME = "__aw_method_pointcut_";
    public static final String SETFIELD_POINTCUT_NAME = "__aw_setfield_pointcut_";
    public static final String GETFIELD_POINTCUT_NAME = "__aw_getfield_pointcut_";
    public static final String THROWS_POINTCUT_NAME = "__aw_throws_pointcut_";
    public static final String CALLERSIDE_POINTCUT_NAME = "__aw_callerside_pointcut_";
    public static final String CFLOW_POINTCUT_NAME = "__aw_cflow_pointcut_";
    public static final String CONTROLLER_POINTCUT_NAME = "__aw_controller_pointcut_";

    /**
     * Parses a given source tree, retrieves the runtime attributes defined in the code
     * and creates an XML definition based on these attributes.
     *
     * @param sourcePath the path to the sources to compile attributes for
     * @param fileName the full name of the file name to compile the attributes to
     */
    public static void compile(final String sourcePath, final String fileName) {
        compile(sourcePath, fileName, null, null);
    }

    /**
     * Parses a given source tree, retrieves the runtime attributes defined in the code
     * and creates an XML definition based on these attributes.
     *
     * @param sourcePath the path to the sources to compile attributes for
     * @param fileName the full name of the file to compile the attributes to
     * @param definitionFileToMerge the full name of the file to merge the compiled definition with
     * @param uuid the UUID for the definition
     */
    public static void compile(final String sourcePath,
                               final String fileName,
                               final String definitionFileToMerge,
                               String uuid) {
        if (sourcePath == null) throw new IllegalArgumentException("source path can not be null");
        if (fileName == null) throw new IllegalArgumentException("file name can not be null");

        AspectWerkzDefinitionImpl definition = getDefinition(definitionFileToMerge);

        parseRuntimeAttributes(definition, sourcePath);

        validate(definition);

        if (uuid == null) {
            uuid = UuidGenerator.generate(definition);
        }

        Document document = createDocument(definition, uuid);
        writeDocumentToFile(document, fileName);
    }

    /**
     * Parses the attributes and creates definitions for the matching attributes.
     *
     * @param definition the definition
     * @param sourcePath the path to the source dir
     */
    public static void parseRuntimeAttributes(final AspectWerkzDefinitionImpl definition,
                                              final String sourcePath) {

        QDoxParser qdoxParser = new QDoxParser(sourcePath);
        String[] allClasses = qdoxParser.getAllClassNames();

        // add the cflow advice to the system
        definition.addAdvice(CFlowPreAdvice.getDefinition());

        // handle the definition attributes
        for (int i = 0; i < allClasses.length; i++) {
            String className = allClasses[i];
            if (!qdoxParser.parse(className)) {
                continue;
            }
            weaveIntroductionDefinitionAttributes(qdoxParser, definition);
            weaveAdviceDefinitionAttributes(qdoxParser, definition);
        }

        // handle the definition references
        for (int i = 0; i < allClasses.length; i++) {
            String className = allClasses[i];
            if (!qdoxParser.parse(className)) {
                continue;
            }
            parseCFlowPointcutAttributes(definition, className, qdoxParser);
            parseIntroductionAttributes(definition, className, qdoxParser);
            parseJoinPointControllerAttributes(definition, className, qdoxParser);
            parseMethodPointcutAttributes(definition, className, qdoxParser);
            parseSetFieldPointcutAttributes(definition, className, qdoxParser);
            parseGetFieldPointcutAttributes(definition, className, qdoxParser);
            parseThrowsPointcutAttributes(definition, className, qdoxParser);
            parseCallerSidePointcutAttributes(definition, className, qdoxParser);
        }
    }

    /**
     * Creates a DOM documents out of the definition.
     *
     * @param definition the AspectWerkz definition
     * @param uuid the UUID for the definition
     * @return the DOM document
     */
    public static Document createDocument(final AspectWerkzDefinitionImpl definition,
                                          final String uuid) {
        if (definition == null) throw new IllegalArgumentException("definition can not be null");

        Document document = DocumentHelper.createDocument();
        document.addDocType(
                "aspectwerkz",
                "-//AspectWerkz//DTD//EN",
                "http://aspectwerkz.codehaus.org/dtd/aspectwerkz.dtd"
        );

        Element root = document.addElement("aspectwerkz");
        Element system = root.addElement("system");
        system.addAttribute("id", uuid);

        handleIntroductionDefinitions(system, definition);
        handleAdviceDefinitions(system, definition);
        handleAspectDefinitions(system, definition);

        return document;
    }

    /**
     * Writes a DOM document to file.
     *
     * @param document the document
     * @param fileName the name of the file (full path)
     */
    public static void writeDocumentToFile(final Document document, final String fileName) {
        try {
            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter writer = new XMLWriter(new FileWriter(fileName), format);
            writer.write(document);
            writer.close();
        }
        catch (IOException e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Handles the introduction definitions.
     *
     * @param root the document root
     * @param definition the AspectWerkz definition
     */
    private static void handleIntroductionDefinitions(final Element root,
                                                      final AspectWerkzDefinitionImpl definition) {
        for (Iterator it = definition.getIntroductionDefinitions().iterator(); it.hasNext();) {
            IntroductionDefinition def = (IntroductionDefinition)it.next();
            addIntroductionDefElement(root, def);
        }
    }

    /**
     * Returns the definition.
     * Grabs the first xmldef definition found.
     * If append is set to true then it loads the definition file from disk otherwise
     * it just creates a new blank one.
     *
     * TODO: handle multiple xmldef definition within the same XML file
     *
     * @param fileName the name of the definition file
     * @return the aspectwerkz definition
     */
    private static AspectWerkzDefinitionImpl getDefinition(final String fileName) {
        AspectWerkzDefinitionImpl definition = null;

        if (fileName != null) {
            File definitionFile = new File(fileName);

            if (definitionFile.exists()) {
                // grab the first xmldef definition
                List definitions = DefinitionLoader.loadDefinitionsFromFile(fileName);
                for (Iterator it = definitions.iterator(); it.hasNext();) {
                    AspectWerkzDefinition def = (AspectWerkzDefinition)it.next();
                    if (def.isXmlDef()) {
                        definition = (AspectWerkzDefinitionImpl)def;
                        break;
                    }
                }
            }
        }
        if (definition == null) {
            definition = new AspectWerkzDefinitionImpl();
        }
        return definition;
    }

    /**
     * Handles the advice definitions.
     *
     * @param root the document root
     * @param definition the AspectWerkz definition
     */
    private static void handleAdviceDefinitions(final Element root,
                                                final AspectWerkzDefinitionImpl definition) {
        // create the cflow pre and post advice definitions
        addAdviceDefElement(root, CFlowPreAdvice.getDefinition());
        addAdviceDefElement(root, CFlowPostAdvice.getDefinition());

        for (Iterator it = definition.getAdviceDefinitions().iterator(); it.hasNext();) {
            AdviceDefinition def = (AdviceDefinition)it.next();
            addAdviceDefElement(root, def);
        }
    }

    /**
     * Handles the introduction definition element.
     *
     * @param root the document root
     * @param introDef the introduction definition
     */
    private static Element addIntroductionDefElement(final Element root,
                                                     final IntroductionDefinition introDef) {
        if (root == null) throw new IllegalArgumentException("root element can not be null");
        if (introDef == null) throw new IllegalArgumentException("introduction definition can not be null");
        Element introDefElement = root.addElement("introduction-def");
        introDefElement.addAttribute("name", introDef.getName());
        introDefElement.addAttribute("interface", introDef.getInterface());
        String implementation = introDef.getImplementation();
        if (implementation != null) {
            introDefElement.addAttribute("implementation", implementation);
        }
        String deploymentModel = introDef.getDeploymentModel();
        if (deploymentModel != null && deploymentModel.length() != 0) {
            introDefElement.addAttribute("deployment-model", deploymentModel);
        }
        else {
            introDefElement.addAttribute("deployment-model", "perJVM");
        }
        String attribute = introDef.getAttribute();
        if (attribute != null && attribute.length() != 0) {
            introDefElement.addAttribute("attribute", attribute);
        }
        return introDefElement;
    }

    /**
     * Handles the advice definition element.
     *
     * @param root the document root
     * @param adviceDef the advice definition
     */
    private static Element addAdviceDefElement(final Element root,
                                               final AdviceDefinition adviceDef) {
        if (root == null) throw new IllegalArgumentException("root element can not be null");
        if (adviceDef == null) throw new IllegalArgumentException("advice definition can not be null");
        Element adviceDefElement = root.addElement("advice-def");
        adviceDefElement.addAttribute("name", adviceDef.getName());
        adviceDefElement.addAttribute("class", adviceDef.getAdviceClassName());
        String deploymentModel = adviceDef.getDeploymentModel();
        if (deploymentModel != null && deploymentModel.length() != 0) {
            adviceDefElement.addAttribute("deployment-model", deploymentModel);
        }
        else {
            adviceDefElement.addAttribute("deployment-model", "perJVM");
        }
        String attribute = adviceDef.getAttribute();
        if (attribute != null && attribute.length() != 0) {
            adviceDefElement.addAttribute("attribute", attribute);
        }

        addAdviceParamElements(adviceDefElement, adviceDef);

        return adviceDefElement;
    }

    /**
     * Handles the advice parameter elements.
     *
     * @param root the document root
     * @param adviceDef the advice definition
     */
    private static void addAdviceParamElements(final Element adviceElement,
                                               final AdviceDefinition adviceDef) {
        if (adviceElement == null) throw new IllegalArgumentException("advice element can not be null");
        if (adviceDef == null) throw new IllegalArgumentException("advice definition can not be null");
        for (Iterator it = adviceDef.getParameters().entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();

            Element adviceParamElement = adviceElement.addElement("param");
            adviceParamElement.addAttribute("name", (String)entry.getKey());
            adviceParamElement.addAttribute("value", (String)entry.getValue());
        }
    }

    /**
     * Handles the aspect defintions.
     *
     * @param root the document root
     * @param definition the AspectWerkz definition
     */
    private static void handleAspectDefinitions(final Element root,
                                                final AspectWerkzDefinitionImpl definition) {
        for (Iterator it = definition.getAspectDefinitions().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();

            Element aspectElement = root.addElement("aspect");
            aspectElement.addAttribute("name", aspectDef.getName());

            handlePointcutDefinitions(aspectElement, aspectDef);
            handleControllerDefinitions(aspectElement, aspectDef);
            handleIntroductionWeavingRules(aspectElement, aspectDef);
            handleAdviceWeavingRules(aspectElement, aspectDef);
        }
    }

    /**
     * Handles the pointcut defintions.
     *
     * @param aspectElement the aspect element
     * @param aspectDef the aspect definition
     */
    private static void handlePointcutDefinitions(final Element aspectElement,
                                                  final AspectDefinition aspectDef) {
        for (Iterator it2 = aspectDef.getPointcutDefs().iterator(); it2.hasNext();) {
            PointcutDefinition pointcutDef = (PointcutDefinition)it2.next();

            Element pointcutDefElement = aspectElement.addElement("pointcut-def");
            pointcutDefElement.addAttribute("name", pointcutDef.getName());
            pointcutDefElement.addAttribute("type", pointcutDef.getType());
            pointcutDefElement.addAttribute("non-reentrant", pointcutDef.getNonReentrant());

            StringBuffer fullPattern = new StringBuffer();
            String type = pointcutDef.getType();
            if (type.equalsIgnoreCase(PointcutDefinition.METHOD) ||
                    type.equalsIgnoreCase(PointcutDefinition.GET_FIELD) ||
                    type.equalsIgnoreCase(PointcutDefinition.SET_FIELD)) {
                String classPattern = pointcutDef.getClassPattern();
                String pattern = pointcutDef.getPattern();
                int space = pattern.indexOf(' ');
                String returnType = pattern.substring(0, space + 1);
                String methodName = pattern.substring(space + 1);
                fullPattern.append(returnType);
                fullPattern.append(classPattern);
                if (pointcutDef.isHierarchical()) {
                    fullPattern.append('+');
                }
                fullPattern.append('.');
                fullPattern.append(methodName);
            }
            else if (type.equalsIgnoreCase(PointcutDefinition.THROWS)) {
                String classPattern = pointcutDef.getClassPattern();
                String pattern = pointcutDef.getPattern();
                int delimiter = pattern.indexOf('#');
                String methodPattern = pattern.substring(0, delimiter);
                String exception = pattern.substring(delimiter + 1);
                int space = methodPattern.indexOf(' ');
                String returnType = methodPattern.substring(0, space + 1);
                String methodName = methodPattern.substring(space + 1);
                fullPattern.append(returnType);
                fullPattern.append(classPattern);
                fullPattern.append('.');
                fullPattern.append(methodName);
                fullPattern.append('#');
                fullPattern.append(exception);
            }
            else if (type.equalsIgnoreCase(PointcutDefinition.CALLER_SIDE)) {
                String callerClassPattern = pointcutDef.getClassPattern();
                String pattern = pointcutDef.getPattern();
                int delimiter = pattern.indexOf('#');
                String calleeClassPattern = pattern.substring(0, delimiter);
                String methodPattern = pattern.substring(delimiter + 1);
                int space = methodPattern.indexOf(' ');
                String returnType = methodPattern.substring(0, space + 1);
                String methodName = methodPattern.substring(space + 1);
                fullPattern.append(callerClassPattern);
                fullPattern.append("->");
                fullPattern.append(returnType);
                fullPattern.append(calleeClassPattern);
                fullPattern.append(".");
                fullPattern.append(methodName);
                ;
            }
            else if (type.equals(PointcutDefinition.CFLOW)) {
                String pattern = pointcutDef.getPattern();
                int delimiter = pattern.indexOf('#');
                String classPattern = pattern.substring(0, delimiter);
                String methodPattern = pattern.substring(delimiter + 1);
                int space = methodPattern.indexOf(' ');
                String returnType = methodPattern.substring(0, space + 1);
                String methodName = methodPattern.substring(space + 1);
                fullPattern.append(returnType);
                fullPattern.append(classPattern);
                fullPattern.append(".");
                fullPattern.append(methodName);
            }
            else {
                throw new RuntimeException("invalid pointcut type: " + pointcutDef.getType());
            }

            pointcutDefElement.addAttribute("pattern", fullPattern.toString());
        }
    }

    /**
     * Handles the join point controllers.
     *
     * @param aspectElement the aspect element
     * @param aspectDef the aspect definition
     */
    private static void handleControllerDefinitions(final Element aspectElement,
                                                    final AspectDefinition aspectDef) {
        for (Iterator it = aspectDef.getControllerDefs().iterator(); it.hasNext();) {
            ControllerDefinition controllerDef = (ControllerDefinition)it.next();

            Element weavingRuleElement = aspectElement.addElement("controller-def");
            weavingRuleElement.addAttribute("pointcut", controllerDef.getExpression());
            weavingRuleElement.addAttribute("class", controllerDef.getClassName());
        }
    }

    /**
     * Handles the introduction weaving rules.
     *
     * @param aspectElement the aspect element
     * @param aspectDef the aspect definition
     */
    private static void handleIntroductionWeavingRules(final Element aspectElement,
                                                       final AspectDefinition aspectDef) {
        for (Iterator it = aspectDef.getIntroductionWeavingRules().iterator(); it.hasNext();) {
            IntroductionWeavingRule weavingRule = (IntroductionWeavingRule)it.next();

            Element weavingRuleElement = aspectElement.addElement("bind-introduction");
            weavingRuleElement.addAttribute("class", weavingRule.getClassPattern());

            for (Iterator it2 = weavingRule.getIntroductionRefs().iterator(); it2.hasNext();) {
                String introductionRef = (String)it2.next();

                Element introductionRefElement = weavingRuleElement.addElement("introduction-ref");
                introductionRefElement.addAttribute("name", introductionRef);
            }
        }
    }

    /**
     * Handles the advice weaving rules.
     *
     * @param aspectElement the aspect element
     * @param aspectDef the aspect definition
     */
    private static void handleAdviceWeavingRules(final Element aspectElement,
                                                 final AspectDefinition aspectDef) {
        for (Iterator it = aspectDef.getAdviceWeavingRules().iterator(); it.hasNext();) {
            AdviceWeavingRule weavingRule = (AdviceWeavingRule)it.next();

            Element weavingRuleElement = aspectElement.addElement("bind-advice");
            weavingRuleElement.addAttribute("pointcut", weavingRule.getExpression());

            String cflowExpression = weavingRule.getCFlowExpression();
            if (cflowExpression != null) {
                weavingRuleElement.addAttribute("cflow", cflowExpression);
            }

            for (Iterator it2 = weavingRule.getAdviceRefs().iterator(); it2.hasNext();) {
                String adviceRef = (String)it2.next();

                Element adviceRefElement = weavingRuleElement.addElement("advice-ref");
                adviceRefElement.addAttribute("name", adviceRef);
            }
        }
    }

    /**
     * Weaves the introduction definition attributes.
     *
     * @param qdoxParser the QDox parser
     * @param definition the definition
     */
    private static void weaveIntroductionDefinitionAttributes(
            final QDoxParser qdoxParser,
            final AspectWerkzDefinitionImpl definition) {

        JavaClass javaClass = qdoxParser.getJavaClass();
        DocletTag[] introductionDefTags = javaClass.getTagsByName(AttributeTag.INTRODUCTION_DEF);

        for (int i = 0; i < introductionDefTags.length; i++) {
            if (introductionDefTags[i] == null) {
                continue;
            }

            IntroductionDefinition introDef = new IntroductionDefinition();
            introDef.setName(introductionDefTags[i].getNamedParameter("name"));
            introDef.setInterface(javaClass.getFullyQualifiedName());
            introDef.setImplementation(introductionDefTags[i].getNamedParameter("implementation"));
            introDef.setDeploymentModel(introductionDefTags[i].getNamedParameter("deployment-model"));
            introDef.setAttribute(introductionDefTags[i].getNamedParameter("attribute"));

            definition.addIntroduction(introDef);
        }
    }

    /**
     * Weaves the advice definition attributes.
     *
     * @param qdoxParser the QDox parser
     * @param definition the definition
     */
    private static void weaveAdviceDefinitionAttributes(
            final QDoxParser qdoxParser,
            final AspectWerkzDefinitionImpl definition) {

        JavaClass javaClass = qdoxParser.getJavaClass();
        DocletTag[] adviceDefTags = javaClass.getTagsByName(AttributeTag.ADVICE_DEF);

        for (int i = 0; i < adviceDefTags.length; i++) {
            if (adviceDefTags[i] == null) {
                continue;
            }

            AdviceDefinition adviceDef = new AdviceDefinition();
            adviceDef.setName(adviceDefTags[i].getNamedParameter("name"));
            adviceDef.setAdviceClassName(javaClass.getFullyQualifiedName());
            adviceDef.setDeploymentModel(adviceDefTags[i].getNamedParameter("deployment-model"));
            adviceDef.setAttribute(adviceDefTags[i].getNamedParameter("attribute"));

            weaveAdviceParamAttributes(javaClass, adviceDef);

            definition.addAdvice(adviceDef);
        }
    }

    /**
     * Weaves the advice param attributes.
     *
     * @param qdoxParser the QDox parser
     * @param definition the definition
     */
    private static void weaveAdviceParamAttributes(
            final JavaClass javaClass,
            final AdviceDefinition adviceDefinition) {

        DocletTag[] adviceDefTags = javaClass.getTagsByName(AttributeTag.ADVICE_PARAM);

        for (int i = 0; i < adviceDefTags.length; i++) {
            if (adviceDefTags[i] == null) {
                continue;
            }

            String adviceRef = adviceDefTags[i].getNamedParameter("advice-ref");
            if (adviceRef.equals(adviceDefinition.getName())) {
                adviceDefinition.addParameter(
                        adviceDefTags[i].getNamedParameter("name"),
                        adviceDefTags[i].getNamedParameter("value"));
            }
        }
    }

    /**
     * Weaves the introduction attributes.
     *
     * @param definition the definition
     * @param className the name of the parsed class
     * @param qdoxParser the QDox parser
     */
    private static void parseIntroductionAttributes(
            final AspectWerkzDefinitionImpl definition,
            final String className,
            final QDoxParser qdoxParser) {

        AspectDefinition aspectDefinition = definition.getAspectDefinition(
                AspectWerkzDefinition.SYSTEM_ASPECT);

        JavaClass javaClass = qdoxParser.getJavaClass();
        DocletTag[] introductionTags = javaClass.getTagsByName(AttributeTag.INTRODUCTION);

        IntroductionWeavingRule weavingRule = new IntroductionWeavingRule();
        weavingRule.setClassPattern(className);

        for (int i = 0; i < introductionTags.length; i++) {
            if (introductionTags[i] == null) {
                continue;
            }
            String[] attributes = introductionTags[i].getParameters();
            for (int j = 0; j < attributes.length; j++) {
                final String introductionRef = definition.
                        getIntroductionNameByAttribute(attributes[j]);
                if (introductionRef == null) {
                    continue;
                }
                weavingRule.addIntroductionRef(introductionRef);
            }
            aspectDefinition.addIntroductionWeavingRule(weavingRule);
        }
    }

    /**
     * Weaves the pointcut controller attributes.
     *
     * @param definition the definition
     * @param className the name of the parsed class
     * @param qdoxParser the QDox parser
     */
    private static void parseJoinPointControllerAttributes(
            final AspectWerkzDefinitionImpl definition,
            final String className,
            final QDoxParser qdoxParser) {
        String pointcutName = CONTROLLER_POINTCUT_NAME + Strings.replaceSubString(className, ".", "_");

        int counter = 0;
        final JavaMethod[] javaMethods = qdoxParser.getJavaMethods();
        for (int i = 0; i < javaMethods.length; i++) {

            DocletTag[] methodTags = javaMethods[i].getTagsByName(AttributeTag.CONTROLLER);
            for (int j = 0; j < methodTags.length; j++) {
                if (methodTags[j] == null) {
                    continue;
                }

                String[] attributes = methodTags[j].getParameters();
                for (int k = 0; k < attributes.length; k++) {
                    String attribute = attributes[k];

                    String expression = pointcutName + counter;

                    // create and add a new pointcut definition
                    PointcutDefinition pointcutDef = new PointcutDefinitionImpl();
                    pointcutDef.setName(expression);
                    pointcutDef.setClassPattern(className);
                    pointcutDef.setPattern(createMethodPattern(javaMethods[i]));
                    pointcutDef.setType(PointcutDefinition.METHOD);
                    definition.getAspectDefinition(AspectWerkzDefinition.SYSTEM_ASPECT).
                            addPointcutDef(pointcutDef);

                    // create a new controller definition
                    ControllerDefinition controllerDef = new ControllerDefinition();
                    controllerDef.setClassName(attribute);
                    controllerDef.setExpression(expression);

                    definition.getAspectDefinition(AspectWerkzDefinition.SYSTEM_ASPECT).
                            addControllerDef(controllerDef);

                    // add the pointcut pattern
                    controllerDef.addMethodPointcutPattern(pointcutDef);

                    counter++;
                    break;
                }
            }
        }
    }

    /**
     * Weaves the method attributes.
     *
     * @param definition the definition
     * @param className the name of the parsed class
     * @param qdoxParser the QDox parser
     */
    private static void parseMethodPointcutAttributes(
            final AspectWerkzDefinitionImpl definition,
            final String className,
            final QDoxParser qdoxParser) {
        String pointcutName = METHOD_POINTCUT_NAME + Strings.replaceSubString(className, ".", "_");

        int counter = 0;
        final JavaMethod[] javaMethods = qdoxParser.getJavaMethods();
        for (int i = 0; i < javaMethods.length; i++) {

            DocletTag[] methodTags = javaMethods[i].getTagsByName(AttributeTag.METHOD);
            for (int j = 0; j < methodTags.length; j++) {
                if (methodTags[j] == null) {
                    continue;
                }

                String cflowRef = methodTags[j].getNamedParameter("cflow");
                String isNonReentrant = methodTags[j].getNamedParameter("non-reentrant");

                String[] attributes = methodTags[j].getParameters();
                for (int k = 0; k < attributes.length; k++) {
                    String attribute = attributes[k];
                    if (attribute.startsWith("cflow=") ||
                            attribute.startsWith("non-reentrant=")) {
                        continue;
                    }

                    for (Iterator it2 = definition.getAdviceDefinitions().iterator(); it2.hasNext();) {
                        String expression = pointcutName + counter;

                        // create and add a new pointcut def
                        PointcutDefinition pointcutDef = new PointcutDefinitionImpl();
                        pointcutDef.setName(expression);
                        pointcutDef.setClassPattern(className);
                        pointcutDef.setPattern(createMethodPattern(javaMethods[i]));
                        pointcutDef.setType(PointcutDefinition.METHOD);
                        pointcutDef.setNonReentrant(isNonReentrant);
                        definition.getAspectDefinition(AspectWerkzDefinition.SYSTEM_ASPECT).
                                addPointcutDef(pointcutDef);

                        String adviceAttribute = ((AdviceDefinition)it2.next()).getAttribute();
                        if (adviceAttribute == null) {
                            continue;
                        }
                        if (adviceAttribute.equals(attribute)) {
                            // get the advice ref
                            String adviceRef = definition.getAdviceNameByAttribute(adviceAttribute);
                            if (adviceRef == null) {
                                // TODO: log a warning
                                continue; // attribute not mapped to an advice
                            }
                            // create and add a new weaving rule def
                            AdviceWeavingRule weavingRule = new AdviceWeavingRule();
                            weavingRule.setExpression(expression);
                            weavingRule.setCFlowExpression(cflowRef);
                            weavingRule.addAdviceRef(adviceRef);
                            definition.getAspectDefinition(AspectWerkzDefinition.SYSTEM_ASPECT).
                                    addAdviceWeavingRule(weavingRule);

                            // add the pointcut pattern
                            weavingRule.addMethodPointcutPattern(pointcutDef);

                            counter++;
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Weaves the set field pointcut attributes.
     *
     * @param definition the definition
     * @param className the name of the parsed class
     * @param qdoxParser the QDox parser
     */
    private static void parseSetFieldPointcutAttributes(
            final AspectWerkzDefinitionImpl definition,
            final String className,
            final QDoxParser qdoxParser) {
        String pointcutName = SETFIELD_POINTCUT_NAME + Strings.replaceSubString(className, ".", "_");

        int counter = 0;
        final JavaField[] javaFields = qdoxParser.getJavaFields();
        for (int i = 0; i < javaFields.length; i++) {

            DocletTag[] setFieldTags = javaFields[i].getTagsByName(AttributeTag.SET_FIELD);
            for (int j = 0; j < setFieldTags.length; j++) {
                if (setFieldTags[j] == null) {
                    continue;
                }

                String[] setFieldAttributes = setFieldTags[j].getParameters();
                for (int k = 0; k < setFieldAttributes.length; k++) {

                    String attribute = setFieldAttributes[k];
                    for (Iterator it2 = definition.getAdviceDefinitions().iterator(); it2.hasNext();) {
                        String expression = pointcutName + counter;

                        // create and add a new pointcut def
                        PointcutDefinition pointcutDef = new PointcutDefinitionImpl();
                        pointcutDef.setName(expression);
                        pointcutDef.setClassPattern(className);
                        pointcutDef.setPattern(createFieldPattern(javaFields[i]));
                        pointcutDef.setType(PointcutDefinition.SET_FIELD);
                        definition.getAspectDefinition(AspectWerkzDefinition.SYSTEM_ASPECT).
                                addPointcutDef(pointcutDef);

                        String adviceAttribute = ((AdviceDefinition)it2.next()).getAttribute();
                        if (adviceAttribute == null) {
                            continue;
                        }
                        if (adviceAttribute.equals(attribute)) {
                            // get the advice ref
                            String adviceRef = definition.getAdviceNameByAttribute(adviceAttribute);
                            if (adviceRef == null) {
                                // TODO: log a warning
                                continue; // attribute not mapped to an advice
                            }

                            // create and add a new weaving rule def
                            AdviceWeavingRule weavingRule = new AdviceWeavingRule();
                            weavingRule.setExpression(expression);
                            weavingRule.addAdviceRef(adviceRef);
                            definition.getAspectDefinition(AspectWerkzDefinition.SYSTEM_ASPECT).
                                    addAdviceWeavingRule(weavingRule);

                            // add the pointcut pattern
                            weavingRule.addSetFieldPointcutPattern(pointcutDef);

                            counter++;
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Weaves the get field pointcut attributes.
     *
     * @param definition the definition
     * @param className the name of the parsed class
     * @param qdoxParser the QDox parser
     */
    private static void parseGetFieldPointcutAttributes(
            final AspectWerkzDefinitionImpl definition,
            final String className,
            final QDoxParser qdoxParser) {
        String pointcutName = GETFIELD_POINTCUT_NAME + Strings.replaceSubString(className, ".", "_");

        int counter = 0;
        final JavaField[] javaFields = qdoxParser.getJavaFields();
        for (int i = 0; i < javaFields.length; i++) {

            DocletTag[] getFieldTags = javaFields[i].getTagsByName(AttributeTag.GET_FIELD);
            for (int j = 0; j < getFieldTags.length; j++) {
                if (getFieldTags[j] == null) {
                    continue;
                }

                String[] getFieldAttributes = getFieldTags[j].getParameters();
                for (int k = 0; k < getFieldAttributes.length; k++) {
                    String attribute = getFieldAttributes[k];

                    for (Iterator it2 = definition.getAdviceDefinitions().iterator(); it2.hasNext();) {
                        String expression = pointcutName + counter;

                        // create and add a new pointcut def
                        PointcutDefinition pointcutDef = new PointcutDefinitionImpl();
                        pointcutDef.setName(expression);
                        pointcutDef.setClassPattern(className);
                        pointcutDef.setPattern(createFieldPattern(javaFields[i]));
                        pointcutDef.setType(PointcutDefinition.GET_FIELD);
                        definition.getAspectDefinition(AspectWerkzDefinition.SYSTEM_ASPECT).
                                addPointcutDef(pointcutDef);

                        String adviceAttribute = ((AdviceDefinition)it2.next()).getAttribute();
                        if (adviceAttribute == null) {
                            continue;
                        }
                        if (adviceAttribute.equals(attribute)) {
                            // get the advice ref
                            String adviceRef = definition.getAdviceNameByAttribute(adviceAttribute);
                            if (adviceRef == null) {
                                // TODO: log a warning
                                continue; // attribute not mapped to an advice
                            }

                            // create and add a new weaving rule def
                            AdviceWeavingRule weavingRule = new AdviceWeavingRule();
                            weavingRule.setExpression(expression);
                            weavingRule.addAdviceRef(adviceRef);
                            definition.getAspectDefinition(AspectWerkzDefinition.SYSTEM_ASPECT).
                                    addAdviceWeavingRule(weavingRule);

                            // add the pointcut pattern
                            weavingRule.addGetFieldPointcutPattern(pointcutDef);

                            counter++;
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Weaves the throws attributes.
     *
     * @param definition the definition
     * @param className the name of the parsed class
     * @param qdoxParser the QDox parser
     */
    private static void parseThrowsPointcutAttributes(
            final AspectWerkzDefinitionImpl definition,
            final String className,
            final QDoxParser qdoxParser) {
        String pointcutName = THROWS_POINTCUT_NAME + Strings.replaceSubString(className, ".", "_");

        int counter = 0;
        final JavaMethod[] javaMethods = qdoxParser.getJavaMethods();
        for (int i = 0; i < javaMethods.length; i++) {

            DocletTag[] throwsTags = javaMethods[i].getTagsByName(AttributeTag.THROWS);
            for (int j = 0; j < throwsTags.length; j++) {
                if (throwsTags[j] == null) {
                    continue;
                }

                String exceptionClassPattern = throwsTags[j].getNamedParameter("exception");

                if (exceptionClassPattern == null) {
                    throw new DefinitionException("exception class not specified for throws attribute at method <" + javaMethods[i].getName() + ">");
                }

                String[] attributes = throwsTags[j].getParameters();
                for (int k = 0; k < attributes.length; k++) {
                    String attribute = attributes[k];
                    if (attribute.startsWith("exception=")) {
                        continue;
                    }
                    for (Iterator it2 = definition.getAdviceDefinitions().iterator(); it2.hasNext();) {
                        String expression = pointcutName + counter;

                        // create and add a new pointcut def
                        PointcutDefinition pointcutDef = new PointcutDefinitionImpl();
                        pointcutDef.setName(expression);
                        pointcutDef.setClassPattern(className);
                        pointcutDef.setPattern(createThrowsPattern(
                                exceptionClassPattern, javaMethods[i]));
                        pointcutDef.setType(PointcutDefinition.THROWS);
                        definition.getAspectDefinition(AspectWerkzDefinition.SYSTEM_ASPECT).
                                addPointcutDef(pointcutDef);

                        String adviceAttribute = ((AdviceDefinition)it2.next()).getAttribute();
                        if (adviceAttribute == null) {
                            continue;
                        }
                        if (adviceAttribute.equals(attribute)) {
                            // get the advice ref
                            String adviceRef = definition.getAdviceNameByAttribute(adviceAttribute);
                            if (adviceRef == null) {
                                // TODO: log a warning
                                continue; // attribute not mapped to an advice
                            }

                            // create and add a new weaving rule def
                            AdviceWeavingRule weavingRule = new AdviceWeavingRule();
                            weavingRule.setExpression(expression);
                            weavingRule.addAdviceRef(adviceRef);
                            definition.getAspectDefinition(AspectWerkzDefinition.SYSTEM_ASPECT).
                                    addAdviceWeavingRule(weavingRule);

                            // add the pointcut pattern
                            weavingRule.addThrowsPointcutPattern(pointcutDef);

                            counter++;
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Weaves the caller side pointcut attributes.
     *
     * @param definition the definition
     * @param className the name of the parsed class
     * @param qdoxParser the QDox parser
     */
    private static void parseCallerSidePointcutAttributes(
            final AspectWerkzDefinitionImpl definition,
            final String className,
            final QDoxParser qdoxParser) {
        String pointcutName = CALLERSIDE_POINTCUT_NAME + Strings.replaceSubString(className, ".", "_");

        int counter = 0;
        final JavaMethod[] javaMethods = qdoxParser.getJavaMethods();
        for (int i = 0; i < javaMethods.length; i++) {

            DocletTag[] callerSideTags = javaMethods[i].getTagsByName(AttributeTag.CALLER_SIDE);
            for (int j = 0; j < callerSideTags.length; j++) {
                if (callerSideTags[j] == null) {
                    continue;
                }
                String callerClassPattern = callerSideTags[j].getNamedParameter("callerclass");
                if (callerClassPattern == null) {
                    throw new DefinitionException("caller class not specified for caller side attribute at method <" + javaMethods[i].getName() + ">");
                }

                String[] callerSideAttributes = callerSideTags[j].getParameters();
                for (int k = 0; k < callerSideAttributes.length; k++) {
                    String attribute = callerSideAttributes[k];
                    if (attribute.startsWith("callerclass=")) {
                        continue;
                    }
                    for (Iterator it2 = definition.getAdviceDefinitions().iterator(); it2.hasNext();) {
                        String expression = pointcutName + counter;

                        // create and add a new pointcut def
                        PointcutDefinition pointcutDef = new PointcutDefinitionImpl();
                        pointcutDef.setName(expression);
                        pointcutDef.setClassPattern(callerClassPattern);
                        pointcutDef.setPattern(createCallerSidePattern(className, javaMethods[i]));
                        pointcutDef.setType(PointcutDefinition.CALLER_SIDE);
                        definition.getAspectDefinition(AspectWerkzDefinition.SYSTEM_ASPECT).
                                addPointcutDef(pointcutDef);

                        String adviceAttribute = ((AdviceDefinition)it2.next()).getAttribute();
                        if (adviceAttribute == null) {
                            continue;
                        }

                        if (adviceAttribute.equals(attribute)) {
                            // get the advice ref
                            String adviceRef = definition.getAdviceNameByAttribute(adviceAttribute);
                            if (adviceRef == null) {
                                // TODO: log a warning
                                continue; // attribute not mapped to an advice
                            }

                            // create and add a new weaving rule def
                            AdviceWeavingRule weavingRule = new AdviceWeavingRule();
                            weavingRule.setExpression(expression);
                            weavingRule.addAdviceRef(adviceRef);
                            definition.getAspectDefinition(AspectWerkzDefinition.SYSTEM_ASPECT).
                                    addAdviceWeavingRule(weavingRule);

                            // add the pointcut pattern
                            weavingRule.addCallerSidePointcutPattern(pointcutDef);

                            counter++;
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Weaves the cflow attributes.
     *
     * @param definition the definition
     * @param className the name of the parsed class
     * @param qdoxParser the QDox parser
     */
    private static void parseCFlowPointcutAttributes(
            final AspectWerkzDefinitionImpl definition,
            final String className,
            final QDoxParser qdoxParser) {

        final JavaMethod[] javaMethods = qdoxParser.getJavaMethods();
        for (int i = 0; i < javaMethods.length; i++) {

            DocletTag[] cflowTags = javaMethods[i].getTagsByName(AttributeTag.CFLOW);
            for (int j = 0; j < cflowTags.length; j++) {
                if (cflowTags[j] == null) {
                    continue;
                }
                String[] attributes = cflowTags[j].getParameters();
                if (attributes.length == 0) {
                    continue;
                }

                // get the user defined name for the cflow pointcut
                String name = attributes[0];

                // create and add a new pointcut def
                PointcutDefinition pointcutDef = new PointcutDefinitionImpl();
                pointcutDef.setName(name);
                pointcutDef.setClassPattern("*");
                String callerSidePattern = createCallerSidePattern(className, javaMethods[i]);
                pointcutDef.setPattern(callerSidePattern);
                pointcutDef.setType(PointcutDefinition.CFLOW);

                definition.getAspectDefinition(AspectWerkzDefinition.SYSTEM_ASPECT).
                        addPointcutDef(pointcutDef);

                // create and add a new weaving rule def
                AdviceWeavingRule weavingRule = new AdviceWeavingRule();
                weavingRule.setExpression(name);
                weavingRule.addAdviceRef(CFlowPreAdvice.NAME);
                weavingRule.addAdviceRef(CFlowPostAdvice.NAME);
                definition.getAspectDefinition(AspectWerkzDefinition.SYSTEM_ASPECT).
                        addAdviceWeavingRule(weavingRule);

                // add the pointcut pattern (a method patterns since the cflow pointcut
                // is dependent on having a method pointcut)
                weavingRule.addCallerSidePointcutPattern(pointcutDef);

                break;
            }
        }
    }

    /**
     * Creates a method regular expression pattern.
     *
     * @param javaMethod the method
     * @return the pattern
     */
    private static String createMethodPattern(final JavaMethod javaMethod) {
        final StringBuffer pattern = new StringBuffer();
        pattern.append(javaMethod.getReturns().getValue());
        pattern.append(' ');
        pattern.append(javaMethod.getName());
        pattern.append('(');
        JavaParameter[] parameters = javaMethod.getParameters();
        for (int l = 0; l < parameters.length; l++) {
            JavaParameter parameter = parameters[l];
            String value = parameter.getType().getValue();
            for (int i = 1; i <= parameter.getType().getDimensions(); i++) {
                value += "[]";
            }
            pattern.append(value);
            if (l != parameters.length - 1) {
                pattern.append(',');
            }
        }
        pattern.append(')');
        return pattern.toString();
    }

    /**
     * Creates a field regular expression pattern.
     *
     * @param javaField the field
     * @return the pattern
     */
    private static String createFieldPattern(final JavaField javaField) {
        final StringBuffer pattern = new StringBuffer();
        String value = javaField.getType().getValue();
        for (int i = 1; i <= javaField.getType().getDimensions(); i++) {
            value += "[]";
        }
        pattern.append(value);
        pattern.append(' ');
        pattern.append(javaField.getName());
        return pattern.toString();
    }

    /**
     * Creates a throws regular expression pattern.
     *
     * @param exceptionClassPattern the name of the exception class
     * @param javaMethod the method
     * @return the pattern
     */
    private static String createThrowsPattern(final String exceptionClassPattern,
                                              final JavaMethod javaMethod) {
        StringBuffer throwsPattern = new StringBuffer();
        throwsPattern.append(createMethodPattern(javaMethod));
        throwsPattern.append('#');
        throwsPattern.append(exceptionClassPattern);
        return throwsPattern.toString();
    }

    /**
     * Creates a caller side regular expression pattern.
     *
     * @param className the name of the class
     * @param javaMethod the method
     * @return the pattern
     */
    private static String createCallerSidePattern(final String className,
                                                  final JavaMethod javaMethod) {
        StringBuffer callerSidePattern = new StringBuffer();
        callerSidePattern.append(className);
        callerSidePattern.append('#');
        callerSidePattern.append(createMethodPattern(javaMethod));
        return callerSidePattern.toString();
    }

    /**
     * Validates the definition.
     *
     * @param definition the definition
     */
    private static void validate(final AspectWerkzDefinitionImpl definition) {
        if (System.getProperty("aspectwerkz.definition.validate", "false").equals("true")) {
            // validate the definition
            DefinitionValidator validator = new DefinitionValidator(definition);
            validator.validate();

            // handle errors in definition
            List errors = validator.getErrorMessages();
            for (Iterator i = errors.iterator(); i.hasNext();) {
                String errorMsg = (String)i.next();

                // TODO: use logger instead of System.out
                System.out.println(errorMsg);
            }
        }
    }

    /**
     * Runs the compiler from the command line.
     *
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("usage: java [options...] org.codehaus.aspectwerkz.metadata.AttributeC <path to src dir> <file name> -merge <file name to merge with> -uuid <uuid for definition>");
            System.out.println("       -merge (or -m) <file name to merge with> tells the compiler which file it should append the compiled attributes to");
            System.out.println("       -uuid (or -u) <uuid for definition> is optional (if not specified one will be generated)");
            System.exit(0);
        }

        System.out.println("AspectWerkz - AttributeC");

        String mergeFile = null;
        String uuid = null;
        if (args.length >= 4) {
            if ((args[2].equals("-m") || args[2].equals("-merge")) && args[3] != null) {
                mergeFile = args[3];
            }
            else if ((args[2].equals("-u") || args[2].equals("-uuid")) && args[3] != null) {
                uuid = args[3];
            }
        }
        if (args.length >= 6) {
            if ((args[4].equals("-m") || args[4].equals("-merge")) && args[5] != null) {
                mergeFile = args[5];
            }
            else if ((args[4].equals("-u") || args[4].equals("-uuid")) && args[5] != null) {
                uuid = args[5];
            }
        }

        System.out.println("Compiling XML definition...");
        if (args.length == 2) {
            AttributeC.compile(args[0], args[1]);
        }
        else {
            if (mergeFile != null) {
                System.out.println("    Merging with: " + mergeFile);
            }
            if (uuid != null) {
                System.out.println("    UUID: " + uuid);
            }
            AttributeC.compile(args[0], args[1], mergeFile, uuid);
        }
        System.out.println("XML definition for classes in " + args[0] + " have been compiled to " + args[1]);
    }

}
