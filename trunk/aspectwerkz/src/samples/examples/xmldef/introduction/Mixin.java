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
package examples.introduction;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: Mixin.java,v 1.5 2003-07-04 13:05:05 jboner Exp $
 *
 * If you want to try out attributes for the introduction then you can use this
 * tag (add an AT character in front of the tag and remove the xml definition)
 *
 * aspectwerkz.introduction-def name=mixin
 *                               implementation=examples.introduction.MixinImpl
 *                               deployment-model=perInstance
 *                               attribute=mixin
 */
public interface Mixin {
    String sayHello();
}
