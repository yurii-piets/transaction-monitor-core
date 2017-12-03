package com.tmc.connection.config;

import com.tmc.connection.services.PropertyService;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * Configuration of database
 */
@Component
public class DatabaseConfig {

    private final static String URL_PATTERN = "{qualifier}.url";
    private final static String USERNAME_PATTERN = "{qualifier}.username";
    private final static String PASSWORD_PATTERN = "{qualifier}.password";
    private final static String DRIVER_CLASSNAME_PATTERN = "{qualifier}.driver-class-name";
    private static final String QUALIFIER_PATTERN = "{qualifier}";

    private final ConfigurableBeanFactory configurableBeanFactory;

    private final PropertyService propertyService;

    private final PropertySourcesPlaceholderConfigurer pspc;

    @Autowired
    public DatabaseConfig(ConfigurableBeanFactory configurableBeanFactory,
                          PropertyService propertyService,
                          PropertySourcesPlaceholderConfigurer pspc) {
        this.configurableBeanFactory = configurableBeanFactory;
        this.propertyService = propertyService;
        this.pspc = pspc;
    }

    /**
     * Declares a DataSource Bean for each qualifier of a database
     *
     * @see DataSource
     */
    @PostConstruct
    public void configure() {
        for (String qualifier : propertyService.getQualifiers()) {
            DataSource dataSource = dataSource(qualifier);
            configurableBeanFactory.registerSingleton(qualifier, dataSource);
        }
    }

    /**
     * Creates a DataSource instance for each qualifier of a database
     *
     * @return DataSource class instance
     * @see DataSource
     */
    private DataSource dataSource(String qualifier) {
        BasicDataSource basicDataSource = new BasicDataSource();

        basicDataSource.setUrl(getRequiredProperty(URL_PATTERN.replace(QUALIFIER_PATTERN, qualifier)));
        basicDataSource.setUsername(getRequiredProperty(USERNAME_PATTERN.replace(QUALIFIER_PATTERN, qualifier)));
        basicDataSource.setPassword(getRequiredProperty(PASSWORD_PATTERN.replace(QUALIFIER_PATTERN, qualifier)));
        basicDataSource.setDriverClassName(getRequiredProperty(DRIVER_CLASSNAME_PATTERN.replace(QUALIFIER_PATTERN, qualifier)));

        return basicDataSource;
    }

    /**
     * Returns value of a property from configured properties files
     *
     * @param key - value is accessed by
     * @return value of a property
     */
    private String getRequiredProperty(String key) {
        if (!pspc.getAppliedPropertySources().contains("localProperties")) {
            throw new IllegalStateException("localProperties are not defined");
        }

        PropertySource<?> localProperties = pspc.getAppliedPropertySources().get("localProperties");

        if (!localProperties.containsProperty(key)) {
            throw new IllegalStateException("Property [\"" + key + "\"] is not defined");
        }

        Object propertyValue = localProperties.getProperty(key);

        if (propertyValue == null) {
            throw new IllegalStateException("Property [\"" + key + "\"] has null path");
        }

        return propertyValue.toString();
    }
}