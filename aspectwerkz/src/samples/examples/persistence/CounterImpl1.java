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

import java.util.List;
import java.util.ArrayList;

public class CounterImpl1 extends AbstractCounterImpl implements Counter1 {

    private static final long serialVersionUID = 1L;

    private int m_counter = 0;
    private List m_list = new ArrayList();

    public CounterImpl1() {
        super();
    }

    public CounterImpl1(int i) {
        super(i);
    }

    public int getCounter1() {
        return m_counter;
    }

    public void increment1() {
        m_counter++;
    }

    public void addItem(String str) {
        m_list.add(str);
    }

    public List getList() {
        return m_list;
    }

    public Object getItem(int index) {
        return m_list.get(index);
    }
}
