package com.techcourse.service;

public interface TransactionManager {

    void begin();

    void commit();

    void rollback();
}
