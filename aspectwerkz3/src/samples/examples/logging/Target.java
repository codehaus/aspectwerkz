/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.logging;

/**
 * serializable 
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class Target {

    /**
     * log level=1 flt=5.8F iconstant=org.codehaus.aspectwerkz.DeploymentModel.PER_CLASS
     */
    private int m_counter1;

    /**
     * log level=1 iconstant=org.codehaus.aspectwerkz.DeploymentModel.PER_THREAD
     */
    private int m_counter2;

    public int getCounter() {
        return m_counter1;
    }

    public void increment() {
        m_counter2 = m_counter2 + 1;
    }

    /**
     * log level=0
     *      sconstant=org.codehaus.aspectwerkz.transform.TransformationUtil.ASPECTWERKZ_PREFIX
     */
    public static int toLog1(int i) {
        System.out.println("Target.toLog1()");
        new Target().toLog2(new String[] {
            "parameter"
        });
        return 1;
    }

    /**
     * log level=3 sarr={"Hello","World", "Jonas's car"}
     */
    public java.lang.String[] toLog2(java.lang.String[] arg) {
        System.out.println("Target.toLog2()");
        new Target().toLog3();
        return null;
    }

    /**
     * log level=4 darr={4.5D,8.98665D,0.00000342}
     */
    public String toLog3() {
        System.out.println("Target.toLog3()");
        return "result";
    }

    public static void main(String[] args) {
        try {
            System.out.println("Target.main");
            Target.toLog1(3);
            Target target = new Target();
            target.increment();
            target.getCounter();
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}