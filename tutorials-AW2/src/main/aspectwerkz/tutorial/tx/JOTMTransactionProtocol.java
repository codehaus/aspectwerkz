/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.tutorial.tx;

import org.objectweb.jotm.Jotm;
import org.codehaus.aspectwerkz.annotation.Aspect;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.naming.NamingException;

/**
 * Concrete JTA ObjectWeb JOTM based protocol.
 *
 * We currently limit the JOTM manager to support transaction within one local JVM.
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
@Aspect("perJVM")
public class JOTMTransactionProtocol extends TransactionAttributeAwareTransactionProtocol {

    private final TransactionManager m_transactionManager;
    private final Jotm m_jotm;

    public JOTMTransactionProtocol() {
        try {
            m_jotm = new Jotm(true, false);
            m_transactionManager = m_jotm.getTransactionManager();
        } catch (NamingException e) {
            throw new TransactionException("Could not create a new JOTM Transaction Manager", e);
        }
    }

    protected TransactionManager getTransactionManager() {
        return m_transactionManager;
    }

    protected UserTransaction getUserTransaction() {
        return m_jotm.getUserTransaction();
    }
}

