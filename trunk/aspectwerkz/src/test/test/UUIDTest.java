package test;

import junit.textui.TestRunner;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import org.codehaus.aspectwerkz.util.UuidGenerator;

/**
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version %I%, %G%
 */
public class UUIDTest extends TestCase {

    private int m_numberOfInvocations = 1000000;

    public void testPerformance() {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < m_numberOfInvocations; i++) {
            String uuid = UuidGenerator.generate(this);
        }
        long time = System.currentTimeMillis() - startTime;
        double timePerUuidGenaration = time / (double)m_numberOfInvocations;
        System.out.println("timePerUuidGenaration = " + timePerUuidGenaration);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(UUIDTest.class);
    }

    public UUIDTest(String name) {
        super(name);
    }
}
