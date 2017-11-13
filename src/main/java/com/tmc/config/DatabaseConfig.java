package com.tmc.config;

import com.tmc.services.DatabasePropertyService;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.PropertySource;
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

    private final PropertySourcesPlaceholderConfigurer pspc;

    @Autowired
    public DatabaseConfig(ConfigurableBeanFactory configurableBeanFactory,
                          DatabasePropertyService databasePropertyService,
                          PropertySourcesPlaceholderConfigurer pspc) {
        this.configurableBeanFactory = configurableBeanFactory;
        this.databasePropertyService = databasePropertyService;
        this.pspc = pspc;
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

        ds.setUrl(getRequiredProperty(URL_PATTERN.replace(QUALIFIER_PATTERN, qualifier)));
        ds.setUsername(getRequiredProperty(USERNAME_PATTERN.replace(QUALIFIER_PATTERN, qualifier)));
        ds.setPassword(getRequiredProperty(PASSWORD_PATTERN.replace(QUALIFIER_PATTERN, qualifier)));
        ds.setDriverClassName(getRequiredProperty(DRIVER_CLASSNAME_PATTERN.replace(QUALIFIER_PATTERN, qualifier)));

        return ds;
    }

    private String getRequiredProperty(String key){
        if(!pspc.getAppliedPropertySources().contains("localProperties")) {
            throw new IllegalStateException("localProperties" + " are not defined");
        }

        PropertySource<?> localProperties = pspc.getAppliedPropertySources().get("localProperties");

        if(!localProperties.containsProperty(key)){
            throw new IllegalStateException("Property [\"" + key + "\"] is not defined");
        }

        Object propertyValue = localProperties.getProperty(key);

        if(propertyValue == null){
            throw new IllegalStateException("Property [\"" + key + "\"] has null value");
        }

        return propertyValue.toString();
    }
}