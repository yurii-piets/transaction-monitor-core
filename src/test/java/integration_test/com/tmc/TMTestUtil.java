package integration_test.com.tmc;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

class TMTestUtil {

    private static final String TMONE_USER = System.getenv().get("PG_USER");
    private static final String TMONE_PASSWORD = System.getenv().get("PG_PASSWORD");
    private static final String TMTWO_USER = System.getenv().get("PG_USER");
    private static final String TMTWO_PASSWORD = System.getenv().get("PG_PASSWORD");

    private final Logger logger = LogManager.getLogger(this.getClass());

    private Connection connectionTmone;

    private Connection connectionTmtwo;

    TMTestUtil() throws SQLException {
        initDataSources();
    }

    private void initDataSources() throws SQLException {
        initDataSourceTmone();
        initDataSourceTmtwo();
    }

    private void initDataSourceTmone() throws SQLException {
        BasicDataSource dataSourceTmone = new BasicDataSource();
        dataSourceTmone.setUrl("jdbc:postgresql://localhost:5432/tmone");
        dataSourceTmone.setUsername(TMONE_USER);
        dataSourceTmone.setPassword(TMONE_PASSWORD);
        dataSourceTmone.setDriverClassName("org.postgresql.Driver");
        connectionTmone = dataSourceTmone.getConnection();
    }

    private void initDataSourceTmtwo() throws SQLException {
        BasicDataSource dataSourceTmtwo = new BasicDataSource();
        dataSourceTmtwo.setUrl("jdbc:postgresql://localhost:5432/tmtwo");
        dataSourceTmtwo.setUsername(TMTWO_USER);
        dataSourceTmtwo.setPassword(TMTWO_PASSWORD);
        dataSourceTmtwo.setDriverClassName("org.postgresql.Driver");
        connectionTmtwo = dataSourceTmtwo.getConnection();
    }

    ResultSet getTmOneQueryResult(String sql){
        try {
            return connectionTmone.createStatement().executeQuery(sql);
        } catch (SQLException e) {
           logger.error("Unexpected: ", e);
        }
        return null;
    }

    ResultSet getTmTwoQueryResult(String sql){
        try {
            return connectionTmtwo.createStatement().executeQuery(sql);
        } catch (SQLException e) {
           logger.error("Unexpected: ", e);
        }
        return null;
    }
}
