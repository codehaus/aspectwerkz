/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.compiler;

import org.codehaus.aspectwerkz.definition.DefinitionLoader;
import org.codehaus.aspectwerkz.definition.SystemDefinitionContainer;
import org.codehaus.aspectwerkz.hook.ClassPreProcessor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * AspectWerkzC allow for precompilation of class / jar / zip given a class preprocessor. <p/>
 * <h2>Usage</h2>
 * <p/>
 * <pre>
 *     java [-Daspectwerkz.classloader.preprocessor={ClassPreProcessorImpl}] -cp [...]
 *     org.codehaus.aspectwerkz.compiler.AspectWerkzC [-verbose] [-haltOnError] [-verify] [-cp {additional cp i}]*  {target
 *     1} .. {target n}
 *       {ClassPreProcessorImpl} : full qualified name of the ClassPreProcessor implementation (must be in classpath)
 *          defaults to org.codehaus.aspectwerkz.transform.AspectWerkzPreProcessor
 *       {additional cp i} : additionnal classpath needed at compile time (eg: myaspect.jar)
 *          use as many -cp options as needed
 *          supports java classpath syntax for classpath separator: ; on windows, : on others
 *       {target i} : exploded dir, jar, zip files to compile
 *       Ant 1.5 must be in the classpath
 * </pre>
 * <p/>
 * <h2>Classpath note</h2>
 * At the beginning of the compilation, all {target i} are added to the classpath automatically. <br/>This is required
 * to support caller side advices. <p/>
 * <h2>Error handling</h2>
 * For each target i, a backup copy is written in ./_aspectwerkzc/i/target <br/>Transformation occurs on original target
 * class/dir/jar/zip file <br/>On failure, target backup is restored and stacktrace is given <br/><br/>If
 * <i>-haltOnError </i> was set, compilations ends and a <b>complete </b> rollback occurs on all targets, else a status
 * report is printed at the end of the compilation, indicating SUCCESS or ERROR for each given target. <br/>If
 * <i>-verify </i> was set, all compiled class are verified during the compilation and an error is generated if the
 * compiled class bytecode is corrupted. The error is then handled according to the <i>-haltOnError </i> option. <br/>
 * <p/>
 * <h2>Manifest.mf update</h2>
 * The Manifest.mf if present is updated wit the following:
 * <ul>
 * <li>AspectWerkzC-created: date of the compilation</li>
 * <li>AspectWerkzC-preprocessor: full qualified classname of the preprocessor used</li>
 * <li>AspectWerkzC-comment: comments</li>
 * </ul>
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class AspectWerkzC {
    // COMMAND LINE OPTIONS
    private static final String COMMAND_LINE_OPTION_DASH = "-";
    private static final String COMMAND_LINE_OPTION_VERBOSE = "-verbose";
    private static final String COMMAND_LINE_OPTION_HALT = "-haltOnError";
    private static final String COMMAND_LINE_OPTION_VERIFY = "-verify";
    private static final String COMMAND_LINE_OPTION_CLASSPATH = "-cp";
    private static final String COMMAND_LINE_OPTION_TARGETS = "compile.targets";

    /**
     * option used to defined the class preprocessor
     */
    private static final String PRE_PROCESSOR_CLASSNAME_PROPERTY = "aspectwerkz.classloader.preprocessor";

    /**
     * default class preprocessor
     */
    private static final String PRE_PROCESSOR_CLASSNAME_DEFAULT = "org.codehaus.aspectwerkz.transform.AspectWerkzPreProcessor";

    private final static String MF_CUSTOM_DATE = "X-AspectWerkzC-created";

    private final static String MF_CUSTOM_PP = "X-AspectWerkzC-preprocessor";

    private final static String MF_CUSTOM_COMMENT = "X-AspectWerkzC-comment";

    private final static String MF_CUSTOM_COMMENT_VALUE = "AspectWerkzC - AspectWerkz compiler, aspectwerkz.codehaus.org";

    private final static SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final static String BACKUP_DIR = "_aspectwerkzc";

    private boolean verify = false;

    private boolean haltOnError = false;

    private String backupDir = BACKUP_DIR;

    /**
     * class loader in which the effective compilation occurs, child of system classloader
     */
    private URLClassLoader compilationLoader = null;

    /**
     * class preprocessor instance used to compile targets
     */
    private ClassPreProcessor preprocessor = null;

    /**
     * index to keep track of {target i} backups
     */
    private int sourceIndex;

    /**
     * Maps the target file to the target backup file
     */
    private Map backupMap = new HashMap();

    /**
     * Maps the target file to a status indicating compilation was successfull
     */
    private Map successMap = new HashMap();

    private long timer;

    /**
     * Utility for file manipulation
     */
    private Utility utility;

    /**
     * Construct a new Utility, restore the index for backup
     */
    public AspectWerkzC() {
        //@todo check for multiple transformation in compiler or in preprocessor ?
        sourceIndex = 0;
        utility = new Utility();
        timer = System.currentTimeMillis();
    }

    /*
     * public void log(String msg) { utility.log(msg); } public void log(String msg, Throwable t) { utility.log(msg);
     * t.printStackTrace(); }
     */
    public void setVerbose(boolean verbose) {
        utility.setVerbose(verbose);
    }

    public void setHaltOnError(boolean haltOnError) {
        this.haltOnError = haltOnError;
    }

    public void setVerify(boolean verify) {
        this.verify = verify;
    }

    public void setBackupDir(String backup) {
        this.backupDir = backup;
    }

    public Utility getUtility() {
        return utility;
    }

    /**
     * Sets the ClassPreProcessor implementation to use. <p/>The ClassLoader will be set to System ClassLoader when
     * transform(className, byteCode, callerClassLoader) will be called to compile a class.
     */
    public void setPreprocessor(String preprocessor) throws CompileException {
        try {
            Class pp = Class.forName(preprocessor);
            this.preprocessor = (ClassPreProcessor) pp.newInstance();
            this.preprocessor.initialize(new Hashtable());
        } catch (Exception e) {
            throw new CompileException("failed to instantiate preprocessor " + preprocessor, e);
        }
    }

    /**
     * Backup source file in backup_dir/index/file. The backupMap is updated for further rollback
     */
    public void backup(File source, int index) {
        // backup source in BACKUP/index dir
        File dest = new File(this.backupDir + File.separator + index + File.separator + source.getName());
        utility.backupFile(source, dest);

        // add to backupMap in case of rollback
        backupMap.put(source, dest);
    }

    /**
     * Restore the backup registered
     */
    public void restoreBackup() {
        for (Iterator i = backupMap.keySet().iterator(); i.hasNext();) {
            File source = (File) i.next();
            if (!successMap.containsKey(source)) {
                File dest = (File) backupMap.get(source);
                utility.backupFile(dest, source);
            }
        }
    }

    /**
     * Delete backup dir at the end of all compilation
     */
    public void postCompile(String message) {
        restoreBackup();
        utility.log(" [backup] removing backup");
        utility.deleteDir(new File(this.backupDir));
        long ms = Math.max(System.currentTimeMillis() - timer, 1 * 1000);
        System.out.println("( " + (int) (ms / 1000) + " s ) " + message);
        if (!haltOnError) {
            for (Iterator i = backupMap.keySet().iterator(); i.hasNext();) {
                File source = (File) i.next();
                if (successMap.containsKey(source)) {
                    System.out.println("SUCCESS: " + source);
                } else {
                    System.out.println("FAILED : " + source);
                }
            }
        }
    }

    /**
     * Compile sourceFile. If prefixPackage is not null, assumes it is the class package information. <p/>Handles :
     * <ul>
     * <li>directory recursively (exploded jar)</li>
     * <li>jar / zip file</li>
     * </ul>
     */
    public void doCompile(File sourceFile, String prefixPackage) throws CompileException {
        if (sourceFile.isDirectory()) {
            File[] classes = sourceFile.listFiles();
            for (int i = 0; i < classes.length; i++) {
                if (classes[i].isDirectory() && !(this.backupDir.equals(classes[i].getName()))) {
                    String packaging = (prefixPackage != null) ? (prefixPackage + "." + classes[i]
                            .getName()) : classes[i].getName();
                    doCompile(classes[i], packaging);
                } else if (classes[i].getName().toLowerCase().endsWith(".class")) {
                    compileClass(classes[i], prefixPackage);
                } else if (isJarFile(classes[i])) {
                    //@todo: jar encountered in a dir - use case ??
                    compileJar(classes[i]);
                }
            }
        } else if (sourceFile.getName().toLowerCase().endsWith(".class")) {
            compileClass(sourceFile, null);
        } else if (isJarFile(sourceFile)) {
            compileJar(sourceFile);
        }
    }

    /**
     * Compiles .class file using fileName as className and given packaging as package name
     */
    public void compileClass(File file, String packaging) throws CompileException {
        InputStream in = null;
        FileOutputStream fos = null;
        try {
            utility.log(" [compile] " + file.getCanonicalPath());

            // dump bytecode in byte[]
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            in = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            while (in.available() > 0) {
                int length = in.read(buffer);
                if (length == -1) {
                    break;
                }
                bos.write(buffer, 0, length);
            }

            // rebuild className
            String className = file.getName().substring(0, file.getName().length() - 6);
            if (packaging != null) {
                className = packaging + '.' + className;
            }

            // transform
            byte[] transformed = null;
            try {
                transformed = preprocessor.preProcess(className, bos.toByteArray(), compilationLoader);
            } catch (Throwable t) {
                throw new CompileException("weaver failed for class: " + className, t);
            }

            // override file
            fos = new FileOutputStream(file);
            fos.write(transformed);
            fos.close();

            // verify modified class
            if (verify) {
                URLClassLoader verifier = new VerifierClassLoader(
                        compilationLoader.getURLs(),
                        ClassLoader.getSystemClassLoader()
                );
                try {
                    utility.log(" [verify] " + className);
                    Class.forName(className, false, verifier);
                } catch (Throwable t) {
                    utility.log(" [verify] corrupted class: " + className);
                    throw new CompileException("corrupted class: " + className, t);
                }
            }
        } catch (IOException e) {
            throw new CompileException("compile " + file.getAbsolutePath() + " failed", e);
        } finally {
            try {
                in.close();
            } catch (Throwable e) {
                ;
            }
            try {
                fos.close();
            } catch (Throwable e) {
                ;
            }
        }
    }

    /**
     * Compile all .class encountered in the .jar/.zip file. <p/>The target.jar is compiled in the
     * target.jar.aspectwerkzc and the target.jar.aspectwerkzc then overrides target.jar on success.
     */
    public void compileJar(File file) throws CompileException {
        utility.log(" [compilejar] " + file.getAbsolutePath());

        // create an empty jar target.jar.aspectwerkzc
        File workingFile = new File(file.getAbsolutePath() + ".aspectwerkzc");
        if (workingFile.exists()) {
            workingFile.delete();
        }
        ZipFile zip = null;
        ZipOutputStream zos = null;
        try {
            zip = new ZipFile(file);
            zos = new ZipOutputStream(new FileOutputStream(workingFile));
            for (Enumeration e = zip.entries(); e.hasMoreElements();) {
                ZipEntry ze = (ZipEntry) e.nextElement();

                // dump bytes read in byte[]
                InputStream in = zip.getInputStream(ze);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                while (in.available() > 0) {
                    int length = in.read(buffer);
                    if (length == -1) {
                        break;
                    }
                    bos.write(buffer, 0, length);
                }
                in.close();

                // transform only .class file
                byte[] transformed = null;
                if (ze.getName().toLowerCase().endsWith(".class")) {
                    utility.log(" [compilejar] compile " + file.getName() + ":" + ze.getName());
                    String className = ze.getName().substring(0, ze.getName().length() - 6);
                    try {
                        transformed = preprocessor.preProcess(
                                className, bos.toByteArray(),
                                compilationLoader
                        );
                    } catch (Throwable t) {
                        throw new CompileException("weaver failed for class: " + className, t);
                    }
                } else {
                    transformed = bos.toByteArray();
                }

                // customize Manifest.mf
                if (ze.getName().toLowerCase().equals("meta-inf/manifest.mf")) {
                    try {
                        Manifest mf = new Manifest(new ByteArrayInputStream(transformed));
                        Attributes at = mf.getMainAttributes();
                        at.putValue(MF_CUSTOM_DATE, DF.format(new Date()));
                        at.putValue(MF_CUSTOM_PP, preprocessor.getClass().getName());
                        at.putValue(MF_CUSTOM_COMMENT, MF_CUSTOM_COMMENT_VALUE);

                        // re read the updated manifest
                        bos.reset();
                        mf.write(bos);
                        transformed = bos.toByteArray();
                    } catch (Exception emf) {
                        emf.printStackTrace();
                    }
                }

                // update target.jar.aspectwerkzc working file
                ZipEntry transformedZe = new ZipEntry(ze.getName());
                transformedZe.setSize(transformed.length);
                CRC32 crc = new CRC32();
                crc.update(transformed);
                transformedZe.setCrc(crc.getValue());
                transformedZe.setMethod(ze.getMethod());
                zos.putNextEntry(transformedZe);
                zos.write(transformed, 0, transformed.length);
            }
            zip.close();
            zos.close();

            // replace file by workingFile
            File swap = new File(file.getAbsolutePath() + ".swap.aspectwerkzc");
            utility.backupFile(file, swap);
            try {
                utility.backupFile(workingFile, new File(file.getAbsolutePath()));
                workingFile.delete();
                swap.delete();
            } catch (Exception e) {
                // restore swapFile
                utility.backupFile(swap, new File(file.getAbsolutePath()));
                workingFile.delete();
                throw new CompileException("compile " + file.getAbsolutePath() + " failed", e);
            }
        } catch (IOException e) {
            throw new CompileException("compile " + file.getAbsolutePath() + " failed", e);
        } finally {
            try {
                zos.close();
            } catch (Throwable e) {
                ;
            }
            try {
                zip.close();
            } catch (Throwable e) {
                ;
            }
        }
    }

    /**
     * Compile given target.
     *
     * @return false if process should stop
     */
    public boolean compile(File source) {
        sourceIndex++;
        backup(source, sourceIndex);
        try {
            doCompile(source, null);
        } catch (CompileException e) {
            utility.log(" [aspectwerkzc] compilation encountered an error");
            e.printStackTrace();
            return (!haltOnError);
        }

        // compile sucessfull
        successMap.put(source, Boolean.TRUE);
        return true;
    }

    /**
     * Set up the compilation path by building a URLClassLoader with all targets in
     *
     * @param targets      to add to compilationLoader classpath
     * @param parentLoader the parent ClassLoader used by the new one
     */
    public void setCompilationPath(File[] targets, ClassLoader parentLoader) {
        URL[] urls = new URL[targets.length];
        int j = 0;
        for (int i = 0; i < targets.length; i++) {
            try {
                urls[j] = targets[i].getCanonicalFile().toURL();
                j++;
            } catch (IOException e) {
                System.err.println("bad target " + targets[i]);
            }
        }

        compilationLoader = new URLClassLoader(urls, parentLoader);
    }

    /**
     * Test if file is a zip/jar file
     */
    public static boolean isJarFile(File source) {
        return (source.isFile() && (source.getName().toLowerCase().endsWith(".jar") || source
                .getName().toLowerCase().endsWith(".zip")));
    }

    /**
     * Usage message
     */
    public static void doHelp() {
        System.out.println("--- AspectWerkzC compiler ---");
        System.out.println("Usage:");
        System.out
                .println(
                        "java -cp ... org.codehaus.aspectwerkz.compiler.AspectWerkzC [-verbose] [-haltOnError] [-verify]  <target 1> .. <target n>"
                );
        System.out.println("  <target i> : exploded dir, jar, zip files to compile");
    }

    /**
     * Creates and configures an AspectWerkzC compiler.
     *
     * @param params a map containing the compiler parameters
     * @return a new and configured <CODE>AspectWerkzC</CODE>
     */
    private static AspectWerkzC createCompiler(Map params) {
        AspectWerkzC compiler = new AspectWerkzC();

        for (Iterator it = params.entrySet().iterator(); it.hasNext();) {
            Map.Entry param = (Map.Entry) it.next();

            if (COMMAND_LINE_OPTION_VERBOSE.equals(param.getKey())) {
                compiler.setVerbose(Boolean.TRUE.equals(param.getValue()));
            } else if (COMMAND_LINE_OPTION_HALT.equals(param.getKey())) {
                compiler.setHaltOnError(Boolean.TRUE.equals(param.getValue()));
            } else if (COMMAND_LINE_OPTION_VERIFY.equals(param.getKey())) {
                compiler.setVerify(Boolean.TRUE.equals(param.getValue()));
            }
        }

        return compiler;
    }

    /**
     * Runs the AspectWerkzC compiler for the <tt>targets</tt> files.
     *
     * @param compiler     a configured <CODE>AspectWerkzC</CODE>
     * @param classLoader  the class loader to be used
     * @param preProcessor fully qualified name of the preprocessor class.
     *                     If <tt>null</tt> than the default is used
     *                     (<CODE>org.codehaus.aspectwerkz.transform.AspectWerkzPreProcessor</CODE>)
     * @param classpath    list of Files representing the classpath (List<File>)
     * @param targets      the list of target files (List<File>)
     */
    public static void compile(AspectWerkzC compiler,
                               ClassLoader classLoader,
                               String preProcessor,
                               List classpath,
                               List targets) {

        List fullPath = new ArrayList(classpath);

        fullPath.addAll(targets);

        compiler.setCompilationPath((File[]) fullPath.toArray(new File[fullPath.size()]), classLoader);

        Thread.currentThread().setContextClassLoader(compiler.compilationLoader);

        // AOPC special fix
        // turn off -Daspectwerkz.definition.file registration and register it at the
        // compilationLoader level instead
        SystemDefinitionContainer.disableSystemWideDefinition();
        SystemDefinitionContainer.deploySystemDefinitions(
                compiler.compilationLoader,
                DefinitionLoader.getDefaultDefinition(compiler.compilationLoader)
        );

        String preprocessorFqn = preProcessor == null ? PRE_PROCESSOR_CLASSNAME_DEFAULT
                : preProcessor;

        try {
            compiler.setPreprocessor(preprocessorFqn);
        } catch (CompileException e) {
            System.err.println("Cannot instantiate ClassPreProcessor: " + preprocessorFqn);
            e.printStackTrace();
            System.exit(-1);
        }

        cleanBackupDir(compiler);

        for (Iterator i = targets.iterator(); i.hasNext();) {
            if (!compiler.compile((File) i.next())) {
                compiler.postCompile("*** An error occured ***");
                System.exit(-1);
            }
        }
        compiler.postCompile("");
    }

    private static void cleanBackupDir(AspectWerkzC compiler) {
        // prepare backup directory
        try {
            File temp = new File(compiler.backupDir);
            if (temp.exists()) {
                compiler.getUtility().deleteDir(temp);
            }
            temp.mkdir();
            (new File(temp, "" + System.currentTimeMillis() + ".timestamp")).createNewFile();
        } catch (Exception e) {
            System.err.println("failed to prepare backup dir: " + compiler.backupDir);
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static void main(String[] args) {
        if (args.length <= 0) {
            doHelp();
            return; //stop here
        }

        Map options = parseOptions(args);
        AspectWerkzC compiler = createCompiler(options);

        compiler.setBackupDir(BACKUP_DIR);

        compile(
                compiler,
                ClassLoader.getSystemClassLoader(),
                System.getProperty(
                        PRE_PROCESSOR_CLASSNAME_PROPERTY,
                        PRE_PROCESSOR_CLASSNAME_DEFAULT
                ),
                (List) options.get(COMMAND_LINE_OPTION_CLASSPATH),
                (List) options.get(COMMAND_LINE_OPTION_TARGETS)
        );
    }

    private static Map parseOptions(String[] args) {
        Map options = new HashMap();
        List targets = new ArrayList();

        for (int i = 0; i < args.length; i++) {
            if (COMMAND_LINE_OPTION_VERBOSE.equals(args[i])) {
                options.put(COMMAND_LINE_OPTION_VERBOSE, Boolean.TRUE);
            } else if (COMMAND_LINE_OPTION_HALT.equals(args[i])) {
                options.put(COMMAND_LINE_OPTION_HALT, Boolean.TRUE);
            } else if (COMMAND_LINE_OPTION_VERIFY.equals(args[i])) {
                options.put(COMMAND_LINE_OPTION_VERIFY, Boolean.TRUE);
            } else if (COMMAND_LINE_OPTION_CLASSPATH.equals(args[i])) {
                if (i == (args.length - 1)) {
                    continue; //FIXME: this is an error
                } else {
                    options.put(
                            COMMAND_LINE_OPTION_CLASSPATH,
                            toFileArray(args[++i], File.pathSeparator)
                    );
                }
            } else if (args[i].startsWith(COMMAND_LINE_OPTION_DASH)) {
                ; // nothing to be done about it
            } else {
                File file = toFile(args[i]);
                if (file == null) {
                    System.err.println("Ignoring inexistant target: " + args[i]);
                } else {
                    targets.add(file);
                }
            }
        }

        options.put(COMMAND_LINE_OPTION_TARGETS, targets);

        return options;
    }

    private static List toFileArray(String str, String sep) {
        if (str == null || str.length() == 0) {
            return new ArrayList();
        }

        List files = new ArrayList();
        int start = 0;
        int idx = str.indexOf(sep, start);
        int len = sep.length();

        while (idx != -1) {
            files.add(new File(str.substring(start, idx)));
            start = idx + len;
            idx = str.indexOf(sep, start);
        }

        files.add(new File(str.substring(start)));

        return files;
    }

    private static File toFile(String path) {
        File file = new File(path);

        return file.exists() ? file : null;
    }
}