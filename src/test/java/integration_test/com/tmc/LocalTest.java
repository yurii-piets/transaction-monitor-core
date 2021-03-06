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

import static integration_test.com.tmc.DatabaseProperties.PROPERTY_SOURCE_FILE_NAME;
import static integration_test.com.tmc.DatabaseProperties.Qualifiers.TMONE_QUALIFIER;
import static integration_test.com.tmc.DatabaseProperties.Qualifiers.TMTWO_QUALIFIER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@DatabaseProperty(path = PROPERTY_SOURCE_FILE_NAME, qualifiers = {TMONE_QUALIFIER, TMTWO_QUALIFIER})
public class LocalTest {

    private final Path pathInit1 = Paths.get(getClass().getClassLoader().getResource("sql/psql/init_db1.sql").toURI());
    private final Path pathInit2 = Paths.get(getClass().getClassLoader().getResource("sql/psql/init_db2.sql").toURI());

    private final Path pathCommit1 = Paths.get(getClass().getClassLoader().getResource("sql/psql/commit_db1.sql").toURI());
    private final Path pathCommit2 = Paths.get(getClass().getClassLoader().getResource("sql/psql/commit_db2.sql").toURI());

    private final Path pathRollback1 = Paths.get(getClass().getClassLoader().getResource("sql/psql/rollback_db1.sql").toURI());
    private final Path pathRollback2 = Paths.get(getClass().getClassLoader().getResource("sql/psql/rollback_db2.sql").toURI());

    private final TransactionService transactionService = TMConfig.boot();

    private final TestUtil testUtil = TestUtil.getInstance();

    public LocalTest() throws URISyntaxException {}

    @Before
    public void before() throws IOException {
        initDatabases();
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

        ResultSet resultSet = testUtil.resultSetForSqlQuery(TMONE_QUALIFIER,
                "select * from zamowienia where idzamowienia=16 and idklienta=15 and opis='insert successful';");
        assertTrue(resultSet.next());

        ResultSet resultSet1 = testUtil.resultSetForSqlQuery(TMTWO_QUALIFIER,
                "select * from oceny where idoceny=16 and idstudenta=8 and przedmiot='insert successful' and ocena='4.5';");
        assertTrue(resultSet1.next());

        ResultSet resultSet2 = testUtil.resultSetForSqlQuery(TMONE_QUALIFIER, "select * from klienci;");
        assertFalse(resultSet2.next());

        ResultSet resultSet3 = testUtil.resultSetForSqlQuery(TMTWO_QUALIFIER,
                "select * from oceny where przedmiot like 'Fizyka%';");

        if (resultSet3.next()) {
            do{
                assertEquals(6.0, resultSet3.getDouble("ocena"), 0.1);
            } while (resultSet3.next());
        } else {
            fail("Result set is empty.");
        }

        ResultSet resultSet4 = testUtil.resultSetForSqlQuery(TMTWO_QUALIFIER,
                "select * from studenci where wydzial='imir';");
        assertFalse(resultSet4.next());
    }

    @Test
    public void runSuccessfulQueriesFromFiles() throws IOException, SQLException {
        transactionService.newTransaction()
            .and()
                .begin(TMONE_QUALIFIER, TMTWO_QUALIFIER)
            .and()
                .addStatement(TMONE_QUALIFIER, pathCommit1)
            .and()
                .addStatement(TMTWO_QUALIFIER, pathCommit2.toFile())
            .and()
                .commit();

        assertSuccessfulQueriesOnTmOne();
        assertSuccessfulQueriesOnTmTwo();
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

    @Test
    public void runEmptyTransactions() {
        Transaction transaction1 = transactionService.newTransaction();
        Transaction transaction2 = transactionService.newTransaction();

        transaction1.begin(TMONE_QUALIFIER, TMTWO_QUALIFIER);
        transaction2.begin(TMONE_QUALIFIER, TMTWO_QUALIFIER);

        transaction2.commit();
        transaction1.commit();
    }

    @Test
    public void runEmptyQuery() {
        transactionService.newTransaction()
                .begin(TMONE_QUALIFIER, TMTWO_QUALIFIER)
            .and()
                .addStatement(TMONE_QUALIFIER, "")
            .and()
                .commit();
    }

    @Test
    public void runFirstFailedAndSecondSuccessfulMixedTransactions() throws SQLException {
        Transaction transaction1 = transactionService.newTransaction();
        Transaction transaction2 = transactionService.newTransaction();

        transaction1.begin(TMONE_QUALIFIER, TMTWO_QUALIFIER);
        transaction2.begin(TMONE_QUALIFIER, TMTWO_QUALIFIER);

        transaction1
                .addStatement(TMONE_QUALIFIER,
                        "insert into klienci values(77, 'Test Failed', 'Test Failed', '000 000 000');")
            .and()
                .addStatement(TMTWO_QUALIFIER, "delete from studenci where idstudenta = 7");

        transaction2
                .addStatement(TMTWO_QUALIFIER,
                        "delete from oceny where przedmiot='Podstawy Elektroniki Cyfrowej' and idstudenta=8;")
            .and()
                .addStatement(TMONE_QUALIFIER, "insert into zamowienia values(16, 1, 'Test Successful');");

        transaction1
                .addStatement(TMONE_QUALIFIER, 
                        "insert into klienci values('Exception thrown', 'Test Failed', 'Test Failed', '000 000 000');");

        transaction2.commit();

        ResultSet resultSet1 = testUtil.resultSetForSqlQuery(TMONE_QUALIFIER, 
                "select * from klienci where idklienta=77");
        assertFalse(resultSet1.next());

        ResultSet resultSet2 = testUtil.resultSetForSqlQuery(TMTWO_QUALIFIER, 
                "select * from studenci where idstudenta=7");
        assertTrue(resultSet2.next());

        ResultSet resultSet3 = testUtil.resultSetForSqlQuery(TMTWO_QUALIFIER,
                "select * from oceny where przedmiot='Podstawy Elektroniki Cyfrowej' and idstudenta=8;");
        assertFalse(resultSet3.next());

        ResultSet resultSet4 = testUtil.resultSetForSqlQuery(TMONE_QUALIFIER, 
                "select from zamowienia where idzamowienia=16;");
        assertTrue(resultSet4.next());

        transaction1.commit();

        ResultSet resultSet5 = testUtil.resultSetForSqlQuery(TMONE_QUALIFIER, 
                "select * from klienci where idklienta=77");
        assertFalse(resultSet5.next());

        ResultSet resultSet6 = testUtil.resultSetForSqlQuery(TMTWO_QUALIFIER, 
                "select * from studenci where idstudenta=7");
        assertTrue(resultSet6.next());
    }

    @Test
    public void runFirstSuccessfulAndSecondSuccessfulMixed() throws SQLException {
        Transaction transaction1 = transactionService.newTransaction();
        Transaction transaction2 = transactionService.newTransaction();

        transaction1.begin(TMONE_QUALIFIER, TMTWO_QUALIFIER);
        transaction2.begin(TMONE_QUALIFIER, TMTWO_QUALIFIER);

        transaction1.addStatement(TMONE_QUALIFIER, "update zamowienia set opis='update successful';");
        transaction1.addStatement(TMTWO_QUALIFIER, "update studenci set nazwa='update successful' where wydzial='ieit';");

        transaction2.addStatement(TMONE_QUALIFIER, "insert into zamowienia values (16, 15, 'insert successful');");
        transaction2.addStatement(TMTWO_QUALIFIER, "update oceny set ocena=1.0 where przedmiot like 'Fizyka%';");

        transaction1.commit();

        ResultSet resultSet1 = testUtil.resultSetForSqlQuery(TMONE_QUALIFIER, "select count(*) as total from zamowienia where opis='update successful'");
        resultSet1.next();
        assertEquals(14, resultSet1.getInt("total"));

        ResultSet resultSet2 = testUtil.resultSetForSqlQuery(TMTWO_QUALIFIER, "select nazwa from studenci where wydzial='ieit';");
        if (resultSet2.next()) {
            do {
                assertEquals("update successful", resultSet2.getString("nazwa"));
            } while (resultSet2.next());
        } else {
            fail("Result set is empty");
        }

        ResultSet resultSet3 = testUtil.resultSetForSqlQuery(TMONE_QUALIFIER, "select * from zamowienia where idzamowienia=16;");
        assertFalse(resultSet3.next());

        ResultSet resultSet4 = testUtil.resultSetForSqlQuery(TMTWO_QUALIFIER, "select ocena from oceny where przedmiot like 'Fizyka%'");
        if (resultSet4.next()) {
            do {
                assertNotEquals(1.0, resultSet4.getDouble("ocena"), 0.1);
            } while (resultSet4.next());
        } else {
            fail("Result set is empty");
        }

        transaction2.commit();

        ResultSet resultSet5 = testUtil.resultSetForSqlQuery(TMONE_QUALIFIER, "select count(*) as total from zamowienia where opis='update successful'");
        resultSet5.next();
        assertEquals(14, resultSet5.getInt("total"));

        ResultSet resultSet6 = testUtil.resultSetForSqlQuery(TMTWO_QUALIFIER, "select nazwa from studenci where wydzial='ieit';");
        if (resultSet6.next()) {
            do {
                assertEquals("update successful", resultSet6.getString("nazwa"));
            } while (resultSet6.next());
        } else {
            fail("Result set is empty");
        }

        ResultSet resultSet7 = testUtil.resultSetForSqlQuery(TMONE_QUALIFIER, "select * from zamowienia where idzamowienia=16;");
        assertTrue(resultSet7.next());

        ResultSet resultSet8 = testUtil.resultSetForSqlQuery(TMTWO_QUALIFIER, "select ocena from oceny where przedmiot like 'Fizyka%'");
        if (resultSet8.next()) {
            do {
                assertEquals(1.0, resultSet8.getInt("ocena"), 0.1);
            } while (resultSet8.next());
        } else {
            fail("Result set is empty");
        }
    }

    @Test
    public void runSuccessfulTransactionsInDifferentThreads() throws SQLException, InterruptedException {
        Thread thread1 = getBothThreadsSuccessfulTestThreadOne();
        Thread thread2 = getBothThreadsSuccessfulTestThreadTwo();

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        ResultSet resultSet1 = testUtil.resultSetForSqlQuery(TMONE_QUALIFIER, 
                "select * from zamowienia where opis='update successful'");
        ResultSet resultSet2 = testUtil.resultSetForSqlQuery(TMONE_QUALIFIER, 
                "select * from zamowienia where opis='trans2 update successful'");
        assertTrue(resultSet1.next() || resultSet2.next() );

        ResultSet resultSet3 = testUtil.resultSetForSqlQuery(TMTWO_QUALIFIER, 
                "select * from oceny where idoceny=16");
        assertTrue(resultSet3.next());

        ResultSet resultSet4 = testUtil.resultSetForSqlQuery(TMONE_QUALIFIER, 
                "select * from zamowienia where idzamowienia=17;");
        assertTrue(resultSet4.next());

        ResultSet resultSet5 = testUtil.resultSetForSqlQuery(TMTWO_QUALIFIER, 
                "select * from studenci where nazwa='update successful'");
        assertFalse(resultSet5.next());

        ResultSet resultSet6 = testUtil.resultSetForSqlQuery(TMTWO_QUALIFIER, 
                "select * from studenci where wydzial='ieit'");
        assertFalse(resultSet6.next());

        ResultSet resultSet7 = testUtil.resultSetForSqlQuery(TMONE_QUALIFIER, 
                "select count(*) as total from klienci where nazwa='Lech Balcerowicz'");
        resultSet7.next();
        assertEquals(3, resultSet7.getInt("total"));
    }

    @Test
    public void runSuccessfulAndRollbackTransactionsInDifferentThreads() throws SQLException, InterruptedException {
        Thread thread1 = getMixedThreadsTestFailingThread();
        Thread thread2 = getMixedThreadsTestSuccessfulThread();

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        ResultSet resultSet1 = testUtil.resultSetForSqlQuery(TMONE_QUALIFIER, 
                "select * from zamowienia where opis='update successful'");
        assertTrue(resultSet1.next());

        ResultSet resultSet2 = testUtil.resultSetForSqlQuery(TMTWO_QUALIFIER, 
                "select * from oceny where idoceny=16");
        assertTrue(resultSet2.next());

        ResultSet resultSet3 = testUtil.resultSetForSqlQuery(TMONE_QUALIFIER, 
                "select COUNT(*) AS total from klienci where nazwa='Lech Balcerowicz'");
        resultSet3.next();
        assertEquals(3, resultSet3.getInt("total"));

        ResultSet resultSet4 = testUtil.resultSetForSqlQuery(TMTWO_QUALIFIER, 
                "select * from studenci where wydzial='imir'");
        assertFalse(resultSet4.next());

        ResultSet resultSet5 = testUtil.resultSetForSqlQuery(TMONE_QUALIFIER, 
                "select * from zamowienia where idzamowienia=17;");
        assertTrue(resultSet5.next());


    }

    private void initDatabases() throws IOException {
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

    private void assertSuccessfulQueriesOnTmOne() throws SQLException {
        ResultSet resultSet = testUtil.resultSetForSqlQuery(TMONE_QUALIFIER, 
                "select * from klienci " +
                        "where idklienta=16 " +
                        "and nazwa='Pariusz Dalka' " +
                        "and miejscowosc='Krakow' " +
                        "and telefon='666 666 666';");
        assertTrue(resultSet.next());

        ResultSet resultSet1 = testUtil.resultSetForSqlQuery(TMONE_QUALIFIER, 
                "select * from klienci " +
                        "where idklienta=23 " +
                        "and nazwa='Rollback Successful' " +
                        "and miejscowosc='PSQL' " +
                        "and telefon='010 001 100';");
        assertTrue(resultSet1.next());

        ResultSet resultSet2 = testUtil.resultSetForSqlQuery(TMONE_QUALIFIER, 
                "select count(*) as total from klienci where nazwa='Lech Balcerowicz';");
        resultSet2.next();
        assertEquals(3, resultSet2.getInt("total"));

        ResultSet resultSet3 = testUtil.resultSetForSqlQuery(TMONE_QUALIFIER, 
                "select * from klienci where miejscowosc='Warszawa';");
        if (resultSet3.next()) {
            do {
                assertEquals("Lech Balcerowicz", resultSet3.getString("nazwa"));
            } while (resultSet3.next());
        } else {
            fail("Result set is empty");
        }

        ResultSet resultSet4 = testUtil.resultSetForSqlQuery(TMONE_QUALIFIER, 
                "select * from zamowienia where idzamowienia!=16;");
        if (resultSet4.next()) {
            do {
                assertEquals("update successful", resultSet4.getString("opis"));
            } while (resultSet4.next());
        } else {
            fail("Result set is empty");
        }

        ResultSet resultSet5 = testUtil.resultSetForSqlQuery(TMONE_QUALIFIER, 
                "select * from zamowienia where idzamowienia=16 and idklienta=15 and opis='insert successful';");
        assertTrue(resultSet5.next());
    }

    private void assertSuccessfulQueriesOnTmTwo() throws SQLException {
        ResultSet resultSet = testUtil.resultSetForSqlQuery(TMTWO_QUALIFIER, 
                "select * from oceny " +
                        "where idoceny=16 " +
                        "and idstudenta=8 " +
                        "and przedmiot='Programownie Obiektowe' " +
                        "and ocena=4.5;");
        assertTrue(resultSet.next());

        ResultSet resultSet1 = testUtil.resultSetForSqlQuery(TMTWO_QUALIFIER, 
                "select * from oceny where przedmiot like 'Fizyka%';");
        if (resultSet1.next()) {
            do {
                assertEquals(2.0, resultSet1.getDouble("ocena"), 0.1);
            } while (resultSet1.next());
        } else {
            fail("Result set is empty");
        }

        ResultSet resultSet2 = testUtil.resultSetForSqlQuery(TMTWO_QUALIFIER, 
                "select * from studenci where wydzial='imir';");
        assertFalse(resultSet2.next());

        ResultSet resultSet3 = testUtil.resultSetForSqlQuery(TMTWO_QUALIFIER, 
                "select * from studenci where wydzial='ieit';");
        if (resultSet3.next()) {
            do {
                assertEquals("update successful", resultSet3.getString("nazwa"));
            } while (resultSet3.next());
        } else {
            fail("Result set is empty");
        }
    }

    private void assertFailedQueriesOnTmOne() throws SQLException {
        ResultSet resultSet = testUtil.resultSetForSqlQuery(TMONE_QUALIFIER, 
                "select * from klienci where nazwa='Rollback Failed'");
        assertFalse(resultSet.next());

        ResultSet resultSet1 = testUtil.resultSetForSqlQuery(TMONE_QUALIFIER, "select * from klienci " +
                " where idklienta=20" +
                " or nazwa='Successful Rollback Commit'" +
                " or miejscowosc='PSQL'" +
                " or telefon='404 404 404';");
        assertFalse(resultSet1.next());

        ResultSet resultSet2 = testUtil.resultSetForSqlQuery(TMONE_QUALIFIER, "select * from klienci " +
                " where idklienta=21" +
                " or nazwa='Another Successful Commit'" +
                " or miejscowosc='PSQL'" +
                " or telefon='403 403 403';");
        assertFalse(resultSet2.next());

        ResultSet resultSet3 = testUtil.resultSetForSqlQuery(TMONE_QUALIFIER, "select * from klienci " +
                " where idklienta=22" +
                " or nazwa='Yet Another Working Insert'" +
                " or miejscowosc='PSQL'" +
                " or telefon='402 402 402';");
        assertFalse(resultSet3.next());

        ResultSet resultSet4 = testUtil.resultSetForSqlQuery(TMONE_QUALIFIER, "select * from zamowienia " +
                " where idzamowienia=16" +
                " or idklienta=20" +
                " or opis='Stop inserting!';");
        assertFalse(resultSet4.next());

        ResultSet resultSet5 = testUtil.resultSetForSqlQuery(TMONE_QUALIFIER, "select * from zamowienia " +
                " where idzamowienia=17" +
                " or idklienta=21" +
                " or opis='9/11 was an inside job';");
        assertFalse(resultSet5.next());

        ResultSet resultSet6 = testUtil.resultSetForSqlQuery(TMONE_QUALIFIER, "select * from zamowienia " +
                " where idzamowienia=18" +
                " or idklienta=22" +
                " or opis='¯\\_(ツ)_/¯';");
        assertFalse(resultSet6.next());
    }

    private void assertFailedQueriesOnTmTwo() throws SQLException {
        ResultSet resultSet8 = testUtil.resultSetForSqlQuery(TMTWO_QUALIFIER, 
                "select * from studenci where nazwa='rollback failed'");
        assertFalse(resultSet8.next());

        ResultSet resultSet9 = testUtil.resultSetForSqlQuery(TMTWO_QUALIFIER, "select * from studenci " +
                "where idstudenta='20' " +
                "or nazwa='Should Not Exist' " +
                "or wydzial='human' " +
                "or wiek=6.0;");
        assertFalse(resultSet9.next());


        ResultSet resultSet10 = testUtil.resultSetForSqlQuery(TMTWO_QUALIFIER, "select * from studenci" +
                " where idstudenta=21" +
                " or nazwa='Should Not Exist'" +
                " or wydzial='human'" +
                " or wiek=5.5;");
        assertFalse(resultSet10.next());

        ResultSet resultSet11 = testUtil.resultSetForSqlQuery(TMTWO_QUALIFIER, 
                "select COUNT(*) AS total from studenci where wydzial='eaiiib';");
        resultSet11.next();
        assertEquals(5, resultSet11.getInt("total"));

        ResultSet resultSet12 = testUtil.resultSetForSqlQuery(TMTWO_QUALIFIER, "select * from studenci " +
                " where nazwa='EXCEPTION THROWN'" +
                " or wydzial='EXCEPTION THROWN';");
        assertFalse(resultSet12.next());
    }

    private Thread getBothThreadsSuccessfulTestThreadOne() {
        return new Thread(() -> transactionService
                    .newTransaction()
                .and()
                    .begin(TMONE_QUALIFIER, TMTWO_QUALIFIER)
                .and()
                    .addStatement(TMONE_QUALIFIER, "update zamowienia set opis='trans2 update successful';")
                .and()
                    .addStatement(TMTWO_QUALIFIER, "update studenci set nazwa='update successful' where wydzial='ieit';")
                .and()
                    .commit()
        );
    }

    private Thread getBothThreadsSuccessfulTestThreadTwo() {
        return new Thread( () -> transactionService
                    .newTransaction()
                .and()
                    .begin(TMONE_QUALIFIER, TMTWO_QUALIFIER)
                .and()
                    .addStatement(TMONE_QUALIFIER, "update zamowienia set opis='update successful';")
                .and()
                    .addStatement(TMTWO_QUALIFIER, "insert into oceny values (16, 8, 'Programownie Obiektowe', 4.5);")
                .and()
                    .addStatement(TMONE_QUALIFIER, "update klienci set nazwa='Lech Balcerowicz' where miejscowosc='Warszawa';")
                .and()
                    .addStatement(TMTWO_QUALIFIER,"delete from studenci * where wydzial='ieit';")
                .and()
                    .addStatement(TMONE_QUALIFIER,"insert into zamowienia values(17, 14, 'another succ');")
                .and()
                    .commit()
        );
    }

    private Thread getMixedThreadsTestFailingThread() {
        return new Thread( () -> transactionService
                    .newTransaction()
                .and()
                    .begin(TMONE_QUALIFIER, TMTWO_QUALIFIER)
                .and()
                    .addStatement(TMONE_QUALIFIER, "update zamowienia set opis='rollback';")
                .and()
                    .addStatement(TMTWO_QUALIFIER, "insert into studenci values " +
                        "('EXCEPTION THROWN', 'EXCEPTION THROWN', 'EXCEPTION THROWN', 'EXCEPTION THROWN');")
                .and()
                    .commit()
        );
    }

    private Thread getMixedThreadsTestSuccessfulThread() {
        return new Thread( () -> transactionService
                    .newTransaction()
                .and()
                    .begin(TMONE_QUALIFIER, TMTWO_QUALIFIER)
                .and()
                    .addStatement(TMTWO_QUALIFIER,"delete from studenci * where wydzial='imir';")
                .and()
                    .addStatement(TMTWO_QUALIFIER, "insert into oceny values (16, 8, 'Programownie Obiektowe', 4.5);")
                .and()
                    .addStatement(TMONE_QUALIFIER, "update klienci set nazwa='Lech Balcerowicz' where miejscowosc='Warszawa';")
                .and()
                    .addStatement(TMONE_QUALIFIER, "update zamowienia set opis='update successful';")
                .and()
                    .addStatement(TMONE_QUALIFIER,"insert into zamowienia values(17, 14, 'another succ');")
                .and()
                    .commit()
        );
    }
}
