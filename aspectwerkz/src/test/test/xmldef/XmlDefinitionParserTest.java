/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.xmldef;

import java.io.File;
import java.util.Iterator;

import junit.framework.TestCase;

import org.codehaus.aspectwerkz.xmldef.definition.IntroductionDefinition;
import org.codehaus.aspectwerkz.xmldef.definition.AdviceDefinition;
import org.codehaus.aspectwerkz.xmldef.definition.AspectDefinition;
import org.codehaus.aspectwerkz.xmldef.definition.BindIntroductionRule;
import org.codehaus.aspectwerkz.xmldef.definition.BindAdviceRule;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.definition.PointcutDefinition;
import org.codehaus.aspectwerkz.definition.XmlParser;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class XmlDefinitionParserTest extends TestCase {

    private File m_input = new File("src/test/newdef.xml");

    public void testIntroductionTag() {
        try {
            AspectWerkzDefinition aspectwerkz = (AspectWerkzDefinition)XmlParser.parseNoCache(m_input.toURL()).get(0);
            Iterator it = aspectwerkz.getIntroductionDefinitions().iterator();
            it.next();//skip "loggable"
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
            AspectWerkzDefinition aspectwerkz = (AspectWerkzDefinition)XmlParser.parseNoCache(m_input.toURL()).get(0);
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
            AspectWerkzDefinition aspectwerkz = (AspectWerkzDefinition)XmlParser.parseNoCache(m_input.toURL()).get(0);
            Iterator it1 = aspectwerkz.getAspectDefinitions().iterator();
            it1.next();// SystemAspect @todo check this with Jonas (side effect of precedence fix)
            AspectDefinition aspect = (AspectDefinition)it1.next();
            assertEquals("Logger", aspect.getName());
            assertEquals("Service", aspect.getExtends());
            assertEquals("services.*", ((BindIntroductionRule)aspect.getBindIntroductionRules().get(0)).getExpression().getExpression());
            assertEquals("loggable", (String)((BindIntroductionRule)aspect.getBindIntroductionRules().get(0)).getIntroductionRefs().get(0));
            assertEquals("services.*", ((BindIntroductionRule)aspect.getBindIntroductionRules().get(1)).getExpression().getExpression());
            assertEquals("loggable", (String)((BindIntroductionRule)aspect.getBindIntroductionRules().get(1)).getIntroductionRefs().get(0));
            assertEquals("start && stop", ((BindAdviceRule)aspect.getBindAdviceRules().get(0)).getExpression().getExpression());
            assertEquals("logging", (String)((BindAdviceRule)aspect.getBindAdviceRules().get(0)).getAdviceRefs().get(0));
            assertEquals("start || stop", ((BindAdviceRule)aspect.getBindAdviceRules().get(1)).getExpression().getExpression());
            assertEquals("logging", (String)((BindAdviceRule)aspect.getBindAdviceRules().get(1)).getAdviceRefs().get(0));
        }
        catch (Exception e) {
            System.out.println(e);
            fail();
        }
    }

    public void testPointcutTag() {
        try {
            final AspectWerkzDefinition aspectwerkz = (AspectWerkzDefinition)XmlParser.parseNoCache(m_input.toURL()).get(0);
            Iterator itl = aspectwerkz.getAspectDefinitions().iterator();
            itl.next(); // SystemAspect @todo validate with Jonas (side effect of precedence fix)
            Iterator it = ((AspectDefinition)itl.next()).getPointcutDefs().iterator();
            it.next();//skip "start"
            PointcutDefinition pointcut2 = (PointcutDefinition)it.next();
            assertEquals("method", pointcut2.getType());
            assertEquals("stop", pointcut2.getName());
            assertEquals("* services.*.stop(..)", pointcut2.getExpression());
            // absract aspect pointcut are added thereafter
            // @todo review precedence
            it.next();//skip "callerSideTest"
            PointcutDefinition pointcut1 = (PointcutDefinition)it.next();
            assertEquals("setField", pointcut1.getType());
            assertEquals("setFieldTest", pointcut1.getName());
            assertEquals("boolean services.*.m_isRunning", pointcut1.getExpression());
        }
        catch (Exception e) {
            System.out.println(e);
            fail();
        }
    }

//    public void testMergeDocuments() {
//        try {
//            Document document1 = DocumentParser.createDocument(m_input.toURL());
//            Document document2 = DocumentParser.createDocument(new File("src/test/aspectwerkz-test.xml").toURL());
//            Document document = DocumentParser.mergeDocuments(document1, document2);
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
