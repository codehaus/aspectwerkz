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

import org.codehaus.aspectwerkz.definition.metadata.ClassFileMetaDataCompiler;

/**
 * <code>ClassFileMetaDataCompilerTask</code> is an Ant Task that parses a
 * given class dir or jar file and retrieves and stores meta-data for all the
 * introduced <code>Introduction</code>s.
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: ClassFileMetaDataCompilerTask.java,v 1.2 2003-05-12 09:20:46 jboner Exp $
 */
public class ClassFileMetaDataCompilerTask extends Task {

    /**
     * The path to the definition file.
     */
    private String m_definitionFile;

    /**
     * The class repository.
     */
    private String m_repository;

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
     * @param repository the source dir
     */
    public void setRepository(final String repository) {
        m_repository = repository;
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
     * Executes the task.
     *
     * @throws BuildException
     */
    public void execute() throws BuildException {
        System.out.println("compiling weave model...");
        ClassFileMetaDataCompiler.compile(m_definitionFile, m_repository, m_metaDataDir);
        System.out.println("weave model for classes in " + m_repository + " have been compiled to " + m_metaDataDir);
    }
}
