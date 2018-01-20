package integration_test.com.tmc.remote;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static integration_test.com.tmc.ConnectionProperties.*;

// TODO: 17/01/2018 refactor current util
class RemoteTestUtil {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private Connection connectionTmthree;

    private Connection connectionTmfour;

    RemoteTestUtil() throws SQLException {
        initDataSources();
    }

    private void initDataSources() throws SQLException {
        initDataSourceTmthree();
        initDataSourceTmfour();
    }

    private void initDataSourceTmthree() throws SQLException {
        BasicDataSource dataSourceTmthree = new BasicDataSource();
        dataSourceTmthree.setUrl(TMTHREE_URL);
        dataSourceTmthree.setUsername(TMTHREE_USER);
        dataSourceTmthree.setPassword(TMTHREE_PASSWORD);
        dataSourceTmthree.setDriverClassName(TMTHREE_DRIVER);
        connectionTmthree = dataSourceTmthree.getConnection();
    }

    private void initDataSourceTmfour() throws SQLException {
        BasicDataSource dataSourceTmthree = new BasicDataSource();
        dataSourceTmthree.setUrl(TMFOUR_URL);
        dataSourceTmthree.setDriverClassName(TMFOUR_DRIVER);
        connectionTmfour = dataSourceTmthree.getConnection();
    }

    ResultSet getTmThreeQueryResult(String sql){
        try {
            return connectionTmthree.createStatement().executeQuery(sql);
        } catch (SQLException e) {
           logger.error("Unexpected: ", e);
        }
        return null;
    }

    ResultSet getTmFourQueryResult(String sql){
        try {
            return connectionTmfour.createStatement().executeQuery(sql);
        } catch (SQLException e) {
           logger.error("Unexpected: ", e);
        }
        return null;
    }
}
