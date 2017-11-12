package com.tmc.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DataFrom {

    @Autowired
    @Qualifier("tmone")
    private DataSource dataSource;

    public Connection dbConnect() throws SQLException {
        return dataSource.getConnection();
    }
}
