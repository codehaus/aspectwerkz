/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.ide.eclipse.core.project;

import org.codehaus.aspectwerkz.ide.eclipse.core.AwCorePlugin;
import org.codehaus.aspectwerkz.ide.eclipse.core.AwLog;
import org.codehaus.aspectwerkz.ide.eclipse.ui.WeaverListener;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;


/**
 * @author avasseur
 * 
 */
public class AwProjectNature implements IProjectNature {

    public final static String NATURE_ID = AwProjectNature.class.getName();

    private IProject project;

    public IProject getProject() {
        return project.getProject();
    }

    public void setProject(IProject project) {
        AwLog.logInfo("nature.setProject " + project.getName());
        this.project = project;
    }

    public void configure() throws CoreException {
//        AwCorePlugin.getDefault().registerWeaverListener(
//                new WeaverListener());
        
        AwLog.logInfo("nature configure");        
        AwCorePlugin.getDefault().addBuilderToProject(project,
                AwAnnotationBuilder.BUILDER_ID);
        AwCorePlugin.getDefault().addBuilderToProject(project,
                AwProjectBuilder.BUILDER_ID);
        new Job("AspectWerkz Nature") {
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    project.getProject().build(IncrementalProjectBuilder.FULL_BUILD,
                            monitor);
//                    project.getProject().build(AwAnnotationBuilder.FULL_BUILD,
//                            AwAnnotationBuilder.BUILDER_ID, null, monitor);
//                    project.getProject().build(AwProjectBuilder.FULL_BUILD,
//                            AwProjectBuilder.BUILDER_ID, null, monitor);
//                    project.getProject().build(AwAnnotationBuilder.FULL_BUILD,
//                            AwAnnotationBuilder.BUILDER_ID, null, monitor);
//                    project.getProject().build(AwProjectBuilder.FULL_BUILD,
//                            AwProjectBuilder.BUILDER_ID, null, monitor);
                } catch (CoreException e) {
                    AwLog.logError(e);
                }
                return Status.OK_STATUS;
            }
        }.schedule();
    }

    public void deconfigure() throws CoreException {
        AwCorePlugin.getDefault().removeBuilderFromProject(project,
                AwProjectBuilder.BUILDER_ID);
        AwCorePlugin.getDefault().removeBuilderFromProject(project,
                AwAnnotationBuilder.BUILDER_ID);
        WeaverListener.deleteMarkers(project);
    }

}