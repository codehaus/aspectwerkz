package test.clapp;

import junit.framework.TestCase;

import java.net.URL;
import java.lang.reflect.Method;

import org.codehaus.aspectwerkz.compiler.VerifierClassLoader;

public class CustomClassLoaderTest extends TestCase {

    private static String targetPath = CustomClassLoaderTest.class.getClassLoader().getResource("test/clapp/Target.class").toString();
    static {
        targetPath = targetPath.substring(0, targetPath.indexOf("test/clapp/Target.class"));
    }

    public void testCustomClassLoaderWeaving() {
        try {
            VerifierClassLoader cl = new VerifierClassLoader(
                    new URL[]{new URL(targetPath)},
                    ClassLoader.getSystemClassLoader());

            Class target = cl.loadClass("test.clapp.Target");
            assertEquals(target.getClassLoader().hashCode(), cl.hashCode());
            Method m = target.getMethod("callme", new Class[]{});
            String res = (String) m.invoke(target.newInstance(), new Object[]{});
            assertEquals("before call after", res);
        } catch (Throwable t) {
            t.printStackTrace();
            fail(t.getMessage());
        }
    }

    public static void main(String a[]) {
        CustomClassLoaderTest me = new CustomClassLoaderTest();
        me.testCustomClassLoaderWeaving();
    }

}
