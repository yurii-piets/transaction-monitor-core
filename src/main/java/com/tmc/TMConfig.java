package com.tmc;

import com.tmc.transaction.service.TransactionService;

public class TMConfig {

    /**
     * @return the instance of TransactionService
     * @see TransactionService
     */
    public static TransactionService boot() {
        return ApplicationContext.transactionService();
    }
}
