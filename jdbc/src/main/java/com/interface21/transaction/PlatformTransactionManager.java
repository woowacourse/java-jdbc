package com.interface21.transaction;

public interface PlatformTransactionManager {

    void init();

    void commit();

    void rollback();
}
