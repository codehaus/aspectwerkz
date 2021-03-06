/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.ide.eclipse.core.project;

import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import java.io.*;
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import org.codehaus.aspectwerkz.reflect.impl.asm.*;
import org.codehaus.aspectwerkz.reflect.*;
import org.codehaus.aspectwerkz.transform.*;
import org.codehaus.aspectwerkz.ide.eclipse.core.AwCorePlugin;
import org.codehaus.aspectwerkz.ide.eclipse.core.AwLog;


/**
 * @author avasseur
 * 
 * FIXME: when incremental, when an aspect is annC after other class compiled,
 * those will miss some advised def. FIXME: full weave: should annC all the
 * aspect first ==> need split of builders so that annC is run entirely BEFORE
 * awC FIXME: aop.xml change should trigger full weave ??
 * 
 */
public class AwProjectBuilder extends IncrementalProjectBuilder {

    public static final String BUILDER_ID = AwProjectBuilder.class.getName();

    protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
            throws CoreException {

        AwLog.logTrace("weaving for "
                + ((kind == FULL_BUILD) ? "full" : "incremental"));

        switch (kind) {
        case FULL_BUILD:
        case CLEAN_BUILD:
            monitor.beginTask("AspectWerkz weaving", 50);
            fullWeave(monitor);
            break;

        case AUTO_BUILD:
        case INCREMENTAL_BUILD:
            if (getDelta(getProject()) == null)
                break;
            monitor.beginTask("AspectWerkz weaving", 1 + 2 * getDelta(
                    getProject()).getAffectedChildren().length);
            incrementalWeave(monitor);
            break;

        default:
            break;
        }

        return null;
    }

    private void fullWeave(IProgressMonitor monitor) throws CoreException {
        getProject().accept(new AwProjectBuilderVisitor(monitor, getProject(), false));
    }

    private void incrementalWeave(IProgressMonitor monitor)
            throws CoreException {
        getDelta(getProject()).accept(new AwProjectBuilderVisitor(monitor, getProject(), false));
    }

    static class AwProjectBuilderVisitor implements IResourceVisitor,
            IResourceDeltaVisitor {

        private IProgressMonitor m_monitor;
        
        private ClassLoader m_projectClassLoader;
        
        private IJavaProject m_jproject;
        
        private boolean m_isTriggered;

        public AwProjectBuilderVisitor(IProgressMonitor monitor, IProject project, boolean isTriggered) { 
            m_monitor = monitor;
        	m_isTriggered = isTriggered;

            // build the project classloader for this build phase
            // Note: AW cache will be flushed between each build phase
            m_jproject = JavaCore.create(project);
            m_projectClassLoader = AwCorePlugin.getDefault().getProjectClassLoader(m_jproject);
        }

        public boolean visit(IResource resource) throws CoreException {
            if (resource.getType() == IResource.FILE) {
                if (resource.getFileExtension().equals("class")) {
                    m_monitor.subTask(resource.getName());

                    weave(resource, m_monitor);

                    m_monitor.worked(1);
                }
            }
            return true;
        }

        public boolean visit(IResourceDelta delta) throws CoreException {
            IResource resource = delta.getResource();

            if (resource.getType() == IResource.FILE) {
                if (resource.getFileExtension().equals("class")) {
                    m_monitor.subTask(resource.getName());

                    switch (delta.getKind()) {
                    case IResourceDelta.ADDED:
                    case IResourceDelta.CHANGED:
                    case IResourceDelta.CONTENT:
                        weave(resource, m_monitor);
                        break;

                    default:
                        break;
                    }

                    m_monitor.worked(1);
                }
            }
            return true;
        }

        private void weave(IResource resource, IProgressMonitor monitor) {
            try {
                // skip JITjp
                if (AwCorePlugin.isJoinPointClass(resource.getName())) {
                    return;
                }
                
                AwLog.logInfo("weaving " + resource.getName());
                
                File file = resource.getRawLocation().toFile();
                byte[] classBytes = AwCorePlugin.getDefault().readClassFile(file);

//                if (checkCancel(monitor))
//                    return;

                // gets the class name from ASM Info
                // NOTE: this should ensure that the cache is flushed...
                ClassInfo classInfo = AsmClassInfo.getClassInfo(classBytes, m_projectClassLoader);
                String className = classInfo.getName();

                System.setProperty("aspectwerkz.transform.verbose", "true");
                System.setProperty("aspectwerkz.transform.details", "true");
                AspectWerkzPreProcessor pp = new AspectWerkzPreProcessor();
                pp.initialize();
                AwLog.logTrace("weaving - " + className + " in " + m_projectClassLoader.toString());

                // change the Thread CL to do the weaving
                AspectWerkzPreProcessor.Output weaved = null;
                ClassLoader currentCL = Thread.currentThread().getContextClassLoader();
                try {
                     Thread.currentThread().setContextClassLoader(m_projectClassLoader);
                     weaved = pp.preProcessWithOutput(className, classBytes, m_projectClassLoader);
                } finally {
                    Thread.currentThread().setContextClassLoader(currentCL);
                }
                
                FileOutputStream os = new FileOutputStream(file);
                os.write(weaved.bytecode);
                os.close();

                monitor.worked(1);

                AwLog.logTrace("weaved " + className);
                
                // notify the listeners
                AwCorePlugin.getDefault().notifyWeaverListener(m_jproject, className, m_projectClassLoader, weaved.emittedJoinPoints, m_isTriggered);
            } catch (Throwable e) {
                AwLog.logError(e);
            }
        }
    }



//    public static boolean deleteAuditMarkers(IProject project) {
//        try {
//            project.deleteMarkers(MARKER_ID, false, IResource.DEPTH_INFINITE);
//            return true;
//        } catch (CoreException e) {
//            AwLog.logError(e);
//            return false;
//        }
//    }

    private boolean checkCancel(IProgressMonitor monitor) {
        if (monitor.isCanceled()) {
            // discard build state if necessary
            throw new OperationCanceledException();
        }
        if (isInterrupted()) {
            // discard build state if necessary
            return true;
        }
        return false;
    }

//    private Map scanPlugin(IFile file) {
//        Map keys = new HashMap();
//        String content = readFile(file);
//        int start = 0;
//        while (true) {
//            start = content.indexOf("\"%", start);
//            if (start < 0)
//                break;
//            int end = content.indexOf('"', start + 2);
//            if (end < 0)
//                break;
//            Location loc = new Location();
//            loc.file = file;
//            loc.key = content.substring(start + 2, end);
//            loc.charStart = start + 1;
//            loc.charEnd = end;
//            keys.put(loc.key, loc);
//            start = end + 1;
//        }
//        return keys;
//    }
//
//    private Map scanProperties(IFile file) {
//        Map keys = new HashMap();
//        String content = readFile(file);
//        int end = 0;
//        while (true) {
//            end = content.indexOf('=', end);
//            if (end < 0)
//                break;
//            int start = end - 1;
//            while (start >= 0) {
//                char ch = content.charAt(start);
//                if (ch == '\r' || ch == '\n')
//                    break;
//                start--;
//            }
//            start++;
//            String found = content.substring(start, end).trim();
//            if (found.length() == 0 || found.charAt(0) == '#'
//                    || found.indexOf('=') != -1)
//                continue;
//            Location loc = new Location();
//            loc.file = file;
//            loc.key = found;
//            loc.charStart = start;
//            loc.charEnd = end;
//            keys.put(loc.key, loc);
//            end++;
//        }
//        return keys;
//    }

    private String readFile(IFile file) {
        if (!file.exists())
            return "";
        InputStream stream = null;
        try {
            stream = file.getContents();
            Reader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuffer result = new StringBuffer(2048);
            char[] buf = new char[2048];
            while (true) {
                int count = reader.read(buf);
                if (count < 0)
                    break;
                result.append(buf, 0, count);
            }
            return result.toString();
        } catch (Exception e) {
            AwLog.logError(e);
            return "";
        } finally {
            try {
                if (stream != null)
                    stream.close();
            } catch (IOException e) {
                AwLog.logError(e);
                return "";
            }
        }
    }

//    private void reportProblem(String msg, Location loc, int violation,
//            boolean isError) {
//
//        try {
//            IMarker marker = loc.file.createMarker(MARKER_ID);
//            marker.setAttribute(IMarker.MESSAGE, msg + ": " + loc.key);
//            marker.setAttribute(IMarker.CHAR_START, loc.charStart);
//            marker.setAttribute(IMarker.CHAR_END, loc.charEnd);
//            marker
//                    .setAttribute(IMarker.SEVERITY,
//                            isError ? IMarker.SEVERITY_ERROR
//                                    : IMarker.SEVERITY_WARNING);
//            marker.setAttribute(KEY, loc.key);
//            marker.setAttribute(VIOLATION, violation);
//        } catch (CoreException e) {
//            AwLog.logError(e);
//            return;
//        }
//    }

//    private class Location {
//        IFile file;
//
//        String key;
//
//        int charStart;
//
//        int charEnd;
//    }

    //	   /**
    //	    * Add this builder to all open projects.
    //	    *
    //	    * For demonstration purposes only.
    //	    * The preferred approach for associating a builder
    //	    * with a project is to use a nature.
    //	    *
    //	    * If you choose this approach, be sure that the
    //	    * hasNature attribute for the builder is "false",
    //	    * otherwise you see a warning in the log stating that
    //	    * your builder was skipped because its nature is
    //	    * missing.
    //	    */
    //	   public static void addBuilderToAllProjects() {
    //	      IProject[] projects = ResourcesPlugin.getWorkspace()
    //	         .getRoot().getProjects();
    //	      for (int i = 0; i < projects.length; i++)
    //	         addBuilderToProject(projects[i]);
    //	   }


}