/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.ClassFormatException;

import java.util.Iterator;
import java.util.ArrayList;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Wraps BCEL ClassGen
 *
 * Note: derived from JMangler UnextendableClassSet, needs refactoring
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @version $Id: AspectWerkzUnextendableClassSet.java,v 1.2 2003-07-23 14:20:31 avasseur Exp $
 */
public class AspectWerkzUnextendableClassSet {

    private String name;

    public ClassGen getClassGen() {
        //@todo remove useless iteration
        return cg;
    }

    private ClassGen cg;


    public AspectWerkzUnextendableClassSet(String name, byte[] bytecode) throws IOException, ClassFormatException {
        this.name = name;
        this.cg = fromByte(name, bytecode);
    }

    public Iterator getIteratorForTransformableClasses() {
        ArrayList al = new ArrayList(1);
        al.add(cg);
        return al.iterator();
    }

    public byte[] getBytecode() {
        return cg.getJavaClass().getBytes();
    }

    public static ClassGen fromByte(String name, byte[] bytecode) throws IOException, ClassFormatException {
        ClassParser parser = new ClassParser(new ByteArrayInputStream(bytecode), "<generated>");
        return new ClassGen(parser.parse());
    }

}
