/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition2;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.io.File;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.definition2.XmlDefinitionParser2;
import org.codehaus.aspectwerkz.definition2.AspectWerkzDefinition2;
import org.codehaus.aspectwerkz.definition2.AspectDefinition2;
import org.codehaus.aspectwerkz.definition2.AspectAttributeParser;

/**
 * A repository for the aspect definitions.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AspectRepository {

    /**
     * The AspectWerkz definition.
     */
    private final AspectWerkzDefinition2 m_definition;

    /**
     * Creates a new aspect repository.
     *
     * @param definition the AspectWerkz definition
     * @param loader the class loader to use
     */
    public AspectRepository(final AspectWerkzDefinition2 definition, final ClassLoader loader) {
        m_definition = definition;
        AspectAttributeParser parser = new Attrib4jAspectAttributeParser();
        for (Iterator it = m_definition.getAspectClassNames().iterator(); it.hasNext();) {
            loadAspect((String)it.next(), loader, parser);
        }
    }

    /**
     * Loads and parser the aspect.
     *
     * @param aspectClassName the class name of the aspect
     * @param loader the class loader to use
     * @param parser the parser
     */
    private void loadAspect(final String aspectClassName,
                            final ClassLoader loader,
                            final AspectAttributeParser parser) {
        try {
            Class klass = loader.loadClass(aspectClassName);
            final AspectDefinition2 aspectMetaData = parser.parse(klass);
            m_definition.addAspectDef(aspectMetaData);
        }
        catch (ClassNotFoundException e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Returns the AspectWerkz definition.
     *
     * @return the AspectWerkz definition
     */
    public AspectWerkzDefinition2 getDefinition() {
        return m_definition;
    }

    /**
     * Main. For testing purposes.
     *
     * @param args
     */
    public static void main(String[] args) {
        AspectRepository repository = new AspectRepository(
                XmlDefinitionParser2.parse(new File("src/samples2/samples.xml")),
                Thread.currentThread().getContextClassLoader()
        );
        AspectWerkzDefinition2 definition = repository.getDefinition();
        for (Iterator it = definition.getAspectDefinitions().iterator(); it.hasNext();) {
            AspectDefinition2 aspectMetaData = (AspectDefinition2)it.next();
            System.out.println("aspectMetaData = " + aspectMetaData);
        }
    }
}
