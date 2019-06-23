package com.revolut.money.transfer.db;

import javax.sql.DataSource;

/**
 * This class provides various DataSources
 */
public class DataSourceFactory {


    /**
     * This method provides H2DataSource
     * @return
     */
    public static DataSource getH2DataSource(){
            return H2DataSource.getInstance();
    }

    /**
     * This method provides MySQLDataSource. Right now It is returning same H2DataSource
     * @return
     */
    public static DataSource getMySQLDataSource(){
        return H2DataSource.getInstance(); // AS OF NOW putting  H2DataSource only but it can be replaced by Oracle DS
    }
}
