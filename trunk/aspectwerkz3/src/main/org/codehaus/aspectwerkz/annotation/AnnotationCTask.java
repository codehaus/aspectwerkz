/************1**************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;

/**
 * AnnotationC Ant task.
 *
 * @author <a href='mailto:the_mindstorm@evolva.ro'>the_mindstorm(at)evolva(dot)ro</a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class AnnotationCTask extends Task {
    protected Path classpath;
    protected boolean verbose;
    protected Path srcdir;
    protected File clsdir;
    protected File properties;
    protected File destdir;
    protected List filesets = new ArrayList();

    public void setVerbose(boolean isVerbose) {
        this.verbose = true;
    }

    public Path createSrc() {
        if (srcdir == null) {
            srcdir = new Path(getProject());
        }
        return srcdir.createPath();
    }

    public void setSrcdir(Path srcdir) {
        if (this.srcdir == null) {
            this.srcdir = srcdir;
        } else {
            this.srcdir.append(srcdir);
        }
    }

    public void setDestdir(File destdir) {
        this.destdir = destdir;
    }

    public void setClsdir(File clsdir) {
        this.clsdir = clsdir;
    }

    public void setProperties(File annotationFile) {
        this.properties = annotationFile;
    }

    public void addSrcfileset(FileSet set) {
        this.filesets.add(set);
    }

    public void execute() throws BuildException {

        try {
            if ((this.srcdir == null || this.srcdir.size() == 0) && this.filesets.size() == 0) {
                throw new BuildException("srcdir or srcfileset cannot be both empty");
            }
            if ((srcdir != null && srcdir.size() != 0) && this.filesets.size() != 0) {
                throw new BuildException("only one of srcdir or srcfileset can be used");
            }

            if (this.clsdir == null || !this.clsdir.isDirectory()) {
                throw new BuildException("clsdir is not a valid directory [" + this.clsdir + "]");
            }
            if (this.properties == null || !this.properties.isFile()) {
                throw new BuildException("properties is not a valid file [" + this.properties + "]");
            }

            boolean useDirs = (srcdir != null && srcdir.size() != 0);
            System.out.println("aici");
            String[] srcList = null;
            if (useDirs) {
                srcList = getSourceDirs(this.srcdir);
            } else {
                srcList = getSourceFiles(this.filesets);
            }

            AnnotationC.compile(
                    srcList,
                    useDirs,
                    this.clsdir.getAbsolutePath(),
                    this.destdir == null ? null : this.destdir.getAbsolutePath(),
                    this.properties.getAbsolutePath()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String[] getSourceFiles(List fsList) throws BuildException {
// deal with the filesets
        for (int i = 0; i < filesets.size(); i++) {
            FileSet fs = (FileSet) filesets.get(i);
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());

            File fromDir = fs.getDir(getProject());

            String[] srcFiles = ds.getIncludedFiles();
            for (int j = 0; j < srcFiles.length; j++) {
                System.out.println(srcFiles[j]);
            }
        }

        return null;
    }

    private String[] getSourceDirs(Path srcPath) throws BuildException {
        String[] list = srcdir.list();
        List dirs = new ArrayList();

        for (int i = 0; i < list.length; i++) {
            File srcDir = getProject().resolveFile(list[i]);
            if (!srcDir.exists()) {
                throw new BuildException(
                        "srcdir \""
                        + srcDir.getPath()
                        + "\" does not exist!", getLocation()
                );
            }

            dirs.add(srcDir.getAbsolutePath());
        }

        return (String[]) dirs.toArray(new String[dirs.size()]);
    }
}
