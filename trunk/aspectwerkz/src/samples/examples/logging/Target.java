/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR and PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package examples.logging;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: Target.java,v 1.13 2003-07-19 20:36:17 jboner Exp $
 */
public class Target {

    public Target() {
        this(1);
    }

    public Target(int i) {
    }

    public Target(String i) {
    }

    /**
     * @aspectwerkz.joinpoint.controller examples.logging.DummyJoinPointController
     * @aspectwerkz.advice.method log
     * @aspectwerkz.advice.method log
     */
    public static void toLog1() {
        new Target().toLog2("parameter");
    }

    /**
     * @aspectwerkz.advice.method log
     */
    private void toLog2(java.lang.String arg) {
        new Target().toLog3();
    }

    /**
     * @aspectwerkz.advice.method log
     */
    private String toLog3() {
        return "result";
    }

    /**
     * @aspectwerkz.advice.method log
     */
    public static void main(String[] args) {
        Target.toLog1();
    }
}
