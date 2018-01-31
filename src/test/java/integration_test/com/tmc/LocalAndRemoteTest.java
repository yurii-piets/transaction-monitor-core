package integration_test.com.tmc;

import com.tmc.TMConfig;
import com.tmc.transaction.service.TransactionService;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;

import static integration_test.com.tmc.DatabaseProperties.Qualifiers.TMFOUR_QUALIFIER;
import static integration_test.com.tmc.DatabaseProperties.Qualifiers.TMONE_QUALIFIER;
import static integration_test.com.tmc.DatabaseProperties.Qualifiers.TMTHREE_QUALIFIER;
import static integration_test.com.tmc.DatabaseProperties.Qualifiers.TMTWO_QUALIFIER;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LocalAndRemoteTest {

    private final Path pathInit1 = Paths.get(getClass().getClassLoader().getResource("sql/psql/init_db1.sql").toURI());
    private final Path pathInit2 = Paths.get(getClass().getClassLoader().getResource("sql/psql/init_db2.sql").toURI());
    private final Path pathInit3 = Paths.get(getClass().getClassLoader().getResource("sql/mysql/init_db.sql").toURI());
    private final Path pathInit4 = Paths.get(getClass().getClassLoader().getResource("sql/ms-azure/init_db.sql").toURI());

    private static final TransactionService transactionService = TMConfig.boot();

    private final TestUtil testUtil = TestUtil.getInstance();

    public LocalAndRemoteTest() throws URISyntaxException {}

    @Before
    public void before() throws IOException {
        initDatabases();
    }

    @Test
    public void runThreeSuccessfulOneFailed() throws SQLException {
        transactionService.newTransaction()
                .begin(TMONE_QUALIFIER, TMTWO_QUALIFIER, TMTHREE_QUALIFIER, TMFOUR_QUALIFIER)
            .and()
                .addStatement(TMONE_QUALIFIER, "update zamowienia set opis='update successful';")
            .and()
                .addStatement(TMTWO_QUALIFIER, "delete from studenci * where wydzial='imir';")
            .and()
                .addStatement(TMTHREE_QUALIFIER, "update studenci set nazwa='update successful' where wydzial='ieit';")
            .and()
                .addStatement(TMFOUR_QUALIFIER, "update klienci set idklienta='EXCEPTION THROWN';")
            .and()
                .commit();

        ResultSet resultSet1 = testUtil.resultSetForSqlQuery(TMONE_QUALIFIER,
                "select * from zamowienia where opis='update successful';");
        assertFalse(resultSet1.next());

        ResultSet resultSet2 = testUtil.resultSetForSqlQuery(TMTWO_QUALIFIER,
                "select * from studenci where wydzial='imir';");
        assertTrue(resultSet2.next());

        ResultSet resultSet3 = testUtil.resultSetForSqlQuery(TMTHREE_QUALIFIER,
                "select * from studenci where nazwa='update successful' and wydzial='ieit';");
        assertFalse(resultSet3.next());
    }

    @Test
    public void runFourSuccessful() throws SQLException {
        transactionService.newTransaction()
                .begin(TMONE_QUALIFIER, TMTWO_QUALIFIER, TMTHREE_QUALIFIER, TMFOUR_QUALIFIER)
            .and()
                .addStatement(TMONE_QUALIFIER, "update zamowienia set opis='update successful';")
            .and()
                .addStatement(TMTWO_QUALIFIER, "delete from studenci * where wydzial='imir';")
            .and()
                .addStatement(TMTHREE_QUALIFIER, "update studenci set nazwa='update successful' where wydzial='ieit';")
            .and()
                .addStatement(TMFOUR_QUALIFIER, "insert into zamowienia values (16, 15, 'insert successful');")
            .and()
                .commit();

        ResultSet resultSet1 = testUtil.resultSetForSqlQuery(TMONE_QUALIFIER,
                "select * from zamowienia where opis='update successful';");
        assertTrue(resultSet1.next());

        ResultSet resultSet2 = testUtil.resultSetForSqlQuery(TMTWO_QUALIFIER,
                "select * from studenci where wydzial='imir';");
        assertFalse(resultSet2.next());

        ResultSet resultSet3 = testUtil.resultSetForSqlQuery(TMTHREE_QUALIFIER,
                "select * from studenci where nazwa='update successful' and wydzial='ieit';");
        assertTrue(resultSet3.next());

        ResultSet resultSet4 = testUtil.resultSetForSqlQuery(TMFOUR_QUALIFIER,
                "select * from zamowienia where idzamowienia=16 and idklienta=15 and opis='insert successful'");
        assertTrue(resultSet4.next());
    }

    private void initDatabases() throws IOException {
        transactionService.newTransaction()
                .begin(TMONE_QUALIFIER, TMTWO_QUALIFIER, TMTHREE_QUALIFIER, TMFOUR_QUALIFIER)
            .and()
                .addStatement(TMONE_QUALIFIER, pathInit1)
            .and()
                .addStatement(TMTWO_QUALIFIER, pathInit2)
            .and()
                .addStatement(TMTHREE_QUALIFIER, pathInit3)
            .and()
                .addStatement(TMFOUR_QUALIFIER, pathInit4)
            .and()
                .commit();
    }
}
