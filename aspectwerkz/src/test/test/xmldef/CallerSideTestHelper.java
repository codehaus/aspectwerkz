/*
 * AspectWerkz AOP Framework.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package test;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: CallerSideTestHelper.java,v 1.3 2003-06-27 09:26:10 jboner Exp $
 */
public class CallerSideTestHelper {

    public CallerSideTestHelper() {
        System.out.println("CallerSideTestHelper.CallerSideTestHelper1");
    }

    public CallerSideTestHelper(int i) {
        System.out.println("CallerSideTestHelper.CallerSideTestHelper2");
    }

    public void passingParameterToAdviceMethod() {
    }

    public String invokeMemberMethodPre() {
        return "invokeMemberMethodPre";
    }

    public String invokeMemberMethodPost() {
        return "invokeMemberMethodPost";
    }

    public String invokeMemberMethodPrePost() {
        return "invokeMemberMethodPrePost";
    }

    public static String invokeStaticMethodPre() {
        return "invokeStaticMethodPre";
    }

    public static String invokeStaticMethodPost() {
        return "invokeStaticMethodPost";
    }

    public static String invokeStaticMethodPrePost() {
        return "invokeStaticMethodPrePost";
    }
}
