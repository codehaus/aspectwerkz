/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaMethod;
import org.codehaus.aspectwerkz.definition.attribute.AfterAttribute;
import org.codehaus.aspectwerkz.definition.attribute.AroundAttribute;
import org.codehaus.aspectwerkz.definition.attribute.AspectAttribute;
import org.codehaus.aspectwerkz.definition.attribute.AttributeEnhancer;
import org.codehaus.aspectwerkz.definition.attribute.BeforeAttribute;
import org.codehaus.aspectwerkz.definition.attribute.CFlowAttribute;
import org.codehaus.aspectwerkz.definition.attribute.CallAttribute;
import org.codehaus.aspectwerkz.definition.attribute.ClassAttribute;
import org.codehaus.aspectwerkz.definition.attribute.CustomAttribute;
import org.codehaus.aspectwerkz.definition.attribute.ExecutionAttribute;
import org.codehaus.aspectwerkz.definition.attribute.GetAttribute;
import org.codehaus.aspectwerkz.definition.attribute.HandlerAttribute;
import org.codehaus.aspectwerkz.definition.attribute.ImplementsAttribute;
import org.codehaus.aspectwerkz.definition.attribute.IntroduceAttribute;
import org.codehaus.aspectwerkz.definition.attribute.SetAttribute;
import org.codehaus.aspectwerkz.definition.attribute.bcel.BcelAttributeEnhancer;
import org.codehaus.aspectwerkz.metadata.QDoxParser;

/**
 * Compiles attributes for the aspects. Can be called from the command line.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class AspectC {

    public static final String ATTR_GENERIC_PREFIX = "Attribute.";
    public static final String ATTR_ASPECT = "Aspect";
    public static final String ATTR_EXECUTION = "Execution";
    public static final String ATTR_CALL = "Call";
    public static final String ATTR_CLASS = "Class";
    public static final String ATTR_SET = "Set";
    public static final String ATTR_GET = "Get";
    public static final String ATTR_HANDLER = "Handler";
    public static final String ATTR_CFLOW = "CFlow";
    public static final String ATTR_AROUND = "Around";
    public static final String ATTR_BEFORE = "Before";
    public static final String ATTR_AFTER = "After";
    public static final String ATTR_INTRODUCE = "Introduce";
    public static final String ATTR_IMPLEMENTS = "Implements";

    /**
     * Verbose logging.
     */
    private boolean m_verbose = false;

    /**
     * Set verbose mode.
     *
     * @param verbose
     */
    public void setVerbose(final boolean verbose) {
        m_verbose = verbose;
    }

    /**
     * Compiles attributes for the aspects.
     *
     * @param sourcePath the path to the sources to compile attributes for
     * @param classPath  the path to the compiled classes matching the source files
     */
    public void compile(final String sourcePath, final String classPath) {
        compile(sourcePath, classPath, classPath);
    }

    /**
     * Compiles attributes for the aspects.
     *
     * @param sourcePath the path to the sources to compile attributes for
     * @param classPath  the path to the compiled classes matching the source files
     * @param destDir    the path where to write the compiled aspects
     */
    public void compile(
            final String sourcePath,
            final String classPath,
            final String destDir) {
        if (sourcePath == null) {
            throw new IllegalArgumentException("source path can not be null");
        }
        if (classPath == null) {
            throw new IllegalArgumentException("class path can not be null");
        }

        final QDoxParser qdoxParser = new QDoxParser(sourcePath);
        String[] classNames = qdoxParser.getAllClassNames();

        for (int i = 0; i < classNames.length; i++) {
            String className = classNames[i];

            AttributeEnhancer enhancer = new BcelAttributeEnhancer(); // TODO: use factory
            if (enhancer.initialize(className, classPath)) {

                if (qdoxParser.parse(className)) {
                    JavaClass javaClass = parseClassAttributes(qdoxParser, enhancer);
                    parseFieldAttributes(javaClass, enhancer);
                    parseMethodAttributes(javaClass, enhancer);
                    parseInnerClassAttributes(javaClass, enhancer);
                    enhancer.write(destDir);
                }
            }
        }
    }

    /**
     * Parses the class attributes.
     *
     * @param qdoxParser the QDox parser
     * @param enhancer   the enhancer
     * @return the java class
     */
    private JavaClass parseClassAttributes(
            final QDoxParser qdoxParser,
            final AttributeEnhancer enhancer) {
        JavaClass javaClass = qdoxParser.getJavaClass();
        parseCustomAttributes(javaClass, enhancer);
        parseAspect(javaClass, enhancer);
        return javaClass;
    }

    /**
     * Parses the field attributes.
     *
     * @param javaClass the java class
     * @param enhancer  the enhancer
     */
    private void parseFieldAttributes(
            final JavaClass javaClass,
            final AttributeEnhancer enhancer) {
        JavaField[] javaFields = javaClass.getFields();
        for (int j = 0; j < javaFields.length; j++) {
            JavaField javaField = javaFields[j];
            parseCustomAttributes(javaField, enhancer);
            parseExecutionPointcut(javaField, enhancer);
            parseCallPointcut(javaField, enhancer);
            parseClassPointcut(javaField, enhancer);
            parseSetPointcut(javaField, enhancer);
            parseGetPointcut(javaField, enhancer);
            parseHandlerPointcut(javaField, enhancer);
            parseCFlowPointcut(javaField, enhancer);
            parseImplementsPointcut(javaField, enhancer);
        }
    }

    /**
     * Parses the method attributes.
     *
     * @param javaClass the java class
     * @param enhancer  the enhancer
     */
    private void parseMethodAttributes(
            final JavaClass javaClass,
            final AttributeEnhancer enhancer) {
        JavaMethod[] javaMethods = javaClass.getMethods();
        for (int j = 0; j < javaMethods.length; j++) {
            JavaMethod javaMethod = javaMethods[j];
            parseCustomAttributes(javaMethod, enhancer);
            parseAroundAdvice(javaMethod, enhancer);
            parseBeforeAdvice(javaMethod, enhancer);
            parseAfterAdvice(javaMethod, enhancer);
        }
    }

    /**
     * Parses the inner class attributes.
     *
     * @param javaClass the java class
     * @param enhancer  the enhancer
     */
    private void parseInnerClassAttributes(
            final JavaClass javaClass,
            final AttributeEnhancer enhancer) {
        JavaClass[] innerClasses = javaClass.getInnerClasses();
        for (int k = 0; k < innerClasses.length; k++) {
            parseIntroduction(innerClasses[k], enhancer);
        }
    }

    /**
     * Parses the custom class attributes.
     *
     * @param javaClass the java class
     * @param enhancer  the attribute enhancer
     */
    private void parseCustomAttributes(
            final JavaClass javaClass,
            final AttributeEnhancer enhancer) {
        DocletTag[] tags = javaClass.getTags();
        for (int i = 0; i < tags.length; i++) {
            DocletTag tag = tags[i];
            if (!isCustomTag(tag)) {
                continue;
            }
            String name = tag.getName();
            String value = tag.getValue();
//                String[] parameters = tag.getParameters();
            enhancer.insertClassAttribute(new CustomAttribute(name, value, null));
            log("class [" + javaClass.getFullyQualifiedName() + ']');
            log("\tattribute [" + name + ' ' + value + ']');
        }
    }

    /**
     * Parses the custom field attributes.
     *
     * @param javaField the java field
     * @param enhancer  the attribute enhancer
     */
    private void parseCustomAttributes(
            final JavaField javaField,
            final AttributeEnhancer enhancer) {
        DocletTag[] tags = javaField.getTags();
        for (int i = 0; i < tags.length; i++) {
            DocletTag tag = tags[i];
            if (!isCustomTag(tag)) {
                continue;
            }
            String name = tag.getName();
            String value = tag.getValue();
//                String[] parameters = tag.getParameters();
            enhancer.insertFieldAttribute(javaField, new CustomAttribute(name, value, null));
            log("field [" + javaField.getParentClass().getFullyQualifiedName() + '.' + javaField.getName() + ']');
            log("\tattribute [" + name + ' ' + value + ']');
        }
    }

    /**
     * Parses the custom method attributes.
     *
     * @param javaMethod the java method
     * @param enhancer   the attribute enhancer
     */
    private void parseCustomAttributes(
            final JavaMethod javaMethod,
            final AttributeEnhancer enhancer) {
        DocletTag[] tags = javaMethod.getTags();
        for (int i = 0; i < tags.length; i++) {
            DocletTag tag = tags[i];
            if (!isCustomTag(tag)) {
                continue;
            }
            String name = tag.getName();
            String value = tag.getValue();
//                String[] parameters = tag.getParameters();
            enhancer.insertMethodAttribute(javaMethod, new CustomAttribute(name, value, null));
            log("method [" + javaMethod.getParentClass().getFullyQualifiedName() + '.' + javaMethod.getName() + ']');
            log("\tattribute [" + name + ' ' + value + ']');
        }
    }

    /**
     * Parses the aspect attribute.
     *
     * @param javaClass the java class
     * @param enhancer  the attribute enhancer
     */
    private void parseAspect(
            final JavaClass javaClass,
            final AttributeEnhancer enhancer) {
        DocletTag aspectTag = javaClass.getTagByName(ATTR_ASPECT);
        if (aspectTag != null) {
            String name = aspectTag.getNamedParameter("name");
            String deploymentModel = null;
            String[] parameters = aspectTag.getParameters();
            for (int j = 0; j < parameters.length; j++) {
                if (parameters[j].startsWith("name=")) {
                    continue;
                }
                else {
                    deploymentModel = parameters[j];
                }
            }
            enhancer.insertClassAttribute(new AspectAttribute(name, deploymentModel));
            log("aspect [" + javaClass.getFullyQualifiedName() + ']');
            log("\tdeployment model [" + deploymentModel + ']');
        }
    }

    /**
     * Parses the execution pointcut attribute.
     *
     * @param javaField the java field
     * @param enhancer  the attribute enhancer
     */
    private void parseExecutionPointcut(
            final JavaField javaField,
            final AttributeEnhancer enhancer) {
        DocletTag pointcutTag = javaField.getTagByName(ATTR_EXECUTION);
        if (pointcutTag == null) {
            return;
        }
        String expression = pointcutTag.getValue();
        enhancer.insertFieldAttribute(
                javaField,
                new ExecutionAttribute(expression)
        );
        log("\texecution pointcut [" + javaField.getName() + "::" + expression + ']');
    }

    /**
     * Parses the call pointcut attribute.
     *
     * @param javaField the java field
     * @param enhancer  the attribute enhancer
     */
    private void parseCallPointcut(
            final JavaField javaField,
            final AttributeEnhancer enhancer) {
        DocletTag pointcutTag = javaField.getTagByName(ATTR_CALL);
        if (pointcutTag == null) {
            return;
        }
        String expression = pointcutTag.getValue();
        enhancer.insertFieldAttribute(
                javaField,
                new CallAttribute(expression)
        );
        log("\tcall pointcut [" + javaField.getName() + "::" + expression + ']');
    }

    /**
     * Parses the class pointcut attribute.
     *
     * @param javaField the java field
     * @param enhancer  the attribute enhancer
     */
    private void parseClassPointcut(
            final JavaField javaField,
            final AttributeEnhancer enhancer) {
        DocletTag pointcutTag = javaField.getTagByName(ATTR_CLASS);
        if (pointcutTag == null) {
            return;
        }
        String expression = pointcutTag.getValue();
        enhancer.insertFieldAttribute(
                javaField,
                new ClassAttribute(expression)
        );
        log("\tclass pointcut [" + javaField.getName() + "::" + expression + ']');
    }

    /**
     * Parses the set pointcut attribute.
     *
     * @param javaField the java field
     * @param enhancer  the attribute enhancer
     */
    private void parseSetPointcut(
            final JavaField javaField,
            final AttributeEnhancer enhancer) {
        DocletTag pointcutTag = javaField.getTagByName(ATTR_SET);
        if (pointcutTag == null) {
            return;
        }
        String expression = pointcutTag.getValue();
        enhancer.insertFieldAttribute(
                javaField,
                new SetAttribute(expression)
        );
        log("\tset pointcut [" + javaField.getName() + "::" + expression + ']');
    }

    /**
     * Parses the get pointcut attribute.
     *
     * @param javaField the java field
     * @param enhancer  the attribute enhancer
     */
    private void parseGetPointcut(
            final JavaField javaField,
            final AttributeEnhancer enhancer) {
        DocletTag pointcutTag = javaField.getTagByName(ATTR_GET);
        if (pointcutTag == null) {
            return;
        }
        String expression = pointcutTag.getValue();
        enhancer.insertFieldAttribute(
                javaField,
                new GetAttribute(expression)
        );
        log("\tget pointcut [" + javaField.getName() + "::" + expression + ']');
    }

    /**
     * Parses the throws pointcut attribute.
     *
     * @param javaField the java field
     * @param enhancer  the attribute enhancer
     */
    private void parseHandlerPointcut(
            final JavaField javaField,
            final AttributeEnhancer enhancer) {
        DocletTag pointcutTag = javaField.getTagByName(ATTR_HANDLER);
        if (pointcutTag == null) {
            return;
        }
        String expression = pointcutTag.getValue();
        enhancer.insertFieldAttribute(
                javaField,
                new HandlerAttribute(expression)
        );
        log("\thandler pointcut [" + javaField.getName() + "::" + expression + ']');
    }

    /**
     * Parses the cflow pointcut attribute.
     *
     * @param javaField the java field
     * @param enhancer  the attribute enhancer
     */
    private void parseCFlowPointcut(
            final JavaField javaField,
            final AttributeEnhancer enhancer) {
        DocletTag pointcutTag = javaField.getTagByName(ATTR_CFLOW);
        if (pointcutTag == null) {
            return;
        }
        String expression = pointcutTag.getValue();
        enhancer.insertFieldAttribute(
                javaField,
                new CFlowAttribute(expression)
        );
        log("\tcflow pointcut [" + javaField.getName() + "::" + expression + ']');
    }

    /**
     * Parses the implements attribute.
     *
     * @param javaField the java field
     * @param enhancer  the attribute enhancer
     */
    private void parseImplementsPointcut(
            final JavaField javaField,
            final AttributeEnhancer enhancer) {
        DocletTag pointcutTag = javaField.getTagByName(ATTR_IMPLEMENTS);
        if (pointcutTag == null) {
            return;
        }
        String expression = pointcutTag.getValue();
        enhancer.insertFieldAttribute(
                javaField,
                new ImplementsAttribute(expression)
        );
        log("\tinterface introduction [" + javaField.getType().getValue() + "::" + expression + ']');
    }

    /**
     * Parses the around advice attribute.
     *
     * @param javaMethod the java method
     * @param enhancer   the attribute enhancer
     */
    private void parseAroundAdvice(
            final JavaMethod javaMethod,
            final AttributeEnhancer enhancer) {
        DocletTag[] aroundAdviceTags = javaMethod.getTagsByName(ATTR_AROUND);
        for (int i = 0; i < aroundAdviceTags.length; i++) {
            DocletTag aroundAdviceTag = aroundAdviceTags[i];
            String name = aroundAdviceTag.getNamedParameter("name");
            StringBuffer buf = new StringBuffer();
            String[] parameters = aroundAdviceTag.getParameters();
            for (int j = 0; j < parameters.length; j++) {
                String parameter = parameters[j];
                if (parameter.startsWith("name=")) {
                    continue;
                }
                buf.append(parameter);
                buf.append(' ');
            }
            String expression = buf.toString().trim();
            enhancer.insertMethodAttribute(
                    javaMethod,
                    new AroundAttribute(name, expression)
            );
            log("\taround advice [" + javaMethod.getName() + "::" + expression + ']');
        }
    }

    /**
     * Parses the pre advice attribute.
     *
     * @param javaMethod the java method
     * @param enhancer   the attribute enhancer
     */
    private void parseBeforeAdvice(
            final JavaMethod javaMethod,
            final AttributeEnhancer enhancer) {
        DocletTag[] beforeAdviceTags = javaMethod.getTagsByName(ATTR_BEFORE);
        for (int i = 0; i < beforeAdviceTags.length; i++) {
            DocletTag beforeAdviceTag = beforeAdviceTags[i];
            String name = beforeAdviceTag.getNamedParameter("name");
            StringBuffer buf = new StringBuffer();
            String[] parameters = beforeAdviceTag.getParameters();
            for (int j = 0; j < parameters.length; j++) {
                String parameter = parameters[j];
                if (parameter.startsWith("name=")) {
                    continue;
                }
                buf.append(parameter);
                buf.append(' ');
            }
            String expression = buf.toString().trim();
            enhancer.insertMethodAttribute(
                    javaMethod,
                    new BeforeAttribute(name, expression)
            );
            log("\tbefore advice [" + javaMethod.getName() + "::" + expression + ']');
        }
    }

    /**
     * Parses the post advice attribute.
     *
     * @param javaMethod the java method
     * @param enhancer   the attribute enhancer
     */
    private void parseAfterAdvice(
            final JavaMethod javaMethod,
            final AttributeEnhancer enhancer) {
        DocletTag[] afterAdviceTags = javaMethod.getTagsByName(ATTR_AFTER);
        for (int i = 0; i < afterAdviceTags.length; i++) {
            DocletTag afterAdviceTag = afterAdviceTags[i];
            String name = afterAdviceTag.getNamedParameter("name");
            StringBuffer buf = new StringBuffer();
            String[] parameters = afterAdviceTag.getParameters();
            for (int j = 0; j < parameters.length; j++) {
                String parameter = parameters[j];
                if (parameter.startsWith("name=")) {
                    continue;
                }
                buf.append(parameter);
                buf.append(' ');
            }
            String expression = buf.toString().trim();
            enhancer.insertMethodAttribute(
                    javaMethod,
                    new AfterAttribute(name, expression)
            );
            log("\tafter advice [" + javaMethod.getName() + "::" + expression + ']');
        }
    }

    /**
     * Parse the @Introduce inner class introductions
     *
     * @param innerClass
     * @param enhancer
     */
    private void parseIntroduction(final JavaClass innerClass, final AttributeEnhancer enhancer) {
        DocletTag[] introductionTags = innerClass.getTagsByName(ATTR_INTRODUCE);
        // support multiples @Introduce tags
        for (int i = 0; i < introductionTags.length; i++) {
            DocletTag introductionTag = introductionTags[i];
            // deploymentModel= parameter
            String deploymentModel = introductionTag.getNamedParameter("deploymentModel");
            StringBuffer buf = new StringBuffer();
            String[] parameters = introductionTag.getParameters();
            for (int j = 0; j < parameters.length; j++) {
                String parameter = parameters[j];
                if (parameter.startsWith("deploymentModel=")) {
                    continue;
                }
                buf.append(parameter);
                buf.append(' ');
            }
            String expression = buf.toString().trim();
            //directly implemented interfaces
            JavaClass[] introducedInterfaceClasses = innerClass.getImplementedInterfaces();
            String[] introducedInterfaceNames = new String[introducedInterfaceClasses.length];
            for (int j = 0; j < introducedInterfaceClasses.length; j++) {
                introducedInterfaceNames[j] = introducedInterfaceClasses[j].getFullyQualifiedName();
                log("\tintroduction introduce [" + introducedInterfaceNames[j] + ']');
            }

//            // This snip shows that QDox builds up class hierarchy correctly
//            // [ extends junit.framework.TestCase in a mixin and remove junit.jar from path ]
//            // ONLY IF super classes are in system classpath
//            // ELSE ignores silently.
//            // This is not what we want for implicit interfaces
//            JavaClass base = innerClass;
//            while (base != null) {
//                System.out.println(base.getFullyQualifiedName());
//                base = base.getSuperJavaClass();
//            }

            // no explicit implements directive
            if (introducedInterfaceNames.length == 0) {
                introducedInterfaceNames =
                enhancer.getNearestInterfacesInHierarchy(innerClass.getFullyQualifiedName());
                if (introducedInterfaceNames.length == 0) {
                    throw new RuntimeException(
                            "no implicit interfaces found for " + innerClass.getFullyQualifiedName()
                    );
                }
                for (int j = 0; j < introducedInterfaceNames.length; j++) {
                    log("\tintroduction introduce implicit [" + introducedInterfaceNames[j] + ']');
                }
            }

            enhancer.insertClassAttribute(
                    new IntroduceAttribute(
                            expression,
                            innerClass.getFullyQualifiedName(),
                            introducedInterfaceNames,
                            deploymentModel
                    )
            );
            log("\tintroduction impl [" + innerClass.getName() + "::" + expression + "] " + deploymentModel);
        }
    }

    /**
     * Checks if the attribute is an aspectwerkz specific custom attribute. Ie the atribute starts with @Attribute.
     *
     * @param tag the tag
     * @return boolean
     */
    private boolean isCustomTag(final DocletTag tag) {
        return tag.getName().startsWith(ATTR_GENERIC_PREFIX);
    }

    /**
     * Logs a message.
     *
     * @param message the message to log
     * @TODO: do not log using System.out.println
     */
    private void log(final String message) {
        if (m_verbose) {
            System.out.println("AspectC::INFO - " + message);
        }
    }

    /**
     * Runs the compiler from the command line.
     *
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("AspectWerkz (c) 2002-2003 Jonas Bonér, Alexandre Vasseur");
            System.out.println(
                    "usage: java [options...] org.codehaus.aspectwerkz.definition.AspectC [-verbose] <path to src dir> <path to classes dir> [<path to destination dir>]"
            );
            System.out.println(
                    "       <path to destination dir> is optional, if omitted the compiled classes will be written to the initial directory"
            );
            System.out.println("       use -verbose to activate verbose logging");
            System.exit(0);
        }

        // analyse arguments and options
        List arguments = new ArrayList();
        List options = new ArrayList();
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                options.add(args[i]);
            }
            else {
                arguments.add(args[i]);
            }
        }

        AspectC compiler = new AspectC();
        compiler.setVerbose(options.contains("-verbose"));

        compiler.log("compiling attributes...");
        if (arguments.size() == 2) {
            compiler.compile((String)arguments.get(0), (String)arguments.get(1));
            compiler.log("compiled classes written to " + (String)arguments.get(1));
        }
        else {
            compiler.compile((String)arguments.get(0), (String)arguments.get(1), (String)arguments.get(2));
            compiler.log("compiled classes written to " + (String)arguments.get(2));
        }
        compiler.log("compilation successful");
    }
}
