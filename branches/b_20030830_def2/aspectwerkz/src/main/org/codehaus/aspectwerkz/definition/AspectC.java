/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;

import org.codehaus.aspectwerkz.metadata.QDoxParser;
import org.codehaus.aspectwerkz.definition.attribute.AspectAttribute;
import org.codehaus.aspectwerkz.definition.attribute.PointcutAttribute;
import org.codehaus.aspectwerkz.definition.attribute.AroundAdviceAttribute;
import org.codehaus.aspectwerkz.definition.attribute.PreAdviceAttribute;
import org.codehaus.aspectwerkz.definition.attribute.PostAdviceAttribute;
import org.codehaus.aspectwerkz.definition.attribute.IntroductionAttribute;
import org.codehaus.aspectwerkz.definition.attribute.AttributeEnhancer;
import org.codehaus.aspectwerkz.definition.attribute.bcel.BcelAttributeEnhancer;

/**
 * Compiles attributes for the aspects. Can be called from the command line.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AspectC {

    public static final String ATTR_ASPECT = "Aspect";
    public static final String ATTR_POINTCUT = "Pointcut";
    public static final String ATTR_AROUND_ADVICE = "AroundAdvice";
    public static final String ATTR_PRE_ADVICE = "PreAdvice";
    public static final String ATTR_POST_ADVICE = "PostAdvice";
    public static final String ATTR_INTRODUCTION = "Introduction";

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
            enhancer.initialize(className, classPath);

            if (qdoxParser.parse(className)) {
                JavaClass javaClass = qdoxParser.getJavaClass();
                boolean isAspect = parseAspect(javaClass, enhancer);

                if (isAspect) {
                    JavaMethod[] javaMethods = javaClass.getMethods();
                    for (int j = 0; j < javaMethods.length; j++) {
                        JavaMethod javaMethod = javaMethods[j];
                        parsePointcut(javaMethod, enhancer);
                        parseAroundAdvice(javaMethod, enhancer);
                        parsePreAdvice(javaMethod, enhancer);
                        parsePostAdvice(javaMethod, enhancer);
                        parseIntroduction(javaMethod, enhancer);
                    }
                    enhancer.write(destDir);
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
     * Parses the pointcut attribute.
     *
     * @param javaMethod the java method
     * @param enhancer the attribute enhancer
     */
    private static void parsePointcut(final JavaMethod javaMethod,
                                      final AttributeEnhancer enhancer) {
        DocletTag[] pointcutTags = javaMethod.getTagsByName(ATTR_POINTCUT);
        StringBuffer pointcutExpr = new StringBuffer();
        for (int k = 0; k < pointcutTags.length; k++) {
            pointcutExpr.append(pointcutTags[k].getValue());
        }
        if (pointcutTags.length != 0) {
            String expression = pointcutExpr.toString();
            enhancer.insertMethodAttribute(
                    javaMethod,
                    new PointcutAttribute(expression)
            );
            log("\tpointcut [" + javaMethod.getName() + "::" + expression + "]");
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
        DocletTag[] aroundAdviceTags = javaMethod.getTagsByName(ATTR_AROUND_ADVICE);
        StringBuffer aroundAdviceExpr = new StringBuffer();
        for (int k = 0; k < aroundAdviceTags.length; k++) {
            aroundAdviceExpr.append(aroundAdviceTags[k].getValue());
        }
        if (aroundAdviceTags.length != 0) {
            String expression = aroundAdviceExpr.toString();
            enhancer.insertMethodAttribute(
                    javaMethod,
                    new AroundAdviceAttribute(expression)
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
    private static void parsePreAdvice(final JavaMethod javaMethod,
                                       final AttributeEnhancer enhancer) {
        DocletTag[] preAdviceTags = javaMethod.getTagsByName(ATTR_PRE_ADVICE);
        StringBuffer preAdviceExpr = new StringBuffer();
        for (int k = 0; k < preAdviceTags.length; k++) {
            preAdviceExpr.append(preAdviceTags[k].getValue());
        }
        if (preAdviceTags.length != 0) {
            String expression = preAdviceExpr.toString();
            enhancer.insertMethodAttribute(
                    javaMethod,
                    new PreAdviceAttribute(expression)
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
    private static void parsePostAdvice(final JavaMethod javaMethod,
                                        final AttributeEnhancer enhancer) {
        DocletTag[] postAdviceTags = javaMethod.getTagsByName(ATTR_POST_ADVICE);
        StringBuffer postAdviceExpr = new StringBuffer();
        for (int k = 0; k < postAdviceTags.length; k++) {
            postAdviceExpr.append(postAdviceTags[k].getValue());
        }
        if (postAdviceTags.length != 0) {
            String expression = postAdviceExpr.toString();
            enhancer.insertMethodAttribute(
                    javaMethod,
                    new PostAdviceAttribute(expression)
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
        DocletTag[] introductionTags = javaMethod.getTagsByName(ATTR_INTRODUCTION);
        StringBuffer introductionExpr = new StringBuffer();
        for (int k = 0; k < introductionTags.length; k++) {
            introductionExpr.append(introductionTags[k].getValue());
        }
        if (introductionTags.length != 0) {
            String expression = introductionExpr.toString();
            enhancer.insertMethodAttribute(
                    javaMethod,
                    new IntroductionAttribute(expression)
            );
            log("\tintroduction [" + javaMethod.getName() + "::" + expression + "]");
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
            System.out.println("usage: java [options...] org.codehaus.aspectwerkz.definition.AspectC <path to src dir> <path to classes dir> <path to destination dir>");
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
