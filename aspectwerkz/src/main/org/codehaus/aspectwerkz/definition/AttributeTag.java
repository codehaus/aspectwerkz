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
package org.codehaus.aspectwerkz.definition;

/**
 * Holds the attribute tag definitions.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: AttributeTag.java,v 1.1 2003-06-09 07:05:24 jboner Exp $
 */
public class AttributeTag {

    /**
     * The name of the introduction definition tag.
     */
    public static final String INTRODUCTION_DEF = "introduction-def";

    /**
     * The name of the advice definition tag.
     */
    public static final String ADVICE_DEF = "advice-def";

    /**
     * The name of the advice param tag.
     */
    public static final String ADVICE_PARAM = "advice-param";

    /**
     * The name of the introduction attributes tag.
     */
    public static final String INTRODUCTION = "introduction";

    /**
     * The name of the method attributes tag.
     */
    public static final String ADVICE_METHOD = "advice:method";

    /**
     * The name of the set field attributes tag.
     */
    public static final String ADVICE_SET_FIELD = "advice:setfield";

    /**
     * The name of the get field attributes tag.
     */
    public static final String ADVICE_GET_FIELD = "advice:getfield";

    /**
     * The name of the throws attributes tag.
     */
    public static final String ADVICE_THROWS = "advice:throws";

    /**
     * The name of the caller side attributes tag.
     */
    public static final String ADVICE_CALLER_SIDE = "advice:callerside";
}
