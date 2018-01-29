package integration_test.com.tmc;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

class TMTestUtil {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private BasicDataSource dataSourceTmone;

    private BasicDataSource dataSourceTmtwo;

    TMTestUtil() {
        initDataSources();
    }

    private void initDataSources() {
        initDataSourceTmone();
        initDataSourceTmtwo();
    }

    private void initDataSourceTmone() {
        dataSourceTmone = new BasicDataSource();
        dataSourceTmone.setUrl("jdbc:postgresql://localhost:5432/tmone");
        dataSourceTmone.setUsername("postgres");
        dataSourceTmone.setPassword("");
        dataSourceTmone.setDriverClassName("org.postgresql.Driver");
    }

    private void initDataSourceTmtwo() {
        dataSourceTmtwo = new BasicDataSource();
        dataSourceTmtwo.setUrl("jdbc:postgresql://localhost:5432/tmtwo");
        dataSourceTmtwo.setUsername("postgres");
        dataSourceTmtwo.setPassword("");
        dataSourceTmtwo.setDriverClassName("org.postgresql.Driver");
    }

    ResultSet getTmOneQueryResult(String sql){
        try {
            return dataSourceTmone.getConnection().createStatement().executeQuery(sql);
        } catch (SQLException e) {
           logger.error("Unexpected: ", e);
        }
        return null;
    }

    ResultSet getTmTwoQueryResult(String sql){
        try {
            return dataSourceTmtwo.getConnection().createStatement().executeQuery(sql);
        } catch (SQLException e) {
           logger.error("Unexpected: ", e);
        }
        return null;
    }
}
