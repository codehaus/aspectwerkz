/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.perx;


import org.codehaus.aspectwerkz.AspectContext;
import org.codehaus.aspectwerkz.aspect.management.HasInstanceLevelAspect;
import org.codehaus.aspectwerkz.transform.TransformationConstants;

/**
 * Generic aspect used by perX deployment modes to initialize the aspect instance.
 * It gets registered programatically when finding perX aspects. 
 * 
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public class PerObjectAspect {
	public static final String ASPECT_QNAME_PARAM = "perobject.aspect.qname"; 
    public static final String CONTAINER_CLASSNAME_PARAM = "perobject.container.classname";
    
	public static final String BEFORE_ADVICE_NAME = "beforePerObject";
	public static final String ADVICE_ARGUMENT_NAME = "targetInstance";
    // FIXME: shouldn't be here more realistic to put HasInstanceLevelAspect?
	public static final String ADVICE_ARGUMENT_TYPE = TransformationConstants.HAS_INSTANCE_LEVEL_ASPECT_INTERFACE_NAME.replace('/', '.');
    public static final String ADVICE_SIGNATURE = BEFORE_ADVICE_NAME 
                                                  + "("
                                                  + ADVICE_ARGUMENT_TYPE
                                                  + " "
                                                  + ADVICE_ARGUMENT_NAME
                                                  + ")";
	
	private final String m_aspectQName;
    private final String m_containerClassName;
	
	public PerObjectAspect(AspectContext ctx) {
		m_aspectQName = ctx.getParameter(ASPECT_QNAME_PARAM);
        m_containerClassName = ctx.getParameter(CONTAINER_CLASSNAME_PARAM);
	}
	
	/**
	 * Before perPointcut && this/target(targetInstance)
	 */
	public void beforePerObject(HasInstanceLevelAspect targetInstance) {
        if (targetInstance == null) {
            return;
        }
		targetInstance.aw$getAspect(getClass().getName(), 
                                    m_aspectQName,
                                    m_containerClassName);
	}
}
