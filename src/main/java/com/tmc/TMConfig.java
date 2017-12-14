package com.tmc;

import com.tmc.transaction.service.TransactionService;

public class TMConfig {

    /**
     * @return new instance TransactionService
     * @see TransactionService
     */
    public static TransactionService boot() {
        return ApplicationContext.transactionService();
    }
}
