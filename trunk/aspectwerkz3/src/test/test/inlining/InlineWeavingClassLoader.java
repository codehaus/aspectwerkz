/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.inlining;

import sun.misc.Resource;
import sun.misc.URLClassPath;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class InlineWeavingClassLoader extends URLClassLoader {

    public InlineWeavingClassLoader(URL[] arg0, ClassLoader loader) {
        super(arg0, loader);
    }

    protected Class findClass(String name) throws ClassNotFoundException {
        String path = name.replace('.', '/').concat(".class");
        Resource res = new URLClassPath(getURLs()).getResource(path, false);
        if (res != null) {
            try {
                byte[] b = res.getBytes();
                InliningPreProcessor pp = new InliningPreProcessor();
                byte[] transformed = pp.preProcess(name, b, this);
                return defineClass(name, transformed, 0, transformed.length);
            } catch (IOException e) {
                throw new ClassNotFoundException(e.getMessage());
            }
        } else {
            throw new ClassNotFoundException(name);
        }
    }

    public static void main(final String args[]) throws Exception {
        String path = System.getProperty("java.class.path");
        ArrayList paths = new ArrayList();
        StringTokenizer st = new StringTokenizer(path, File.pathSeparator);
        while (st.hasMoreTokens()) {
            paths.add((new File(st.nextToken())).getCanonicalFile().toURL());
        }

        ClassLoader cl = new InlineWeavingClassLoader(
                (URL[])paths.toArray(new URL[]{}),
                ClassLoader.getSystemClassLoader().getParent()
        );
        Thread.currentThread().setContextClassLoader(cl);
        String s = args[0];
        String[] args1 = new String[args.length - 1];
        if (args1.length > 0) {
            System.arraycopy(args, 1, args1, 0, args.length - 1);
        }
        Class class1 = cl.loadClass(s);
        Method method = class1.getMethod("main", new Class[]{String[].class});
        method.invoke(null, new Object[]{args1});
    }
}
