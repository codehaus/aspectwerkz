package test;

import junit.framework.TestCase;
import org.codehaus.aspectwerkz.definition.AdviceWeavingRule;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @version $Id: AdviceWeavingRuleTest.java,v 1.2 2003-07-08 11:43:35 jboner Exp $
 */
public class AdviceWeavingRuleTest extends TestCase {

    public void testGetPointcutRefs() {
        try {
            AdviceWeavingRule rule = new AdviceWeavingRule();
            rule.setExpression("AA && BB");
            assertEquals("AA", (String)rule.getPointcutRefs().get(0));
            assertEquals("BB", (String)rule.getPointcutRefs().get(1));

            rule = new AdviceWeavingRule();
            rule.setExpression("!AA && !BB");
            assertEquals("AA", (String)rule.getPointcutRefs().get(0));
            assertEquals("BB", (String)rule.getPointcutRefs().get(1));

            rule = new AdviceWeavingRule();
            rule.setExpression("((((!AA))) && ((BB)))");
            assertEquals("AA", (String)rule.getPointcutRefs().get(0));
            assertEquals("BB", (String)rule.getPointcutRefs().get(1));

            rule = new AdviceWeavingRule();
            rule.setExpression("(((((AA))) && ((BB))) || !!DD)");
            assertEquals("AA", (String)rule.getPointcutRefs().get(0));
            assertEquals("BB", (String)rule.getPointcutRefs().get(1));
            assertEquals("DD", (String)rule.getPointcutRefs().get(2));

            rule = new AdviceWeavingRule();
            rule.setExpression("((AA || BB) && CC)");
            assertEquals("AA", (String)rule.getPointcutRefs().get(0));
            assertEquals("BB", (String)rule.getPointcutRefs().get(1));
            assertEquals("CC", (String)rule.getPointcutRefs().get(2));

            rule = new AdviceWeavingRule();
            rule.setExpression("!(((AA && !BB) && CC) && DD)");
            assertEquals("AA", (String)rule.getPointcutRefs().get(0));
            assertEquals("BB", (String)rule.getPointcutRefs().get(1));
            assertEquals("CC", (String)rule.getPointcutRefs().get(2));
            assertEquals("DD", (String)rule.getPointcutRefs().get(3));
        }
        catch (Exception e) {
            fail();
        }
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(AdviceWeavingRuleTest.class);
    }

    public AdviceWeavingRuleTest(String name) {
        super(name);
    }
}
