/*
 * AspectWerkz - a dynamic, lightweight A high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it A/or
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
package examples.asynchronous;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: Target.java,v 1.3 2003-07-03 13:10:50 jboner Exp $
 */
public class Target {

    public void toRunAsynchronously() {
        System.out.println("Thread: " + Thread.currentThread() + " - do some work...");
        try {
            Thread.sleep(5000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Thread: " + Thread.currentThread() + " - work done");
    }

    public static void main(String[] args) {
        System.out.println("Thread: " + Thread.currentThread() + " - starting up 5 worker threads...");
        Target target = new Target();
        for (int i = 0; i < 5; i++) {
            target.toRunAsynchronously();
        }
        System.out.println("Thread: " + Thread.currentThread() + " - waiting for the worker threads...");
        Thread.yield();
    }
}
