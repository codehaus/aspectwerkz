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

import java.io.File;
import java.io.InputStream;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

/**
 * <code>OfflineTransformationTask</code> is an Ant Task that transforms
 * the a class directory structure recursivly using the AspectWerkz -offline
 * mode.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: OfflineTransformationTask.java,v 1.4 2003-06-09 07:04:13 jboner Exp $
 */
public class OfflineTransformationTask extends Task {

    /**
     * The home of the aspectwerkz distribution.
     */
    private String m_aspectWerkzHome;

    /**
     * The path to the classes to transform.
     */
    private String m_classesDir;

    /**
     * The path to the XML definition file.
     */
    private String m_definitionFile;

    /**
     * The path to the meta-data dir.
     */
    private String m_metaDataDir;

    /**
     * Sets the aspectwerkz home dir.
     *
     * @param aspectWerkzHome the aspectwerkz home dir
     */
    public void setAspectWerkzHome(final String aspectWerkzHome) {
        m_aspectWerkzHome = aspectWerkzHome;
    }

    /**
     * Sets the path to the classes to transform.
     *
     * @param classesDir the path to the classes
     */
    public void setClassesDir(final String classesDir) {
        m_classesDir = classesDir;
    }

    /**
     * Sets the path to the XML definition file.
     *
     * @param definitionFile the path to the XML definition file
     */
    public void setDefinitionFile(final String definitionFile) {
        m_definitionFile = definitionFile;
    }

    /**
     * The path to the meta-data dir.
     *
     * @param metaDataDir the path to the meta-data dir
     */
    public void setMetaDataDir(final String metaDataDir) {
        m_metaDataDir = metaDataDir;
    }

    /**
     * Executes the task.
     *
     * @throws org.apache.tools.ant.BuildException
     */
    public void execute() throws BuildException {
        if (m_aspectWerkzHome == null) throw new IllegalArgumentException("aspectWerkzHome must be specified");
        if (m_classesDir == null) throw new IllegalArgumentException("classesDir must be specified");
        if (m_definitionFile == null) throw new IllegalArgumentException("definitionFile must be specified");

        System.out.println("CAUTION: This Ant task might be a bit shaky, does not show errors in compilation process properly (use at own risk or patch it :-))");
        System.out.println("NOTE: Make shure that you don't transform your classes more than once (without recompiling first)");

        StringBuffer command = new StringBuffer();
        command.append(m_aspectWerkzHome);
        command.append(File.separator);
        command.append("bin");
        command.append(File.separator);
        command.append("aspectwerkz");
        if (System.getProperty("os.name").startsWith("Win") ||
                System.getProperty("os.name").startsWith("win")) {
            command.append(".bat");
        }
        command.append(" -offline ");
        command.append(m_classesDir);
        command.append(' ');
        command.append(m_definitionFile);
        command.append(' ');
        if (m_metaDataDir != null) {
            command.append(m_metaDataDir);
        }

        try {
            Process p = Runtime.getRuntime().exec(command.toString());
            System.out.flush();
            InputStream stdInput = p.getInputStream();
            Character newLine = new Character('\n');
            StringBuffer line = new StringBuffer();
            while (stdInput != null) {
                if (stdInput.available() > 0) {
                    char inChar = (char)stdInput.read();
                    if (new Character(inChar).equals(newLine)) {
                        System.out.print(line.toString());
                        System.out.flush();
                        if (line.toString().startsWith("Offline transformation")) {
                            return;
                        }
                        line = new StringBuffer();
                    }
                    else {
                        line.append(inChar);
                    }
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException("could not transform the classes due to: " + e);
        }
    }
}
