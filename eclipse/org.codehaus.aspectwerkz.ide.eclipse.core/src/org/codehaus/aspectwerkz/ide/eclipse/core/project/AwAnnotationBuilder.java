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
        AwAnnotationBuilderVisitor awAnnCVisitor = new AwAnnotationBuilderVisitor(monitor, false);
        getDelta(getProject()).accept(awAnnCVisitor);
        if (awAnnCVisitor.m_hasEncounteredAspects) {
            // trigger a full weaving
            AwLog.logInfo("TODO Rebuilding project - aspects have changed..");
            // Note: we have to reweave the aspect themselves since they may affect each another
            //TODO filter out already annC classes ie getProject\getDelta, else
            // we have the annotation twices and it will confuse the runtime...
            //getProject().build(FULL_BUILD, JavaCore.BUILDER_ID, null, monitor);
            //getProject().accept(awAnnCVisitor);
            //getProject().accept(new AwProjectBuilder.AwProjectBuilderVisitor(monitor, getProject(), false));
        }
    }

    class AwAnnotationBuilderVisitor implements IResourceVisitor,
            IResourceDeltaVisitor {
        
        private IProgressMonitor m_monitor;

        private final boolean m_isFull;
        
        private ClassLoader m_projectClassLoader;
        
        private String[] m_pathFiles;
        
        private IJavaProject m_jproject;
        
        private Set m_definitions;
        
        private String[] m_annotationPropsFiles;
        
        private boolean m_hasEncounteredAspects = false;

        public AwAnnotationBuilderVisitor(IProgressMonitor monitor, boolean isFull) {
            m_monitor = monitor;
            m_isFull = isFull;
            
            // get the project classloader
            m_jproject = JavaCore.create(getProject());
            m_projectClassLoader = AwCorePlugin.getDefault().getProjectClassLoader(m_jproject);
            
            // get the annotation.properties files
            try  {
	            Enumeration annotationProps = m_projectClassLoader.getResources("annotation.properties");
	            List annotationPropsFilesList = new ArrayList();
	            while (annotationProps.hasMoreElements()) {
	                String annFile = ((URL)annotationProps.nextElement()).getFile().toString();
	                annotationPropsFilesList.add(annFile);
	                AwLog.logInfo("using custom annotations " + annFile);
	            }
	            m_annotationPropsFiles = (String[])annotationPropsFilesList.toArray(new String[]{});
            } catch (Throwable t) {
                AwLog.logError("cannot access annotation.properties file(s)", t);
                m_annotationPropsFiles = new String[0];
            }
            
            // build the classpath we will use to run AnnotationC so that it find 
            // custom annotations etc
            List pathURLs = AwCorePlugin.getDefault().getProjectClassPathURLs(m_jproject);
            m_pathFiles = new String[pathURLs.size()];
            int i = 0;
            for (Iterator urls = pathURLs.iterator(); urls.hasNext(); i++) {
                m_pathFiles[i] = ((URL) urls.next()).getFile().toString();
            }

            // get the definitions to know when we are rebuilding an aspect
            ClassLoader currentCL = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(m_projectClassLoader);
                m_definitions = SystemDefinitionContainer.getDefinitionsFor(m_projectClassLoader);
            } finally {
                Thread.currentThread().setContextClassLoader(currentCL);
            }
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

        private boolean isAspect(String className) {
            boolean isAspect = false;
            for (Iterator it = m_definitions.iterator(); it.hasNext();) {
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
            return isAspect;
        }
        
        private void annotate(IResource resource, IProgressMonitor monitor, boolean isFull) {
            // change the Thread classloader since we are going to access the AOP defs.
            ClassLoader currentCL = Thread.currentThread().getContextClassLoader();
            try {
                // skip JITjp
                if (AwCorePlugin.isJoinPointClass(resource.getName())) {
                    return;
                }
                AwLog.logInfo("annotate " + resource.getName());

                
                Thread.currentThread().setContextClassLoader(m_projectClassLoader);

                // gets the class name from ASM Info
                File file = resource.getRawLocation().toFile();
                byte[] classBytes = AwCorePlugin.getDefault().readClassFile(file);
                ClassInfo classInfo = AsmClassInfo.getClassInfo(classBytes, m_projectClassLoader);
                String className = classInfo.getName();
                AwLog.logTrace("got name from bytes " + className);
                
                // skip inner class since they will be annnotated when their outer is annotated
                // TODO  handle aspect as inner class change triggering
                if (className.indexOf('$')>0) {
                    return;
                }
                
                // check if we have at least one aspect in the delta when we are not doing a full build
                if (!m_isFull && !m_hasEncounteredAspects) {
                    boolean isAspect = isAspect(className);
                    if (isAspect) {
                        AwLog.logInfo("Detected a change in aspect " + className);
                        m_hasEncounteredAspects = true;
                    }
                }
//                    IProject project = resource.getProject();
                    //TODO how to trigger a nested build / clear state etc ?
//                    project.getProject().build(IncrementalProjectBuilder.FULL_BUILD,monitor);                
//                    getProject().getProject().build(AwAnnotationBuilder.FULL_BUILD,
//                            AwAnnotationBuilder.BUILDER_ID, null, monitor);
//                    getProject().getProject().build(AwProjectBuilder.FULL_BUILD,
//                            AwProjectBuilder.BUILDER_ID, null, monitor);
//                    AwLog.logInfo("DONE Detected a change in aspect " + className);
//                    AwLog.logInfo("full .? " + isFull );
//                    //return;

                // extract the file path
                int segments = Strings.splitString(className, ".").length;
                // = 2 for pack.Hello
                IPath pathToFile = resource.getRawLocation().removeLastSegments(segments);
                String destDir = pathToFile.toFile().toString();
                AwLog.logTrace("will annotate to " + destDir);

                // call AnnotationC for only one file, dest dir = src dir
                boolean verbose = true;

                // source file for this resource
                IResource sourceFile = AwCorePlugin.getDefault().findSourceForResource(getProject(), resource, className);
                if (checkCancel(monitor))
                    return;

                if (sourceFile == null) {
                    AwLog.logInfo("cannot find source for compiled resource "
                            + resource.getRawLocation().toString());
                } else {
    	                String targetFile = sourceFile.getRawLocation().toFile().toString();
    	                AnnotationC.compile(verbose, new String[0],
    	                        new String[] { targetFile }, m_pathFiles, destDir,
    	                        m_annotationPropsFiles.length==0?null:m_annotationPropsFiles);
    	                AwLog.logTrace("annotated " + className + " from " + targetFile);
                }
                if (checkCancel(monitor))
                    return;

                monitor.worked(1);

                // write back
                // -> has been done by AnnotationC
            } catch (Throwable e) {
                AwLog.logError(e);
    	    } finally {
    	        Thread.currentThread().setContextClassLoader(currentCL);
    	    }
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

}