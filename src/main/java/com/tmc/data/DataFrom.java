package com.tmc.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DataFrom {

    private final ConfigurableApplicationContext applicationContext;

    @Autowired
    public DataFrom(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public Connection getConnectionByQualifier(String qualifier) throws SQLException {
        DataSource dataSource = applicationContext.getBean(qualifier, DataSource.class);
        return dataSource.getConnection();
    }
}
