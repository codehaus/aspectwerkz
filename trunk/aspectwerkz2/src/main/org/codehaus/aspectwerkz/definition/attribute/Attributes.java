/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.attribute;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.aspectwerkz.definition.attribute.bcel.BcelAttributeExtractor;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

/**
 * Retrieves attributes on class, method and field level
 * <p/>
 * Based on code from the Attrib4j project by Mark Pollack and Ted Neward (http://attrib4j.sourceforge.net/).
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class Attributes {

    /**
     * Hold a cache of AttributeExtractors so we don't have to load the class loaded repeatedly when accessing custom
     * attributes.
     */
    private static Map m_extractorCache = new HashMap();

    /**
     * Return the list (possibly empty) of custom attributes associated with the class "klass".
     *
     * @param klass The java.lang.Class object to find the attributes on.
     * @return The possibly 0-length array of attributes
     */
    public static Object[] getAttributes(final Class klass) {
        return getAttrExtractor(klass).getClassAttributes();
    }

    /**
     * Return all the attributes associated with the given method.
     *
     * @param method The java.lang.reflect.Method describing the method.
     * @return Attribute[] all attributes associated with the method. Returns a 0 length array in case no attributes
     *         were found.
     */
    public static Object[] getAttributes(final Method method) {
        Class klass = method.getDeclaringClass();

        ArrayList attribList = new ArrayList();
        // search for superclass
        while (true) {
            Object[] returnAttribs = searchForMethodAttribs(klass, method);
            if (returnAttribs.length > 0) {
                // already in the list and the attribute is allowed to be specified mulitple times.
                attribList.addAll(Arrays.asList(returnAttribs));
            }
            Class superClass = klass.getSuperclass();
            if (superClass == null) {
                break;
            }
            else if (superClass.getName().startsWith("java.")) {
                break;
            }
            else {
                klass = superClass;
            }
        }
        // search for interfaces.
        while (true) {
            Class[] interfaceClasses = klass.getInterfaces();
            for (int i = 0; i < interfaceClasses.length; i++) {
                Object[] intAttribs = searchForMethodAttribs(interfaceClasses[i], method);
                if (intAttribs.length > 0) {
                    attribList.addAll(Arrays.asList(intAttribs));
                }
            }
            Class superClass = klass.getSuperclass();
            if (superClass == null) {
                break;
            }
            else if (superClass.getName().startsWith("java.")) {
                break;
            }
            else {
                klass = superClass;
            }
        }
        return attribList.toArray(new Object[attribList.size()]);
    }

    /**
     * Return the list (possibly empty) of custom attributes associated with the field.
     *
     * @param field The java.lang.reflect.Field object to find the attributes on.
     * @return The possibly 0-length array of attributes
     */
    public static Object[] getAttributes(final Field field) {
        return getAttrExtractor(field.getDeclaringClass()).getFieldAttributes(field.getName());
    }

    /**
     * Searches for method attributes
     *
     * @param klass
     * @param method
     * @return Attribute[]
     */
    private static Object[] searchForMethodAttribs(final Class klass, final Method method) {
        AttributeExtractor extractor = getAttrExtractor(klass);
        if (extractor != null) {
            String[] paramTypes = new String[method.getParameterTypes().length];
            for (int i = 0; i < paramTypes.length; i++) {
                String paramType = method.getParameterTypes()[i].getName();

                // TODO: is this fix generic? are there other cases not handled?
                // handle array types
                if (paramType.startsWith("[L")) {
                    paramType = paramType.substring(2, paramType.length() - 1) + "[]";
                }
                paramTypes[i] = paramType;
            }
            return extractor.getMethodAttributes(method.getName(), paramTypes);
        }
        else {
            return new Object[0];
        }
    }

    /**
     * Return the list (possibly empty) of custom attributes associated with the class.
     *
     * @param klass The Class object to find the attributes on.
     * @return The possibly 0-length array of attributes
     */
    private static synchronized AttributeExtractor getAttrExtractor(final Class klass) {
        AttributeExtractor extractor = null;
        if ((extractor = (AttributeExtractor)m_extractorCache.get(klass)) == null) {
            String className = klass.getName();
            try {
                ClassLoader loader = klass.getClassLoader();
                if (loader != null) {
                    // TODO: use factory
                    extractor = new BcelAttributeExtractor();
                    extractor.initialize(className, klass.getClassLoader());
                    m_extractorCache.put(klass, extractor);
                }
                else {
                    // bootstrap classloader
                    extractor = new BcelAttributeExtractor();
                    extractor.initialize(className, ClassLoader.getSystemClassLoader());
                    m_extractorCache.put(klass, extractor);
                }
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        }
        return extractor;
    }
}