/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.ide.eclipse.core.project;

import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import java.io.*;
import java.net.URL;
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.ui.PlatformUI;

import org.codehaus.aspectwerkz.reflect.impl.asm.*;
import org.codehaus.aspectwerkz.reflect.*;
import org.codehaus.aspectwerkz.transform.*;
import org.codehaus.aspectwerkz.util.Strings;
import org.codehaus.aspectwerkz.hook.*;
import org.codehaus.aspectwerkz.ide.eclipse.core.AwCorePlugin;
import org.codehaus.aspectwerkz.ide.eclipse.core.AwLog;
import org.codehaus.aspectwerkz.annotation.*;
import org.codehaus.aspectwerkz.definition.AspectDefinition;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.definition.SystemDefinitionContainer;


/**
 * @author avasseur
 * 
 * FIXME: when incremental, when an aspect is annC after other class compiled,
 * those will miss some advised def. FIXME: full weave: should annC all the
 * aspect first ==> need split of builders so that annC is run entirely BEFORE
 * awC FIXME: aop.xml change should trigger full weave ??
 * 
 */
public class AwAnnotationBuilder extends IncrementalProjectBuilder {

    public static final String BUILDER_ID = AwAnnotationBuilder.class.getName();

    private static final String MARKER_ID = "FIXME";

    //	      FavoritesPlugin.getDefault().getDescriptor()
    //	         .getUniqueIdentifier() + ".auditmarker";
    //
    public static final String KEY = "key";

    public static final String VIOLATION = "violation";

    protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
            throws CoreException {

        AwLog.logTrace("annotation for "
                + ((kind == FULL_BUILD) ? "full" : "incremental"));

        switch (kind) {
        case FULL_BUILD:
        case CLEAN_BUILD:
            monitor.beginTask("AspectWerkz annotation", 50);
            fullAnnotationC(monitor);
            break;

        case AUTO_BUILD:
        case INCREMENTAL_BUILD:
            if (getDelta(getProject()) == null)
                break;
            monitor.beginTask("AspectWerkz annotation", 1 + 2 * getDelta(
                    getProject()).getAffectedChildren().length);
            incrementalAnnotationC(monitor);
            break;

        default:
            break;
        }

        //	      	 
        //	      	 
        //	         ResourcesPlugin.getWorkspace().run(
        //	            new IWorkspaceRunnable() {
        //	               public void run(IProgressMonitor monitor)
        //	                  throws CoreException
        //	               {
        //	                  weave(monitor);
        //	               }
        //	            },
        //	            monitor
        //	         );
        //	      }
        return null;
    }

    //	   private boolean shouldWeave(int kind) {
    //	      if (kind == FULL_BUILD)
    //	         return true;
    //	      IResourceDelta delta = getDelta(getProject());
    //	      if (delta == null)
    //	         return false;
    //	      IResourceDelta[] children = delta.getAffectedChildren();
    //	      for (int i = 0; i < children.length; i++) {
    //	         IResourceDelta child = children[i];
    //	         String fileName = child.getProjectRelativePath().lastSegment();
    //	         // at least one class
    //	         if (fileName.endsWith(".class"))
    //	            return true;
    //	      }
    //	      return false;
    //	   }

    private void fullAnnotationC(IProgressMonitor monitor) throws CoreException {
        getProject().accept(new AwAnnotationBuilderVisitor(monitor, true));
    }

    private void incrementalAnnotationC(IProgressMonitor monitor)
            throws CoreException {
        getDelta(getProject()).accept(new AwAnnotationBuilderVisitor(monitor, false));
    }

    private class AwAnnotationBuilderVisitor implements IResourceVisitor,
            IResourceDeltaVisitor {
        
        private final boolean m_isFull;

        private IProgressMonitor m_monitor;

        public AwAnnotationBuilderVisitor(IProgressMonitor monitor, boolean isFull) {
            m_monitor = monitor;
            m_isFull = isFull;
        }

        public boolean visit(IResource resource) throws CoreException {
            if (resource.getType() == IResource.FILE) {
                if (resource.getFileExtension().equals("class")) {
                    m_monitor.subTask(resource.getName());

                    annotate(resource, m_monitor, m_isFull);

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
                        annotate(resource, m_monitor, m_isFull);
                        break;

                    default:
                        break;
                    }

                    m_monitor.worked(1);
                }
            }
            return true;
        }

    }

    private void annotate(IResource resource, IProgressMonitor monitor, boolean isFull) {
        try {
            // skip JITjp
            if (AwCorePlugin.isJoinPointClass(resource.getName())) {
                return;
            }
            
            AwLog.logInfo("annotate " + resource.getName());
            IJavaProject jproject = JavaCore.create(getProject());
            ClassLoader pcl = AwCorePlugin.getDefault().getProjectClassLoader(
                    jproject);
            
            File file = resource.getRawLocation().toFile();
            byte[] classBytes = AwCorePlugin.getDefault().readClassFile(file);

            if (checkCancel(monitor))
                return;

            // gets the class name from ASM Info
            ClassInfo classInfo = AsmClassInfo.getClassInfo(classBytes, pcl);
            String className = classInfo.getName();
            AwLog.logTrace("got name from bytes " + className);
            
            // skip inner class since they will be annnotated when their outer is annotated
            if (className.indexOf('$')>0) {
                return;
            }
            
            // check if we have an aspect
            boolean isAspect = false;
            Set defs = SystemDefinitionContainer.getDefinitionsFor(pcl);
            for (Iterator it = defs.iterator(); it.hasNext();) {
                SystemDefinition def = (SystemDefinition)it.next();
                Collection aspectDefs = def.getAspectDefinitions();
                for (Iterator at = aspectDefs.iterator(); at.hasNext();) {
                    AspectDefinition aspectDef = (AspectDefinition)at.next();
                    if (aspectDef.getClassName().equals(className)) {
                        isAspect = true;
                        break;
                    }
                }
                if (isAspect) {
                    break;
                }
            }
            if (isAspect) {
                AwLog.logInfo("Detected a change in aspect " + className);
                IProject project = resource.getProject();
                project.getProject().build(IncrementalProjectBuilder.FULL_BUILD,
                        monitor);                
//                getProject().getProject().build(AwAnnotationBuilder.FULL_BUILD,
//                        AwAnnotationBuilder.BUILDER_ID, null, monitor);
//                getProject().getProject().build(AwProjectBuilder.FULL_BUILD,
//                        AwProjectBuilder.BUILDER_ID, null, monitor);
//                AwLog.logInfo("DONE Detected a change in aspect " + className);
//                AwLog.logInfo("full .? " + isFull );
                //return;
            }

            // AnnotationC
            // lookup of annotation.properties
            Enumeration annotationProps = pcl.getResources("annotation.properties");
            List annotationPropsFilesList = new ArrayList();
            while (annotationProps.hasMoreElements()) {
                String annFile = ((URL)annotationProps.nextElement()).getFile().toString();
                annotationPropsFilesList.add(annFile);
                AwLog.logInfo("using custom annotations " + annFile);
            }
            String[] annotationPropsFile = (String[])annotationPropsFilesList.toArray(new String[]{});
            
            // extract the file path
            int segments = Strings.splitString(className, ".").length;
            // = 2 for pack.Hello
            IPath pathToFile = resource.getRawLocation().removeLastSegments(segments);
            String destDir = pathToFile.toFile().toString();
            AwLog.logTrace("will annotate to " + destDir);

            // call AnnotationC for only one file, dest dir = src dir
            boolean verbose = true;
            // classpath for custom annotations etc
            List pathURLs = AwCorePlugin.getDefault().getProjectClassPathURLs(
                    jproject);
            String[] pathFiles = new String[pathURLs.size()];
            int i = 0;
            for (Iterator urls = pathURLs.iterator(); urls.hasNext(); i++) {
                pathFiles[i] = ((URL) urls.next()).getFile().toString();
            }

            // source file for this resource
            IResource sourceFile = AwCorePlugin.getDefault().findSourceForResource(getProject(), resource, className);
            if (checkCancel(monitor))
                return;

            if (sourceFile == null) {
                AwLog.logInfo("cannot find source for compiled resource "
                        + resource.getRawLocation().toString());
            } else {
                String targetFile = sourceFile.getRawLocation().toFile()
                        .toString();
                AnnotationC.compile(verbose, new String[0],
                        new String[] { targetFile }, pathFiles, destDir,
                        annotationPropsFile.length==0?null:annotationPropsFile);
                AwLog.logTrace("annotated " + className + " from " + targetFile);
            }
            if (checkCancel(monitor))
                return;

            monitor.worked(1);

            // write back
            // -> has been done by AnnotationC
        } catch (Throwable e) {
            AwLog.logError(e);
        }
    }

    //	      if (!deleteAuditMarkers(getProject()))
    //	         return;
    //	      
    //	      if (checkCancel(monitor))
    //	         return;
    //
    //	      Map pluginKeys = scanPlugin(getProject().getFile("plugin.xml"));
    //	      monitor.worked(1);
    //	      
    //	      if (checkCancel(monitor))
    //	         return;
    //	      Map propertyKeys = scanProperties(
    //	         getProject().getFile("plugin.properties"));
    //	      monitor.worked(1);
    //	      
    //	      if (checkCancel(monitor))
    //	         return;
    //	      Iterator iter = pluginKeys.entrySet().iterator();
    //	      while (iter.hasNext()) {
    //	         Map.Entry entry = (Map.Entry) iter.next();
    //	         if (!propertyKeys.containsKey(entry.getKey()))
    //	            reportProblem(
    //	               "Missing property key",
    //	               ((Location) entry.getValue()),
    //	               1,
    //	               true);
    //	      }
    //	      monitor.worked(1);
    //	      
    //	      if (checkCancel(monitor))
    //	         return;
    //
    //	      iter = propertyKeys.entrySet().iterator();
    //	      while (iter.hasNext()) {
    //	         Map.Entry entry = (Map.Entry) iter.next();
    //	         if (!pluginKeys.containsKey(entry.getKey()))
    //	            reportProblem(
    //	               "Unused property key",
    //	               ((Location) entry.getValue()),
    //	               2,
    //	               false);
    //	      }
    //	      
    //	   }

    public static boolean deleteAuditMarkers(IProject project) {
        try {
            project.deleteMarkers(MARKER_ID, false, IResource.DEPTH_INFINITE);
            return true;
        } catch (CoreException e) {
            AwLog.logError(e);
            return false;
        }
    }

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

    private Map scanPlugin(IFile file) {
        Map keys = new HashMap();
        String content = readFile(file);
        int start = 0;
        while (true) {
            start = content.indexOf("\"%", start);
            if (start < 0)
                break;
            int end = content.indexOf('"', start + 2);
            if (end < 0)
                break;
            Location loc = new Location();
            loc.file = file;
            loc.key = content.substring(start + 2, end);
            loc.charStart = start + 1;
            loc.charEnd = end;
            keys.put(loc.key, loc);
            start = end + 1;
        }
        return keys;
    }

    private Map scanProperties(IFile file) {
        Map keys = new HashMap();
        String content = readFile(file);
        int end = 0;
        while (true) {
            end = content.indexOf('=', end);
            if (end < 0)
                break;
            int start = end - 1;
            while (start >= 0) {
                char ch = content.charAt(start);
                if (ch == '\r' || ch == '\n')
                    break;
                start--;
            }
            start++;
            String found = content.substring(start, end).trim();
            if (found.length() == 0 || found.charAt(0) == '#'
                    || found.indexOf('=') != -1)
                continue;
            Location loc = new Location();
            loc.file = file;
            loc.key = found;
            loc.charStart = start;
            loc.charEnd = end;
            keys.put(loc.key, loc);
            end++;
        }
        return keys;
    }

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

    private void reportProblem(String msg, Location loc, int violation,
            boolean isError) {

        try {
            IMarker marker = loc.file.createMarker(MARKER_ID);
            marker.setAttribute(IMarker.MESSAGE, msg + ": " + loc.key);
            marker.setAttribute(IMarker.CHAR_START, loc.charStart);
            marker.setAttribute(IMarker.CHAR_END, loc.charEnd);
            marker
                    .setAttribute(IMarker.SEVERITY,
                            isError ? IMarker.SEVERITY_ERROR
                                    : IMarker.SEVERITY_WARNING);
            marker.setAttribute(KEY, loc.key);
            marker.setAttribute(VIOLATION, violation);
        } catch (CoreException e) {
            AwLog.logError(e);
            return;
        }
    }

    private class Location {
        IFile file;

        String key;

        int charStart;

        int charEnd;
    }

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