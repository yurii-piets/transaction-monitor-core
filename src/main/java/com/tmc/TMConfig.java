package com.tmc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * TMConfig defines a Spring Context to make possible usage of Java Beans and Spring Components injections
 * and using all Spring framework features
 */
@SpringBootApplication
public class TMConfig {

    private ConfigurableApplicationContext applicationContext;

    public void boot(String[] args) {
        applicationContext = SpringApplication.run(TMConfig.class, args);
    }
}
