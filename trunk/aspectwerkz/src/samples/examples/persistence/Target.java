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
package examples.persistence;

import org.codehaus.aspectwerkz.AspectWerkz;

/**
 * @introduction counter1 counter2
 */
public class Target {

    // CAUTION: lousy hardcoded UUID, only works for testing
    // see: http://aspectwerkz.sourceforge.net/documentation.html#Persistence
    // how to handle the UUID issue in a real-world application
    private String m_uuid = getClass().getName();

    public String getUuid() {
        return m_uuid;
    }

    public void invoke() { // dummy method for counting the invocations
    }

    public static void main(String[] args) {
        AspectWerkz.initialize();
        Target target = new Target();

        target.invoke();

        ((Counter1)target).increment1();
        System.out.println("introduction - persistent int field 1: " +
                ((Counter1)target).getCounter1());

        ((Counter2)target).increment2();
        System.out.println("introduction - persistent int field 2: " +
                ((Counter2)target).getCounter2());

        ((Counter1)target).addItem("item" + ((Counter1)target).getList().size());
        System.out.println("introduction - persistent java.util.List field: " + ((Counter1)target).getList());
    }
}
