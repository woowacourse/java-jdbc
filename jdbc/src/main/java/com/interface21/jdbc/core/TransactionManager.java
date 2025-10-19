package com.interface21.jdbc.core;

import java.sql.Connection;

public interface TransactionManager {

    void begin();

    Connection getCurrentConnection();

    void commit();

    void rollback();
}
