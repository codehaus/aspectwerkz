/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.compiler;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.types.selectors.FilenameSelector;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Delete;

import java.io.File;

/**
 * Utility class providing file manipulation facilities.
 * <p/>
 * This implementation uses Ant task programmaticaly.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class Utility {

    /** Ant project */
    private Project project;

    /** Ant logger */
    private BuildLogger logger;

    private boolean verbose = false;

    /**
     * Constructs a new project and attach simple logger
     */
    public Utility() {
        project = new Project();
        logger = new DefaultLogger();
        logger.setMessageOutputLevel(0);
        logger.setOutputPrintStream(System.out);
        logger.setErrorPrintStream(System.err);
        project.addBuildListener(logger);
    }

    /**
     * Set verbosity
     */
    public void setVerbose() {
        logger.setMessageOutputLevel(2);
        verbose = true;
    }

    /**
     * Delete recursively a directory and the directory itself
     */
    public void deleteDir(File dir) {
        Delete task = new Delete();
        task.setProject(project);
        task.setTaskName("delete");

        FilenameSelector fns = new FilenameSelector();
        fns.setName("**/*");
        FileSet fs = new FileSet();
        fs.setDir(dir);
        fs.addFilename(fns);
        task.addFileset(fs);
        task.setIncludeEmptyDirs(true);
        task.perform();

        dir.delete();
    }

    /**
     * Copy a file or directory recursively
     */
    public void backupFile(File source, File dest) {
        Copy task = new Copy();
        task.setProject(project);
        task.setTaskName("backup");
        task.setVerbose(verbose);
        //@todo haltOnError
        //copyTask.setFailOnError(haltOnError);

        if (source.isDirectory()) {
            FilenameSelector fns = new FilenameSelector();
            fns.setName("**/*");
            FileSet fs = new FileSet();
            fs.setDir(source);
            fs.addFilename(fns);
            task.addFileset(fs);
            task.setTodir(dest);
            task.setIncludeEmptyDirs(true);
        }
        else {
            task.setFile(source);
            task.setTofile(dest);
        }

        task.setOverwrite(true);
        task.setPreserveLastModified(true);

        task.execute();
    }

    public void log(String msg) {
        project.log(msg);
    }

}
