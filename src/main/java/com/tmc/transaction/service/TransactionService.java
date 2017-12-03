package com.tmc.transaction.service;

import com.tmc.ApplicationContext;
import com.tmc.transaction.core.def.Transaction;
import com.tmc.transaction.core.impl.TransactionImpl;

public class TransactionService {

    private final ApplicationContext applicationContext;

    public TransactionService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * @return on each demand returns new instance of a Transaction
     * @see Transaction
     */
    public Transaction newTransaction() {
        return new TransactionImpl(
                applicationContext.connectionService(),
                applicationContext.propertyService(),
                applicationContext.commandsExecutor()
        );
    }
}
