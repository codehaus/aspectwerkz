/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.deployment;

import junit.framework.TestCase;
import org.codehaus.aspectwerkz.transform.inlining.Deployer;
import org.codehaus.aspectwerkz.transform.inlining.DeploymentHandle;
import org.codehaus.aspectwerkz.definition.PreparedPointcut;
import org.codehaus.aspectwerkz.definition.DefinitionLoader;
import org.codehaus.aspectwerkz.definition.SystemDefinition;

/**
 * FIXME BUG with multiple advice - not in logging sample
 * FIXME add XML defined aspect test
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public class DeployerTest extends TestCase {
    private static String s_logString = "";

    public DeployerTest(String name) {
        super(name);
    }

    public void testDeployUndeployUsingHandle() {
        s_logString = "";

        deployUndeployUsingHandle();
        assertEquals("deployUndeployUsingHandle ", s_logString);
        s_logString = "";

        DeploymentHandle handle = Deployer.deploy(AnnDefAspect.class);

        deployUndeployUsingHandle();
        assertEquals("before deployUndeployUsingHandle after ", s_logString);
        s_logString = "";

        Deployer.undeploy(handle);

        deployUndeployUsingHandle();
        assertEquals("deployUndeployUsingHandle ", s_logString);
    }

    public void testDeployUndeployUsingPreparedPointcut() {
        s_logString = "";

        deployUndeployUsingPreparedPointcut();
        assertEquals("deployUndeployUsingPreparedPointcut ", s_logString);
        s_logString = "";

        final SystemDefinition systemDef = DefinitionLoader.getDefinition(
                Thread.currentThread().getContextClassLoader(), "tests"
        );
        PreparedPointcut preparedPointcut = systemDef.getPreparedPointcut("deployUndeployUsingPreparedPointcut");

        Deployer.deploy(AnnDefAspect.class, preparedPointcut);

        deployUndeployUsingPreparedPointcut();
        assertEquals("before deployUndeployUsingPreparedPointcut after ", s_logString);
        s_logString = "";

        Deployer.undeploy(AnnDefAspect.class);

        deployUndeployUsingPreparedPointcut();
        assertEquals("deployUndeployUsingPreparedPointcut ", s_logString);
    }

    private void deployUndeployUsingHandle() {
        log("deployUndeployUsingHandle ");
    }

    private void deployUndeployUsingPreparedPointcut() {
        log("deployUndeployUsingPreparedPointcut ");
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(DeployerTest.class);
    }

    public static void log(final String wasHere) {
        s_logString += wasHere;
    }
}
