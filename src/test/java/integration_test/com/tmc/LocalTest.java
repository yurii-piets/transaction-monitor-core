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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

@DatabaseProperty(path = LocalTest.PROPERTY_SOURCE_FILE_NAME, qualifiers = {LocalTest.TMONE_QUALIFIER, LocalTest.TMTWO_QUALIFIER})
public class LocalTest {
    final static String PROPERTY_SOURCE_FILE_NAME = "test.properties";
    final static String TMONE_QUALIFIER = "tmone";
    final static String TMTWO_QUALIFIER = "tmtwo";

    private final Path pathInit1 = Paths.get(getClass().getClassLoader().getResource("sql/init_db1.sql").toURI());
    private final Path pathInit2 = Paths.get(getClass().getClassLoader().getResource("sql/init_db2.sql").toURI());

    private final Path pathCommit1 = Paths.get(getClass().getClassLoader().getResource("sql/commit_db1.sql").toURI());
    private final Path pathCommit2 = Paths.get(getClass().getClassLoader().getResource("sql/commit_db2.sql").toURI());

    private final Path pathRollback1 = Paths.get(getClass().getClassLoader().getResource("sql/rollback_db1.sql").toURI());
    private final Path pathRollback2 = Paths.get(getClass().getClassLoader().getResource("sql/rollback_db2.sql").toURI());

    private static final TransactionService transactionService = TMConfig.boot();

    private final TMTestUtil testUtil = new TMTestUtil();

    public LocalTest() throws URISyntaxException, SQLException {
    }

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

        ResultSet resultSet = testUtil.getTmOneQueryResult("SELECT * FROM zamowienia WHERE idzamowienia=16 AND idklienta=15 AND opis='insert successful';");
        assertTrue(resultSet.next());

        ResultSet resultSet1 = testUtil.getTmTwoQueryResult("SELECT * FROM oceny WHERE idoceny=16 AND idstudenta=8 AND przedmiot='insert successful' AND ocena='4.5';");
        assertTrue(resultSet1.next());

        ResultSet resultSet2 = testUtil.getTmOneQueryResult("SELECT * FROM klienci;");
        assertFalse(resultSet2.next());

        ResultSet resultSet3 = testUtil.getTmTwoQueryResult("SELECT * FROM oceny WHERE przedmiot like 'Fizyka%';");

        while (resultSet3.next()) {
            assertEquals(6.0, resultSet3.getDouble("ocena"), 0.1);
        }

        ResultSet resultSet4 = testUtil.getTmTwoQueryResult("SELECT * FROM studenci WHERE wydzial='imir';");
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
                .addStatement(TMONE_QUALIFIER, "insert into klienci values('Exception thrown', 'Test Failed', 'Test Failed', '000 000 000');");

        transaction2.commit();

        ResultSet resultSet1 = testUtil.getTmOneQueryResult("select * from klienci where idklienta=77");
        assertFalse(resultSet1.next());

        ResultSet resultSet2 = testUtil.getTmTwoQueryResult("select * from studenci where idstudenta=7");
        assertTrue(resultSet2.next());

        ResultSet resultSet3 = testUtil.getTmTwoQueryResult("select * from oceny where przedmiot='Podstawy Elektroniki Cyfrowej' and idstudenta=8;");
        assertFalse(resultSet3.next());

        ResultSet resultSet4 = testUtil.getTmOneQueryResult("select from zamowienia where idzamowienia=16;");
        assertTrue(resultSet4.next());

        transaction1.commit();

        ResultSet resultSet5 = testUtil.getTmOneQueryResult("select * from klienci where idklienta=77");
        assertFalse(resultSet5.next());

        ResultSet resultSet6 = testUtil.getTmTwoQueryResult("select * from studenci where idstudenta=7");
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

        ResultSet resultSet1 = testUtil.getTmOneQueryResult("select count(*) as total from zamowienia where opis='update successful'");
        resultSet1.next();
        assertEquals(14, resultSet1.getInt("total"));

        ResultSet resultSet2 = testUtil.getTmTwoQueryResult("select nazwa from studenci where wydzial='ieit';");
        while (resultSet2.next()){
            assertEquals("update successful", resultSet2.getString("nazwa"));
        }

        ResultSet resultSet3 = testUtil.getTmOneQueryResult("select * from zamowienia where idzamowienia=16;");
        assertFalse(resultSet3.next());

        ResultSet resultSet4 = testUtil.getTmTwoQueryResult("select ocena from oceny where przedmiot like 'Fizyka%'");
        while(resultSet4.next()){
            assertNotEquals(1.0, resultSet4.getDouble("ocena"), 0.1);
        }

        transaction2.commit();

        ResultSet resultSet5 = testUtil.getTmOneQueryResult("select count(*) as total from zamowienia where opis='update successful'");
        resultSet5.next();
        assertEquals(14, resultSet5.getInt("total"));

        ResultSet resultSet6 = testUtil.getTmTwoQueryResult("select nazwa from studenci where wydzial='ieit';");
        while (resultSet6.next()){
            assertEquals("update successful", resultSet6.getString("nazwa"));
        }

        ResultSet resultSet7 = testUtil.getTmOneQueryResult("select * from zamowienia where idzamowienia=16;");
        assertTrue(resultSet7.next());

        ResultSet resultSet8 = testUtil.getTmTwoQueryResult("select ocena from oceny where przedmiot like 'Fizyka%'");
        while(resultSet8.next()){
            assertEquals(1.0, resultSet8.getInt("ocena"), 0.1);
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

        ResultSet resultSet1 = testUtil.getTmOneQueryResult("select * from zamowienia where opis='update successful'");
        ResultSet resultSet2 = testUtil.getTmOneQueryResult("select * from zamowienia where opis='trans2 update successful'");
        assertTrue(resultSet1.next() || resultSet2.next() );

        ResultSet resultSet3 = testUtil.getTmTwoQueryResult("select * from oceny where idoceny=16");
        assertTrue(resultSet3.next());

        ResultSet resultSet4 = testUtil.getTmOneQueryResult("select * from zamowienia where idzamowienia=17;");
        assertTrue(resultSet4.next());

        ResultSet resultSet5 = testUtil.getTmTwoQueryResult("select * from studenci where nazwa='update successful'");
        assertFalse(resultSet5.next());

        ResultSet resultSet6 = testUtil.getTmTwoQueryResult("select * from studenci where wydzial='ieit'");
        assertFalse(resultSet6.next());

        ResultSet resultSet7 = testUtil.getTmOneQueryResult("SELECT COUNT(*) AS total FROM klienci WHERE nazwa='Lech Balcerowicz'");
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

        ResultSet resultSet1 = testUtil.getTmOneQueryResult("select * from zamowienia where opis='update successful'");
        assertTrue(resultSet1.next());

        ResultSet resultSet2 = testUtil.getTmTwoQueryResult("select * from oceny where idoceny=16");
        assertTrue(resultSet2.next());

        ResultSet resultSet3 = testUtil.getTmOneQueryResult("SELECT COUNT(*) AS total FROM klienci WHERE nazwa='Lech Balcerowicz'");
        resultSet3.next();
        assertEquals(3, resultSet3.getInt("total"));

        ResultSet resultSet4 = testUtil.getTmTwoQueryResult("select * from studenci where wydzial='imir'");
        assertFalse(resultSet4.next());

        ResultSet resultSet5 = testUtil.getTmOneQueryResult("select * from zamowienia where idzamowienia=17;");
        assertTrue(resultSet5.next());


    }

    private void assertSuccessfulQueriesOnTmOne() throws SQLException {
        ResultSet resultSet = testUtil.getTmOneQueryResult("SELECT * FROM klienci WHERE idklienta=16 AND nazwa='Pariusz Dalka' AND miejscowosc='Krakow' AND telefon='666 666 666';");
        assertTrue(resultSet.next());

        ResultSet resultSet1 = testUtil.getTmOneQueryResult("SELECT * FROM klienci WHERE idklienta=23 AND nazwa='Rollback Successful' AND miejscowosc='PSQL' AND telefon='010 001 100';");
        assertTrue(resultSet1.next());

        ResultSet resultSet2 = testUtil.getTmOneQueryResult("SELECT COUNT(*) AS total FROM klienci WHERE nazwa='Lech Balcerowicz';");
        resultSet2.next();
        assertEquals(3, resultSet2.getInt("total"));

        ResultSet resultSet3 = testUtil.getTmOneQueryResult("SELECT * FROM klienci WHERE miejscowosc='Warszawa';");
        while (resultSet3.next()) {
            assertEquals("Lech Balcerowicz", resultSet3.getString("nazwa"));
        }

        ResultSet resultSet4 = testUtil.getTmOneQueryResult("SELECT * FROM zamowienia WHERE idzamowienia!=16;");
        while (resultSet4.next()) {
            assertEquals("update successful", resultSet4.getString("opis"));
        }

        ResultSet resultSet5 = testUtil.getTmOneQueryResult("SELECT * FROM zamowienia WHERE idzamowienia=16 AND idklienta=15 AND opis='insert successful';");
        assertTrue(resultSet5.next());
    }

    private void assertSuccessfulQueriesOnTmTwo() throws SQLException {
        ResultSet resultSet = testUtil.getTmTwoQueryResult("SELECT * FROM oceny WHERE idoceny=16 AND idstudenta=8 AND przedmiot='Programownie Obiektowe' AND ocena=4.5;");
        assertTrue(resultSet.next());

        ResultSet resultSet1 = testUtil.getTmTwoQueryResult("SELECT * FROM oceny WHERE przedmiot LIKE 'Fizyka%';");
        while (resultSet1.next()) {
            assertEquals(2.0, resultSet1.getDouble("ocena"), 0.1);
        }

        ResultSet resultSet2 = testUtil.getTmTwoQueryResult("SELECT * FROM studenci WHERE wydzial='imir';");
        assertFalse(resultSet2.next());

        ResultSet resultSet3 = testUtil.getTmTwoQueryResult("SELECT * FROM studenci WHERE wydzial='ieit';");
        while (resultSet.next()) {
            assertEquals("update successful", resultSet3.getString("nazwa"));
        }
    }

    private void assertFailedQueriesOnTmOne() throws SQLException {
        ResultSet resultSet = testUtil.getTmOneQueryResult("SELECT * FROM klienci WHERE nazwa='Rollback Failed'");
        assertFalse(resultSet.next());

        ResultSet resultSet1 = testUtil.getTmOneQueryResult("SELECT * FROM klienci " +
                " WHERE idklienta=20" +
                " OR nazwa='Successful Rollback Commit'" +
                " OR miejscowosc='PSQL'" +
                " OR telefon='404 404 404';");
        assertFalse(resultSet1.next());

        ResultSet resultSet2 = testUtil.getTmOneQueryResult("SELECT * FROM klienci " +
                " WHERE idklienta=21" +
                " OR nazwa='Another Successful Commit'" +
                " OR miejscowosc='PSQL'" +
                " OR telefon='403 403 403';");
        assertFalse(resultSet2.next());

        ResultSet resultSet3 = testUtil.getTmOneQueryResult("SELECT * FROM klienci " +
                " WHERE idklienta=22" +
                " OR nazwa='Yet Another Working Insert'" +
                " OR miejscowosc='PSQL'" +
                " OR telefon='402 402 402';");
        assertFalse(resultSet3.next());

        ResultSet resultSet4 = testUtil.getTmOneQueryResult("SELECT * FROM zamowienia " +
                " WHERE idzamowienia=16" +
                " OR idklienta=20" +
                " OR opis='Stop inserting!';");
        assertFalse(resultSet4.next());

        ResultSet resultSet5 = testUtil.getTmOneQueryResult("SELECT * FROM zamowienia " +
                " WHERE idzamowienia=17" +
                " OR idklienta=21" +
                " OR opis='9/11 was an inside job';");
        assertFalse(resultSet5.next());

        ResultSet resultSet6 = testUtil.getTmOneQueryResult("SELECT * FROM zamowienia " +
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
                    .addStatement(TMTWO_QUALIFIER, "insert into studenci values" +
                        "  ('EXCEPTION THROWN', 'EXCEPTION THROWN', 'EXCEPTION THROWN', 'EXCEPTION THROWN');")
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
