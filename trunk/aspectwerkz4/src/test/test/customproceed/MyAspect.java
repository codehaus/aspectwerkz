/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.customproceed;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class MyAspect {

    public static interface ProceedWithIntArg {
        Object proceed(int i);
    }

    public static interface ProceedWithLongArg {
        Object proceed(long l);
    }

    public static interface ProceedWithStringArg {
        Object proceed(String s);
    }

    public static interface ProceedWithMiscArgs {
        Object proceed(long i, String s, int[][] matrix);
    }

    /**
     * @Around("execution(* test.customproceed.CustomProceedTest.setInt(int)) && args(int i)")
     */
    public Object around1(ProceedWithIntArg jp, int i) {
        CustomProceedTest.log("around1 ");
        CustomProceedTest.log(new Integer(i).toString());
        CustomProceedTest.log(" ");
        return jp.proceed(1);
    }

    /**
     * @Around("execution(* test.customproceed.CustomProceedTest.setLong(long)) && args(long l)")
     */
    public Object around2(ProceedWithLongArg jp, long l) {
        CustomProceedTest.log("around2 ");
        CustomProceedTest.log(new Long(l).toString());
        CustomProceedTest.log(" ");
        return jp.proceed(2);
    }

    /**
     * @Around("execution(* test.customproceed.CustomProceedTest.setString(s)) && args(String s)")
     */
    public Object around3(ProceedWithStringArg jp, String s) {
        CustomProceedTest.log("around3 ");
        CustomProceedTest.log(s);
        CustomProceedTest.log(" ");
        return jp.proceed("gnitset");
    }

    public Object around4(ProceedWithMiscArgs jp, long i, String s, int[][] matrix) {
        CustomProceedTest.log("around4 ");
        CustomProceedTest.log(new Long(i).toString());
        CustomProceedTest.log(" ");
        CustomProceedTest.log(s);
        CustomProceedTest.log(" ");
        CustomProceedTest.log(new Integer(matrix[0][0]).toString());
        CustomProceedTest.log(" ");
        matrix[0][0] = 123;
        return jp.proceed(12345, "gnitset", matrix);
    }
}
