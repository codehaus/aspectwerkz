/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;

import org.apache.xmlbeans.impl.jam.*;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.annotation.instrumentation.AttributeEnhancer;
import org.codehaus.aspectwerkz.annotation.instrumentation.bcel.BcelAttributeEnhancer;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.net.URLClassLoader;
import java.net.URL;

/**
 * Annotation compiler.
 * <p/>
 * Extracts the annotations from JavaDoc tags and inserts them into the bytecode of the class.
 * <p/>
 * Uses JAM, which allows the annotations to be strongly typed and to be swappable to JSR-175 annotations.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AnnotationC {
    public static final String ANNOTATION_ASPECT = "Aspect";
    public static final String ANNOTATION_AROUND = "Around";
    public static final String ANNOTATION_BEFORE = "Before";
    public static final String ANNOTATION_AFTER = "After";
    public static final String ANNOTATION_EXPRESSION = "Expression";
    public static final String ANNOTATION_IMPLEMENTS = "Implements";
    public static final String ANNOTATION_INTRODUCE = "Introduce";

    /**
     * Verbose logging.
     */
    private static boolean s_verbose = false;

    /**
     * The user defined annotations.
     * <p/>
     * [annotation-name:annotation-proxy-class-name] pairs
     */
    private static Map s_userAnnotations = new HashMap();

    /**
     * The class loader.
     */
    private static URLClassLoader s_loader;

    /**
     * Runs the compiler from the command line.
     *
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("AspectWerkz (c) 2002-2004 Jonas Bonér, Alexandre Vasseur");
            System.out.println(
                    "usage: java [options...] org.codehaus.aspectwerkz.annotation.AnnotationC [-verbose] <path to src dir> <path to classes dir> [<path to destination dir>]"
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
            } else {
                arguments.add(args[i]);
            }
        }
        s_verbose = options.contains("-verbose");
        log("compiling annotations...");
        log("(NOTE: if no output is seen, then nothing is compiled)");
        if (arguments.size() == 2) {
            compile((String)arguments.get(0), (String)arguments.get(1));
            log("compiled classes written to " + arguments.get(1));
        } else {
            compile((String)arguments.get(0), (String)arguments.get(1), (String)arguments.get(2));
            log("compiled classes written to " + arguments.get(2));
        }
        log("compilation successful");
    }

    /**
     * Compiles attributes for the aspects.
     *
     * @param sourcePath the path to the sources to compile attributes for
     * @param classPath  the path to the compiled classes matching the source files
     */
    public static void compile(final String sourcePath, final String classPath) {
        compile(sourcePath, classPath, classPath);
    }

    /**
     * Compiles attributes for the aspects.
     *
     * @param sourcePath the path to the sources to compile attributes for
     * @param classPath  the path to the compiled classes matching the source files
     * @param destDir    the path where to write the compiled aspects
     */
    public static void compile(final String sourcePath, final String classPath, final String destDir) {
        if (sourcePath == null) {
            throw new IllegalArgumentException("source path can not be null");
        }
        if (classPath == null) {
            throw new IllegalArgumentException("class path can not be null");
        }
        try {
            s_loader =
            new URLClassLoader(new URL[]{new File(classPath).toURL()}, ClassLoader.getSystemClassLoader());
            JamServiceFactory factory = JamServiceFactory.getInstance();
            JamServiceParams params = factory.createServiceParams();

            // parse XML and register all use def. annotations
            retrieveUserDefinedAnnotations();

            // register the annotations of interest
            registerSystemAnnotations(params);
            registerUserDefinedAnnotations(params);

            // register the source files of interest
            params.includeSourcePattern(new File[]{new File(sourcePath)}, "**/*.java");

            // get all the classes
            JamService service = factory.createService(params);
            JClass[] classes = service.getAllClasses();
            for (int i = 0; i < classes.length; i++) {
                JClass clazz = classes[i];
                AttributeEnhancer enhancer = new BcelAttributeEnhancer();
                if (enhancer.initialize(clazz.getQualifiedName(), classPath, clazz.isStatic())) {
                    // parse the annotations
                    if (clazz.isStatic()) {
                        handleInnerClassAnnotations(enhancer, clazz);
                    } else {
                        handleClassAnnotations(enhancer, clazz);
                    }
                    JMethod[] methods = clazz.getDeclaredMethods();
                    for (int j = 0; j < methods.length; j++) {
                        handleMethodAnnotations(enhancer, methods[j]);
                    }
                    JField[] fields = clazz.getDeclaredFields();
                    for (int j = 0; j < fields.length; j++) {
                        handleFieldAnnotations(enhancer, fields[j]);
                    }

                    // write enhanced class to disk
                    enhancer.write(destDir);
                }
            }
        } catch (IOException e) {
            throw new DefinitionException(e.toString(), e);
        }
    }

    public static String convertToJavaStyleInnerClassFileName(final String classFileName) {
        String newClassFileName;
        int index = classFileName.lastIndexOf('/');
        if (index == -1) {
            return classFileName;
        } else {
            newClassFileName = classFileName.substring(0, index) + '$' +
                               classFileName.substring(index + 1, classFileName.length());
            return newClassFileName;
        }
    }

    public static String convertToJavaStyleInnerClassName(final String classFileName) {
        String newClassFileName;
        int index = classFileName.lastIndexOf('.');
        if (index == -1) {
            return classFileName;
        } else {
            newClassFileName = classFileName.substring(0, index) + '$' +
                               classFileName.substring(index + 1, classFileName.length());
            return newClassFileName;
        }
    }

    private static void retrieveUserDefinedAnnotations() {
    }

    private static void handleClassAnnotations(final AttributeEnhancer enhancer, final JClass clazz) {
        JAnnotation ann = clazz.getAnnotation(ANNOTATION_ASPECT);
        if (ann != null) {
            AspectAnnotationProxy aspectProxy = (AspectAnnotationProxy)ann.getProxy();
            enhancer.insertClassAttribute(new AnnotationInfo(ANNOTATION_ASPECT, aspectProxy));
            log("aspect [" + clazz.getQualifiedName() + ']');
            log("    deployment model [" + aspectProxy.deploymentModel() + ']');
        }
    }

    private static void handleMethodAnnotations(final AttributeEnhancer enhancer, final JMethod method) {
        JAnnotation aroundAnnotation = method.getAnnotation(ANNOTATION_AROUND);
        if (aroundAnnotation != null) {
            AroundAnnotationProxy aroundProxy = (AroundAnnotationProxy)aroundAnnotation.getProxy();
            if (aroundProxy != null) {
                enhancer.insertMethodAttribute(method, new AnnotationInfo(ANNOTATION_AROUND, aroundProxy));
                log("    around advice [" + method.getSimpleName() + "::" + aroundProxy.pointcut() + ']');
            }
        }
        JAnnotation beforeAnnotation = method.getAnnotation(ANNOTATION_BEFORE);
        if (beforeAnnotation != null) {
            BeforeAnnotationProxy beforeProxy = (BeforeAnnotationProxy)beforeAnnotation.getProxy();
            if (beforeProxy != null) {
                enhancer.insertMethodAttribute(method, new AnnotationInfo(ANNOTATION_BEFORE, beforeProxy));
                log("    before [" + method.getSimpleName() + "::" + beforeProxy.pointcut() + ']');
            }
        }
        JAnnotation afterAnnotation = method.getAnnotation(ANNOTATION_AFTER);
        if (afterAnnotation != null) {
            AfterAnnotationProxy afterProxy = (AfterAnnotationProxy)afterAnnotation.getProxy();
            if (afterProxy != null) {
                enhancer.insertMethodAttribute(method, new AnnotationInfo(ANNOTATION_AFTER, afterProxy));
                log("    after advice [" + method.getSimpleName() + "::" + afterProxy.pointcut() + ']');
            }
        }
    }

    private static void handleFieldAnnotations(final AttributeEnhancer enhancer, final JField field) {
        JAnnotation expressionAnnotation = field.getAnnotation(ANNOTATION_EXPRESSION);
        if (expressionAnnotation != null) {
            ExpressionAnnotationProxy expressionProxy = (ExpressionAnnotationProxy)expressionAnnotation.getProxy();
            if (expressionProxy != null) {
                enhancer.insertFieldAttribute(field, new AnnotationInfo(ANNOTATION_EXPRESSION, expressionProxy));
                log("    pointcut [" + field.getSimpleName() + "::" + expressionProxy.expression() + ']');
            }
        }
        JAnnotation implementsAnnotation = field.getAnnotation(ANNOTATION_IMPLEMENTS);
        if (implementsAnnotation != null) {
            ImplementsAnnotationProxy implementsProxy = (ImplementsAnnotationProxy)implementsAnnotation.getProxy();
            if (implementsProxy != null) {
                enhancer.insertFieldAttribute(field, new AnnotationInfo(ANNOTATION_IMPLEMENTS, implementsProxy));
                log("    interface introduction [" + field.getSimpleName() + "::" + implementsProxy.expression() + ']');
            }
        }
    }

    private static void handleInnerClassAnnotations(final AttributeEnhancer enhancer, final JClass innerClass) {
        JAnnotation introduceAnnotation = innerClass.getAnnotation(ANNOTATION_INTRODUCE);
        if (introduceAnnotation != null) {
            IntroduceAnnotationProxy introduceProxy = (IntroduceAnnotationProxy)introduceAnnotation.getProxy();
            if (introduceProxy != null) {
                //directly implemented interfaces
                JClass[] introducedInterfaceClasses = innerClass.getInterfaces();
                String[] introducedInterfaceNames = new String[introducedInterfaceClasses.length];
                for (int j = 0; j < introducedInterfaceClasses.length; j++) {
                    introducedInterfaceNames[j] = introducedInterfaceClasses[j].getQualifiedName();
                    log("    interface introduction [" + introducedInterfaceNames[j] + ']');
                }
                if (introducedInterfaceNames.length == 0) {
                    String innerClassName = AnnotationC.convertToJavaStyleInnerClassName(
                            innerClass.getQualifiedName()
                    );
                    introducedInterfaceNames = enhancer.getNearestInterfacesInHierarchy(innerClassName);
                    if (introducedInterfaceNames.length == 0) {
                        throw new RuntimeException("no implicit interfaces found for " + innerClassName);
                    }
                    for (int j = 0; j < introducedInterfaceNames.length; j++) {
                        log("    interface introduction [" + introducedInterfaceNames[j] + ']');
                    }
                }
                introduceProxy.setIntroducedInterfaces(introducedInterfaceNames);
                log(
                        "    mixin introduction [" + innerClass.getQualifiedName() + "::" +
                        introduceProxy.expression() + "] "
                );
                log("    deployment model [" + introduceProxy.deploymentModel() + ']');
                enhancer.insertClassAttribute(new AnnotationInfo(ANNOTATION_INTRODUCE, introduceProxy));
            }
        }
    }

    private static void registerSystemAnnotations(final JamServiceParams params) {
        params.registerAnnotationProxy(AspectAnnotationProxy.class, ANNOTATION_ASPECT);
        params.registerAnnotationProxy(AroundAnnotationProxy.class, ANNOTATION_AROUND);
        params.registerAnnotationProxy(BeforeAnnotationProxy.class, ANNOTATION_BEFORE);
        params.registerAnnotationProxy(AfterAnnotationProxy.class, ANNOTATION_AFTER);
        params.registerAnnotationProxy(ExpressionAnnotationProxy.class, ANNOTATION_EXPRESSION);
        params.registerAnnotationProxy(ImplementsAnnotationProxy.class, ANNOTATION_IMPLEMENTS);
        params.registerAnnotationProxy(IntroduceAnnotationProxy.class, ANNOTATION_INTRODUCE);
    }

    private static void registerUserDefinedAnnotations(final JamServiceParams params) {
        for (Iterator it = s_userAnnotations.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            String name = (String)entry.getKey();
            String className = (String)entry.getValue();
            Class klass;
            try {
                klass = s_loader.loadClass(className);
            } catch (ClassNotFoundException e) {
                throw new DefinitionException(
                        "AnnotationC::ERROR" + className +
                        " could not be found on system classpath or class path provided as argument to the compiler"
                );
            }
            params.registerAnnotationProxy(klass, name);
        }
    }

    /**
     * Logs a message.
     *
     * @param message the message to log
     */
    private static void log(final String message) {
        if (s_verbose) {
            System.out.println("AnnotationC::INFO - " + message);
        }
    }
}
