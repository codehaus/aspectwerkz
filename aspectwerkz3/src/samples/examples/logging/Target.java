/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.logging;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class Target {

    /**
     * @log level=1  flt=5.8F iconstant=org.codehaus.aspectwerkz.DeploymentModel.PER_CLASS
     */
    private int m_counter1;

    /**
     * @log level=1 iconstant=org.codehaus.aspectwerkz.DeploymentModel.PER_THREAD
     */
    private int m_counter2;

    public int getCounter() {
        return m_counter1;
    }

    public void increment() {
        m_counter2 = m_counter2 + 1;
    }

    /**
     * @log level=0 sconstant=org.codehaus.aspectwerkz.transform.TransformationUtil.ASPECTWERKZ_PREFIX
     */
    public static void toLog1() {
        new Target().toLog2("parameter");
    }

    /**
     * @log level=3 sarr={"Hello","World", "Jonas's car"}
     */
    protected void toLog2(java.lang.String arg) {
        new Target().toLog3();
    }

    /**
     * @log level=4 darr={4.5D,8.98665D,0.00000342}
     */
    private String toLog3() {
        return "result";
    }

    public static void main(String[] args) {
        System.out.println("Target.main");
        Target.toLog1();
        Target target = new Target();
        target.increment();
        target.getCounter();
    }
}
