package com.tmc.transaction.service;

import com.tmc.transaction.core.def.Transaction;
import org.springframework.context.ConfigurableApplicationContext;

public class TransactionService {

    private final ConfigurableApplicationContext applicationContext;

    public TransactionService(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public Transaction newTransaction() {
        return applicationContext.getBean(Transaction.class);
    }
}
