package test;

import java.io.Serializable;

public interface Introductions extends Serializable {
    void method();

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
