/*
 * $Id: AspectWerkzCTask.java,v 1.1.2.1 2004-10-14 16:34:25 avasseur Exp $
 * $Date: 2004-10-14 16:34:25 $
 */
package org.codehaus.aspectwerkz.compiler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.FileSet;

/**
 * AspectWerkzC offline Ant task.
 * Use the following parameters - all are optional
 * <ul>
 * <li><em>verbose</em>: flag marking the weaver verbosity
 * <li><em>definition</em>: path to aspect definition xml file (optional, can be found on the path as META-INF/aop.xml - even several)</li>
 * </ul>
 * <p/>
 * Inner parameters - mainly required
 * <ul>
 * <li><em>classpath</em>: <tt>Path</tt>-like structure for the classpath to be used by the weaver</li>
 * <li><em>target</em>: <tt>Path</tt>-like structure for the class directories to be weaved</li>
 * <li>fileset: same as target</li>
 * </ul
 * <p/>
 * Options that we usually pass as JVM options are also available but are unlikely to be used
 * <ul>
 * <li><em>backupdir</em>: directory path to backup original classes, defautls to ./_aspectwerkzc
 * <li><em>preprocessor</em>: fully qualified name of the preprocessor. If not set default is used.</li>
 * </ul>
 *
 * @author <a href='mailto:the_mindstorm@evolva.ro'>the_mindstorm(at)evolva(dot)ro</a>
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class AspectWerkzCTask extends Task {
    private final static String AW_TRANSFORM_VERBOSE = "aspectwerkz.transform.verbose";

    private static final String AW_DEFINITION_FILE = "aspectwerkz.definition.file";

    private boolean m_verbose;
    private File m_backupdir;
    private String m_preprocessor;
    private File m_definitionFile;
    private Path m_classpath;
    private Path m_target;
    //private List m_filesets = new ArrayList();


    /**
     * definition=..
     * @param defFile
     */
    public void setDefinition(File defFile) {
        m_definitionFile = defFile;
    }

    /**
     * verbose=..
     * @param verbose
     */
    public void setVerbose(boolean verbose) {
        m_verbose = verbose;
    }

    //-- <target .., <targetpath.. and targetdir=.. targetpathref=..

    public Path createTarget() {
        if (m_target == null)
            m_target = new Path(getProject());
        return m_target.createPath();
    }

    public void setTargetdir(Path srcDir) {
        if (m_target == null)
            m_target = srcDir;
        else
            m_target.append(srcDir);
    }

    public void setTargetpath(Path targetpath) {
        if (m_target == null)
            m_target = targetpath;
        else
            m_target.append(targetpath);
    }

    public Path createTargetpath() {
        if (m_target == null)
            m_target = new Path(getProject());
        return m_target.createPath();
    }

    public void setTargetpathRef(Reference r) {
        createTargetpath().setRefid(r);
    }

    /**
     * backupdir=..
     * @param backupDir
     */
    public void setBackupdir(File backupDir) {
        m_backupdir = backupDir;
    }

    /**
     * preprocessor=..
     * @param preprocessorFqn
     */
    public void setPreprocessor(String preprocessorFqn) {
        m_preprocessor = preprocessorFqn;
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

//    //---- fileset for source files
//    public void addFileset(FileSet fileset) {
//        m_filesets.add(fileset);
//    }

    public void execute() throws BuildException {
        try {
            if (m_definitionFile!=null && !!m_definitionFile.exists() && !m_definitionFile.isFile()) {
                throw new BuildException("Definition file provided does not exists");
            }

            AspectWerkzC compiler = new AspectWerkzC();

            compiler.setHaltOnError(true);
            compiler.setVerbose(m_verbose);
            compiler.setVerify(false);

            if (m_definitionFile != null) {
                System.setProperty(AW_DEFINITION_FILE, m_definitionFile.getAbsolutePath());
            }

            if (m_verbose) {
                System.setProperty(AW_TRANSFORM_VERBOSE, m_verbose ? "true" : "false");
            }

            if (m_backupdir != null && m_backupdir.isDirectory()) {
                compiler.setBackupDir(m_backupdir.getAbsolutePath());
            }

            if (m_verbose) {
                System.out.println("Classpath    : " + dump(getDirectories(m_classpath)));
                System.out.println("Target       : " + dump(getDirectories(m_target)));
                System.out.println("Definition   : " + m_definitionFile);
                System.out.println("Backupdir    : " + m_backupdir);
                System.out.println("Preprocessor : " + m_preprocessor);
            }

            AspectWerkzC.compile(compiler,
                                 getClass().getClassLoader(),
                                 m_preprocessor,
                                 getDirectories(m_classpath),
                                 getDirectories(m_target)
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new BuildException(e, getLocation());
        }
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
            dirs.add(dir);//.getAbsolutePath());
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
}
