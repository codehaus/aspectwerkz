/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;

import org.apache.xmlbeans.impl.jam.*;
import org.codehaus.aspectwerkz.annotation.instrumentation.AttributeEnhancer;
import org.codehaus.aspectwerkz.annotation.instrumentation.bcel.BcelAttributeEnhancer;
import org.codehaus.aspectwerkz.exception.DefinitionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

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
    private static final String COMMAND_LINE_OPTION_DASH = "-";
    private static final String COMMAND_LINE_OPTION_VERBOSE = "-verbose";
    private static final String COMMAND_LINE_OPTION_CUSTOM = "-custom";
    private static final String COMMAND_LINE_OPTION_SRC = "-src";
    private static final String COMMAND_LINE_OPTION_CLASSES = "-classes";
    private static final String COMMAND_LINE_OPTION_DEST = "-dest";
    private static final String FILE_PATTERN = "**/*.java";

    /**
     * Verbose logging.
     */
    private static boolean s_verbose = false;

    /**
     * The class loader.
     */
    private static URLClassLoader s_loader;

    /**
     * The custom annotations.
     */
    private static Map s_customAnnotations = new HashMap();

    /**
     * Runs the compiler from the command line.
     *
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 4) {
            printUsage();
        }
        Map commandLineOptions = parseCommandLineOptions(args);
        compile(
                (String)commandLineOptions.get(COMMAND_LINE_OPTION_SRC),
                (String)commandLineOptions.get(COMMAND_LINE_OPTION_CLASSES),
                (String)commandLineOptions.get(COMMAND_LINE_OPTION_DEST),
                (String)commandLineOptions.get(COMMAND_LINE_OPTION_CUSTOM)
        );
    }

    /**
     * Compiles attributes for the aspects.
     *
     * @param sourcePath              the path to the sources to compile attributes for
     * @param classPath               the path to the compiled classes matching the source files
     * @param destDir                 the path where to write the compiled aspects (can be NULL)
     * @param annotationPropetiesFile the annotation properties file (for custom annotations) (can be NULL)
     */
    public static void compile(
            final String sourcePath, final String classPath, String destDir,
            final String annotationPropetiesFile) {
        if (sourcePath == null) {
            throw new IllegalArgumentException("source path can not be null");
        }
        if (classPath == null) {
            throw new IllegalArgumentException("class path can not be null");
        }
        if (destDir == null) {
            destDir = classPath;
        }
        try {
            s_loader =
            new URLClassLoader(new URL[]{new File(classPath).toURL()}, ClassLoader.getSystemClassLoader());
        } catch (MalformedURLException e) {
            String message = "URL [" + classPath + "] is not valid: " + e.toString();
            logError(message);
            throw new DefinitionException(message, e);
        }
        JamService service = initializeJamService(annotationPropetiesFile, sourcePath);
        doCompile(service, classPath, destDir);
    }

    /**
     * @param classFileName
     * @return
     */
    public static String convertToJavaStyleInnerClassFileName(final String classFileName) {
        String newClassFileName;
        int index = classFileName.lastIndexOf('/');
        if (index == -1) {
            return classFileName;
        } else {
            newClassFileName = classFileName.substring(0, index) + '$'
                               + classFileName.substring(index + 1, classFileName.length());
            return newClassFileName;
        }
    }

    /**
     * @param classFileName
     * @return
     */
    public static String convertToJavaStyleInnerClassName(final String classFileName) {
        String newClassFileName;
        int index = classFileName.lastIndexOf('.');
        if (index == -1) {
            return classFileName;
        } else {
            newClassFileName = classFileName.substring(0, index) + '$'
                               + classFileName.substring(index + 1, classFileName.length());
            return newClassFileName;
        }
    }

    /**
     * Initializes the JAM service.
     *
     * @param annotationPropetiesFile
     * @param sourcePath
     * @return the JAM service
     */
    private static JamService initializeJamService(final String annotationPropetiesFile, final String sourcePath) {
        JamServiceFactory factory = JamServiceFactory.getInstance();
        JamServiceParams params = factory.createServiceParams();

        // set the custom javadoc parser
        params.setJavadocTagParser(new CustomJavadocTagParser());

        // register the annotations of interest
        registerSystemAnnotations(params);
        registerUserDefinedAnnotations(params, annotationPropetiesFile);

        // register the source files of interest
        params.includeSourcePattern(new File[]{new File(sourcePath)}, FILE_PATTERN);

        // create the JAM service
        JamService service;
        try {
            service = factory.createService(params);
        } catch (IOException e) {
            String message = "could not create the JAM service due to: " + e.toString();
            logError(message);
            throw new DefinitionException(message, e);
        }
        return service;
    }

    /**
     * Compiles the annotations.
     *
     * @param service   the JAM service
     * @param classPath
     * @param destDir
     */
    private static void doCompile(JamService service, final String classPath, String destDir) {
        logInfo("compiling annotations...");
        logInfo("note: if no output is seen, then nothing is compiled");

        // get all the classes
        JClass[] classes = service.getAllClasses();
        for (int i = 0; i < classes.length; i++) {
            JClass clazz = classes[i];
            try {
                AttributeEnhancer enhancer = new BcelAttributeEnhancer();
                if (enhancer.initialize(clazz.getQualifiedName(), classPath)) {
                    handleClassAnnotations(enhancer, clazz);
                    handleInnerClassAnnotations(enhancer, clazz, classPath, destDir);

                    //                    handleInnerClassAnnotations(classPath, destDir, clazz);
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
            } catch (Throwable e) {
                e.printStackTrace();
                logWarning(
                        "could not compile annotations for class [" + clazz.getQualifiedName() + "] due to: "
                        + e.toString()
                );
            }
        }
        logInfo("compiled classes written to " + destDir);
        logInfo("compilation successful");
    }

    /**
     * @param enhancer
     * @param clazz
     */
    private static void handleClassAnnotations(final AttributeEnhancer enhancer, final JClass clazz) {
        JAnnotation ann = clazz.getAnnotation(ANNOTATION_ASPECT);
        if (ann != null) {
            AspectAnnotationProxy aspectProxy = (AspectAnnotationProxy)ann.getProxy();
            enhancer.insertClassAttribute(new AnnotationInfo(ANNOTATION_ASPECT, aspectProxy));
            logInfo("aspect [" + clazz.getQualifiedName() + ']');
            logInfo("    deployment model [" + aspectProxy.deploymentModel() + ']');
        }
        for (Iterator it = s_customAnnotations.keySet().iterator(); it.hasNext();) {
            String annotationName = (String)it.next();
            JAnnotation customAnnotation = clazz.getAnnotation(annotationName);
            if (customAnnotation != null) {
                AnnotationProxyBase annotationProxy = (AnnotationProxyBase)customAnnotation.getProxy();
                if (annotationProxy != null) {
                    enhancer.insertClassAttribute(new AnnotationInfo(annotationName, annotationProxy));
                    logInfo("custom class annotation [" + annotationName + " @ " + clazz.getQualifiedName() + ']');
                }
            }
        }
    }

    /**
     * @param enhancer
     * @param method
     */
    private static void handleMethodAnnotations(final AttributeEnhancer enhancer, final JMethod method) {
        JAnnotation aroundAnnotation = method.getAnnotation(ANNOTATION_AROUND);
        if (aroundAnnotation != null) {
            AroundAnnotationProxy aroundProxy = (AroundAnnotationProxy)aroundAnnotation.getProxy();
            if (aroundProxy != null) {
                enhancer.insertMethodAttribute(method, new AnnotationInfo(ANNOTATION_AROUND, aroundProxy));
                logInfo("    around advice [" + method.getSimpleName() + " :: " + aroundProxy.pointcut() + ']');
            }
        }
        JAnnotation beforeAnnotation = method.getAnnotation(ANNOTATION_BEFORE);
        if (beforeAnnotation != null) {
            BeforeAnnotationProxy beforeProxy = (BeforeAnnotationProxy)beforeAnnotation.getProxy();
            if (beforeProxy != null) {
                enhancer.insertMethodAttribute(method, new AnnotationInfo(ANNOTATION_BEFORE, beforeProxy));
                logInfo("    before [" + method.getSimpleName() + " :: " + beforeProxy.pointcut() + ']');
            }
        }
        JAnnotation afterAnnotation = method.getAnnotation(ANNOTATION_AFTER);
        if (afterAnnotation != null) {
            AfterAnnotationProxy afterProxy = (AfterAnnotationProxy)afterAnnotation.getProxy();
            if (afterProxy != null) {
                enhancer.insertMethodAttribute(method, new AnnotationInfo(ANNOTATION_AFTER, afterProxy));
                logInfo("    after advice [" + method.getSimpleName() + " :: " + afterProxy.pointcut() + ']');
            }
        }
        for (Iterator it = s_customAnnotations.keySet().iterator(); it.hasNext();) {
            String annotationName = (String)it.next();
            JAnnotation customAnnotation = method.getAnnotation(annotationName);
            if (customAnnotation != null) {
                AnnotationProxyBase annotationProxy = (AnnotationProxyBase)customAnnotation.getProxy();
                if (annotationProxy != null) {
                    enhancer.insertMethodAttribute(method, new AnnotationInfo(annotationName, annotationProxy));
                    logInfo("custom method annotation [" + annotationName + " @ " + method.getQualifiedName() + ']');
                }
            }
        }
    }

    /**
     * @param enhancer
     * @param field
     */
    private static void handleFieldAnnotations(final AttributeEnhancer enhancer, final JField field) {
        JAnnotation expressionAnnotation = field.getAnnotation(ANNOTATION_EXPRESSION);
        if (expressionAnnotation != null) {
            ExpressionAnnotationProxy expressionProxy = (ExpressionAnnotationProxy)expressionAnnotation.getProxy();
            if (expressionProxy != null) {
                enhancer.insertFieldAttribute(field, new AnnotationInfo(ANNOTATION_EXPRESSION, expressionProxy));
                logInfo("    pointcut [" + field.getSimpleName() + " :: " + expressionProxy.expression() + ']');
            }
        }
        JAnnotation implementsAnnotation = field.getAnnotation(ANNOTATION_IMPLEMENTS);
        if (implementsAnnotation != null) {
            ImplementsAnnotationProxy implementsProxy = (ImplementsAnnotationProxy)implementsAnnotation.getProxy();
            if (implementsProxy != null) {
                enhancer.insertFieldAttribute(field, new AnnotationInfo(ANNOTATION_IMPLEMENTS, implementsProxy));
                logInfo(
                        "    interface introduction [" + field.getSimpleName() + " :: " + implementsProxy.expression()
                        + ']'
                );
            }
        }
        for (Iterator it = s_customAnnotations.keySet().iterator(); it.hasNext();) {
            String annotationName = (String)it.next();
            JAnnotation customAnnotation = field.getAnnotation(annotationName);
            if (customAnnotation != null) {
                AnnotationProxyBase annotationProxy = (AnnotationProxyBase)customAnnotation.getProxy();
                if (annotationProxy != null) {
                    enhancer.insertFieldAttribute(field, new AnnotationInfo(annotationName, annotationProxy));
                    logInfo("custom field annotation [" + annotationName + " @ " + field.getQualifiedName() + ']');
                }
            }
        }
    }

    /**
     * @param enhancer
     * @param clazz
     * @param classPath
     * @param destDir
     */
    private static void handleInnerClassAnnotations(
            final AttributeEnhancer enhancer, final JClass clazz, final String classPath, final String destDir) {
        JClass[] innerClasses = clazz.getClasses();
        for (int i = 0; i < innerClasses.length; i++) {
            JClass innerClass = innerClasses[i];
            String innerClassName = innerClass.getQualifiedName();
            JAnnotation introduceAnnotation = innerClass.getAnnotation(ANNOTATION_INTRODUCE);
            if (introduceAnnotation != null) {
                IntroduceAnnotationProxy introduceProxy = (IntroduceAnnotationProxy)introduceAnnotation.getProxy();
                if (introduceProxy != null) {
                    //directly implemented interfaces
                    JClass[] introducedInterfaceClasses = innerClass.getInterfaces();
                    String[] introducedInterfaceNames = new String[introducedInterfaceClasses.length];
                    for (int j = 0; j < introducedInterfaceClasses.length; j++) {
                        introducedInterfaceNames[j] = introducedInterfaceClasses[j].getQualifiedName();
                        logInfo("    interface introduction [" + introducedInterfaceNames[j] + ']');
                    }
                    if (introducedInterfaceNames.length == 0) {
                        introducedInterfaceNames = enhancer.getNearestInterfacesInHierarchy(innerClassName);
                        if (introducedInterfaceNames.length == 0) {
                            throw new RuntimeException("no implicit interfaces found for " + innerClassName);
                        }
                        for (int j = 0; j < introducedInterfaceNames.length; j++) {
                            logInfo("    interface introduction [" + introducedInterfaceNames[j] + ']');
                        }
                    }
                    introduceProxy.setIntroducedInterfaces(introducedInterfaceNames);
                    introduceProxy.setInnerClassName(innerClassName);
                    logInfo(
                            "    mixin introduction [" + innerClass.getQualifiedName() + " :: "
                            + introduceProxy.expression() + "] deployment model [" +
                            introduceProxy.deploymentModel()
                            + ']'
                    );
                    enhancer.insertClassAttribute(new AnnotationInfo(ANNOTATION_INTRODUCE, introduceProxy));
                }
            }
            try {
                // TODO: for safety we don not support parsing inner classes of inner classes (good or bad?)
                AttributeEnhancer innerClassEnhancer = new BcelAttributeEnhancer();
                if (innerClassEnhancer.initialize(innerClass.getQualifiedName(), classPath)) {
                    handleClassAnnotations(innerClassEnhancer, innerClass);
                    JMethod[] methods = innerClass.getDeclaredMethods();
                    for (int k = 0; k < methods.length; k++) {
                        handleMethodAnnotations(innerClassEnhancer, methods[k]);
                    }
                    JField[] fields = innerClass.getDeclaredFields();
                    for (int k = 0; k < fields.length; k++) {
                        handleFieldAnnotations(innerClassEnhancer, fields[k]);
                    }

                    // write enhanced class to disk
                    innerClassEnhancer.write(destDir);
                }
            } catch (Throwable e) {
                logWarning(
                        "could not compile annotations for class [" + innerClassName + "] due to: " +
                        e.toString()
                );
            }
        }
    }

    /**
     * @param params
     */
    private static void registerSystemAnnotations(final JamServiceParams params) {
        params.registerAnnotationProxy(AspectAnnotationProxy.class, ANNOTATION_ASPECT);
        params.registerAnnotationProxy(AroundAnnotationProxy.class, ANNOTATION_AROUND);
        params.registerAnnotationProxy(BeforeAnnotationProxy.class, ANNOTATION_BEFORE);
        params.registerAnnotationProxy(AfterAnnotationProxy.class, ANNOTATION_AFTER);
        params.registerAnnotationProxy(ExpressionAnnotationProxy.class, ANNOTATION_EXPRESSION);
        params.registerAnnotationProxy(ImplementsAnnotationProxy.class, ANNOTATION_IMPLEMENTS);
        params.registerAnnotationProxy(IntroduceAnnotationProxy.class, ANNOTATION_INTRODUCE);
    }

    /**
     * @param params
     */
    private static void registerUserDefinedAnnotations(final JamServiceParams params, final String propertiesFile) {
        if (propertiesFile == null) {
            return;
        }
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(propertiesFile));
        } catch (Exception e) {
            String message = "custom annotation properties can not be loaded: " + e.toString();
            logWarning(message);
            throw new DefinitionException(message);
        }
        for (Iterator it = properties.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            String name = ((String)entry.getKey()).trim();
            String className = ((String)entry.getValue()).trim();
            Class klass;
            try {
                klass = s_loader.loadClass(className);
            } catch (ClassNotFoundException e) {
                String message = className
                                 +
                                 " could not be found on system classpath or class path provided as argument to the compiler";
                logError(message);
                throw new DefinitionException(message);
            }
            logInfo("register custom annotation [" + name + " :: " + className + ']');
            params.registerAnnotationProxy(klass, name);
            s_customAnnotations.put(name, className);
        }
    }

    /**
     * Prints the usage.
     */
    private static void printUsage() {
        System.out.println("AspectWerkz (c) 2002-2004 Jonas Bonér, Alexandre Vasseur");
        System.out.println(
                "usage: java [options...] org.codehaus.aspectwerkz.annotation.AnnotationC [-verbose] -src <path to src dir> -classes <path to classes dir> [-dest <path to destination dir>] [-custom <property file for custom annotations>]"
        );
        System.out.println(
                "       -dest <path to destination dir> is optional, if omitted the compiled classes will be written to the initial directory"
        );
        System.out.println(
                "       -custom <property file for cutom annotations> is optional, only needed if you have custom annotations you want to compile"
        );
        System.out.println("       -verbose activates compilation status information");
        System.exit(0);
    }

    /**
     * Parses the command line options.
     *
     * @param args the arguments
     * @return a map with the options
     */
    private static Map parseCommandLineOptions(final String[] args) {
        final Map arguments = new HashMap();
        try {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals(COMMAND_LINE_OPTION_VERBOSE)) {
                    s_verbose = true;
                } else if (args[i].startsWith(COMMAND_LINE_OPTION_DASH)) {
                    String option = args[i++];
                    String value = args[i];
                    arguments.put(option, value);
                }
            }
        } catch (Exception e) {
            logError("options list to compiler is not valid");
            System.exit(1);
        }
        return arguments;
    }

    /**
     * Logs an INFO message.
     *
     * @param message the message
     */
    private static void logInfo(final String message) {
        if (s_verbose) {
            System.out.println("AnnotationC::INFO - " + message);
        }
    }

    /**
     * Logs an ERROR message.
     *
     * @param message the message
     */
    private static void logError(final String message) {
        if (s_verbose) {
            System.err.println("AnnotationC::ERROR - " + message);
        }
    }

    /**
     * Logs an WARNING message.
     *
     * @param message the message
     */
    private static void logWarning(final String message) {
        if (s_verbose) {
            System.err.println("AnnotationC::WARNING - " + message);
        }
    }
}
