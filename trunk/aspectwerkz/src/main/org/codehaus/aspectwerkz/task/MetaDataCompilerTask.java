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

/**
 * Base class for the meta-data compilers Ant tasks.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: MetaDataCompilerTask.java,v 1.4 2003-06-26 19:27:17 jboner Exp $
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
