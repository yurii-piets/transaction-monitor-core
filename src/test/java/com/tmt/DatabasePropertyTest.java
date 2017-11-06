package com.tmt;


import com.tmc.annotation.DatabaseProperty;
import com.tmc.TMConfig;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.Set;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;

@DatabaseProperty("test.properties")
public class DatabasePropertyTest {

    private static TMConfig tmConfig;

    @BeforeClass
    public static void initTMConfig() {
        tmConfig = new TMConfig();
        tmConfig.init(new String[]{});
    }

    @After
    public void clearFiles(){
        tmConfig.getConfigurationFiles().clear();
    }

    @Test
    public void testOneConfigurationFile() {
        Set<File> files = tmConfig.getConfigurationFiles();

        files.remove(null);

        assertFalse(files.isEmpty());
        assertEquals(1, files.size());
    }
}
