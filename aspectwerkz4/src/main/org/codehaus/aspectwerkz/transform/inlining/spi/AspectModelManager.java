/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.inlining.spi;

import java.util.StringTokenizer;

import org.codehaus.aspectwerkz.ContextClassLoader;
import org.codehaus.aspectwerkz.exception.DefinitionException;

/**
 * Manages the different aspect model implementations that is registered.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public class AspectModelManager {

    private static final String ASPECT_MODELS = "aspectwerkz.extension.aspectmodels";
    private static final String DELIMITER = ":";

    /**
     * The aspects models that are registered
     */
    private static AspectModel[] m_aspectModels = new AspectModel[]{};

    static {
        registerAspectModels(System.getProperty(ASPECT_MODELS, null));
    }

    /**
     * Returns an array with all the aspect models that has been registered.
     *
     * @return an array with the aspect models
     */
    public static AspectModel[] getModels() {
        return m_aspectModels;
    }

    /**
     * Returns the advice model for a specific aspect model type id.
     *
     * @param type the aspect model type id
     * @return the aspect model
     */
    public static AspectModel getModelFor(String type) {
        for (int i = 0; i < m_aspectModels.length; i++) {
            AspectModel aspectModel = m_aspectModels[i];
            if (aspectModel.getAspectModelType().equals(type)) {
                return aspectModel;
            }
        }
        return null;
    }

    /**
     * Registers aspect models.
     *
     * @param aspectModels the class names of the aspect models to register concatenated and separated with a ':'.
     */
    private static void registerAspectModels(final String aspectModels) {
        if (aspectModels != null) {
            StringTokenizer tokenizer = new StringTokenizer(aspectModels, DELIMITER);
            m_aspectModels = new AspectModel[tokenizer.countTokens()];
            for (int i = 0; i < m_aspectModels.length; i++) {
                final String className = tokenizer.nextToken();
                try {
                    final Class modelClass = ContextClassLoader.loadClass(className);
                    m_aspectModels[i] = (AspectModel) modelClass.newInstance();
                } catch (ClassNotFoundException e) {
                    throw new DefinitionException(
                            "aspect model implementation class not found [" +
                            className + "]: " + e.toString()
                    );
                } catch (Exception e) {
                    throw new DefinitionException(
                            "aspect model implementation class could not be instantiated [" +
                            className +
                            "] - make sure it has a default no argument constructor: " +
                            e.toString()
                    );
                }
            }
        }
    }
}
