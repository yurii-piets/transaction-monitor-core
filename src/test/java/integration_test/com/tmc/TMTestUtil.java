package integration_test.com.tmc;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

// TODO: 17/01/2018 refactor current util
class TMTestUtil {

    private static final String TMONE_URL = "jdbc:postgresql://localhost:5432/tmone";
    private static final String TMONE_USER = System.getenv().get("PG_USER");
    private static final String TMONE_PASSWORD = System.getenv().get("PG_PASSWORD");
    private static final String TMONE_DRIVER = "org.postgresql.Driver";

    private static final String TMTWO_URL = "jdbc:postgresql://localhost:5432/tmtwo";
    private static final String TMTWO_USER = System.getenv().get("PG_USER");
    private static final String TMTWO_PASSWORD = System.getenv().get("PG_PASSWORD");
    private static final String TMTWO_DRIVER = "org.postgresql.Driver";

    private static final String TMTHREE_URL = "jdbc:mysql://sql11.freemysqlhosting.net:3306/sql11214352";
    private static final String TMTHREE_USER = "sql11214352";
    private static final String TMTHREE_PASSWORD = "4DgP52934w";
    private static final String TMTHREE_DRIVER = "com.mysql.jdbc.Driver";

    private static final String TMFOUR_URL = "jdbc:sqlserver://omegadb.database.windows.net:1433;database=OmegaDB;user=omega;password=@dmin1234;encrypt=true;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";
    private static final String TMFOUR_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    private final Logger logger = LogManager.getLogger(this.getClass());

    private Connection connectionTmone;

    private Connection connectionTmtwo;

    private Connection connectionTmthree;

    private Connection connectionTmfour;

    TMTestUtil() throws SQLException {
        initDataSources();
    }

    private void initDataSources() throws SQLException {
        initDataSourceTmone();
        initDataSourceTmtwo();
        initDataSourceTmthree();
        initDataSourceTmfour();
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
