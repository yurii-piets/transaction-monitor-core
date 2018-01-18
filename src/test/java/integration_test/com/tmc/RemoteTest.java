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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@DatabaseProperty(path = RemoteTest.PROPERTY_SOURCE_FILE_NAME, qualifiers = {RemoteTest.TMTHREE_QUALIFIER, RemoteTest.TMFOUR_QUALIFIER})
public class RemoteTest {
    static final String PROPERTY_SOURCE_FILE_NAME = "test.properties";
    static final String TMTHREE_QUALIFIER = "tmthree";
    static final String TMFOUR_QUALIFIER = "tmfour";

    private final Path pathInit = Paths.get(getClass().getClassLoader().getResource("sql/ms-azure/init_db.sql").toURI());
    private final Path pathCommit = Paths.get(getClass().getClassLoader().getResource("sql/ms-azure/commit_db.sql").toURI());
    private final Path pathRollback = Paths.get(getClass().getClassLoader().getResource("sql/ms-azure/rollback_db.sql").toURI());

    private static final TransactionService transactionService = TMConfig.boot();

    private final TMTestUtil testUtil = new TMTestUtil();

    public RemoteTest() throws URISyntaxException, SQLException {}

    @Before
    public void before() throws IOException {
        transactionService.newTransaction()
            .and()
                .begin(TMFOUR_QUALIFIER)
            .and()
                .addStatement(TMFOUR_QUALIFIER, pathInit)
            .and()
                .commit();
    }

    @Test
    public void runSuccessfulQueries() throws SQLException {
        String insertQueryTmThree = "insert into zamowienia values (16, 15, 'insert successful');";
        String updateQueryTmThree = "update klienci set nazwa='update successful' where miejscowosc='Warszawa';";
        String deleteQueryTmThree = "delete from klienci";

        Transaction transaction = transactionService.newTransaction();
        transaction.begin(TMFOUR_QUALIFIER);

        transaction.addStatement(TMFOUR_QUALIFIER, insertQueryTmThree);

        transaction.addStatement(TMFOUR_QUALIFIER, updateQueryTmThree);

        transaction.addStatement(TMFOUR_QUALIFIER, deleteQueryTmThree);

        transaction.commit();

        ResultSet resultSet = testUtil.getTmFourQueryResult(
                "SELECT * FROM zamowienia WHERE idzamowienia=16 AND idklienta=15 AND opis='insert successful';");
        assertTrue(resultSet.next());

        ResultSet resultSet2 = testUtil.getTmFourQueryResult("SELECT * FROM klienci;");
        assertFalse(resultSet2.next());
    }

}
