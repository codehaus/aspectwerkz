/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.xmldef.clapp;

import java.util.Hashtable;
import java.util.Arrays;
import java.io.ByteArrayInputStream;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.generic.ClassGen;

import org.codehaus.aspectwerkz.hook.ClassPreProcessor;


/**
 * Add marker interface java.util.EventListener to processed class
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class AddInterfacePreProcessor implements ClassPreProcessor {

    private void log(String s) {
        System.out.println(Thread.currentThread().getName() + ": AddInterfacePreProcessor: " + s);
    }

    public void initialize(Hashtable hashtable) {
        ;
    }

    public byte[] preProcess(String klass, byte abyte[], ClassLoader caller) {
        try {
            log(klass);

            // build the ClassGen
            ClassParser parser = new ClassParser(new ByteArrayInputStream(abyte), "<generated>");//@todo is this needed _"+klass+">");
            ClassGen cg = new ClassGen(parser.parse());

            // instrument
            if ( ! cg.isInterface() && ! Arrays.asList(cg.getInterfaceNames()).contains("java.util.EventListener") )
                cg.addInterface("java.util.EventListener");

            try {
                cg.getJavaClass().dump("_dump/"+klass.replace('.', '/')+".class");
            } catch (Exception e) {
                System.err.println("failed to dump " + klass);
                e.printStackTrace();
            }


            return cg.getJavaClass().getBytes();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

}
