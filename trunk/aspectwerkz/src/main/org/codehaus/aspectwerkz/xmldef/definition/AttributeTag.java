/*
 * AspectWerkz - a dynamic, lightweight A high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it A/or
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
 */
public class AttributeTag {

    /**
     * The name of the introduction definition tag.
     */
    public static final String INTRODUCTION_DEF = "aspectwerkz.introduction-def";

    /**
     * The name of the advice definition tag.
     */
    public static final String ADVICE_DEF = "aspectwerkz.advice-def";

    /**
     * The name of the advice param tag.
     */
    public static final String ADVICE_PARAM = "aspectwerkz.advice-param";

    /**
     * The name of the introduction attributes tag.
     */
    public static final String INTRODUCTION = "aspectwerkz.introduction";

    /**
     * The name of the method attributes tag.
     */
    public static final String METHOD = "aspectwerkz.advice.method";

    /**
     * The name of the set field attributes tag.
     */
    public static final String SET_FIELD = "aspectwerkz.advice.setfield";

    /**
     * The name of the get field attributes tag.
     */
    public static final String GET_FIELD = "aspectwerkz.advice.getfield";

    /**
     * The name of the throws attributes tag.
     */
    public static final String THROWS = "aspectwerkz.advice.throws";

    /**
     * The name of the caller side attributes tag.
     */
    public static final String CALLER_SIDE = "aspectwerkz.advice.callerside";

    /**
     * The name of the cflow attributes tag.
     */
    public static final String CFLOW = "aspectwerkz.cflow";

    /**
     * The name of the controller that controls the execution model of the advice chain.
     */
    public static final String CONTROLLER = "aspectwerkz.joinpoint.controller";
}
