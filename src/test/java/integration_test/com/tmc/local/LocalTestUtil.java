package integration_test.com.tmc.local;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static integration_test.com.tmc.ConnectionProperties.*;

class LocalTestUtil {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private Connection connectionTmone;

    private Connection connectionTmtwo;

    LocalTestUtil() throws SQLException {
        initDataSources();
    }

    private void initDataSources() throws SQLException {
        initDataSourceTmone();
        initDataSourceTmtwo();
    }

    private void initDataSourceTmone() throws SQLException {
        BasicDataSource dataSourceTmone = new BasicDataSource();
        dataSourceTmone.setUrl(TMONE_URL);
        dataSourceTmone.setUsername(TMONE_USER);
        dataSourceTmone.setPassword(TMONE_PASSWORD);
        dataSourceTmone.setDriverClassName(TMONE_DRIVER);
        connectionTmone = dataSourceTmone.getConnection();
    }

    private void initDataSourceTmtwo() throws SQLException {
        BasicDataSource dataSourceTmtwo = new BasicDataSource();
        dataSourceTmtwo.setUrl(TMTWO_URL);
        dataSourceTmtwo.setUsername(TMTWO_USER);
        dataSourceTmtwo.setPassword(TMTWO_PASSWORD);
        dataSourceTmtwo.setDriverClassName(TMTWO_DRIVER);
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
