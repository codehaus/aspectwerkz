/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test;

import junit.framework.TestCase;

import java.io.Serializable;
import java.io.ObjectStreamClass;
import java.lang.reflect.Modifier;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtNewMethod;
import javassist.CtField;
import javassist.CtConstructor;
import org.codehaus.aspectwerkz.transform.delegation.JavassistHelper;
import org.objectweb.asm.Constants;

/**
 * Test the Javassist based SerialVerUid computation.
 * See AW-244 for synthetic members bug.
 * 
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class SerialVerUidTest extends TestCase implements Serializable {

    public Object[] someMethod() {return null;}

    public SerialVerUidTest() {super();}

    public SerialVerUidTest(Object[] foo) {;}

    protected static final int someField = 32;

    public void testSerialVerUid() throws Throwable {
        Class javaClass = this.getClass();
        long javaSerialVerUid = ObjectStreamClass.lookup(javaClass).getSerialVersionUID();

        CtClass javassistClass = ClassPool.getDefault().get(this.getClass().getName());
        long javassistSerialVerUid = JavassistHelper.calculateSerialVerUid(javassistClass);

        assertEquals(javaSerialVerUid, javassistSerialVerUid);
    }

    public void testSerialVerUidSynthetic() throws Throwable {
        CtClass javassistClass = ClassPool.getDefault().get(this.getClass().getName());
        // build a class with synthetic method, field, ctor
        javassistClass.setName(this.getClass().getName()+"Generated");
        int syntheticModifier = Constants.ACC_SYNTHETIC | Modifier.PUBLIC;
        javassistClass.addMethod(CtNewMethod.make(syntheticModifier, CtClass.intType, "syntheticDo", new CtClass[]{}, new CtClass[]{}, "{return 0;}", javassistClass));
        CtField field = new CtField(CtClass.intType, "syntheticField", javassistClass);
        field.setModifiers(syntheticModifier);
        javassistClass.addField(field);
        CtConstructor ctor = new CtConstructor(new CtClass[]{CtClass.intType}, javassistClass);
        ctor.setModifiers(syntheticModifier);
        ctor.setBody("{super();}");
        javassistClass.addConstructor(ctor);

        long javassistSerialVerUid = JavassistHelper.calculateSerialVerUid(javassistClass);

        Class javaClassGenerated = javassistClass.toClass();
        long javaSerialVerUid = ObjectStreamClass.lookup(javaClassGenerated).getSerialVersionUID();

        assertEquals(javaSerialVerUid, javassistSerialVerUid);

    }

}
