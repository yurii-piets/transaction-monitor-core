package integration_test.com.tmc;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MainTestUtil {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private BasicDataSource dataSourceTmone;

    private BasicDataSource dataSourceTmtwo;

    public MainTestUtil() {
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

    public ResultSet getTmnOneQueryResult(String sql){
        try {
            return dataSourceTmone.getConnection().createStatement().executeQuery(sql);
        } catch (SQLException e) {
           logger.error("Unexpected: ", e);
        }
        return null;
    }

    public ResultSet getTmTwoQueryResult(String sql){
        try {
            return dataSourceTmtwo.getConnection().createStatement().executeQuery(sql);
        } catch (SQLException e) {
           logger.error("Unexpected: ", e);
        }
        return null;
    }
}
