/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.annotation;

import org.codehaus.aspectwerkz.transform.AspectWerkzPreProcessor;
import org.codehaus.aspectwerkz.transform.inlining.AsmNullAdapter;
import org.codehaus.backport175.reader.bytecode.AnnotationReader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.CodeAdapter;
import org.objectweb.asm.attrs.Attributes;
import org.objectweb.asm.attrs.RuntimeVisibleAnnotations;
import junit.framework.TestCase;

import java.util.List;
import java.util.ArrayList;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * AW-278
 * We compile annoation with ASM, and read them back with ASM at weave time
 * then if offline mode was used, the joinpoint advice list is build based on the
 * annotation on the original method - thus we need to enforce that the annotations have been copied.
 * <p/>
 * Note: this test has dependancy on ASM so we need to add ASM in the classpath without having it remapped with
 * jarjar (since we do not jarjar the tests)
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class AnnotationCopyTest extends TestCase {

    public void testWeaveAndReadnnotation() throws Throwable {
        ClassLoader classLoader = this.getClass().getClassLoader();

        // grab the bytecode from the file system (not weaved since test are using load time weaving)
        InputStream is = classLoader.getResourceAsStream("test/annotation/AnnotationTest.class");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        for (int b = is.read(); b != -1; b = is.read()) {
            os.write(b);
        }
        byte[] bytes = os.toByteArray();

        // emulate the weaving, which should preserve annotations even if methods are wrapped
        AspectWerkzPreProcessor awpp = new AspectWerkzPreProcessor();
        awpp.initialize();
        byte[] weaved = awpp.preProcess("test.annotation.AnnotationTest", bytes, classLoader);

        // check that we have 2 RV
        ClassReader asmReader = new ClassReader(weaved);
        asmReader.accept(
                new ClassAdapter(AsmNullAdapter.NullClassAdapter.NULL_CLASS_ADAPTER) {
                    public CodeVisitor visitMethod(int i, String s, String s1, String[] strings, Attribute attribute) {
                        if ("publicMethod".equals(s) && "()V".equals(s1)) {
                            assertTrue(attribute instanceof RuntimeVisibleAnnotations);
                            RuntimeVisibleAnnotations rvs = (RuntimeVisibleAnnotations) attribute;
                            assertEquals(2, rvs.annotations.size());
                        }
                        return super.visitMethod(i, s, s1, strings, attribute);
                    }
                },
                Attributes.getDefaultAttributes(),
                true
        );
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(AnnotationCopyTest.class);
    }


}
