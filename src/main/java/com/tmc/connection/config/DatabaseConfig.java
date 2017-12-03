package com.tmc.connection.config;

import com.tmc.ApplicationContext;
import com.tmc.connection.services.PropertyService;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;

/**
 * Configuration of database
 */
public class DatabaseConfig {

    private final static String URL_PATTERN = "{qualifier}.url";
    private final static String USERNAME_PATTERN = "{qualifier}.username";
    private final static String PASSWORD_PATTERN = "{qualifier}.password";
    private final static String DRIVER_CLASSNAME_PATTERN = "{qualifier}.driver-class-name";
    private static final String QUALIFIER_PATTERN = "{qualifier}";

    private final ApplicationContext applicationContext;

    private final PropertyService propertyService;

    public DatabaseConfig(ApplicationContext applicationContext,
                          PropertyService propertyService) {
        this.applicationContext = applicationContext;
        this.propertyService = propertyService;

        configure();
    }

    /**
     * Declares a DataSource Bean for each qualifier of a database
     *
     * @see DataSource
     */
    private void configure() {
        for (String qualifier : propertyService.getQualifiers()) {
            DataSource dataSource = dataSource(qualifier);
            applicationContext.addDataSource(qualifier, dataSource);
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

        basicDataSource.setUrl(propertyService.getRequiredProperty(URL_PATTERN.replace(QUALIFIER_PATTERN, qualifier)));
        basicDataSource.setUsername(propertyService.getRequiredProperty(USERNAME_PATTERN.replace(QUALIFIER_PATTERN, qualifier)));
        basicDataSource.setPassword(propertyService.getRequiredProperty(PASSWORD_PATTERN.replace(QUALIFIER_PATTERN, qualifier)));
        basicDataSource.setDriverClassName(propertyService.getRequiredProperty(DRIVER_CLASSNAME_PATTERN.replace(QUALIFIER_PATTERN, qualifier)));

        return basicDataSource;
    }
}