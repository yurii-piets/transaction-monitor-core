# Transaction monitor #

Applications allows to perform single transaction on several SQL-Databases.

To use Transaction monitor:
1. Add dependency to your project.
2. Put annotation `@DatabaseProperty` on any of your classes, with initialized two fields:
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
3. Create property file with database parameters. Each parameter must have a prefix that contains database qualifier used in the program. For each parameter that is surrounded ``${...}`` (e.g. ``${PROPERTY_NAME}``) value will be initialized as the same named environmental variable in your system.  
    
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
4. Put this line were transaction is needed to be performed :``TransactionService transactionService = TMConfig.boot();``.
Start new transaction by calling: `transactionService.newTransaction()`.

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
- To perform local tests (`LocalTest.java`), should be created two local PSQL databases, named `tmone` and `tmtwo`, and all required parameters should be specified in `test.properties` (url,username, password).

- To perform remote tests (`RemoteTest.java`) internet connection is required. On remote host are located **MS Azure** database and **MySql** database.

##Programming patterns ##
Programmings patterns that are used:
1. Command
    As ``DatabaseCommnad.java`` to separated each statement added by user to single command that can be executed on specified database.
2. Pool
    As ``ConnectionPool.java`` to access the database via jdbc connection, that are represented as class, and instances are stored in Connection Pool. Max pool size is 10 connection. If all connection are in use, method call will wait until one of anther connection will be released back to poll.
3. Unity of Work
    As ``Transaction.java`` to guarantee all of **ACID** *(Atomicity, Consistency, Isolation, Durability)* principles to be followed. 