package test;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.definition.AspectDefinition;
import org.codehaus.aspectwerkz.definition.IntroductionDefinition;
import org.codehaus.aspectwerkz.definition.PointcutDefinition;
import org.codehaus.aspectwerkz.definition.Dom4jXmlDefinitionParser;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: AspectWerkzDefinitionTest.java,v 1.5 2003-06-09 07:04:13 jboner Exp $
 */
public class AspectWerkzDefinitionTest extends TestCase {

    private AspectWerkzDefinition m_def;

    public void testGetIntroductions() {
        Object[] introductions = m_def.getIntroductionDefinitions().toArray();
        assertEquals("counter1", ((IntroductionDefinition)introductions[0]).getName());
        assertEquals("counter2", ((IntroductionDefinition)introductions[1]).getName());
        assertEquals("perInstance", ((IntroductionDefinition)introductions[0]).getDeploymentModel());
        assertEquals("perInstance", ((IntroductionDefinition)introductions[1]).getDeploymentModel());
        assertEquals("examples.persistence.Counter1", ((IntroductionDefinition)introductions[0]).getInterface());
        assertEquals("examples.persistence.Counter2", ((IntroductionDefinition)introductions[1]).getInterface());
        assertEquals("examples.persistence.CounterImpl1", ((IntroductionDefinition)introductions[0]).getImplementation());
        assertEquals("examples.persistence.CounterImpl2", ((IntroductionDefinition)introductions[1]).getImplementation());
    }

    public void testGetAspects() {
        Object[] aspects = m_def.getAspectDefinitions().toArray();
        assertEquals("examples.persistence.Target", ((AspectDefinition)aspects[0]).getPattern());
        assertEquals("examples.caching.Pi", ((AspectDefinition)aspects[1]).getPattern());
        assertEquals("examples.logging.Target", ((AspectDefinition)aspects[2]).getPattern());
        assertEquals("counter1", (String)((AspectDefinition)aspects[0]).getIntroductions().get(0));
        assertEquals("counter2", (String)((AspectDefinition)aspects[0]).getIntroductions().get(1));
        assertEquals("counter1", (String)((AspectDefinition)aspects[0]).getIntroductions().toArray()[0]);
        assertEquals("counter2", (String)((AspectDefinition)aspects[0]).getIntroductions().toArray()[1]);
        assertEquals("invoke", (String)((PointcutDefinition)((AspectDefinition)aspects[0]).getPointcuts().toArray()[0]).getPatterns().get(0));
        assertEquals("invoke2", (String)((PointcutDefinition)((AspectDefinition)aspects[0]).getPointcuts().toArray()[0]).getPatterns().get(1));
        assertEquals("invoke3", (String)((PointcutDefinition)((AspectDefinition)aspects[0]).getPointcuts().toArray()[0]).getPatterns().get(2));
        assertEquals("invoke4", (String)((PointcutDefinition)((AspectDefinition)aspects[0]).getPointcuts().toArray()[0]).getPatterns().get(3));
        assertEquals("counter3", (String)((PointcutDefinition)((AspectDefinition)aspects[0]).getPointcuts().toArray()[0]).getAdvices().get(0));
    }

    public void testGetAspectTargetClassNames() {
        String[] targetClassNames = m_def.getAspectTargetClassNames();
        assertEquals("test.MyPreAdvice1", targetClassNames[0]);
        assertEquals("examples.caching.CachingAdvice", targetClassNames[1]);
        assertEquals("examples.caching.Pi", targetClassNames[2]);
        assertEquals("test.MyThrowsAdvice1", targetClassNames[3]);
        assertEquals("examples.persistence.InvocationCounterAdvice", targetClassNames[4]);
        assertEquals("examples.persistence.CounterImpl2", targetClassNames[5]);
        assertEquals("examples.persistence.CounterImpl1", targetClassNames[6]);
        assertEquals("examples.logging.Target", targetClassNames[7]);
        assertEquals("examples.persistence.Target", targetClassNames[8]);
        assertEquals("examples.logging.LoggingAdvice", targetClassNames[9]);
   }

    public void testGetIntroductionInterfaceName() {
        assertEquals("examples.persistence.Counter1", m_def.getIntroductionInterfaceName("counter1"));
    }

    public void testGetIntroductionIndexFor() {
        assertEquals(1, m_def.getIntroductionIndexFor("counter1"));
        assertEquals(2, m_def.getIntroductionIndexFor("counter2"));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(AspectWerkzDefinitionTest.class);
    }

    public void setUp() {
        Dom4jXmlDefinitionParser defParser = new Dom4jXmlDefinitionParser();
        m_def = defParser.parseNoCache(new File("src/test/definitiontest.xml"));
    }

    public AspectWerkzDefinitionTest(String name) {
        super(name);
    }
}
