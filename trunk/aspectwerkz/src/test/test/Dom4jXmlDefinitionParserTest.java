package test;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.definition.IntroductionDefinition;
import org.codehaus.aspectwerkz.definition.AdviceDefinition;
import org.codehaus.aspectwerkz.definition.AspectDefinition;
import org.codehaus.aspectwerkz.definition.PointcutDefinition;
import org.codehaus.aspectwerkz.definition.Dom4jXmlDefinitionParser;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: Dom4jXmlDefinitionParserTest.java,v 1.2 2003-06-09 07:04:13 jboner Exp $
 */
public class Dom4jXmlDefinitionParserTest extends TestCase {

    private File m_input = new File("src/test/xmltest.xml");

    public void testIntroductionTag() {
        try {
            final AspectWerkzDefinition aspectwerkz =
                   Dom4jXmlDefinitionParser.parse(m_input);
            final Iterator it = aspectwerkz.getIntroductionDefinitions().iterator();
            final IntroductionDefinition introduction1 = (IntroductionDefinition)it.next();
            assertEquals("serializable", introduction1.getName());
            assertEquals("java.io.Serializable", introduction1.getInterface());
            assertEquals("attribute", introduction1.getAttribute());
            final IntroductionDefinition introduction2 = (IntroductionDefinition)it.next();
            assertEquals("mixin", introduction2.getName());
            assertEquals("mixins.Mixin", introduction2.getInterface());
            assertEquals("mixins.MixinImpl", introduction2.getImplementation());
            assertEquals("perInstance", introduction2.getDeploymentModel());
            assertEquals("attribute", introduction2.getAttribute());
        }
        catch (Exception e) {
            fail();
        }
    }

    public void testAdviceTag() {
        try {
            final AspectWerkzDefinition aspectwerkz =
                   Dom4jXmlDefinitionParser.parse(m_input);
            final Iterator it = aspectwerkz.getAdviceDefinitions().iterator();
            final AdviceDefinition advice1 = (AdviceDefinition)it.next();
            assertEquals("methodAdvicePerClass", advice1.getName());
            assertEquals("advices.MyMethodAdvice", advice1.getClassName());
            assertEquals("perClass", advice1.getDeploymentModel());
            assertEquals("attribute", advice1.getAttribute());
            final AdviceDefinition advice2 = (AdviceDefinition)it.next();
            assertEquals("methodAdvicePerJVM", advice2.getName());
            assertEquals("advices.MyMethodAdvice", advice2.getClassName());
            assertEquals("perJVM", advice2.getDeploymentModel());
            assertEquals("attribute", advice2.getAttribute());
        }
        catch (Exception e) {
            fail();
        }
    }

    public void testAspectTag() {
        try {
            final AspectWerkzDefinition aspectwerkz =
                   Dom4jXmlDefinitionParser.parse(m_input);
            final Iterator it1 = aspectwerkz.getAspectDefinitions().iterator();
            final AspectDefinition aspect = (AspectDefinition)it1.next();
            assertEquals("test.SomeClass", aspect.getPattern());
            assertEquals("serializable", (String)aspect.getIntroductions().get(0));
            assertEquals("mixin", (String)aspect.getIntroductions().get(1));
            final List introductions = aspect.getIntroductions();
            assertEquals("serializable", introductions.get(0));
            assertEquals("mixin", introductions.get(1));
        }
        catch (Exception e) {
            System.out.println(e);
            fail();
        }
    }

    public void testPointcutTag() {
        try {
            final AspectWerkzDefinition aspectwerkz =
                   Dom4jXmlDefinitionParser.parse(m_input);
            final Iterator it = ((AspectDefinition)aspectwerkz.getAspectDefinitions().iterator().next()).getPointcuts().iterator();
            final PointcutDefinition pointcut1 = (PointcutDefinition)it.next();
            assertEquals("method", pointcut1.getType());
            assertEquals("someMemberMethod", (String)pointcut1.getPatterns().get(0));
            assertEquals("methodAdvicePerClass", pointcut1.getAdvices().get(0));
            assertEquals("methodAdvicePerJVM", pointcut1.getAdvices().get(1));
            final List advices = pointcut1.getAdvices();
            assertEquals("methodAdvicePerClass", advices.get(0));
            assertEquals("methodAdvicePerJVM", advices.get(1));
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
        return new junit.framework.TestSuite(Dom4jXmlDefinitionParserTest.class);
    }

    public Dom4jXmlDefinitionParserTest(String name) {
        super(name);
    }
}
