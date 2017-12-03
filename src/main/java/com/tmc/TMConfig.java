package com.tmc;

import com.tmc.transaction.service.TransactionService;

/**
 * TMConfig defines a Spring Context to make possible usage of Java Beans and Spring Components injections
 * and using all Spring framework features
 */
public class TMConfig {

    /**
     * @return new instance TransactionService
     * @see TransactionService
     */
    public static TransactionService boot() {
        return ApplicationContext.getTransactionService();
    }
}
