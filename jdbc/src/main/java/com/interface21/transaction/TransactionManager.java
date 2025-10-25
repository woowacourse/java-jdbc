package com.interface21.transaction;

public interface TransactionManager {

    void begin();

    void commit();

    void rollback();

    void cleanup();
}
