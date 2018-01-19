package integration_test.com.tmc.remote;

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

@DatabaseProperty(path = RemoteTest.PROPERTY_SOURCE_FILE_NAME, qualifiers = {RemoteTest.TMTHREE_QUALIFIER, RemoteTest.TMFOUR_QUALIFIER})
public class RemoteTest {
    static final String PROPERTY_SOURCE_FILE_NAME = "test.properties";
    static final String TMTHREE_QUALIFIER = "tmthree";
    static final String TMFOUR_QUALIFIER = "tmfour";

    private final Path pathInit3 = Paths.get(getClass().getClassLoader().getResource("sql/mysql/init_db.sql").toURI());
    private final Path pathCommit3 = Paths.get(getClass().getClassLoader().getResource("sql/mysql/commit_db.sql").toURI());
    private final Path pathRollback3 = Paths.get(getClass().getClassLoader().getResource("sql/mysql/rollback_db.sql").toURI());

    private final Path pathInit4 = Paths.get(getClass().getClassLoader().getResource("sql/ms-azure/init_db.sql").toURI());
    private final Path pathCommit4 = Paths.get(getClass().getClassLoader().getResource("sql/ms-azure/commit_db.sql").toURI());
    private final Path pathRollback4 = Paths.get(getClass().getClassLoader().getResource("sql/ms-azure/rollback_db.sql").toURI());

    private static final TransactionService transactionService = TMConfig.boot();

    private final RemoteTestUtil testUtil = new RemoteTestUtil();

    public RemoteTest() throws URISyntaxException, SQLException {}

    @Before
    public void before() throws IOException {
        transactionService.newTransaction()
            .and()
                .begin(TMTHREE_QUALIFIER, TMFOUR_QUALIFIER)
            .and()
                .addStatement(TMTHREE_QUALIFIER, pathInit3)
            .and()
                .addStatement(TMFOUR_QUALIFIER, pathInit4)
            .and()
                .commit();
    }

    @Test
    public void runSuccessfulQueries() throws SQLException {
        String insertQueryTmThree = "insert into oceny values (16, 8, 'insert successful', 4.5);";
        String insertQueryTmFour = "insert into zamowienia values (16, 15, 'insert successful');";

        String updateQueryTmThree = "update oceny set ocena=6.0 where przedmiot like 'Fizyka%';";
        String updateQueryTmFour = "update klienci set nazwa='update successful' where miejscowosc='Warszawa';";

        String deleteQueryTmThree = "delete from studenci where wydzial='imir';";
        String deleteQueryTmFour = "delete from klienci";

        Transaction transaction = transactionService.newTransaction();
        transaction.begin(TMTHREE_QUALIFIER, TMFOUR_QUALIFIER);

        transaction.addStatement(TMTHREE_QUALIFIER, insertQueryTmThree);
        transaction.addStatement(TMFOUR_QUALIFIER, insertQueryTmFour);

        transaction.addStatement(TMTHREE_QUALIFIER, updateQueryTmThree);
        transaction.addStatement(TMFOUR_QUALIFIER, updateQueryTmFour);

        transaction.addStatement(TMTHREE_QUALIFIER, deleteQueryTmThree);
        transaction.addStatement(TMFOUR_QUALIFIER, deleteQueryTmFour);

        transaction.commit();

        ResultSet resultSet = testUtil.getTmFourQueryResult("SELECT * FROM zamowienia WHERE idzamowienia=16 AND idklienta=15 AND opis='insert successful';");
        assertTrue(resultSet.next());

        ResultSet resultSet1 = testUtil.getTmThreeQueryResult("SELECT * FROM oceny WHERE idoceny=16 AND idstudenta=8 AND przedmiot='insert successful' AND ocena='4.5';");
        assertTrue(resultSet1.next());

        ResultSet resultSet2 = testUtil.getTmFourQueryResult("SELECT * FROM klienci;");
        assertFalse(resultSet2.next());

        ResultSet resultSet3 = testUtil.getTmThreeQueryResult("SELECT * FROM oceny WHERE przedmiot like 'Fizyka%';");

        while (resultSet3.next()) {
            assertEquals(6.0, resultSet3.getDouble("ocena"), 0.1);
        }

        ResultSet resultSet4 = testUtil.getTmThreeQueryResult("SELECT * FROM studenci WHERE wydzial='imir';");
        assertFalse(resultSet4.next());
    }

    @Test
    public void runSuccessfulQueriesFromFiles() throws IOException, SQLException {
        transactionService.newTransaction()
            .and()
                .begin(TMTHREE_QUALIFIER, TMFOUR_QUALIFIER)
            .and()
                .addStatement(TMTHREE_QUALIFIER, pathCommit3)
            .and()
                .addStatement(TMFOUR_QUALIFIER, pathCommit4.toFile())
            .and()
                .commit();

        assertSuccessfulQueriesOnTmThree();
        assertSuccessfulQueriesOnTmFour();
    }

    @Test
    public void runFirstFailedSecondSuccessfulQueriesFromFiles() throws IOException, SQLException {
        transactionService.newTransaction()
            .and()
                .begin(TMTHREE_QUALIFIER, TMFOUR_QUALIFIER)
            .and()
                .addStatement(TMTHREE_QUALIFIER, pathRollback3)
            .and()
                .addStatement(TMFOUR_QUALIFIER, pathCommit4)
            .and()
                .commit();

        assertFailedQueriesOnTmThree();
        assertFailedQueriesOnTmFour();
    }

    @Test
    public void runFirstSuccessSecondFailedQueriesFromFiles() throws IOException, SQLException {
        transactionService.newTransaction()
            .and()
                .begin(TMTHREE_QUALIFIER, TMFOUR_QUALIFIER)
            .and()
                .addStatement(TMTHREE_QUALIFIER, pathCommit3)
            .and()
                .addStatement(TMFOUR_QUALIFIER, pathRollback4)
            .and()
                .commit();

        assertFailedQueriesOnTmThree();
        assertFailedQueriesOnTmFour();
    }

    @Test
    public void runFailedQueriesFromFile() throws IOException, SQLException {
        transactionService.newTransaction()
            .and()
                .begin(TMTHREE_QUALIFIER, TMFOUR_QUALIFIER)
            .and()
                .addStatement(TMTHREE_QUALIFIER, pathRollback3)
            .and()
                .addStatement(TMFOUR_QUALIFIER, pathRollback4)
            .and()
                .commit();

        assertFailedQueriesOnTmThree();
        assertFailedQueriesOnTmFour();
    }

    @Test
    public void runEmptyTransactions() {
        Transaction transaction1 = transactionService.newTransaction();
        Transaction transaction2 = transactionService.newTransaction();

        transaction1.begin(TMTHREE_QUALIFIER, TMFOUR_QUALIFIER);
        transaction2.begin(TMTHREE_QUALIFIER, TMFOUR_QUALIFIER);

        transaction2.commit();
        transaction1.commit();
    }

    @Test
    public void runEmptyQuery() {
        transactionService.newTransaction()
                .begin(TMTHREE_QUALIFIER, TMFOUR_QUALIFIER)
            .and()
                .addStatement(TMFOUR_QUALIFIER, "")
            .and()
                .commit();
    }

    @Test
    public void runFirstFailedAndSecondSuccessfulMixedTransactions() throws SQLException {
        Transaction transaction1 = transactionService.newTransaction();
        Transaction transaction2 = transactionService.newTransaction();

        transaction1.begin(TMTHREE_QUALIFIER, TMFOUR_QUALIFIER);
        transaction2.begin(TMTHREE_QUALIFIER, TMFOUR_QUALIFIER);

        transaction1
                .addStatement(TMFOUR_QUALIFIER,
                        "insert into klienci values(77, 'Test Failed', 'Test Failed', '000 000 000');")
                .and()
                .addStatement(TMTHREE_QUALIFIER, "delete from studenci where idstudenta = 7");

        transaction2
                .addStatement(TMTHREE_QUALIFIER,
                        "delete from oceny where przedmiot='Podstawy Elektroniki Cyfrowej' and idstudenta=8;")
                .and()
                .addStatement(TMFOUR_QUALIFIER, "insert into zamowienia values(16, 1, 'Test Successful');");

        transaction1
                .addStatement(TMFOUR_QUALIFIER, "insert into klienci values('Exception thrown', 'Test Failed', 'Test Failed', '000 000 000');");

        transaction2.commit();

        ResultSet resultSet1 = testUtil.getTmFourQueryResult("select * from klienci where idklienta=77");
        assertFalse(resultSet1.next());

        ResultSet resultSet2 = testUtil.getTmThreeQueryResult("select * from studenci where idstudenta=7");
        assertTrue(resultSet2.next());

        ResultSet resultSet3 = testUtil.getTmThreeQueryResult("select * from oceny where przedmiot='Podstawy Elektroniki Cyfrowej' and idstudenta=8;");
        assertFalse(resultSet3.next());

        ResultSet resultSet4 = testUtil.getTmFourQueryResult("select * from zamowienia where idzamowienia=16;");
        assertTrue(resultSet4.next());

        transaction1.commit();

        ResultSet resultSet5 = testUtil.getTmFourQueryResult("select * from klienci where idklienta=77");
        assertFalse(resultSet5.next());

        ResultSet resultSet6 = testUtil.getTmThreeQueryResult("select * from studenci where idstudenta=7");
        assertTrue(resultSet6.next());
    }

    @Test
    public void runFirstSuccessfulAndSecondSuccessfulMixed() throws SQLException {
        Transaction transaction1 = transactionService.newTransaction();
        Transaction transaction2 = transactionService.newTransaction();

        transaction1.begin(TMTHREE_QUALIFIER, TMFOUR_QUALIFIER);
        transaction2.begin(TMTHREE_QUALIFIER, TMFOUR_QUALIFIER);

        transaction1.addStatement(TMFOUR_QUALIFIER, "update zamowienia set opis='update successful';");
        transaction1.addStatement(TMTHREE_QUALIFIER, "update studenci set nazwa='update successful' where wydzial='ieit';");

        transaction2.addStatement(TMFOUR_QUALIFIER, "insert into zamowienia values (16, 15, 'insert successful');");
        transaction2.addStatement(TMTHREE_QUALIFIER, "update oceny set ocena=1.0 where przedmiot like 'Fizyka%';");

        transaction1.commit();

        ResultSet resultSet1 = testUtil.getTmFourQueryResult("select count(*) as total from zamowienia where opis='update successful'");
        resultSet1.next();
        assertEquals(14, resultSet1.getInt("total"));

        ResultSet resultSet2 = testUtil.getTmThreeQueryResult("select nazwa from studenci where wydzial='ieit';");
        while (resultSet2.next()){
            assertEquals("update successful", resultSet2.getString("nazwa"));
        }

        ResultSet resultSet3 = testUtil.getTmFourQueryResult("select * from zamowienia where idzamowienia=16;");
        assertFalse(resultSet3.next());

        ResultSet resultSet4 = testUtil.getTmThreeQueryResult("select ocena from oceny where przedmiot like 'Fizyka%'");
        while(resultSet4.next()){
            assertNotEquals(1.0, resultSet4.getDouble("ocena"), 0.1);
        }

        transaction2.commit();

        ResultSet resultSet5 = testUtil.getTmFourQueryResult("select count(*) as total from zamowienia where opis='update successful'");
        resultSet5.next();
        assertEquals(14, resultSet5.getInt("total"));

        ResultSet resultSet6 = testUtil.getTmThreeQueryResult("select nazwa from studenci where wydzial='ieit';");
        while (resultSet6.next()){
            assertEquals("update successful", resultSet6.getString("nazwa"));
        }

        ResultSet resultSet7 = testUtil.getTmFourQueryResult("select * from zamowienia where idzamowienia=16;");
        assertTrue(resultSet7.next());

        ResultSet resultSet8 = testUtil.getTmThreeQueryResult("select ocena from oceny where przedmiot like 'Fizyka%'");
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

        ResultSet resultSet1 = testUtil.getTmFourQueryResult("select * from zamowienia where opis='update successful'");
        ResultSet resultSet2 = testUtil.getTmFourQueryResult("select * from zamowienia where opis='trans2 update successful'");
        assertTrue(resultSet1.next() || resultSet2.next() );

        ResultSet resultSet3 = testUtil.getTmThreeQueryResult("select * from oceny where idoceny=16");
        assertTrue(resultSet3.next());

        ResultSet resultSet4 = testUtil.getTmFourQueryResult("select * from zamowienia where idzamowienia=17;");
        assertTrue(resultSet4.next());

        ResultSet resultSet5 = testUtil.getTmThreeQueryResult("select * from studenci where nazwa='update successful'");
        assertFalse(resultSet5.next());

        ResultSet resultSet6 = testUtil.getTmThreeQueryResult("select * from studenci where wydzial='ieit'");
        assertFalse(resultSet6.next());

        ResultSet resultSet7 = testUtil.getTmFourQueryResult("SELECT COUNT(*) AS total FROM klienci WHERE nazwa='Lech Balcerowicz'");
        resultSet7.next();
        assertEquals(3, resultSet7.getInt("total"));
    }

    @Test
    public void runSuccessfulAndFailedTransactionsInDifferentThreads() throws SQLException, InterruptedException {
        Thread thread1 = getMixedThreadsTestFailingThread();
        Thread thread2 = getMixedThreadsTestSuccessfulThread();

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        ResultSet resultSet1 = testUtil.getTmFourQueryResult("select * from zamowienia where opis='update successful'");
        assertTrue(resultSet1.next());

        ResultSet resultSet2 = testUtil.getTmThreeQueryResult("select * from oceny where idoceny=16");
        assertTrue(resultSet2.next());

        ResultSet resultSet3 = testUtil.getTmFourQueryResult("SELECT COUNT(*) AS total FROM klienci WHERE nazwa='Lech Balcerowicz'");
        resultSet3.next();
        assertEquals(3, resultSet3.getInt("total"));

        ResultSet resultSet4 = testUtil.getTmThreeQueryResult("select * from studenci where wydzial='imir'");
        assertFalse(resultSet4.next());

        ResultSet resultSet5 = testUtil.getTmFourQueryResult("select * from zamowienia where idzamowienia=17;");
        assertTrue(resultSet5.next());


    }

    private void assertSuccessfulQueriesOnTmThree() throws SQLException {
        ResultSet resultSet = testUtil.getTmThreeQueryResult("SELECT * FROM oceny WHERE idoceny=16 AND idstudenta=8 AND przedmiot='Programownie Obiektowe' AND ocena=4.5;");
        assertTrue(resultSet.next());

        ResultSet resultSet1 = testUtil.getTmThreeQueryResult("SELECT * FROM oceny WHERE przedmiot LIKE 'Fizyka%';");
        while (resultSet1.next()) {
            assertEquals(2.0, resultSet1.getDouble("ocena"), 0.1);
        }

        ResultSet resultSet2 = testUtil.getTmThreeQueryResult("SELECT * FROM studenci WHERE wydzial='imir';");
        assertFalse(resultSet2.next());

        ResultSet resultSet3 = testUtil.getTmThreeQueryResult("SELECT * FROM studenci WHERE wydzial='ieit';");
        while (resultSet.next()) {
            assertEquals("update successful", resultSet3.getString("nazwa"));
        }
    }

    private void assertSuccessfulQueriesOnTmFour() throws SQLException {
        ResultSet resultSet = testUtil.getTmFourQueryResult("SELECT * FROM klienci WHERE idklienta=16 AND nazwa='Pariusz Dalka' AND miejscowosc='Krakow' AND telefon='666 666 666';");
        assertTrue(resultSet.next());

        ResultSet resultSet1 = testUtil.getTmFourQueryResult("SELECT * FROM klienci WHERE idklienta=23 AND nazwa='Rollback Successful' AND miejscowosc='PSQL' AND telefon='010 001 100';");
        assertTrue(resultSet1.next());

        ResultSet resultSet2 = testUtil.getTmFourQueryResult("SELECT COUNT(*) AS total FROM klienci WHERE nazwa='Lech Balcerowicz';");
        resultSet2.next();
        assertEquals(3, resultSet2.getInt("total"));

        ResultSet resultSet3 = testUtil.getTmFourQueryResult("SELECT * FROM klienci WHERE miejscowosc='Warszawa';");
        while (resultSet3.next()) {
            assertEquals("Lech Balcerowicz", resultSet3.getString("nazwa"));
        }

        ResultSet resultSet4 = testUtil.getTmFourQueryResult("SELECT * FROM zamowienia WHERE idzamowienia!=16;");
        while (resultSet4.next()) {
            assertEquals("update successful", resultSet4.getString("opis"));
        }

        ResultSet resultSet5 = testUtil.getTmFourQueryResult("SELECT * FROM zamowienia WHERE idzamowienia=16 AND idklienta=15 AND opis='insert successful';");
        assertTrue(resultSet5.next());
    }

    private void assertFailedQueriesOnTmThree() throws SQLException {
        ResultSet resultSet8 = testUtil.getTmThreeQueryResult("SELECT * FROM studenci WHERE nazwa='rollback failed'");
        assertFalse(resultSet8.next());

        ResultSet resultSet9 = testUtil.getTmThreeQueryResult("SELECT * FROM studenci" +
                " WHERE idstudenta='20'" +
                " OR nazwa='Should Not Exist'" +
                " OR wydzial='human'" +
                " OR wiek=6.0;");
        assertFalse(resultSet9.next());


        ResultSet resultSet10 = testUtil.getTmThreeQueryResult("SELECT * FROM studenci" +
                " WHERE idstudenta=21" +
                " OR nazwa='Should Not Exist'" +
                " OR wydzial='human'" +
                " OR wiek=5.5;");
        assertFalse(resultSet10.next());

        ResultSet resultSet11 = testUtil.getTmThreeQueryResult("SELECT COUNT(*) AS total FROM studenci WHERE wydzial='eaiiib';");
        resultSet11.next();
        assertEquals(5, resultSet11.getInt("total"));

        ResultSet resultSet12 = testUtil.getTmThreeQueryResult("SELECT * FROM studenci " +
                " WHERE nazwa='EXCEPTION THROWN'" +
                " OR wydzial='EXCEPTION THROWN';");
        assertFalse(resultSet12.next());
    }

    private void assertFailedQueriesOnTmFour() throws SQLException {
        ResultSet resultSet = testUtil.getTmFourQueryResult("SELECT * FROM klienci WHERE nazwa='Rollback Failed'");
        assertFalse(resultSet.next());

        ResultSet resultSet1 = testUtil.getTmFourQueryResult("SELECT * FROM klienci " +
                " WHERE idklienta=20" +
                " OR nazwa='Successful Rollback Commit'" +
                " OR miejscowosc='PSQL'" +
                " OR telefon='404 404 404';");
        assertFalse(resultSet1.next());

        ResultSet resultSet2 = testUtil.getTmFourQueryResult("SELECT * FROM klienci " +
                " WHERE idklienta=21" +
                " OR nazwa='Another Successful Commit'" +
                " OR miejscowosc='PSQL'" +
                " OR telefon='403 403 403';");
        assertFalse(resultSet2.next());

        ResultSet resultSet3 = testUtil.getTmFourQueryResult("SELECT * FROM klienci " +
                " WHERE idklienta=22" +
                " OR nazwa='Yet Another Working Insert'" +
                " OR miejscowosc='PSQL'" +
                " OR telefon='402 402 402';");
        assertFalse(resultSet3.next());

        ResultSet resultSet4 = testUtil.getTmFourQueryResult("SELECT * FROM zamowienia " +
                " WHERE idzamowienia=16" +
                " OR idklienta=20" +
                " OR opis='Stop inserting!';");
        assertFalse(resultSet4.next());

        ResultSet resultSet5 = testUtil.getTmFourQueryResult("SELECT * FROM zamowienia " +
                " WHERE idzamowienia=17" +
                " OR idklienta=21" +
                " OR opis='9/11 was an inside job';");
        assertFalse(resultSet5.next());

        ResultSet resultSet6 = testUtil.getTmFourQueryResult("SELECT * FROM zamowienia " +
                " WHERE idzamowienia=18" +
                " OR idklienta=22" +
                " OR opis='¯\\_(ツ)_/¯';");
        assertFalse(resultSet6.next());
    }

    private Thread getBothThreadsSuccessfulTestThreadOne() {
        return new Thread(() -> transactionService
                .newTransaction()
            .and()
                .begin(TMTHREE_QUALIFIER, TMFOUR_QUALIFIER)
            .and()
                .addStatement(TMFOUR_QUALIFIER, "update zamowienia set opis='trans2 update successful';")
            .and()
                .addStatement(TMTHREE_QUALIFIER, "update studenci set nazwa='update successful' where wydzial='ieit';")
            .and()
                .commit()
        );
    }

    private Thread getBothThreadsSuccessfulTestThreadTwo() {
        return new Thread( () -> transactionService
                .newTransaction()
            .and()
                .begin(TMTHREE_QUALIFIER, TMFOUR_QUALIFIER)
            .and()
                .addStatement(TMFOUR_QUALIFIER, "update zamowienia set opis='update successful';")
            .and()
                .addStatement(TMTHREE_QUALIFIER, "insert into oceny values (16, 8, 'Programownie Obiektowe', 4.5);")
            .and()
                .addStatement(TMFOUR_QUALIFIER, "update klienci set nazwa='Lech Balcerowicz' where miejscowosc='Warszawa';")
            .and()
                .addStatement(TMTHREE_QUALIFIER,"delete from studenci where wydzial='ieit';")
            .and()
                .addStatement(TMFOUR_QUALIFIER,"insert into zamowienia values(17, 14, 'another succ');")
            .and()
                .commit()
        );
    }

    private Thread getMixedThreadsTestFailingThread() {
        return new Thread( () -> transactionService
                .newTransaction()
            .and()
                .begin(TMFOUR_QUALIFIER, TMTHREE_QUALIFIER)
            .and()
                .addStatement(TMFOUR_QUALIFIER, "update zamowienia set opis='rollback';")
            .and()
                .addStatement(TMTHREE_QUALIFIER, "insert into studenci values" +
                        "  ('EXCEPTION THROWN', 'EXCEPTION THROWN', 'EXCEPTION THROWN', 'EXCEPTION THROWN');")
            .and()
                .commit()
        );
    }

    private Thread getMixedThreadsTestSuccessfulThread() {
        return new Thread( () -> transactionService
                .newTransaction()
            .and()
                .begin(TMFOUR_QUALIFIER, TMTHREE_QUALIFIER)
            .and()
                .addStatement(TMTHREE_QUALIFIER,"delete from studenci where wydzial='imir';")
            .and()
                .addStatement(TMTHREE_QUALIFIER, "insert into oceny values (16, 8, 'Programownie Obiektowe', 4.5);")
            .and()
                .addStatement(TMFOUR_QUALIFIER, "update klienci set nazwa='Lech Balcerowicz' where miejscowosc='Warszawa';")
            .and()
                .addStatement(TMFOUR_QUALIFIER, "update zamowienia set opis='update successful';")
            .and()
                .addStatement(TMFOUR_QUALIFIER,"insert into zamowienia values(17, 14, 'another succ');")
            .and()
                .commit()
        );
    }
}
