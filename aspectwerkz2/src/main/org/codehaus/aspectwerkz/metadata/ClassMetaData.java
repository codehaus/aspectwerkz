/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.metadata;

import java.util.List;

/**
 * Interface for the class metadata implementations.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface ClassMetaData extends MetaData {
    /**
     * Returns the name of the class.
     *
     * @return the name of the class
     */
    String getName();

    /**
     * Returns the class modifiers.
     *
     * @return the class modifiers
     */
    int getModifiers();

    /**
     * Returns a list with all the constructors meta-data.
     *
     * @return the constructors meta-data
     */
    List getConstructors();

    /**
     * Returns a list with all the methods meta-data even the inherited methods.
     *
     * @return the methods meta-data for all the methods
     */
    List getAllMethods();

    /**
     * Returns a list with all the methods meta-data.
     *
     * @return the methods meta-data
     */
    List getMethods();

    /**
     * Returns a list with all the field meta-data.
     *
     * @return the field meta-data
     */
    List getFields();

    /**
     * Returns the interfaces.
     *
     * @return the interfaces
     */
    List getInterfaces();

    /**
     * Returns the super class.
     *
     * @return the super class
     */
    ClassMetaData getSuperClass();
}
