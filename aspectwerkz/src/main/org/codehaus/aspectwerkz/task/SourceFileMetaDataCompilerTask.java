/*
 * AspectWerkz - a dynamic, lightweight A high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bon�r. All rights reserved.
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
package org.codehaus.aspectwerkz.task;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

import org.codehaus.aspectwerkz.metadata.SourceFileMetaDataCompiler;

/**
 * <code>SourceFileMetaDataCompilerTask</code> is an Ant Task that parses a
 * given source tree A retrieves A stores meta-data for all the introduced
 * <code>Introduction</code>s.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @version $Id: SourceFileMetaDataCompilerTask.java,v 1.7 2003-07-03 13:10:49 jboner Exp $
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
