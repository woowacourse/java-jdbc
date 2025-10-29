package com.interface21.jdbc.core;

import java.sql.Connection;

import javax.sql.DataSource;

public interface TransactionManager {

    void begin(DataSource dataSource);

    Connection getCurrentConnection(DataSource dataSource);

    void commit(DataSource dataSource);

    void rollback(DataSource dataSource);
}
