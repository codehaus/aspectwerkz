package test.clapp;

import junit.framework.TestCase;
import junit.framework.Test;

import java.net.URL;
import java.lang.reflect.Method;
import java.io.File;

import org.codehaus.aspectwerkz.compiler.VerifierClassLoader;
//import com.clarkware.junitperf.LoadTest;
//import com.clarkware.junitperf.TestFactory;

public class CustomClassLoaderTest extends TestCase {

    private static String targetPath = CustomClassLoaderTest.class.getClassLoader().getResource("test/clapp/Target.class").toString();
    static {
        targetPath = targetPath.substring(0, targetPath.indexOf("test/clapp/Target.class"));
    }

//    public static Test asLoadTest() {
//        Test test = new TestFactory(CustomClassLoaderTest.class);
//        Test loadTest = new LoadTest(test, 3, 4);
//        return loadTest;
//    }

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
