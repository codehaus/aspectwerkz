/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.attribdef.aspect;

import java.io.Serializable;
import test.attribdef.Introductions;

import org.codehaus.aspectwerkz.attribdef.aspect.Aspect;

/**
 * @Aspect perJVM
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class IntroductionTestAspect extends Aspect {

    // ============ Introductions ============

    /**
     * @Implements test.attribdef.ToBeIntroduced
     */
     Introductions introductions;

    /**
     * @Implements test.attribdef.ToBeIntroduced
     */
     Serializable serializable;

    /**
     * @Introduce introductions
     */
    public void method() throws RuntimeException {
    }

    /**
     * @Introduce introductions
     */
    public void noArgs() throws RuntimeException {
    }

    /**
     * @Introduce introductions
     */
    public long longArg(long arg) {
        return arg;
    }

    /**
     * @Introduce introductions
     */
    public int intArg(int arg) {
        return arg;
    }

    /**
     * @Introduce introductions
     */
    public short shortArg(short arg) {
        return arg;
    }

    /**
     * @Introduce introductions
     */
    public double doubleArg(double arg) {
        return arg;
    }

    /**
     * @Introduce introductions
     */
    public float floatArg(float arg) {
        return arg;
    }

    /**
     * @Introduce introductions
     */
    public byte byteArg(byte arg) {
        return arg;
    }

    /**
     * @Introduce introductions
     */
    public boolean booleanArg(boolean arg) {
        return arg;
    }

    /**
     * @Introduce introductions
     */
    public char charArg(char arg) {
        return arg;
    }

    /**
     * @Introduce introductions
     */
    public Object objectArg(Object arg) {
        return arg;
    }

    /**
     * @Introduce introductions
     */
    public String[] arrayArg(String[] arg) {
        return arg;
    }

    /**
     * @Introduce introductions
     */
    public int variousArguments1(String str, int i, float f, Object o, long l) throws RuntimeException {
        return str.hashCode() + i + (int)f + o.hashCode() + (int)l;
    }

    /**
     * @Introduce introductions
     */
    public int variousArguments2(float f, int i, String str1, Object o, long l, String str2) throws RuntimeException {
        return (int)f + i + str1.hashCode() + o.hashCode() + (int)l + str2.hashCode();
    }

    /**
     * @Introduce introductions
     */
    public void getVoid() throws RuntimeException {
    }

    /**
     * @Introduce introductions
     */
    public long getLong() throws RuntimeException {
        return 1L;
    }

    /**
     * @Introduce introductions
     */
    public int getInt() throws RuntimeException {
        return 1;
    }

    /**
     * @Introduce introductions
     */
    public short getShort() throws RuntimeException {
        return 1;
    }

    /**
     * @Introduce introductions
     */
    public double getDouble() throws RuntimeException {
        return 1.1D;
    }

    /**
     * @Introduce introductions
     */
    public float getFloat() throws RuntimeException {
        return 1.1F;
    }

    /**
     * @Introduce introductions
     */
    public byte getByte() throws RuntimeException {
        return Byte.parseByte("1");
    }

    /**
     * @Introduce introductions
     */
    public char getChar() throws RuntimeException {
        return 'A';
    }

    /**
     * @Introduce introductions
     */
    public boolean getBoolean() throws RuntimeException {
        return true;
    }
}
