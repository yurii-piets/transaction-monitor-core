package com.tmc.connection.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
// TODO: 16/11/2017 think about better name
public class DataConnection {

    private final ConfigurableApplicationContext applicationContext;

    private final Map<String, Connection> cachedConnections = new HashMap<>();

    @Autowired
    public DataConnection(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public Set<Connection> getAllConnections() {
        Map<String, DataSource> dataSourcesBeans = applicationContext.getBeansOfType(DataSource.class);
        Set<Connection> connectionsSet = new HashSet<>();

        for (String key: dataSourcesBeans.keySet()){
            if(cachedConnections.containsKey(key)) {
                connectionsSet.add(cachedConnections.get(key));
            } else {
                cachedConnections.put(key, cachedConnections.get(key));
                connectionsSet.add(cachedConnections.get(key));
            }
        }

        return connectionsSet;
    }

    public Connection getConnectionByQualifier(String qualifier) {
        if(cachedConnections.containsKey(qualifier)) {
            Connection connection = cachedConnections.get(qualifier);
            return connection;
        }

        DataSource dataSource = applicationContext.getBean(qualifier, DataSource.class);
        if(dataSource == null) {
            throw new IllegalArgumentException("Database qualifier: [" + qualifier + "] does not exist");
        }


        Connection connection = null;
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            // TODO: 16/11/2017 refactor this
            e.printStackTrace();
        }
        cachedConnections.put(qualifier, connection);
        return connection;
    }

    public void clearCache(){
        cachedConnections.clear();
    }
}
