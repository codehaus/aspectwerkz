package test;

import java.io.File;
import java.util.Iterator;

import junit.framework.TestCase;

import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.definition.IntroductionDefinition;
import org.codehaus.aspectwerkz.definition.AdviceDefinition;
import org.codehaus.aspectwerkz.definition.PointcutDefinition;
import org.codehaus.aspectwerkz.definition.IntroductionWeavingRule;
import org.codehaus.aspectwerkz.definition.AspectDefinition;
import org.codehaus.aspectwerkz.definition.AdviceWeavingRule;
import org.codehaus.aspectwerkz.definition.XmlDefinitionParser;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: XmlDefinitionParserTest.java,v 1.2 2003-06-17 15:46:00 jboner Exp $
 */
public class XmlDefinitionParserTest extends TestCase {

    private File m_input = new File("src/test/newdef.xml");

    public void testIntroductionTag() {
        try {
            AspectWerkzDefinition aspectwerkz =
                   XmlDefinitionParser.parse(m_input);
            Iterator it = aspectwerkz.getIntroductionDefinitions().iterator();
            IntroductionDefinition introduction2 = (IntroductionDefinition)it.next();
            assertEquals("mixin", introduction2.getName());
            assertEquals("mixins.Mixin", introduction2.getInterface());
            assertEquals("mixins.MixinImpl", introduction2.getImplementation());
            assertEquals("perInstance", introduction2.getDeploymentModel());
            assertEquals("attribute", introduction2.getAttribute());
            IntroductionDefinition introduction1 = (IntroductionDefinition)it.next();
            assertEquals("loggable", introduction1.getName());
            assertEquals("logging.Loggable", introduction1.getInterface());
            assertEquals("loggable", introduction1.getAttribute());
        }
        catch (Exception e) {
            System.out.println("e = " + e);
            fail();
        }
    }

    public void testAdviceTag() {
        try {
            AspectWerkzDefinition aspectwerkz =
                   XmlDefinitionParser.parse(m_input);
            Iterator it = aspectwerkz.getAdviceDefinitions().iterator();
            AdviceDefinition advice1 = (AdviceDefinition)it.next();
            assertEquals("logging", advice1.getName());
            assertEquals("examples.logging.LoggingAdvice", advice1.getClassName());
            assertEquals("perClass", advice1.getDeploymentModel());
            assertEquals("log", advice1.getAttribute());
        }
        catch (Exception e) {
            fail();
        }
    }

    public void testAspectTag() {
        try {
            AspectWerkzDefinition aspectwerkz =
                   XmlDefinitionParser.parse(m_input);
            Iterator it1 = aspectwerkz.getAspectDefinitions().iterator();
            AspectDefinition aspect = (AspectDefinition)it1.next();
            assertEquals("Logger", aspect.getName());
            assertEquals("Service", aspect.getExtends());
            assertEquals("services.*", ((IntroductionWeavingRule)aspect.getIntroductionWeavingRules().get(0)).getClassPattern());
            assertEquals("loggable", (String)((IntroductionWeavingRule)aspect.getIntroductionWeavingRules().get(0)).getIntroductionRefs().get(0));
            assertEquals("services.*", ((IntroductionWeavingRule)aspect.getIntroductionWeavingRules().get(1)).getClassPattern());
            assertEquals("loggable", (String)((IntroductionWeavingRule)aspect.getIntroductionWeavingRules().get(1)).getIntroductionRefs().get(0));
            assertEquals("start && stop", ((AdviceWeavingRule)aspect.getAdviceWeavingRules().get(0)).getExpression());
            assertEquals("logging", (String)((AdviceWeavingRule)aspect.getAdviceWeavingRules().get(0)).getAdviceRefs().get(0));
            assertEquals("start || stop", ((AdviceWeavingRule)aspect.getAdviceWeavingRules().get(1)).getExpression());
            assertEquals("logging", (String)((AdviceWeavingRule)aspect.getAdviceWeavingRules().get(1)).getAdviceRefs().get(0));
        }
        catch (Exception e) {
            System.out.println(e);
            fail();
        }
    }

    public void testPointcutTag() {
        try {
            final AspectWerkzDefinition aspectwerkz =
                   XmlDefinitionParser.parse(m_input);
            Iterator it = ((AspectDefinition)aspectwerkz.getAspectDefinitions().iterator().next()).getPointcuts().iterator();
            PointcutDefinition pointcut2 = (PointcutDefinition)it.next();
            assertEquals("method", pointcut2.getType());
            assertEquals("stop", pointcut2.getName());
            assertEquals("services.*", pointcut2.getClassPattern());
            assertEquals("* stop(..)", pointcut2.getPattern());
            PointcutDefinition pointcut1 = (PointcutDefinition)it.next();
            assertEquals("method", pointcut1.getType());
            assertEquals("start", pointcut1.getName());
            assertEquals("services.*", pointcut1.getClassPattern());
            assertEquals("* start(..)", pointcut1.getPattern());
        }
        catch (Exception e) {
            System.out.println(e);
            fail();
        }
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(XmlDefinitionParserTest.class);
    }

    public XmlDefinitionParserTest(String name) {
        super(name);
    }
}
