/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test;

import java.io.Serializable;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public interface Introductions extends Serializable {

    void noArgs();

    long longArg(long arg);

    int intArg(int arg);

    short shortArg(short arg);

    double doubleArg(double arg);

    float floatArg(float arg);

    byte byteArg(byte arg);

    boolean booleanArg(boolean arg);

    char charArg(char arg);

    Object objectArg(Object arg);

    String[] arrayArg(String[] arg);

    void getVoid() throws RuntimeException;

    long getLong() throws RuntimeException;

    int getInt() throws RuntimeException;

    short getShort() throws RuntimeException;

    double getDouble() throws RuntimeException;

    float getFloat() throws RuntimeException;

    byte getByte() throws RuntimeException;

    char getChar() throws RuntimeException;

    boolean getBoolean() throws RuntimeException;

    int variousArguments1(String str, int i, float f, Object o, long l) throws RuntimeException;

    int variousArguments2(float f, int i, String str1, Object o, long l, String str2) throws RuntimeException;
}
