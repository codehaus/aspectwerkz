/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.xmldef.introduction;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 *
 * If you want to try out attributes for the introduction then you can use this
 * tag (add an AT character in front of the tag and remove the xml definition)
 *
 * @aspectwerkz.introduction-def name=mixin
 *                               implementation=examples.xmldef.introduction.MixinImpl
 *                               deployment-model=perInstance
 *                               attribute=mixin
 */
public interface Mixin {
    String sayHello();
}
