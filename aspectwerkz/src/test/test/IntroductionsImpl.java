package test;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @version $Id: IntroductionsImpl.java,v 1.3 2003-06-20 06:14:27 jboner Exp $
 */
public class IntroductionsImpl implements Introductions {

    public void method() throws RuntimeException {
    }

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
