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

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileFilter;

import gnu.trove.TObjectIntHashMap;

import org.codehaus.aspectwerkz.AspectWerkz;
import org.codehaus.aspectwerkz.ContextClassLoader;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.metadata.MetaDataCompiler;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.util.UuidGenerator;

/**
 * Implements the weaving model for the system.
 * The weave model is an abstract object representation of the how the
 * application will be transformed.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: WeaveModel.java,v 1.6 2003-06-27 09:26:10 jboner Exp $
 */
public class WeaveModel implements Serializable {

    /**
     * Serial version UID.
     * @todo recalculate
     */
    private static final long serialVersionUID = -2072601774035191615L;;

    /**
     * The path to the definition file.
     */
    public static final String DEFINITION_FILE =
            System.getProperty("aspectwerkz.definition.file", null);

    /**
     * The path to the meta-data dir.
     */
    public static String META_DATA_DIR =
            System.getProperty("aspectwerkz.metadata.dir", null);

    /**
     * The name of the timestamp file.
     */
    public static final String TIMESTAMP = ".model_timestamp";

    /**
     * The timestamp, holding the last time that the weave model was read.
     */
    private static File s_timestamp;

    /**
     * Holds the weave models.
     */
    private static Map s_weaveModels = new HashMap();

    /**
     * Holds the introduction meta-data.
     */
    private final Map m_introductionMetaData = new HashMap();

    /**
     * The AspectWerkz definition.
     */
    private final AspectWerkzDefinition m_definition;

    /**
     * A UUID for the weave model.
     */
    private final String m_uuid;

    /**
     * Loads and returns all weave models.
     *
     * @todo timestamp handling is not implemented for this method
     * @return a list with all the weave models
     */
    public static List loadModels() {
        final List weaveModels = new ArrayList();

        if (DEFINITION_FILE != null && META_DATA_DIR == null) {
            // definition file is specified but no meta-data dir =>
            // create a weave model in memory
            weaveModels.add(createModel());
        }
        else if (META_DATA_DIR == null) {
            // no definition file and no meta-data dir =>
            // try to locate the default weave model as a resource on the classpath
            weaveModels.add(loadModelAsResource(AspectWerkz.DEFAULT_SYSTEM));
        }
        else {
            // we have a meta-data dir => read in all weave models
            File metaDataDir = new File(META_DATA_DIR);
            if (!metaDataDir.exists()) throw new RuntimeException(META_DATA_DIR + " meta-data directory does not exist. Create a meta-data dir and specify it with the -Daspectwerkz.metadata.dir=... option (or remove the option completely)");

            FileFilter fileFilter = new FileFilter() {
                public boolean accept(File file) {
                    return file.getName().startsWith(MetaDataCompiler.WEAVE_MODEL);
                }
            };
            File[] files = metaDataDir.listFiles(fileFilter);

            try {
                synchronized (s_weaveModels) {
                    for (int i = 0; i < files.length; i++) {
                        File file = files[i];
                        ObjectInputStream in =
                                new ObjectInputStream(new FileInputStream(file));
                        final WeaveModel weaveModel = (WeaveModel)in.readObject();
                        in.close();
//                        setTimestamp();
                        s_weaveModels.put(weaveModel.getUuid(), weaveModel);
                        weaveModels.add(weaveModel);
                    }
                }
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        }
        return weaveModels;
    }

    /**
     * Loads the current weave model from disk.
     * Only loads from the disk if the timestamp for the latest parsing is
     * older than the timestamp for the weave model.
     *
     * @todo does the lazy loading and timestamp stuff really work? In all cases?
     *
     * @param uuid the uuid for the weave model to load (null is allowed if only
     *             XML definition is used)
     * @return the weave model
     */
    public static WeaveModel loadModel(final String uuid) {
        if (DEFINITION_FILE != null && META_DATA_DIR == null) {
            // definition file is specified but no meta-data dir =>
            // create a weave model in memory
            return createModel();
        }
        else if (META_DATA_DIR == null) {
            // no meta-data dir => try to locate the weave model as a resource
            // on the classpath
            return loadModelAsResource(uuid);
        }
        else {
            // meta-data dir specified => try locate the weave model in the
            // meta-data dir
            return loadModelFromSpecificMetaDataDir(uuid);
        }
    }

    /**
     * Creates a new weave model in memory, is not written to disk.
     * Only creates a new model if the XML definition has changed.
     *
     * @return the weave model
     */
    public static WeaveModel createModel() {
        boolean isDirty = false;

        final AspectWerkzDefinition definition =
                AspectWerkzDefinition.
                getDefinition(isDirty);

        if (isDirty || !s_weaveModels.containsKey(AspectWerkz.DEFAULT_SYSTEM)) {
            synchronized (s_weaveModels) {
                s_weaveModels.put(
                        AspectWerkz.DEFAULT_SYSTEM,
                        new WeaveModel(definition, AspectWerkz.DEFAULT_SYSTEM));
            }
        }
        return (WeaveModel)s_weaveModels.get(AspectWerkz.DEFAULT_SYSTEM);
    }

    /**
     * Loads an existing weave model from disk.
     * Only loads a new model from disk if it has changed.
     *
     * @param uuid the uuid for the weave model to load
     * @return the weave model
     */
    public static WeaveModel loadModelAsResource(final String uuid) {
//        if (s_weaveModels.containsKey(uuid)) {
//            return (WeaveModel)s_weaveModels.get(uuid);
//        }
        final StringBuffer weaveModelName = new StringBuffer();
        weaveModelName.append(MetaDataCompiler.WEAVE_MODEL);
        weaveModelName.append(uuid);
        weaveModelName.append(MetaDataCompiler.WEAVE_MODEL_SUFFIX);

        synchronized (s_weaveModels) {
            InputStream in = ContextClassLoader.getResourceAsStream(weaveModelName.toString());
            if (in == null) throw new RuntimeException("weave model with UUID <" + uuid + "> could not be found on classpath");

            try {
                ObjectInputStream oin = new ObjectInputStream(in);
                final WeaveModel weaveModel = (WeaveModel)oin.readObject();
                s_weaveModels.put(weaveModel.getUuid(), weaveModel);
                oin.close();
                return weaveModel;
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        }
    }

    /**
     * Loads an existing weave model from disk.
     * Is called when the meta-data dir has been specified.
     * Only loads a new model from disk if it has changed.
     *
     * @param uuid the uuid for the weave model to load
     * @return the weave model
     */
    public static WeaveModel loadModelFromSpecificMetaDataDir(final String uuid) {
        if (!new File(META_DATA_DIR).exists()) throw new RuntimeException(META_DATA_DIR + " meta-data directory does not exist. Create a meta-data dir and specify it with the -Daspectwerkz.metadata.dir=... option (or remove the option completely)");

        final StringBuffer weaveModelPath = new StringBuffer();
        weaveModelPath.append(META_DATA_DIR);
        weaveModelPath.append(File.separator);
        weaveModelPath.append(MetaDataCompiler.WEAVE_MODEL);
        weaveModelPath.append(uuid);
        weaveModelPath.append(MetaDataCompiler.WEAVE_MODEL_SUFFIX);

        // get the timestamp of the weave model file
        final long weaveModelTimestamp =
                new File(weaveModelPath.toString()).lastModified();

        if (weaveModelTimestamp == 0) {
            return createModel(); // no weave model; fall back on creating one in memory
        }

        // weave model is not updated; don't read, return old version
        if (weaveModelTimestamp < getTimestamp() && s_weaveModels.containsKey(uuid)) {
            return (WeaveModel)s_weaveModels.get(uuid);
        }

        // read weave model from disk
        try {
            synchronized (s_weaveModels) {
                File file = new File(weaveModelPath.toString());
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
                final WeaveModel weaveModel = (WeaveModel)in.readObject();
                in.close();
                setTimestamp();
                s_weaveModels.put(weaveModel.getUuid(), weaveModel);
                return weaveModel;
            }
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Returns the definition with a specific UUID.
     *
     * @param uuid the UUID
     * @return the definition
     */
    public static AspectWerkzDefinition getDefinition(final String uuid) {
        WeaveModel weaveModel = (WeaveModel)s_weaveModels.get(uuid);
        if (weaveModel == null) {
            weaveModel = WeaveModel.loadModel(uuid);
        }
        return weaveModel.getDefinition();
    }

    /**
     * Creates a new weave model. Sets the aspectwerkz definition.
     *
     * @param definition the definition
     */
    public WeaveModel(final AspectWerkzDefinition definition) {
        m_uuid = UuidGenerator.generate(this);
        m_definition = definition;
    }

    /**
     * Creates a new weave model. Sets the aspectwerkz definition.
     *
     * @param definition the definition
     * @param uuid the pre-defined uuid (override the generated)
     */
    public WeaveModel(final AspectWerkzDefinition definition,
                      final String uuid) {
        m_uuid = uuid;
        m_definition = definition;
    }

    /**
     * Returns the UUID for the weave model.
     *
     * @return the UUID
     */
    public String getUuid() {
        return m_uuid;
    }

    /**
     * Returns the AspectWerkz definition.
     *
     * @return the AspectWerkz definition
     */
    public AspectWerkzDefinition getDefinition() {
        return m_definition;
    }

    /**
     * Adds the compiled meta-data for a certain introduction.
     *
     * @param classMetaData the meta-data for a certain introduction
     */
    public void addIntroductionMetaData(final ClassMetaData classMetaData) {
        m_introductionMetaData.put(classMetaData.getClassName(), classMetaData);
    }

    /**
     * Returns the names of the introductions.
     *
     * @param className the name of the class
     * @return the names
     */
    public List getIntroductionNames(final String className) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        return m_definition.getIntroductionNames(className);
    }

    /**
     * Returns the name of the interface for an introduction.
     *
     * @param introductionName the name of the introduction
     * @return the name of the interface
     */
    public String getIntroductionInterfaceName(final String introductionName) {
        if (introductionName == null) throw new IllegalArgumentException("introduction name can not be null");
        return m_definition.getIntroductionInterfaceName(introductionName);
    }

    /**
     * Returns the name of the implementation for an introduction.
     *
     * @param introductionName the name of the introduction
     * @return the name of the interface
     */
    public String getIntroductionImplementationName(final String introductionName) {
        if (introductionName == null) throw new IllegalArgumentException("introduction name can not be null");
        return m_definition.getIntroductionImplementationName(introductionName);
    }

    /**
     * Returns the index for a specific introduction.
     *
     * @param introductionName the name of the introduction
     * @return the index
     */
    public int getIntroductionIndex(final String introductionName) {
        if (introductionName == null) throw new IllegalArgumentException("introduction name can not be null");
        return m_definition.getIntroductionIndex(introductionName);
    }

    /**
     * Returns the indexes for the introductions.
     *
     * @return the indexes
     */
    public TObjectIntHashMap getIntroductionIndexes() {
        return m_definition.getIntroductionIndexes();
    }

    /**
     * Returns the methods meta-data for a specific introduction.
     *
     * @param introductionName the name of the introduction
     * @return a list with the methods meta-data
     */
    public List getIntroductionMethodsMetaData(final String introductionName) {
        if (introductionName == null) throw new IllegalArgumentException("introduction name can not be null");

        String implName = getIntroductionImplementationName(introductionName);
        if (implName == null) {
            return null; // interface introduction
        }

        List methodMetaDataList = null;
        try {
            methodMetaDataList = ((ClassMetaData)m_introductionMetaData.
                    get(implName)).getMethods();
        }
        catch (NullPointerException e) {
            StringBuffer cause = new StringBuffer();
            cause.append("meta-data for introduction ");
            cause.append(introductionName);
            cause.append(" could not be found (have you compiled and specified a weave model?)");
            throw new DefinitionException(cause.toString());
        }
        return methodMetaDataList;
    }

    /**
     * Checks if a class is advised
     *
     * @param className the name or the class
     * @return boolean
     */
    public boolean isAdvised(final String className) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        return m_definition.isAdvised(className);
    }

    /**
     * Checks if a class has an <tt>Introduction</tt>.
     *
     * @param className the name or the class
     * @return boolean
     */
    public boolean hasIntroductions(final String className) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        return m_definition.hasIntroductions(className);
    }

    /**
     * Checks if a method has a <tt>MethodPointcut</tt>.
     *
     * @param className the name or the class
     * @param methodMetaData the method meta-data
     * @return boolean
     */
    public boolean hasMethodPointcut(final String className,
                                     final MethodMetaData methodMetaData) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");
        return m_definition.hasMethodPointcut(className, methodMetaData);
    }

    /**
     * Checks if a class and field has a <tt>GetFieldPointcut</tt>.
     *
     * @param className the name or the class
     * @param fieldMetaData the meta-data for the field
     * @return boolean
     */
    public boolean hasGetFieldPointcut(final String className,
                                       final FieldMetaData fieldMetaData) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        if (fieldMetaData == null) throw new IllegalArgumentException("field meta-data can not be null");
        return m_definition.hasGetFieldPointcut(className, fieldMetaData);
    }

    /**
     * Checks if a class and field has a <tt>SetFieldPointcut</tt>.
     *
     * @param className the name or the class
     * @param fieldMetaData the meta-data for the field
     * @return boolean
     */
    public boolean hasSetFieldPointcut(final String className,
                                       final FieldMetaData fieldMetaData) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        if (fieldMetaData == null) throw new IllegalArgumentException("field meta-data can not be null");
        return m_definition.hasSetFieldPointcut(className, fieldMetaData);
    }

    /**
     * Checks if a class and method has a <tt>ThrowsPointcut</tt>.
     *
     * @param className the name or the class
     * @param methodMetaData the meta-data for the method
     * @return boolean
     */
    public boolean hasThrowsPointcut(final String className,
                                     final MethodMetaData methodMetaData) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");
        return m_definition.hasThrowsPointcut(className, methodMetaData);
    }

    /**
     * Checks if a class should care about advising caller side method invocations.
     *
     * @param className the name or the class
     * @param methodMetaData the meta-data for the method
     * @return boolean
     */
    public boolean hasCallerSidePointcut(final String className) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        return m_definition.hasCallerSidePointcut(className);
    }

    /**
     * Checks if a class and field has a <tt>ConstructorPointcut</tt>.
     *
     * @param className the name or the class
     * @param methodMetaData the meta-data for the constructor
     * @return boolean
     */
    public boolean hasConstructorPointcut(final String className,
                                          final MethodMetaData methodMetaData) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("constructor meta-data can not be null");

        // TODO: implement
        return true;
    }

    /**
     * Checks if a method is a defined as a caller side method.
     *
     * @param className the name or the class
     * @param methodMetaData the name or the method
     * @return boolean
     */
    public boolean isCallerSideMethod(final String className,
                                      final MethodMetaData methodMetaData) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");
        return m_definition.isCallerSideMethod(className, methodMetaData);
    }

    /**
     * Sets the timestamp for the latest version of the weave model.
     */
    private static void setTimestamp() {
        final long newModifiedTime = System.currentTimeMillis();
        boolean success = s_timestamp.setLastModified(newModifiedTime);
        if (!success) {
        }
    }

    /**
     * Returns the timestamp for the last version of the weave model.
     *
     * @return the timestamp
     */
    private static long getTimestamp() {
        if (s_timestamp == null) {
            s_timestamp = new File(META_DATA_DIR + File.separator + TIMESTAMP);
        }
        final long modifiedTime = s_timestamp.lastModified();
        if (modifiedTime == 0L) {
            // no timestamp
            try {
                s_timestamp.createNewFile();
            }
            catch (IOException e) {
                throw new RuntimeException("could not create timestamp file: " + s_timestamp.getAbsolutePath());
            }
        }
        return modifiedTime;
    }
}
