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

import org.codehaus.aspectwerkz.attribdef.Pointcut;
import org.codehaus.aspectwerkz.attribdef.aspect.Aspect;

/**
 * @Aspect perJVM
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class IntroductionTestAspect extends Aspect {

    // ============ Pointcuts ============

    /**
     * @Class test.attribdef.ToBeIntroduced
     */
    Pointcut classes;

    // ============ Introductions ============

    /**
     * @Implements classes
     */
     Introductions introductions;

    /**
     * @Implements classes
     */
     Serializable serializable;

    /**
     * @Introduction classes
     */
    public void method() throws RuntimeException {
    }

    /**
     * @Introduction classes
     */
    public void noArgs() throws RuntimeException {
    }

    /**
     * @Introduction classes
     */
    public long longArg(long arg) {
        return arg;
    }

    /**
     * @Introduction classes
     */
    public int intArg(int arg) {
        return arg;
    }

    /**
     * @Introduction classes
     */
    public short shortArg(short arg) {
        return arg;
    }

    /**
     * @Introduction classes
     */
    public double doubleArg(double arg) {
        return arg;
    }

    /**
     * @Introduction classes
     */
    public float floatArg(float arg) {
        return arg;
    }

    /**
     * @Introduction classes
     */
    public byte byteArg(byte arg) {
        return arg;
    }

    /**
     * @Introduction classes
     */
    public boolean booleanArg(boolean arg) {
        return arg;
    }

    /**
     * @Introduction classes
     */
    public char charArg(char arg) {
        return arg;
    }

    /**
     * @Introduction classes
     */
    public Object objectArg(Object arg) {
        return arg;
    }

    /**
     * @Introduction classes
     */
    public String[] arrayArg(String[] arg) {
        return arg;
    }

    /**
     * @Introduction classes
     */
    public int variousArguments1(String str, int i, float f, Object o, long l) throws RuntimeException {
        return str.hashCode() + i + (int)f + o.hashCode() + (int)l;
    }

    /**
     * @Introduction classes
     */
    public int variousArguments2(float f, int i, String str1, Object o, long l, String str2) throws RuntimeException {
        return (int)f + i + str1.hashCode() + o.hashCode() + (int)l + str2.hashCode();
    }

    /**
     * @Introduction classes
     */
    public void getVoid() throws RuntimeException {
    }

    /**
     * @Introduction classes
     */
    public long getLong() throws RuntimeException {
        return 1L;
    }

    /**
     * @Introduction classes
     */
    public int getInt() throws RuntimeException {
        return 1;
    }

    /**
     * @Introduction classes
     */
    public short getShort() throws RuntimeException {
        return 1;
    }

    /**
     * @Introduction classes
     */
    public double getDouble() throws RuntimeException {
        return 1.1D;
    }

    /**
     * @Introduction classes
     */
    public float getFloat() throws RuntimeException {
        return 1.1F;
    }

    /**
     * @Introduction classes
     */
    public byte getByte() throws RuntimeException {
        return Byte.parseByte("1");
    }

    /**
     * @Introduction classes
     */
    public char getChar() throws RuntimeException {
        return 'A';
    }

    /**
     * @Introduction classes
     */
    public boolean getBoolean() throws RuntimeException {
        return true;
    }
}
