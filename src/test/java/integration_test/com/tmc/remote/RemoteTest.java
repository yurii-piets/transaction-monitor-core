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
}
