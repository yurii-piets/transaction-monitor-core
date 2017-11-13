package com.tmc.config;

import com.tmc.services.DatabasePropertyService;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@Component
public class DatabaseConfig {

    private final static String URL_PATTERN = "{qualifier}.url";
    private final static String USERNAME_PATTERN = "{qualifier}.username";
    private final static String PASSWORD_PATTERN = "{qualifier}.password";
    private final static String DRIVER_CLASSNAME_PATTERN = "{qualifier}.driver-class-name";
    private static final String QUALIFIER_PATTERN = "{qualifier}";

    private final ConfigurableBeanFactory configurableBeanFactory;

    private final DatabasePropertyService databasePropertyService;

    private final Environment environment;

    @Autowired
    public DatabaseConfig(ConfigurableBeanFactory configurableBeanFactory,
                          DatabasePropertyService databasePropertyService,
                          Environment environment) {
        this.configurableBeanFactory = configurableBeanFactory;
        this.databasePropertyService = databasePropertyService;
        this.environment = environment;
    }

    @PostConstruct
    public void configure() {
        for (String qualifier : databasePropertyService.getQualifiers()) {
            DataSource dataSource = dataSource(qualifier);
            configurableBeanFactory.registerSingleton(qualifier, dataSource);
        }
    }

    private DataSource dataSource(String qualifier) {
        BasicDataSource ds = new BasicDataSource();

        ds.setUrl(environment.getRequiredProperty(URL_PATTERN.replace(QUALIFIER_PATTERN, qualifier)));
        ds.setUsername(environment.getRequiredProperty(USERNAME_PATTERN.replace(QUALIFIER_PATTERN, qualifier)));
        ds.setPassword(environment.getRequiredProperty(PASSWORD_PATTERN.replace(QUALIFIER_PATTERN, qualifier)));
        ds.setDriverClassName(environment.getRequiredProperty(DRIVER_CLASSNAME_PATTERN.replace(QUALIFIER_PATTERN, qualifier)));

        return ds;
    }
}