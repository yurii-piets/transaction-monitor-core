package com.tmc.connection.pool.def;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionPool {

    Connection acquire(String qualifier) throws SQLException;
    void release(Connection connection);
}
