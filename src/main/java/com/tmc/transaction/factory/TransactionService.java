package com.tmc.transaction.factory;

import com.tmc.transaction.core.def.Transaction;
import org.springframework.context.ConfigurableApplicationContext;

public class TransactionService {

    private final ConfigurableApplicationContext applicationContext;

    public TransactionService(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public Transaction getTransaction() {
        return applicationContext.getBean(Transaction.class);
    }
}
