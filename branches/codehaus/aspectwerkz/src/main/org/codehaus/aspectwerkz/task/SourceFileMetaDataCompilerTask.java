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
package org.codehaus.aspectwerkz.task;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

import org.codehaus.aspectwerkz.definition.metadata.SourceFileMetaDataCompiler;

/**
 * <code>SourceFileMetaDataCompilerTask</code> is an Ant Task that parses a
 * given source tree and retrieves and stores meta-data for all the introduced
 * <code>Introduction</code>s.
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: SourceFileMetaDataCompilerTask.java,v 1.1.1.1 2003-05-11 15:14:56 jboner Exp $
 */
public class SourceFileMetaDataCompilerTask extends Task {

    /**
     * The path to the definition file.
     */
    private String m_definitionFile;

    /**
     * The source dir.
     */
    private String m_sourceDir;

    /**
     * The meta-data dir.
     */
    private String m_metaDataDir;

    /**
     * Sets the definition file path.
     *
     * @param definitionFile the definition file path
     */
    public void setDefinitionFile(final String definitionFile) {
        m_definitionFile = definitionFile;
    }

    /**
     * Sets the source dir.
     *
     * @param sourceDir the source dir
     */
    public void setSourceDir(final String sourceDir) {
        m_sourceDir = sourceDir;
    }

    /**
     * Sets the meta-data dir.
     *
     * @param metaDataDir the meta-data dir
     */
    public void setMetaDataDir(final String metaDataDir) {
        m_metaDataDir = metaDataDir;
    }

    /**
     * Executes the task.
     *
     * @throws BuildException
     */
    public void execute() throws BuildException {
        System.out.println("compiling meta-data...");
        SourceFileMetaDataCompiler.compile(m_definitionFile, m_sourceDir, m_metaDataDir);
        System.out.println("meta-data for classes in " + m_sourceDir + " have been compiled to " + m_metaDataDir);
    }
}
