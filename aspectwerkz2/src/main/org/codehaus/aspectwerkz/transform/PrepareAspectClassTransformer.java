/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.HashSet;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.CtNewMethod;
import javassist.CtConstructor;
import javassist.ClassMap;

import org.codehaus.aspectwerkz.definition.DefinitionLoader;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.JavassistMetaDataMaker;
import org.codehaus.aspectwerkz.metadata.MetaDataInspector;

/**
 * Prepares the aspect classes.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class PrepareAspectClassTransformer implements Transformer {

    /**
     * The aspects' class names.
     */
    List m_aspectClassNames = null;

    /**
     * Map with all the loaded aspects, mapped per class loader.
     */
    private Map m_loadedAspects = new WeakHashMap();

    /**
     * Creates a new instance of the transformer.
     */
    public PrepareAspectClassTransformer() {
        m_aspectClassNames = DefinitionLoader.getAspectClassNames();
    }

    /**
     * Makes the member method transformations.
     * 
     * @param context the transformation context
     * @param klass   the class set.
     */
    public void transform(final Context context, final Klass klass) throws Exception {
        for (Iterator it = m_aspectClassNames.iterator(); it.hasNext();) {

            String className = (String)it.next();
            String klassName = klass.getName();

            if (className.equals(klassName)) {
                ClassLoader loader = context.getLoader();
                Set aspects = (Set)m_loadedAspects.get(loader);
                if (aspects == null) {
                    aspects = new HashSet();
                    m_loadedAspects.put(loader, aspects);
                }
                else {
                    if (aspects.contains(klassName)) {
                        return;
                    }
                }

                aspects.add(klassName);

                final CtClass ctClass = klass.getCtClass();
                ClassMetaData classMetaData = JavassistMetaDataMaker.createClassMetaData(ctClass);

                if (!MetaDataInspector.hasField(classMetaData, TransformationUtil.CROSS_CUTTING_INFO_CLASS_FIELD)) {
                    addCrossCuttingInfoField(ctClass);
                    context.markAsAdvised();
                }
                if (!MetaDataInspector.hasInterface(classMetaData, TransformationUtil.CROSS_CUTTING_CLASS)) {
                    addCrossCuttableInterface(ctClass);
                    context.markAsAdvised();
                }
                addGetCrossCuttingInfoMethod(ctClass);
                addSetCrossCuttingInfoMethod(ctClass);
                addInitializingConstructor(ctClass);
            }
        }
    }

    /**
     * Adds constructor that sets the cross-cutting info instance before initializing the instance.
     *
     * @param ctClass
     * @throws CannotCompileException
     * @throws NotFoundException
     */
    private void addInitializingConstructor(final CtClass ctClass) throws CannotCompileException, NotFoundException {
        StringBuffer body = new StringBuffer();
        body.append('{');

        body.append(TransformationUtil.CROSS_CUTTING_INFO_CLASS_FIELD);
        body.append(" = $1; }");
        CtConstructor newCtor = new CtConstructor(
                new CtClass[]{ctClass.getClassPool().get(TransformationUtil.CROSS_CUTTING_INFO_CLASS)},
                ctClass
        );
        CtConstructor[] constructors = ctClass.getConstructors();
        for (int i = 0; i < constructors.length; i++) {
            CtConstructor oldCtor = constructors[i];
            if (oldCtor.getParameterTypes().length == 0) {
                newCtor.setBody(oldCtor, new ClassMap());
                newCtor.insertBeforeBody(body.toString());
                ctClass.addConstructor(newCtor);
                return;
            }
        }
        throw new RuntimeException(
                "cross-cutting class does not define default no-argument constructor [" + ctClass.getName().replace('/', '.') + "]"
        );
    }

    /**
     * Adds a method that returns the cross cutting info to the target class.
     *
     * @param ctClass
     * @throws CannotCompileException
     * @throws NotFoundException
     */
    private void addGetCrossCuttingInfoMethod(final CtClass ctClass) throws CannotCompileException, NotFoundException {
        StringBuffer body = new StringBuffer();
        body.append("{ return ");
        body.append(TransformationUtil.CROSS_CUTTING_INFO_CLASS_FIELD);
        body.append("; }");
        ctClass.addMethod(
                CtNewMethod.make(
                        ctClass.getClassPool().get(TransformationUtil.CROSS_CUTTING_INFO_CLASS),
                        TransformationUtil.GET_CROSS_CUTTING_INFO_METHOD,
                        new CtClass[]{},
                        new CtClass[]{},
                        body.toString(),
                        ctClass
                )
        );
    }

    /**
     * Adds a method that sets the cross cutting info for the target class.
     *
     * @param ctClass
     * @throws CannotCompileException
     * @throws NotFoundException
     */
    private void addSetCrossCuttingInfoMethod(final CtClass ctClass) throws CannotCompileException, NotFoundException {
        StringBuffer body = new StringBuffer();
        body.append('{');
        body.append(TransformationUtil.CROSS_CUTTING_INFO_CLASS_FIELD);
        body.append(" = $1; }");
        ctClass.addMethod(
                CtNewMethod.make(
                        CtClass.voidType,
                        TransformationUtil.SET_CROSS_CUTTING_INFO_METHOD,
                        new CtClass[]{ctClass.getClassPool().get(TransformationUtil.CROSS_CUTTING_INFO_CLASS)},
                        new CtClass[]{},
                        body.toString(),
                        ctClass
                )
        );
    }

    /**
     * Creates a new static class field.
     * 
     * @param ctClass the class
     */
    private void addCrossCuttingInfoField(final CtClass ctClass) throws NotFoundException, CannotCompileException {
        CtField field = new CtField(
                ctClass.getClassPool().get(TransformationUtil.CROSS_CUTTING_INFO_CLASS),
                TransformationUtil.CROSS_CUTTING_INFO_CLASS_FIELD,
                ctClass
        );
        field.setModifiers(Modifier.PRIVATE | Modifier.FINAL);
        ctClass.addField(field);
    }

    /**
     * Adds a new interface to the class.
     *
     * @param ctClass the class
     */
    private void addCrossCuttableInterface(final CtClass ctClass) throws NotFoundException {
        ctClass.addInterface(ctClass.getClassPool().get(TransformationUtil.CROSS_CUTTING_CLASS));
    }
}
