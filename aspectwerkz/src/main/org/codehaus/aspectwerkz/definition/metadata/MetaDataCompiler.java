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
package org.codehaus.aspectwerkz.definition.metadata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.IOException;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.definition.metadata.ClassMetaData;

/**
 * Base class for the meta-data compilers.
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: MetaDataCompiler.java,v 1.1.1.1 2003-05-11 15:14:00 jboner Exp $
 */
public abstract class MetaDataCompiler {

    /**
     * The suffix for the meta-data file.
     */
    public static final String META_DATA_FILE_SUFFIX = ".ser";

    /**
     * Name of the class names file.
     */
    public static final String CLASS_LIST = "classes";

    /**
     * The name of the weave model file.
     */
    public static final Object WEAVE_MODEL = "weaveModel";

    /**
     * Create the meta-data dir (if it does not exist).
     *
     * @param metaDataDir the meta-data dir
     */
    protected static void createMetaDataDir(final String metaDataDir) {
        File dir = new File(metaDataDir);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new RuntimeException("could not create directory " + metaDataDir);
            }
        }
    }

    /**
     * Save the class meta-data to file.
     *
     * @param metaDataDir the dir to save to
     * @param weaveModel the weave model so save
     */
    protected static void saveWeaveModelToFile(final String metaDataDir,
                                               final WeaveModel weaveModel) {
        final StringBuffer filename = new StringBuffer();
        filename.append(metaDataDir);
        filename.append(File.separator);
        filename.append(WEAVE_MODEL);
        filename.append(META_DATA_FILE_SUFFIX);

        try {
            ObjectOutput out = new ObjectOutputStream(
                    new FileOutputStream(filename.toString()));
            out.writeObject(weaveModel);
            out.close();
        }
        catch (IOException e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Save the class meta-data to file.
     *
     * @param metaDataDir the dir to save to
     * @param classToParse the name of the class parsed
     * @param classMetaData the meta-data
     */
    protected static void saveClassMetaDataToFile(
            final String metaDataDir,
            final String classToParse,
            final ClassMetaData classMetaData) {
        final StringBuffer filename = new StringBuffer();
        filename.append(metaDataDir);
        filename.append(File.separator);
        filename.append(classToParse);
        filename.append(META_DATA_FILE_SUFFIX);
        try {
            ObjectOutput out = new ObjectOutputStream(
                    new FileOutputStream(filename.toString()));
            out.writeObject(classMetaData);
            out.close();
        }
        catch (IOException e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Saves the list of class names to file.
     *
     * @param metaDataDir the dir to save to
     * @param classMetaData the meta-data
     */
    protected static void saveClassNamesToFile(final String metaDataDir,
                                               final ClassList classList) {
        final StringBuffer filename = new StringBuffer();
        filename.append(metaDataDir);
        filename.append(File.separator);
        filename.append(CLASS_LIST);
        filename.append(META_DATA_FILE_SUFFIX);
        try {
            ObjectOutput out = new ObjectOutputStream(
                    new FileOutputStream(filename.toString()));
            out.writeObject(classList);
            out.close();
        }
        catch (IOException e) {
            throw new WrappedRuntimeException(e);
        }
    }
}
