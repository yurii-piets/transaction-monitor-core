package integration_test.com.tmc;

import com.tmc.TMConfig;
import com.tmc.connection.annotation.DatabaseProperty;
import com.tmc.transaction.core.def.Transaction;
import com.tmc.transaction.service.TransactionService;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@DatabaseProperty(path = MainTest.PROPERTY_SOURCE_FILE_NAME, qualifiers = {MainTest.TMONE_QUALIFIER, MainTest.TMTWO_QUALIFIER})
public class MainTest {
    final static String PROPERTY_SOURCE_FILE_NAME = "test.properties";
    final static String TMONE_QUALIFIER = "tmone";
    final static String TMTWO_QUALIFIER = "tmtwo";

    private final Path pathInit1 = Paths.get(getClass().getClassLoader().getResource("sql/init_db1.sql").toURI());
    private final Path pathInit2 = Paths.get(getClass().getClassLoader().getResource("sql/init_db2.sql").toURI());

    private final Path pathCommit1 = Paths.get(getClass().getClassLoader().getResource("sql/commit_db1.sql").toURI());
    private final Path pathCommit2 = Paths.get(getClass().getClassLoader().getResource("sql/commit_db2.sql").toURI());

    private final Path pathRollback1 = Paths.get(getClass().getClassLoader().getResource("sql/rollback_db1.sql").toURI());
    private final Path pathRollback2 = Paths.get(getClass().getClassLoader().getResource("sql/rollback_db2.sql").toURI());

    private final TransactionService transactionService = TMConfig.boot();

    private final  MainTestUtil testUtil = new MainTestUtil();

    public MainTest() throws URISyntaxException {}

    @Before
    public void initDB() throws IOException {
        transactionService.newTransaction()
            .and()
                .begin(TMONE_QUALIFIER, TMTWO_QUALIFIER)
            .and()
                .addStatement(TMONE_QUALIFIER, pathInit1)
            .and()
                .addStatement(TMTWO_QUALIFIER, pathInit2)
            .and()
                .commit();
    }

    @Test
    public void runSuccessfulQueries() throws SQLException {
        String insertQueryTMONE = "insert into zamowienia values (16, 15, 'insert successful');";
        String insertQueryTMTWO = "insert into oceny values (16, 8, 'insert successful', 4.5);";

        String updateQueryTMONE = "update klienci set nazwa='update successful' where miejscowosc='Warszawa';";
        String updateQueryTMTWO = "update oceny set ocena=6.0 where przedmiot like 'Fizyka%';";

        String deleteQueryTMONE = "delete from klienci";
        String deleteQueryTMTWO = "delete from studenci * where wydzial='imir';";

        Transaction transaction = transactionService.newTransaction();
        transaction.begin(TMONE_QUALIFIER, TMTWO_QUALIFIER);

        transaction.addStatement(TMONE_QUALIFIER, insertQueryTMONE);
        transaction.addStatement(TMTWO_QUALIFIER, insertQueryTMTWO);

        transaction.addStatement(TMONE_QUALIFIER, updateQueryTMONE);
        transaction.addStatement(TMTWO_QUALIFIER, updateQueryTMTWO);

        transaction.addStatement(TMONE_QUALIFIER, deleteQueryTMONE);
        transaction.addStatement(TMTWO_QUALIFIER, deleteQueryTMTWO);

        transaction.commit();

        ResultSet resultSet = testUtil.getTmnOneQueryResult("SELECT * FROM zamowienia WHERE idzamowienia=16 AND idklienta=15 AND opis='insert successful';");
        assertTrue(resultSet.next());

        ResultSet resultSet1 = testUtil.getTmTwoQueryResult("SELECT * FROM oceny WHERE idoceny=16 AND idstudenta=8 AND przedmiot='insert successful' AND ocena='4.5';");
        assertTrue(resultSet1.next());

        ResultSet resultSet2 = testUtil.getTmnOneQueryResult("SELECT * FROM klienci;");
        assertFalse(resultSet2.next());

        ResultSet resultSet3 = testUtil.getTmTwoQueryResult("SELECT * FROM oceny WHERE przedmiot like 'Fizyka%';");

        while (resultSet3.next()){
            assertEquals(6.0, resultSet3.getDouble("ocena"), 0.1);
        }

        ResultSet resultSet4 = testUtil.getTmTwoQueryResult("SELECT * FROM studenci WHERE wydzial='imir';");
        assertFalse(resultSet4.next());
    }

    @Test
    public void runFromFiles() throws IOException {
        transactionService.newTransaction()
                .begin(TMONE_QUALIFIER, TMTWO_QUALIFIER)
            .and()
                .addStatement(TMONE_QUALIFIER, pathCommit1)
            .and()
                .addStatement(TMTWO_QUALIFIER, pathCommit2.toFile())
            .and()
                .commit();

        assertSuccessfulQueriesOnTmOne();
        assertSuccessfulQueriesInTmTwo();
    }

    @Test
    public void runSuccessfulQueriesFromFiles() throws IOException {
        transactionService.newTransaction()
            .and()
                .begin(TMONE_QUALIFIER, TMTWO_QUALIFIER)
            .and()
                .addStatement(TMONE_QUALIFIER, pathCommit1)
            .and()
                .addStatement(TMTWO_QUALIFIER, pathCommit2)
            .and()
                .commit();

        assertSuccessfulQueriesOnTmOne();
        assertSuccessfulQueriesInTmTwo();
    }

    @Test
    public void runFirstFailedSecondSuccessfulQueriesFromFiles() throws IOException, SQLException {
        transactionService.newTransaction()
            .and()
                .begin(TMONE_QUALIFIER, TMTWO_QUALIFIER)
            .and()
                .addStatement(TMONE_QUALIFIER, pathRollback1)
            .and()
                .addStatement(TMTWO_QUALIFIER, pathCommit2)
            .and()
                .commit();

        assertFailedQueriesOnTmOne();
        assertFailedQueriesOnTmTwo();
    }

    @Test
    public void runFirstSuccessSecondFailedQueriesFromFiles() throws IOException, SQLException {
        transactionService.newTransaction()
            .and()
                .begin(TMONE_QUALIFIER, TMTWO_QUALIFIER)
            .and()
                .addStatement(TMONE_QUALIFIER, pathCommit1)
            .and()
                .addStatement(TMTWO_QUALIFIER, pathRollback2)
            .and()
                .commit();

        assertFailedQueriesOnTmOne();
        assertFailedQueriesOnTmTwo();
    }

    @Test
    public void runFailedQueriesFromFile() throws IOException, SQLException {
        transactionService.newTransaction()
            .and()
                .begin(TMONE_QUALIFIER, TMTWO_QUALIFIER)
            .and()
                .addStatement(TMONE_QUALIFIER, pathRollback1)
            .and()
                .addStatement(TMTWO_QUALIFIER, pathRollback2)
            .and()
                .commit();

        assertFailedQueriesOnTmOne();
        assertFailedQueriesOnTmTwo();
    }

    private void assertSuccessfulQueriesOnTmOne(){
        // TODO: 03/12/2017 write selects for asserts
    }

    private void assertSuccessfulQueriesInTmTwo(){
        // TODO: 03/12/2017 write selects for asserts
    }

    private void assertFailedQueriesOnTmOne() throws SQLException {
        ResultSet resultSet = testUtil.getTmnOneQueryResult("SELECT * FROM klienci WHERE nazwa='Rollback Failed'");
        assertFalse(resultSet.next());

        ResultSet resultSet1 = testUtil.getTmnOneQueryResult("SELECT * FROM klienci " +
                " WHERE idklienta=20" +
                " OR nazwa='Successful Rollback Commit'" +
                " OR miejscowosc='PSQL'" +
                " OR telefon='404 404 404';");
        assertFalse(resultSet1.next());

        ResultSet resultSet2 = testUtil.getTmnOneQueryResult("SELECT * FROM klienci " +
                " WHERE idklienta=21" +
                " OR nazwa='Another Successful Commit'" +
                " OR miejscowosc='PSQL'" +
                " OR telefon='403 403 403';");
        assertFalse(resultSet2.next());

        ResultSet resultSet3 = testUtil.getTmnOneQueryResult("SELECT * FROM klienci " +
                " WHERE idklienta=22" +
                " OR nazwa='Yet Another Working Insert'" +
                " OR miejscowosc='PSQL'" +
                " OR telefon='402 402 402';");
        assertFalse(resultSet3.next());

        ResultSet resultSet4 = testUtil.getTmnOneQueryResult("SELECT * FROM zamowienia " +
                " WHERE idzamowienia=16" +
                " OR idklienta=20" +
                " OR opis='Stop inserting!';");
        assertFalse(resultSet4.next());

        ResultSet resultSet5 = testUtil.getTmnOneQueryResult("SELECT * FROM zamowienia " +
                " WHERE idzamowienia=17" +
                " OR idklienta=21" +
                " OR opis='9/11 was an inside job';");
        assertFalse(resultSet5.next());

        ResultSet resultSet6 = testUtil.getTmnOneQueryResult("SELECT * FROM zamowienia " +
                " WHERE idzamowienia=18" +
                " OR idklienta=22" +
                " OR opis='¯\\_(ツ)_/¯';");
        assertFalse(resultSet6.next());
    }

    private void assertFailedQueriesOnTmTwo() throws SQLException {
        ResultSet resultSet8 = testUtil.getTmTwoQueryResult("SELECT * FROM studenci WHERE nazwa='rollback failed'");
        assertFalse(resultSet8.next());

        ResultSet resultSet9 = testUtil.getTmTwoQueryResult("SELECT * FROM studenci" +
                " WHERE idstudenta='20'" +
                " OR nazwa='Should Not Exist'" +
                " OR wydzial='human'" +
                " OR wiek=6.0;");
        assertFalse(resultSet9.next());


        ResultSet resultSet10 = testUtil.getTmTwoQueryResult("SELECT * FROM studenci" +
                " WHERE idstudenta=21" +
                " OR nazwa='Should Not Exist'" +
                " OR wydzial='human'" +
                " OR wiek=5.5;");
        assertFalse(resultSet10.next());

        ResultSet resultSet11 = testUtil.getTmTwoQueryResult("SELECT COUNT(*) AS total FROM studenci WHERE wydzial='eaiiib';");
        resultSet11.next();
        assertEquals(5, resultSet11.getInt("total"));

        ResultSet resultSet12 = testUtil.getTmTwoQueryResult("SELECT * FROM studenci " +
                " WHERE nazwa='EXCEPTION THROWN'" +
                " OR wydzial='EXCEPTION THROWN';");
        assertFalse(resultSet12.next());
    }

}
