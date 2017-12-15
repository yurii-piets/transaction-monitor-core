package com.tmc.connection.config;

import com.tmc.connection.services.PropertyService;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Configuration of database
 */
public class DatabaseConfig {

    private final static String URL_PATTERN = "{qualifier}.url";
    private final static String USERNAME_PATTERN = "{qualifier}.username";
    private final static String PASSWORD_PATTERN = "{qualifier}.password";
    private final static String DRIVER_CLASSNAME_PATTERN = "{qualifier}.driver-class-name";
    private final static String QUALIFIER_PATTERN = "{qualifier}";

    private final Map<String, DataSource> dataSources;

    private final PropertyService propertyService;

    public DatabaseConfig(Map<String, DataSource> dataSources,
                          PropertyService propertyService) {
        this.dataSources = dataSources;
        this.propertyService = propertyService;

        configureDataSources();
    }

    /**
     * Declares a DataSource objects for each qualifier of a database
     *
     * @see DataSource
     */
    private void configureDataSources() {
        for (String qualifier : propertyService.getQualifiers()) {
            DataSource dataSource = dataSource(qualifier);
            dataSources.put(qualifier, dataSource);
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