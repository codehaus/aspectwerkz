/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
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
