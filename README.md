# Transaction monitor #

Application allows to perform single transaction on several SQL-Databases.

# How to use the transaction monitor #
1. Add a dependency to your project.
2. Put the annotation `@DatabaseProperty` on any of your classes, with two initialized fields:
    - path - Path to where databases parameters are specified
    - qualifiers - Qualifiers by which databases will be differentiated.

    e.g.
    ```
    @DatabaseProperty(path = PROPERTY_SOURCE_FILE_NAME, qualifiers = {TMONE_QUALIFIER, TMTWO_QUALIFIER})
    public class Application {
        final static String PROPERTY_SOURCE_FILE_NAME = "test.properties";
        final static String TMONE_QUALIFIER = "tmone";
        final static String TMTWO_QUALIFIER = "tmtwo";

        ...
    }
    ```
3. Create a property file with database parameters. Each parameter must have a prefix that contains a database qualifier used in the program. For each parameter that is surrounded by ``${...}`` (e.g. ``${PROPERTY_NAME}``) the value will be initialized with the value of an environmental variable in your system.

    e.g.
    ```
    tmone.url=jdbc:postgresql://localhost:5432/tmone
    tmone.username=${PG_USER}
    tmone.password=${PG_PASSWORD}
    tmone.driver-class-name=org.postgresql.Driver

    tmtwo.url=jdbc:postgresql://localhost:5432/tmtwo
    tmtwo.username=${PG_USER}
    tmtwo.password=${PG_PASSWORD}
    tmtwo.driver-class-name=org.postgresql.Driver
    ```
4. Put the following line in where a transaction is needed to be performed :``TransactionService transactionService = TMConfig.boot();``.
Start a new transaction by calling: `transactionService.newTransaction()`.

    e.g.
    ```
            TransactionService transactionService = TMConfig.boot();

            transactionService.newTransaction()
                .and()
                    .begin(TMONE_QUALIFIER, TMTWO_QUALIFIER)
                .and()
                    .addStatement(TMONE_QUALIFIER, "insert into ...")
                .and()
                    .addStatement(TMTWO_QUALIFIER, new File("example.sql"))
                .and()
                    .commit();
    ```

## Testing ##    
- To perform the local tests (`LocalTest.java`), two local PSQL databases should be created, named `tmone` and `tmtwo`, and all of the required parameters (url, username, password) should be specified in the `test.properties` file.

- To perform the remote tests (`RemoteTest.java`) internet connection is required. The remote host contains a **MS Azure** database and a **MySql** database.

## Programming patterns ##
Programmings patterns that are used:
1. Command
    Implemented in ``DatabaseCommand.java`` by separating each statement added by the user to a single command that can be executed on a specified database.
2. Pool
    Implemented in ``ConnectionPool.java`` to access the database via a JDBC connection that is represented as a class, and their particular instances are stored in the Connection Pool. Max pool size is 10 connections. If all connections are in use, the method call will wait until one of the other connections is released back to the pool.
3. Unit of Work
    Used in ``Transaction.java`` to guarantee all of **ACID** *(Atomicity, Consistency, Isolation, Durability)* principles to be followed.
