/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package awbench.weave;

import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;
import org.codehaus.aspectwerkz.org.objectweb.asm.Constants;
import org.codehaus.aspectwerkz.org.objectweb.asm.ClassWriter;
import org.codehaus.aspectwerkz.org.objectweb.asm.CodeVisitor;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class GenerateClasses implements Constants {

    private final static String DUMP_DIR = "_dump2";

    private final static String CLASS_NAME_PREFIX = "awbench/weave/Generated_";

    public int m_classCount;

    public int m_count;

    public GenerateClasses(int classCount, int methodCount) {
        m_classCount = classCount;
        m_count = methodCount;
    }

    public void generate() throws Throwable {
        for (int i = 0; i < m_classCount; i++) {
            ClassWriter cv = AsmHelper.newClassWriter(true);

            String className = CLASS_NAME_PREFIX + i;
            cv.visit(
                    AsmHelper.JAVA_VERSION,
                    ACC_PUBLIC + ACC_SUPER + ACC_SYNTHETIC,
                    className,
                    Object.class.getName().replace('.', '/'),
                    new String[]{IGenerated.class.getName().replace('.', '/')},
                    null
            );

            CodeVisitor mv = cv.visitMethod(
                    ACC_PUBLIC,
                    "<init>",
                    "()V",
                    new String[0],
                    null
            );
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);

            for (int m = 0; m < m_count; m++) {
                mv = cv.visitMethod(
                        (m==0)?ACC_PUBLIC:
                        ACC_PRIVATE,//private to have wrapper, public for no wrappers
                        "method_" + m,
                        "()I",
                        new String[0],
                        null
                );

                // each method calls the next method
                if (m != m_count-1) {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitMethodInsn(INVOKEVIRTUAL, className, "method_" + (m+1), "()I");
                }
                AsmHelper.loadIntegerConstant(mv, m);
                mv.visitInsn(IRETURN);
                mv.visitMaxs(0, 0);
            }

            cv.visitEnd();

            AsmHelper.dumpClass(DUMP_DIR, className, cv);
        }
    }

    public static void main(String args[]) throws Throwable {
        int CLASS_COUNT = 100;
        int METHOD_COUNT = 100;

        if (args.length == 2) {
            CLASS_COUNT = Integer.parseInt(args[0]);
            METHOD_COUNT = Integer.parseInt(args[1]);
        }

        int JP_COUNT = (METHOD_COUNT*2 -1/*last method has no call jp*/ + 1/*init exec*/)*CLASS_COUNT;
        GenerateClasses me = new GenerateClasses(CLASS_COUNT, METHOD_COUNT);

        System.out.println("********* Generate");
        System.out.println(" classes: " + CLASS_COUNT);
        System.out.println(" methods: " + METHOD_COUNT);
        System.out.println(" jps: " + JP_COUNT);
        System.out.println("*************************************");

        me.generate();
    }



}
