package com.interface21.jdbc.datasource;

public interface TransactionManager {

    void begin();

    void commit();

    void rollback();
}
