/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.ide.eclipse.core;


import org.codehaus.aspectwerkz.transform.inlining.EmittedJoinPoint;
import org.codehaus.aspectwerkz.util.Strings;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.ui.plugin.*;
import org.osgi.framework.BundleContext;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Main s_plugin class
 * 
 * @author avasseur
 */
public class AwCorePlugin extends AbstractUIPlugin {

    private static AwCorePlugin s_plugin;

    private ResourceBundle m_resourceBundle;
    
    private List/*IWeaverListener*/ m_weaverListeners = new ArrayList();


    public AwCorePlugin() {
        super();
        s_plugin = this;
        try {
            m_resourceBundle = ResourceBundle
                    .getBundle("org.codehaus.aspectwerkz.ide.eclipse.core.CorePluginResources");
        } catch (MissingResourceException x) {
            m_resourceBundle = null;
        }
    }

    public void start(BundleContext context) throws Exception {
        super.start(context);
    }

    public void stop(BundleContext context) throws Exception {
        super.stop(context);
    }

    public static AwCorePlugin getDefault() {
        return s_plugin;
    }
    
    public void registerWeaverListener(IWeaverListener listener) {
        m_weaverListeners.add(listener);
    }
    
    public void notifyWeaverListener(IJavaProject jproject, String className, ClassLoader loader,
            			 			 EmittedJoinPoint[] emittedJoinPoint) {
        for (Iterator it = m_weaverListeners.iterator(); it.hasNext();) {
            ((IWeaverListener)it.next()).onWeaved(jproject, className, loader, emittedJoinPoint);
        }
    }
    
    public static String getResourceString(String key) {
        ResourceBundle bundle = AwCorePlugin.getDefault().getResourceBundle();
        try {
            return (bundle != null) ? bundle.getString(key) : key;
        } catch (MissingResourceException e) {
            return key;
        }
    }

    public ResourceBundle getResourceBundle() {
        return m_resourceBundle;
    }

    public URLClassLoader getProjectClassLoader(IJavaProject project) {
        List paths = getProjectClassPathURLs(project);
        URL pathUrls[] = (URL[]) paths.toArray(new URL[0]);
        return new URLClassLoader(pathUrls, Thread.currentThread().getContextClassLoader());
    }

    public List getProjectClassPathURLs(IJavaProject project) {
        List paths = new ArrayList();
        try {
            // configured classpath
            IClasspathEntry classpath[] = project.getRawClasspath();
            for (int i = 0; i < classpath.length; i++) {
                IClasspathEntry path = classpath[i];
                if (path.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
                    URL url = path.getPath().toFile().toURL();
                    paths.add(url);
                }
            }
            // build output, relative to project
            IPath location = getProjectLocation(project.getProject());
            IPath outputPath = location.append(project.getOutputLocation().removeFirstSegments(1));
            paths.add(outputPath.toFile().toURL());
        } catch (Exception e) {
            AwLog.logError("Could not build project path", e);
        }
        return paths;
    }
    
    public IPath getProjectLocation(IProject project) {
        if (project.getRawLocation() == null) {
            return project.getLocation();
        } else {
            return project.getRawLocation();
        }
    }

    public byte[] readClassFile(File file) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        InputStream in = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        while (in.available() > 0) {
            int length = in.read(buffer);
            if (length == -1) {
                break;
            }
            bos.write(buffer, 0, length);
        }
        in.close();
        bos.close();
        return bos.toByteArray();
    }

    public void addBuilderToProject(IProject project, String builderId) {
        AwLog.logInfo("add builder " + builderId + " to " + project.getName());
        try {
            IProjectDescription desc = project.getDescription();
            ICommand[] commands = desc.getBuildSpec();

            //add builders to project
            ICommand builderCommand = desc.newCommand();
            builderCommand.setBuilderName(builderId);

            ICommand[] newCommands = new ICommand[commands.length + 1];
            System.arraycopy(commands, 0, newCommands, 0, commands.length);
            newCommands[newCommands.length - 1] = builderCommand;
            desc.setBuildSpec(newCommands);
            project.setDescription(desc, null);
        } catch (CoreException e) {
            AwLog.logError("Could not add builder " + builderId, e);
        }
    }

    public void removeBuilderFromProject(IProject project, String builderId) {
        AwLog.logInfo("remove builder " + builderId + " to " + project.getName());
        try {
            IProjectDescription description = project.getDescription();

            // look for builder
	        int index = -1;
	        ICommand[] cmds = description.getBuildSpec();
	        for (int j = 0; j < cmds.length; j++) {
	            if (cmds[j].getBuilderName().equals(builderId)) {
	                index = j;
	                break;
	            }
	        }
	        // not found
	        if (index == -1)
	            return;

	        // remove builder from project
	        List newCmds = new ArrayList();
	        newCmds.addAll(Arrays.asList(cmds));
	        newCmds.remove(index);
	        description.setBuildSpec((ICommand[]) newCmds.toArray(new ICommand[0]));
	        project.setDescription(description, null);
        } catch (CoreException e) {
            AwLog.logError("could not remove builder " + builderId, e);
        }
    }

    public IResource findSourceForResource(IProject project, IResource resource, final String className) throws CoreException {
        // source file for this resource
        //TODO can we use resource (.class file)
        return MyIResourceProxyVisitor.resourceOf(project, className);
    }

    private static class MyIResourceProxyVisitor implements IResourceProxyVisitor {
        /**
         * the class name we search the source file of
         */
        private String m_className;
        
        /**
         * The outer class name for inner class or itself.
         */
        private String m_outerClassName;
        
        /**
         * derived from m_className, contains name without package info and suffixed with .java,
         * with support for Anonymous class ??
         */
        private String m_sourceSuffix;

        /**
         * the found source file
         */
        private IResource m_source;
        
        /**
         * helper method to find the source file of className within the project
         * 
         * @param project
         * @param className
         * @return - can be null if not found
         * @throws CoreException
         */
        public static IResource resourceOf(IProject project, String className) throws CoreException {
            MyIResourceProxyVisitor v = new MyIResourceProxyVisitor(className);
            project.accept(v, IResource.FILE);
            return v.getResource();
        }
        
        private MyIResourceProxyVisitor(String className) {
            m_className = className;
            if (className.indexOf('$') > 0) {
                // inner class
                m_outerClassName = className.substring(0, className.lastIndexOf('$'));
            } else {
                m_outerClassName = m_className;
            }
            String[] classNameParts = Strings.splitString(m_outerClassName, ".");
	        m_sourceSuffix = classNameParts[classNameParts.length-1] + ".java";
        }
        
        public IResource getResource() {
            return m_source;
        }

        public boolean visit(IResourceProxy resourceProxy) {
            // already found ?
            if (m_source != null) {
                return false;
            }
            // must end with the suffix
            if (!resourceProxy.getName().endsWith(m_sourceSuffix)) {
                return true;
            }
            // must match the outerClassName
            if (resourceProxy.requestFullPath().toString().endsWith(
                    m_outerClassName.replace('.', '/') + ".java")) {
                m_source = resourceProxy.requestResource();
                return false;
            }
            return true;
        }
    }
    
}