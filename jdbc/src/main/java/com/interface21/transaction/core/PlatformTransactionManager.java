package com.interface21.transaction.core;

public interface PlatformTransactionManager {

    void init();

    void commit();

    void rollback();
}
