/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.logging;

import org.codehaus.aspectwerkz.annotation.TypedAnnotationProxy;

/**
 * The 'log' annotation proxy.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class LogAnnotationProxy extends TypedAnnotationProxy {
    public static final int INFO = 0;

    public static final int ERROR = 1;

    public static final int WARNING = 2;

    private int m_level;

    private float m_flt;

    private int m_iConstant;

    private String m_sConstant;

    private double[] m_darr;

    private String[] m_sarr;

    public int level() {
        return m_level;
    }

    public void setlevel(int value) {
        System.out.println("value = " + value);
        m_level = value;
    }

    public float flt() {
        return m_flt;
    }

    public void setflt(float aFloat) {
        System.out.println("aFloat = " + aFloat);
        m_flt = aFloat;
    }

    public int iconstant() {
        return m_iConstant;
    }

    public void seticonstant(int constant) {
        System.out.println("iconstant = " + constant);
        m_iConstant = constant;
    }

    public String sconstant() {
        return m_sConstant;
    }

    public void setsconstant(String constant) {
        System.out.println("sconstant = " + constant);
        m_sConstant = constant;
    }

    public double[] darr() {
        return m_darr;
    }

    public void setdarr(double[] darr) {
        System.out.println("darr = " + darr);
        m_darr = darr;
    }

    public String[] sarr() {
        return m_sarr;
    }

    public void setsarr(String[] sarr) {
        for (int i = 0; i < sarr.length; i++) {
            String name = sarr[i];
            System.out.print("sarr = " + name);
            System.out.print(" ");
        }
        System.out.println("");
        m_sarr = sarr;
    }
}