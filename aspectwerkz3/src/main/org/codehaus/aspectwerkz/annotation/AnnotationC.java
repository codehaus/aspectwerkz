/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;


import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.codehaus.aspectwerkz.annotation.instrumentation.AttributeEnhancer;
import org.codehaus.aspectwerkz.annotation.instrumentation.asm.AsmAttributeEnhancer;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.StaticJoinPoint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * <p/>Annotation compiler. <p/>Extracts the annotations from JavaDoc tags and inserts them into the bytecode of the
 * class.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
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
    private static final String COMMAND_LINE_OPTION_SRCFILES = "-srcfiles";
    private static final String COMMAND_LINE_OPTION_SRCINCLUDES = "-srcincludes";
    private static final String COMMAND_LINE_OPTION_CLASSES = "-classes";
    private static final String COMMAND_LINE_OPTION_DEST = "-dest";

    static final String[] SYSTEM_ANNOTATIONS = new String[]{
        ANNOTATION_ASPECT, ANNOTATION_AROUND, ANNOTATION_BEFORE, ANNOTATION_AFTER,
        ANNOTATION_EXPRESSION, ANNOTATION_IMPLEMENTS, ANNOTATION_INTRODUCE
    };

    /**
     * The annotations properties file define by the user.
     */
    public static final Properties ANNOTATION_DEFINITION = new Properties();

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
    private static final String FILE_SEPARATOR = ",";//FIXME why not using System default ? Is it for Ant task ??

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
                (String) commandLineOptions.get(COMMAND_LINE_OPTION_SRC),
                (String) commandLineOptions.get(COMMAND_LINE_OPTION_SRCFILES),
                (String) commandLineOptions.get(COMMAND_LINE_OPTION_SRCINCLUDES),
                (String) commandLineOptions.get(COMMAND_LINE_OPTION_CLASSES),
                (String) commandLineOptions.get(COMMAND_LINE_OPTION_DEST),
                (String) commandLineOptions.get(COMMAND_LINE_OPTION_CUSTOM)
        );
    }

    /**
     * Compiles the annotations.
     *
     * @param srcDirList
     * @param srcFileList
     * @param classPath
     * @param destDir
     * @param annotationPropetiesFile
     */
    private static void compile(final String srcDirList,
                                final String srcFileList,
                                final String srcFileIncludes,
                                final String classPath,
                                String destDir,
                                final String annotationPropetiesFile) {
        if (srcDirList == null && srcFileList == null && srcFileIncludes == null) {
            throw new IllegalArgumentException("one of src or srcfiles or srcincludes must be not null");
        }
        if ((srcDirList != null && srcFileList != null) || (srcDirList != null && srcFileIncludes != null)
            || (srcFileList != null && srcFileIncludes != null)) { // FIXME: refactor
            throw new IllegalArgumentException("maximum one of src, srcfiles or srcincludes must be not null");
        }
        if (classPath == null) {
            throw new IllegalArgumentException("class path can not be null");
        }
        if (destDir == null) {
            destDir = classPath;
        }

        String[] srcDirs = new String[0];
        String[] srcFiles = new String[0];
        if (srcDirList != null) {
            srcDirs = split(srcDirList, File.pathSeparator);
        } else if (srcFileList != null) {
            srcFiles = split(srcFileList, FILE_SEPARATOR);
        } else {
            srcFiles = loadSourceList(srcFileIncludes);
        }

        compile(s_verbose, srcDirs, srcFiles, split(classPath, File.pathSeparator), destDir, annotationPropetiesFile);
    }

    /**
     * Compiles the annotations.
     *
     * @param verbose
     * @param srcDirs
     * @param srcFiles
     * @param classpath
     * @param destDir
     * @param annotationPropertiesFile
     */
    public static void compile(final boolean verbose,
                               final String[] srcDirs,
                               final String[] srcFiles,
                               final String[] classpath,
                               final String destDir,
                               final String annotationPropertiesFile) {

        s_verbose = verbose;
        URL[] classPath = new URL[classpath.length];
        try {
            for (int i = 0; i < classpath.length; i++) {
                classPath[i] = new File(classpath[i]).toURL();
            }
            s_loader = new URLClassLoader(classPath, AnnotationC.class.getClassLoader());
        } catch (MalformedURLException e) {
            String message = "URL [" + classPath + "] is not valid: " + e.toString();
            logError(message);
            throw new DefinitionException(message, e);
        }

        String destDirToUse = destDir;
        if (destDir == null) {
            if (classpath.length != 1) {
                throw new DefinitionException("destDir must be specified since classpath is composite");
            }
            destDirToUse = classpath[0];
        }

        final AnnotationManager manager = new AnnotationManager();

        logInfo("parsing source dirs:");
        for (int i = 0; i < srcDirs.length; i++) {
            logInfo("    " + srcDirs[i]);
        }
        manager.addSourceTrees(srcDirs);

        for (int i = 0; i < srcFiles.length; i++) {
            logInfo("    " + srcFiles[i]);
            manager.addSource(srcFiles[i]);
        }

        doCompile(annotationPropertiesFile, classPath, manager, destDirToUse);
    }

    /**
     * Compiles the annotations.
     *
     * @param annotationPropetiesFile
     * @param classPath
     * @param manager
     * @param destDir
     */
    private static void doCompile(final String annotationPropetiesFile,
                                  final URL[] classPath,
                                  final AnnotationManager manager,
                                  final String destDir) {

        logInfo("compiling annotations...");
        logInfo("note: if no output is seen, then nothing is compiled");

        // register annotations
        registerSystemAnnotations(manager);
        registerUserDefinedAnnotations(manager, annotationPropetiesFile);

        // get all the classes
        JavaClass[] classes = manager.getAllClasses();
        for (int i = 0; i < classes.length; i++) {
            JavaClass clazz = classes[i];
            logInfo("class [" + clazz.getFullyQualifiedName() + ']');
            try {
                AttributeEnhancer enhancer = new AsmAttributeEnhancer();
                if (enhancer.initialize(clazz.getFullyQualifiedName(), classPath)) {
                    handleClassAnnotations(manager, enhancer, clazz);
                    handleInnerClassAnnotations(manager, enhancer, clazz, classPath, destDir);
                    JavaMethod[] methods = clazz.getMethods();
                    for (int j = 0; j < methods.length; j++) {
                        JavaMethod method = methods[j];
                        if (method.isConstructor()) {
                            handleConstructorAnnotations(manager, enhancer, method);
                        } else {
                            handleMethodAnnotations(manager, enhancer, method);
                        }
                    }
                    JavaField[] fields = clazz.getFields();
                    for (int j = 0; j < fields.length; j++) {
                        handleFieldAnnotations(manager, enhancer, fields[j]);
                    }

                    // write enhanced class to disk
                    enhancer.write(destDir);
                }
            } catch (Throwable e) {
                e.printStackTrace();
                logWarning(
                        "could not compile annotations for class ["
                        + clazz.getFullyQualifiedName() + "] due to: " + e.toString()
                );
            }
        }
        logInfo("compiled classes written to " + destDir);
        logInfo("compilation successful");
    }

    /**
     * Handles the class annotations.
     *
     * @param manager
     * @param enhancer
     * @param clazz
     */
    private static void handleClassAnnotations(final AnnotationManager manager,
                                               final AttributeEnhancer enhancer,
                                               final JavaClass clazz) {

        Annotation[] annotations = manager.getAnnotations(ANNOTATION_ASPECT, clazz);
        for (int i = 0; i < annotations.length; i++) {
            Annotation annotation = annotations[i];
            if (annotation != null) {
                AspectAnnotationProxy aspectProxy = (AspectAnnotationProxy) annotation;
                if (aspectProxy.aspectName() == null) {
                    aspectProxy.setAspectName(clazz.getFullyQualifiedName());
                }
                enhancer.insertClassAttribute(new AnnotationInfo(ANNOTATION_ASPECT, aspectProxy));
                logInfo("aspect [" + clazz.getFullyQualifiedName() + ']');
                logInfo("    deployment model [" + aspectProxy.deploymentModel() + ']');
            }
        }
        for (Iterator it = s_customAnnotations.keySet().iterator(); it.hasNext();) {
            String annotationName = (String) it.next();
            Annotation[] customAnnotations = manager.getAnnotations(annotationName, clazz);
            for (int i = 0; i < customAnnotations.length; i++) {
                Annotation customAnnotation = customAnnotations[i];
                if (customAnnotation != null) {
                    enhancer.insertClassAttribute(
                            new AnnotationInfo(
                                    annotationName,
                                    customAnnotation
                            )
                    );
                    logInfo(
                            "    custom class annotation [" + annotationName + " @ "
                            + clazz.getFullyQualifiedName() + ']'
                    );
                }
            }
        }
    }

    /**
     * Handles the method annotations.
     *
     * @param manager
     * @param enhancer
     * @param method
     */
    private static void handleMethodAnnotations(final AnnotationManager manager,
                                                final AttributeEnhancer enhancer,
                                                final JavaMethod method) {
        // Pointcut with signature
        Annotation[] expressionAnnotations = manager.getAnnotations(ANNOTATION_EXPRESSION, method);
        for (int i = 0; i < expressionAnnotations.length; i++) {
            Annotation expressionAnnotation = expressionAnnotations[i];
            if (expressionAnnotation != null) {
                ExpressionAnnotationProxy expressionProxy = (ExpressionAnnotationProxy) expressionAnnotation;
                AnnotationC.registerCallParameters(expressionProxy, method);
                enhancer.insertMethodAttribute(
                        method, new AnnotationInfo(
                                ANNOTATION_EXPRESSION,
                                expressionProxy
                        )
                );
                logInfo(
                        "    pointcut [" + AnnotationC.getShortCallSignature(method) + " :: "
                        + expressionProxy.expression() + ']'
                );
            }
        }
        Annotation[] aroundAnnotations = manager.getAnnotations(ANNOTATION_AROUND, method);
        for (int i = 0; i < aroundAnnotations.length; i++) {
            Annotation aroundAnnotation = aroundAnnotations[i];
            if (aroundAnnotation != null) {
                AroundAnnotationProxy aroundProxy = (AroundAnnotationProxy) aroundAnnotation;
                AnnotationC.registerCallParameters(aroundProxy, method);
                enhancer.insertMethodAttribute(
                        method, new AnnotationInfo(
                                ANNOTATION_AROUND,
                                aroundProxy
                        )
                );
                logInfo(
                        "    around advice [" + AnnotationC.getShortCallSignature(method) + " :: "
                        + aroundProxy.pointcut() + ']'
                );
            }
        }
        Annotation[] beforeAnnotations = manager.getAnnotations(ANNOTATION_BEFORE, method);
        for (int i = 0; i < beforeAnnotations.length; i++) {
            Annotation beforeAnnotation = beforeAnnotations[i];
            if (beforeAnnotation != null) {
                BeforeAnnotationProxy beforeProxy = (BeforeAnnotationProxy) beforeAnnotation;
                AnnotationC.registerCallParameters(beforeProxy, method);
                enhancer.insertMethodAttribute(
                        method, new AnnotationInfo(
                                ANNOTATION_BEFORE,
                                beforeProxy
                        )
                );
                logInfo(
                        "    before [" + AnnotationC.getShortCallSignature(method) + " :: "
                        + beforeProxy.pointcut() + ']'
                );
            }
        }
        Annotation[] afterAnnotations = manager.getAnnotations(ANNOTATION_AFTER, method);
        for (int i = 0; i < afterAnnotations.length; i++) {
            Annotation afterAnnotation = afterAnnotations[i];
            if (afterAnnotation != null) {
                AfterAnnotationProxy afterProxy = (AfterAnnotationProxy) afterAnnotation;
                AnnotationC.registerCallParameters(afterProxy, method);
                enhancer.insertMethodAttribute(
                        method, new AnnotationInfo(
                                ANNOTATION_AFTER,
                                afterProxy
                        )
                );
                logInfo(
                        "    after advice [" + AnnotationC.getShortCallSignature(method) + " :: "
                        + afterProxy.pointcut() + ']'
                );
            }
        }
        for (Iterator it = s_customAnnotations.keySet().iterator(); it.hasNext();) {
            String annotationName = (String) it.next();
            Annotation[] customAnnotations = manager.getAnnotations(annotationName, method);
            for (int i = 0; i < customAnnotations.length; i++) {
                Annotation customAnnotation = customAnnotations[i];
                if (customAnnotation != null) {
                    enhancer.insertMethodAttribute(
                            method, new AnnotationInfo(
                                    annotationName,
                                    customAnnotation
                            )
                    );
                    logInfo(
                            "    custom method annotation [" + annotationName + " @ "
                            + method.getParentClass().getName() + '.' + method.getName() +
                            ']'
                    );
                }
            }
        }
    }

    /**
     * Handles the constructor annotations.
     *
     * @param manager
     * @param enhancer
     * @param constructor
     */
    private static void handleConstructorAnnotations(final AnnotationManager manager,
                                                     final AttributeEnhancer enhancer,
                                                     final JavaMethod constructor) {

        Annotation[] aroundAnnotations = manager.getAnnotations(ANNOTATION_AROUND, constructor);
        for (int i = 0; i < aroundAnnotations.length; i++) {
            Annotation aroundAnnotation = aroundAnnotations[i];
            if (aroundAnnotation != null) {
                AroundAnnotationProxy aroundProxy = (AroundAnnotationProxy) aroundAnnotation;
                enhancer.insertConstructorAttribute(constructor, new AnnotationInfo(ANNOTATION_AROUND, aroundProxy));
                logInfo(
                        "    around advice [" + constructor.getName() + " :: "
                        + aroundProxy.pointcut() + ']'
                );
            }
        }
        Annotation[] beforeAnnotations = manager.getAnnotations(ANNOTATION_BEFORE, constructor);
        for (int i = 0; i < beforeAnnotations.length; i++) {
            Annotation beforeAnnotation = beforeAnnotations[i];
            if (beforeAnnotation != null) {
                BeforeAnnotationProxy beforeProxy = (BeforeAnnotationProxy) beforeAnnotation;
                enhancer.insertConstructorAttribute(constructor, new AnnotationInfo(ANNOTATION_BEFORE, beforeProxy));
                logInfo(
                        "    before [" + constructor.getName() + " :: " + beforeProxy.pointcut()
                        + ']'
                );
            }
        }
        Annotation[] afterAnnotations = manager.getAnnotations(ANNOTATION_AFTER, constructor);
        for (int i = 0; i < afterAnnotations.length; i++) {
            Annotation afterAnnotation = afterAnnotations[i];
            if (afterAnnotation != null) {
                AfterAnnotationProxy afterProxy = (AfterAnnotationProxy) afterAnnotation;
                enhancer.insertConstructorAttribute(constructor, new AnnotationInfo(ANNOTATION_AFTER, afterProxy));
                logInfo(
                        "    after advice [" + constructor.getName() + " :: "
                        + afterProxy.pointcut() + ']'
                );
            }
        }
        for (Iterator it = s_customAnnotations.keySet().iterator(); it.hasNext();) {
            String annotationName = (String) it.next();
            Annotation[] customAnnotations = manager.getAnnotations(annotationName, constructor);
            for (int i = 0; i < customAnnotations.length; i++) {
                Annotation customAnnotation = customAnnotations[i];
                if (customAnnotation != null) {
                    enhancer.insertConstructorAttribute(
                            constructor, new AnnotationInfo(annotationName, customAnnotation)
                    );
                    logInfo(
                            "    custom constructor annotation [" + annotationName + " @ "
                            + constructor.getParentClass().getName() + '.' +
                            constructor.getName()
                            + ']'
                    );
                }
            }
        }
    }

    /**
     * Handles the field annotations.
     *
     * @param manager
     * @param enhancer
     * @param field
     */
    private static void handleFieldAnnotations(final AnnotationManager manager,
                                               final AttributeEnhancer enhancer,
                                               final JavaField field) {

        Annotation[] expressionAnnotations = manager.getAnnotations(ANNOTATION_EXPRESSION, field);
        for (int i = 0; i < expressionAnnotations.length; i++) {
            Annotation expressionAnnotation = expressionAnnotations[i];
            if (expressionAnnotation != null) {
                ExpressionAnnotationProxy expressionProxy = (ExpressionAnnotationProxy) expressionAnnotation;
                enhancer.insertFieldAttribute(
                        field, new AnnotationInfo(
                                ANNOTATION_EXPRESSION,
                                expressionProxy
                        )
                );
                logInfo(
                        "    pointcut [" + field.getName() + " :: " + expressionProxy.expression()
                        + ']'
                );
            }
        }
        Annotation[] implementsAnnotations = manager.getAnnotations(ANNOTATION_IMPLEMENTS, field);
        for (int i = 0; i < implementsAnnotations.length; i++) {
            Annotation implementsAnnotation = implementsAnnotations[i];
            if (implementsAnnotation != null) {
                ImplementsAnnotationProxy implementsProxy = (ImplementsAnnotationProxy) implementsAnnotation;
                enhancer.insertFieldAttribute(
                        field, new AnnotationInfo(
                                ANNOTATION_IMPLEMENTS,
                                implementsProxy
                        )
                );
                logInfo(
                        "    interface introduction [" + field.getName() + " :: "
                        + implementsProxy.expression() + ']'
                );
            }
        }
        for (Iterator it = s_customAnnotations.keySet().iterator(); it.hasNext();) {
            String annotationName = (String) it.next();
            Annotation[] customAnnotations = manager.getAnnotations(annotationName, field);
            for (int i = 0; i < customAnnotations.length; i++) {
                Annotation customAnnotation = customAnnotations[i];
                if (customAnnotation != null) {
                    enhancer.insertFieldAttribute(
                            field, new AnnotationInfo(
                                    annotationName,
                                    customAnnotation
                            )
                    );
                    logInfo(
                            "    custom field annotation [" + annotationName + " @ "
                            + field.getName() + ']'
                    );
                }
            }
        }
    }

    /**
     * Handles the inner class annotations.
     *
     * @param manager
     * @param enhancer
     * @param clazz
     * @param classPath
     * @param destDir
     */
    private static void handleInnerClassAnnotations(final AnnotationManager manager,
                                                    final AttributeEnhancer enhancer,
                                                    final JavaClass clazz,
                                                    final URL[] classPath,
                                                    final String destDir) {

        JavaClass[] innerClasses = clazz.getInnerClasses();
        for (int i = 0; i < innerClasses.length; i++) {
            JavaClass innerClass = innerClasses[i];
            String innerClassName = innerClass.getFullyQualifiedName();
            Annotation[] introduceAnnotations = manager.getAnnotations(
                    ANNOTATION_INTRODUCE,
                    innerClass
            );
            for (int k = 0; k < introduceAnnotations.length; k++) {
                Annotation introduceAnnotation = introduceAnnotations[k];
                if (introduceAnnotation != null) {
                    IntroduceAnnotationProxy introduceProxy = (IntroduceAnnotationProxy) introduceAnnotation;
                    if (introduceProxy != null) {
                        //directly implemented interfaces
                        JavaClass[] introducedInterfaceClasses = innerClass
                                .getImplementedInterfaces();
                        String[] introducedInterfaceNames = new String[introducedInterfaceClasses.length];
                        for (int j = 0; j < introducedInterfaceClasses.length; j++) {
                            introducedInterfaceNames[j] = introducedInterfaceClasses[j]
                                    .getFullyQualifiedName();
                            logInfo(
                                    "    interface introduction [" + introducedInterfaceNames[j]
                                    + ']'
                            );
                        }
                        if (introducedInterfaceNames.length == 0) {
                            introducedInterfaceNames = enhancer
                                    .getNearestInterfacesInHierarchy(innerClassName);
                            if (introducedInterfaceNames.length == 0) {
                                throw new RuntimeException(
                                        "no implicit interfaces found for "
                                        + innerClassName
                                );
                            }
                            for (int j = 0; j < introducedInterfaceNames.length; j++) {
                                logInfo(
                                        "    interface introduction ["
                                        + introducedInterfaceNames[j] + ']'
                                );
                            }
                        }
                        introduceProxy.setIntroducedInterfaces(introducedInterfaceNames);
                        introduceProxy.setInnerClassName(innerClassName);
                        logInfo(
                                "    mixin introduction [" + innerClass.getFullyQualifiedName()
                                + " :: " + introduceProxy.expression() +
                                "] deployment model ["
                                + introduceProxy.deploymentModel() + ']'
                        );
                        enhancer.insertClassAttribute(
                                new AnnotationInfo(
                                        ANNOTATION_INTRODUCE,
                                        introduceProxy
                                )
                        );
                    }
                }
            }
            //            try {
            //                // TODO: we do not support parsing inner classes of inner classes
            //                AttributeEnhancer innerClassEnhancer = new AsmAttributeEnhancer();
            //                if (innerClassEnhancer.initialize(innerClass.getFullyQualifiedName(), classPath)) {
            //                    handleClassAnnotations(manager, innerClassEnhancer, innerClass);
            //                    JavaMethod[] methods = innerClass.getMethods();
            //                    for (int k = 0; k < methods.length; k++) {
            //                        handleMethodAnnotations(manager, innerClassEnhancer, methods[k]);
            //                    }
            //                    JavaField[] fields = innerClass.getFields();
            //                    for (int k = 0; k < fields.length; k++) {
            //                        handleFieldAnnotations(manager, innerClassEnhancer, fields[k]);
            //                    }
            //
            //                    // write enhanced class to disk
            //                    innerClassEnhancer.write(destDir);
            //                }
            //            } catch (Throwable e) {
            //                logWarning("could not compile annotations for class ["
            //                    + innerClassName
            //                    + "] due to: "
            //                    + e.toString());
            //            }
        }
    }

    /**
     * Registers the system annotations.
     *
     * @param manager the annotations manager
     */
    private static void registerSystemAnnotations(final AnnotationManager manager) {
        manager.registerAnnotationProxy(AspectAnnotationProxy.class, ANNOTATION_ASPECT);
        manager.registerAnnotationProxy(AroundAnnotationProxy.class, ANNOTATION_AROUND);
        manager.registerAnnotationProxy(BeforeAnnotationProxy.class, ANNOTATION_BEFORE);
        manager.registerAnnotationProxy(AfterAnnotationProxy.class, ANNOTATION_AFTER);
        manager.registerAnnotationProxy(ExpressionAnnotationProxy.class, ANNOTATION_EXPRESSION);
        manager.registerAnnotationProxy(ImplementsAnnotationProxy.class, ANNOTATION_IMPLEMENTS);
        manager.registerAnnotationProxy(IntroduceAnnotationProxy.class, ANNOTATION_INTRODUCE);
    }

    /**
     * Registers the user defined annotations.
     *
     * @param manager        the annotations manager
     * @param propertiesFile
     */
    private static void registerUserDefinedAnnotations(final AnnotationManager manager,
                                                       final String propertiesFile) {
        if (propertiesFile == null) {
            return;
        }
        try {
            ANNOTATION_DEFINITION.load(new FileInputStream(propertiesFile));
        } catch (Exception e) {
            String message = "custom annotation properties can not be loaded: " + e.toString();
            logWarning(message);
            throw new DefinitionException(message);
        }
        for (Iterator it = ANNOTATION_DEFINITION.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            String name = ((String) entry.getKey()).trim();
            String className = ((String) entry.getValue()).trim();
            Class klass;
            if (className.equals("")) {
                // use default untyped annotation proxy
                klass = UntypedAnnotationProxy.class;
                className = klass.getName();
            } else {
                try {
                    klass = s_loader.loadClass(className);
                } catch (ClassNotFoundException e) {
                    String message = className
                                     +
                                     " could not be found on system classpath or class path provided as argument to the compiler";
                    logError(message);
                    throw new DefinitionException(message);
                }
            }
            logInfo("register custom annotation [" + name + " :: " + className + ']');
            manager.registerAnnotationProxy(klass, name);
            s_customAnnotations.put(name, className);
        }
    }

    /**
     * Prints the usage.
     */
    private static void printUsage() {
        System.out.println("AspectWerkz (c) 2002-2004 Jonas Bonér, Alexandre Vasseur");
        System.out
                .println(
                        "usage: java [options...] org.codehaus.aspectwerkz.annotation.AnnotationC [-verbose] -src <path to src dir> | -srcfiles <list of files> | -srcincludes <path to file> -classes <path to classes dir> [-dest <path to destination dir>] [-custom <property file for custom annotations>]"
                );
        System.out.println(
                "       -src <path to src dir> provides the list of source directories separated by File.pathSeparator"
        );
        System.out.println("       -srcpath <list of files> provides a comma separated list of source files");
        System.out.println(
                "       -srcincludes <path to file> provides the path to a file containing the list of source files (one name per line)"
        );
        System.out
                .println(
                        "       -dest <path to destination dir> is optional, if omitted the compiled classes will be written to the initial directory"
                );
        System.out
                .println(
                        "       -custom <property file for cutom annotations> is optional, only needed if you have custom annotations you want to compile"
                );
        System.out.println("       -verbose activates compilation status information");
        System.out.println("");
        System.out.println("Note: only one of -src -srcpath and -srcincludes may be used");

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

    private static String getShortCallSignature(final JavaMethod method) {
        StringBuffer buffer = new StringBuffer(method.getName());
        buffer.append("(");
        for (int i = 0; i < method.getParameters().length; i++) {
            JavaParameter javaParameter = method.getParameters()[i];
            if (javaParameter.getType().toString().equals(JoinPoint.class.getName())) {
                buffer.append("JoinPoint");
            } else if (javaParameter.getType().toString().equals(StaticJoinPoint.class.getName())) {
                buffer.append("StaticJoinPoint");
            } else {
                buffer.append(javaParameter.getType().toString());
                buffer.append(" ");
                buffer.append(javaParameter.getName());
            }
            if (i + 1 < method.getParameters().length) {
                buffer.append(", ");
            }
        }
        buffer.append(")");
        return buffer.toString();
    }

    private static void registerCallParameters(final ParameterizedAnnotationProxy proxy, final JavaMethod method) {
        for (int j = 0; j < method.getParameters().length; j++) {
            JavaParameter javaParameter = method.getParameters()[j];
            proxy.addArgument(javaParameter.getName(), javaParameter.getType().toString());
        }
    }

    private static String[] split(String str, String sep) {
        if (str == null || str.length() == 0) {
            return new String[0];
        }

        int start = 0;
        int idx = str.indexOf(sep, start);
        int len = sep.length();
        List strings = new ArrayList();

        while (idx != -1) {
            strings.add(str.substring(start, idx));
            start = idx + len;
            idx = str.indexOf(sep, start);
        }

        strings.add(str.substring(start));

        return (String[]) strings.toArray(new String[strings.size()]);
    }

    /**
     * Load and solve relative to working directory the list of files.
     *
     * @param srcIncludes
     * @return
     */
    private static String[] loadSourceList(final String srcIncludes) {
        File currentDir = new File(".");
        List files = new ArrayList();
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(srcIncludes));

            String line = reader.readLine();
            File tmpFile = null;
            while (line != null) {
                if (line.length() > 0) {
                    tmpFile = new File(currentDir, line);
                    if (!tmpFile.isFile()) {
                        logWarning("file not found: [" + tmpFile + "]");
                    } else {
                        files.add(tmpFile.getAbsolutePath());
                    }
                }
                line = reader.readLine();
            }
        } catch (IOException ioe) {
            throw new BuildException(
                    "an error occured while reading from pattern file: "
                    + srcIncludes, ioe
            );
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException ioe) {
                    //Ignore exception
                }
            }
        }

        return (String[]) files.toArray(new String[files.size()]);
    }
}