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

import org.codehaus.aspectwerkz.metadata.SourceFileMetaDataCompiler;

/**
 * <code>SourceFileMetaDataCompilerTask</code> is an Ant Task that parses a
 * given source tree A retrieves A stores meta-data for all the introduced
 * <code>Introduction</code>s.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class SourceFileMetaDataCompilerTask extends MetaDataCompilerTask {

    /**
     * The source dir.
     */
    private String m_sourceDir;

    /**
     * Sets the source dir.
     *
     * @param sourceDir the source dir
     */
    public void setSourceDir(final String sourceDir) {
        m_sourceDir = sourceDir;
    }

    /**
     * Executes the task.
     *
     * @throws BuildException
     */
    public void execute() throws BuildException {
        System.out.println("compiling weave model...");
        SourceFileMetaDataCompiler.compile(m_definitionFile, m_sourceDir, m_metaDataDir, m_uuid);
        System.out.println("weave model for classes in " + m_sourceDir + " have been compiled to " + m_metaDataDir);
    }
}
