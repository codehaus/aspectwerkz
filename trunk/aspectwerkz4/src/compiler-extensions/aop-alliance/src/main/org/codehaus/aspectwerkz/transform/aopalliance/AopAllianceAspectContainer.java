/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.aopalliance;

import org.codehaus.aspectwerkz.aspect.AbstractAspectContainer;
import org.codehaus.aspectwerkz.AspectContext;
import org.codehaus.aspectwerkz.ContextClassLoader;

/**
 * Default aspect container for the AOP Alliance compliant aspects, can only handle aspects/interceptors
 * with no argument default constructors.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class AopAllianceAspectContainer extends AbstractAspectContainer {

    /**
     * Creates a new aspect container that instantiates AOP Alliance interceptors.
     *
     * @param ctx the aspect context
     */
    public AopAllianceAspectContainer(final AspectContext ctx) {
        super(ctx);
    }

    /**
     * Creates a new aspect (interceptor) instance.
     *
     * @return the new aspect (interceptor) instance
     */
    protected Object createAspect() {
        final String className = m_aspectContext.getAspectDefinition().getClassName();
        try {
            return ContextClassLoader.loadClass(className).newInstance();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("could not load AOP Alliance interceptor [" + className + "]: " + e.toString());
        } catch (Exception e) {
            throw new RuntimeException("could not instantiate AOP Alliance interceptor [" + className + "]: " + e.toString());
        }

    }
}
