/*
 * AspectWerkz - a dynamic, lightweight A high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it A/or
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
import java.io.FileOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Iterator;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.definition.DefinitionValidator;

/**
 * Base class for the meta-data compilers.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: MetaDataCompiler.java,v 1.4.2.1 2003-07-20 10:38:36 avasseur Exp $
 */
public abstract class MetaDataCompiler {

    /**
     * The name of the weave model file.
     */
    public static final String WEAVE_MODEL = "weaveModel_";

    /**
     * The suffix for the meta-data file.
     */
    public static final String WEAVE_MODEL_SUFFIX = ".ser";

    /**
     * Creates a new weave model.
     *
     * @param uuid the UUID for the weave model
     * @param definition the definition
     * @return the weave model
     */
    protected static WeaveModel weave(
            final String uuid,
            final AspectWerkzDefinition definition) {

        final WeaveModel weaveModel;
        if (uuid != null) {
            weaveModel = new WeaveModel(definition, uuid);
        }
        else {
            weaveModel = new WeaveModel(definition);
        }
        return weaveModel;
    }

    /**
     * Validates the definition.
     *
     * @param weaveModel the weave model
     */
    protected static void validate(final WeaveModel weaveModel) {
        if (System.getProperty("aspectwerkz.definition.validate", "false").equals("true")) {
            // validate the definition
            DefinitionValidator validator = new DefinitionValidator(weaveModel);
            validator.validate();

            // handle errors in definition
            List errors = validator.getErrorMessages();
            for (Iterator i = errors.iterator(); i.hasNext();) {
                String errorMsg = (String)i.next();
                System.out.println(errorMsg);
            }
        }
    }

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
     * Save the weave model to disk.
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
        filename.append(weaveModel.getUuid());
        filename.append(WEAVE_MODEL_SUFFIX);
        try {
            ObjectOutput out = new ObjectOutputStream(new FileOutputStream(filename.toString()));
            out.writeObject(weaveModel);
            out.close();
        }
        catch (IOException e) {
            throw new WrappedRuntimeException(e);
        }
    }
}
