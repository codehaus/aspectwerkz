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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;

/**
 * AnnotationC Ant task.
 *
 * @author <a href='mailto:the_mindstorm@evolva.ro'>the_mindstorm(at)evolva(dot)ro</a>
 */
public class AnnotationCTask extends Task {
    protected Path classpath;
    protected boolean verbose;
    protected Path srcdir;
    protected List filesets = new ArrayList();
    protected File srcincludes;
    protected Path clsdir;
    protected File properties;
    protected File destdir;
    // deal with duplicated parameters
    protected int srcParamType = SRC_NOTSET;

    private static final int SRC_NOTSET = 0;
    private static final int SRC_FILESET = 1;
    private static final int SRC_DIR = 2;
    private static final int SRC_DIRPATH = 3;
    private static final int SRC_FILE = 4;
    private static final String[] MSG = {"", "srcfileset", "srcdir", "inner src", "srcincludes"};

    public void setVerbose(boolean isVerbose) {
        this.verbose = true;
    }

    public void setDestdir(File destdir) {
        this.destdir = destdir;
    }

    public Path createSrc() {
        if (isSet(SRC_DIRPATH)) {
            throw tooManyParameters(SRC_DIRPATH);
        }
        if (srcdir == null) {
            srcdir = new Path(getProject());
        }
        return srcdir.createPath();
    }

    public void setSrcdir(Path srcdir) {
        if (isSet(SRC_DIR)) {
            throw tooManyParameters(SRC_DIR);
        }

        if (this.srcdir == null) {
            this.srcdir = srcdir;
        } else {
            this.srcdir.append(srcdir);
        }
    }

    public void setSrcincludes(File srcinclude) {
        if (isSet(SRC_FILE)) {
            throw tooManyParameters(SRC_FILE);
        }
        this.srcincludes = srcinclude;
    }

    public void addSrcfileset(FileSet set) {
        if (isSet(SRC_FILESET)) {
            throw tooManyParameters(SRC_FILESET);
        }
        this.filesets.add(set);
    }

    public Path createCls() {
        if (this.clsdir == null) {
            clsdir = new Path(getProject());
        }
        return clsdir.createPath();
    }

    public void setClsdir(Path clsdir) {
        if (this.clsdir == null) {
            this.clsdir = clsdir;
        } else {
            this.clsdir.append(clsdir);
        }
    }

    public void setProperties(File annotationFile) {
        this.properties = annotationFile;
    }

    public void execute() throws BuildException {
        try {
            if ((this.srcdir == null || this.srcdir.size() == 0) && this.filesets.size() == 0
                && (this.srcincludes == null || !this.srcincludes.isFile())) {
                throw new BuildException("srcdir, srcfileset and srcincludes cannot be all empty");
            }
            if (this.clsdir == null || this.clsdir.size() == 0) {
                throw new BuildException("clsdir cannot be null");
            }
            if (this.properties == null || !this.properties.isFile()) {
                throw new BuildException("properties is not a valid file [" + this.properties + "]");
            }

            boolean useDirs = (srcdir != null && srcdir.size() != 0);

            String[] srcList = null;
            switch (this.srcParamType) {
                case SRC_DIR:
                case SRC_DIRPATH:
                    srcList = getDirList(this.srcdir, "srcdir");
                    break;
                case SRC_FILESET:
                    srcList = getSourceFiles(this.filesets);
                    break;
                case SRC_FILE:
                    srcList = getSourceFiles(this.srcincludes);
                    break;
            }

            AnnotationC.compile(
                    this.verbose,
                    srcList,
                    useDirs,
                    getDirList(this.clsdir, "cls"),
                    this.destdir == null ? null : this.destdir.getAbsolutePath(),
                    this.properties.getAbsolutePath()
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new BuildException(e);
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

    private String[] getSourceFiles(File file) throws BuildException {
        List files = new ArrayList();
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(file));

            String line = reader.readLine();
            File tmpFile = null;
            while (line != null) {
                if (line.length() > 0) {
                    tmpFile = getProject().resolveFile(line);
                    if (!tmpFile.isFile()) {
                        log("file not found: [" + tmpFile + "]", Project.MSG_WARN);
                    } else {
                        files.add(tmpFile.getAbsolutePath());
                    }
                }
                line = reader.readLine();
            }
        } catch (IOException ioe) {
            throw new BuildException(
                    "an error occured while reading from pattern file: " + file,
                    ioe
            );
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException ioe) {
                    //Ignore exception
                }
            }
        }

        return (String[]) files.toArray(new String[files.size()]);
    }

    private String[] getDirList(Path path, String type) throws BuildException {
        String[] list = path.list();
        List dirs = new ArrayList();

        for (int i = 0; i < list.length; i++) {
            File dir = getProject().resolveFile(list[i]);
            if (!dir.exists()) {
                throw new BuildException(
                        type + " \"" + dir.getPath() + "\" does not exist!",
                        getLocation()
                );
            }

            dirs.add(dir.getAbsolutePath());
        }

        return (String[]) dirs.toArray(new String[dirs.size()]);
    }

    /**
     * Checks the src related parameters.
     */
    private boolean isSet(int srcParam) {
        if (SRC_NOTSET == this.srcParamType) { // nothing is set
            this.srcParamType = srcParam;
            return false;
        }
        if (SRC_DIRPATH == srcParam && SRC_DIRPATH == this.srcParamType) { // inner is allowed multiple times
            return false;
        }
        return true;
    }

    private BuildException tooManyParameters(int srcType) {
        return new BuildException(
                "cannot set both " + MSG[this.srcParamType] + " and "
                + MSG[srcType]
        );
    }
}