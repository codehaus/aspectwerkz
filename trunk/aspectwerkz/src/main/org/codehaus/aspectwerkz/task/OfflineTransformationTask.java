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
package org.codehaus.aspectwerkz.task;

import java.io.*;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

/**
 * <code>OfflineTransformationTask</code> is an Ant Task that transforms
 * the a class directory structure recursivly using the AspectWerkz -offline
 * mode.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: OfflineTransformationTask.java,v 1.6 2003-07-03 13:10:49 jboner Exp $
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
        if (m_aspectWerkzHome == null) throw new BuildException("aspectWerkzHome must be specified");
        if (m_classesDir == null) throw new BuildException("classesDir must be specified");
        if (m_definitionFile == null) throw new BuildException("definitionFile must be specified");

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
            Process p = Runtime.getRuntime().exec(command.toString(), new String[]{"ASPECTWERKZ_HOME=" + m_aspectWerkzHome, "JAVA_HOME=" + System.getProperty("java.home"), "CLASSPATH=" + System.getProperty("java.class.path")});
            System.out.flush();
            BufferedReader stdOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String out, err = null;

            while ((out = stdOut.readLine()) != null || (err = stdErr.readLine()) != null) {
                if (out != null) {
                    System.out.println(out);
                    System.out.flush();
                }
                if (err != null) {
                    System.err.println("Error: " + err);
                }
            }
            p.waitFor();
            if (p.exitValue() != 0)
                throw new BuildException("Failed to transform classes, exit code: " + p.exitValue());
        } catch (Throwable e) {
            throw new BuildException("could not transform the classes due to: " + e);
        }
    }
}
