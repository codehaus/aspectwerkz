/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.codehaus.aspectwerkz.metadata.FieldMetaData;
import org.codehaus.aspectwerkz.metadata.JavassistMetaDataMaker;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.definition.DefinitionLoader;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import javassist.CtClass;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.CtField;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;

/**
 * Transforms member fields to become "aspect-aware".
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AdviseFieldTransformer implements Transformer {

    /**
     * Transforms the fields.
     *
     * @param context the transformation context
     * @param klass the class set.
     */
    public void transform(final Context context, final Klass klass) throws CannotCompileException {

        // loop over all the definitions
        for (Iterator it = DefinitionLoader.getDefinitions().iterator(); it.hasNext();) {
            final SystemDefinition definition = (SystemDefinition)it.next();

            final CtClass cg = klass.getCtClass();
            final ClassMetaData classMetaData = JavassistMetaDataMaker.createClassMetaData(cg);

            if (classFilter(definition, classMetaData, cg)) {
                return;
            }

            final Set fieldJoinPoints = new HashSet();

            // anonym ExprEditor
            ExprEditor fieldAccessEd = new /*LookAhead*/ExprEditor() {

                boolean isClassAdvised = false;
                boolean isClassAdvisedForStaticField = false;

                //TODO collection support
                public void edit(FieldAccess f, MethodCall m) throws CannotCompileException {
                    try {
                        System.out.println(f.getFieldName());
                        System.out.println(m.getClassName() + "." + m.getMethodName());
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                public void edit(FieldAccess f) throws CannotCompileException {
                    try {
                        CtBehavior where = null;
                        try {
                            where = f.where();
                        }
                        catch (RuntimeException e) {
                            // <clinit> access leads to a bug in Javassist
                            where = cg.getClassInitializer();
                        }

                        // filter caller context
                        if (methodFilter(where)) {
                            return;
                        }

                        // get field accessed information
                        final String fieldName = f.getFieldName();
                        final String signature = f.getField().getType().getName() + " " + fieldName;//TODO check for Array types
                        FieldMetaData fieldMetaData = JavassistMetaDataMaker.createFieldMetaData(f.getField());

                        // handles collection access differently
                        if (fieldMetaData.getType().equals("java.util.Collection") ||
                                fieldMetaData.getType().equals("java.util.Enumeration") ||
                                fieldMetaData.getType().equals("java.util.Iterator") ||
                                fieldMetaData.getType().equals("java.util.List") ||
                                fieldMetaData.getType().equals("java.util.Map") ||
                                fieldMetaData.getType().equals("java.util.Set") ||
                                fieldMetaData.getType().equals("java.util.SortedMap") ||
                                fieldMetaData.getType().equals("java.util.SortedSet")) {

                            //System.out.println("Collection access - TODO - " + f.getLineNumber());
                            return;
                        }

                        String uuid;
                        if (f.isReader()) {
                            uuid = getFieldFilter(definition, classMetaData, fieldMetaData);
                        }
                        else {
                            uuid = setFieldFilter(definition, classMetaData, fieldMetaData);
                        }

                        if (uuid != null) {
                            isClassAdvised = true;

                            // create static Class field if needed
                            if (!isClassAdvisedForStaticField && f.isStatic()) {
                                createStaticClassField(cg);
                                isClassAdvisedForStaticField = true;
                            }

                            // store the join point field data
                            JoinPointFieldData data = new JoinPointFieldData(
                                    fieldName, signature, f.isStatic(), f.isReader(), uuid);
                            if (!fieldJoinPoints.contains(data)) {
                                fieldJoinPoints.add(data);
                                addJoinPointField(cg, fieldName, signature, uuid, f.isStatic(), f.isReader());
                            }

                            // replace
                            String jpFieldName = getJoinPointName(fieldName, f.isStatic(), f.isReader());
                            StringBuffer code = new StringBuffer("{");
                            code.append("$0.").append(jpFieldName).append(".pre();");
                            if (f.isReader()) {
                                code.append("$_ = $proceed($$);");
                            }
                            else {
                                code.append("$proceed($$);");
                            }
                            code.append("$0.").append(jpFieldName).append(".post();");
                            code.append("}");

                            f.replace(code.toString());
                            context.markAsAdvised();
                        }
                    }
                    catch (NotFoundException nfe) {
                        nfe.printStackTrace();
                    }
                }
            };

            cg.instrument(fieldAccessEd);
        }
    }

    /**
     * Adds a new join point member field.
     *
     * @param cg class gen
     * @param fieldName
     * @param fieldSignature
     * @param uuid
     * @param isStatic
     * @param isGet field read or field write
     * @throws NotFoundException
     * @throws CannotCompileException
     */
    private void addJoinPointField(final CtClass cg,
                                   final String fieldName,
                                   final String fieldSignature,
                                   final String uuid,
                                   final boolean isStatic,
                                   final boolean isGet)
            throws NotFoundException, CannotCompileException {

        final String joinPointPrefix = getJoinPointPrefix(isStatic, isGet);
        final String joinPoint = getJoinPointName(joinPointPrefix, fieldName);

        // field already there
        try {
            cg.getField(joinPoint);
            return;
        }
        catch (NotFoundException e) {
            ;//go on to add it
        }

        CtField field;
        StringBuffer code = new StringBuffer("new ");
        if (isStatic) {
            if (isGet) {
                field = new CtField(
                        cg.getClassPool().get(TransformationUtil.STATIC_FIELD_GET_JOIN_POINT_CLASS),
                        joinPoint, cg);
                code.append(TransformationUtil.STATIC_FIELD_GET_JOIN_POINT_CLASS);
            }
            else {
                field = new CtField(
                        cg.getClassPool().get(TransformationUtil.STATIC_FIELD_SET_JOIN_POINT_CLASS),
                        joinPoint, cg);
                code.append(TransformationUtil.STATIC_FIELD_SET_JOIN_POINT_CLASS);
            }
            field.setModifiers(Modifier.PRIVATE | Modifier.FINAL | Modifier.STATIC);
            code.append("(");
            code.append("\"").append(uuid).append("\"");
            code.append(", ").append(TransformationUtil.STATIC_CLASS_FIELD);
            code.append(", \"").append(fieldSignature).append("\"");
        }
        else {
            if (isGet) {
                field = new CtField(
                        cg.getClassPool().get(TransformationUtil.MEMBER_FIELD_GET_JOIN_POINT_CLASS),
                        joinPoint, cg);
                code.append(TransformationUtil.MEMBER_FIELD_GET_JOIN_POINT_CLASS);
            }
            else {
                field = new CtField(
                        cg.getClassPool().get(TransformationUtil.MEMBER_FIELD_SET_JOIN_POINT_CLASS),
                        joinPoint, cg);
                code.append(TransformationUtil.MEMBER_FIELD_SET_JOIN_POINT_CLASS);
            }
            field.setModifiers(Modifier.PRIVATE | Modifier.FINAL);
            code.append("(");
            code.append("\"").append(uuid).append("\"");
            code.append(", this");
            code.append(", \"").append(fieldSignature).append("\"");
        }
        code.append(");");

        cg.addField(field, code.toString());
    }

    /**
     * Filters the classes to be transformed.
     *
     * @param definition the definition
     * @param classMetaData the meta-data for the class
     * @param cg the class to filter
     * @return boolean true if the method should be filtered away
     */
    private boolean classFilter(final SystemDefinition definition,
                                final ClassMetaData classMetaData,
                                final CtClass cg) {
        if (cg.isInterface() ||
                TransformationUtil.hasSuperClass(classMetaData, "org.codehaus.aspectwerkz.attribdef.aspect.Aspect") ||
                TransformationUtil.hasSuperClass(classMetaData, "org.codehaus.aspectwerkz.xmldef.advice.AroundAdvice") ||
                TransformationUtil.hasSuperClass(classMetaData, "org.codehaus.aspectwerkz.xmldef.advice.PreAdvice") ||
                TransformationUtil.hasSuperClass(classMetaData, "org.codehaus.aspectwerkz.xmldef.advice.PostAdvice")) {
            return true;
        }
        String className = cg.getName();
        if (definition.inExcludePackage(className)) {
            return true;
        }
        if (!definition.inIncludePackage(className)) {
            return true;
        }

        if (definition.hasSetPointcut(classMetaData) ||
                definition.hasGetPointcut(classMetaData)) {
            return false;
        }
        return true;
    }

    /**
     * Filters the methods.
     *
     * @param method the method to filter
     * @return boolean true if the method should be filtered away
     */
    private boolean methodFilter(final CtBehavior method) {
        return Modifier.isNative(method.getModifiers())
                || Modifier.isAbstract(method.getModifiers())
                || method.getName().startsWith(TransformationUtil.ASPECTWERKZ_PREFIX);
    }

    /**
     * Filters the PUTFIELD's to be transformed.
     *
     * @param definition the definition
     * @param classMetaData the class to filter
     * @param fieldMetaData the field to filter
     * @return the UUID for the weave model
     */
    private String setFieldFilter(final SystemDefinition definition,
                                  final ClassMetaData classMetaData,
                                  final FieldMetaData fieldMetaData) {
        if (fieldMetaData.getName().startsWith(TransformationUtil.ASPECTWERKZ_PREFIX)) {
            return null;
        }
        if (definition.hasSetPointcut(classMetaData, fieldMetaData)) {
            return definition.getUuid();
        }
        return null;
    }

    /**
     * Filters the GETFIELD's to be transformed.
     *
     * @param definition the definition
     * @param classMetaData the class to filter
     * @param fieldMetaData the field to filter
     * @return the UUID for the weave model
     */
    private String getFieldFilter(final SystemDefinition definition,
                                  final ClassMetaData classMetaData,
                                  final FieldMetaData fieldMetaData) {
        if (fieldMetaData.getName().startsWith(TransformationUtil.ASPECTWERKZ_PREFIX)) {
            return null;
        }
        if (definition.hasGetPointcut(classMetaData, fieldMetaData)) {
            return definition.getUuid();
        }
        return null;
    }

    /**
     * Returns the join point prefix.
     *
     * @param isStatic
     * @param isGet
     * @return
     */
    private static String getJoinPointPrefix(final boolean isStatic, final boolean isGet) {
        String joinPointPrefix;
        if (isGet) {
            if (isStatic) {
                joinPointPrefix = TransformationUtil.STATIC_FIELD_GET_JOIN_POINT_PREFIX;
            }
            else {
                joinPointPrefix = TransformationUtil.MEMBER_FIELD_GET_JOIN_POINT_PREFIX;
            }
        }
        else {
            if (isStatic) {
                joinPointPrefix = TransformationUtil.STATIC_FIELD_SET_JOIN_POINT_PREFIX;
            }
            else {
                joinPointPrefix = TransformationUtil.MEMBER_FIELD_SET_JOIN_POINT_PREFIX;
            }
        }
        return joinPointPrefix;
    }

    /**
     * Returns the name of the join point.
     *
     * @param joinPointPrefix the prefix
     * @param fieldName the name of the field
     * @return the name of the join point
     */
    private static String getJoinPointName(final String joinPointPrefix, final String fieldName) {
        final StringBuffer joinPoint = new StringBuffer();
        joinPoint.append(joinPointPrefix);
        joinPoint.append(fieldName);
        return joinPoint.toString();
    }

    /**
     * Returns the name of the join point.
     *
     * @param fieldName the name of the field
     * @param isStatic
     * @param isGet
     * @return the name of the join point
     */
    private static String getJoinPointName(final String fieldName, boolean isStatic, boolean isGet) {
        final StringBuffer joinPoint = new StringBuffer();
        joinPoint.append(getJoinPointPrefix(isStatic, isGet));
        joinPoint.append(fieldName);
        return joinPoint.toString();
    }

    /**
     * Create a Class field
     *
     * @param cg
     * @throws NotFoundException
     * @throws CannotCompileException
     */
    private void createStaticClassField(final CtClass cg) throws NotFoundException, CannotCompileException {
        final String className = cg.getName();

        CtField field = new CtField(cg.getClassPool().get("java.lang.Class"),
                TransformationUtil.STATIC_CLASS_FIELD,
                cg);
        field.setModifiers(Modifier.STATIC | Modifier.PRIVATE);
        cg.addField(field, "java.lang.Class.forName(\"" + className + "\")");
    }

    /**
     * Container for the join point field data.
     */
    private static class JoinPointFieldData {

        private String m_name;
        private String m_signature;
        private boolean m_isStatic;
        private boolean m_isGet;
        private String m_uuid;

        public JoinPointFieldData(final String name,
                                  final String signature,
                                  final boolean isStatic,
                                  final boolean isGet,
                                  final String uuid) {
            m_name = name;
            m_signature = signature;
            m_isStatic = isStatic;
            m_isGet = isGet;
            m_uuid = uuid;
        }

        public String getName() {
            return m_name;
        }

        public String getSignature() {
            return m_signature;
        }

        public String getUuid() {
            return m_uuid;
        }

        public boolean isStatic() {
            return m_isStatic;
        }

        public boolean isGet() {
            return m_isGet;
        }

        public boolean isSet() {
            return !m_isGet;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof JoinPointFieldData)) return false;

            final JoinPointFieldData object = (JoinPointFieldData)o;

            if (m_isGet != object.m_isGet) return false;
            if (m_isStatic != object.m_isStatic) return false;
            if (m_name != null ? !m_name.equals(object.m_name) : object.m_name != null) return false;
            if (m_signature != null ? !m_signature.equals(object.m_signature) : object.m_signature != null) return false;
            if (m_uuid != null ? !m_uuid.equals(object.m_uuid) : object.m_uuid != null) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = (m_name != null ? m_name.hashCode() : 0);
            result = 29 * result + (m_signature != null ? m_signature.hashCode() : 0);
            result = 29 * result + (m_isStatic ? 1 : 0);
            result = 29 * result + (m_isGet ? 1 : 0);
            result = 29 * result + (m_uuid != null ? m_uuid.hashCode() : 0);
            return result;
        }
    }
}
