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
import org.objectweb.asm.Type;
import org.objectweb.asm.attrs.RuntimeInvisibleAnnotations;
import org.objectweb.asm.attrs.Annotation;
import org.objectweb.asm.attrs.RuntimeVisibleAnnotations;
import org.codehaus.aspectwerkz.annotation.AnnotationInfo;
import org.codehaus.aspectwerkz.reflect.impl.asm.AsmClassInfo;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.ref.WeakReference;

/**
 * Helper visitor to extract Annotations.
 * The visitors are not writing any bytecode and using a Null ClassVisitor / Code Visitor as a target instead.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class AsmAnnotationHelper {

    private final static String INIT_METHOD_NAME = "<init>";

    public static final Attribute[] ANNOTATIONS_ATTRIBUTES = new Attribute[]{
        new RuntimeInvisibleAnnotations(),
        new RuntimeVisibleAnnotations()
    };

    /**
     * Generic extractor
     */
    private static class AnnotationExtractor extends NullClassAdapter {

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
         * @param loader      classLoader used to instantiate proxy of custom annotations
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
         *
         * @param annotations
         * @param name        of the member for which we want the annotations
         * @param desc        of the member for which we want the annotations
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

        public void visitField(final int access,
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
     * A NullClassAdapter that does nothing.
     * Can be used to speed up ASM and avoid unecessary bytecode writing thru a regular ClassWriter when this is not
     * needed (read only purpose).
     */
    public static class NullClassAdapter implements ClassVisitor {

        public final static ClassVisitor NULL_CLASS_ADAPTER = new NullClassAdapter();

        public void visit(int i, int i1, String s, String s1, String[] strings, String s2) {
        }

        public void visitInnerClass(String s, String s1, String s2, int i) {
        }

        public void visitField(int i, String s, String s1, Object o, Attribute attribute) {
        }

        public CodeVisitor visitMethod(int i, String s, String s1, String[] strings, Attribute attribute) {
            return NullCodeAdapter.NULL_CODE_ADAPTER;
        }

        public void visitAttribute(Attribute attribute) {
        }

        public void visitEnd() {
        }
    }

    /**
     * A NullCodeAdapter that does nothing.
     * Can be used to speed up ASM and avoid unecessary bytecode writing thru a regular CodeWriter when this is not
     * needed (read only purpose)
     */
    public static class NullCodeAdapter implements CodeVisitor {

        public final static CodeVisitor NULL_CODE_ADAPTER = new NullCodeAdapter();

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

    /**
     * Build and return a dynamic proxy representing the given ASM Annotation.
     * The proxy implements the AspectWerkz Annotation interface, as well as the user type Annotation.
     * Each elements of the annotation is proxied if needed or agressively created unless Class types to not trigger
     * any nested loading.
     *
     * Note: JSR-175 does not support Annotation value of N-dimensional array. At most 1 dimension is supported.
     *
     * @param annotation
     * @param loader
     * @return
     */
    public static org.codehaus.aspectwerkz.annotation.Annotation getAnnotationProxy(Annotation annotation, ClassLoader loader) {
        String annotationClassName = Type.getType(annotation.type).getClassName();
        List elementValues = annotation.elementValues;
        List annotationValues = new ArrayList(elementValues.size());
        for (int i = 0; i < elementValues.size(); i++) {
            Object[] element = (Object[]) elementValues.get(i);
            String name = (String) element[0];
            Object valueHolder = getAnnotationValueHolder(element[1], loader);
            annotationValues.add(new AnnotationElement(name, valueHolder));
        }

        try {
            Class typeClass = Class.forName(annotationClassName, false, loader);
            Object proxy = Proxy.newProxyInstance(
                    loader,
                    new Class[]{org.codehaus.aspectwerkz.annotation.Annotation.class, typeClass},
                    new Java5AnnotationInvocationHandler(annotationClassName, annotationValues)
            );
            return (org.codehaus.aspectwerkz.annotation.Annotation) proxy;
        } catch (ClassNotFoundException e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Turn an ASM Annotation value into a concrete Java value holder, unless the value is of type
     * Class, in which case we wrap it behind a LazyClass() object so that actual loading of the class
     * will be done lazily
     *
     * @param value
     * @param loader
     * @return
     */
    private static Object getAnnotationValueHolder(Object value, ClassLoader loader) {
        if (value instanceof Annotation.EnumConstValue) {
            Annotation.EnumConstValue enumAsmValue = (Annotation.EnumConstValue) value;
            try {
                Class enumClass = Class.forName(Type.getType(enumAsmValue.typeName).getClassName(), false, loader);
                Field enumConstValue = enumClass.getField(enumAsmValue.constName);
                return enumConstValue.get(null);
            } catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        } else if (value instanceof Type) {
            // TODO may require additional filtering ?
            return new LazyClass(((Type) value).getClassName());
        } else if (value instanceof Annotation) {
            return getAnnotationProxy(((Annotation) value), loader);
        } else if (value instanceof Object[]) {
            Object[] values = (Object[]) value;
            Object[] holders = new Object[values.length];
            for (int i = 0; i < values.length; i++) {
                holders[i] = getAnnotationValueHolder(values[i], loader);
            }
            return holders;
        }
        return value;
    }

    /**
     * Dynamic proxy handler for ASM Annotations we extract
     * The handler resolve the LazyClass to a concrete Class
     * <p/>
     * Based on ASM article on ONJava TODO http blabla
     *
     * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
     */
    private static class Java5AnnotationInvocationHandler implements InvocationHandler {

        private final String m_annotationClassName;

        private final List m_annotationElements;

        public Java5AnnotationInvocationHandler(String type, List annotationElements) {
            this.m_annotationClassName = type;
            this.m_annotationElements = annotationElements;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();

            if ("toString".equals(name)) {
                //TODO implement toString as per JSR-175 spec
                StringBuffer sb = new StringBuffer(m_annotationClassName);
                sb.append("[");
                String sep = "";
                for (Iterator iterator = m_annotationElements.iterator(); iterator.hasNext();) {
                    AnnotationElement annotationElement = (AnnotationElement) iterator.next();
                    sb.append(sep).append(annotationElement.name + "=" + annotationElement.valueHolder.toString());
                    sep = "; ";
                }
                sb.append("]");
                return sb.toString();
            } else if ("annotationType".equals(name)) {
                // funny, may explain why 1.5 Annotation intf has annotationType + getClass
                // since a dynamic proxy handler cannot hijack getClass() ..
                return Class.forName(m_annotationClassName, false, proxy.getClass().getClassLoader());
            } else if ("value".equals(name)) {
                if (m_annotationElements.isEmpty()) {
                    return null;
                } else {
                    // we could check that we don't have more than one element
                    return ((AnnotationElement) m_annotationElements.get(0)).resolveValueHolderFrom(
                            proxy.getClass().getClassLoader()
                    );
                }
            } else {
                for (Iterator iterator = m_annotationElements.iterator(); iterator.hasNext();) {
                    AnnotationElement annotationElement = (AnnotationElement) iterator.next();
                    if (name.equals(annotationElement.name)) {
                        return annotationElement.resolveValueHolderFrom(proxy.getClass().getClassLoader());
                    }
                }
                // element not found for such a name
                throw new RuntimeException("Invalid element on Annotation @" + m_annotationClassName + " : " + name);
            }
        }
    }

    /**
     * A wrapper for a className, that will allow late loading of the actual Class object of an annotation value
     *
     * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
     */
    private static class LazyClass {
        LazyClass(String className) {
            this.className = className;
        }

        String className;

        public String toString() {
            return className + ".class";
        }

        Class getActualClassFrom(ClassLoader loader) {
            try {
                return Class.forName(className, false, loader);
            } catch (ClassNotFoundException e) {
                throw new WrappedRuntimeException(e);
            }
        }

    }

    /**
     * A structure for an Annotation element
     */
    private static class AnnotationElement {
        String name;
        Object valueHolder;
        private boolean isLazyClass = false;
        private boolean isLazyClassArray = false;

        AnnotationElement(String name, Object valueHolder) {
            this.name = name;
            this.valueHolder = valueHolder;
            if (valueHolder instanceof LazyClass) {
                isLazyClass = true;
            } else if (valueHolder instanceof Object[]) {
                if (((Object[])valueHolder).getClass().getComponentType().isAssignableFrom(LazyClass.class)) {
                    isLazyClassArray = true;
                }
            }
        }

        Object resolveValueHolderFrom(ClassLoader loader) {
            if (isLazyClass) {
                return ((LazyClass) valueHolder).getActualClassFrom(loader);
            } else if (isLazyClassArray) {
                Object[] annotationValueHolderArray = (Object[]) valueHolder;
                Class[] resolved = new Class[annotationValueHolderArray.length];
                for (int i = 0; i < annotationValueHolderArray.length; i++) {
                    resolved[i] = ((LazyClass)annotationValueHolderArray[i]).getActualClassFrom(loader);
                }
                return resolved;
                //TODO support N dimension array needed ?
            } else {
                return valueHolder;
            }
        }
    }

}
