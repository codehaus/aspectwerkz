/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.ide.eclipse.core.launch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.aspectwerkz.ide.eclipse.core.AwLog;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.*;
import org.eclipse.jdt.internal.launching.JavaLocalApplicationLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.*;


/**
 * @author avasseur
 * 
 */
public class LaunchConfigurationDelegate extends
        JavaLocalApplicationLaunchConfigurationDelegate implements
        ILaunchConfigurationDelegate {

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.eclipse.debug.core.ILaunchConfiguration,
     *      java.lang.String, org.eclipse.debug.core.ILaunch,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public void launch(ILaunchConfiguration configuration, String mode,
            ILaunch launch, IProgressMonitor monitor) throws CoreException {
        // FIXME
        // move this logic in the Tab with a JRE Tab . listener
        // to have ONE AW-env.jar Plug per JRE
        // http://help.eclipse.org/help30/index.jsp?topic=/org.eclipse.jdt.doc.isv/reference/api/org/eclipse/jdt/debug/ui/launchConfigurations/JavaJRETab.html
        //
        // - well not sure - can be some sort of specific tab if someone wants
        // to hack SWT
        // where then you can turn on / off global LTW.

        IVMInstall vm = getVMInstall(configuration);

        // prepare Plug for LTW
        File file = null;
        String tempDir = null;
        try {
            file = File.createTempFile("aspectwerkz", null);
            tempDir = file.getParentFile().getCanonicalPath();
        } catch (IOException e) {
            throw new CoreException(AwLog.createStatus(1, 1,
                    "cannot access temp dir", e));
        }

        String jarKey = tempDir + File.separator + ".aspectwerkz-env-"
                + vm.getName().replace(' ', '_') + ".jar";

        // if not in launch configuration bcl/p, add it, else if not the same,
        // change it
        String[][] bootclasspath = getBootpathExt(configuration);
        String[] bootclasspathPrepend = bootclasspath[0];
        if (bootclasspathPrepend == null)
            bootclasspathPrepend = new String[0];
        List newBootclasspathPrepend = new ArrayList();
        newBootclasspathPrepend.add(jarKey);
        for (int i = 0; i < bootclasspathPrepend.length; i++) {
            String prependEntry = bootclasspathPrepend[i];
            if (prependEntry.equals(jarKey)) {
                // will keep it
                ;
            } else if (prependEntry.indexOf(".aspectwerkz-env-") >= 0) {
                // other VM version - dump it
                ;
            } else {
                // keep user entries
                newBootclasspathPrepend.add(prependEntry);
            }
        }

        // FIXME do not generate it every time
        // we are using Plug.main to go thru the launch configured VM...
        // FIXME: needs some work in AW to get rid of tools.jar dependancy in
        // Plug
        IVMRunner vmRunner = vm.getVMRunner(ILaunchManager.RUN_MODE);
        String[] classPath = JavaRuntime
                .computeDefaultRuntimeClassPath(getJavaProject(configuration));
        VMRunnerConfiguration vmConfig = new VMRunnerConfiguration(
                "org.codehaus.aspectwerkz.hook.Plug", classPath);
        vmConfig.setProgramArguments(new String[] { "-target", jarKey });
        ILaunch prepareLaunch = new Launch(null, ILaunchManager.RUN_MODE, null);
        vmRunner.run(vmConfig, prepareLaunch, null);

        // now launch user configuration including -Xbootclasspath/p
        // FIXME: support for JRockit : use EnvironmentDetect in AW to check
        // that ?
        ILaunchConfigurationWorkingCopy copy = configuration
                .copy("AspectWerkz - prepared load time weaving - debug");
        copy.setAttribute(
                IJavaLaunchConfigurationConstants.ATTR_BOOTPATH_PREPEND,
                newBootclasspathPrepend);
        //FIXME - does not seems to be taken into account - cannot figure why
        // in eclipse source
        // see D:\eclipse-SDK-3.0-win32\plugins\org.eclipse.jdt.launching_3.0.0
        // launching.jar
        // JavaLocalApplicationLaunchConfigurationDelegate

        //FIXME instead patch VM args - should use some AW method to preserve
        // former config
        String vmpre = "-Xbootclasspath/p:\"" + jarKey;
        // we need to add AW-core as well
        for (int i = 0; i < classPath.length; i++) {
            String entry = classPath[i];
            if (entry.indexOf("aspectwerkz-core-") > 0) {
                vmpre = vmpre + File.pathSeparator + entry;
            }
        }
        vmpre = vmpre + "\"";

        String older = configuration.getAttribute(
                IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "");
        vmpre = vmpre + " " + older;
        copy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
                vmpre);

        // FIXME remove when ok
        copy.doSave();

        super.launch(copy, mode, launch, monitor);
    }

}