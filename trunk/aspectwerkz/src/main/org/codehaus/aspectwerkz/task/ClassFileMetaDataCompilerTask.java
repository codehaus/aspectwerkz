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

import org.apache.tools.ant.BuildException;

import org.codehaus.aspectwerkz.definition.metadata.ClassFileMetaDataCompiler;

/**
 * <code>ClassFileMetaDataCompilerTask</code> is an Ant Task that parses a
 * given class dir or jar file and retrieves and stores meta-data for all the
 * introduced <code>Introduction</code>s.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: ClassFileMetaDataCompilerTask.java,v 1.3 2003-06-09 07:04:13 jboner Exp $
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
