package integration_test.com.tmc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static integration_test.com.tmc.DatabaseProperties.Qualifiers.TMFOUR_QUALIFIER;
import static integration_test.com.tmc.DatabaseProperties.Qualifiers.TMONE_QUALIFIER;
import static integration_test.com.tmc.DatabaseProperties.Qualifiers.TMTHREE_QUALIFIER;
import static integration_test.com.tmc.DatabaseProperties.Qualifiers.TMTWO_QUALIFIER;
import static integration_test.com.tmc.DatabaseProperties.TMFOUR_DRIVER;
import static integration_test.com.tmc.DatabaseProperties.TMFOUR_URL;
import static integration_test.com.tmc.DatabaseProperties.TMONE_DRIVER;
import static integration_test.com.tmc.DatabaseProperties.TMONE_PASSWORD;
import static integration_test.com.tmc.DatabaseProperties.TMONE_URL;
import static integration_test.com.tmc.DatabaseProperties.TMONE_USER;
import static integration_test.com.tmc.DatabaseProperties.TMTHREE_DRIVER;
import static integration_test.com.tmc.DatabaseProperties.TMTHREE_PASSWORD;
import static integration_test.com.tmc.DatabaseProperties.TMTHREE_URL;
import static integration_test.com.tmc.DatabaseProperties.TMTHREE_USER;
import static integration_test.com.tmc.DatabaseProperties.TMTWO_DRIVER;
import static integration_test.com.tmc.DatabaseProperties.TMTWO_PASSWORD;
import static integration_test.com.tmc.DatabaseProperties.TMTWO_URL;
import static integration_test.com.tmc.DatabaseProperties.TMTWO_USER;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class TestUtil {

    @Getter
    private final static TestUtil instance = new TestUtil();

    private final Map<String, DataSource> dataSources = new HashMap<>();

    private final Map<String, Connection> connections = new HashMap<>();

    ResultSet resultSetForSqlQuery(String qualifier, String sql) throws SQLException {
        Connection connection = connection(qualifier);

        Statement statement = connection.createStatement();
        return statement.executeQuery(sql);
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
        return dataSources.computeIfAbsent(qualifier, this::createDataSource);
    }

    private DataSource createDataSource(String qualifier) {
        switch (qualifier) {
            case TMONE_QUALIFIER:
                return dataSourceTmone();

            case TMTWO_QUALIFIER:
                return dataSourceTmtwo();

            case TMTHREE_QUALIFIER:
                return dataSourceTmthree();

            case TMFOUR_QUALIFIER:
                return dataSourceTmfour();

            default:
                throw new IllegalStateException("Unknown database qualifier:{ " + qualifier + " }");
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
