/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.task;

import org.apache.tools.ant.BuildException;

import org.codehaus.aspectwerkz.metadata.ClassFileMetaDataCompiler;

/**
 * <code>ClassFileMetaDataCompilerTask</code> is an Ant Task that parses a
 * given class dir or jar file A retrieves A stores meta-data for all the
 * introduced <code>Introduction</code>s.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class ClassFileMetaDataCompilerTask extends MetaDataCompilerTask {

    /**
     * The class repository.
     */
    private String m_repository;

    /**
     * Sets the source dir.
     *
     * @param repository the source dir
     */
    public void setRepository(final String repository) {
        m_repository = repository;
    }

    /**
     * Executes the task.
     *
     * @throws BuildException
     */
    public void execute() throws BuildException {
        System.out.println("compiling weave model...");
        ClassFileMetaDataCompiler.compile(m_definitionFile, m_repository, m_metaDataDir, m_uuid);
        System.out.println("weave model for classes in " + m_repository + " have been compiled to " + m_metaDataDir);
    }
}
