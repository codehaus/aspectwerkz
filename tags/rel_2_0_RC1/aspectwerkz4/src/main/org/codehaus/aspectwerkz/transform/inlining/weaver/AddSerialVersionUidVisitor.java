/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.inlining.weaver;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.security.MessageDigest;
import java.lang.reflect.Modifier;

import org.objectweb.asm.*;
import org.codehaus.aspectwerkz.transform.Context;
import org.codehaus.aspectwerkz.transform.TransformationConstants;
import org.codehaus.aspectwerkz.transform.inlining.ContextImpl;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.reflect.FieldInfo;
import org.codehaus.aspectwerkz.reflect.ConstructorInfo;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

/**
 * Finalizes the weaving process by adding a serial version UID to the classes that does not have one defined.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class AddSerialVersionUidVisitor extends ClassAdapter implements TransformationConstants {

    private final ContextImpl m_ctx;
    private final ClassInfo m_classInfo;
    private boolean m_hasSerialVersionUIDField = false;

    /**
     * Creates a new finalizing visitor.
     *
     * @param cv
     * @param classInfo
     * @param ctx
     */
    public AddSerialVersionUidVisitor(final ClassVisitor cv, final ClassInfo classInfo, final Context ctx) {
        super(cv);
        m_ctx = (ContextImpl) ctx;
        m_classInfo = classInfo;
    }

    /**
     * Remember if we have already the static class field for multi-weaving scheme.
     *
     * @param access
     * @param name
     * @param desc
     * @param value
     * @param attrs
     */
    public void visitField(int access, String name, String desc, Object value, Attribute attrs) {
        if (name.equals(SERIAL_VERSION_UID_FIELD_NAME)) {
            m_hasSerialVersionUIDField = true;
        }
        super.visitField(access, name, desc, value, attrs);
    }

    /**
     * Finalizes the weaving process.
     */
    public void visitEnd() {
        if (!m_ctx.isAdvised()) {
            super.visitEnd();
            return;
        }
        if (!m_hasSerialVersionUIDField) {
            long uid = calculateSerialVersionUID(m_classInfo);
            super.visitField(ACC_STATIC | ACC_FINAL, SERIAL_VERSION_UID_FIELD_NAME, "J", new Long(uid), null);
        }
        super.visitEnd();
    }

    /**
     * Calculates the serialVerUid for a class.
     *
     * @param classInfo the class info
     * @return the serialVersionUID for the class
     */
    public static long calculateSerialVersionUID(final ClassInfo classInfo) {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bout);

            // class name.
            String className = classInfo.getName();
            out.writeUTF(className);

            // class modifiers.
            int classMods = classInfo.getModifiers() &
                            (Constants.ACC_PUBLIC | Constants.ACC_FINAL |
                             Constants.ACC_INTERFACE | Constants.ACC_ABSTRACT);

            MethodInfo[] methods = classInfo.getMethods();

            // fixes bug in javac
            if ((classMods & Constants.ACC_INTERFACE) != 0) {
                classMods = (methods.length > 0) ?
                            (classMods | Constants.ACC_ABSTRACT) :
                            (classMods & ~Constants.ACC_ABSTRACT);
            }
            out.writeInt(classMods);

            // interfaces.
            ClassInfo[] interfaces = classInfo.getInterfaces();
            if (interfaces != null) {
                String[] interfaceNames = new String[interfaces.length];
                for (int i = 0; i < interfaces.length; i++) {
                    interfaceNames[i] = interfaces[i].getName();
                }
                Arrays.sort(interfaceNames);
                for (int i = 0; i < interfaces.length; i++) {
                    out.writeUTF(interfaceNames[i]);
                }
            }
            // fields.
            FieldInfo[] fields = classInfo.getFields();
            if (fields != null) {
                Arrays.sort(
                        fields, new Comparator() {
                            public int compare(Object o1, Object o2) {
                                FieldInfo field1 = (FieldInfo) o1;
                                FieldInfo field2 = (FieldInfo) o2;
                                return field1.getName().compareTo(field2.getName());
                            }
                        }
                );
                for (int i = 0; i < fields.length; i++) {
                    FieldInfo field = fields[i];
                    int mods = field.getModifiers();
                    if (((mods & Constants.ACC_PRIVATE) == 0) ||
                        ((mods & (Constants.ACC_STATIC |
                                  Constants.ACC_TRANSIENT)) == 0)) {
                        out.writeUTF(field.getName());
                        out.writeInt(mods & filterSynthetic());
                        out.writeUTF(field.getSignature());
                    }
                }
            }

            // handle static initialization.
            if (classInfo.hasStaticInitializer()) {
                out.writeUTF(CLINIT_METHOD_NAME);
                out.writeInt(Constants.ACC_STATIC);
                out.writeUTF("()V");
            }

            // handle constructors.
            ConstructorInfo[] constructors = classInfo.getConstructors();
            Arrays.sort(
                    constructors, new Comparator() {
                        public int compare(Object o1, Object o2) {
                            try {
                                ConstructorInfo c1 = (ConstructorInfo) o1;
                                ConstructorInfo c2 = (ConstructorInfo) o2;
                                return c1.getSignature().compareTo(c2.getSignature());
                            } catch (Exception e) {
                                throw new WrappedRuntimeException(e);
                            }
                        }
                    }
            );
            for (int i = 0; i < constructors.length; i++) {
                ConstructorInfo constructor = constructors[i];
                int mods = constructor.getModifiers();
                if ((mods & Constants.ACC_PRIVATE) == 0) {
                    out.writeUTF(INIT_METHOD_NAME);
                    out.writeInt(mods);
                    out.writeUTF(constructor.getSignature().replace('/', '.'));
                }
            }

            // handle regular methods.
            Arrays.sort(
                    methods, new Comparator() {
                        public int compare(Object o1, Object o2) {
                            try {
                                MethodInfo m1 = (MethodInfo) o1;
                                MethodInfo m2 = (MethodInfo) o2;
                                int value = m1.getName().compareTo(m2.getName());
                                if (value == 0) {
                                    value = m1.getSignature().compareTo(m2.getSignature());
                                }
                                return value;
                            } catch (Exception e) {
                                throw new WrappedRuntimeException(e);
                            }
                        }
                    }
            );
            for (int i = 0; i < methods.length; i++) {
                MethodInfo method = methods[i];
                int mods = method.getModifiers();
                if ((mods & Constants.ACC_PRIVATE) == 0) {
                    out.writeUTF(method.getName());
                    out.writeInt(mods & filterSynthetic());
                    out.writeUTF(method.getSignature().replace('/', '.'));
                }
            }

            // calculate hash.
            out.flush();
            MessageDigest digest = MessageDigest.getInstance("SHA");
            byte[] digested = digest.digest(bout.toByteArray());
            long hash = 0;
            for (int i = Math.min(digested.length, 8) - 1; i >= 0; i--) {
                hash = (hash << 8) | (digested[i] & 0xFF);
            }
            return hash;
        } catch (Exception e) {
            e.printStackTrace();
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Filters synthetic modifiers.
     *
     * @return
     */
    private static int filterSynthetic() {
        return (Modifier.PUBLIC |
                Modifier.PRIVATE |
                Modifier.PROTECTED |
                Modifier.STATIC |
                Modifier.FINAL |
                Modifier.TRANSIENT |
                Modifier.VOLATILE);
    }
}