/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.hook;

import java.io.*;

/**
 * Redirects stream using an internal buffer of size 2048
 * Used to redirect std(in/out/err) streams of the target VM
 *
 * Inspired from Ant StreamPumper class, which seems better than the JPDA Sun demo
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @version $Id: StreamRedirectThread.java,v 1.1.2.1 2003-07-16 08:09:54 avasseur Exp $
 */
class StreamRedirectThread extends Thread {

    private static final int BUFFER_SIZE = 2048;
    private static final int SLEEP = 5;

    private InputStream is;
    private OutputStream os;

    public StreamRedirectThread(String name, InputStream is, OutputStream os) {
        super(name);
        setPriority(Thread.MAX_PRIORITY-1);
        this.is = is;
        this.os = os;
    }

    public void run() {
        byte buf[] = new byte[BUFFER_SIZE];
        int i;
        try {
            while((i = is.read(buf)) > 0) {
                os.write(buf, 0, i);
                try {
                    Thread.sleep(SLEEP);
                } catch(InterruptedException e) { ; }
             }
        } catch (Exception e) {
            ;
        } finally {
            ;//notify();
        }
     }



/*
    public StreamRedirectThread(String name, InputStream in, OutputStream out) {
        super(name);
        this.in = new InputStreamReader(in);
        this.out = new OutputStreamWriter(out);
        setPriority(Thread.MAX_PRIORITY-1);
    }

    public void run() {
        try {
            char[] cbuf = new char[BUFFER_SIZE];
            int count;
            System.out.println("read" + this.getName());
            while ((count = in.read(cbuf, 0, BUFFER_SIZE)) >= 0) {
                System.out.println("write" + this.getName());
                out.write(cbuf, 0, count);
                out.flush();
            }
            out.flush();
        } catch (IOException e) {
            System.err.println("Child I/O Transfer failed - " + e);
        } finally {
            try {
                out.close();
                in.close();
            } catch(IOException e) {
                ;
            }
        }
    }
*/
}
