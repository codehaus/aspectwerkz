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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package examples.logging;

public class Target {

    public Target() {}
    public Target(int i) {}
    public Target(String i) {}

    /**
     * @advice:method log
     */
    public static void toLog1() {
        System.out.println("\tinvoking toLog1");
    }

    /**
     * @advice:method log
     */
    private void toLog2(java.lang.String arg) {
        System.out.println("\tinvoking toLog2");
    }

    /**
     * @advice:method log
     */
    private String toLog3() {
        System.out.println("\tinvoking toLog3");
        return "result";
    }

    public static void main(String[] args) {
        Target target = new Target();
        Target.toLog1();
        target.toLog2("parameter");
        target.toLog3();
    }

    public static class Inner {
        public static void log() {
            System.out.println("Inner.log");
        }
    }
}
