package test;

import junit.framework.TestCase;

import org.codehaus.aspectwerkz.Aspect;
import org.codehaus.aspectwerkz.AspectWerkz;
import org.codehaus.aspectwerkz.definition.regexp.Pattern;
import org.codehaus.aspectwerkz.introduction.Introduction;
import org.codehaus.aspectwerkz.pointcut.MethodPointcut;
import org.codehaus.aspectwerkz.pointcut.FieldPointcut;
import org.codehaus.aspectwerkz.pointcut.ThrowsPointcut;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: AspectTest.java,v 1.2 2003-06-09 07:04:13 jboner Exp $
 */
public class AspectTest extends TestCase {

    public void testGetAspectName() {
        assertEquals(getClass().getName(), ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).getPattern());
    }

    public void testCreateWhenReadOnly() {
        AspectWerkz.getSystem("tests").createAspect(Pattern.compileClassPattern("testCreateWhenReadOnly")).markReadOnly();

        try {
            ((Aspect)AspectWerkz.getSystem("tests").getAspects("testCreateWhenReadOnly").get(0)).createMethodPointcut("* someMethod()");
        }
        catch (Exception e) {
        }
        try {
            ((Aspect)AspectWerkz.getSystem("tests").getAspects("testCreateWhenReadOnly").get(0)).createGetFieldPointcut("* someMethod");
        }
        catch (Exception e) {
        }
        try {
            ((Aspect)AspectWerkz.getSystem("tests").getAspects("testCreateWhenReadOnly").get(0)).createSetFieldPointcut("* someMethod");
        }
        catch (Exception e) {
        }
        try {
            ((Aspect)AspectWerkz.getSystem("tests").getAspects("testCreateWhenReadOnly").get(0)).createThrowsPointcut("* someMethod()#foo.bar.someException");
        }
        catch (Exception e) {
            return;
        }
        fail("exception expected");
    }

    public void testCreateMethodPointcut() {
        assertEquals("* testCreateMethodPointcut()", ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).createMethodPointcut("* testCreateMethodPointcut()").getName());
    }

    public void testGetMethodPointcut() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).createMethodPointcut("* testGetMethodPointcut()");
        assertEquals("* testGetMethodPointcut()", ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).getMethodPointcut("* testGetMethodPointcut()").getName());
    }

    public void testAddMethodPointcut() {
        MethodPointcut pointcut = new MethodPointcut("* testAddMethodPointcut()");
        ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).addMethodPointcut(pointcut);
        assertEquals("* testAddMethodPointcut()", ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).getMethodPointcut("* testAddMethodPointcut()").getName());
    }

    public void testAddMethodPointcuts() {
        MethodPointcut pointcut1 = new MethodPointcut("* testAddMethodPointcuts1()");
        MethodPointcut pointcut2 = new MethodPointcut("* testAddMethodPointcuts2()");
        MethodPointcut pointcut3 = new MethodPointcut("* testAddMethodPointcuts3()");
        ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).addMethodPointcuts(new MethodPointcut[]{pointcut1, pointcut2, pointcut3});
        assertEquals("* testAddMethodPointcuts1()", ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).getMethodPointcut("* testAddMethodPointcuts1()").getName());
        assertEquals("* testAddMethodPointcuts2()", ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).getMethodPointcut("* testAddMethodPointcuts2()").getName());
        assertEquals("* testAddMethodPointcuts3()", ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).getMethodPointcut("* testAddMethodPointcuts3()").getName());
    }

    public void testCreateGetFieldPointcut() {
        assertEquals("* testCreateGetFieldPointcut", ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).createGetFieldPointcut("* testCreateGetFieldPointcut").getName());
    }

    public void testGetGetFieldPointcut() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).createGetFieldPointcut("* testGetGetFieldPointcut");
        assertEquals("* testGetGetFieldPointcut", ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).getGetFieldPointcut("* testGetGetFieldPointcut").getName());
    }

    public void testAddGetFieldPointcut() {
        FieldPointcut pointcut = new FieldPointcut("* testAddGetFieldPointcut");
        ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).addGetFieldPointcut(pointcut);
        assertEquals("* testAddGetFieldPointcut", ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).getGetFieldPointcut("* testAddGetFieldPointcut").getName());
    }

    public void testAddGetFieldPointcuts() {
        FieldPointcut pointcut1 = new FieldPointcut("* testAddGetFieldPointcuts1");
        FieldPointcut pointcut2 = new FieldPointcut("* testAddGetFieldPointcuts2");
        FieldPointcut pointcut3 = new FieldPointcut("* testAddGetFieldPointcuts3");
        ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).addGetFieldPointcuts(new FieldPointcut[]{pointcut1, pointcut2, pointcut3});
        assertEquals("* testAddGetFieldPointcuts1", ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).getGetFieldPointcut("* testAddGetFieldPointcuts1").getName());
        assertEquals("* testAddGetFieldPointcuts2", ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).getGetFieldPointcut("* testAddGetFieldPointcuts2").getName());
        assertEquals("* testAddGetFieldPointcuts3", ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).getGetFieldPointcut("* testAddGetFieldPointcuts3").getName());
    }

    public void testCreateSetFieldPointcut() {
        assertEquals("* testCreateSetFieldPointcut", ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).createSetFieldPointcut("* testCreateSetFieldPointcut").getName());
    }

    public void testGetSetFieldPointcut() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).createSetFieldPointcut("* testGetSetFieldPointcut");
        assertEquals("* testGetSetFieldPointcut", ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).getSetFieldPointcut("* testGetSetFieldPointcut").getName());
    }

    public void testAddSetFieldPointcut() {
        FieldPointcut pointcut = new FieldPointcut("* testAddSetFieldPointcut");
        ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).addSetFieldPointcut(pointcut);
        assertEquals("* testAddSetFieldPointcut", ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).getSetFieldPointcut("* testAddSetFieldPointcut").getName());
    }

    public void testAddSetFieldPointcuts() {
        FieldPointcut pointcut1 = new FieldPointcut("* testAddSetFieldPointcuts1");
        FieldPointcut pointcut2 = new FieldPointcut("* testAddSetFieldPointcuts2");
        FieldPointcut pointcut3 = new FieldPointcut("* testAddSetFieldPointcuts3");
        ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).addSetFieldPointcuts(new FieldPointcut[]{pointcut1, pointcut2, pointcut3});
        assertEquals("* testAddSetFieldPointcuts1", ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).getSetFieldPointcut("* testAddSetFieldPointcuts1").getName());
        assertEquals("* testAddSetFieldPointcuts2", ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).getSetFieldPointcut("* testAddSetFieldPointcuts2").getName());
        assertEquals("* testAddSetFieldPointcuts3", ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).getSetFieldPointcut("* testAddSetFieldPointcuts3").getName());
    }

    public void testCreateThrowsPointcut() {
        assertEquals("* testCreateThrowsPointcut()#java.lang.Exception",
                ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).createThrowsPointcut("* testCreateThrowsPointcut()#java.lang.Exception").getName());
    }

    public void testGetThrowsPointcut() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).createThrowsPointcut("* testGetThrowsPointcut()#java.lang.Exception");
        assertEquals("* testGetThrowsPointcut()#java.lang.Exception",
                ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).getThrowsPointcut("* testGetThrowsPointcut()#java.lang.Exception").getName());
    }

    public void testAddThrowsPointcut() {
        ThrowsPointcut pointcut = new ThrowsPointcut("* testAddThrowsPointcut()#java.lang.Exception");
        ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).addThrowsPointcut(pointcut);
        assertEquals("* testAddThrowsPointcut()#java.lang.Exception",
                ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).getThrowsPointcut("* testAddThrowsPointcut()#java.lang.Exception").getName());
    }

    public void testAddThrowsPointcuts() {
        ThrowsPointcut pointcut1 = new ThrowsPointcut("* testAddThrowsPointcuts1()#java.lang.Exception");
        ThrowsPointcut pointcut2 = new ThrowsPointcut("* testAddThrowsPointcuts2()#java.lang.Exception");
        ThrowsPointcut pointcut3 = new ThrowsPointcut("* testAddThrowsPointcuts3()#java.lang.Exception");
        ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).addThrowsPointcuts(new ThrowsPointcut[]{pointcut1, pointcut2, pointcut3});
        assertEquals("* testAddThrowsPointcuts1()#java.lang.Exception", ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).getThrowsPointcut("* testAddThrowsPointcuts1()#java.lang.Exception").getName());
        assertEquals("* testAddThrowsPointcuts2()#java.lang.Exception", ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).getThrowsPointcut("* testAddThrowsPointcuts2()#java.lang.Exception").getName());
        assertEquals("* testAddThrowsPointcuts3()#java.lang.Exception", ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).getThrowsPointcut("* testAddThrowsPointcuts3()#java.lang.Exception").getName());
    }

    public void testHasThrowsPointcut1() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).createThrowsPointcut("* testHasThrowsPointcut1()#java.lang.Exception");
        assertTrue(((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).hasThrowsPointcut("testHasThrowsPointcut1", "java.lang.Exception"));
    }

    public void testAddIntroduction() {
        Introduction introduction = new Introduction("serializable", "java.io.Serializable");
        AspectWerkz.getSystem("tests").register("serializable", introduction);
        AspectWerkz.getSystem("tests").createAspect(Pattern.compileClassPattern("testAddIntroduction")).addIntroduction("serializable");
        assertTrue(((Aspect)AspectWerkz.getSystem("tests").getAspects("testAddIntroduction").get(0)).hasIntroductions());
        assertEquals(1, ((Aspect)AspectWerkz.getSystem("tests").getAspects("testAddIntroduction").get(0)).getIntroductions().length);
        assertEquals("serializable", ((Aspect)AspectWerkz.getSystem("tests").getAspects("testAddIntroduction").get(0)).getIntroductions()[0]);
    }

    public void testGetIntroductions() {
        AspectWerkz.getSystem("tests").createAspect(Pattern.compileClassPattern("testGetIntroductions")).addIntroductions(new String[]{"intro1", "intro2"});
        String[] introductions = ((Aspect)AspectWerkz.getSystem("tests").getAspects("testGetIntroductions").get(0)).getIntroductions();
        assertEquals(2, introductions.length);
    }

    public void testAddIntroductions() {
        Introduction introduction1 = new Introduction("intro1", "java.io.Serializable");
        Introduction introduction2 = new Introduction("intro2", "java.lang.Comparable");
        AspectWerkz.getSystem("tests").register("intro1", introduction1);
        AspectWerkz.getSystem("tests").register("intro2", introduction2);
        AspectWerkz.getSystem("tests").createAspect(Pattern.compileClassPattern("testAddIntroductions")).addIntroductions(new String[]{"intro1", "intro2"});
        assertEquals(2, ((Aspect)AspectWerkz.getSystem("tests").getAspects("testAddIntroductions").get(0)).getIntroductions().length);
        assertEquals("intro1", ((Aspect)AspectWerkz.getSystem("tests").getAspects("testAddIntroductions").get(0)).getIntroductions()[0]);
        assertEquals("intro2", ((Aspect)AspectWerkz.getSystem("tests").getAspects("testAddIntroductions").get(0)).getIntroductions()[1]);
    }

    public void testReadOnly() {
        assertEquals(false, ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).isReadOnly());
        ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).markReadOnly();
        assertEquals(true, ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).isReadOnly());
    }

    public void testEquals() {
        Aspect aspect1 = new Aspect("toString");
        Aspect aspect2 = new Aspect("toString");
        assertTrue(aspect2.equals(aspect2));
        assertTrue(aspect1.equals(aspect1));
        assertTrue(!aspect2.equals(aspect1));
        assertTrue(!aspect1.equals(aspect2));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(AspectTest.class);
    }

    public AspectTest(String name) {
        super(name);
        AspectWerkz.getSystem("tests").initialize();
        AspectWerkz.getSystem("tests").createAspect(Pattern.compileClassPattern(getClass().getName()));
    }
}
