/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.hook.impl;

import org.codehaus.aspectwerkz.hook.ClassLoaderPatcher;
import org.codehaus.aspectwerkz.hook.ClassLoaderPreProcessor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.CodeAdapter;
import org.objectweb.asm.Type;
import org.objectweb.asm.Constants;
import org.objectweb.asm.Label;
import org.objectweb.asm.attrs.Attributes;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Instruments the java.lang.ClassLoader to plug in the Class PreProcessor mechanism using ASM. <p/>We are using a
 * lazy initialization of the class preprocessor to allow all class pre processor logic to be in system classpath and
 * not in bootclasspath. <p/>This implementation should support IBM custom JRE
 *
 * @author Chris Nokleberg
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class ClassLoaderPreProcessorImpl implements ClassLoaderPreProcessor {

    private final static String CLASSLOADER_CLASS_NAME = "java/lang/ClassLoader";
    private final static String DEFINECLASS0_METHOD_NAME = "defineClass0";


    private static final String DESC_CORE = "Ljava/lang/String;[BIILjava/security/ProtectionDomain;";
    private static final String DESC_PREFIX = "(" + DESC_CORE;
    private static final String DESC_HELPER = "(Ljava/lang/ClassLoader;" + DESC_CORE + ")[B";

    public ClassLoaderPreProcessorImpl() {
    }

    /**
     * Patch caller side of defineClass0
     * byte[] weaved = ..hook.impl.ClassPreProcessorHelper.defineClass0Pre(this, args..);
     * klass = defineClass0(name, weaved, 0, weaved.length, protectionDomain);
     *
     * @param classLoaderBytecode
     * @return
     */
    public byte[] preProcess(byte[] classLoaderBytecode) {
        try {
            ClassWriter cw = new ClassWriter(true);
            ClassLoaderVisitor cv = new ClassLoaderVisitor(cw);
            ClassReader cr = new ClassReader(classLoaderBytecode);
            cr.accept(cv, Attributes.getDefaultAttributes(), false);
            return cw.toByteArray();
        } catch (Exception e) {
            System.err.println("failed to patch ClassLoader:");
            e.printStackTrace();
            return classLoaderBytecode;
        }
    }

    private static class ClassLoaderVisitor extends ClassAdapter {
        public ClassLoaderVisitor(ClassVisitor cv) {
            super(cv);
        }
        public CodeVisitor visitMethod(int access, String name, String desc, String[] exceptions, Attribute attrs) {
            CodeVisitor cv = super.visitMethod(access, name, desc, exceptions, attrs);
            Type[] args = Type.getArgumentTypes(desc);
            return new PreProcessingVisitor(cv, access, args);
        }
    }

    /**
     * @author Chris Nokleberg
     */
    private static class PreProcessingVisitor extends RemappingCodeVisitor {
        public PreProcessingVisitor(CodeVisitor cv, int access, Type[] args) {
            super(cv, access, args);
        }

        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            if (DEFINECLASS0_METHOD_NAME.equals(name) && CLASSLOADER_CLASS_NAME.equals(owner)) {
                Type[] args = Type.getArgumentTypes(desc);
                if (args.length < 5 || !desc.startsWith(DESC_PREFIX)) {
                     throw new Error("non supported JDK, native call not supported: " + desc);
                }
                // store all args in local variables
                int[] locals = new int[args.length];
                for (int i = args.length - 1; i >= 0; i--) {
                    cv.visitVarInsn(args[i].getOpcode(Constants.ISTORE),
                                    locals[i] = nextLocal(args[i].getSize()));
                }
                for (int i = 0; i < 5; i++) {
                    cv.visitVarInsn(args[i].getOpcode(Constants.ILOAD), locals[i]);
                }
                super.visitMethodInsn(Constants.INVOKESTATIC,
                                      "org/codehaus/aspectwerkz/hook/impl/ClassPreProcessorHelper",
                                      "defineClass0Pre",
                                      DESC_HELPER);
                cv.visitVarInsn(Constants.ASTORE, locals[1]);
                cv.visitVarInsn(Constants.ALOAD, 0);
                cv.visitVarInsn(Constants.ALOAD, locals[0]); // name
                cv.visitVarInsn(Constants.ALOAD, locals[1]); // bytes
                cv.visitInsn(Constants.ICONST_0); // offset
                cv.visitVarInsn(Constants.ALOAD, locals[1]);
                cv.visitInsn(Constants.ARRAYLENGTH); // length
                cv.visitVarInsn(Constants.ALOAD, locals[4]); // protection domain
                for (int i = 5; i < args.length; i++) {
                    cv.visitVarInsn(args[i].getOpcode(Constants.ILOAD), locals[i]);
                }
            }
            super.visitMethodInsn(opcode, owner, name, desc);
        }
    }

    /**
     * @author Chris Nokleberg
     */
    private static class State {
        Map locals = new HashMap();
        int firstLocal;
        int nextLocal;

        State(int access, Type[] args) {
            nextLocal = ((Constants.ACC_STATIC & access) != 0) ? 0 : 1;
            for (int i = 0; i < args.length; i++) {
                nextLocal += args[i].getSize();
            }
            firstLocal = nextLocal;
        }
    }

    /**
     * @author Chris Nokleberg
     */
    private static class IntRef {
        int key;
        public boolean equals(Object o) {
            return key == ((IntRef)o).key;
        }
        public int hashCode() {
            return key;
        }
    }

    /**
     * @author Chris Nokleberg
     */
    private static class RemappingCodeVisitor extends CodeAdapter {
       private State state;
       private IntRef check = new IntRef();


      public RemappingCodeVisitor(CodeVisitor v, int access, Type[] args) {
          super(v);
          state = new State(access, args);
      }

      public RemappingCodeVisitor(RemappingCodeVisitor wrap) {
          super(wrap.cv);
          this.state = wrap.state;
      }

      protected int nextLocal(int size) {
          int var = state.nextLocal;
          state.nextLocal += size;
          return var;
      }

      private int remap(int var, int size) {
          if (var < state.firstLocal) {
              return var;
          }
          check.key = (size == 2) ? ~var : var;
          Integer value = (Integer)state.locals.get(check);
          if (value == null) {
              IntRef ref = new IntRef();
              ref.key = check.key;
              state.locals.put(ref, value = new Integer(nextLocal(size)));
          }
          return value.intValue();
      }

      public void visitIincInsn(int var, int increment) {
          cv.visitIincInsn(remap(var, 1), increment);
      }

      public void visitLocalVariable(String name, String desc, Label start, Label end, int index) {
          cv.visitLocalVariable(name, desc, start, end, remap(index, 0));
      }

      public void visitVarInsn(int opcode, int var) {
          int size;
          switch (opcode) {
          case Constants.LLOAD:
          case Constants.LSTORE:
          case Constants.DLOAD:
          case Constants.DSTORE:
              size = 2;
              break;
          default:
              size = 1;
          }
          cv.visitVarInsn(opcode, remap(var, size));
      }

      public void visitMaxs(int maxStack, int maxLocals) {
          cv.visitMaxs(0, 0);
      }
  }

    public static void main(String args[]) throws Exception {
        ClassLoaderPreProcessor me = new ClassLoaderPreProcessorImpl();
        InputStream is = ClassLoader.getSystemClassLoader().getParent().getResourceAsStream("java/lang/ClassLoader.class");
        byte[] out = me.preProcess(ClassLoaderPatcher.inputStreamToByteArray(is));
        is.close();
        File dir = new File("_boot/java/lang/");
        dir.mkdirs();
        OutputStream os = new FileOutputStream("_boot/java/lang/ClassLoader.class");
        os.write(out);
        os.close();
    }
}