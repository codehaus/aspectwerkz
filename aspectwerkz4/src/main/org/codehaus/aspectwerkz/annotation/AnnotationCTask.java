/************1**************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

/**
 * AnnotationC Ant task.
 *
 * Use the following parameters to configure the task:
 * <ul>
 * <li>verbose: [optional] flag marking the task verbosity [true / false]</li>
 * <li>properties: [optional] path to a properties file when user-defined annoations are to be used</li>
 * <li>destdir: [optional unless input classes are in more than one path] directory where to put annnotated class files</li>
 * <li>copytodest: [optional] filename pattern to copy extra resources like dtd, xml, or properties files that were found
 *      in the input classes path(s). By defaults, only ".class" files will be handled. It is ignored if "destdir" is not set.</li>
 * </ul>
 * <p/>
 * Use the following parameters to configure the classpath to point to the classes to be weaved. Those can be specified
 * with nested elements as well / instead:
 * <ul>
 * <li>classpath: classpath of classes to annotated, as well as classpath to discover user-defined annotations if any</li>
 * <li>classpathref: classpath reference of classes to annotated, as well as classpath to discover user-defined annotations if any</li>
 * <li>srcdir: directory where to find annotated java source files</li>
 * <li>sourcepath: path where to find annotated java source files</li>
 * <li>sourcepathref: path reference where to find annotated java source files</li>
 * </ul>
 * <p/>
 * Nested elements are similar to the "javac" task when you configure a classpath and a sourcepath:
 * <ul>
 * <li>classpath: Path-like structure of classes to annotated, as well as classpath to discover user-defined annotations if any</li>
 * <li>src: single path entry of annotated java source files</li>
 * <li>sourcepath: Path-like structure of annotated java source files</li>
 * <li>fileset: fileset to contain annotated java source files</li>
 * </ul>
 * <p/>
 *
 * @author <a href='mailto:the_mindstorm@evolva.ro'>the_mindstorm(at)evolva(dot)ro</a>
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class AnnotationCTask extends Task {

    private final static String CLASS_PATTERN = "**/*.class";


    private boolean m_verbose;
    private String m_includePattern;
    private Path m_classpath;
    private Path m_src;
    private File m_properties;
    private File m_destdir;
    private List m_filesets = new ArrayList();

    /**
     * properties=..
     *
     * @param annotationFile
     */
    public void setProperties(File annotationFile) {
        m_properties = annotationFile;
    }

    /**
     * <task verbose=..>
     *
     * @param isVerbose
     */
    public void setVerbose(boolean isVerbose) {
        m_verbose = isVerbose;
    }

    /**
     * <task copytodest="** slash *">
     * @param pattern
     */
    public void setCopytodest(String pattern) {
        m_includePattern = pattern;
    }

    /**
     * <task destdir=..>
     *
     * @param destdir
     */
    public void setDestdir(File destdir) {
        m_destdir = destdir;
    }

    //-- <src .., <sourcepath.. and srcdir=.. sourcepathref=..

    public Path createSrc() {
        if (m_src == null)
            m_src = new Path(getProject());
        return m_src.createPath();
    }

    public void setSrcdir(Path srcDir) {
        if (m_src == null)
            m_src = srcDir;
        else
            m_src.append(srcDir);
    }

    public void setSourcepath(Path sourcepath) {
        if (m_src == null)
            m_src = sourcepath;
        else
            m_src.append(sourcepath);
    }

    public Path createSourcepath() {
        if (m_src == null)
            m_src = new Path(getProject());
        return m_src.createPath();
    }

    public void setSourcepathRef(Reference r) {
        createSourcepath().setRefid(r);
    }


    //--- classpath

    public void setClasspath(Path classpath) {
        if (m_classpath == null)
            m_classpath = classpath;
        else
            m_classpath.append(classpath);
    }

    public Path createClasspath() {
        if (m_classpath == null)
            m_classpath = new Path(getProject());
        return m_classpath.createPath();
    }

    public void setClasspathRef(Reference r) {
        createClasspath().setRefid(r);
    }

    //---- fileset for source files

    public void addFileset(FileSet fileset) {
        m_filesets.add(fileset);
    }

    //-- Ant task

    public void execute() throws BuildException {
        try {
            if (m_classpath == null) {
                throw new BuildException("No classes specified [<classpath, classpath=.. classpathref=..]");
            }
            if (m_destdir == null && m_classpath.list().length > 1) {
                throw new BuildException(
                        "When using more than one classpath directory, it is mandatory to specify [destdir=..]"
                );
            }
            if (m_filesets.size() == 0 && (m_src == null || m_src.size() == 0)) {
                throw new BuildException("No source specified [<include, <sourcepath, srcdir=..]");
            }
            if (m_properties != null && !m_properties.exists() && !m_properties.isFile()) {
                throw new BuildException("properties file specified but not a valid file [" + m_properties + "]");
            }

            // compute source directory list
            List srcDirs = getDirectories(m_src);
            List srcFiles = getFilesetFiles(m_filesets);
            List classpathDirs = getDirectories(m_classpath);

            if (m_verbose) {
                System.out.println("Source dir   : " + dump(srcDirs));
                System.out.println("Source files : " + dump(srcFiles));
                System.out.println("Classpath    : " + dump(classpathDirs));
                System.out.println("Destdir      : " + m_destdir);
                System.out.println("Properties   : " + m_properties);
                System.out.println("Copytodest   : " + m_includePattern);
            }

            AnnotationC.compile(
                    m_verbose,
                    (String[])srcDirs.toArray(new String[]{}),
                    (String[])srcFiles.toArray(new String[]{}),
                    (String[])classpathDirs.toArray(new String[]{}),
                    m_destdir == null ? null : m_destdir.getAbsolutePath(),
                    m_properties == null ? null : m_properties.getAbsolutePath()
            );

            if (m_destdir != null) {
                if (m_verbose) {
                    System.out.println("Copying residual files to dest dir...");
                }
                copySourcesToDest();
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new BuildException(e);
        }
    }

    private List getFilesetFiles(List filesets) throws BuildException {
        List files = new ArrayList();
        for (Iterator iterator = filesets.iterator(); iterator.hasNext();) {
            FileSet fileset = (FileSet) iterator.next();
            DirectoryScanner ds = fileset.getDirectoryScanner(getProject());
            for (int i = 0; i < ds.getIncludedFiles().length; i++) {
                String file = ds.getIncludedFiles()[i];
                files.add(ds.getBasedir() + File.separator + file);
            }
        }
        return files;
    }

    private List getDirectories(Path path) throws BuildException {
        List dirs = new ArrayList();
        if (path == null)
            return dirs;
        for (int i = 0; i < path.list().length; i++) {
            File dir = getProject().resolveFile(path.list()[i]);
            if (!dir.exists()) {
                throw new BuildException(" \"" + dir.getPath() + "\" does not exist!", getLocation());
            }
            dirs.add(dir.getAbsolutePath());
        }
        return dirs;
    }

    private String dump(List strings) {
        StringBuffer sb = new StringBuffer();
        for (Iterator iterator = strings.iterator(); iterator.hasNext();) {
            Object o = (Object) iterator.next();
            sb.append(o.toString()).append(File.pathSeparator);
        }
        return sb.toString();
    }

    private void copySourcesToDest() throws BuildException {
        Copy copy = new Copy();

        copy.setProject(getProject());
        copy.setTodir(m_destdir);
        copy.setOverwrite(false);
        copy.setTaskName("copy");
        copy.setVerbose(m_verbose);

        List sourceDir = getDirectories(m_src);
        for (Iterator iterator = sourceDir.iterator(); iterator.hasNext();) {
            String dir = (String) iterator.next();
            FileSet anonFs  = new FileSet();
            anonFs.setIncludes(CLASS_PATTERN);
            if (m_includePattern != null) {
                anonFs.setIncludes(m_includePattern);
            }
            anonFs.setDir(new File(dir));
            copy.addFileset(anonFs);
        }
        copy.execute();
    }

}