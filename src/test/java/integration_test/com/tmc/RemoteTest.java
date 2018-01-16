package integration_test.com.tmc;

import com.tmc.connection.annotation.DatabaseProperty;
import org.junit.Test;

import static org.junit.Assert.fail;

@DatabaseProperty(path = LocalTest.PROPERTY_SOURCE_FILE_NAME, qualifiers = {RemoteTest.TMTHREE_QUALIFIER, RemoteTest.TMFOUR_QUALIFIER})
public class RemoteTest {
    static final String TMTHREE_QUALIFIER = "tmthree";
    static final String TMFOUR_QUALIFIER = "tmfour";

    @Test
    public void test(){
        fail();
    }
}
