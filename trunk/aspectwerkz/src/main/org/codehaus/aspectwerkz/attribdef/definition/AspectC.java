/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.attribdef.definition;

import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaField;

import org.codehaus.aspectwerkz.metadata.QDoxParser;
import org.codehaus.aspectwerkz.attribdef.definition.attribute.AttributeEnhancer;
import org.codehaus.aspectwerkz.attribdef.definition.attribute.AspectAttribute;
import org.codehaus.aspectwerkz.attribdef.definition.attribute.AroundAttribute;
import org.codehaus.aspectwerkz.attribdef.definition.attribute.BeforeAttribute;
import org.codehaus.aspectwerkz.attribdef.definition.attribute.AfterAttribute;
import org.codehaus.aspectwerkz.attribdef.definition.attribute.IntroduceAttribute;
import org.codehaus.aspectwerkz.attribdef.definition.attribute.ImplementsAttribute;
import org.codehaus.aspectwerkz.attribdef.definition.attribute.ExecutionAttribute;
import org.codehaus.aspectwerkz.attribdef.definition.attribute.CallAttribute;
import org.codehaus.aspectwerkz.attribdef.definition.attribute.ClassAttribute;
import org.codehaus.aspectwerkz.attribdef.definition.attribute.SetAttribute;
import org.codehaus.aspectwerkz.attribdef.definition.attribute.GetAttribute;
import org.codehaus.aspectwerkz.attribdef.definition.attribute.ThrowsAttribute;
import org.codehaus.aspectwerkz.attribdef.definition.attribute.CFlowAttribute;
import org.codehaus.aspectwerkz.attribdef.definition.attribute.bcel.BcelAttributeEnhancer;
import org.codehaus.aspectwerkz.Alex;

import java.util.ArrayList;
import java.util.List;

/**
 * Compiles attributes for the aspects. Can be called from the command line.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class AspectC {

    public static final String ATTR_ASPECT = "Aspect";
    public static final String ATTR_EXECUTION = "Execution";
    public static final String ATTR_CALL = "Call";
    public static final String ATTR_CLASS = "Class";
    public static final String ATTR_SET = "Set";
    public static final String ATTR_GET = "Get";
    public static final String ATTR_THROWS = "Throws";
    public static final String ATTR_CFLOW = "CFlow";
    public static final String ATTR_AROUND = "Around";
    public static final String ATTR_BEFORE = "Before";
    public static final String ATTR_AFTER = "After";
    public static final String ATTR_INTRODUCE = "Introduce";
    public static final String ATTR_IMPLEMENTS = "Implements";

    /** verbose log */
    private boolean verbose = false;

    /**
     * Set verbose mode
     *
     * @param verbose
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Compiles attributes for the aspects.
     *
     * @param sourcePath the path to the sources to compile attributes for
     * @param classPath the path to the compiled classes matching the source files
     */
    public void compile(final String sourcePath, final String classPath) {
        compile(sourcePath, classPath, classPath);
    }

    /**
     * Compiles attributes for the aspects.
     *
     * @param sourcePath the path to the sources to compile attributes for
     * @param classPath the path to the compiled classes matching the source files
     * @param destDir the path where to write the compiled aspects
     */
    public void compile(final String sourcePath,
                               final String classPath,
                               final String destDir) {
        if (sourcePath == null) throw new IllegalArgumentException("source path can not be null");
        if (classPath == null) throw new IllegalArgumentException("class path can not be null");

        final QDoxParser qdoxParser = new QDoxParser(sourcePath);
        String[] classNames = qdoxParser.getAllClassNames();

        for (int i = 0; i < classNames.length; i++) {
            String className = classNames[i];

            AttributeEnhancer enhancer = new BcelAttributeEnhancer(); // TODO: use factory
            if (enhancer.initialize(className, classPath)) {

                if (qdoxParser.parse(className)) {
                    JavaClass javaClass = qdoxParser.getJavaClass();
                    boolean isAspect = parseAspect(javaClass, enhancer);

                    if (isAspect) {
                        JavaField[] javaFields = javaClass.getFields();
                        for (int j = 0; j < javaFields.length; j++) {
                            JavaField javaField = javaFields[j];
                            parseExecutionPointcut(javaField, enhancer);
                            parseCallPointcut(javaField, enhancer);
                            parseClassPointcut(javaField, enhancer);
                            parseSetPointcut(javaField, enhancer);
                            parseGetPointcut(javaField, enhancer);
                            parseThrowsPointcut(javaField, enhancer);
                            parseCFlowPointcut(javaField, enhancer);
                            parseImplementsPointcut(javaField, enhancer);
                        }

                        JavaMethod[] javaMethods = javaClass.getMethods();
                        for (int j = 0; j < javaMethods.length; j++) {
                            JavaMethod javaMethod = javaMethods[j];
                            parseAroundAdvice(javaMethod, enhancer);
                            parseBeforeAdvice(javaMethod, enhancer);
                            parseAfterAdvice(javaMethod, enhancer);
                        }

                        JavaClass[] innerClasses = javaClass.getInnerClasses();
                        for (int k = 0; k < innerClasses.length; k++) {
                            parseIntroduction(innerClasses[k], enhancer);
                        }

                        enhancer.write(destDir);
                    }
                }
            }
        }
    }

    /**
     * Parses the aspect attribute.
     *
     * @param javaClass the java class
     * @param enhancer the attribute enhancer
     */
    private boolean parseAspect(final JavaClass javaClass,
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
            log("compiling aspect [" + javaClass.getName() + "]");
            log("\tdeployment model [" + deploymentModel + "]");
            return true;
        }
        return false;
    }

    /**
     * Parses the execution pointcut attribute.
     *
     * @param javaField the java field
     * @param enhancer the attribute enhancer
     */
    private void parseExecutionPointcut(final JavaField javaField,
                                               final AttributeEnhancer enhancer) {
        DocletTag pointcutTag = javaField.getTagByName(ATTR_EXECUTION);
        if (pointcutTag == null) return;
        String expression = pointcutTag.getValue();
        enhancer.insertFieldAttribute(
                javaField.getName(),
                new ExecutionAttribute(expression)
        );
        log("\texecution pointcut [" + javaField.getName() + "::" + expression + "]");
    }

    /**
     * Parses the call pointcut attribute.
     *
     * @param javaField the java field
     * @param enhancer the attribute enhancer
     */
    private void parseCallPointcut(final JavaField javaField,
                                          final AttributeEnhancer enhancer) {
        DocletTag pointcutTag = javaField.getTagByName(ATTR_CALL);
        if (pointcutTag == null) return;
        String expression = pointcutTag.getValue();
        enhancer.insertFieldAttribute(
                javaField.getName(),
                new CallAttribute(expression)
        );
        log("\tcall pointcut [" + javaField.getName() + "::" + expression + "]");
    }

    /**
     * Parses the class pointcut attribute.
     *
     * @param javaField the java field
     * @param enhancer the attribute enhancer
     */
    private void parseClassPointcut(final JavaField javaField,
                                           final AttributeEnhancer enhancer) {
        DocletTag pointcutTag = javaField.getTagByName(ATTR_CLASS);
        if (pointcutTag == null) return;
        String expression = pointcutTag.getValue();
        enhancer.insertFieldAttribute(
                javaField.getName(),
                new ClassAttribute(expression)
        );
        log("\tclass pointcut [" + javaField.getName() + "::" + expression + "]");
    }

    /**
     * Parses the set pointcut attribute.
     *
     * @param javaField the java field
     * @param enhancer the attribute enhancer
     */
    private void parseSetPointcut(final JavaField javaField,
                                         final AttributeEnhancer enhancer) {
        DocletTag pointcutTag = javaField.getTagByName(ATTR_SET);
        if (pointcutTag == null) return;
        String expression = pointcutTag.getValue();
        enhancer.insertFieldAttribute(
                javaField.getName(),
                new SetAttribute(expression)
        );
        log("\tset pointcut [" + javaField.getName() + "::" + expression + "]");
    }

    /**
     * Parses the get pointcut attribute.
     *
     * @param javaField the java field
     * @param enhancer the attribute enhancer
     */
    private void parseGetPointcut(final JavaField javaField,
                                         final AttributeEnhancer enhancer) {
        DocletTag pointcutTag = javaField.getTagByName(ATTR_GET);
        if (pointcutTag == null) return;
        String expression = pointcutTag.getValue();
        enhancer.insertFieldAttribute(
                javaField.getName(),
                new GetAttribute(expression)
        );
        log("\tget pointcut [" + javaField.getName() + "::" + expression + "]");
    }

    /**
     * Parses the throws pointcut attribute.
     *
     * @param javaField the java field
     * @param enhancer the attribute enhancer
     */
    private void parseThrowsPointcut(final JavaField javaField,
                                            final AttributeEnhancer enhancer) {
        DocletTag pointcutTag = javaField.getTagByName(ATTR_THROWS);
        if (pointcutTag == null) return;
        String expression = pointcutTag.getValue();
        enhancer.insertFieldAttribute(
                javaField.getName(),
                new ThrowsAttribute(expression)
        );
        log("\tthrows pointcut [" + javaField.getName() + "::" + expression + "]");
    }

    /**
     * Parses the cflow pointcut attribute.
     *
     * @param javaField the java field
     * @param enhancer the attribute enhancer
     */
    private void parseCFlowPointcut(final JavaField javaField,
                                           final AttributeEnhancer enhancer) {
        DocletTag pointcutTag = javaField.getTagByName(ATTR_CFLOW);
        if (pointcutTag == null) return;
        String expression = pointcutTag.getValue();
        enhancer.insertFieldAttribute(
                javaField.getName(),
                new CFlowAttribute(expression)
        );
        log("\tcflow pointcut [" + javaField.getName() + "::" + expression + "]");
    }

    /**
     * Parses the implements attribute.
     *
     * @param javaField the java field
     * @param enhancer the attribute enhancer
     */
    private void parseImplementsPointcut(final JavaField javaField,
                                                final AttributeEnhancer enhancer) {
        DocletTag pointcutTag = javaField.getTagByName(ATTR_IMPLEMENTS);
        if (pointcutTag == null) return;
        String expression = pointcutTag.getValue();
        enhancer.insertFieldAttribute(
                javaField.getName(),
                new ImplementsAttribute(expression)
        );
        log("\tinterface introduction [" + javaField.getType().getValue() + "::" + expression + "]");
    }

    /**
     * Parses the around advice attribute.
     *
     * @param javaMethod the java method
     * @param enhancer the attribute enhancer
     */
    private void parseAroundAdvice(final JavaMethod javaMethod,
                                          final AttributeEnhancer enhancer) {
        DocletTag[] aroundAdviceTags = javaMethod.getTagsByName(ATTR_AROUND);
        for (int i = 0; i < aroundAdviceTags.length; i++) {
            DocletTag aroundAdviceTag = aroundAdviceTags[i];
            String name = aroundAdviceTag.getNamedParameter("name");
            StringBuffer buf = new StringBuffer();
            String[] parameters = aroundAdviceTag.getParameters();
            for (int j = 0; j < parameters.length; j++) {
                String parameter = parameters[j];
                if (parameter.startsWith("name=")) continue;
                buf.append(parameter);
                buf.append(' ');
            }
            String expression = buf.toString().trim();
            enhancer.insertMethodAttribute(
                    javaMethod,
                    new AroundAttribute(name, expression)
            );
            log("\taround advice [" + javaMethod.getName() + "::" + expression + "]");
        }
    }

    /**
     * Parses the pre advice attribute.
     *
     * @param javaMethod the java method
     * @param enhancer the attribute enhancer
     */
    private void parseBeforeAdvice(final JavaMethod javaMethod,
                                          final AttributeEnhancer enhancer) {
        DocletTag[] beforeAdviceTags = javaMethod.getTagsByName(ATTR_BEFORE);
        for (int i = 0; i < beforeAdviceTags.length; i++) {
            DocletTag beforeAdviceTag = beforeAdviceTags[i];
            String name = beforeAdviceTag.getNamedParameter("name");
            StringBuffer buf = new StringBuffer();
            String[] parameters = beforeAdviceTag.getParameters();
            for (int j = 0; j < parameters.length; j++) {
                String parameter = parameters[j];
                if (parameter.startsWith("name=")) continue;
                buf.append(parameter);
                buf.append(' ');
            }
            String expression = buf.toString().trim();
            enhancer.insertMethodAttribute(
                    javaMethod,
                    new BeforeAttribute(name, expression)
            );
            log("\tbefore advice [" + javaMethod.getName() + "::" + expression + "]");
        }
    }

    /**
     * Parses the post advice attribute.
     *
     * @param javaMethod the java method
     * @param enhancer the attribute enhancer
     */
    private void parseAfterAdvice(final JavaMethod javaMethod,
                                         final AttributeEnhancer enhancer) {
        DocletTag[] afterAdviceTags = javaMethod.getTagsByName(ATTR_AFTER);
        for (int i = 0; i < afterAdviceTags.length; i++) {
            DocletTag afterAdviceTag = afterAdviceTags[i];
            String name = afterAdviceTag.getNamedParameter("name");
            StringBuffer buf = new StringBuffer();
            String[] parameters = afterAdviceTag.getParameters();
            for (int j = 0; j < parameters.length; j++) {
                String parameter = parameters[j];
                if (parameter.startsWith("name=")) continue;
                buf.append(parameter);
                buf.append(' ');
            }
            String expression = buf.toString().trim();
            enhancer.insertMethodAttribute(
                    javaMethod,
                    new AfterAttribute(name, expression)
            );
            log("\tafter advice [" + javaMethod.getName() + "::" + expression + "]");
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
        for (int i = 0; i < introductionTags.length; i++) {
            DocletTag introductionTag = introductionTags[i];
            String expression = introductionTag.getValue();
            JavaClass[] introducedInterfaceClasses = innerClass.getImplementedInterfaces();
            String[] introducedInterfaceNames = new String[introducedInterfaceClasses.length];
            for (int j = 0; j < introducedInterfaceClasses.length; j++) {
                introducedInterfaceNames[j] = introducedInterfaceClasses[j].getFullyQualifiedName();
                log("\tintroduction introduce [" + introducedInterfaceNames[j] +"]");
            }
            enhancer.insertClassAttribute(
                    new IntroduceAttribute(
                            expression,
                            innerClass.getFullyQualifiedName(),
                            introducedInterfaceNames)
            );
            log("\tintroduction impl [" + innerClass.getName() + "::" + expression + "]");
        }
    }

    /**
     * Logs a message.
     *
     * @TODO: do not log using System.out.println
     *
     * @param message the message to log
     */
    private void log(final String message) {
        if (verbose)
            System.out.println("AspectC::INFO - " + message);
    }

    /**
     * Runs the compiler from the command line.
     *
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("AspectWerkz (c) 2002-2003 The AspectWerkz Team");
            System.out.println("usage: java [options...] org.codehaus.aspectwerkz.attribdef.definition.AspectC [-verbose] <path to src dir> <path to classes dir> [<path to destination dir>]");
            System.out.println("       <path to destination dir> is optional, if omitted the compiled aspects will be written to the initial directory");
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

        compiler.log("compiling aspects...");
        if (arguments.size() == 2) {
            compiler.compile((String)arguments.get(0), (String)arguments.get(1));
            compiler.log("compiled aspects written to " + (String)arguments.get(1));
        }
        else {
            compiler.compile((String)arguments.get(0), (String)arguments.get(1), (String)arguments.get(2));
            compiler.log("compiled aspects written to " + (String)arguments.get(2));
        }
        compiler.log("compilation successful");
    }
}
