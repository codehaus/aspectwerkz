/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
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
import org.codehaus.aspectwerkz.metadata.AttributeC;
import org.dom4j.Document;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class XmlDefinitionParserTest extends TestCase {

    private File m_input = new File("src/test/newdef.xml");

    public void testIntroductionTag() {
        try {
            AspectWerkzDefinition aspectwerkz = XmlDefinitionParser.parseNoCache(m_input.toURL());
            Iterator it = aspectwerkz.getIntroductionDefinitions().iterator();
            IntroductionDefinition introduction2 = (IntroductionDefinition)it.next();
            assertEquals("mixin", introduction2.getName());
            assertEquals("mixins.Mixin", introduction2.getInterface());
            assertEquals("mixins.MixinImpl", introduction2.getImplementation());
            assertEquals("perInstance", introduction2.getDeploymentModel());
            assertEquals("attribute", introduction2.getAttribute());
        }
        catch (Exception e) {
            System.out.println("e = " + e);
            fail();
        }
    }

    public void testAdviceTag() {
        try {
            AspectWerkzDefinition aspectwerkz = XmlDefinitionParser.parseNoCache(m_input.toURL());
            Iterator it = aspectwerkz.getAdviceDefinitions().iterator();
            AdviceDefinition advice1 = (AdviceDefinition)it.next();
            assertEquals("logging", advice1.getName());
            assertEquals("examples.logging.LoggingAdvice", advice1.getAdviceClassName());
            assertEquals("perClass", advice1.getDeploymentModel());
            assertEquals("log", advice1.getAttribute());
        }
        catch (Exception e) {
            fail();
        }
    }

    public void testAspectTag() {
        try {
            AspectWerkzDefinition aspectwerkz = XmlDefinitionParser.parseNoCache(m_input.toURL());
            Iterator it1 = aspectwerkz.getAspectDefinitions().iterator();
            it1.next();// SystemAspect @todo check this with Jonas (side effect of precedence fix)
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
            final AspectWerkzDefinition aspectwerkz = XmlDefinitionParser.parseNoCache(m_input.toURL());
            Iterator itl = aspectwerkz.getAspectDefinitions().iterator();
            itl.next(); // SystemAspect @todo validate with Jonas (side effect of precedence fix)
            Iterator it = ((AspectDefinition)itl.next()).getPointcutDefs().iterator();
            PointcutDefinition pointcut2 = (PointcutDefinition)it.next();
            assertEquals("method", pointcut2.getType());
            assertEquals("stop", pointcut2.getName());
            assertEquals("services.*", pointcut2.getClassPattern());
            assertEquals("* stop(..)", pointcut2.getPattern());
            PointcutDefinition pointcut1 = (PointcutDefinition)it.next();
            assertEquals("setField", pointcut1.getType());
            assertEquals("setFieldTest", pointcut1.getName());
            assertEquals("services.*", pointcut1.getClassPattern());
            assertEquals("boolean m_isRunning", pointcut1.getPattern());
        }
        catch (Exception e) {
            System.out.println(e);
            fail();
        }
    }

//    public void testMergeDocuments() {
//        try {
//            Document document1 = XmlDefinitionParser.createDocument(m_input.toURL());
//            Document document2 = XmlDefinitionParser.createDocument(new File("src/test/aspectwerkz-test.xml").toURL());
//            Document document = XmlDefinitionParser.mergeDocuments(document1, document2);
//            AttributeC.writeDocumentToFile(document, "c:\\temp\\mergedDocument.xml");
//        }
//        catch (Exception e) {
//            System.out.println(e);
//            fail();
//        }
//    }

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
