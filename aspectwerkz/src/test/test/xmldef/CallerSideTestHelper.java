/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.xmldef.xmldef;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class CallerSideTestHelper {

    public CallerSideTestHelper() {
    }

    public CallerSideTestHelper(int i) {
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
