/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.definition;

/**
 * Holds the definition of the transaction demarcation for a specific method.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class TransactionDefinition implements Definition {

    public static final String TRANSACTION = "transaction";
    public static final String TX_REQUIRES = "requires";
    public static final String TX_REQUIRES_NEW = "requires-new";

    private String m_txType;

    public String getTransactionType() {
        return m_txType;
    }

    public void setTransactionType(final String transactionType) {
        m_txType = transactionType;
    }

    public boolean isTxRequires() {
        return m_txType.equals(TX_REQUIRES);
    }

    public boolean isTxRequiresNew() {
        return m_txType.equals(TX_REQUIRES_NEW);
    }
}
