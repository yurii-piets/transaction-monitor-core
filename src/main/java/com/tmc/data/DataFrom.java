package com.tmc.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.SQLException;

@Service
@Component
public class DataFrom {

    private final DataSource dataSource;

    @Autowired
    public DataFrom(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void dbConnect() throws SQLException {
        dataSource.getConnection();
    }
}
