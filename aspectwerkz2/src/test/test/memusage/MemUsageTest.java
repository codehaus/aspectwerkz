/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.memusage;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.net.URLClassLoader;
import java.net.URL;
import java.io.File;
import java.util.List;
import java.util.ArrayList;

import org.codehaus.aspectwerkz.hook.impl.WeavingClassLoader;
import org.apache.bcel.Constants;
import org.apache.bcel.generic.*;

/**
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @TODO: Port to javassist (and hack the attribute def)
 * <p/>
 * XML definition:
 * <p/>
 * <advice-def name="memUsage" class="test.xmldef.memusage.MyAroundAdvice"/> <aspect name="memUsage"> <pointcut-def
 * name="pcHello" type="method" pattern="* test.xmldef.memusage.Hello+.sayHello*(..)"/> <bind-advice pointcut="pcHello">
 * <advice-ref name="memUsage"/> </bind-advice> </aspect>
 * <p/>
 * <p/>
 * This test can create as many classes as needed. Classes are dumped in _temp dir. A single test loads classFactor
 * classes and creates and invokes instanceFactor instances of each Use CLASS_KSIZE and INSTANCE_KSIZE to adapt the
 * object sizes.
 * <p/>
 * Eg: with 50 class and 10 instance with KSize of 10 for each this leads to 50 * 10k + 50*10 * 10k = apprx 5.5 M
 * <p/>
 * If isClassCache and isInstanceCache are set, classes and instances cannot be GC.
 * <p/>
 * It is possible to run offline mode on the dumped class in _temp as well to check the GC usage.
 */
public class MemUsageTest extends TestCase {

    private static boolean areClassWritten = false;

    /**
     * a statci byte[] array for generated class to have a larger mem fooprint
     */
    private final static int CLASS_KSIZE = 10;

    /**
     * a instance array field for generated class to have a larger mem fooprint
     */
    private final static int INSTANCE_KSIZE = 10;

    /**
     * each generated class has this number of sayHello<XX>() instance methods
     */
    private final static int HELLO_METHOD_COUNT = 30;

    /**
     * if set, created class are kept to avoid GC during the test
     */
    private boolean isClassCache = true;

    /**
     * if set, created instances are kept to avoid GC during the test
     */
    private boolean isInstanceCache = true;

    /**
     * number of class HelloXX created thru bcel for the test
     */
    private int classFactor;

    /**
     * number of instance of each HelloXX class instanciated for the test
     */
    private int instanceFactor;

    /**
     * class cache
     */
    private List classCache = new ArrayList();

    /**
     * instance cache
     */
    private List instanceCache = new ArrayList();

    /**
     * Create a new test
     */
    public MemUsageTest(String s, int classFactor, int instanceFactor, boolean isClassCache, boolean isInstanceCache) {
        super(s);
        this.classFactor = classFactor;
        this.instanceFactor = instanceFactor;
        this.isClassCache = isClassCache;
        this.isInstanceCache = isInstanceCache;
    }

    /**
     * Creates a Hello implementation class file
     *
     * @param dir       where to store generated file
     * @param className
     */
    private void createClassFile(String dir, String className) {
        // class classNameXX implements Hello
        ClassGen cg = new ClassGen(
                className, "java.lang.Object",
                "<generated>", Constants.ACC_PUBLIC | Constants.ACC_SUPER,
                new String[]{Hello.class.getName()}
        );
        ConstantPoolGen cp = cg.getConstantPool();
        InstructionFactory factory = new InstructionFactory(cg);

        // private byte[] buffer = new byte[XXX];
        FieldGen field = new FieldGen(Constants.ACC_PRIVATE, new ArrayType(Type.BYTE, 1), "buffer", cp);
        cg.addField(field.getField());

        // private static byte[] sbuffer = new byte[XXX];
        field = new FieldGen(Constants.ACC_PRIVATE | Constants.ACC_STATIC, new ArrayType(Type.BYTE, 1), "sbuffer", cp);
        cg.addField(field.getField());

        // <init> to initialize buffer field
        InstructionList il = new InstructionList();
        MethodGen method = new MethodGen(
                Constants.ACC_PUBLIC, Type.VOID, Type.NO_ARGS,
                new String[]{}, "<init>", className, il, cp
        );
        il.append(factory.createLoad(Type.OBJECT, 0));
        il.append(
                factory.createInvoke("java.lang.Object", "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL)
        );
        il.append(factory.createLoad(Type.OBJECT, 0));
        il.append(new PUSH(cp, INSTANCE_KSIZE * 1000));
        il.append(factory.createNewArray(Type.BYTE, (short)1));
        il.append(factory.createFieldAccess(className, "buffer", new ArrayType(Type.BYTE, 1), Constants.PUTFIELD));
        il.append(factory.createReturn(Type.VOID));
        method.setMaxStack();
        method.setMaxLocals();
        cg.addMethod(method.getMethod());
        il.dispose();

        // <clinit> to initialize sbuffer field
        il = new InstructionList();
        method =
        new MethodGen(Constants.ACC_STATIC, Type.VOID, Type.NO_ARGS, new String[]{}, "<clinit>", className, il, cp);
        il.append(new PUSH(cp, CLASS_KSIZE * 1000));
        il.append(factory.createNewArray(Type.BYTE, (short)1));
        il.append(factory.createFieldAccess(className, "sbuffer", new ArrayType(Type.BYTE, 1), Constants.PUTSTATIC));
        il.append(factory.createReturn(Type.VOID));
        method.setMaxStack();
        method.setMaxLocals();
        cg.addMethod(method.getMethod());
        il.dispose();

        // sayHello<XX> 0..()
        for (int i = 0; i < HELLO_METHOD_COUNT; i++) {
            method = new MethodGen(
                    Constants.ACC_PUBLIC, // access flags
                    Type.STRING, // return type
                    new Type[]{}, // arg type
                    new String[]{}, // arg names
                    "sayHello" + i, className, // method, class
                    il, cp
            );
            il.append(new PUSH(cp, "sayHello" + i));
            il.append(factory.createReturn(Type.STRING));
            method.setInstructionList(il);
            method.setMaxLocals();
            method.setMaxStack();
            cg.addMethod(method.getMethod());
            il.dispose();
        }

        // dump in dir
        try {
            cg.getJavaClass().dump(dir + File.separator + className.replace('.', File.separatorChar) + ".class");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Assumes cl classloader has access to classPrefix + 1..classFactor classes Call operation on them Each call is a
     * JUnit assertion
     */
    private void callClassesOneByOne(ClassLoader cl, String classPrefix) throws Exception {
        Class klass = null;
        Hello instance = null;
        for (int i = 1; i <= classFactor; i++) {
            klass = Class.forName(classPrefix + i, true, cl);
            if (isClassCache) {
                classCache.add(klass);
            }
            for (int j = 1; j <= instanceFactor; j++) {
                instance = (Hello)klass.newInstance();
                if (isInstanceCache) {
                    instanceCache.add(instance);
                }
                for (int k = 0; k < HELLO_METHOD_COUNT; k++) {
                    //System.out.print(":");
                    assertEquals(
                            "before sayHello" + k + " after",
                            klass.getMethod("sayHello" + k, new Class[]{}).invoke(instance, new Object[]{})
                    );
                    ///*no aspect*/assertEquals("sayHello"+k, klass.getMethod("sayHello"+k, new Class[]{}).invoke(instance, new Object[]{}));
                }
            }
        }
    }

    /**
     * Continue calling class instances Can be used with profiling tool to check memory fooprint
     */
    private void continueCalls() {
        if (!isInstanceCache) {
            return;
        }
        Hello instance = null;
        while (true) {
            for (int i = 0; i < instanceCache.size(); i++) {
                instance = (Hello)instanceCache.get(i);
                for (int k = 0; k < HELLO_METHOD_COUNT; k++) {
                    try {
                        assertEquals(
                                "before sayHello" + k + " after",
                                instance.getClass().getMethod("sayHello" + k, new Class[]{}).invoke(
                                        instance, new Object[]{}
                                )
                        );
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                Thread.sleep(200);
                System.out.print(".");
            }
            catch (Exception e) {
                ;
            }
        }
    }

    /**
     * Release cached instances and classes
     */
    private void releaseCache() {
        while (!instanceCache.isEmpty()) {
            instanceCache.remove(0);
        }
        while (!classCache.isEmpty()) {
            classCache.remove(0);
        }
    }

    /**
     * Creates classFactor classes in dir whith name classPrefix + 1..classFactor
     */
    private void createClassFiles(String dir, String classPrefix) {
        for (int i = 1; i <= classFactor; i++) {
            createClassFile(dir, classPrefix + i);
        }
    }

    /**
     * Run with forced weaving thru custom classloader -Daspectwerkz.definition.file=src\test\test-xmldef.xml
     * -Daspectwerkz.transform.verbose=yes -Daspectwerkz.transform.dump=test..*
     */
    public void runThruWeavingClassLoader() throws Exception {
        ClassLoader cl = new WeavingClassLoader(
                new URL[]{(new File("_temp")).toURL()}, ClassLoader.getSystemClassLoader()
        );
        createClassFiles("_temp", "atest");
        long ms = System.currentTimeMillis();
        callClassesOneByOne(cl, "atest");
        System.out.println("completed in: " + (System.currentTimeMillis() - ms));
        //continueCalls();// uncomment me if needed
        releaseCache();// uncomment me if needed
    }

    /**
     * Run as normal Is weaved thru online mode
     */
    public void runThruStandardClassLoader() throws Exception {
        ClassLoader cl = new URLClassLoader(
                new URL[]{(new File("_temp")).toURL()}, ClassLoader.getSystemClassLoader()
        );
        createClassFiles("_temp", "HelloClass");
        long ms = System.currentTimeMillis();
        callClassesOneByOne(cl, "HelloClass");
        System.out.println("completed in: " + (System.currentTimeMillis() - ms));
        releaseCache();
    }

    /**
     * Test hook
     */
    public void testLongRun() throws Exception {
        runThruStandardClassLoader();
    }

    public synchronized void setUp() {
        if (!areClassWritten) {
            //System.out.println("creating");
            createClassFile("_temp", "HelloClass");
            areClassWritten = true;
            //System.out.println("created");
        }
    }

    public void tearDown() {
        //todo clean _temp
    }

    //-- junit hooks --//

    public static void main(String[] args) throws Throwable {
        //junit.textui.TestRunner.run(suite());

        // uncomment for inside IDE use
        MemUsageTest me = new MemUsageTest("test", 10, 20, true, true);
        me.createClassFiles("_temp", "HelloClass");
        me.runThruWeavingClassLoader();
    }

    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new MemUsageTest("testLongRun", 10, 20, true, true));
        return suite;
    }


}
