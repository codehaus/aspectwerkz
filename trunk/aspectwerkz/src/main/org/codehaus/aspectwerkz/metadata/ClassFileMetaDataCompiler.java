/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.codehaus.aspectwerkz.metadata;

import java.io.File;
import java.io.IOException;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Enumeration;
import java.util.Collection;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;

import java.lang.reflect.Method;
import java.lang.reflect.Field;

import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.MetaDataCompiler;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.FieldMetaData;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.definition.IntroductionDefinition;

/**
 * Parses a given jar file or class dir and compiles meta-data for all the
 * introduced <code>Introduction</code>s.
 * <p/>
 * Can be called from the command line.
 *
 * @todo only compile if we have a change in the class or jar file
 * @todo problem with inner classes
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: ClassFileMetaDataCompiler.java,v 1.1 2003-06-17 14:58:31 jboner Exp $
 */
public class ClassFileMetaDataCompiler extends MetaDataCompiler {

    /**
     * Parses a given jar/zip file or class dir, compiles and stores meta-data for
     * all methods for all the introduced <code>Introduction</code>s as XML files
     * in a directory specified.
     *
     * @param definitionFile the definition file to use
     * @param classPath the path to the classes, directory or jar/zip file
     * @param metaDataDir the path to the dir where to store the meta-data
     */
    public static void compile(final String definitionFile,
                               final String classPath,
                               final String metaDataDir) {
        compile(definitionFile, classPath, metaDataDir, null);
    }

    /**
     * Parses a given jar/zip file or class dir, compiles and stores meta-data for
     * all methods for all the introduced <code>Introduction</code>s as XML files
     * in a directory specified.
     *
     * @param definitionFile the definition file to use
     * @param classPath the path to the classes, directory or jar/zip file
     * @param metaDataDir the path to the dir where to store the meta-data
     * @param uuid the user-defined UUID for the createWeaveModel model
     */
    public static void compile(final String definitionFile,
                               final String classPath,
                               final String metaDataDir,
                               final String uuid) {
        if (definitionFile == null) throw new IllegalArgumentException("definitionFile can not be null");
        if (classPath == null) throw new IllegalArgumentException("classPath can not be null");
        if (metaDataDir == null) throw new IllegalArgumentException("metaDataDir can not be null");

        createMetaDataDir(metaDataDir);
        final AspectWerkzDefinition definition =
                AspectWerkzDefinition.getDefinition(definitionFile);

        final WeaveModel weaveModel = createWeaveModel(uuid, definition);
        compileIntroductionMetaData(weaveModel, classPath);
        saveWeaveModelToFile(metaDataDir, weaveModel);
    }

    /**
     * Compiles the class meta-data for all introduced implementations.
     *
     * @param model the createWeaveModel model
     * @param classPath the pathe to the classes
     * @param metaDataDir the meta-data dir
     */
    private static void compileIntroductionMetaData(final WeaveModel model,
                                                    final String classPath) {
        ClassLoader loader = getClassLoader(classPath);
        Collection introductions =
                model.getDefinition().getIntroductionDefinitions();

        Set classSet = getClassSet(classPath);
        for (Iterator it1 = classSet.iterator(); it1.hasNext();) {

            final String className = (String)it1.next();
            for (Iterator it2 = introductions.iterator(); it2.hasNext();) {

                final String introduction =
                        ((IntroductionDefinition)it2.next()).
                        getImplementation();

                if (introduction == null) continue; // interface introduction

                if (introduction.equals(className)) {
                    model.addIntroductionMetaData(
                            parseClass(loader, className, classPath));
                }
            }
        }
    }

    /**
     * Compiles the class list.
     *
     * @param classPath the dir or jar file with the classes.
     * @return the class list
     */
    private static Set getClassSet(final String classPath) {
        File dir = new File(classPath);
        final Set classSet = new HashSet();
        if (classPath.endsWith(".jar") || classPath.endsWith(".zip")) {
            try {
                ZipFile zf = new ZipFile(classPath);
                for (Enumeration entries = zf.entries(); entries.hasMoreElements();) {
                    String zipEntryName = ((ZipEntry)entries.nextElement()).getName();
                    if (zipEntryName.endsWith(".class")) {
                        int index1 = zipEntryName.lastIndexOf('.');
                        if (index1 != -1) {
                            zipEntryName = zipEntryName.substring(0, index1);
                        }
                        int index2 = zipEntryName.indexOf('$');
                        if (index2 != -1) {
                            zipEntryName = zipEntryName.substring(0, index2);
                        }
                        classSet.add(zipEntryName.replace('/', '.'));
                    }
                }
            }
            catch (IOException e) {
                throw new RuntimeException("could not open jar/zip file " + classPath + " for reading");
            }
        }
        else {
            collectClassNames(dir, dir, classSet);
        }
        return classSet;
    }

    /**
     * Visits all files recursively and collects the class names.
     *
     * @param rootDir the root dir
     * @param file the file
     * @param classes the advisable class set
     */
    private static void collectClassNames(final File rootDir,
                                          final File file,
                                          final Set classes) {
        if (file.isDirectory()) {
            String[] children = file.list();
            for (int i = 0; i < children.length; i++) {
                collectClassNames(rootDir, new File(file, children[i]), classes);
            }
        }
        else {
            String filePath = file.getPath();
            int dirLength = rootDir.getPath().length();
            filePath = filePath.substring(dirLength + 1);
            int index1 = filePath.lastIndexOf('.');
            if (index1 != -1) {
                filePath = filePath.substring(0, index1);
            }
            int index2 = filePath.indexOf('$');
            if (index2 != -1) {
                filePath = filePath.substring(0, index2);
            }
            filePath = filePath.replace(File.separatorChar, '.');

            classes.add(filePath);
        }
    }

    /**
     * Returns a classloader that can load the classes from the path specified.
     *
     * @param classPath the path to the classes.
     * @return the classloader
     */
    private static ClassLoader getClassLoader(final String classPath) {
        final URLClassLoader loader;
        try {
            File repository = new File(classPath);
            loader = new URLClassLoader(new URL[]{repository.toURL()});
        }
        catch (MalformedURLException e) {
            throw new RuntimeException("class repository " + classPath + " does not exist");
        }
        return loader;
    }

    /**
     * Parses a class, retrieves, wrappes up and returns it's meta-data.
     *
     * @param loader the class loader loading the class repository
     * @param classToParse the name of the class to compile
     * @param classPath the path to the class dir or jar file
     * @return the meta-data for the class
     */
    private static ClassMetaData parseClass(final ClassLoader loader,
                                            final String classToParse,
                                            final String classPath) {
        final Class klass;
        try {
            klass = loader.loadClass(classToParse);
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException(classToParse + " could not be found in class repository specified: " + classPath);
        }

        final Method[] methods = klass.getDeclaredMethods();
        final Field[] fields = klass.getDeclaredFields();

        final List methodList = new ArrayList(methods.length);
        for (int i = 0; i < methods.length; i++) {
            methodList.add(createMethodMetaData(methods[i]));
        }

        final List fieldList = new ArrayList(fields.length);
        for (int i = 0; i < fields.length; i++) {
            fieldList.add(createFieldMetaData(fields[i]));
        }

        final ClassMetaData classMetaData = new ClassMetaData();
        classMetaData.setName(classToParse);
        classMetaData.setMethods(methodList);
        classMetaData.setFields(fieldList);

        return classMetaData;
    }

    /**
     * Create a new <code>MethodMetaData</code>.
     *
     * @param method the method
     * @return the method meta-data
     */
    private static MethodMetaData createMethodMetaData(final Method method) {
        final MethodMetaData methodMetaData = new MethodMetaData();

        methodMetaData.setName(method.getName());
        methodMetaData.setModifiers(method.getModifiers());
        methodMetaData.setReturnType(method.getReturnType().getName());

        Class[] parameters = method.getParameterTypes();
        String[] parameterTypes = new String[parameters.length];
        for (int j = 0; j < parameters.length; j++) {
            parameterTypes[j] = parameters[j].getName();
        }
        methodMetaData.setParameterTypes(parameterTypes);

        Class[] exceptions = method.getExceptionTypes();
        String[] exceptionTypes = new String[exceptions.length];
        for (int j = 0; j < exceptions.length; j++) {
            exceptionTypes[j] = exceptions[j].getName();
        }
        methodMetaData.setExceptionTypes(exceptionTypes);
        return methodMetaData;
    }

    /**
     * Create a new <code>FieldMetaData</code>.
     *
     * @param field the field
     * @return the field meta-data
     */
    private static FieldMetaData createFieldMetaData(final Field field) {
        final FieldMetaData fieldMetaData = new FieldMetaData();

        fieldMetaData.setName(field.getName());
        fieldMetaData.setModifiers(field.getModifiers());
        fieldMetaData.setType(field.getType().getName());

        return fieldMetaData;
    }

    /**
     * Runs the compiler from the command line.
     *
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("usage: java [options...] org.codehaus.aspectwerkz.metadata.ClassFileMetaDataCompiler <pathToDefinitionFile> <pathToClasses> <pathToMetaDataDir> <uuidForWeaveModel>");
            System.out.println("       <uuidForWeaveModel> is optional (if not specified one will be generated)");
            System.exit(0);
        }
        System.out.println("compiling weave model...");
        if (args.length == 4) {
            ClassFileMetaDataCompiler.compile(args[0], args[1], args[2], args[3]);
        }
        else {
            ClassFileMetaDataCompiler.compile(args[0], args[1], args[2]);
        }
        System.out.println("weave model for classes in " + args[1] + " have been compiled to " + args[2]);
    }
}
