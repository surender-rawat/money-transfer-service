package com.revolut.money.transfer.db;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * Provides a singleton object which has <code>getConnection</code> method and abstracts the application from the
 * particular database implementation.
 * <p>
 * TODO: Use the interface and provide this object into DTO class constructor directly. To be able replace the database
 * implementation easily
 */
public class H2DataSource {
    private static final Logger log = LoggerFactory.getLogger(H2DataSource.class);

    private static final HikariDataSource ds;

    static {
        ds = new HikariDataSource();
        //initializing the in-memry H2 database and initialize it by the schema and some initial data
        ds.setJdbcUrl("jdbc:h2:mem:test;" +
                "INIT=RUNSCRIPT FROM 'classpath:db_schema/schema.sql'\\;RUNSCRIPT FROM 'classpath:db_schema/init_data.sql';" +
                "TRACE_LEVEL_FILE=4");
        //TODO login and password should be provided trough system variables
        ds.setUsername("user");
        ds.setPassword("passsword");
        //We are using frequently manual transaction management in the app. So we don't want to have transaction
        //commit for each request
        ds.setAutoCommit(false);

        log.info("The database has been initialized");
    }

    private H2DataSource() {

    }

    public static DataSource getInstance(){
        return ds;
    }


}
