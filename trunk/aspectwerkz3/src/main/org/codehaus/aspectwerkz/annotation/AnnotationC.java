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
import org.codehaus.aspectwerkz.annotation.instrumentation.AttributeEnhancer;
import org.codehaus.aspectwerkz.annotation.instrumentation.bcel.BcelAttributeEnhancer;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 *         <p/>
 *         Annotation compiler.
 *         <p/>
 *         Extracts the annotations from JavaDoc tags and inserts them into the bytecode of the class.
 * @TODO: document methods and fields
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
    public static final String[] SYSTEM_ANNOTATIONS = new String[] {
                                                          ANNOTATION_ASPECT, ANNOTATION_AROUND, ANNOTATION_BEFORE,
                                                          ANNOTATION_AFTER, ANNOTATION_EXPRESSION, ANNOTATION_IMPLEMENTS,
                                                          ANNOTATION_INTRODUCE
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
        compile((String)commandLineOptions.get(COMMAND_LINE_OPTION_SRC),
                (String)commandLineOptions.get(COMMAND_LINE_OPTION_CLASSES),
                (String)commandLineOptions.get(COMMAND_LINE_OPTION_DEST),
                (String)commandLineOptions.get(COMMAND_LINE_OPTION_CUSTOM));
    }

    /**
    * Compiles attributes for the aspects.
    *
    * @param sourcePath              the path to the sources to compile attributes for
    * @param classPath               the path to the compiled classes matching the source files
    * @param destDir                 the path where to write the compiled aspects (can be NULL)
    * @param annotationPropetiesFile the annotation properties file (for custom annotations) (can be NULL)
    */
    public static void compile(final String sourcePath, final String classPath, String destDir,
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
            s_loader = new URLClassLoader(new URL[] { new File(classPath).toURL() }, ClassLoader.getSystemClassLoader());
        } catch (MalformedURLException e) {
            String message = "URL [" + classPath + "] is not valid: " + e.toString();
            logError(message);
            throw new DefinitionException(message, e);
        }
        doCompile(annotationPropetiesFile, classPath, sourcePath, destDir);
    }

    /**
    * Compiles the annotations.
    *
    * @param classPath
    * @param destDir
    */
    private static void doCompile(final String annotationPropetiesFile, final String classPath,
                                  final String sourcePath, final String destDir) {
        logInfo("compiling annotations...");
        logInfo("note: if no output is seen, then nothing is compiled");

        // create the annotation manager
        final AnnotationManager manager = new AnnotationManager();
        manager.addSourceTrees(new String[] { sourcePath });

        // register annotations
        registerSystemAnnotations(manager);
        registerUserDefinedAnnotations(manager, annotationPropetiesFile);

        // get all the classes
        JavaClass[] classes = manager.getAllClasses();
        for (int i = 0; i < classes.length; i++) {
            JavaClass clazz = classes[i];
            try {
                AttributeEnhancer enhancer = new BcelAttributeEnhancer();
                if (enhancer.initialize(clazz.getFullyQualifiedName(), classPath)) {
                    handleClassAnnotations(manager, enhancer, clazz);
                    handleInnerClassAnnotations(manager, enhancer, clazz, classPath, destDir);
                    JavaMethod[] methods = clazz.getMethods();
                    for (int j = 0; j < methods.length; j++) {
                        handleMethodAnnotations(manager, enhancer, methods[j]);
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
                logWarning("could not compile annotations for class [" + clazz.getFullyQualifiedName() + "] due to: "
                           + e.toString());
            }
        }
        logInfo("compiled classes written to " + destDir);
        logInfo("compilation successful");
    }

    /**
    * Handles the class annotations.
    *
    * @param enhancer
    * @param clazz
    */
    private static void handleClassAnnotations(final AnnotationManager manager, final AttributeEnhancer enhancer,
                                               final JavaClass clazz) {
        Annotation[] annotations = manager.getAnnotations(ANNOTATION_ASPECT, clazz);
        for (int i = 0; i < annotations.length; i++) {
            Annotation annotation = annotations[i];
            if (annotation != null) {
                AspectAnnotationProxy aspectProxy = (AspectAnnotationProxy)annotation;
                enhancer.insertClassAttribute(new AnnotationInfo(ANNOTATION_ASPECT, aspectProxy));
                logInfo("aspect [" + clazz.getFullyQualifiedName() + ']');
                logInfo("    deployment model [" + aspectProxy.deploymentModel() + ']');
            }
        }
        for (Iterator it = s_customAnnotations.keySet().iterator(); it.hasNext();) {
            String annotationName = (String)it.next();
            Annotation[] customAnnotations = manager.getAnnotations(annotationName, clazz);
            for (int i = 0; i < customAnnotations.length; i++) {
                Annotation customAnnotation = customAnnotations[i];
                if (customAnnotation != null) {
                    enhancer.insertClassAttribute(new AnnotationInfo(annotationName, customAnnotation));
                    logInfo("custom class annotation [" + annotationName + " @ " + clazz.getFullyQualifiedName() + ']');
                }
            }
        }
    }

    /**
    * Handles the method annotations.
    *
    * @param enhancer
    * @param method
    */
    private static void handleMethodAnnotations(final AnnotationManager manager, final AttributeEnhancer enhancer,
                                                final JavaMethod method) {
        Annotation[] aroundAnnotations = manager.getAnnotations(ANNOTATION_AROUND, method);
        for (int i = 0; i < aroundAnnotations.length; i++) {
            Annotation aroundAnnotation = aroundAnnotations[i];
            if (aroundAnnotation != null) {
                AroundAnnotationProxy aroundProxy = (AroundAnnotationProxy)aroundAnnotation;
                enhancer.insertMethodAttribute(method, new AnnotationInfo(ANNOTATION_AROUND, aroundProxy));
                logInfo("    around advice [" + method.getName() + " :: " + aroundProxy.pointcut() + ']');
            }
        }
        Annotation[] beforeAnnotations = manager.getAnnotations(ANNOTATION_BEFORE, method);
        for (int i = 0; i < beforeAnnotations.length; i++) {
            Annotation beforeAnnotation = beforeAnnotations[i];
            if (beforeAnnotation != null) {
                BeforeAnnotationProxy beforeProxy = (BeforeAnnotationProxy)beforeAnnotation;
                enhancer.insertMethodAttribute(method, new AnnotationInfo(ANNOTATION_BEFORE, beforeProxy));
                logInfo("    before [" + method.getName() + " :: " + beforeProxy.pointcut() + ']');
            }
        }
        Annotation[] afterAnnotations = manager.getAnnotations(ANNOTATION_AFTER, method);
        for (int i = 0; i < afterAnnotations.length; i++) {
            Annotation afterAnnotation = afterAnnotations[i];
            if (afterAnnotation != null) {
                AfterAnnotationProxy afterProxy = (AfterAnnotationProxy)afterAnnotation;
                enhancer.insertMethodAttribute(method, new AnnotationInfo(ANNOTATION_AFTER, afterProxy));
                logInfo("    after advice [" + method.getName() + " :: " + afterProxy.pointcut() + ']');
            }
        }
        for (Iterator it = s_customAnnotations.keySet().iterator(); it.hasNext();) {
            String annotationName = (String)it.next();
            Annotation[] customAnnotations = manager.getAnnotations(annotationName, method);
            for (int i = 0; i < customAnnotations.length; i++) {
                Annotation customAnnotation = customAnnotations[i];
                if (customAnnotation != null) {
                    enhancer.insertMethodAttribute(method, new AnnotationInfo(annotationName, customAnnotation));
                    logInfo("custom method annotation [" + annotationName + " @ " + method.getParentClass().getName()
                            + '.' + method.getName() + ']');
                }
            }
        }
    }

    /**
    * Handles the field annotations.
    *
    * @param enhancer
    * @param field
    */
    private static void handleFieldAnnotations(final AnnotationManager manager, final AttributeEnhancer enhancer,
                                               final JavaField field) {
        Annotation[] expressionAnnotations = manager.getAnnotations(ANNOTATION_EXPRESSION, field);
        for (int i = 0; i < expressionAnnotations.length; i++) {
            Annotation expressionAnnotation = expressionAnnotations[i];
            if (expressionAnnotation != null) {
                ExpressionAnnotationProxy expressionProxy = (ExpressionAnnotationProxy)expressionAnnotation;
                enhancer.insertFieldAttribute(field, new AnnotationInfo(ANNOTATION_EXPRESSION, expressionProxy));
                logInfo("    pointcut [" + field.getName() + " :: " + expressionProxy.expression() + ']');
            }
        }
        Annotation[] implementsAnnotations = manager.getAnnotations(ANNOTATION_IMPLEMENTS, field);
        for (int i = 0; i < implementsAnnotations.length; i++) {
            Annotation implementsAnnotation = implementsAnnotations[i];
            if (implementsAnnotation != null) {
                ImplementsAnnotationProxy implementsProxy = (ImplementsAnnotationProxy)implementsAnnotation;
                enhancer.insertFieldAttribute(field, new AnnotationInfo(ANNOTATION_IMPLEMENTS, implementsProxy));
                logInfo("    interface introduction [" + field.getName() + " :: " + implementsProxy.expression() + ']');
            }
        }
        for (Iterator it = s_customAnnotations.keySet().iterator(); it.hasNext();) {
            String annotationName = (String)it.next();
            Annotation[] customAnnotations = manager.getAnnotations(annotationName, field);
            for (int i = 0; i < customAnnotations.length; i++) {
                Annotation customAnnotation = customAnnotations[i];
                if (customAnnotation != null) {
                    enhancer.insertFieldAttribute(field, new AnnotationInfo(annotationName, customAnnotation));
                    logInfo("custom field annotation [" + annotationName + " @ " + field.getName() + ']');
                }
            }
        }
    }

    /**
    * Handles the inner class annotations.
    *
    * @param enhancer
    * @param clazz
    * @param classPath
    * @param destDir
    */
    private static void handleInnerClassAnnotations(final AnnotationManager manager, final AttributeEnhancer enhancer,
                                                    final JavaClass clazz, final String classPath, final String destDir) {
        JavaClass[] innerClasses = clazz.getInnerClasses();
        for (int i = 0; i < innerClasses.length; i++) {
            JavaClass innerClass = innerClasses[i];
            String innerClassName = innerClass.getFullyQualifiedName();
            Annotation[] introduceAnnotations = manager.getAnnotations(ANNOTATION_INTRODUCE, innerClass);
            for (int k = 0; k < introduceAnnotations.length; k++) {
                Annotation introduceAnnotation = introduceAnnotations[k];
                if (introduceAnnotation != null) {
                    IntroduceAnnotationProxy introduceProxy = (IntroduceAnnotationProxy)introduceAnnotation;
                    if (introduceProxy != null) {
                        //directly implemented interfaces
                        JavaClass[] introducedInterfaceClasses = innerClass.getImplementedInterfaces();
                        String[] introducedInterfaceNames = new String[introducedInterfaceClasses.length];
                        for (int j = 0; j < introducedInterfaceClasses.length; j++) {
                            introducedInterfaceNames[j] = introducedInterfaceClasses[j].getFullyQualifiedName();
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
                        logInfo("    mixin introduction [" + innerClass.getFullyQualifiedName() + " :: "
                                + introduceProxy.expression() + "] deployment model ["
                                + introduceProxy.deploymentModel() + ']');
                        enhancer.insertClassAttribute(new AnnotationInfo(ANNOTATION_INTRODUCE, introduceProxy));
                    }
                }
            }
            try {
                // TODO: for safety we don not support parsing inner classes of inner classes (good or bad?)
                AttributeEnhancer innerClassEnhancer = new BcelAttributeEnhancer();
                if (innerClassEnhancer.initialize(innerClass.getFullyQualifiedName(), classPath)) {
                    handleClassAnnotations(manager, innerClassEnhancer, innerClass);
                    JavaMethod[] methods = innerClass.getMethods();
                    for (int k = 0; k < methods.length; k++) {
                        handleMethodAnnotations(manager, innerClassEnhancer, methods[k]);
                    }
                    JavaField[] fields = innerClass.getFields();
                    for (int k = 0; k < fields.length; k++) {
                        handleFieldAnnotations(manager, innerClassEnhancer, fields[k]);
                    }

                    // write enhanced class to disk
                    innerClassEnhancer.write(destDir);
                }
            } catch (Throwable e) {
                logWarning("could not compile annotations for class [" + innerClassName + "] due to: " + e.toString());
            }
        }
    }

    /**
    * @param manager
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
    * @param manager
    */
    private static void registerUserDefinedAnnotations(final AnnotationManager manager, final String propertiesFile) {
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
            Map.Entry entry = (Map.Entry)it.next();
            String name = ((String)entry.getKey()).trim();
            String className = ((String)entry.getValue()).trim();
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
                                     + " could not be found on system classpath or class path provided as argument to the compiler";
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
        System.out.println("usage: java [options...] org.codehaus.aspectwerkz.annotation.AnnotationC [-verbose] -src <path to src dir> -classes <path to classes dir> [-dest <path to destination dir>] [-custom <property file for custom annotations>]");
        System.out.println("       -dest <path to destination dir> is optional, if omitted the compiled classes will be written to the initial directory");
        System.out.println("       -custom <property file for cutom annotations> is optional, only needed if you have custom annotations you want to compile");
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

    /**
    * @param classFileName
    * @return
    * @TODO not used, remove?
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
    * @TODO not used, remove?
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
}
