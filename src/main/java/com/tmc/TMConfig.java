package com.tmc;

import com.tmc.transaction.factory.TransactionService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * TMConfig defines a Spring Context to make possible usage of Java Beans and Spring Components injections
 * and using all Spring framework features
 */
@SpringBootApplication
public class TMConfig {

    public static TransactionService boot(String... args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(TMConfig.class, args);
        return new TransactionService(applicationContext);
    }
}
