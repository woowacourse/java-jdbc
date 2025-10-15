package com.interface21.transaction.support;

public interface TransactionManager {

    void begin();

    void commit();

    void rollback();
}
