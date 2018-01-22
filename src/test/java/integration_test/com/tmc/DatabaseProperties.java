package integration_test.com.tmc;

abstract class DatabaseProperties {

    abstract class Qualifiers {

        final static String TMONE_QUALIFIER = "tmone";
        final static String TMTWO_QUALIFIER = "tmtwo";
        final static String TMTHREE_QUALIFIER = "tmthree";
        final static String TMFOUR_QUALIFIER = "tmfour";
    }

    final static String PROPERTY_SOURCE_FILE_NAME = "test.properties";

    final static String TMONE_URL = "jdbc:postgresql://localhost:5432/tmone";
    final static String TMONE_USER = System.getenv().get("PG_USER");
    final static String TMONE_PASSWORD = System.getenv().get("PG_PASSWORD");
    final static String TMONE_DRIVER = "org.postgresql.Driver";

    final static String TMTWO_URL = "jdbc:postgresql://localhost:5432/tmtwo";
    final static String TMTWO_USER = System.getenv().get("PG_USER");
    final static String TMTWO_PASSWORD = System.getenv().get("PG_PASSWORD");
    final static String TMTWO_DRIVER = "org.postgresql.Driver";

    final static String TMTHREE_URL = "jdbc:mysql://sql11.freemysqlhosting.net:3306/sql11214352";
    final static String TMTHREE_USER = "sql11214352";
    final static String TMTHREE_PASSWORD = "4DgP52934w";
    final static String TMTHREE_DRIVER = "com.mysql.jdbc.Driver";

    final static String TMFOUR_URL = "jdbc:sqlserver://omegadb.database.windows.net:1433;database=OmegaDB;user=omega;password=@dmin1234;encrypt=true;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";
    final static String TMFOUR_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
}
