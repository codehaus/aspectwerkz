/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.unitofwork;

import aspectwerkz.aosd.unitofwork.PersistableUnitOfWork;
import aspectwerkz.aosd.unitofwork.UnitOfWork;

/**
 * Factory for the UnitOfWork implementations.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class UnitOfWorkFactory {

    /**
     * Returns an new UnitOfWork instance.
     *
     * @param type the type of UnitOfWork
     * @return the UnitOfWork
     */
    public static UnitOfWork newInstance(final UnitOfWorkType type) {
        if (type.equals(UnitOfWorkType.DEFAULT)) {
            return new UnitOfWork();
        }
        else if (type.equals(UnitOfWorkType.PERSISTABLE)) {
            return new PersistableUnitOfWork();
        }
        else if (type.equals(UnitOfWorkType.JTA_AWARE)) {
            return new JtaAwareUnitOfWork();
        }
        else {
            return new UnitOfWork();
        }
    }
}

