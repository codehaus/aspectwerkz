/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation.instrumentation.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.attrs.RuntimeInvisibleAnnotations;
import org.objectweb.asm.attrs.Annotation;
import org.objectweb.asm.attrs.RuntimeVisibleAnnotations;
import org.codehaus.aspectwerkz.annotation.AnnotationInfo;
import org.codehaus.aspectwerkz.reflect.impl.asm.AsmClassInfo;

import java.util.List;
import java.util.Iterator;

/**
 * Helper visitor to extract Annotations.
 * The visitors are not writing any bytecode and using a Null ClassVisitor / Code Visitor as a target instead.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class AsmAnnotationHelper {

    private final static String INIT_METHOD_NAME = "<init>";

    public final static ClassVisitor NULL_CLASS_VISITOR = new NullClassVisitor();

    public static final Attribute[] ANNOTATIONS_ATTRIBUTES = new Attribute[]{
        new RuntimeInvisibleAnnotations(),
        new RuntimeVisibleAnnotations()
    };

    /**
     * Generic extractor
     */
    private static class AnnotationExtractor extends NullClassVisitor {

        /**
         * The list where encountered annotation will be put
         */
        protected List m_annotations;

        /**
         * This classloader will be used to instantiate the proxy instance for Custom Annotation support (1.3/1.4).
         * See CustomAttribute that wraps in a RuntimeInvisibleAnnotation the user custom annotations.
         * <br/>Note: no weak reference is used since the visitor is created for a one shot usage.
         */
        protected ClassLoader m_loader;

        /**
         * Generic extractor
         *
         * @param annotations list where to put annotations
         * @param loader classLoader used to instantiate proxy of custom annotations
         */
        private AnnotationExtractor(List annotations, final ClassLoader loader) {
            m_annotations = annotations;
            m_loader = loader;
        }
    }

    /**
     * Extracts class level annotations
     */
    public static class ClassAnnotationExtractor extends AnnotationExtractor {

        public ClassAnnotationExtractor(List annotations, final ClassLoader loader) {
            super(annotations, loader);
        }

        public void visitAttribute(final Attribute attribute) {
            m_annotations = extractAnnotations(m_annotations, attribute, m_loader);
            super.visitAttribute(attribute);
        }
    }

    /**
     * Generic extractor for member (ctor, method, field) annotations extraction
     */
    private static class MemberAnnotationExtractor extends AnnotationExtractor {

        /**
         * Member name (method name, "<init>", field name
         */
        protected String m_name;

        /**
         * Member descriptor (as in visitMethod/visitField ASM methods)
         */
        protected String m_desc;

        /**
         * Method annotation extractor
         * @param annotations
         * @param name of the member for which we want the annotations
         * @param desc of the member for which we want the annotations
         * @param loader
         */
        private MemberAnnotationExtractor(List annotations, String name, String desc, final ClassLoader loader) {
            super(annotations, loader);
            m_name = name;
            m_desc = desc;
        }
    }

    /**
     * Method annotations extractor
     */
    public static class MethodAnnotationExtractor extends MemberAnnotationExtractor {

        public MethodAnnotationExtractor(List annotations, String name, String desc, final ClassLoader loader) {
            super(annotations, name, desc, loader);
        }

        public CodeVisitor visitMethod(final int access,
                                       final String name,
                                       final String desc,
                                       final String[] exceptions,
                                       final Attribute attrs) {
            if (name.equals(m_name) && desc.equals(m_desc)) {
                m_annotations = extractAnnotations(m_annotations, attrs, m_loader);
            }
            return super.visitMethod(access, name, desc, exceptions, attrs);
        }
    }

    /**
     * Constructor annotations extractor
     */
    public static class ConstructorAnnotationExtractor extends MethodAnnotationExtractor {

        public ConstructorAnnotationExtractor(List annotations, String desc, final ClassLoader loader) {
            super(annotations, INIT_METHOD_NAME, desc, loader);
        }
    }

    /**
     * Field annotations extractor
     */
    public static class FieldAnnotationExtractor extends MemberAnnotationExtractor {

        public FieldAnnotationExtractor(List annotations, String name, final ClassLoader loader) {
            super(annotations, name, null, loader);
        }

        public void visitField (final int access,
          final String name,
          final String desc,
          final Object value,
          final Attribute attrs) {
            // no match on desc
            if (name.equals(m_name)) {
                m_annotations = extractAnnotations(m_annotations, attrs, m_loader);
            }
            super.visitField(access, name, desc, value, attrs);
        }
    }

    /**
     * A NullClassVisitor that does nothing.
     * Can be used to speed up ASM and avoid unecessary bytecode writing thru a regular ClassWriter when this is not
     * needed (read only purpose).
     */
    private static class NullClassVisitor implements ClassVisitor {

        public void visit(int i, int i1, String s, String s1, String[] strings, String s2) {
        }

        public void visitInnerClass(String s, String s1, String s2, int i) {
        }

        public void visitField(int i, String s, String s1, Object o, Attribute attribute) {
        }

        public CodeVisitor visitMethod(int i, String s, String s1, String[] strings, Attribute attribute) {
            return NullCodeVisitor.NULL_CODE_VISITOR;
        }

        public void visitAttribute(Attribute attribute) {
        }

        public void visitEnd() {
        }
    }

    /**
     * A NullCodeVisitor that does nothing.
     * Can be used to speed up ASM and avoid unecessary bytecode writing thru a regular CodeWriter when this is not
     * needed (read only purpose)
     */
    private static class NullCodeVisitor implements CodeVisitor {

        private final static CodeVisitor NULL_CODE_VISITOR = new NullCodeVisitor();

        public void visitInsn(int opcode) {
        }

        public void visitIntInsn(int opcode, int operand) {
        }

        public void visitVarInsn(int opcode, int var) {
        }

        public void visitTypeInsn(int opcode, String desc) {
        }

        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        }

        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        }

        public void visitJumpInsn(int opcode, Label label) {
        }

        public void visitLabel(Label label) {
        }

        public void visitLdcInsn(Object cst) {
        }

        public void visitIincInsn(int var, int increment) {
        }

        public void visitTableSwitchInsn(int min, int max, Label dflt, Label labels[]) {
        }

        public void visitLookupSwitchInsn(Label dflt, int keys[], Label labels[]) {
        }

        public void visitMultiANewArrayInsn(String desc, int dims) {
        }

        public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        }

        public void visitMaxs(int maxStack, int maxLocals) {
        }

        public void visitLocalVariable(String name, String desc, Label start, Label end, int index) {
        }

        public void visitLineNumber(int line, Label start) {
        }

        public void visitAttribute(Attribute attr) {
        }
    }

    /**
     * Helper method to extract Runtime(In)VisibleAnnotations and unwrap custom annotation proxies
     *
     * @param annotations
     * @param attribute
     * @param loader
     * @return annotations list populated
     */
    public static List extractAnnotations(List annotations, final Attribute attribute, final ClassLoader loader) {
        for (Attribute current = attribute; current != null; current = current.next) {
            if (current instanceof RuntimeInvisibleAnnotations) {
                for (Iterator it = ((RuntimeInvisibleAnnotations) current).annotations.iterator(); it.hasNext();) {
                    Annotation annotation = (Annotation) it.next();
                    if (CustomAttribute.TYPE.equals(annotation.type)) {
                        annotations.add(CustomAttributeHelper.extractCustomAnnotation(annotation));
                    } else {
                        AnnotationInfo annotationInfo = AsmClassInfo.getAnnotationInfo(
                                annotation,
                                loader
                        );
                        annotations.add(annotationInfo);
                    }
                }
            } else if (current instanceof RuntimeVisibleAnnotations) {
                for (Iterator it = ((RuntimeVisibleAnnotations) current).annotations.iterator(); it.hasNext();) {
                    Annotation annotation = (Annotation) it.next();
                    AnnotationInfo annotationInfo = AsmClassInfo.getAnnotationInfo(
                            annotation,
                            loader
                    );
                    annotations.add(annotationInfo);
                }
            }
        }
        return annotations;
    }
}
