package integration_test.com.tmc;

public abstract class ConnectionProperties {

    public final static String PROPERTY_SOURCE_FILE_NAME = "test.properties";
    public final static String TMONE_QUALIFIER = "tmone";
    public final static String TMTWO_QUALIFIER = "tmtwo";

    public static final String TMTHREE_QUALIFIER = "tmthree";
    public static final String TMFOUR_QUALIFIER = "tmfour";

    public static final String TMONE_URL = "jdbc:postgresql://localhost:5432/tmone";
    public static final String TMONE_USER = System.getenv().get("PG_USER");
    public static final String TMONE_PASSWORD = System.getenv().get("PG_PASSWORD");
    public static final String TMONE_DRIVER = "org.postgresql.Driver";

    public static final String TMTWO_URL = "jdbc:postgresql://localhost:5432/tmtwo";
    public static final String TMTWO_USER = System.getenv().get("PG_USER");
    public static final String TMTWO_PASSWORD = System.getenv().get("PG_PASSWORD");
    public static final String TMTWO_DRIVER = "org.postgresql.Driver";

    public static final String TMTHREE_URL = "jdbc:mysql://sql11.freemysqlhosting.net:3306/sql11214352";
    public static final String TMTHREE_USER = "sql11214352";
    public static final String TMTHREE_PASSWORD = "4DgP52934w";
    public static final String TMTHREE_DRIVER = "com.mysql.jdbc.Driver";

    public static final String TMFOUR_URL = "jdbc:sqlserver://omegadb.database.windows.net:1433;database=OmegaDB;user=omega;password=@dmin1234;encrypt=true;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";
    public static final String TMFOUR_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
}
