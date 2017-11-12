package com.tmc;

import com.tmc.services.DatabasePropertyService;
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

    public void init(String[] args) {
        applicationContext = SpringApplication.run(TMConfig.class, args);
    }

    public void testPropertySources(){
        DatabasePropertyService propertyService = applicationContext.getBean(DatabasePropertyService.class);
        propertyService.getNames();
    }
}
