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

    private final ConfigurableBeanFactory configurableBeanFactory;

    private final DatabasePropertyService databasePropertyService;

    private final Environment env;

    @Autowired
    public DatabaseConfig(ConfigurableBeanFactory configurableBeanFactory, DatabasePropertyService databasePropertyService, Environment env) {
        this.configurableBeanFactory = configurableBeanFactory;
        this.databasePropertyService = databasePropertyService;
        this.env = env;
    }

    @PostConstruct
    public void configure() {
        for (String qualifier : databasePropertyService.getQualifiers()) {
            DataSource dataSource = dataSource(qualifier);
            configurableBeanFactory.registerSingleton(qualifier, dataSource);
        }
    }

    private final static String URL_PATTERN = "{qualifier}.url";
    private final static String USERNAME_PATTERN = "{qualifier}.username";
    private final static String PASSWORD_PATTERN = "{qualifier}.password";
    private final static String DRIVER_CLASSNAME_PATTERN = "{qualifier}.driver-class-name";

    private DataSource dataSource(String qualifier) {
        BasicDataSource ds = new BasicDataSource();

        String replaceUrl = URL_PATTERN.replace("{qualifier}", qualifier);
        String replaceUsername = USERNAME_PATTERN.replace("{qualifier}", qualifier);
        String replaceDriverClass = DRIVER_CLASSNAME_PATTERN.replace("{qualifier}", qualifier);
        String replacePassword = PASSWORD_PATTERN.replace("{qualifier}", qualifier);

        String url = env.getRequiredProperty(replaceUrl);
        String username = env.getRequiredProperty(replaceUsername);
        String password = env.getRequiredProperty(replacePassword);
        String driverClassName = env.getRequiredProperty(replaceDriverClass);

        ds.setUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setDriverClassName(driverClassName);
        return ds;
    }
}