/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.codehaus.aspectwerkz.advice;

/**
 * Is invoked before that a specific join point (method or field) is executed.
 * <p/>
 * Supports four different deployment models:
 *     PER_JVM, PER_CLASS, PER_INSTANCE and PER_THREAD.<br/>
 *
 * The PER_JVM deployment model performance a bit better than the other models
 * since no synchronization and object creation is needed.
 *
 * @see aspectwerkz.DeploymentModel
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: PreAdvice.java,v 1.1.1.1 2003-05-11 15:13:40 jboner Exp $
 */
public abstract class PreAdvice extends AbstractPrePostAdvice {

    /**
     * Sets the class of the class.
     */
    public PreAdvice() {
        super();
    }
}
