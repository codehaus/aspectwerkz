/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * <code>OfflineTransformationTask</code> is an Ant Task that transforms the a class directory structure recursivly
 * using the AspectWerkz -offline mode.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class OfflineTransformationTask extends Task {

    /**
     * The home of the aspectwerkz distribution.
     */
    private String m_aspectWerkzHome;

    /**
     * The path to the classes to transform.
     */
    private String m_classesToTransform;

    /**
     * The path to the XML definition file.
     */
    private String m_definitionFile;

    /**
     * The class path.
     */
    private String m_classPath;

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
     * @param classesToTransform the path to the classes
     */
    public void setClassesToTransform(final String classesToTransform) {
        m_classesToTransform = classesToTransform;
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
     * @param classPath the path to the meta-data dir
     */
    public void setClassPath(final String classPath) {
        m_classPath = classPath;
    }

    /**
     * Executes the task.
     *
     * @throws org.apache.tools.ant.BuildException
     *
     */
    public void execute() throws BuildException {
        if (m_aspectWerkzHome == null) {
            throw new BuildException("AspectWerkz home dir must be specified");
        }
        if (m_classesToTransform == null) {
            throw new BuildException("classes to transform must be specified");
        }
        if (m_definitionFile == null) {
            throw new BuildException("definition file must be specified");
        }

        System.out.println(
                "CAUTION: This Ant task might be a bit shaky, does not show errors in compilation process properly (use at own risk or patch it :-))"
        );
        System.out.println(
                "NOTE: Make shure that you don't transform your classes more than once (without recompiling first)"
        );

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
        command.append(m_definitionFile);
        command.append(' ');
        if (m_classPath != null) {
            command.append(m_classPath);
        }
        command.append(' ');
        command.append(m_classesToTransform);

        try {
            Process p = Runtime.getRuntime().exec(
                    command.toString(),
                    new String[]{
                        "ASPECTWERKZ_HOME=" + m_aspectWerkzHome, "JAVA_HOME=" + System.getProperty("java.home"),
                        "CLASSPATH=" + System.getProperty("java.class.path")
                    }
            );
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
            if (p.exitValue() != 0) {
                throw new BuildException("Failed to transform classes, exit code: " + p.exitValue());
            }
        }
        catch (Throwable e) {
            throw new BuildException("could not transform the classes due to: " + e);
        }
    }
}
