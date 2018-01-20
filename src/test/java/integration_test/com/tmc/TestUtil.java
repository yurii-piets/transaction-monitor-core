package integration_test.com.tmc;

import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static integration_test.com.tmc.ConnectionProperties.TMFOUR_DRIVER;
import static integration_test.com.tmc.ConnectionProperties.TMFOUR_URL;
import static integration_test.com.tmc.ConnectionProperties.TMONE_DRIVER;
import static integration_test.com.tmc.ConnectionProperties.TMONE_PASSWORD;
import static integration_test.com.tmc.ConnectionProperties.TMONE_QUALIFIER;
import static integration_test.com.tmc.ConnectionProperties.TMONE_URL;
import static integration_test.com.tmc.ConnectionProperties.TMONE_USER;
import static integration_test.com.tmc.ConnectionProperties.TMTHREE_DRIVER;
import static integration_test.com.tmc.ConnectionProperties.TMTHREE_PASSWORD;
import static integration_test.com.tmc.ConnectionProperties.TMTHREE_URL;
import static integration_test.com.tmc.ConnectionProperties.TMTHREE_USER;
import static integration_test.com.tmc.ConnectionProperties.TMTWO_DRIVER;
import static integration_test.com.tmc.ConnectionProperties.TMTWO_PASSWORD;
import static integration_test.com.tmc.ConnectionProperties.TMTWO_QUALIFIER;
import static integration_test.com.tmc.ConnectionProperties.TMTWO_URL;
import static integration_test.com.tmc.ConnectionProperties.TMTWO_USER;

public class TestUtil {

    private Map<String, DataSource> dataSources = new HashMap<>();

    private Map<String, Connection> connections = new HashMap<>();

    public ResultSet resultSetForSqlQuery(String qualifier, String sql) throws SQLException {
        Connection connection = connection(qualifier);

        Statement statement = connection.createStatement();
        if (statement != null) {
            return statement.executeQuery(sql);
        }

        return null;
    }

    private Connection connection(String qualifier) throws SQLException {
        Connection connection = connections.get(qualifier);

        if (connection == null) {
            DataSource dataSource = dataSource(qualifier);

            if (dataSource != null) {
                connection = dataSource.getConnection();
                connections.put(qualifier, connection);
            }
        }

        return connection;
    }

    private DataSource dataSource(String qualifier) {
        return dataSources.computeIfAbsent(qualifier, s -> createDataSource(qualifier));
    }

    private DataSource createDataSource(String qualifier) {
        switch (qualifier) {
            case TMONE_QUALIFIER:
                return dataSourceTmone();

            case TMTWO_QUALIFIER:
                return dataSourceTmtwo();

            case TMTHREE_DRIVER:
                return dataSourceTmthree();

            case TMFOUR_DRIVER:
                return dataSourceTmfour();

            default:
                throw new IllegalStateException("Unknown database qualifier.");
        }
    }


    private DataSource dataSourceTmone() {
        BasicDataSource dataSource = new BasicDataSource();

        dataSource.setUrl(TMONE_URL);
        dataSource.setUsername(TMONE_USER);
        dataSource.setPassword(TMONE_PASSWORD);
        dataSource.setDriverClassName(TMONE_DRIVER);

        return dataSource;
    }

    private DataSource dataSourceTmtwo() {
        BasicDataSource dataSource = new BasicDataSource();

        dataSource.setUrl(TMTWO_URL);
        dataSource.setUsername(TMTWO_USER);
        dataSource.setPassword(TMTWO_PASSWORD);
        dataSource.setDriverClassName(TMTWO_DRIVER);

        return dataSource;
    }

    private DataSource dataSourceTmthree() {
        BasicDataSource dataSource = new BasicDataSource();

        dataSource.setUrl(TMTHREE_URL);
        dataSource.setUsername(TMTHREE_USER);
        dataSource.setPassword(TMTHREE_PASSWORD);
        dataSource.setDriverClassName(TMTHREE_DRIVER);

        return dataSource;
    }

    private DataSource dataSourceTmfour() {
        BasicDataSource dataSource = new BasicDataSource();

        dataSource.setUrl(TMFOUR_URL);
        dataSource.setDriverClassName(TMFOUR_DRIVER);

        return dataSource;
    }
}
