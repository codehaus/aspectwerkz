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

/**
 * Compiles attributes for the aspects. Can be called from the command line.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
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

    /**
     * Compiles attributes for the aspects.
     *
     * @param sourcePath the path to the sources to compile attributes for
     * @param classPath the path to the compiled classes matching the source files
     */
    public static void compile(final String sourcePath, final String classPath) {
        compile(sourcePath, classPath, classPath);
    }

    /**
     * Compiles attributes for the aspects.
     *
     * @param sourcePath the path to the sources to compile attributes for
     * @param classPath the path to the compiled classes matching the source files
     * @param destDir the path where to write the compiled aspects
     */
    public static void compile(final String sourcePath,
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
                            parseIntroduction(javaMethod, enhancer);
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
    private static boolean parseAspect(final JavaClass javaClass,
                                       final AttributeEnhancer enhancer) {
        DocletTag[] aspectTags = javaClass.getTagsByName(ATTR_ASPECT);
        for (int j = 0; j < aspectTags.length; j++) {
            String deploymentModel = aspectTags[j].getValue();
            enhancer.insertClassAttribute(new AspectAttribute(deploymentModel));
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
    private static void parseExecutionPointcut(final JavaField javaField,
                                               final AttributeEnhancer enhancer) {
        DocletTag[] pointcutTags = javaField.getTagsByName(ATTR_EXECUTION);
        StringBuffer pointcutExpr = new StringBuffer();
        for (int k = 0; k < pointcutTags.length; k++) {
            pointcutExpr.append(pointcutTags[k].getValue());
        }
        if (pointcutTags.length != 0) {
            String expression = pointcutExpr.toString();
            enhancer.insertFieldAttribute(
                    javaField.getName(),
                    new ExecutionAttribute(expression)
            );
            log("\texecution pointcut [" + javaField.getName() + "::" + expression + "]");
        }
    }

    /**
     * Parses the call pointcut attribute.
     *
     * @param javaField the java field
     * @param enhancer the attribute enhancer
     */
    private static void parseCallPointcut(final JavaField javaField,
                                          final AttributeEnhancer enhancer) {
        DocletTag[] pointcutTags = javaField.getTagsByName(ATTR_CALL);
        StringBuffer pointcutExpr = new StringBuffer();
        for (int k = 0; k < pointcutTags.length; k++) {
            pointcutExpr.append(pointcutTags[k].getValue());
        }
        if (pointcutTags.length != 0) {
            String expression = pointcutExpr.toString();
            enhancer.insertFieldAttribute(
                    javaField.getName(),
                    new CallAttribute(expression)
            );
            log("\tcall pointcut [" + javaField.getName() + "::" + expression + "]");
        }
    }

    /**
     * Parses the class pointcut attribute.
     *
     * @param javaField the java field
     * @param enhancer the attribute enhancer
     */
    private static void parseClassPointcut(final JavaField javaField,
                                           final AttributeEnhancer enhancer) {
        DocletTag[] pointcutTags = javaField.getTagsByName(ATTR_CLASS);
        StringBuffer pointcutExpr = new StringBuffer();
        for (int k = 0; k < pointcutTags.length; k++) {
            pointcutExpr.append(pointcutTags[k].getValue());
        }
        if (pointcutTags.length != 0) {
            String expression = pointcutExpr.toString();
            enhancer.insertFieldAttribute(
                    javaField.getName(),
                    new ClassAttribute(expression)
            );
            log("\tclass pointcut [" + javaField.getName() + "::" + expression + "]");
        }
    }

    /**
     * Parses the set pointcut attribute.
     *
     * @param javaField the java field
     * @param enhancer the attribute enhancer
     */
    private static void parseSetPointcut(final JavaField javaField,
                                         final AttributeEnhancer enhancer) {
        DocletTag[] pointcutTags = javaField.getTagsByName(ATTR_SET);
        StringBuffer pointcutExpr = new StringBuffer();
        for (int k = 0; k < pointcutTags.length; k++) {
            pointcutExpr.append(pointcutTags[k].getValue());
        }
        if (pointcutTags.length != 0) {
            String expression = pointcutExpr.toString();
            enhancer.insertFieldAttribute(
                    javaField.getName(),
                    new SetAttribute(expression)
            );
            log("\tset pointcut [" + javaField.getName() + "::" + expression + "]");
        }
    }

    /**
     * Parses the get pointcut attribute.
     *
     * @param javaField the java field
     * @param enhancer the attribute enhancer
     */
    private static void parseGetPointcut(final JavaField javaField,
                                         final AttributeEnhancer enhancer) {
        DocletTag[] pointcutTags = javaField.getTagsByName(ATTR_GET);
        StringBuffer pointcutExpr = new StringBuffer();
        for (int k = 0; k < pointcutTags.length; k++) {
            pointcutExpr.append(pointcutTags[k].getValue());
        }
        if (pointcutTags.length != 0) {
            String expression = pointcutExpr.toString();
            enhancer.insertFieldAttribute(
                    javaField.getName(),
                    new GetAttribute(expression)
            );
            log("\tget pointcut [" + javaField.getName() + "::" + expression + "]");
        }
    }

    /**
     * Parses the throws pointcut attribute.
     *
     * @param javaField the java field
     * @param enhancer the attribute enhancer
     */
    private static void parseThrowsPointcut(final JavaField javaField,
                                            final AttributeEnhancer enhancer) {
        DocletTag[] pointcutTags = javaField.getTagsByName(ATTR_THROWS);
        StringBuffer pointcutExpr = new StringBuffer();
        for (int k = 0; k < pointcutTags.length; k++) {
            pointcutExpr.append(pointcutTags[k].getValue());
        }
        if (pointcutTags.length != 0) {
            String expression = pointcutExpr.toString();
            enhancer.insertFieldAttribute(
                    javaField.getName(),
                    new ThrowsAttribute(expression)
            );
            log("\tthrows pointcut [" + javaField.getName() + "::" + expression + "]");
        }
    }

    /**
     * Parses the cflow pointcut attribute.
     *
     * @param javaField the java field
     * @param enhancer the attribute enhancer
     */
    private static void parseCFlowPointcut(final JavaField javaField,
                                           final AttributeEnhancer enhancer) {
        DocletTag[] pointcutTags = javaField.getTagsByName(ATTR_CFLOW);
        StringBuffer pointcutExpr = new StringBuffer();
        for (int k = 0; k < pointcutTags.length; k++) {
            pointcutExpr.append(pointcutTags[k].getValue());
        }
        if (pointcutTags.length != 0) {
            String expression = pointcutExpr.toString();
            enhancer.insertFieldAttribute(
                    javaField.getName(),
                    new CFlowAttribute(expression)
            );
            log("\tcflow pointcut [" + javaField.getName() + "::" + expression + "]");
        }
    }

    /**
     * Parses the implements attribute.
     *
     * @param javaField the java field
     * @param enhancer the attribute enhancer
     */
    private static void parseImplementsPointcut(final JavaField javaField,
                                                final AttributeEnhancer enhancer) {
        DocletTag[] pointcutTags = javaField.getTagsByName(ATTR_IMPLEMENTS);
        StringBuffer pointcutExpr = new StringBuffer();
        for (int k = 0; k < pointcutTags.length; k++) {
            pointcutExpr.append(pointcutTags[k].getValue());
        }
        if (pointcutTags.length != 0) {
            String expression = pointcutExpr.toString();
            enhancer.insertFieldAttribute(
                    javaField.getName(),
                    new ImplementsAttribute(expression)
            );
            log("\tinterface introduction [" + javaField.getType().getValue() + "::" + expression + "]");
        }
    }

    /**
     * Parses the around advice attribute.
     *
     * @param javaMethod the java method
     * @param enhancer the attribute enhancer
     */
    private static void parseAroundAdvice(final JavaMethod javaMethod,
                                          final AttributeEnhancer enhancer) {
        DocletTag[] aroundAdviceTags = javaMethod.getTagsByName(ATTR_AROUND);
        StringBuffer aroundAdviceExpr = new StringBuffer();
        for (int k = 0; k < aroundAdviceTags.length; k++) {
            aroundAdviceExpr.append(aroundAdviceTags[k].getValue());
        }
        if (aroundAdviceTags.length != 0) {
            String expression = aroundAdviceExpr.toString();
            enhancer.insertMethodAttribute(
                    javaMethod,
                    new AroundAttribute(expression)
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
    private static void parseBeforeAdvice(final JavaMethod javaMethod,
                                          final AttributeEnhancer enhancer) {
        DocletTag[] preAdviceTags = javaMethod.getTagsByName(ATTR_BEFORE);
        StringBuffer preAdviceExpr = new StringBuffer();
        for (int k = 0; k < preAdviceTags.length; k++) {
            preAdviceExpr.append(preAdviceTags[k].getValue());
        }
        if (preAdviceTags.length != 0) {
            String expression = preAdviceExpr.toString();
            enhancer.insertMethodAttribute(
                    javaMethod,
                    new BeforeAttribute(expression)
            );
            log("\tpre advice [" + javaMethod.getName() + "::" + expression + "]");
        }
    }

    /**
     * Parses the post advice attribute.
     *
     * @param javaMethod the java method
     * @param enhancer the attribute enhancer
     */
    private static void parseAfterAdvice(final JavaMethod javaMethod,
                                         final AttributeEnhancer enhancer) {
        DocletTag[] postAdviceTags = javaMethod.getTagsByName(ATTR_AFTER);
        StringBuffer postAdviceExpr = new StringBuffer();
        for (int k = 0; k < postAdviceTags.length; k++) {
            postAdviceExpr.append(postAdviceTags[k].getValue());
        }
        if (postAdviceTags.length != 0) {
            String expression = postAdviceExpr.toString();
            enhancer.insertMethodAttribute(
                    javaMethod,
                    new AfterAttribute(expression)
            );
            log("\tpost advice [" + javaMethod.getName() + "::" + expression + "]");
        }
    }

    /**
     * Parses the introduction attribute.
     *
     * @param javaMethod the java method
     * @param enhancer the attribute enhancer
     */
    private static void parseIntroduction(final JavaMethod javaMethod,
                                          final AttributeEnhancer enhancer) {
        DocletTag[] introductionTags = javaMethod.getTagsByName(ATTR_INTRODUCE);
        StringBuffer introductionExpr = new StringBuffer();
        for (int k = 0; k < introductionTags.length; k++) {
            introductionExpr.append(introductionTags[k].getValue());
        }
        if (introductionTags.length != 0) {
            String expression = introductionExpr.toString();
            enhancer.insertMethodAttribute(
                    javaMethod,
                    new IntroduceAttribute(expression)
            );
            log("\tmethod introduction [" + javaMethod.getName() + "::" + expression + "]");
        }
    }

    /**
     * Logs a message.
     *
     * @TODO: do not log using System.out.println
     *
     * @param message the message to log
     */
    private static void log(final String message) {
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
            System.out.println("usage: java [options...] org.codehaus.aspectwerkz.attribdef.definition.AspectC <path to src dir> <path to classes dir> <path to destination dir>");
            System.out.println("       <path to destination dir> is optional, if omitted the compiled aspects will be written to the initial directory");
            System.exit(0);
        }
        System.out.println("compiling aspects...");
        if (args.length == 2) {
            AspectC.compile(args[0], args[1]);
            System.out.println("compiled aspects written to " + args[1]);
        }
        else {
            AspectC.compile(args[0], args[1], args[2]);
            System.out.println("compiled aspects written to " + args[2]);
        }
        System.out.println("compilation successful");
    }
}
