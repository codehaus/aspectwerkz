/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.xmldef;

import junit.framework.TestCase;
import org.codehaus.aspectwerkz.xmldef.AspectWerkz;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class PerformanceTest extends TestCase {

    private boolean m_printInfo = true;
    private int m_numberOfInvocations = 1000000;

    public void testAdvisedMethodNoAdvicePerformance() {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < m_numberOfInvocations; i++) {
            nonAdvisedMethod();
        }
        long time = System.currentTimeMillis() - startTime;
        double timePerInvocationNormalMethod = time / (double)m_numberOfInvocations;
        startTime = System.currentTimeMillis();
        for (int i = 0; i < m_numberOfInvocations; i++) {
            methodAdvisedMethodNoAdvice();
        }
        time = System.currentTimeMillis() - startTime;
        double timePerInvocation = time / (double)m_numberOfInvocations;
        double overhead = timePerInvocation - timePerInvocationNormalMethod;
        if (m_printInfo) System.out.println("\nOverhead, advised method no advice: " + overhead);
    }

    public void testMethodAdvicePerJVMPerformance() {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < m_numberOfInvocations; i++) {
            nonAdvisedMethod();
        }
        long time = System.currentTimeMillis() - startTime;
        double timePerInvocationNormalMethod = time / (double)m_numberOfInvocations;
        startTime = System.currentTimeMillis();
        for (int i = 0; i < m_numberOfInvocations; i++) {
            methodAdvisedMethodPerJVM();
        }
        time = System.currentTimeMillis() - startTime;
        double timePerInvocation = time / (double)m_numberOfInvocations;
        double overhead = timePerInvocation - timePerInvocationNormalMethod;
        if (m_printInfo) System.out.println("\nOverhead, advised method PER_JVM advice: " + overhead);
    }

    public void testMethodAdvicePerClassPerformance() {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < m_numberOfInvocations; i++) {
            nonAdvisedMethod();
        }
        long time = System.currentTimeMillis() - startTime;
        double timePerInvocationNormalMethod = time / (double)m_numberOfInvocations;
        startTime = System.currentTimeMillis();
        for (int i = 0; i < m_numberOfInvocations; i++) {
            methodAdvisedMethodPerClass();
        }
        time = System.currentTimeMillis() - startTime;
        double timePerInvocation = time / (double)m_numberOfInvocations;
        double overhead = timePerInvocation - timePerInvocationNormalMethod;
        if (m_printInfo) System.out.println("\nOverhead, advised method PER_CLASS advice: " + overhead);
    }

    public void testMethodAdvicePerInstancePerformance() {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < m_numberOfInvocations; i++) {
            nonAdvisedMethod();
        }
        long time = System.currentTimeMillis() - startTime;
        double timePerInvocationNormalMethod = time / (double)m_numberOfInvocations;
        startTime = System.currentTimeMillis();
        for (int i = 0; i < m_numberOfInvocations; i++) {
            methodAdvisedMethodPerInstance();
        }
        time = System.currentTimeMillis() - startTime;
        double timePerInvocation = time / (double)m_numberOfInvocations;
        double overhead = timePerInvocation - timePerInvocationNormalMethod;
        if (m_printInfo) System.out.println("\nOverhead, advised method PER_INSTANCE advice: " + overhead);
    }

    public void testMethodAdvicePerThreadPerformance() {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < m_numberOfInvocations; i++) {
            nonAdvisedMethod();
        }
        long time = System.currentTimeMillis() - startTime;
        double timePerInvocationNormalMethod = time / (double)m_numberOfInvocations;
        startTime = System.currentTimeMillis();
        for (int i = 0; i < m_numberOfInvocations; i++) {
            methodAdvisedMethodPerThread();
        }
        time = System.currentTimeMillis() - startTime;
        double timePerInvocation = time / (double)m_numberOfInvocations;
        double overhead = timePerInvocation - timePerInvocationNormalMethod;
        if (m_printInfo) System.out.println("\nOverhead, advised method PER_THREAD advice: " + overhead);
    }

    public void testIntroductionPerJVMPerformance() {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < m_numberOfInvocations; i++) {
            nonAdvisedMethod();
        }
        long time = System.currentTimeMillis() - startTime;
        double timePerInvocationNormalMethod = time / (double)m_numberOfInvocations;
        startTime = System.currentTimeMillis();
        PerJVM perJVM = (PerJVM)this;
        for (int i = 0; i < m_numberOfInvocations; i++) {
            perJVM.runPerJVM();
        }
        time = System.currentTimeMillis() - startTime;
        double timePerInvocation = time / (double)m_numberOfInvocations;
        double overhead = timePerInvocation - timePerInvocationNormalMethod;
        if (m_printInfo) System.out.println("\nOverhead, method introduction PER_JVM: " + overhead);
    }

    public void testIntroductionPerClassPerformance() {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < m_numberOfInvocations; i++) {
            nonAdvisedMethod();
        }
        long time = System.currentTimeMillis() - startTime;
        double timePerInvocationNormalMethod = time / (double)m_numberOfInvocations;
        startTime = System.currentTimeMillis();
        PerClass perClass = (PerClass)this;
        for (int i = 0; i < m_numberOfInvocations; i++) {
            perClass.runPerClass();
        }
        time = System.currentTimeMillis() - startTime;
        double timePerInvocation = time / (double)m_numberOfInvocations;
        double overhead = timePerInvocation - timePerInvocationNormalMethod;
        if (m_printInfo) System.out.println("\nOverhead, method introduction PER_CLASS: " + overhead);
    }

    public void testIntroductionPerInstancePerformance() {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < m_numberOfInvocations; i++) {
            nonAdvisedMethod();
        }
        long time = System.currentTimeMillis() - startTime;
        double timePerInvocationNormalMethod = time / (double)m_numberOfInvocations;
        startTime = System.currentTimeMillis();
        PerInstance perInstance = (PerInstance)this;
        for (int i = 0; i < m_numberOfInvocations; i++) {
            perInstance.runPerInstance();
        }
        time = System.currentTimeMillis() - startTime;
        double timePerInvocation = time / (double)m_numberOfInvocations;
        double overhead = timePerInvocation - timePerInvocationNormalMethod;
        if (m_printInfo) System.out.println("\nOverhead, method introduction PER_INSTANCE: " + overhead);
    }

    public void testIntroductionPerThreadPerformance() {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < m_numberOfInvocations; i++) {
            nonAdvisedMethod();
        }
        long time = System.currentTimeMillis() - startTime;
        double timePerInvocationNormalMethod = time / (double)m_numberOfInvocations;
        startTime = System.currentTimeMillis();
        PerThread perThread = (PerThread)this;
        for (int i = 0; i < m_numberOfInvocations; i++) {
            perThread.runPerThread();
        }
        time = System.currentTimeMillis() - startTime;
        double timePerInvocation = time / (double)m_numberOfInvocations;
        double overhead = timePerInvocation - timePerInvocationNormalMethod;
        if (m_printInfo) System.out.println("\nOverhead, method introduction PER_THREAD: " + overhead);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(PerformanceTest.class);
    }

    public PerformanceTest(String name) {
        super(name);
        AspectWerkz.getSystem("tests").initialize();
    }

    // ==== methods to test ====

    public void nonAdvisedMethod() {
    }

    public void preAdvisedMethodPerJVM() {
    }

    public void preAdvisedMethodPerClass() {
    }

    public void preAdvisedMethodPerInstance() {
    }

    public void preAdvisedMethodPerThread() {
    }

    public void methodAdvisedMethodPerJVM() {
    }

    public void methodAdvisedMethodPerClass() {
    }

    public void methodAdvisedMethodPerInstance() {
    }

    public void methodAdvisedMethodPerThread() {
    }

    public void methodAdvisedMethodNoAdvice() {
    }
}
