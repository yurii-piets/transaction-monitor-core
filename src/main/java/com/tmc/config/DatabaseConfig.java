package com.tmc.config;

import com.tmc.services.DatabasePropertyService;
import lombok.Setter;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@Configuration
public class DatabaseConfig implements BeanFactoryAware {

    @Setter
    private BeanFactory beanFactory;

    @Autowired
    private DatabasePropertyService databasePropertyService;

    @Autowired
    private Environment env;

    @PostConstruct
    public void configure() {
        Assert.state(beanFactory instanceof ConfigurableBeanFactory, "wrong bean factory type");
        ConfigurableBeanFactory configurableBeanFactory = (ConfigurableBeanFactory) beanFactory;
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
        ds.setUrl(env.getProperty(URL_PATTERN.replace("{qualifier}", qualifier)));
        ds.setUsername(env.getProperty(USERNAME_PATTERN.replace("{qualifier}", qualifier)));
        ds.setPassword(env.getProperty(PASSWORD_PATTERN.replace("{qualifier}", qualifier)));
        ds.setDriverClassName(env.getProperty(DRIVER_CLASSNAME_PATTERN.replace("{qualifier}", qualifier)));
        return ds;
    }
}