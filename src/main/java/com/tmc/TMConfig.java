package com.tmc;

import com.tmc.transaction.service.TransactionService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * TMConfig defines a Spring Context to make possible usage of Java Beans and Spring Components injections
 * and using all Spring framework features
 */
@SpringBootApplication
public class TMConfig {

    /**
     * @param args the application arguments (usually passed from a Java main method)
     * @return new instance TransactionService
     * @see TransactionService
     */
    // TODO: 21/11/2017 think about how to make SpringApplication.run called only one time
    public static TransactionService boot(String... args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(TMConfig.class, args);
        return new TransactionService(applicationContext);
    }
}
