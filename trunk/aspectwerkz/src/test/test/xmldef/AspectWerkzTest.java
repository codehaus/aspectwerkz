/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.xmldef;

import java.util.Collection;
import java.util.Iterator;

import junit.framework.TestCase;

import org.codehaus.aspectwerkz.AspectMetaData;
import org.codehaus.aspectwerkz.xmldef.XmlDefSystem;
import org.codehaus.aspectwerkz.xmldef.advice.Advice;
import org.codehaus.aspectwerkz.xmldef.advice.PreAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.IndexTuple;
import org.codehaus.aspectwerkz.SystemLoader;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AspectWerkzTest extends TestCase {

    public void testSetDeploymentModelForAdvice() {
        assertEquals(DeploymentModel.PER_JVM, ((XmlDefSystem)SystemLoader.getSystem("tests")).getAdvice("methodAdvice1").getDeploymentModel());
        ((XmlDefSystem)SystemLoader.getSystem("tests")).getAdvice("methodAdvice1").setDeploymentModel(DeploymentModel.PER_CLASS);
        assertEquals(DeploymentModel.PER_CLASS, ((XmlDefSystem)SystemLoader.getSystem("tests")).getAdvice("methodAdvice1").getDeploymentModel());
    }

    public void testRegisterAspect() {
        ((XmlDefSystem)SystemLoader.getSystem("tests")).register(
                new AspectMetaData("tests", getClass().getName(), DeploymentModel.PER_JVM)
        );
        Collection aspects = SystemLoader.getSystem("tests").getAspectsMetaData();
        for (Iterator it = aspects.iterator(); it.hasNext();) {
            AspectMetaData aspect = (AspectMetaData)it.next();
            if (aspect.getName().equals(getClass().getName())) {
                return;
            }
        }
        fail();
    }

    public void testRegisterAdvice() {
        Advice advice = new PreAdvice() {
            public void execute(final JoinPoint joinPoint) {
            }
        };
        ((XmlDefSystem)SystemLoader.getSystem("tests")).register("testRegisterAdvice", advice);
        assertNotNull(((XmlDefSystem)SystemLoader.getSystem("tests")).getAdvice("testRegisterAdvice"));
    }

    public void testFindAdviceByIndex() {
        Advice advice = new PreAdvice() {
            public void execute(final JoinPoint joinPoint) {
            }
        };
        ((XmlDefSystem)SystemLoader.getSystem("tests")).register("testFindAdviceByIndex", advice);
        IndexTuple index = SystemLoader.getSystem("tests").getAdviceIndexFor("testFindAdviceByIndex");
        assertEquals(((XmlDefSystem)SystemLoader.getSystem("tests")).getAdvice("testFindAdviceByIndex"), ((XmlDefSystem)SystemLoader.getSystem("tests")).getAdvice(index));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(AspectWerkzTest.class);
    }

    public AspectWerkzTest(String name) {
        super(name);
        SystemLoader.getSystem("tests").initialize();
    }
}
