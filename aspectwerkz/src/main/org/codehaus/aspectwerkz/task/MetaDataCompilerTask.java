/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.task;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

/**
 * Base class for the meta-data compilers Ant tasks.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public abstract class MetaDataCompilerTask extends Task {

    /**
     * The path to the definition file.
     */
    protected String m_definitionFile;

    /**
     * The meta-data dir.
     */
    protected String m_metaDataDir;

    /**
     * The user-defined UUID for the weave model.
     */
    protected String m_uuid;

    /**
     * Sets the definition file path.
     *
     * @param definitionFile the definition file path
     */
    public void setDefinitionFile(final String definitionFile) {
        m_definitionFile = definitionFile;
    }

    /**
     * Sets the weave model dir.
     *
     * @param metaDataDir the weave model dir
     */
    public void setMetaDataDir(final String metaDataDir) {
        m_metaDataDir = metaDataDir;
    }

    /**
     * Sets the user-defined UUID for the weave model.
     *
     * @param uuid the UUID
     */
    public void setUuid(final String uuid) {
        m_uuid = uuid;
    }

    /**
     * To be overridden.
     *
     * @throws BuildException
     */
    public abstract void execute() throws BuildException;
}
