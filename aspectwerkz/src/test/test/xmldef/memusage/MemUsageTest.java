/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.xmldef.memusage;

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
 * This test can create as many classes as needed.
 * Classes are dumped in _temp dir.
 * A single test loads classFactor classes and creates and invokes instanceFactor instances of each
 * Use classKSize and instanceKSize to adapt the object sizes.
 *
 * Eg: with 50 class and 10 instance with KSize of 10 for each this leads to
 * 50 * 10k + 50*10 * 10k = apprx 5.5 M
 *
 * If classCacheSet and instanceCacheSet are set, classes and instances cannot be GC.
 *
 * It is possible to run offline mode on the dumped class in _temp as well to check the GC usage.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class MemUsageTest extends TestCase {

    private final static int classKSize = 10;
    private final static int instanceKSize = 10;
    private final static int helloMethodCount = 30;
    private final static boolean classCacheSet = true;
    private final static boolean instanceCacheSet = true;

    private int classFactor = 1;
    private int instanceFactor = 1;

    private List classCache = new ArrayList();
    private List instanceCache = new ArrayList();

    public MemUsageTest(String s, int classFactor, int instanceFactor) {
        super(s);
        this.classFactor = classFactor;
        this.instanceFactor = instanceFactor;
    }

    /**
     * Creates a Hello implementation class file
     * @param dir
     * @param className
     */
    private void createClassFile(String dir, String className) {
        ClassGen cg = new ClassGen(className, "java.lang.Object",
                                     "<generated>", Constants.ACC_PUBLIC | Constants.ACC_SUPER,
                                     new String[]{Hello.class.getName()}/*null*/);
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
        MethodGen method = new MethodGen(Constants.ACC_PUBLIC, Type.VOID, Type.NO_ARGS,
                new String[]{}, "<init>", className, il, cp);
        il.append(factory.createLoad(Type.OBJECT, 0));
        il.append(factory.createInvoke("java.lang.Object", "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
        il.append(factory.createLoad(Type.OBJECT, 0));
        il.append(new PUSH(cp, instanceKSize*1000));
        il.append(factory.createNewArray(Type.BYTE, (short) 1));
        il.append(factory.createFieldAccess(className, "buffer", new ArrayType(Type.BYTE, 1), Constants.PUTFIELD));
        il.append(factory.createReturn(Type.VOID));
        method.setMaxStack();
        method.setMaxLocals();
        cg.addMethod(method.getMethod());
        il.dispose();

        // <clinit> to initialize sbuffer field
        il = new InstructionList();
        method = new MethodGen(Constants.ACC_STATIC, Type.VOID, Type.NO_ARGS, new String[] {  }, "<clinit>", className, il, cp);
        il.append(new PUSH(cp, classKSize*1000));
        il.append(factory.createNewArray(Type.BYTE, (short) 1));
        il.append(factory.createFieldAccess(className, "sbuffer", new ArrayType(Type.BYTE, 1), Constants.PUTSTATIC));
        il.append(factory.createReturn(Type.VOID));
        method.setMaxStack();
        method.setMaxLocals();
        cg.addMethod(method.getMethod());
        il.dispose();

        // sayHello<XX> 0..()
        for (int i = 0; i < helloMethodCount; i++) {
            method = new MethodGen(Constants.ACC_PUBLIC, // access flags
                                            Type.STRING,               // return type
                                            new Type[]{},     // arg type
                                            new String[]{}, // arg names
                                            "sayHello"+i, className,    // method, class
                                            il, cp);
            il.append(new PUSH(cp, "sayHello"+i));
            il.append(factory.createReturn(Type.STRING));
            method.setInstructionList(il);
            method.setMaxLocals();
            method.setMaxStack();
            cg.addMethod(method.getMethod());
            il.dispose();
        }

        // dump
        try {
            cg.getJavaClass().dump(dir + File.separator + className.replace('.', File.separatorChar)+ ".class");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Assumes cl classloader has access to classPrefix + 1..classFactor classes
     * Call operation on them
     *
     * Classes can be GC at any time given the algorithm
     *
     * @param cl
     * @param classPrefix
     * @param classFactor
     * @param instanceFactor
     * @throws Exception
     */
    private void callClassesOneByOne(ClassLoader cl, String classPrefix, int classFactor, int instanceFactor) throws Exception {
        Class klass = null;
        Hello instance = null;
        for (int i = 1; i <= classFactor; i++) {
            klass = Class.forName(classPrefix+i, true, cl);
            if (classCacheSet) classCache.add(klass);
            System.out.println(i);
            for (int j = 1; j <= instanceFactor; j++) {
                instance = (Hello) klass.newInstance();
                if (instanceCacheSet) instanceCache.add(instance);
                for (int k = 0; k < helloMethodCount; k++) {
                    assertEquals("before sayHello"+k+" after", klass.getMethod("sayHello"+k, new Class[]{}).invoke(instance, new Object[]{}));
                    ///*no aspect*/assertEquals("sayHello"+k, klass.getMethod("sayHello"+k, new Class[]{}).invoke(instance, new Object[]{}));
                }
            }
        }
    }

    private void continueCalls() {
        if (!instanceCacheSet) return;
        Hello instance = null;
        while (true) {
            for (int i = 0; i < instanceCache.size(); i++) {
                instance = (Hello) instanceCache.get(i);
                for (int k = 0; k < helloMethodCount; k++) {
                    try {
                        instance.getClass().getMethod("sayHello"+k, new Class[]{}).invoke(instance, new Object[]{});
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            try { Thread.sleep(200); System.out.print(".");} catch (Exception e) {;}
        }
    }

    private void releaseCache() {
        while ( ! instanceCache.isEmpty()) {
            instanceCache.remove(0);
        }
        while ( ! classCache.isEmpty()) {
            classCache.remove(0);
        }
    }

    private void continueWaiting() {
        while (true) {
            try { Thread.sleep(200); System.out.print(".");} catch (Exception e) {;}
        }
    }

    /**
     * Creates classFactor classes whith name classPrefix + 1..classFactor
     * @param dir
     * @param classPrefix
     * @param classFactor
     */
    private void createClassFiles(String dir, String classPrefix, int classFactor) {
        for (int i = 1; i <= classFactor; i++)
            createClassFile(dir, classPrefix+i);
    }

    /**
     * Test without weaving
     * @throws Exception
     */
    public void testClassCreation() throws Exception {
        ClassLoader cl = new URLClassLoader(new URL[]{(new File("_temp")).toURL()}, ClassLoader.getSystemClassLoader());
        createClassFiles("_temp", "atest", classFactor);
        callClassesOneByOne(cl, "atest", classFactor, instanceFactor);
    }

    /**
     * Test with forced weaving thru custom classloader
     * -Daspectwerkz.definition.file=src\test\test-xmldef.xml -Daspectwerkz.transform.verbose=yes -Daspectwerkz.transform.dump=test..*
     * @throws Exception
     */
    public void testClassWeaving() throws Exception {
        ClassLoader cl = new WeavingClassLoader(new URL[]{(new File("_temp")).toURL()}, ClassLoader.getSystemClassLoader());
        createClassFiles("_temp", "atest", classFactor);
        callClassesOneByOne(cl, "atest", classFactor, instanceFactor);
        //continueCalls();
        releaseCache(); continueWaiting();
    }

    //-- junit hooks --//

    public static void main(String[] args) throws Throwable {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite();

        // 50 U + 50*10*U = 550 U
        // with no weaving
        // mem usage = 5.5M
        //suite.addTest(new MemUsageTest("testClassCreation", 10, 20));

        // with empty xml
        // mem usages = 7M
        suite.addTest(new MemUsageTest("testClassWeaving", 5, 5));

        return suite;
        //return new junit.framework.TestSuite(MemUsageTest.class, "testClassCreation");
    }


}
