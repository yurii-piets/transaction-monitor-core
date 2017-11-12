package com.tmc.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DataFrom {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    public Connection getConnectionByQualifier(String qualifier) throws SQLException {
        DataSource dataSource = applicationContext.getBean(qualifier, DataSource.class);
        return dataSource.getConnection();
    }
}
