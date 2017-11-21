package com.tmc.transaction.service;

import com.tmc.transaction.core.def.Transaction;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Serviced that is a proxy class between a Spring context of TM library and
 * user's no context application
 */
public class TransactionService {

    private final ConfigurableApplicationContext applicationContext;

    public TransactionService(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * @return on each demand returns new instance of a Transaction
     * @see Transaction
     */
    public Transaction newTransaction() {
        return applicationContext.getBean(Transaction.class);
    }
}
