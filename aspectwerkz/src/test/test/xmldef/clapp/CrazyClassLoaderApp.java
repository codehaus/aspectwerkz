/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.xmldef.clapp;

import java.net.URLClassLoader;
import java.net.URL;
import java.io.File;


/**
 * App loading lots of class in lots of threads
 *
 * Mandatory args = thread number, loop per thread, pause between loops<br/>
 * If no args are provided, defaults to 2, 5, 5ms.<br/>
 * <br/>
 * Each thread loop loads DummyClass thru a dedicated URLClassLoader (no parent) at each loop<br/>
 * test.xmldef.clapp.DummyClass and test.xmldef.clapp.ReentrantDummyClass must be in directory specified thru
 * -DDummyClass, defaults <i>ASPECTWERKZ_HOME</i>/target/test-classes
 * <br/>
 * During the DummyClass clinit, another class is loaded thru another URLClassLoader (no parent)
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class CrazyClassLoaderApp {

    private static final String DUMMYCLASS_LOCATION_PROP = "DummyClass";
    public static String DUMMYCLASS_LOCATION = System.getProperty(DUMMYCLASS_LOCATION_PROP);
    static {
        if (DUMMYCLASS_LOCATION == null)
            DUMMYCLASS_LOCATION = System.getProperty("ASPECTWERKZ_HOME")+File.separator+"target"+File.separator+"test-classes";
    }

    /**
     * log
     */
    private static void log(String s) {
        System.out.println(s);
    }

    /**
     * launch all thread and join() with them
     */
    public static void main(String args[]) throws Exception {
        int thread = 2;
        int count = 5;
        int mspause = 5;
        try {
            thread = Integer.parseInt(args[0]);
            count = Integer.parseInt(args[1]);
            mspause = Integer.parseInt(args[2]);
        } catch (Exception e) {
            ;
        }

        long start = System.currentTimeMillis();
        log("BEGIN:"+thread+":"+count+":"+mspause+":"+DUMMYCLASS_LOCATION);

        Thread[] threads = new Thread[thread];
        for (int i = 0; i < thread; i++) {
            Worker w = new Worker(count, mspause);
            w.setPriority(Thread.MAX_PRIORITY-1);
            w.start();
            log("started " + i);
            threads[i]=w;
        }

        for (int i=0; i<thread; i++) {
            threads[i].join();
            log("joined " + i);
        }

        log("END");
        log("( "+(int)(System.currentTimeMillis()-start)/1000+" s)");
        log("classes="+thread*count*2);
        System.exit(0);
    }

    private static class Worker extends Thread {

		public static transient int total = 0;

        int count = 10;
        long mspause = 1000;
        URL url = null;

        public Worker(int count, long mspause) {
            this.count = count;
            this.mspause = mspause;
            try {
                this.url = new java.io.File(DUMMYCLASS_LOCATION).toURL();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void run() {
            int i = 0;
            while (i<count) {
                try {
                    i++;
                    ClassLoader tmpLoader = new URLClassLoader(new URL[]{url}, null);
                    Class dummyClass = tmpLoader.loadClass("test.xmldef.clapp.DummyClass");
                    Object dummyInstance = dummyClass.newInstance();
                    total++;
                    log(total+" "+this.getName() + ":" + i + ":DumyClass.hashcode=" + dummyInstance.getClass().hashCode());
                    synchronized(this) {
                        wait(mspause);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
