/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.xmldef.definition;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.codehaus.aspectwerkz.MethodComparator;

/**
 * Holds the introduction definition.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class IntroductionDefinition implements Serializable {

    /**
     * The name of the introduction.
     */
    private String m_name;

    private String m_interface;
    private String m_implementation;

    /**
     * The deployment model.
     */
    private String m_deploymentModel;

    /**
     * The attribute.
     */
    private String m_attribute = "";

    /**
     * The method introductions.
     */
    private final List m_methodIntroductions = new ArrayList();

    /**
     * The interface introductions.
     */
    private final List m_interfaceIntroductions = new ArrayList();

    /**
     * Returns the name or the introduction.
     *
     * @return the name
     */
    public String getName() {
        return m_name;
    }

    /**
     * Sets the name or the introduction.
     *
     * @param name the name
     */
    public void setName(final String name) {
        m_name = name.trim();
    }

    /**
     * Returns the class name of the interface.
     *
     * @return the class name
     */
    public String getInterface() {
        return m_interface;
    }

    /**
     * Sets the class name or the interface.
     *
     * @param anInterface the class name
     */
    public void setInterface(final String anInterface) {
        m_interface = anInterface.trim();
    }

    /**
     * Returns the class name or the implementation.
     *
     * @return the class name
     */
    public String getImplementation() {
        return m_implementation;
    }

    /**
     * Sets the class name of the implementation.
     *
     * @param implementation the class name
     */
    public void setImplementation(final String implementation) {
        m_implementation = implementation.trim();
    }

    /**
     * Returns the the deployment model for the advice
     *
     * @return the deployment model
     */
    public String getDeploymentModel() {
        return m_deploymentModel;
    }

    /**
     * Sets the deployment model for the advice.
     *
     * @param deploymentModel the deployment model
     */
    public void setDeploymentModel(final String deploymentModel) {
        m_deploymentModel = deploymentModel.trim();
    }

    /**
     * Returns the attribute.
     *
     * @return the attribute
     */
    public String getAttribute() {
        return m_attribute;
    }

    /**
     * Sets the attribute.
     *
     * @param attribute the attribute
     */
    public void setAttribute(final String attribute) {
        m_attribute = attribute;
    }

//    /**
//     * ALEX
//     * Adds a new method introduction.
//     *
//     * @param introductionMetaData the introduction
//     */
//    public void addMethodIntroduction(final MethodIntroductionDefinition introductionMetaData) {
//        m_methodIntroductions.add(introductionMetaData);
//    }

//    /**
//     * ALEX
//     * Adds a new interface introduction.
//     *
//     * @param interfaceIntroDef the introduction
//     */
//    public void addInterfaceIntroduction(final InterfaceIntroductionDefinition introductionMetaData) {
//        m_interfaceIntroductions.add(introductionMetaData);
//    }

//    /**
//     * ALEX
//     * Returns the method introductions.
//     *
//     * @TODO: gets sorted every time, have a flag?
//     *
//     * @return the introductions
//     */
//    public List getMethodIntroductions() {
//        return sortMethodIntroductions(m_methodIntroductions);
//    }

    /**
     * Returns the interface introductions.
     *
     * @return the introductions
     */
    public List getInterfaceIntroductions() {
        return m_interfaceIntroductions;
    }

//    /**
//     * ALEX
//     * Sorts the introductions by method.
//     *
//     * @param introductions a list with the introductions to sort
//     * @return a sorted list with the introductions
//     */
//    public static List sortMethodIntroductions(final List introductions) {
//        Collections.sort(introductions, new Comparator() {
//            private Comparator m_comparator = MethodComparator.getInstance(MethodComparator.NORMAL_METHOD);
//
//            public int compare(final Object obj1, final Object obj2) {
//                MethodIntroductionDefinition introduction1 = (MethodIntroductionDefinition)obj1;
//                MethodIntroductionDefinition introduction2 = (MethodIntroductionDefinition)obj2;
//                return m_comparator.compare(introduction1.getMethod(), introduction2.getMethod());
//            }
//        });
//        return introductions;
//    }
}
