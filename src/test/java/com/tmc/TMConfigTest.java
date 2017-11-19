package com.tmc;

import com.tmc.transaction.core.def.Transaction;
import com.tmc.transaction.service.TransactionService;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class TMConfigTest {

    private TransactionService transactionService;

    @Before
    public void init(){
        this.transactionService = TMConfig.boot();
    }

    @Test
    public void testBoot() throws Exception {
        assertNotNull(transactionService);
    }

    @Test
    public void transactionsNotEquals() {
        Transaction transaction1 = transactionService.getTransaction();
        Transaction transaction2 = transactionService.getTransaction();

        assertNotEquals(transaction1, transaction2);
    }
}