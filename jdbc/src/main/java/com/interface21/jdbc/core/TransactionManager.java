package com.interface21.jdbc.core;

import java.sql.Connection;

public interface TransactionManager {

    void begin();

    Connection getCurrentConnection();

    Connection getOrCreateConnection();

    void commit();

    void rollback();
}
