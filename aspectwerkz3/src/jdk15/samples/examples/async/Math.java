/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.async;

// static import for easier use of inner aspect Annotations @Async and @Service
import examples.async.AsyncAspect.Async;
import examples.async.AsyncAspect.Service;

import java.lang.reflect.Method;

import org.codehaus.aspectwerkz.annotation.Annotations;
import org.codehaus.aspectwerkz.annotation.Annotation;

/**
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
@Service
public class Math {

    @Async(timeout=5)
    public void asyncAdd(int a, int b) {
        System.out.printf(
                "[ %s ] %d + %d = %d\n",
                Thread.currentThread().getName(),
                a, b, (a+b));
    }

    public void substract(int a, int b) {
        System.out.printf(
                "[ %s ] %d - %d = %d\n",
                Thread.currentThread().getName(),
                a, b, (a-b)
        );
    }

    public static void main(String args[]) throws Throwable {
        Math math = new Math();
        Method method = Math.class.getDeclaredMethod("asyncAdd", new Class[]{int.class, int.class});
        AsyncAnnotationProxy ann = (AsyncAnnotationProxy)Annotations.getAnnotation(
                "examples.async.AsyncAspect$Async", method
        );
        System.out.println("ann.getName() = " + ann.getName());
        System.out.println("ann.timeout() = " + ann.timeout());

        math.asyncAdd(2, 4);
        math.asyncAdd(2, 5);
        math.asyncAdd(2, 6);
        math.asyncAdd(4, 4);
        math.asyncAdd(8, 4);


        math.substract(2, 4);
        math.substract(2, 5);
        math.substract(2, 6);
        math.substract(4, 4);
        math.substract(8, 4);
    }


}
