/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.attribdef.aspect;

import java.io.Serializable;
import org.codehaus.aspectwerkz.attribdef.aspect.Aspect;
import test.attribdef.Introductions;

/**
 * @Aspect perJVM
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class IntroductionTestAspect extends Aspect {

    // ============ Introductions ============

    /**
     * @Implements test.attribdef.ToBeIntroduced
     */
     Serializable serzializable;

    /**
     * @Introduce test.attribdef.ToBeIntroduced
     */
    private class MyImpl implements Serializable, Introductions {
        //TODO: allow naming of mixin instead of innerClass FQN

        /**
         * a no arg constructor is needed since mixin is private
         */
        public MyImpl() {};

        public void noArgs() throws RuntimeException {
        }

        public long longArg(long arg) {
            return arg;
        }

        public int intArg(int arg) {
            return arg;
        }

        public short shortArg(short arg) {
            return arg;
        }

        public double doubleArg(double arg) {
            return arg;
        }

        public float floatArg(float arg) {
            return arg;
        }

        public byte byteArg(byte arg) {
            return arg;
        }

        public boolean booleanArg(boolean arg) {
            return arg;
        }

        public char charArg(char arg) {
            return arg;
        }

        public Object objectArg(Object arg) {
            return arg;
        }

        public String[] arrayArg(String[] arg) {
            return arg;
        }

        public int variousArguments1(String str, int i, float f, Object o, long l) throws RuntimeException {
            return str.hashCode() + i + (int)f + o.hashCode() + (int)l;
        }

        public int variousArguments2(float f, int i, String str1, Object o, long l, String str2) throws RuntimeException {
            return (int)f + i + str1.hashCode() + o.hashCode() + (int)l + str2.hashCode();
        }

        public void getVoid() throws RuntimeException {
        }

        public long getLong() throws RuntimeException {
            return 1L;
        }

        public int getInt() throws RuntimeException {
            return 1;
        }

        public short getShort() throws RuntimeException {
            return 1;
        }

        public double getDouble() throws RuntimeException {
            return 1.1D;
        }

        public float getFloat() throws RuntimeException {
            return 1.1F;
        }

        public byte getByte() throws RuntimeException {
            return Byte.parseByte("1");
        }

        public char getChar() throws RuntimeException {
            return 'A';
        }

        public boolean getBoolean() throws RuntimeException {
            return true;
        }
    }

    /**
     * Other implementation
     * For now explicit implements is needed (extends is not enough - bug in swapping)
     */
    public class MyOtherImpl extends MyImpl implements Introductions, Serializable {

        public void noArgs() throws RuntimeException {
        }

        public long longArg(long arg) {
            return arg;
        }

        /**
         * used by test case
         */
        public int intArg(int arg) {
            return -1 * arg;
        }

        public short shortArg(short arg) {
            return arg;
        }

        public double doubleArg(double arg) {
            return arg;
        }

        public float floatArg(float arg) {
            return arg;
        }

        public byte byteArg(byte arg) {
            return arg;
        }

        public boolean booleanArg(boolean arg) {
            return arg;
        }

        public char charArg(char arg) {
            return arg;
        }

        public Object objectArg(Object arg) {
            return arg;
        }

        public String[] arrayArg(String[] arg) {
            return arg;
        }

        public int variousArguments1(String str, int i, float f, Object o, long l) throws RuntimeException {
            return str.hashCode() + i + (int)f + o.hashCode() + (int)l;
        }

        public int variousArguments2(float f, int i, String str1, Object o, long l, String str2) throws RuntimeException {
            return (int)f + i + str1.hashCode() + o.hashCode() + (int)l + str2.hashCode();
        }

        public void getVoid() throws RuntimeException {
        }

        public long getLong() throws RuntimeException {
            return 1L;
        }

        public int getInt() throws RuntimeException {
            return -1;
        }

        public short getShort() throws RuntimeException {
            return 1;
        }

        public double getDouble() throws RuntimeException {
            return 1.1D;
        }

        public float getFloat() throws RuntimeException {
            return 1.1F;
        }

        public byte getByte() throws RuntimeException {
            return Byte.parseByte("1");
        }

        public char getChar() throws RuntimeException {
            return 'A';
        }

        public boolean getBoolean() throws RuntimeException {
            return true;
        }
    }

}
